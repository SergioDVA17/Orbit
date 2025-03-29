package com.sm.DatePlanPia

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.json.JSONArray


class Score : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scoreView = inflater.inflate(R.layout.score, container, false)

        val workSpinner = scoreView.findViewById<Spinner>(R.id.workSpinner)
        val workAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.work_array, android.R.layout.simple_spinner_item
        )
        workSpinner.adapter = workAdapter

        val recreationSpinner = scoreView.findViewById<Spinner>(R.id.recreationSpinner)
        val recreationAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.work_array, android.R.layout.simple_spinner_item
        )
        recreationSpinner.adapter = recreationAdapter
        return scoreView
    }
    override fun onResume() {
        super.onResume()
        val sharedPref =
            requireContext().getSharedPreferences("UserActivities", Context.MODE_PRIVATE)
        val jsonArr = JSONArray(sharedPref.getString("userData", ""))
        Log.d("TEST", "TEST")
        var workCounter = 0
        var schoolCounter = 0
        var recreationCounter = 0
        var meetingCounter = 0
        var socialCounter = 0

        for (i in 0 until jsonArr.length()) {
            val jsonObject = jsonArr.getJSONObject(i)
            val type = jsonObject.getString("activityType")
            when (type) {
                "Work" -> workCounter += 1
                "School" -> schoolCounter += 1
                "Recreation" -> recreationCounter += 1
                "Meeting" -> meetingCounter += 1
                "Social" -> socialCounter += 1
            }
        }

        val balanceChart: PieChart = requireView().findViewById(R.id.balanceChart)
        balanceChart.description.text = "Your Balance"


        val data = mutableListOf<PieEntry>()
        if (workCounter > 0) {
            data.add(PieEntry(workCounter.toFloat(), "Work"))
        }
        if (schoolCounter > 0){
            data.add(PieEntry(schoolCounter.toFloat(), "School"))
        }
        if (recreationCounter > 0) {
            data.add(PieEntry(recreationCounter.toFloat(), "Recreation"))
        }
        if (meetingCounter > 0){
            data.add(PieEntry(meetingCounter.toFloat(), "Meeting"))
        }
        if (socialCounter > 0) {
            data.add(PieEntry(socialCounter.toFloat(), "Social"))
        }

        val dataSet: PieDataSet = PieDataSet(data, "      (Legend)")
        balanceChart.setEntryLabelColor(Color.BLACK)
        val colors = listOf(Color.rgb(255, 56, 56),
                            Color.rgb(28, 236, 255),
                            Color.rgb(20, 255, 138),
                            Color.rgb(255, 161, 161),
                            Color.rgb(158, 61, 255))
        dataSet.colors = colors
        balanceChart.data = PieData(dataSet)

        var variance: Float = 0f
        var totalWork = 0f
        var totalRecreation = 0f
        totalWork += workCounter  + schoolCounter + meetingCounter
        totalRecreation += recreationCounter + socialCounter
        var mean = (totalWork + totalRecreation) / 2f
        variance += (mean-totalWork)* (mean-totalWork)
        variance += (mean-totalRecreation)* (mean-totalRecreation)
        //the variance should be correct now.

        var score = 1000 - variance*100
        if (totalWork == 0f){
            score -= 100
        }
        if (totalRecreation == 0f){
            score -= 100
        }

        var scoreText: TextView = requireView().findViewById(R.id.finalScoreText)
        if (mean == 0f){
            scoreText.text = "0 / 1000"
        }else{
            if (score.toInt() < 0){
                //Negative score doesn't make sense
                score = 0f
            }
            scoreText.text = score.toInt().toString() + " / 1000"
        }

    }
}