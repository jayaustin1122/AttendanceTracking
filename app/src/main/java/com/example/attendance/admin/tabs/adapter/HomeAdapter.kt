package com.example.attendance.admin.tabs.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.attendance.R
import com.example.attendance.admin.tabs.DetailsFragment
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.ItemRowHomeBinding

class HomeAdapter(
    private val context: Context,
    private var usersArrayList: List<UsersModel>,
    private val navController: NavController
) : RecyclerView.Adapter<HomeAdapter.ViewHolderUsers>() {

    inner class ViewHolderUsers(val binding: ItemRowHomeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderUsers {
        val binding = ItemRowHomeBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolderUsers(binding)
    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderUsers, position: Int) {
        val model = usersArrayList[position]
        holder.apply {
            Glide.with(context)
                .load(model.image)
                .into(binding.imageUser)

            binding.tvName.text = model.fullName
            binding.userType.text = model.userType

            itemView.setOnClickListener {
                val detailsFragment = DetailsFragment()
                val bundle = Bundle().apply {
                    putString("uid", model.uid)
                    putString("image", model.image)
                    putString("fullName", model.fullName)
                }
                detailsFragment.arguments = bundle
                navController.navigate(R.id.detailsFragment, bundle)
            }
        }
    }
}
