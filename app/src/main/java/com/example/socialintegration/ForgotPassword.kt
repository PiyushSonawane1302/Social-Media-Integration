package com.example.socialintegration

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.socialintegration.databinding.FragmentForgotPasswordBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : Fragment() {

    private var _binding:FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.resetPass.setOnClickListener {
            val email = binding.emailForgotPassEt.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Enter your Email", Toast.LENGTH_SHORT).show()
            }else {

                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(
                    OnCompleteListener() {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Recovery mail sent to your Registered email",
                                Toast.LENGTH_SHORT
                            ).show()
                           val action = ForgotPasswordDirections.actionForgotPasswordToLoginFragment()
                            view.findNavController().navigate(action)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please Check your email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }

    }

}