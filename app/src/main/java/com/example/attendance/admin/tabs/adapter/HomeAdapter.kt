package com.example.attendance.admin.tabs.adapter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.attendance.R
import com.example.attendance.admin.tabs.DetailsFragment
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.ItemRowHomeBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeAdapter(
    private val context: Context,
    private var usersArrayList: List<UsersModel>,
    private val navController: NavController

):
    RecyclerView.Adapter<HomeAdapter.ViewHolderUsers>() {
    private lateinit var binding : ItemRowHomeBinding
    private val database = Firebase.database
    inner class ViewHolderUsers(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name : TextView = binding.tvName
        val userType : TextView = binding.userType
        val image : ImageView = binding.imageUser
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderUsers {
        binding = ItemRowHomeBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolderUsers(binding.root)
    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderUsers, position: Int) {
        val model = usersArrayList[position]
        val fullName = model.fullName
        val userType2 = model.userType
        val image = model.image
        val uid = model.uid
        val fingerPrint = model.fingerPrint
        val rfid = model.RFID
        holder.apply {
            Glide.with(this@HomeAdapter.context)
                .load(image)
                .into(binding.imageUser)

            binding.tvName.text = fullName
            binding.userType.text = userType2
        }
        holder.itemView.setOnClickListener {
            val detailsFragment = DetailsFragment()
            val bundle = Bundle().apply {
                putString("uid", uid)
                putString("image", image)
                putString("fullName", fullName)
            }
            detailsFragment.arguments = bundle
            navController.navigate(R.id.detailsFragment, bundle)
        }

    }


}