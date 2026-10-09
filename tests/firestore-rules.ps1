# Run against the local emulator only.
$ErrorActionPreference = 'Stop'
$taskBase = 'http://127.0.0.1:8080/v1/projects/demo-together/databases/(default)/documents'
$taskPrefix = 'projects/demo-together/databases/(default)/documents/'
$taskPair = 'aabbccddeeff00112233445566778899'
function B64Url([string]$value) {
    [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($value)).TrimEnd('=').Replace('+','-').Replace('/','_')
}
function Token([string]$uid) {
    $claims = @{sub=$uid; user_id=$uid; aud='demo-together'; iss='https://securetoken.google.com/demo-together'; iat=1700000000; exp=2100000000; firebase=@{sign_in_provider='password'}} | ConvertTo-Json -Depth 10 -Compress
    (B64Url '{"alg":"none","typ":"JWT"}') + '.' + (B64Url $claims) + '.'
}
function V($value) {
    if ($null -eq $value) { return @{nullValue=$null} }
    if ($value -is [bool]) { return @{booleanValue=$value} }
    if ($value -is [string]) { return @{stringValue=$value} }
    if ($value -is [System.Collections.IDictionary]) {
        $fields = @{}
        foreach ($key in $value.Keys) { $fields[$key] = V $value[$key] }
        return @{mapValue=@{fields=$fields}}
    }
    return @{arrayValue=@{values=@($value | ForEach-Object { V $_ })}}
}
function WriteDoc([string]$path, $data) {
    $fields = @{}
    foreach ($key in $data.Keys) { $fields[$key] = V $data[$key] }
    @{update=@{name=$taskPrefix+$path; fields=$fields}}
}
$script:passed = 0
function Check([string]$name, [string]$uid, [string]$method, [string]$url, $body, [int]$expected) {
    $headers = @{}
    if ($uid) { $headers.Authorization = 'Bearer ' + (Token $uid) }
    $args = @{Uri=$url; Method=$method; Headers=$headers; UseBasicParsing=$true}
    if ($null -ne $body) { $args.Body = $body | ConvertTo-Json -Depth 30 -Compress; $args.ContentType='application/json' }
    try { $response = Invoke-WebRequest @args; $status = [int]$response.StatusCode }
    catch {
        if (!$_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
        if ($status -ne $expected) { Write-Host $_.ErrorDetails.Message }
    }
    if ($status -ne $expected) { throw "$name expected $expected, got $status" }
    $script:passed++
    Write-Host "PASS $name"
}
Invoke-WebRequest -UseBasicParsing -Method Delete -Uri 'http://127.0.0.1:8080/emulator/v1/projects/demo-together/databases/(default)/documents' | Out-Null
$commit = $taskBase + ':commit'
$pair = @{members=@('alice'); names=@{alice='Alice'}}
Check 'create pair and profile atomically' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair" $pair),(WriteDoc 'users/alice' @{pairId=$taskPair}))} 200
Check 'cannot enumerate spaces' 'mallory' 'GET' "$taskBase/pairs" $null 403
Check 'signed out cannot read invite' '' 'GET' "$taskBase/pairs/$taskPair" $null 403
Check 'invite can be read to join' 'bob' 'GET' "$taskBase/pairs/$taskPair" $null 200
$pair = @{members=@('alice','bob'); names=@{alice='Alice'; bob='Bob'}}
Check 'second member joins atomically' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair" $pair),(WriteDoc 'users/bob' @{pairId=$taskPair}))} 200
$third = @{members=@('alice','bob','mallory'); names=@{alice='Alice'; bob='Bob'; mallory='Mallory'}}
Check 'third member denied' 'mallory' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair" $third),(WriteDoc 'users/mallory' @{pairId=$taskPair}))} 403
$now = [TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, 'Korea Standard Time')
$today = $now.ToString('yyyy-MM-dd')
$memo = @{ownerId='alice'; date=$today; text='Daily memo'}
Check 'owner saves memo' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/memos/alice_$today" $memo))} 200
Check 'partner reads memo' 'bob' 'GET' "$taskBase/pairs/$taskPair/memos/alice_$today" $null 200
Check 'partner cannot overwrite memo' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/memos/alice_$today" $memo))} 403
Check 'outsider cannot read memo' 'mallory' 'GET' "$taskBase/pairs/$taskPair/memos/alice_$today" $null 403
Check 'owner saves profile' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/profiles/alice" @{bio='Hello';resolve='Every day'}))} 200
Check 'partner cannot change profile' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/profiles/alice" @{bio='Changed';resolve='Every day'}))} 403
$pair.names.alice='New Alice'
Check 'partner cannot change name' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair" $pair))} 403
Check 'owner changes name' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair" $pair))} 200
$goal = @{ownerId='alice'; title='Walk'; start=$today; end=$null}
Check 'owner creates goal' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 200
Check 'partner reads goal' 'bob' 'GET' "$taskBase/pairs/$taskPair/goals/walk" $null 200
Check 'outsider cannot read goal' 'mallory' 'GET' "$taskBase/pairs/$taskPair/goals/walk" $null 403
$entry = @{ownerId='alice'; goalId='walk'; date=$today; done=$true}
Check 'owner records completion' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/entries/walk_$today" $entry))} 200
$entry.done = $false
Check 'partner cannot edit owner record' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/entries/walk_$today" $entry))} 403
Check 'owner can correct record' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/entries/walk_$today" $entry))} 200
$future = $now.AddDays(1).ToString('yyyy-MM-dd')
$entry.date = $future
Check 'future record denied' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/entries/walk_$future" $entry))} 403
$past = $now.AddDays(-1).ToString('yyyy-MM-dd')
$entry.date = $past
Check 'before goal creation denied' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/entries/walk_$past" $entry))} 403
$goal.title = 'Updated walk'
Check 'partner cannot rename goal' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 403
Check 'owner renames goal' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 200
$goal.title = ''
Check 'blank title denied' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 403
$goal.title = 'Updated walk'
$goal.end = $today
Check 'partner cannot archive goal' 'bob' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 403
Check 'owner archives goal' 'alice' 'POST' $commit @{writes=@((WriteDoc "pairs/$taskPair/goals/walk" $goal))} 200
Check 'partner cannot delete record' 'bob' 'DELETE' "$taskBase/pairs/$taskPair/entries/walk_$today" $null 403
Check 'owner clears record' 'alice' 'DELETE' "$taskBase/pairs/$taskPair/entries/walk_$today" $null 200
Check 'partner cannot delete goal' 'bob' 'DELETE' "$taskBase/pairs/$taskPair/goals/walk" $null 403
Check 'owner deletes goal' 'alice' 'DELETE' "$taskBase/pairs/$taskPair/goals/walk" $null 200
Write-Host "$script:passed security checks passed."
