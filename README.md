# Study Manager

## 기능 구현
<최형우>
-[x] 카카오톡 로그인
-[x] 자동 녹음 기능 구현
-[] 위치 공유 기능
-[] 단체 채팅방
-[] 오류 수정
-[] 녹음 기능 +@

## 🔍 진행 방식

- 하루마다 자기가 구현한 부분 매번 갱신해서 Readme에 작성하기
- 각자 기능 구현하고 체크하기
- 읽어보고 요구 사항, 에러 발생등에 날짜 적어주세요
- 애매하게 하지 말고 확실히 만들어서 A+ 받읍시다

## 📮 프로젝트 진행 사항(11월 30일 부터 새로 작성)

- 11/30
    [최형우]
      채팅방 기능 구현 1
- 12/1

- 12/2
    [최형우]
        채팅방 기능 구현 2
- 12/3

  - 12/4
    [최형우]
        채팅방 기능 구현 3
- 12/5
    [최형우]
        login ui 수정, join ui 수정
        안쓰는 png 파일 삭제
        전체적인 ui 삭제
- 12/6

- 12/7

- 12/8

- 12/9

- 12/10

- 12/11

- 12/12

- 12/13

- 12/14

- 12/15

- 12/16

- 12/17

- 12/18

- 12/19

- 12/20

- 12/21

- 12/22

- 12/23 [어플제출일]

## 🚀 기능 요구 사항

대학 공부를 도와주는 어플을 만든다

- 스터디 팀 매칭 기능
- 녹음 기능
- 공부에 도움이 되는 자잘한 기능들



## 🎯 추가한 부분
- 11/12 auth.LogInActivty.kt에 kakao 로그인 기능 추가
- 11/12 auth.introAcitivy.kt부분으로 가던 인텐트 삭제, 바로 loginactvity로 가도록 수정
- 11/13 로그아웃시 인텐트 LoginActivity로 수정
- 백그라운드 녹음 연동 수정

### 발생한 에러 지속적으로 업데이트하기!
-[x]파이어베이스 접근 오류 
  kakao 로그인 개발시 firebase에 접근이 안되서 오류가 발생 to 강민기

-[] 홈 화면에 아래의 버튼 텍스트가 내려가서 잘림
-[] 로그아웃 버튼이 개인정보 수정 액티비티에 들어가 있음, 마이페이지에 있어야 할 것 같음
-[] 스터디원 찾기에 모든 사람을 추가 시킬 때 마다 이미 추가된 친구라고 뜸 왜 첨부터 다 친구?
-[] 준비중인 기능: 메시지함의 메세제 클릭, 이메일로 친구 추가, 단체 채팅, 스터디 파일 공유, 팀 설정, 위치 공유, 스터디 메시지 보내기

-[] 자유게시판에서 남의 게시물을 내가 수정할 수 있음
-[] 자유게시판 댓글을 수정/삭제 못함
-[] 자유게시판 댓글 보내기 버튼이 조금 잘림
-[] 자유게시판 글 삭제 버튼 작동 안함
-[] 자유게시판 글 들어갔을 때 위에 title 배너 삭제하는게 좋을 것 같음

-[x] 개인공부방에 과목정보수정 버튼 작동 안함
-[] 시간표가 길어지면 뒷시간의 과목들이 아래로 내려가서 안 보임
-[] 녹음 저장할 때 default 과목 이름
-[] 녹음 재생에 배속 버튼을 계속 누르면 튕김
-[] 대시보드에 내가 안 넣은 과목이 있음
-[] 스터디원 매칭에 o나x를 선택한 대상이라도 계속 뜸
-[] 개인정보 수정에 성별 칸의 힌트가 학년으로 되어있음
-[] 개인정보 수정에서 이미지가 한참 뒤에 수정됨
-[] 비속어 검열?
-[] 게시물 작성에 내용 칸이 너무 작아서 3줄 적으면 잘리기 시작함 
-[] 게시물 수정할 시에 수정했다는 표시 추가하기
-[] 단체게시판 글쓰기의 입력 버튼 작동 안함
-[x] 스터디 그룹에서 탈퇴하면 앱이 사라짐
-[x] 로그인시 이미지 없으면 튕김

