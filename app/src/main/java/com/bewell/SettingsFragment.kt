package com.bewell

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bewell.databinding.FragmentSettingsBinding
import com.bewell.storage.repository.MeasureRepository
import com.bewell.viewmodels.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by inject()
    private val measureRepo: MeasureRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vm =
            ViewModelProvider(this)[SettingsViewModel::class.java]

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.emailText.text = auth.currentUser!!.email

        binding.signOutButton.setOnClickListener {
            try {
                measureRepo.snapshotListener.remove()
            } catch (e: Exception) {}

            auth.signOut()

            requireActivity().finish()

            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}