package com.example.attendance

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.attendance.databinding.FragmentSignUpBinding
import com.example.attendance.viewmodels.SignUpViewModel

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var progressDialog: ProgressDialog
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var selectedImage: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.imageView2.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        binding.btnSignUp.setOnClickListener {
            validateData()
        }

        viewModel.rfidData.observe(viewLifecycleOwner, Observer {
            binding.etRfid.setText(it)
        })
        viewModel.fingerPrint.observe(viewLifecycleOwner, Observer {
            binding.etFingerPrint.setText(it)
        })

        viewModel.signUpStatus.observe(viewLifecycleOwner, Observer { status ->
            Toast.makeText(this.requireContext(), status, Toast.LENGTH_SHORT).show()
            if (status == "Account Created") {
                findNavController().navigate(R.id.loginFragment)
            }
        })
    }

    private fun validateData() {
        val email = binding.etEmailSignUp.text.toString().trim()
        val pass = binding.etPasswordSignUp.text.toString().trim()
        val fullName = binding.etFullname.text.toString().trim()
        val pinCode = binding.etFingerPrint.text.toString().trim()
        val rfid = binding.etRfid.text.toString().trim()

        when {
            email.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Email...", Toast.LENGTH_SHORT).show()
            pass.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Password...", Toast.LENGTH_SHORT).show()
            fullName.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Name...", Toast.LENGTH_SHORT).show()
            pinCode.isEmpty() -> Toast.makeText(this.requireContext(), "Scan Finger Print ...", Toast.LENGTH_SHORT).show()
            rfid.isEmpty() -> Toast.makeText(this.requireContext(), "Tap Your Card", Toast.LENGTH_SHORT).show()
            !::selectedImage.isInitialized -> Toast.makeText(this.requireContext(), "Please Upload a Picture", Toast.LENGTH_SHORT).show()
            else -> viewModel.createUserAccount(email, pass, selectedImage, fullName, pinCode, progressDialog)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImage = data.data!!
            binding.imageView2.setImageURI(selectedImage)
        }
    }
}
