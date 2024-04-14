package com.example.mhg

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.example.mhg.VO.BLEViewModel
import com.example.mhg.databinding.FragmentReportGoalBinding
import java.lang.Exception
import java.util.UUID


class ReportGoalFragment : Fragment() {
    lateinit var binding: FragmentReportGoalBinding
    private val viewModel: BLEViewModel by activityViewModels()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            ?: throw Exception("Bluetooth is not supported by this device")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportGoalBinding.inflate(inflater)
        return binding.root
    }
    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -----! 블루투스 켜기 끄기 설정 보존 시작 !-----
        val sharedPref = context?.getSharedPreferences("deviceSettings", Context.MODE_PRIVATE)
        val modeEditor = sharedPref?.edit()
        val bleMode = sharedPref?.getBoolean("bleMode", false)
        if (bleMode == true) {
            binding.schReportBluetooth.isChecked = true
        } else {
            binding.schReportBluetooth.isChecked = false
        }
        val bleFilter= IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        requireActivity().registerReceiver(receiver, bleFilter)
        binding.schReportBluetooth.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                if (haveAllPermissions(requireContext())) {
                    bluetooth.adapter.enable()
                    modeEditor?.putBoolean("bleMode", isChecked)
                    modeEditor?.apply()
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), ALL_BLE_PERMISSIONS, REQUEST_CODE)
                }
            } else {
                bluetooth.adapter?.disable()
                modeEditor?.putBoolean("bleMode", isChecked)
                modeEditor?.apply()
            }
        } // -----! 블루투스 켜기 끄기 설정 보존 끝 !-----

        // -----! 블루투스 기기 연결 후 viewmodel에서 꺼내서 gatt 서비스 쓰기 시작 !-----
        viewModel.gatt.observe(viewLifecycleOwner) {gatt ->
            if (gatt != null) {
                Log.w("연결 후 gatt", "데이터: $gatt")
                val services = gatt.services
                // TODO GATT에서 METHOD를 통해서 데이터 전처리 해야함.
                binding.tvGattData.text = gatt.services.toString()
            }
        }

        // -----! 블루투스 기기 연결 후 viewmodel에서 꺼내서 gatt 서비스 쓰기 끝 !-----
    }
    val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    // -----! 기기 검색 하는 새 창 !-----
                    val dialog = DeviceConnectDialogFragment()
                    dialog.show(requireActivity().supportFragmentManager, "deviceConnectDialogFragment")
                }
            }
        }
    }

    // BLE 주변 장치 찾기 위한 클래스 + 스캐너
    private lateinit var bluetooth : BluetoothManager
    private var selectedDevice : BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null
    private var services: List<BluetoothGattService> = emptyList()
    val REQUEST_CODE = 8080
    // -----! 첫 블루투스 사용 권한 설정 시작 !-----
    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.w("권한 설정", "Permission True")
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter.isEnabled) {
                    val dialog = DeviceConnectDialogFragment()
                    dialog.show(requireActivity().supportFragmentManager, "deviceConnectDialogFragment")
                }
                Log.w("BLE권한설정", "ALL PERMISSION PERMITTED !")
            } else {
                Toast.makeText(requireContext(), "블루투스 권한을 설정해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun haveAllPermissions(context: Context) =
        ALL_BLE_PERMISSIONS
            .all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    // -----! 첫 블루투스 사용 권한 설정 끝 !-----


    val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    // 특성에서 데이터를 추출하는 곳.
    @RequiresPermission(anyOf = ["android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH", "android.permission.ACCESS_FINE_LOCATION", "android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"])
    fun readCharacterisitic(serviceUUID: UUID, characteristicUUID: UUID) {
        val service = viewModel.gatt.value?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)

        if (characteristic != null) {
            val success = viewModel.gatt.value?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()

        // 브로드캐스트 리시버 해제
        requireActivity().unregisterReceiver(receiver)
    }
}