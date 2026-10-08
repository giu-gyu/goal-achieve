# 우리의 하루

두 사람이 각자 매일 실천할 목표를 만들고, 완료 여부와 지난 기록 및 주간·월간 달성률을 함께 보는 네이티브 안드로이드 앱입니다. Android 8.0 이상을 지원합니다.

## 사용 흐름

1. 두 사람 모두 각자의 이메일과 비밀번호로 가입합니다.
2. 한 사람이 이름을 입력하고 **새 커플 공간 만들기**를 누릅니다.
3. **우리** 탭에서 초대 코드를 공유합니다.
4. 상대방은 자신의 이름과 받은 코드를 입력해 **상대방 공간에 참여하기**를 누릅니다.
5. **오늘** 탭에서 매일 반복할 목표를 만들고 완료 또는 미완료를 기록합니다.
6. **기록** 탭의 날짜를 눌러 과거 날짜를 조회하거나 자신의 기록을 수정합니다.
7. **달성률** 탭에서 주간·월간을 선택하고 이전 기간도 조회합니다.

한 공간에는 두 계정만 참여할 수 있습니다. 목표와 기록은 두 사람에게 보이며, 수정은 소유자만 할 수 있습니다. 목표를 종료하면 다음 날부터 목표가 표시되지 않으며 기존 기록은 유지됩니다. 같은 계정으로 로그인하면 다른 휴대폰에서도 공간이 복원됩니다.

## 실제 사용을 위한 Firebase 설정

설정되지 않은 APK에서는 **체험해보기**만 가능합니다. 체험 기록은 메모리에만 저장되며 앱 종료 시 사라집니다. 실제 데이터를 쓰려면 아래 설정 후 APK를 다시 빌드해야 합니다.

