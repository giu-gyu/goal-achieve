# 검증 결과

- APK 빌드: 성공 (Android SDK 35 / Gradle 8.11.1 / JDK 17)
- 계산 JUnit 테스트: 4개 통과, 실패 0개
- Firestore 접근 규칙: 로컬 에뮬레이터에서 18개 검사 통과
- Android Lint: 오류 0개, 경고 5개 (라이브러리 새 버전 안내 4개, 백업 설정 안내 1개)
- APK 서명: v2 검증 통과

검사 대상은 이 프로젝트의 실제 Kotlin 계산 코드와 firestore.rules입니다. 규칙 검사는 가입 공간 생성·참여, 두 명 제한, 외부 접근 제한, 소유권, 미래 날짜 제한, 생성 전 날짜 제한, 종료·취소 권한을 검증했습니다.

artifacts/together-daily-demo.apk는 Firebase 연결 파일 없이 빌드한 체험용입니다. 체험 데이터는 서버에 저장되지 않습니다. 실제 Firebase 프로젝트 배포, 실제 계정 로그인 및 두 휴대폰 실시간 동기화, 실기기 UI 테스트는 아직 수행하지 않았습니다. README.md에 설정과 설치 절차가 있습니다.

개발용 서명 키는 .tools/debug.keystore에 있습니다. 이후 동일 앱 업데이트를 위해 보관하세요.

## Firebase 연결 및 휴대폰 업데이트

- Firebase 프로젝트: goal-achieve-8205f
- app/google-services.json의 Android 패키지 com.together.daily 일치 확인
- Google Services 설정 적용 빌드 성공, 계산 테스트 통과, Lint 오류 0개
- artifacts/together-daily.apk 생성
- 연결된 Galaxy SM-S928N에 기존 앱을 유지한 채 업데이트 설치 성공 및 실행 확인
- 실제 이메일 로그인 연결 검사: CONFIGURATION_NOT_FOUND 반환. Firebase Authentication 초기 설정과 이메일/비밀번호 활성화가 필요함
- 계정 생성이 실패하여 임시 테스트 계정은 생성되지 않았으며 Firestore 데이터도 쓰지 않음
- 실제 계정 로그인 및 두 휴대폰 기록 동기화는 아직 검증하지 못함

## 실제 Firebase 설정 재확인

- 이메일/비밀번호 가입: 정상 활성화 확인.
- Firestore API: 비활성화(SERVICE_DISABLED) 응답. Firestore Database 생성 또는 API 활성화가 필요함. 접근 규칙은 아직 검증할 수 없음.
- 검사용 계정의 자동 삭제: HTTP 401로 실패. Authentication 사용자 목록의 connection-check-...@example.com 계정을 수동 삭제해야 함.
- Firestore 문서는 생성하거나 변경하지 않았음.
## 게시 후 실제 서버 검증

- Firebase 이메일/비밀번호 가입 정상 확인.
- 실제 Firestore 접근 검사 5/5 통과: 초대 코드 직접 조회, 커플 목록 열람 차단, 다른 사람 프로필 접근 차단, 자신의 프로필 조회, 비참여자 목표 조회 차단.
- 검사용 계정 삭제 성공(HTTP 200). Firestore 문서는 생성하거나 변경하지 않았음.
- Firebase 설정이 적용된 APK가 휴대폰에 설치되어 있음. 두 사용자의 실제 가입·초대·기록 공유는 각자 계정으로 진행해야 함.

## 디자인 업데이트 1.1.0

- 크림·살구·라벤더 색상, 네이티브 커플 일러스트, 통일된 선 아이콘과 하단 메뉴 적용.
- 오늘 화면의 주간 날짜 표시, 목표 카드, 완료 버튼과 상태 표현 개선.
- 기록 화면에 월간 달력과 날짜별 기록 선택 추가.
- 달성률 화면에 전체 요약 링과 완료·미완료·미기록 집계, 목표별 진행률 제공.
- 로그인·가입·커플 연결·초대 코드·목표 추가 화면 디자인 개선. 초대 코드 복사 기능 추가.
- Firebase 데이터 구조와 보안 규칙, 기존 목표·기록·로그인 유지.
- 빌드 및 JUnit 계산 테스트 4개 통과. Android Lint 오류 0개.
- Galaxy SM-S928N에 업데이트 설치 성공(versionCode 2 / versionName 1.1.0). 오늘·기록·달성률·우리 화면을 실기기에서 확인.
- 최신 APK를 /sdcard/together-daily.apk 및 /sdcard/Download/together-daily.apk에 복사. 파일 관리자에서 APK 표시 확인, 다운로드 사본의 SHA-256 일치 확인.



