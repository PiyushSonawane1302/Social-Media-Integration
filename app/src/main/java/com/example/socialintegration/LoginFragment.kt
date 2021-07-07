package com.example.socialintegration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.socialintegration.databinding.FragmentLoginBinding
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginFragment : Fragment() {

    companion object {
        private const val RC_SIGN_IN = 100
        private const val EMAIL = "email"
        private const val TAG = "TEST"
    }


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callBackManager: CallbackManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.loginButton.fragment = this
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        if (firebaseUser != null) {
            val action = LoginFragmentDirections.actionLoginFragmentToDetailsFragment()
            view.findNavController().navigate(action)
        }


        callBackManager = CallbackManager.Factory.create()
        binding.loginButton.setPermissions(listOf(EMAIL))

        binding.facebookLogin.setOnClickListener {
            binding.loginButton.performClick()
        }

        binding.loginButton.registerCallback(
            callBackManager,
            object : FacebookCallback<LoginResult?> {

                override fun onSuccess(loginResult: LoginResult?) {
                    if (loginResult != null) {
                        Log.d(TAG, "Success")
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                }

                override fun onCancel() {
                    Log.d(TAG, "Cancelled !")
                }

                override fun onError(exception: FacebookException) {
                    Log.d(TAG, "onError : " + exception.message)
                }
            })



        binding.loginBtn.setOnClickListener {
            val email = binding.emailLoginEt.text.toString()
            val pass = binding.passwordLoginEt.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter email or password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.d("TEST", "Login Clicked Email : $email , Pass : $pass")
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                    OnCompleteListener {
                        if (it.isSuccessful) {
                            checkMailVerification()
                            val action =
                                LoginFragmentDirections.actionLoginFragmentToDetailsFragment()
                            view.findNavController().navigate(action)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Account not exist Please create account or Check Details",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }


        binding.forgotPass.setOnClickListener {
            Log.d("TEST", "Forgot Pass Clicked")
        }

        binding.register.setOnClickListener {
            Log.d("TEST", "Register Clicked")
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            view.findNavController().navigate(action)
        }

        binding.googleSignin.setOnClickListener {
            Log.d("TEST", "Google Signin Clicked")
            signIn()

        }


    }


    private fun handleFacebookAccessToken(token: AccessToken) {

        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    val action = LoginFragmentDirections.actionLoginFragmentToDetailsFragment()
                    findNavController().navigate(action)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    private fun checkMailVerification() {

        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        if (firebaseUser != null) {

            if (firebaseUser.isEmailVerified) {
                Toast.makeText(requireContext(), "Logged In", Toast.LENGTH_SHORT).show()
                val action = LoginFragmentDirections.actionLoginFragmentToDetailsFragment()
                view?.findNavController()?.navigate(action)
            } else {
                Toast.makeText(requireContext(), "Please Verify your email", Toast.LENGTH_SHORT)
                    .show()
                firebaseAuth.signOut()
            }

        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("Sign In Activity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("Sign In Activity", "Google sign in failed", e)
                }
            } else {
                Log.w("Sign In Activity", "Google sign in failed", exception)
            }
        } else {
            callBackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("Sign In Activity", "signInWithCredential:success")
                Toast.makeText(requireContext(), "Logged In", Toast.LENGTH_SHORT).show()
                val action = LoginFragmentDirections.actionLoginFragmentToDetailsFragment()
                view?.findNavController()?.navigate(action)
            } else {
                // If sign in fails, display a message to the user.
                Log.w("Sign In Activity", "signInWithCredential:failure", task.exception)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}