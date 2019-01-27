package com.example.czarek.czarkomondo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.czarek.czarkomondo.activities.SettingsActivity
import com.example.czarek.czarkomondo.fragments.MoreActionsFragment
import com.example.czarek.czarkomondo.fragments.TrackTrainingFragment
import com.example.czarek.czarkomondo.fragments.TrackTrainingFragment.Companion.REQUEST_CHECK_SETTINGS
import com.example.czarek.czarkomondo.fragments.TrainingHistoryFragment
import com.example.czarek.czarkomondo.services.TrackTrainingService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val fragments: List<Fragment> = listOf(TrainingHistoryFragment(), TrackTrainingFragment(), MoreActionsFragment())
    private lateinit var sharedPref: SharedPreferences

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when(item.itemId){
            R.id.navigation_user_activity_history -> fragments[0]
            R.id.navigation_home -> fragments[1]
            R.id.navigation_more -> fragments[2]
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        stopService(Intent(this, TrackTrainingService::class.java))
        val pendingIntent: PendingIntent? =
            intent?.getParcelableExtra(TrackTrainingService.RESOLUTION_DATA_KEY)
        pendingIntent?.let {
            startIntentSenderForResult(
                pendingIntent.intentSender,
                REQUEST_CHECK_SETTINGS,
                null,
                0,
                0,
                0,
                null
            )
        }
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        if(sharedPref.getBoolean(getString(R.string.preference_first_launch), true)){
            with(sharedPref.edit()){
                putBoolean(getString(R.string.preference_first_launch), false)
                apply()
            }
            showDialog()
        }

        navigation.setOnNavigationItemSelectedListener(this)
        navigation.selectedItemId = R.id.navigation_home
    }

    private fun showDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.first_launch_dialog_title))
        builder.setMessage(getString(R.string.first_launch_dialog_body))

        builder.setPositiveButton(R.string.go_to_settings) { _, _ ->
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        builder.show()
    }
}