- 메모 초안의 rememberSaveable 키에서 원격 saved 값을 제거하고, 변경 중인 초안을 지연 조회나 저장 실패가 덮어쓰지 않도록 수정. 미확정 Firestore 스냅샷은 저장 결과에 반영하지 않음. 서버 쓰기 완료를 기다린 뒤 성공 메시지와 초안 기준값을 갱신. 초안은 계정·날짜별 SharedPreferences에 보관. 권한 거부와 연결 지연은 화면에 표시.
- 로컬 demo-together Firestore 에뮬레이터 검사 48/48 통과. 두 계정의 메모 저장·수정·삭제와 저장 내용 왕복 확인, 새 목표 필드 저장, 외부 계정 차단 확인.
- Firebase 설정 포함 APK: artifacts/together-daily.apk, 쏘규-Daily-챌린지.apk. package com.together.daily, versionCode 3, versionName 1.2.0. 기존 개발 서명키 사용.
- 실제 Firebase 서버 규칙 게시·실제 두 계정 저장·휴대폰 설치 및 화면 검증은 이번 작업에서 수행하지 않음.


- 변경 전 현재 상태: ddf5d09. 당시 APK를 Galaxy SM-S928N에 업데이트 완료. 아래 추가 수정본은 휴대폰 연결 해제 이후 작성했으며 실기기에 설치하지 않음.
- firestore.rules 변경 없이 기존 공동 공간 접근 규칙으로 새 저장 필드를 지원함. 실제 Firebase 데이터 수정 및 규칙 배포는 수행하지 않음.
- 최종 APK: artifacts/together-daily.apk, 쏘규-Daily-챌린지.apk. 기존 개발 서명 및 package com.together.daily 유지. 실기기에서 새 팝업·폭죽·압축 편집 화면 확인은 미수행.
## 기록 메모 간소화·성별 표 색상·인물 이모티콘 (2026-10-12)

- 작업 시작 전 64ca58a APK를 Galaxy SM-S928N에 데이터 유지 방식으로 업데이트 성공. 이후 수정본은 APK로 준비하며 휴대폰에 설치하지 않음.
- 요청별 독립 커밋: 1 bf5ec52 (날짜+본문 메모 행), 2 3034165 (빈 목표 테이블 유지), 3 1ef779a (성별 표 색상 및 배경), 4 aa84d8e (프로필 성별/인물 선택 및 이름 표시).
- 로컬 Firestore 에뮬레이터 검사 58/58 통과. 남녀 성별·인물 스타일의 두 계정 저장/공유 조회, 기존 자기소개 유지, 외부 계정 접근 차단 검증. 테스트 커밋 d036d90. 서버 규칙 변경 및 실제 Firebase 데이터 수정 없음.
- assembleDebug + testDebugUnitTest + lintDebug 성공. 단위 테스트 24/24 통과, Lint 오류 0. 실기기에서 추가 수정 UI 확인은 미수행.
## 정렬 수동 스크롤·오늘 전환·20색 팔레트 (2026-10-12)

- assembleDebug + testDebugUnitTest + lintDebug 성공. 단위 테스트 24/24 통과, Android Lint 오류 0. 새 UI의 실기기 터치 검증 및 설치는 미수행.
- 목표 색상 저장·조회 범위를 GoalPalettes.indices로 확장하고 기존 6색 인덱스 유지. Firebase 규칙 변경 없음.
- artifacts/together-daily.apk 및 쏘규-Daily-챌린지.apk 갱신.
## 반복 안내·목표 색상 제거 (2026-10-12)

- 6364f99: 오늘·기록·달성률의 횟수 미설정 목표 안내를 제거하고 빈 줄·구분점을 정리. 횟수 지정 목표의 주 N회 표시는 유지.
- ce77fd3: 목표 색상 설정·모델·팔레트·저장/조회 및 개별 카드 적용 제거. 기록의 성별 테이블 색상 유지. 원격 기존 색상 필드 삭제 없음.
- assembleDebug + testDebugUnitTest + lintDebug 성공. 기존 단위 테스트 24/24 통과, Lint 오류 0. 실기기 설치·UI 검증은 미수행.
- 최신 artifacts/together-daily.apk 및 쏘규-Daily-챌린지.apk 갱신.

