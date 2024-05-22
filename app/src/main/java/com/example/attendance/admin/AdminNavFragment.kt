package com.example.attendance.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.attendance.R
import com.example.attendance.admin.tabs.HomeAdminFragment
import com.example.attendance.admin.tabs.LogsAdminFragment
import com.example.attendance.admin.tabs.SettingsAdminFragment
import com.example.attendance.databinding.FragmentAdminNavBinding
import com.example.attendance.user.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminNavFragment : Fragment() {
    private lateinit var binding : FragmentAdminNavBinding
    private lateinit var homeFragment: Fragment
    private lateinit var settingsFragment: Fragment
    private val logsFragment: Fragment by lazy { LogsAdminFragment() }
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminNavBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager = requireActivity().supportFragmentManager
        homeFragment = HomeAdminFragment()
        settingsFragment = SettingsAdminFragment()

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_home -> homeFragment
                R.id.navigation_Logs -> logsFragment
                R.id.navigation_settings -> settingsFragment
                else -> return@setOnNavigationItemSelectedListener false
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commitAllowingStateLoss() // Use commitAllowingStateLoss() to retain fragment state
            true
        }
        if (savedInstanceState == null) {
            // Initially load the HomeFragment only if it's not already added
            if (!homeFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, homeFragment)
                    .commit()
            }
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }
}