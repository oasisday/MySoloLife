package mysololife.example.sololife.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mysololife.databinding.FragmentHomeBinding
import com.getkeepsafe.taptargetview.R
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import mysololife.example.sololife.Constants.Companion.TUTORIAL
import mysololife.example.sololife.Constants.Companion.TUTORIAL_DONE

class PopupManager(private val fragment : Fragment, private val binding: FragmentHomeBinding) {

    val sharedPreferences = fragment.requireActivity().getSharedPreferences(TUTORIAL, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()


    fun showViewTypePrompt() {
        if (!sharedPreferences.getBoolean(TUTORIAL_DONE, false)){
            TapTargetView.showFor(
                fragment.requireActivity(),
                TapTarget.forView(
                    binding.tutorialBtn,
                    "새로운 기능에 대한 간략한 안내를 드리겠습니다 :)",
                    "튜토리얼을 진행하려면 화면에서 팝업 버튼을 찾아 클릭하세요. \n튜토리얼을 ★끝까지 완료★ 하면 이후에는 해당 기능에 대한 팝업이 자동으로 나타나지 않습니다.\n\n또한 이 버튼을 클릭하여 튜토리얼을 다시 진행 할 수 있습니다."
                )
                    .tintTarget(false)
                    .titleTextSize(30)
                    .descriptionTextSize(18)
                    .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                    .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                    .outerCircleAlpha(0.99f)
                    .targetRadius(20)
                    .drawShadow(true)
                    .dimColor(android.R.color.white)
                    .textColor(android.R.color.white),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        showMainBoardPrompt()
                    }
                })
        }
    }

    fun showMainBoardPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.btneveryoneboard,
                "자유 게시판",
                "유저들과 소통할 수 있는 공간으로 질문과 답변 등을 자유롭게 할 수 있습니다.\n\n앱의 다양한 기능에 대한 소식과 업데이트 내용을 확인할 수 있는 ★★[공지]★★  게시글을 사용전 꼭! 제발! 무조건! 확실하게! 진짜! 확인해주세요\n\n"
            )
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(18)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(35)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showMatchingPrompt()
                }
            })

    }

    fun showMatchingPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.matchingBtn,
                "스터디원을 찾아보세요 :)",
                "[유저 리스트] - 기능 업데이트\n\n서로 친구 요청을 보내고, 상호간에 요청을 보냈을 경우 친구 목록에 뜨게 됩니다.\n\n 친구 (대기) 목록에서 상대방을 클릭하면 메세지를 보낼 수 있습니다.\n\n매칭을 통해 스터디 팀을 빠르게 만들어 보세요!\n"
            )
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(35)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showmakestudyBtnPrompt()
                }
            })
    }

    fun showmakestudyBtnPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.makestudyBtn,
                "친구 목록에서 팀원을 골라보세요 :)",
                "친구 목록을 클릭하여 개인 채팅을 진행 할 수 있습니다.\n\n친구들과 함께 스터디를 만들고, 스터디 기능을 사용해 보세요!")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(30)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    val targetY = binding.scrollview4.getChildAt(0).height / 2  // 중간 위치 계산
                    binding.scrollview4.smoothScrollTo(0, targetY)
                    showrecorderBtnPrompt()
                }
            })
    }

    fun showrecorderBtnPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.btnVoiceRecorder,
                "녹음기 신기능 추가",
                "녹음한 강의 목록을 ★롱클릭★ 할 시 강의 이름 변경과 삭제 기능을 추가했습니다.")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(30)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showcameraBtnPrompt()
                }
            })
    }
    fun showcameraBtnPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.btnCamera,
                "무음 카메라 업데이트",
                "무음 카메라는 도서관이나 조용한 장소에서 공부 할 때 다른 사람들을 방해하지 않으면서 학습 자료를 촬영 할 수 있습니다!\n\n또한 강의 중에 무음 카메라를 사용하여 강의 내용을 쉽게 기록할 수 있습니다.")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(30)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showtranslateBtnPrompt()
                }
            })
    }

    fun showtranslateBtnPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.btnTranslate,
                "번역기 기능",
                "변역할 언어를 선택 할 시 Firebase ML Kit에서 실시간으로 해당 번역 모델 언어를 다운로드 한 후 번역을 진행합니다.")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(30)
                .drawShadow(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    binding.scrollview4.fullScroll(ScrollView.FOCUS_DOWN)
                    showlocationBtnPrompt()
                }
            })
    }

    fun showlocationBtnPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.locationshare,
                "#위치 공유 기능",
                "위치 공유 기능이 업데이트 되었습니다!\n\n 상대의 아이콘을 누른 후 이모티콘을 실시간으로 상대에게 보낼 수 있습니다.\n\n이 기능을 사용할 때만 수동으로 위치 기능을 활성화 하고, 액티비티에서 나가면 위치를 추적하지 않습니다.")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(50)
                .drawShadow(true)
                .transparentTarget(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showallchatPrompt()
                }
            })
    }
    fun showallchatPrompt() {
        TapTargetView.showFor(
            fragment.requireActivity(),
            TapTarget.forView(
                binding.chatBtn,
                "#전체 채팅방 기능",
                "개인 채팅, 팀 채팅, 전체 채팅 기능을 업데이트 했어요\n\n이제 튜토리얼을 완료하셨습니다. 지금 채팅방에서 다른 사용자들과 소통하며 인사 메시지를 보내보세요!")
                .tintTarget(false)
                .titleTextSize(35)
                .descriptionTextSize(16)
                .targetCircleColor(com.example.mysololife.R.color.maincolor_seven2)
                .outerCircleColor(com.example.mysololife.R.color.maincolor_seven)
                .outerCircleAlpha(0.99f)
                .targetRadius(50)
                .drawShadow(true)
                .transparentTarget(true)
                .dimColor(android.R.color.white)
                .textColor(android.R.color.white),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    editor.putBoolean(TUTORIAL_DONE,true)
                    editor.apply()
                }
            })
    }

}