주당 횟수 기본 숨김 (8149bbc): 추가·수정 모두 기본 접힘, 설정 버튼으로 펼침/접힘, X 선택 시 OFF 및 접힘. 기존 저장 횟수 유지. assembleDebug/testDebugUnitTest/lintDebug 성공, 단위 테스트 24개 통과, 실기기 설치·터치 검증 미수행.


메모 작성자 분리 및 기록 여백 개선: e394e5c는 짝꿍 탭에 내 메모 작성기가 노출되던 오류 수정, 선택 작성자/날짜 조회 및 짝꿍 읽기 전용 표시와 회귀 테스트 추가. a35c414는 날짜 우선·요일 다음 줄 표시, 줄간격/폰트 패딩 및 기록 영역 여백 축소. assembleDebug/testDebugUnitTest/lintDebug 성공, 단위 테스트 26/26 통과. 실제 Firebase 저장 데이터 변경 없음. 실기기 설치·화면 확인 미수행.

## 주 경계·메모 편집·감정 이모티콘

- d131502: 월요일 기준 주가 달라지는 표시 날짜 앞에 2.5dp 세로선. 헤더·목표·완료율 행에 적용.
- f75af2a: 기록의 내 메모 길게 누르기 편집, 원래 날짜 서버 저장 후 닫기, 빈 내용 삭제, 실패 시 유지. 짝꿍 메모는 읽기 전용.
- ec2a1ed: 기본 얼굴/곱슬머리/금발 제거, 감정 28종 및 동작 4종 총 32종. 기존 동작 인덱스 보존. 선택은 기록·달성률 memberLabel에 반영. 기존 저장 동작과 잘못된 인덱스 대체 테스트 추가.
- assembleDebug/testDebugUnitTest/lintDebug 성공. 단위 테스트 28/28 통과. 실제 Firebase 데이터 수정·규칙 배포·휴대폰 설치·화면 검증은 미수행.
## 오늘 탭 날짜 이동 (2026-10-13)

- assembleDebug + testDebugUnitTest + lintDebug 성공. 단위 테스트 28/28 통과.
- artifacts/together-daily.apk 및 쏘규-Daily-챌린지.apk 갱신.
- Firestore 규칙 변경 없음. 실제 서버 저장 및 휴대폰 설치·화면 검증은 미수행.

## 메모 입력 초기화 및 기록 복사 (2026-10-13)

- 오늘 메모 칸 유지, 저장 성공 후 입력 초기화. 작성자·날짜별 Compose 상태 분리, 저장된 본문 재입력 제거. 상대방 칸은 기록 조회 안내 표시.
- 기록 메모 길게 누르기: 본문 복사. 내 메모 짧게 누르기: 편집.
- assembleDebug/testDebugUnitTest/lintDebug 성공, APK 두 파일 갱신. 휴대폰 화면 및 실제 서버 저장 검증 미수행.


## 기록·달성률 통합 (2026-10-13)

- assembleDebug/testDebugUnitTest/lintDebug 성공, APK 두 파일 갱신. 실제 휴대폰 화면 확인 미수행.


## 목표 유형별 달성률·색상 (2026-10-13)

- assembleDebug/testDebugUnitTest/lintDebug 성공. APK 두 파일 갱신. 실제 휴대폰 화면·서버 저장 검증은 미수행.


## 휴대폰 업데이트 (2026-10-14)

- Galaxy SM-S928N에 adb install -r 성공, 기존 데이터 유지 방식으로 업데이트 및 앱 실행.
- /sdcard/together-daily.apk 및 /sdcard/Download/together-daily.apk 복사 완료. 두 파일의 SHA-256이 로컬 최신 APK와 일치.

## 프로필·설정·메모 반응·기록 UI 검증 (2026-10-14)

- 메모 복사/회원별 반응 및 취소, 본문 수정 시 반응 유지, 길게 누르기 복사 효과 적용.
- 앰버/로즈/틸 색상과 기록 표 우측 범례, 동일 행 주당 횟수 배지 적용.
- APK 두 파일 갱신. ADB 연결 기기 없음. 실제 휴대폰 화면·프로필 저장·반응 공유 검증과 휴대폰 업데이트는 미수행.


