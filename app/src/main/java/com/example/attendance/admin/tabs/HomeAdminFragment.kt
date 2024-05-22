package com.example.attendance.admin.tabs

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendance.R
import com.example.attendance.admin.tabs.adapter.HomeAdapter
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.FragmentHomeAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeAdminFragment : Fragment() {
    private lateinit var binding : FragmentHomeAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var accArrayList : ArrayList<UsersModel>

    //adapter
    private lateinit var adapter : HomeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@HomeAdminFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getUsers()
    }

    private fun getUsers() {

            //initialize
            accArrayList = ArrayList()

            val dbRef = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list
                    accArrayList.clear()
                    for (data in snapshot.children){
                        //data as model
                        val model = data.getValue(UsersModel::class.java)
                            accArrayList.add(model!!)

                    }
                    //set up adapter
                    adapter = HomeAdapter(this@HomeAdminFragment.requireContext(), accArrayList)
                    //set to recycler
                    binding.recy.setHasFixedSize(true)
                    binding.recy.layoutManager = LinearLayoutManager(context)
                    binding.recy.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

}