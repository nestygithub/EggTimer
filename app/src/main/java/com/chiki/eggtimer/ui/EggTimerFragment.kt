package com.chiki.eggtimer.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.chiki.eggtimer.R
import com.chiki.eggtimer.databinding.FragmentEggTimerBinding

class EggTimerFragment : Fragment() {
    //ViewModels
    private lateinit var eggTimerViewModel: EggTimerViewModel
    //Binding
    private var _binding: FragmentEggTimerBinding? = null
    private val binding get() = _binding!!

    //Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEggTimerBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eggTimerViewModel = ViewModelProvider(this).get(EggTimerViewModel::class.java)

        binding.eggTimerViewModel = eggTimerViewModel
        binding.lifecycleOwner = this

        createNotificationChannel(getString(R.string.egg_notification_channel_id),getString(R.string.egg_notification_channel_name))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createNotificationChannel(channelId: String, channelName: String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for breakfast"
            notificationChannel.setShowBadge(false)

            val notificationManager = requireActivity().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}