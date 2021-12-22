package com.chiki.eggtimer.ui

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chiki.eggtimer.receiver.AlarmReceiver
import com.chiki.eggtimer.R
import com.chiki.eggtimer.util.cancelNotifications
import com.chiki.eggtimer.util.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EggTimerViewModel(private val app:Application):AndroidViewModel(app) {

    private val minute: Long = 60_000L  //One Minute
    private val second: Long = 1_000L   //One Second

    //Preferences
    private val TRIGGER_TIME = "TRIGGER_AT"     //Key to save data in the preferences
    private var prefs = app.getSharedPreferences("com.example.android.eggtimernotifications", Context.MODE_PRIVATE)  //To access the preferences saved by the app

    //Time
    private val timerLengthOptions: IntArray = app.resources.getIntArray(R.array.minutes_array) //Amount of time depending on the option selected by the user
    private lateinit var timer: CountDownTimer

    //Alarm
    private val REQUEST_CODE = 0
    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager      //To manage all alarms and notifications
    private val notifyPendingIntent: PendingIntent
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)


    //States
    private val _elapsedTime = MutableLiveData<Long>()          //Current Time left for cooking
    val elapsedTime: LiveData<Long> get() = _elapsedTime
    private val _timeSelection = MutableLiveData<Int>()         //Cooking time selected by the user
    val timeSelection: LiveData<Int> get() = _timeSelection
    private var _alarmOn = MutableLiveData<Boolean>()           //To know the state of the alarm on/off
    val isAlarmOn: LiveData<Boolean> get() = _alarmOn

    //Lifecycle
    init {
        _alarmOn.value =PendingIntent.getBroadcast(getApplication(),REQUEST_CODE,notifyIntent,PendingIntent.FLAG_NO_CREATE) !=null
        notifyPendingIntent = PendingIntent.getBroadcast(getApplication(),REQUEST_CODE,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        //If alarm is not null, resume the timer back for this alarm
        if(_alarmOn.value!!){
            createTimer()
        }
    }

    //User Inputs
    fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }       //User selects the cooking time
    fun setAlarm(isChecked: Boolean) {
        when (isChecked) {
            true -> timeSelection.value?.let { startTimer(it) }
            false -> cancelNotification()
        }
    }                     //User starts/cancels the time and alarm

    //Actions
    private fun startTimer(timerLengthSelection: Int){
        _alarmOn.value?.let {
            if(!it){
                _alarmOn.value = true       //Sets the alarm on
                val selectedInterval = when(timerLengthSelection){
                    0-> second * 10
                    else-> timerLengthOptions[timerLengthSelection] * minute
                } //Decides the cooking time depending on the user's selection
                val triggerTime = SystemClock.elapsedRealtime() + selectedInterval //Decides when to trigger the alarm adding the interval to the current system time

                val notificationManager = ContextCompat.getSystemService(app,NotificationManager::class.java) as NotificationManager
                notificationManager.cancelNotifications()
                AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager,AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,notifyPendingIntent)

                viewModelScope.launch {
                    saveTimer(triggerTime)
                }
            }
        }
        createTimer()
    }       //Time to cook, start the timer and sets alarm
    private fun cancelNotification(){
        resetTimer()
        alarmManager.cancel(notifyPendingIntent)
    }                       //The alarm is not needed anymore
    private fun createTimer(){
        viewModelScope.launch {
            val triggerTime = loadTimer()
            timer = object : CountDownTimer(triggerTime,second){
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if(_elapsedTime.value!! <=0){
                        resetTimer()
                    }
                }       //Updates the current time in visual
                override fun onFinish() {
                    resetTimer()
                }                               //Time is over, it resets the timer
            }
            timer.start()
        }
    }                               //Creates the timer with the selected options
    private fun resetTimer(){
        timer.cancel()
        _elapsedTime.value = 0
        _alarmOn.value = false
    }                               //Cancels the timer and the alarm
    private suspend fun saveTimer(triggerTime: Long) = withContext(Dispatchers.IO){
            prefs.edit().putLong(TRIGGER_TIME,triggerTime).apply()
        }   //Saves the time in preferences
    private suspend fun loadTimer():Long = withContext(Dispatchers.IO){
            prefs.getLong(TRIGGER_TIME,0L)
        }               //Retrieves the time from prefrences
}