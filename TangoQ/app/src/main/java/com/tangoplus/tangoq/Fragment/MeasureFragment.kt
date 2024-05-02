package com.tangoplus.tangoq.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.tangoplus.tangoq.Adapter.PainPartRVAdpater
import com.tangoplus.tangoq.Dialog.LoginDialogFragment
import com.tangoplus.tangoq.Dialog.MeasurePainPartDialogFragment
import com.tangoplus.tangoq.Listener.OnPartCheckListener
import com.tangoplus.tangoq.Object.Singleton_t_user
import com.tangoplus.tangoq.R
import com.tangoplus.tangoq.ViewModel.GraphVO
import com.tangoplus.tangoq.ViewModel.MeasureViewModel
import com.tangoplus.tangoq.databinding.FragmentMeasureBinding
import kotlin.random.Random


class MeasureFragment : Fragment(), OnPartCheckListener {
    lateinit var binding : FragmentMeasureBinding
    val viewModel : MeasureViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMeasureBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------! 통증 부위 관리 시작 !------
        binding.tvMsAddPart.setOnClickListener {
            val dialog = MeasurePainPartDialogFragment()
            dialog.show(requireActivity().supportFragmentManager, "LoginDialogFragment")

        } // ------! 통증 부위 관리 끝 !------

        // ------!  이름 + 통증 부위 시작 !------
        val t_userdata = Singleton_t_user.getInstance(requireContext())
        val userJson= t_userdata.jsonObject?.getJSONObject("data")
        binding.tvMsUserName.text = userJson?.optString("user_name")

        // TODO 1. t_measure등에서 가져오는 결과값이 있다 --> 밸런스 점수, 뭐 측정일자 같은거 업데이트
        // TODO 2. 일단 통증 부위를 일단 넣을 수 있게.
        viewModel.parts.observe(viewLifecycleOwner) { parts ->
            if (parts.isEmpty()) { // 통증 부위 2개 이하 선택
                binding.llMsEmpty.visibility = View.VISIBLE

            } else { // part로 선택한 게 있을 때.
                binding.llMsEmpty.visibility = View.GONE
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                val leftData = parts.filterIndexed { index, _ -> index % 2 == 0 }.toMutableList()
                val leftadapter = PainPartRVAdpater(leftData, "Pp", this@MeasureFragment)
                binding.rvMsLeft.adapter = leftadapter
                if (leftData.isNotEmpty()) {
                    binding.rvMsLeft.layoutManager = linearLayoutManager
                }

                val rightData = parts.filterIndexed { index, _ -> index % 2 == 1 }.toMutableList()
                val rightadapter = PainPartRVAdpater(rightData, "Pp", this@MeasureFragment)
                Log.v("adapter데이터", "${leftData}, ${rightData}")
                binding.rvMsRight.adapter = rightadapter
                if (rightData.isNotEmpty()) {
                    binding.rvMsRight.layoutManager = linearLayoutManager2
                }
            }
        } // ------!  이름 + 통증 부위 끝 !------


        // ------! 꺾은선 그래프 시작 !------
        val lineChart = binding.lcMs
        val lcXAxis = lineChart.xAxis
        val lcYAxisLeft = lineChart.axisLeft
        val lcYAxisRight = lineChart.axisRight
        val lcLegend = lineChart.legend

        val lcDataList : MutableList<GraphVO> = mutableListOf()
//        val startMonth = (month + 8) % 12
//        for (i in 0 until 12) {
//            val currentMonth = (startMonth + i) % 12
//            val monthLabel = if (currentMonth == 0) "12월" else "${currentMonth}월"
//            lcDataList.add(ChartVO(monthLabel, Random.nextInt(99)))
//        }
        val weekList = listOf("월", "화", "수", "목", "금", "토", "일")
        for (i in weekList) {
            lcDataList.add(GraphVO(i, Random.nextInt(99)))
        }
        val lcEntries : MutableList<Entry> = mutableListOf()
        for (i in lcDataList.indices) {
            // entry는 y축에 넣는 데이터 형식을 말함. Entry의 1번째 인자는 x축의 데이터의 순서, 두 번째 인자는 y값
            lcEntries.add(Entry(i.toFloat(), lcDataList[i].yAxis.toFloat()))
        }
        val lcLineDataSet = LineDataSet(lcEntries, "")
        lcLineDataSet.apply {
            color = resources.getColor(R.color.mainColor, null)
            circleRadius = 3F
            lineWidth = 3F
            mode = LineDataSet.Mode.LINEAR
            valueTextSize = 0F
            setCircleColors(resources.getColor(R.color.mainColor))
            setDrawCircleHole(true)

        }

