package com.example.czarek.czarkomondo.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.activities.*
import com.example.czarek.czarkomondo.adapters.ActionsAdapter
import com.example.czarek.czarkomondo.models.SettingsListItem
import kotlinx.android.synthetic.main.fragment_more_actions.*

class MoreActionsFragment : Fragment() {

    private val settings = listOf(
        SettingsListItem(R.drawable.settings_profile_icon, R.string.settings_profile_text),
        SettingsListItem(R.drawable.settings_achievements_icon, R.string.settings_achievements_text),
        SettingsListItem(R.drawable.settings_resolution_icon, R.string.settings_resolution_text),
        SettingsListItem(R.drawable.settings_credits_icon, R.string.credits)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings_list.adapter = ActionsAdapter(activity!!.baseContext, settings)
        settings_list.setOnItemClickListener{_, _, position, _ ->
            when(position){
                0 -> startActivity(Intent(activity, SettingsActivity::class.java))
                1 -> startActivity(Intent(activity, AchievementsActivity::class.java))
                2 -> startActivity(Intent(activity, ResolutionActivity::class.java))
                3 -> startActivity(Intent(activity, CreditsActivity::class.java))
                else -> throw UnsupportedOperationException("Item that does not exist clicked :O")
            }
        }
    }
}
