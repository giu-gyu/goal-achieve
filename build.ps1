param([switch]$TestOnly)
$ErrorActionPreference = 'Stop'
$taskOriginalRoot = $PSScriptRoot
$taskRoot = $PSScriptRoot
$taskDrive = $null
try {
    # Native Android tools on Windows sometimes fail on Korean paths.
    if ($taskRoot -match '[^\x00-\x7F]') {
        foreach ($taskLetter in @('R','S','T','U','V','W','X','Y','Z')) {
            $taskCandidate = ('{0}:' -f $taskLetter)
            if (!(Test-Path -LiteralPath ($taskCandidate + '\'))) {
                & subst.exe $taskCandidate $taskRoot
                if ($LASTEXITCODE -ne 0) { throw 'Unable to create a temporary ASCII drive for the build.' }
                $taskDrive = $taskCandidate
                $taskRoot = $taskCandidate + '\'
                break
            }
        }
        if (!$taskDrive) { throw 'No spare drive letter is available for the build.' }
    }
    Set-Location -LiteralPath $taskRoot
    $taskJdk = Get-ChildItem -LiteralPath (Join-Path $taskRoot '.tools/jdk') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($taskJdk) { $env:JAVA_HOME = $taskJdk.FullName }
    $env:GRADLE_USER_HOME = Join-Path $taskRoot '.tools/gradle-cache'
    $env:ANDROID_HOME = Join-Path $taskRoot '.tools/android-sdk'
    if (!(Test-Path -LiteralPath "$env:JAVA_HOME/bin/java.exe")) { throw 'JDK 17 is required. Set JAVA_HOME.' }
    $taskGradle = Join-Path $taskRoot '.tools/gradle/gradle-8.11.1/bin/gradle.bat'
    if (!(Test-Path -LiteralPath $taskGradle)) { throw 'Prepare Gradle 8.11.1 under .tools/gradle.' }
    $taskKey = Join-Path $taskRoot '.tools/debug.keystore'
    if (!(Test-Path -LiteralPath $taskKey)) {
        & "$env:JAVA_HOME/bin/keytool.exe" -genkeypair -keystore $taskKey -storepass android -keypass android -alias androiddebugkey -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -validity 10000
        if ($LASTEXITCODE -ne 0) { throw 'Unable to create the development signing key.' }
    }
    if ($TestOnly) { & $taskGradle testDebugUnitTest --no-daemon --console=plain }
    else { & $taskGradle assembleDebug testDebugUnitTest lintDebug wrapper --no-daemon --console=plain }
    if ($LASTEXITCODE -ne 0) { throw 'Build failed. See the errors above.' }
} finally {
    Set-Location -LiteralPath $taskOriginalRoot
    if ($taskDrive) { & subst.exe $taskDrive /D }
}