1. [Firebase Console](https://console.firebase.google.com/)에서 **프로젝트 만들기**를 선택합니다. Analytics는 이 앱에 필수가 아닙니다.
2. 프로젝트 설정에서 Android 앱을 추가합니다.
   - Android 패키지 이름: **com.together.daily**
   - 앱 닉네임: 우리의 하루
   - 이메일·비밀번호 로그인에는 SHA 인증서 등록이 필요하지 않습니다.
3. **google-services.json 다운로드**를 누르고 파일을 이 프로젝트의 **app/google-services.json**으로 넣습니다.
4. **Authentication → 시작하기 → 로그인 방법 → 이메일/비밀번호**를 사용 설정합니다. 이메일 링크 로그인은 필요하지 않습니다.
5. **Firestore Database → 데이터베이스 만들기**에서 기본 데이터베이스를 만들고 **프로덕션 모드**를 선택합니다. 가능하면 서울 리전을 선택합니다.
6. Firestore의 **규칙** 탭에 이 프로젝트의 **firestore.rules** 전체 내용을 붙여넣고 **게시**합니다. 기본 테스트 모드 규칙으로 사용하지 마세요.
7. 프로젝트를 다시 빌드한 뒤 같은 APK를 두 휴대폰에 설치합니다.

Firebase 계정이나 서버 관리 비밀키는 앱에 넣지 않습니다. google-services.json은 앱 연결 설정 파일입니다. 실제 데이터가 쌓이기 전에 본인의 Firebase 프로젝트에 위 규칙이 게시되어 있는지 확인해주세요.

안내 근거: [Firebase Android 연결](https://firebase.google.com/docs/android/setup), [이메일·비밀번호 인증](https://firebase.google.com/docs/auth/android/password-auth), [Firestore 보안 규칙](https://firebase.google.com/docs/firestore/security/rules-conditions).

## 빌드

Android Studio에서 이 폴더를 열고 Gradle 동기화를 마친 뒤 **Build APK(s)**를 실행합니다. JDK 17, Android SDK 35, Gradle 8.11.1을 사용합니다.

이 작업 폴더에 내려받은 빌드 도구를 사용할 때는 PowerShell에서:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./build.ps1
```

생성 위치: app/build/outputs/apk/debug/app-debug.apk

Firebase 설정 파일이 없더라도 빌드는 가능하며 체험 모드로 동작합니다. 파일이 추가되면 Google Services 플러그인이 자동 적용됩니다.

## 휴대폰에 설치

APK를 카카오톡, USB 또는 클라우드 드라이브로 휴대폰에 전달하고 파일을 열어 설치합니다. 파일을 여는 앱에 대해 Android의 **이 출처 허용**을 켜야 할 수 있습니다. Android 8.0 이상이 필요합니다. 플레이스토어 등록 없이 두 사람의 휴대폰에 직접 설치할 수 있습니다.

APK 업데이트는 같은 서명 키로 빌드해야 합니다. 이 폴더의 .tools/debug.keystore를 보관하세요. 설정이 바뀐 APK를 설치하기 전에 기존 앱을 지울 필요는 없습니다.

## 계산 기준

- 완료율 = 완료한 날 / 대상 날짜 수 × 100, 정수 반올림.
- 대상 날짜: 선택한 주 또는 달과 목표의 시작~종료 기간이 겹치는 날 중 오늘까지.
- 미완료와 미기록은 별도로 표시하며 모두 분모에 포함합니다.
- 목표 생성 전·종료 후·미래 날짜는 제외합니다.
- 주간은 월요일~일요일, 월간은 해당 월의 1일~마지막 날입니다.
- 날짜 기준은 Asia/Seoul입니다. 목표 종료일도 대상 날짜에 포함합니다.

## 검증

계산 로직의 JUnit 테스트와 Firestore 접근 규칙에 대한 로컬 에뮬레이터 테스트를 제공합니다.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./build.ps1 -TestOnly
```

규칙 검사는 실제 Firebase 프로젝트를 건드리지 않으며 demo-together 로컬 에뮬레이터에만 요청합니다:

```powershell
& '.tools/jdk/jdk-17.0.20.1+1/bin/java.exe' -jar '.tools/firestore-emulator.jar' --host=127.0.0.1 --port=8080 --project_id=demo-together --rules=firestore.rules
# 별도 터미널
powershell.exe -NoProfile -ExecutionPolicy Bypass -File tests/firestore-rules.ps1
```

## 현재 범위

매일 반복 목표, 두 계정 공간, 실시간 조회, 기록 수정·취소, 목표 종료, 기간별 통계를 제공합니다. 알림, 특정 요일 반복, 계정 삭제, 커플 해제 및 데이터 내보내기는 현재 포함하지 않습니다. 초대 코드는 상대방에게만 공유하세요. 코드가 있으면 로그인한 사용자는 참여 전 공간의 이름 목록을 읽을 수 있고 빈 두 번째 자리에 참여할 수 있습니다. 목표·기록은 참여자만 조회할 수 있습니다.

네트워크 연결이 끊기면 캐시된 기록을 조회할 수 있으며 저장 요청은 연결 복구 후 반영됩니다. 실제 프로젝트 연결, 두 실기기 동기화와 설치 확인은 설정 파일을 받은 후 진행해야 합니다.


## 디자인 업데이트 (1.1.0)

오늘 화면의 한 주 날짜 표시를 누르면 해당 날짜의 기록으로 이동합니다. 기록 화면에서는 달력으로 날짜를 고릅니다. 목표의 메뉴(점 세 개)에서 기록 취소 또는 목표 종료를 선택할 수 있습니다. ‘우리’ 탭에서 초대 코드 복사도 가능합니다.

최신 설치 파일은 artifacts/together-daily.apk입니다. 두 휴대폰 모두 같은 파일로 업데이트하면 됩니다. 기존 앱을 지우지 말고 덮어 설치하세요. 휴대폰에는 내장 저장공간 최상단과 다운로드 폴더에 together-daily.apk로 복사해 두었습니다.
