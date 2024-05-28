package com.example.attendance.admin.tabs

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.attendance.R
import com.example.attendance.databinding.FragmentSettingsAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SettingsAdminFragment : Fragment() {
    private lateinit var binding : FragmentSettingsAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        loadUsersInfo()
        binding.btnUpdateProfile.setOnClickListener {
            findNavController().apply {

                navigate(R.id.editFragment) // Navigate to LoginFragment
            }
        }
        binding.btnLogout.setOnClickListener {
            progressDialog.setMessage("Logging Out...")
            progressDialog.show()
            view.postDelayed({
                auth.signOut()
                progressDialog.dismiss()

                findNavController().apply {
                    popBackStack(R.id.settingsAdminFragment, false)
                    navigate(R.id.loginFragment)
                }
            }, 2000)
        }
        binding.btnBack.setOnClickListener {
            findNavController().apply {
                navigate(R.id.userFragment) // Navigate to LoginFragment
            }
        }
    }
    private fun loadUsersInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val image = "${snapshot.child("image").value}"
                    val name = "${snapshot.child("fullName").value}"
                    //set data
                    binding.tvName.text = name
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.imgAccount)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}