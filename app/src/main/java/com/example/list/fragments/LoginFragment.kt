package com.example.lsit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.example.list.data.User
import com.example.list.data.UserLogin
import com.example.list.fragments.LoginViewModel
import com.example.list1110.databinding.FragmentLoginBinding
import com.example.list.interfaces.MainActivityInterface
import com.example.list.repository.AppRepository

class LoginFragment : Fragment() {

    companion object {
        private var INSTANCE: LoginFragment ?= null

        fun getInstance(): LoginFragment {
            if (INSTANCE == null) INSTANCE = LoginFragment()
            return INSTANCE ?: throw Exception("LoginFragment not created")
        }

        fun newInstance(): LoginFragment {
            INSTANCE = LoginFragment()
            return INSTANCE!!
        }
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var _binding: FragmentLoginBinding
    private var usesr: User = User()
    private var usered: UserLogin = UserLogin()
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btLogin.setOnClickListener {
            usered.login = binding.etLogin.text.toString()
            usered.password = binding.etPassword.text.toString()
            usesr.login = binding.etLogin.text.toString()
            usesr.password = binding.etPassword.text.toString()
            AppRepository.getInstance().registration(usesr)
            AppRepository.getInstance().login(usesr,usered)

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        val ma = (requireActivity() as MainActivityInterface)
        ma.updateTitle("АВТОРИЗАЦИЯ \"${viewModel}\"")
    }
}