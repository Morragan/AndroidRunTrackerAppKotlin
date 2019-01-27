package com.example.czarek.czarkomondo.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.TrainingRepository
import com.example.czarek.czarkomondo.TrainingViewModel
import com.example.czarek.czarkomondo.TrainingViewModelFactory
import com.example.czarek.czarkomondo.adapters.TrainingHistoryAdapter
import kotlinx.android.synthetic.main.fragment_training_history.*


class TrainingHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_training_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TrainingHistoryAdapter(activity!!)
        val viewModel =
            TrainingViewModelFactory(activity!!.application).create(TrainingViewModel::class.java)
        viewModel.trainingsList.observe(this, Observer { trainings ->
            trainings?.let { adapter.setData(it) }
        })
        training_history_list.adapter = adapter
        training_history_list.layoutManager = LinearLayoutManager(activity!!.applicationContext)
    }

}
