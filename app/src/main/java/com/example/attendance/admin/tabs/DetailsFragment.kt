package com.example.attendance.admin.tabs

import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.admin.tabs.adapter.LogsAdapter
import com.example.attendance.admin.viewmodels.LogsViewModel
import com.example.attendance.databinding.FragmentDetailsBinding

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val logsViewModel: LogsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        val rfid = arguments?.getString("rfid")
        val fingerPrint = arguments?.getString("fingerPrint")

        if (rfid != null && fingerPrint != null) {
            logsViewModel.fetchLogs(rfid, fingerPrint)
        }

        logsViewModel.logs.observe(viewLifecycleOwner) { logList ->
            if (logList != null) {
                val filteredLogs = logList.filter { log ->
                    log.rfid == rfid && log.fingerPrint == fingerPrint
                }
                val logsAdapter = LogsAdapter(filteredLogs)
                binding.recycler.adapter = logsAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
