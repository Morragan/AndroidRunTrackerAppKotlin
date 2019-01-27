package com.example.czarek.czarkomondo.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.czarek.czarkomondo.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        profile_weight.setText(sharedPreferences.getInt(getString(R.string.preference_weight), 70).toString())
        profile_height.setText(sharedPreferences.getInt(getString(R.string.preference_height), 170).toString())
        profile_gender.setSelection(sharedPreferences.getInt(getString(R.string.preference_gender), 0))
        profile_vibration.setText(sharedPreferences.getInt(getString(R.string.preference_vibe_interval), 1000).toString())

        profile_save.setOnClickListener {
            try {
                val weight = profile_weight.text.toString().toInt()
                val height = profile_height.text.toString().toInt()
                val gender = profile_gender.selectedItemPosition
                val vibration = profile_vibration.text.toString().toInt()
                with(sharedPreferences.edit()) {
                    putInt(getString(R.string.preference_weight), weight)
                    putInt(getString(R.string.preference_height), height)
                    putInt(getString(R.string.preference_gender), gender)
                    putInt(getString(R.string.preference_vibe_interval), vibration)
                    apply()
                }
                finish()
            } catch (ex: ClassCastException) {
                Toast.makeText(baseContext, getString(R.string.validation_error), Toast.LENGTH_LONG).show()
            }

        }
    }
}
