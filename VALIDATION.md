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


## 목표 분류·횟수와 메모 저장 업데이트 1.2.0

- 메모 초안의 rememberSaveable 키에서 원격 saved 값을 제거하고, 변경 중인 초안을 지연 조회나 저장 실패가 덮어쓰지 않도록 수정. 미확정 Firestore 스냅샷은 저장 결과에 반영하지 않음. 서버 쓰기 완료를 기다린 뒤 성공 메시지와 초안 기준값을 갱신. 초안은 계정·날짜별 SharedPreferences에 보관. 권한 거부와 연결 지연은 화면에 표시.
- Goal.schedule(WEEKDAY/WEEKEND), weeklyTarget(선택 사항) 직렬화와 기존 목표의 평일 기본값 지원. 추가·수정·정렬 화면과 오늘·기록·달성률 필터 적용.
- 기록 순서: 평일 나/상대방, 주말·공휴일 나/상대방. 목표별 기간 달성률 표시. 주당 횟수 계산은 주마다 상한을 적용하고 초과분 이월하지 않음.
- 공휴일은 한국 전국 2026~2027년과 대체공휴일, 2026 지방선거, 노동절·제헌절 변경 반영. 임시공휴일이나 이후 연도는 연간 표 업데이트 필요.
- assembleDebug + testDebugUnitTest + lintDebug 성공. 단위 테스트 20/20 통과(메모 초안 5, 기존 계산 7, 분류·횟수 8). Android Lint 오류 0.
- 로컬 demo-together Firestore 에뮬레이터 검사 48/48 통과. 두 계정의 메모 저장·수정·삭제와 저장 내용 왕복 확인, 새 목표 필드 저장, 외부 계정 차단 확인.
- Firebase 설정 포함 APK: artifacts/together-daily.apk, 쏘규-Daily-챌린지.apk. package com.together.daily, versionCode 3, versionName 1.2.0. 기존 개발 서명키 사용.
- 실제 Firebase 서버 규칙 게시·실제 두 계정 저장·휴대폰 설치 및 화면 검증은 이번 작업에서 수행하지 않음.
