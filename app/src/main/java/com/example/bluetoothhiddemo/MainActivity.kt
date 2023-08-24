package com.example.bluetoothhiddemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothhiddemo.adapter.BTListAdapter
import com.example.bluetoothhiddemo.data.BTData
import com.example.bluetoothhiddemo.databinding.ActivityMainBinding
import com.example.bluetoothhiddemo.tools.HidConsts
import com.example.bluetoothhiddemo.tools.hasPermission
import com.example.bluetoothhiddemo.tools.isEnableBluetooth
import com.example.bluetoothhiddemo.tools.showToast
import com.example.bluetoothhiddemo.utils.HidUtils
import com.example.bluetoothhiddemo.utils.PermissionUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import com.example.bluetoothhiddemo.scrcpy.ScrcpyActivity

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mBinding: ActivityMainBinding

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mRvSetData = ArrayList<BTData>()
    private val mBTAdapter = BTListAdapter(mRvSetData)
    private lateinit var mBackgroundThread: HandlerThread
    private lateinit var mHandler: Handler
    private val START_DISCOVERY_BT = 1001
    private val CANCEL_DISCOVERY_BT = 1002

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH
    )


    var bluetoothPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (isEnableBluetooth()) {
                showToast(R.string.toast_bluetooth_on)
            } else {
                showToast(R.string.toast_bluetooth_off)
            }
        }
    }

    var discoverPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, ": ${it.resultCode}")
        if (it.resultCode == 120) {
//            start()
        }
    }
    var connectPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            showToast(R.string.toast_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initBt()
        initView()
        initHandler()
        registerBroadcast()
        PermissionUtil.checkPermissions(this, permissions)
    }

    private fun initBt() {
        val manager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter
        HidUtils.setBluetoothAdapter(mBluetoothAdapter)
        if (mBluetoothAdapter == null) {
            Toast.makeText(this@MainActivity, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show()
        }
    }
    private fun initView() {

        mBinding.rvList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mBinding.rvList.adapter = mBTAdapter

        mBinding.btDiscovery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31 && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return@setOnClickListener
            }
            if (!isEnableBluetooth()) {
                bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                return@setOnClickListener
            }
            mHandler.sendEmptyMessage(START_DISCOVERY_BT)
//            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
            mBTAdapter.clearData()

        }

        mBinding.btnMouse.setOnClickListener{
            if(HidUtils.isConnected()){
                startActivity(Intent(this,MouseActivity::class.java))
            }
        }

        mBinding.btnKeyboard.setOnClickListener{
            if(HidUtils.isConnected()){
                startActivity(Intent(this,KeyboardActivity::class.java))
            }
        }

        mBinding.btnGame.setOnClickListener{
            if(HidUtils.isConnected()){
                startActivity(Intent(this,GameActivity::class.java))
            }
        }

        mBinding.btnMirror.setOnClickListener{
            startActivity(Intent(this, ScrcpyActivity::class.java))
        }

        mBTAdapter.setOnItemClickListener(object : BTListAdapter.OnItemClickListener {
            override fun onItemClick(name: String, address: String) {

                HidUtils.setStrBtAddress(address)
                HidUtils.registerApp(applicationContext)
                HidConsts.reporters(applicationContext)



//                val intent: Intent =
//                    Intent(this@MainActivity, HidControlActivity::class.java).apply {
//                        putExtra(BLUETOOTH_NAME, name)
//                        putExtra(BLUETOOTH_ADDRESS, address)
//                    }
//                startActivity(intent)
            }
        })
        mBinding.rlConnected.setOnClickListener {
//            val intent: Intent = Intent(this@MainActivity, HidControlActivity::class.java).apply {
//                putExtra(BLUETOOTH_NAME, mBinding.tvName.text.toString())
//                putExtra(BLUETOOTH_ADDRESS, mBinding.tvAddress.text.toString())
//            }
//            startActivity(intent)
        }
    }

    private fun initHandler() {
        mBackgroundThread = HandlerThread("BluetoothBackground")
        mBackgroundThread.start()
        mHandler = object : Handler(mBackgroundThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    START_DISCOVERY_BT -> bluetoothDiscovery()
                    CANCEL_DISCOVERY_BT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PermissionUtil.checkPermission(
                                this@MainActivity, Manifest.permission.BLUETOOTH_SCAN
                            )
                        }
                        mBluetoothAdapter?.cancelDiscovery()
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun bluetoothDiscovery() {
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter?.cancelDiscovery()
        }
        mBluetoothAdapter?.startDiscovery()
    }




    private fun registerBroadcast() {
        val startFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val finishFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val stateFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mBluetoothBroadcast, startFilter)
        registerReceiver(mBluetoothBroadcast, finishFilter)
        registerReceiver(mBluetoothBroadcast, foundFilter)
        registerReceiver(mBluetoothBroadcast, stateFilter)
    }
    // BroadcastReceiver
    private val mBluetoothBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Bluetooth start discovery")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Bluetooth finish discovery")
                }

                BluetoothDevice.ACTION_FOUND -> {
//                    Log.d(TAG, "Bluetooth discovery found")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PermissionUtil.checkPermission(
                            this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT
                        )
                    }
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address
                    if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(deviceHardwareAddress)) {
                        val btData = BTData(deviceName!!, deviceHardwareAddress!!)
                        mRvSetData.add(btData)
                        mBTAdapter.notifyItemChanged(mRvSetData.size - 1)
                    }
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    Log.d(TAG, "Bluetooth cancel discovery")
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothBroadcast)
    }
}