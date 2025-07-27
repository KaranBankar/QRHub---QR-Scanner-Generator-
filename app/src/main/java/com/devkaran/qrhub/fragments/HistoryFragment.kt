package com.devkaran.qrhub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devkaran.qrhub.adapters.QRCodeAdapter
import com.devkaran.qrhub.databinding.FragmentHistoryBinding
import com.devkaran.qrhub.model.QRCodeViewModel
import com.devkaran.qrhub.model.QRCodeViewModelFactory
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: QRCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, QRCodeViewModelFactory(requireActivity().application))
            .get(QRCodeViewModel::class.java)

        val adapter = QRCodeAdapter()
        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }

        // Collect Flow in a coroutine scope
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allQRCodes.collect { qrCodes ->
                adapter.submitList(qrCodes)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}