#### 피어리뷰 심각한 오류
-[] 스터디원 매칭 및 추가 안됨
-[] 제트플림 ui 망가짐
-[x] 홈버튼을 누르니 로그아웃, 로그아웃 마진 늘리기
-[x] 메시지함에 누르면, 이유는 모르겠는데 닉네임 아래 소개글? 같은 것이 뜨고 누르면 아직 준비되지 않은 기능이라 떠서 아쉽습니다. 그저 ui만 테스트 느낌으로 만든 것인지 궁금합니다.
-[] 강의를 등록하고 뒤로 가면 다시 강의 등록하던 페이지로 돌아갑니다.
-[] 이유는 모르겠으나, 강의 녹음을 하면 두 번씩 저장됩니다. 강의 녹음을 들을 때 5초 전후로 갈 줄 알았는데 1초 전후로 가게 됩니다. 강의를 녹음하고 저장할 때, 언더바 앞에 맨 처음만 강의 이름만 같으면 모두 해당 강의로 들어가게 됩니다.
-[] 카메라 기능이 되지 않습니다.
-[] 번역하기에서 한글을 치고 JAPANESE->GERMAN으로 하면 번역이 일어나지 않고 멈춥니다.
-[] 번역기에 write를 누르면 앱이 튕깁니다. 그리고 갤러리 사진을 클릭해도 적용이 안됩니다.
-[] 친구 목록 메시지 보내기 안 됩니다.(현재 하얀 화면만 나옴)
-[] 촬영기능 작동하지 않는 것으로 보입니다.(현재 검은 화면만 나옴)
-[] 단체 채팅 구현 안됩니다.
-[] 번역기능에서 write 버튼 아무것도 적지 않고 클릭시 튕깁니다.
-[] 스터디룸을 만들고 해당 창에 들어갔는데 그룹 게시판 외에는 활용할 수 있는 것이 없었습니다. 스터디룸 추가 기능이라는 이름 아래에 여러 박스들이 있었는데, 아무것도 적히지 않은 채 빈칸으로 보였습니다. 또한 탭으로 어플을 사용했는데 스터디룸 화면에서 아래쪽 40퍼센트 정도가 애매하게 비어 있었습니다.
-[] 번역에서 사진이 번역되는건지 모르겠습니다. 시간이 5분이 지나도 안 됐습니다.
-[] 홈 화면에서 카메라를 누르면 정상적으로 작동이 되지 않습니다.
-[] 매칭한 스터디원 목록을 조회할 수 없습니다. (정확히는 어디서 조회해야하는 지 모르겠음)
-[] 추가 하지 않은 친구가 목록에서 생기고, 추가한 친구는 목록에 생기지 않습니다.
-[] 카카오 로그인 유저는 이메일 추가가 불가능합니다.
-[] 이메일 친구 추가 버튼이 작동하지 않습니다.
-[] 알람이 뜨고 그 알람을 통해 녹음을 진행할 때 녹음을 취소하면 팅기는 오류가 있습니다.
-[] 이따금씩 발생하는 튕김 현상 개선 필요합니다.
-[] 수락하지 않은 스터디원이 친구 추가가 되어 있고 추가한 친구는 친구 추가가 안되어 있는 현상 개선이 필요합니다.
-[] 스터디룸 추가 기능 안뜹니다.
-[x] 기종에 따라 UI가 잘리는 현상 존재합니다.
-[x] 카카오 로그인 시 개인 정보의 이메일 란에 "null"이라는 문자열이 기재되어 있고, 이는 이메일로 친구초대 기능에 문제를 발생시킬 것으로 보입니다. 또한 카카오 로그인 시 개인 정보의 자기소개란에 이상한 문자열이 들어가 있습니다. 심각하진 않지만 오류라고 생각되어 여기에 기재합니다.


#### 피어 리뷰 바라는 점
-[] 추가될 기능 추가하기
-[] 가끔식 튕기는 현상 해결
-[] 앱의 용량 관리, 단순화
-[] ui 구성 다듬기
-[] 넘어가는 화면들의 ui 개선
-[x] 프로필 이미지 등록하는 칸임을 알게 해주기
-[] 메뉴 아이콘과 이름 같이 색칠
-[] 개인 공부방, 강의실 이름 하나 확정
-[] 강의 녹음 끝나면 처음으로
-[] 스터디원 매칭 좌우 경고없이 추가되거나 거절, so 사용하는 방법(튜토리얼 띄우기)
-[] 무음카메라 번역기 문제 해결
-[] 시간 예약시 시계로 돼있어서 조작 어려움
-[] 텍스트 입력 가능한 키보드 아이콘 너무 밑에 있음
-[x] 성별 입력 라디오 버튼 처리 , 성별 칸에 다른거 넣을 수 있음
-[] 친구목록에서 삭제하기
-[] 이미 추가되어 있는 친구 추천 안뜨게하기
-[] 내용 없이 이미지만 올릴 수 있게하기, 글쓰기 입력눌러도 반응 없음
-[] 스터디룸 정렬 조절
-[] 녹음 예약시 과목이름이 아니라 실제 과목 이름 저장
- [] ui일관성 개선하기
- [] 녹음 예약기능 설정시 과목 설정해도 알람 뜨는 오류
- [] 알람의 과목명이 바뀌는 오류
- [] 카메라 기능, 친구 추가 기능
- [] 시간표 추가 최대 2개밖에 설정 안되는 부분 아쉽
- [x] 마이페이지 소개 이상한 값
- [] 과목 전부 삭제 시 알람 설정하면 삭제 못함
- [] 스크롤 기능 완성하기
- [] 구현 안된거 카메라 등 구현하기
- [] 무음 카메라
- [] 휴대폰 화면 구성 이상
- [] 과목별 친구 추천 기능 만들기
- [] 강의 녹음 예약 동일하게 표시 x
- [] 카카오 로그인 로딩 화면 띄우기
- [] 앱 한번만 뒤로가기 해도 꺼짐
- [] 토스트 문구 오류
- [x] 메시지함 기능 완성 x
- [] 시간표 글자 잘림
- [] 채팅 기능 추가
- [] 튜토리얼 만들어주세요 

### 라이브러리
- SmplrAlarm 사용자 지정 알람 추가하기
  

#### 동료 요구 사항
  카카오톡 로그인 시 이동이 성별이 안보임
  
  -지권이형
    만들어야 하는 페이지 activity_setting xml 파일 완성하기 (youtube에 setting 창등 참고하기)
    번역기 기능 문제 해결하기 (무조건...)
    무음 카메라 기능 만들기
    번역기 무조건 고쳐주세요

  -민기형
    밑의 오류 수정 부탁드립니다.
    파일 공유 기능 추가해주세요

#### 해야할것 Last
 -[x] 메인 UI 수정 / 형우
 -[x] 그룹 메인 UI 수정 / 형우
 -[x] 게시판 (자유게시판, QNA) UI 수정 / 민기
 -[] 그룹 위치 공유 기능(후순위) / 형우,민기
 -[] 파일(후순위) / 민기
 -[] 카메라(완성시키기) -> 시간 안되면 -> 소리나게 하거나 / 그냥 카메라 연결 / 형우
 -[x] 녹음기 수정 / 형우
 -[] 메세지 UI / 형우
