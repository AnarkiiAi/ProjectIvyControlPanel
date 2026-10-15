package com.anarkii.control

import android.app.ActivityManager
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.provider.Settings
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kotlin.math.roundToInt

class MainActivity : FragmentActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var displayManager: DisplayManager
    private lateinit var audioManager: AudioManager
    private lateinit var activityManager: ActivityManager

    private lateinit var ethernetTile: TextView
    private lateinit var bluetoothTile: TextView
    private lateinit var deviceTile: TextView
    private lateinit var audioTile: TextView
    private lateinit var displayTile: TextView

    private lateinit var projectivyTile: TextView
    private lateinit var appsTile: TextView
    private lateinit var storageTile: TextView
    private lateinit var memoryTile: TextView
    private lateinit var updateTile: TextView

    private lateinit var searchButton: View
    private lateinit var settingsButton: View

    private var ethernetConnected = false
    private var projectivyScreenOpening = false
    private var appsScreenOpening = false
    private var updateScreenOpening = false

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private data class LaunchableApp(
        val name: String,
        val packageName: String,
        val activityName: String
    )

    private val ethernetCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                ethernetConnected = true
                updateEthernetTile()
            }

            override fun onLost(network: Network) {
                ethernetConnected =
                    isEthernetCurrentlyConnected()

                updateEthernetTile()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                ethernetConnected =
                    networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET
                    )

                updateEthernetTile()
            }
        }

    private val bluetoothReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (
                    intent?.action ==
                    BluetoothAdapter.ACTION_STATE_CHANGED
                ) {
                    updateBluetoothTile()
                }
            }
        }

    private val displayListener =
        object : DisplayManager.DisplayListener {

            override fun onDisplayAdded(displayId: Int) {
                updateDisplayTile()
            }

            override fun onDisplayRemoved(displayId: Int) {
                updateDisplayTile()
            }

            override fun onDisplayChanged(displayId: Int) {
                if (
                    displayId ==
                    Display.DEFAULT_DISPLAY
                ) {
                    updateDisplayTile()
                }
            }
        }

    private val audioDeviceCallback =
        object : AudioDeviceCallback() {

            override fun onAudioDevicesAdded(
                addedDevices: Array<out AudioDeviceInfo>
            ) {
                updateAudioTile()
            }

            override fun onAudioDevicesRemoved(
                removedDevices: Array<out AudioDeviceInfo>
            ) {
                updateAudioTile()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        applyBackgroundMode(intent)

        ethernetTile =
            findViewById(R.id.ethernetTile)

        bluetoothTile =
            findViewById(R.id.bluetoothTile)

        deviceTile =
            findViewById(R.id.deviceTile)

        audioTile =
            findViewById(R.id.volumeTile)

        displayTile =
            findViewById(R.id.displayTile)

        projectivyTile =
            findViewById(R.id.projectivyTile)

        appsTile =
            findViewById(R.id.appsTile)

        storageTile =
            findViewById(R.id.storageTile)

        memoryTile =
            findViewById(R.id.memoryTile)

        updateTile =
            findViewById(R.id.updateTile)

        searchButton =
            findViewById(R.id.searchButton)

        settingsButton =
            findViewById(R.id.settingsButton)

        searchButton.setOnClickListener {
            openAppSearch()
        }

        settingsButton.setOnClickListener {
            openAndroidSettings()
        }

        connectivityManager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        ethernetConnected =
            isEthernetCurrentlyConnected()

        updateEthernetTile()

        val ethernetRequest =
            NetworkRequest.Builder()
                .addTransportType(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )
                .build()

        connectivityManager
            .registerNetworkCallback(
                ethernetRequest,
                ethernetCallback
            )

        updateBluetoothTile()

        registerReceiver(
            bluetoothReceiver,
            IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED
            )
        )

        updateDeviceTile()

        audioManager =
            getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager

        audioManager
            .registerAudioDeviceCallback(
                audioDeviceCallback,
                mainHandler
            )

        updateAudioTile()

        displayManager =
            getSystemService(
                Context.DISPLAY_SERVICE
            ) as DisplayManager

        displayManager
            .registerDisplayListener(
                displayListener,
                mainHandler
            )

        updateDisplayTile()

        updateProjectivyTile()

        projectivyTile.setOnClickListener {
            openProjectivySettings()
        }

        updateManageAppsTile()

        appsTile.setOnClickListener {
            openManageApps()
        }

        updateStorageTile()

        activityManager =
            getSystemService(
                Context.ACTIVITY_SERVICE
            ) as ActivityManager

        updateMemoryTile()

        memoryTile.setOnClickListener {
            clearBackgroundMemory()
        }

        updateSystemUpdateTile()

        updateTile.setOnClickListener {
            openSystemUpdate()
        }

        setupRemoteNavigation()
    }

    private fun applyBackgroundMode(
        sourceIntent: Intent
    ) {
        val solidBackground =
            sourceIntent.getBooleanExtra(
                "SOLID_BACKGROUND",
                false
            )

        findViewById<View>(
            R.id.anarkiiRoot
        ).setBackgroundColor(
            Color.TRANSPARENT
        )

        val controlPanel =
            findViewById<View>(
                R.id.controlPanel
            )

        if (solidBackground) {

            controlPanel.setBackgroundResource(
                R.drawable.control_panel_black
            )

        } else {

            controlPanel.setBackgroundResource(
                R.drawable.control_panel_glass
            )
        }
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        applyBackgroundMode(intent)
    }

    /*
     * =========================================================
     * HEADER - APP SEARCH
     * =========================================================
     */

    private fun openAppSearch() {

        val allApps =
            getLaunchableApps()

        val filteredApps =
            allApps.toMutableList()

        val dialog =
            Dialog(this)

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    20,
                    24,
                    20
                )

                setBackgroundColor(
                    Color.parseColor("#F21A1A20")
                )
            }

        val title =
            TextView(this).apply {

                text =
                    "Search Apps"

                textSize =
                    18f

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    4,
                    0,
                    4,
                    12
                )
            }

        val searchField =
            EditText(this).apply {

                hint =
                    "Search installed apps"

                textSize =
                    15f

                setSingleLine(true)

                setTextColor(
                    Color.WHITE
                )

                setHintTextColor(
                    Color.parseColor("#9AFFFFFF")
                )

                setBackgroundColor(
                    Color.parseColor("#22FFFFFF")
                )

                setPadding(
                    16,
                    10,
                    16,
                    10
                )
            }

        val listView =
            ListView(this).apply {

                dividerHeight =
                    1

                isFocusable =
                    true

                isFocusableInTouchMode =
                    false
            }

        val adapter =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                filteredApps.map {
                    it.name
                }.toMutableList()
            )

        listView.adapter =
            adapter

        container.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            searchField,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            listView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        dialog.setContentView(
            container
        )

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            setLayout(
                520,
                420
            )

            setGravity(
                Gravity.CENTER
            )

            setSoftInputMode(
                WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE
            )
        }

        searchField.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val query =
                        s
                            ?.toString()
                            ?.trim()
                            ?.lowercase()
                            .orEmpty()

                    filteredApps.clear()

                    if (query.isEmpty()) {

                        filteredApps.addAll(
                            allApps
                        )

                    } else {

                        filteredApps.addAll(
                            allApps.filter {

                                it.name
                                    .lowercase()
                                    .contains(
                                        query
                                    )
                            }
                        )
                    }

                    adapter.clear()

                    adapter.addAll(
                        filteredApps.map {
                            it.name
                        }
                    )

                    adapter.notifyDataSetChanged()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        listView.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            val app =
                filteredApps
                    .getOrNull(
                        position
                    )
                    ?: return@setOnItemClickListener

            dialog.dismiss()

            launchApp(
                app
            )
        }

        dialog.setOnShowListener {

            dialog.window?.setLayout(
                520,
                420
            )

            searchField.requestFocus()

            mainHandler.postDelayed({

                val inputMethodManager =
                    getSystemService(
                        INPUT_METHOD_SERVICE
                    ) as InputMethodManager

                inputMethodManager
                    .showSoftInput(
                        searchField,
                        InputMethodManager
                            .SHOW_IMPLICIT
                    )

            }, 200)
        }

        dialog.show()

        dialog.window?.setLayout(
            520,
            420
        )
    }

    private fun getLaunchableApps():
            List<LaunchableApp> {

        val packageManager =
            packageManager

        val result =
            mutableMapOf<
                    String,
                    LaunchableApp
                    >()

        val tvIntent =
            Intent(
                Intent.ACTION_MAIN
            ).apply {

                addCategory(
                    Intent.CATEGORY_LEANBACK_LAUNCHER
                )
            }

        val standardIntent =
            Intent(
                Intent.ACTION_MAIN
            ).apply {

                addCategory(
                    Intent.CATEGORY_LAUNCHER
                )
            }

        val resolvedApps =
            packageManager
                .queryIntentActivities(
                    tvIntent,
                    0
                ) +
                    packageManager
                        .queryIntentActivities(
                            standardIntent,
                            0
                        )

        resolvedApps.forEach {
                resolveInfo ->

            val activityInfo =
                resolveInfo.activityInfo
                    ?: return@forEach

            val packageName =
                activityInfo.packageName

            if (
                packageName ==
                this.packageName
            ) {
                return@forEach
            }

            val label =
                resolveInfo
                    .loadLabel(
                        packageManager
                    )
                    ?.toString()
                    ?.trim()

            val appName =
                if (
                    label.isNullOrBlank()
                ) {
                    packageName
                } else {
                    label
                }

            result[packageName] =
                LaunchableApp(
                    name =
                        appName,
                    packageName =
                        packageName,
                    activityName =
                        activityInfo.name
                )
        }

        return result
            .values
            .sortedBy {
                it.name.lowercase()
            }
    }

    private fun launchApp(
        app: LaunchableApp
    ) {

        try {

            val launchIntent =
                packageManager
                    .getLeanbackLaunchIntentForPackage(
                        app.packageName
                    )
                    ?: packageManager
                        .getLaunchIntentForPackage(
                            app.packageName
                        )
                    ?: Intent().apply {

                        setClassName(
                            app.packageName,
                            app.activityName
                        )

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

            startActivity(
                launchIntent
            )

        } catch (
            _: Exception
        ) {
        }
    }

    /*
     * =========================================================
     * HEADER - ANDROID SETTINGS
     * =========================================================
     */

    private fun openAndroidSettings() {

        try {

            startActivity(
                Intent(
                    Settings.ACTION_SETTINGS
                )
            )

        } catch (
            _: Exception
        ) {
        }
    }

    /*
     * =========================================================
     * REMOTE NAVIGATION
     * =========================================================
     */

    private fun setupRemoteNavigation() {

        val allTiles =
            listOf(
                ethernetTile,
                bluetoothTile,
                deviceTile,
                audioTile,
                displayTile,
                projectivyTile,
                appsTile,
                storageTile,
                memoryTile,
                updateTile
            )

        allTiles.forEach { tile ->

            tile.isFocusable = true
            tile.isFocusableInTouchMode = false
            tile.isClickable = true

            tile.onFocusChangeListener =
                View.OnFocusChangeListener {
                        view,
                        hasFocus ->

                    val card =
                        view.parent as? View
                            ?: view

                    if (hasFocus) {

                        card.setBackgroundResource(
                            R.drawable.control_tile_focused
                        )

                        card.alpha =
                            1.0f

                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.LOLLIPOP
                        ) {

                            card.elevation =
                                10f
                        }

                    } else {

                        card.setBackgroundResource(
                            R.drawable.control_tile_glass
                        )

                        card.alpha =
                            1.0f

                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.LOLLIPOP
                        ) {

                            card.elevation =
                                0f
                        }
                    }
                }
        }

        searchButton.isFocusable =
            true

        searchButton.isFocusableInTouchMode =
            false

        searchButton.isClickable =
            true

        settingsButton.isFocusable =
            true

        settingsButton.isFocusableInTouchMode =
            false

        settingsButton.isClickable =
            true

        searchButton.onFocusChangeListener =
            View.OnFocusChangeListener {
                    view,
                    hasFocus ->

                if (hasFocus) {

                    view.setBackgroundResource(
                        R.drawable.header_button_focused
                    )

                    view.alpha =
                        1.0f

                } else {

                    view.background =
                        null

                    view.alpha =
                        0.72f
                }
            }

        settingsButton.onFocusChangeListener =
            View.OnFocusChangeListener {
                    view,
                    hasFocus ->

                if (hasFocus) {

                    view.setBackgroundResource(
                        R.drawable.header_button_focused
                    )

                    view.alpha =
                        1.0f

                } else {

                    view.background =
                        null

                    view.alpha =
                        0.72f
                }
            }

        ethernetTile.nextFocusLeftId =
            ethernetTile.id

        ethernetTile.nextFocusRightId =
            bluetoothTile.id

        bluetoothTile.nextFocusLeftId =
            ethernetTile.id

        bluetoothTile.nextFocusRightId =
            deviceTile.id

        deviceTile.nextFocusLeftId =
            bluetoothTile.id

        deviceTile.nextFocusRightId =
            audioTile.id

        audioTile.nextFocusLeftId =
            deviceTile.id

        audioTile.nextFocusRightId =
            displayTile.id

        displayTile.nextFocusLeftId =
            audioTile.id

        displayTile.nextFocusRightId =
            displayTile.id

        projectivyTile.nextFocusLeftId =
            projectivyTile.id

        projectivyTile.nextFocusRightId =
            appsTile.id

        appsTile.nextFocusLeftId =
            projectivyTile.id

        appsTile.nextFocusRightId =
            storageTile.id

        storageTile.nextFocusLeftId =
            appsTile.id

        storageTile.nextFocusRightId =
            memoryTile.id

        memoryTile.nextFocusLeftId =
            storageTile.id

        memoryTile.nextFocusRightId =
            updateTile.id

        updateTile.nextFocusLeftId =
            memoryTile.id

        updateTile.nextFocusRightId =
            updateTile.id

        ethernetTile.nextFocusDownId =
            projectivyTile.id

        projectivyTile.nextFocusUpId =
            ethernetTile.id

        bluetoothTile.nextFocusDownId =
            appsTile.id

        appsTile.nextFocusUpId =
            bluetoothTile.id

        deviceTile.nextFocusDownId =
            storageTile.id

        storageTile.nextFocusUpId =
            deviceTile.id

        audioTile.nextFocusDownId =
            memoryTile.id

        memoryTile.nextFocusUpId =
            audioTile.id

        displayTile.nextFocusDownId =
            updateTile.id

        updateTile.nextFocusUpId =
            displayTile.id

        displayTile.nextFocusUpId =
            settingsButton.id

        settingsButton.nextFocusDownId =
            displayTile.id

        settingsButton.nextFocusLeftId =
            searchButton.id

        settingsButton.nextFocusRightId =
            settingsButton.id

        searchButton.nextFocusRightId =
            settingsButton.id

        searchButton.nextFocusLeftId =
            searchButton.id

        searchButton.nextFocusDownId =
            displayTile.id

        searchButton.nextFocusUpId =
            searchButton.id

        settingsButton.nextFocusUpId =
            settingsButton.id

        ethernetTile.nextFocusUpId =
            ethernetTile.id

        bluetoothTile.nextFocusUpId =
            bluetoothTile.id

        deviceTile.nextFocusUpId =
            deviceTile.id

        audioTile.nextFocusUpId =
            audioTile.id

        projectivyTile.nextFocusDownId =
            projectivyTile.id

        appsTile.nextFocusDownId =
            appsTile.id

        storageTile.nextFocusDownId =
            storageTile.id

        memoryTile.nextFocusDownId =
            memoryTile.id

        updateTile.nextFocusDownId =
            updateTile.id

        ethernetTile.post {
            ethernetTile.requestFocus()
        }
    }

    /*
     * =========================================================
     * ETHERNET
     * =========================================================
     */

    private fun isEthernetCurrentlyConnected():
            Boolean {

        return connectivityManager
            .allNetworks
            .any { network ->

                val capabilities =
                    connectivityManager
                        .getNetworkCapabilities(
                            network
                        )

                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                ) == true &&
                        capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET
                        )
            }
    }

    private fun updateEthernetTile() {

        runOnUiThread {

            val status =
                if (ethernetConnected) {
                    "Connected"
                } else {
                    "Disconnected"
                }

            val statusColor =
                if (ethernetConnected) {
                    Color.parseColor("#78E84F")
                } else {
                    Color.parseColor("#FF5F57")
                }

            ethernetTile.text =
                createStatusText(
                    title = "Ethernet",
                    status = status,
                    statusColor = statusColor
                )
        }
    }

    /*
     * =========================================================
     * BLUETOOTH
     * =========================================================
     */

    private fun updateBluetoothTile() {

        val bluetoothManager =
            getSystemService(
                Context.BLUETOOTH_SERVICE
            ) as BluetoothManager

        val bluetoothAdapter =
            bluetoothManager.adapter

        val bluetoothEnabled =
            bluetoothAdapter?.isEnabled == true

        runOnUiThread {

            val status =
                if (bluetoothEnabled) {
                    "On"
                } else {
                    "Off"
                }

            val statusColor =
                if (bluetoothEnabled) {
                    Color.parseColor("#78E84F")
                } else {
                    Color.parseColor("#FF5F57")
                }

            bluetoothTile.text =
                createStatusText(
                    title = "Bluetooth",
                    status = status,
                    statusColor = statusColor
                )
        }
    }

    /*
     * =========================================================
     * DEVICE
     * =========================================================
     */

    private fun updateDeviceTile() {

        var deviceName =
            Settings.Global.getString(
                contentResolver,
                "device_name"
            )

        if (deviceName.isNullOrBlank()) {
            deviceName =
                Build.MODEL
        }

        if (
            deviceName.equals(
                "SHIELD Android TV",
                ignoreCase = true
            )
        ) {
            deviceName =
                "SHIELD TV"
        }

        runOnUiThread {

            deviceTile.text =
                createStatusText(
                    title = "Device",
                    status = deviceName,
                    statusColor =
                        Color.parseColor("#C879FF")
                )
        }
    }

    /*
     * =========================================================
     * AUDIO
     * =========================================================
     */

    private fun updateAudioTile() {

        val outputDevices =
            audioManager.getDevices(
                AudioManager.GET_DEVICES_OUTPUTS
            )

        val status =
            when {

                outputDevices.any {

                    it.type ==
                            AudioDeviceInfo.TYPE_HDMI ||

                            it.type ==
                            AudioDeviceInfo.TYPE_HDMI_ARC ||

                            (
                                    Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.S &&
                                            it.type ==
                                            AudioDeviceInfo.TYPE_HDMI_EARC
                                    )

                } -> {
                    "HDMI"
                }

                outputDevices.any {

                    it.type ==
                            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP

                } -> {
                    "Bluetooth"
                }

                outputDevices.any {

                    it.type ==
                            AudioDeviceInfo.TYPE_USB_DEVICE ||

                            it.type ==
                            AudioDeviceInfo.TYPE_USB_HEADSET

                } -> {
                    "USB Audio"
                }

                outputDevices.any {

                    it.type ==
                            AudioDeviceInfo.TYPE_LINE_ANALOG ||

                            it.type ==
                            AudioDeviceInfo.TYPE_LINE_DIGITAL

                } -> {
                    "Line Out"
                }

                outputDevices.any {

                    it.type ==
                            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER

                } -> {
                    "Speaker"
                }

                else -> {
                    "Unknown"
                }
            }

        runOnUiThread {

            audioTile.text =
                createStatusText(
                    title = "Audio",
                    status = status,
                    statusColor =
                        Color.parseColor("#FF6B6B")
                )
        }
    }

    /*
     * =========================================================
     * DISPLAY
     * =========================================================
     */

    private fun updateDisplayTile() {

        val display =
            displayManager.getDisplay(
                Display.DEFAULT_DISPLAY
            ) ?: return

        val mode =
            display.mode

        val width =
            mode.physicalWidth

        val height =
            mode.physicalHeight

        val refreshRate =
            mode.refreshRate
                .roundToInt()

        val verticalResolution =
            minOf(
                width,
                height
            )

        val resolution =
            when {

                verticalResolution >= 2160 ->
                    "2160p"

                verticalResolution >= 1440 ->
                    "1440p"

                verticalResolution >= 1080 ->
                    "1080p"

                verticalResolution >= 720 ->
                    "720p"

                else ->
                    "${verticalResolution}p"
            }

        val hdrText =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O &&
                display.isHdr
            ) {
                "HDR"
            } else {
                "SDR"
            }

        val status =
            "$resolution $refreshRate $hdrText"

        runOnUiThread {

            displayTile.text =
                createStatusText(
                    title = "Display",
                    status = status,
                    statusColor =
                        Color.parseColor("#FFC84A")
                )
        }
    }

    /*
     * =========================================================
     * PROJECTIVY
     * =========================================================
     */

    private fun updateProjectivyTile() {

        projectivyTile.text =
            createStatusText(
                title = "Projectivy",
                status = "Settings",
                statusColor =
                    Color.parseColor("#79E6F2")
            )
    }

    private fun openProjectivySettings() {

        if (projectivyScreenOpening) {
            return
        }

        projectivyScreenOpening =
            true

        projectivyTile.isClickable =
            false

        projectivyTile.text =
            createStatusText(
                title = "Projectivy",
                status = "Opening...",
                statusColor =
                    Color.parseColor("#FFC84A")
            )

        mainHandler.postDelayed({

            try {

                val projectivyIntent =
                    Intent(
                        "tv.projectivy.SETTINGS"
                    )

                startActivity(
                    projectivyIntent
                )

            } catch (
                _: Exception
            ) {

                projectivyScreenOpening =
                    false

                projectivyTile.isClickable =
                    true

                projectivyTile.text =
                    createStatusText(
                        title = "Projectivy",
                        status = "Unavailable",
                        statusColor =
                            Color.parseColor("#FF5F57")
                    )
            }

        }, 200)
    }

    /*
     * =========================================================
     * MANAGE APPS
     * =========================================================
     */

    private fun updateManageAppsTile() {

        appsTile.text =
            createStatusText(
                title = "Manage Apps",
                status = "Open",
                statusColor =
                    Color.parseColor("#79E6F2")
            )
    }

    private fun openManageApps() {

        if (appsScreenOpening) {
            return
        }

        appsScreenOpening =
            true

        appsTile.isClickable =
            false

        appsTile.text =
            createStatusText(
                title = "Manage Apps",
                status = "Opening...",
                statusColor =
                    Color.parseColor("#FFC84A")
            )

        mainHandler.postDelayed({

            try {

                val appsIntent =
                    Intent(
                        Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
                    )

                startActivity(
                    appsIntent
                )

            } catch (
                _: Exception
            ) {

                appsScreenOpening =
                    false

                appsTile.isClickable =
                    true

                appsTile.text =
                    createStatusText(
                        title = "Manage Apps",
                        status = "Unavailable",
                        statusColor =
                            Color.parseColor("#FF5F57")
                    )
            }

        }, 200)
    }

    /*
     * =========================================================
     * STORAGE
     * =========================================================
     */

    private fun updateStorageTile() {

        val statFs =
            StatFs(
                Environment
                    .getDataDirectory()
                    .path
            )

        val totalBytes =
            statFs.totalBytes

        val availableBytes =
            statFs.availableBytes

        val usedBytes =
            totalBytes -
                    availableBytes

        val usedPercent =
            if (totalBytes > 0) {

                (
                        usedBytes.toDouble() /
                                totalBytes.toDouble() *
                                100.0
                        ).roundToInt()

            } else {
                0
            }

        runOnUiThread {

            storageTile.text =
                createStatusText(
                    title = "Storage",
                    status =
                        "$usedPercent% Used",
                    statusColor =
                        Color.parseColor("#FF9655")
                )
        }
    }

    /*
     * =========================================================
     * MEMORY
     * =========================================================
     */

    private fun updateMemoryTile() {

        val memoryInfo =
            ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(
            memoryInfo
        )

        val totalMemory =
            memoryInfo.totalMem

        val availableMemory =
            memoryInfo.availMem

        val freePercent =
            if (totalMemory > 0) {

                (
                        availableMemory.toDouble() /
                                totalMemory.toDouble() *
                                100.0
                        ).roundToInt()

            } else {
                0
            }

        runOnUiThread {

            memoryTile.text =
                createStatusText(
                    title = "Clear Memory",
                    status =
                        "$freePercent% Free",
                    statusColor =
                        Color.WHITE
                )
        }
    }

    private fun clearBackgroundMemory() {

        memoryTile.isClickable =
            false

        memoryTile.text =
            createStatusText(
                title = "Clear Memory",
                status = "Clearing...",
                statusColor =
                    Color.parseColor("#FFC84A")
            )

        Thread {

            try {

                val runningProcesses =
                    activityManager
                        .runningAppProcesses

                runningProcesses
                    ?.forEach { process ->

                        if (
                            process.importance >=
                            ActivityManager
                                .RunningAppProcessInfo
                                .IMPORTANCE_BACKGROUND
                        ) {

                            process.pkgList
                                ?.forEach {
                                        packageName ->

                                    if (
                                        packageName !=
                                        this@MainActivity
                                            .packageName
                                    ) {

                                        try {

                                            activityManager
                                                .killBackgroundProcesses(
                                                    packageName
                                                )

                                        } catch (
                                            _: Exception
                                        ) {
                                        }
                                    }
                                }
                        }
                    }

            } catch (
                _: Exception
            ) {
            }

            mainHandler.postDelayed({

                memoryTile.text =
                    createStatusText(
                        title =
                            "Clear Memory",
                        status =
                            "Cleared",
                        statusColor =
                            Color.parseColor("#78E84F")
                    )

                mainHandler.postDelayed({

                    updateMemoryTile()

                    memoryTile.isClickable =
                        true

                }, 1500)

            }, 700)

        }.start()
    }

    /*
     * =========================================================
     * SYSTEM UPDATE
     * =========================================================
     */

    private fun updateSystemUpdateTile() {

        updateTile.text =
            createStatusText(
                title = "System Update",
                status = "Check",
                statusColor =
                    Color.parseColor("#78E84F")
            )
    }

    private fun openSystemUpdate() {

        if (updateScreenOpening) {
            return
        }

        updateScreenOpening =
            true

        updateTile.isClickable =
            false

        updateTile.text =
            createStatusText(
                title = "System Update",
                status = "Checking...",
                statusColor =
                    Color.parseColor("#FFC84A")
            )

        mainHandler.postDelayed({

            try {

                val updateIntent =
                    Intent(
                        "android.settings.SYSTEM_QUICK_UPDATE_SETTINGS"
                    )

                startActivity(
                    updateIntent
                )

            } catch (
                _: Exception
            ) {

                updateScreenOpening =
                    false

                updateTile.isClickable =
                    true

                updateTile.text =
                    createStatusText(
                        title =
                            "System Update",
                        status =
                            "Unavailable",
                        statusColor =
                            Color.parseColor("#FF5F57")
                    )
            }

        }, 250)
    }

    /*
     * =========================================================
     * COLOURED STATUS TEXT
     * =========================================================
     */

    private fun createStatusText(
        title: String,
        status: String,
        statusColor: Int
    ): SpannableString {

        val fullText =
            "$title\n$status"

        val styledText =
            SpannableString(
                fullText
            )

        styledText.setSpan(
            ForegroundColorSpan(
                Color.WHITE
            ),
            0,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        styledText.setSpan(
            ForegroundColorSpan(
                statusColor
            ),
            title.length + 1,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return styledText
    }

    /*
     * =========================================================
     * RESUME
     * =========================================================
     */

    override fun onResume() {
        super.onResume()

        if (::bluetoothTile.isInitialized) {
            updateBluetoothTile()
        }

        if (::deviceTile.isInitialized) {
            updateDeviceTile()
        }

        if (::audioManager.isInitialized) {
            updateAudioTile()
        }

        if (::displayManager.isInitialized) {
            updateDisplayTile()
        }

        if (
            ::projectivyTile.isInitialized &&
            projectivyScreenOpening
        ) {

            projectivyScreenOpening =
                false

            projectivyTile.isClickable =
                true

            updateProjectivyTile()
        }

        if (
            ::appsTile.isInitialized &&
            appsScreenOpening
        ) {

            appsScreenOpening =
                false

            appsTile.isClickable =
                true

            updateManageAppsTile()
        }

        if (::storageTile.isInitialized) {
            updateStorageTile()
        }

        if (::activityManager.isInitialized) {
            updateMemoryTile()
        }

        if (
            ::updateTile.isInitialized &&
            updateScreenOpening
        ) {

            updateScreenOpening =
                false

            updateTile.isClickable =
                true

            updateSystemUpdateTile()
        }
    }

    /*
     * =========================================================
     * CLEANUP
     * =========================================================
     */

    override fun onDestroy() {
        super.onDestroy()

        try {

            connectivityManager
                .unregisterNetworkCallback(
                    ethernetCallback
                )

        } catch (
            _: Exception
        ) {
        }

        try {

            unregisterReceiver(
                bluetoothReceiver
            )

        } catch (
            _: Exception
        ) {
        }

        try {

            audioManager
                .unregisterAudioDeviceCallback(
                    audioDeviceCallback
                )

        } catch (
            _: Exception
        ) {
        }

        try {

            displayManager
                .unregisterDisplayListener(
                    displayListener
                )

        } catch (
            _: Exception
        ) {
        }
    }
}