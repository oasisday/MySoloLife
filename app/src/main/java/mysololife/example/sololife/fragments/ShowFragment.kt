package mysololife.example.sololife.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentShowBinding
import mysololife.example.sololife.Matching


class ShowFragment : Fragment() {
    private lateinit var binding : FragmentShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show, container, false)


        binding.CardBtn.setOnClickListener{
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }


        return binding.root
    }


}