- assembleDebug/testDebugUnitTest/lintDebug 성공. 테스트 35/35 통과.
- APK 두 파일 갱신. SM-S928N 내장 저장공간·Download에 복사, SHA-256 일치 확인. install -r 성공 및 앱 실행.


## 달성률 진행 날짜 기준 (2026-10-14)

- 달성률 탭의 미래 날짜 제외, 회원별 오늘 미기록 시 어제까지 계산, 오늘 첫 완료/미완료 기록부터 오늘 포함.
- 화요일 완료 100%, 미기록 수요일 100% 유지, 오늘 시작 후 전체 목표 반영, 상대방/미래 기록 제외, 주당 횟수 유지 검증.
- assembleDebug/testDebugUnitTest/lintDebug 성공. 단위 테스트 39/39 통과. APK 두 파일 갱신. ADB 연결 기기 없음으로 휴대폰 설치 미수행.

## 주당 횟수 설정 단순화 (2026-10-18)

- 목표 횟수는 0~6회와 매일(7회), 기본값 7로 통일. 기존 횟수 없는 목표는 7로 로드.
- 달력 데이터와 날짜별 유형 처리, 관련 UI·색상 범례·직렬화·테스트 삭제.
- assembleDebug, testDebugUnitTest, lintDebug 성공. 단위 테스트 30/30 통과.
- 0회 분모 0, 매일 7일, 중복 기록, 주별 초과분 미이월, 미래 기록 제외, 목표 기간 경계 검증.
- 실기기 화면 조작 검증 및 운영 데이터 변경은 수행하지 않음. Firestore 규칙 변경 없음.

## 순서 드래그 안정화 및 가장자리 자동 스크롤 (2026-10-18)

- 드래그 제스처를 목록 컨테이너에서 처리하고 활성 카드는 별도 오버레이로 표시하여 행 재배치·재활용에 따른 좌표 변경과 제스처 취소 방지.
- 순서 교환 시 첫 항목 키를 따라가는 스크롤 보정을 막고 현재 보이는 목록 위치 유지.
- 위·아래 64dp 영역에서 프레임 단위 자동 스크롤, 가장자리에 가까울수록 최대 640dp/s. 손가락을 멈춰도 작동하며 종료·취소 시 중단.
- 카드 양옆 여백의 수동 스크롤 유지. 스크롤과 드래그의 터치 충돌 방지.
- assembleDebug, testDebugUnitTest, lintDebug 성공. 테스트 33/33 통과: 중앙 정지·양방향 속도·속도 상한·작은/빈 뷰포트 검증 추가.
- 실기기 터치 드래그 및 화면 육안 검증은 수행하지 않음.

## 하루 여러 메모·길게 누르기 메뉴·다중 반응 (2026-10-18)

- 신규 메모를 UUID 문서에 추가하고 기존 날짜 단위 문서는 문서 ID 그대로 조회·수정. 오늘/기록에서 날짜·작성시각·ID로 정렬하고 메모별 키 사용.
- 개별 수정/삭제는 문서 ID를 사용하고 나머지 메모와 반응 유지. 동일 입력 재시도는 보관한 ID 재사용, 실패 후 다른 내용 저장은 새 ID 생성.
- 길게 누르기와 일반 탭은 수정(내 메모만)·복사·이모티콘 선택 메뉴 표시. 길게 누르기 자체는 복사하지 않음.
- 회원별 반응을 배열로 저장하여 여러 개 선택/개별 해제. 기존 문자열 반응도 배열로 읽고 본문 수정 시 유지.
- assembleDebug, testDebugUnitTest, lintDebug 성공. 단위 테스트 36/36 통과.
- 로컬 Firestore 에뮬레이터 검사 54/54 통과: 같은 날 독립 메모 두 개·반응 배열 저장 왕복·다른 메모 삭제 후 첫 메모 보존·외부 계정 차단 포함. Firestore 규칙 변경 없음.
- 실기기 화면·터치 동작 및 운영 Firestore 검증은 수행하지 않음. 과거에 덮어써져 사라진 본문 복구는 포함하지 않음.