        lcXAxis.apply {
            textSize = 14f
            textColor = resources.getColor(R.color.subColor500)
            labelRotationAngle = 2F
            setDrawAxisLine(true)
            setDrawGridLines(false)
            lcXAxis.valueFormatter = (IndexAxisValueFormatter(lcDataList.map { it.xAxis }))
            setLabelCount(lcDataList.size, true)
            lcXAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
            axisLineWidth = 1.0f
        }
        lcYAxisLeft.apply {
            setDrawGridLines(true)
            setDrawAxisLine(false)
//            setDrawGridLinesBehindData(true)
//            setDrawZeroLine(false)
            setLabelCount(3, false)
            setDrawLabels(false)
//            textColor = resources.getColor(R.color.subColor500)
//            axisLineWidth = 1.5f
        }
        lcYAxisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setLabelCount(0, false)
            setDrawLabels(false)
        }
        lcLegend.apply {
            lcLegend.formSize = 0f
        }
        lineChart.apply {
            data = LineData(lcLineDataSet)
            notifyDataSetChanged()
            description.text = ""
            setScaleEnabled(false)
            invalidate()
        }
        // ---- 꺾은선 그래프 코드 끝 ----

        // ---- 막대 그래프 코드 시작 ----
        val barChart = binding.bcMs
        val bcXAxis = barChart.xAxis
        val bcYAxisLeft = barChart.axisLeft
        val bcYAxisRight = barChart.axisRight
        val bcLegend = barChart.legend

        val bcDataList : MutableList<GraphVO> = mutableListOf()

//        for (i in 0 until 12) {
//            val currentMonth = (startMonth + i) % 12
//            val monthLabel = if (currentMonth == 0) "12월" else "${currentMonth}월"
//            bcDataList.add(ChartVO(monthLabel, Random.nextInt(99)))
//        }
        val bcData = listOf( "12:00", "6:00", "12:00", "6:00")
        val bcEntries = mutableListOf<String>()
        for (hour in 0 until 24) {
            val timeLabel = String.format("%02d:00", hour) // 시간 형식을 "00:00"으로 포맷
            bcDataList.add(GraphVO(timeLabel, Random.nextInt(99)))

            // 30분 뒤의 시간 추가
            val halfHourLabel = String.format("%02d:30", hour)
            bcDataList.add(GraphVO(halfHourLabel, Random.nextInt(99)))
        }

        val interval = bcDataList.size / bcData.size // 48 / 4
        Log.v("수치", "$interval, ${bcData.size}")
        for (i in 1 .. bcDataList.size) {
            if (i % interval == 0) {
                val j = i / interval  // 12 24 36 48
                bcEntries.add(bcData[j-1])
            } else {
                bcEntries.add(" ")
            }
        }
        Log.v("bcEntries", "$bcEntries, ${bcEntries.size}")

        val rcEntries : MutableList<BarEntry> = mutableListOf()
        for (i in bcDataList.indices) {
            // entry는 y축에 넣는 데이터 형식을 말함. Entry의 1번째 인자는 x축의 데이터의 순서, 두 번째 인자는 y값
            rcEntries.add(BarEntry(i.toFloat(), bcDataList[i].yAxis.toFloat()))
        }
        val bcLineDataSet = BarDataSet(rcEntries, "")
        bcLineDataSet.apply {
            color = resources.getColor(R.color.mainColor, null)
            valueTextSize = 0F
        }

        bcXAxis.apply {
            textSize = 14f
            textColor = resources.getColor(R.color.subColor500)
            labelRotationAngle = 1.0F
            setDrawAxisLine(false)
            setDrawGridLines(false)
            bcXAxis.valueFormatter = IndexAxisValueFormatter(bcData)
            setLabelCount(bcData.size, true)
            bcXAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
            axisLineWidth = 0f

        }
        bcYAxisLeft.apply {
            setDrawGridLines(true)
            setDrawAxisLine(false)
//            setDrawGridLinesBehindData(true)
//            setDrawZeroLine(false)
            setLabelCount(3, false)
            setDrawLabels(false)
//            textColor = resources.getColor(R.color.subColor500)
//            axisLineWidth = 1.5f
        }
        bcYAxisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setLabelCount(0, false)
            setDrawLabels(false)

        }
        bcLegend.apply {
            bcLegend.formSize = 0f
        }
        barChart.apply {
            data = BarData(bcLineDataSet)
            notifyDataSetChanged()
            description.text = ""
            setScaleEnabled(false)
            invalidate()
        }
        // ---- 막대 그래프 코드 끝 ----
    }

    override fun onPartCheck(part: Pair<String, String>, checked: Boolean) {
        TODO("Not yet implemented")
    }

}