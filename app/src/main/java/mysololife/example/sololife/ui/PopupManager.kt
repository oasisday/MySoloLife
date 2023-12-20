package mysololife.example.sololife.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
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
                    binding.textView3,
                    "앱에 오신것을 환영합니다 :)",
                    "신기능에 대해 간단하게 안내해 드립니다.\n튜토리얼을 진행하려면 화면에서 팝업 버튼을 찾아 클릭하세요. 튜토리얼을 끝까지 완료하면 이후에는 해당 기능에 대한 팝업이 자동으로 나타나지 않습니다."
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
                "앱 사용자들 간에 자유롭게 소통할 수 있는 공간으로 질문과 답변 등을 자유롭게 할 수 있습니다.\n또한, 앱의 다양한 기능에 대한 소식과 업데이트 내용을 확인할 수 있는 [공지] 게시글을 확인하세요!\n\n"
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
                "스터디원 매칭",
                "[카드뷰]\n (왼쪽->오른쪽) 방향으로 스와이프 시 친구 요청을 보내고, 반대 방향으로 스와이프시 거절합니다.\n\n[유저 리스트] - 기능 업데이트 \n테스트 하실 때 편의를 위해 유저 리스트를 통해서 친구 추가를 하게 되면 상대방의 수락 없이 바로 친구 추가가 되도록 임시로 수정하였습니다.\n매칭 통해 스터디 팀을 빠르게 만들어 보세요!\n"
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
                "스터디원 매칭에서 수락한 친구는 대기 목록에 들어가게 되고, 서로 매칭을 수락한 친구는 친구 목록에 추가됩니다.\n(단, 빠른 테스트를 위해 팝업창으로 친구 추가를 할 시 즉시 친구 추가를 할 수 있도록 코드를 수정하였습니다.)\n\n친구 목록을 클릭하여 개인 채팅을 진행 할 수 있습니다.\n친구들과 함께 스터디를 만들고, 스터디 기능을 사용해 보세요!")
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

                }
            })
    }
//
//    fun showMinimizePrompt() {
//        TapTargetView.showFor(
//            fragment.requireActivity(),
//            TapTarget.forView(
//                binding.buttonMinimize,
//                fragment.requireActivity().getString(R.string.popupPrompt),
//                fragment.requireActivity().getString(R.string.popupExplain)
//            )
//                .tintTarget(false)
//                .titleTextSize(40)
//                .descriptionTextSize(20)
//                .outerCircleColor(R.color.mlightgray)
//                .outerCircleAlpha(0.9f)
//                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
//                .drawShadow(true)
//                .textColor(R.color.black)
//                .targetRadius(70),
//            object : TapTargetView.Listener() {
//                override fun onTargetClick(view: TapTargetView?) {
//                    super.onTargetClick(view)
//                    editor.putBoolean(TUTORIAL_DONE,true)
//                    editor.apply()
//                }
//            })
//    }
}