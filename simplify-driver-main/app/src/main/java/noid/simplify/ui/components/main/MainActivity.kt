package noid.simplify.ui.components.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.DateFormat
import noid.simplify.constants.FilterBy
import noid.simplify.constants.Url
import noid.simplify.data.UserPreferences
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityMainBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.services.MyFirebaseMessagingService
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.forgot.ChangePasswordActivity
import noid.simplify.ui.components.jobdetail.JobDetailActivity
import noid.simplify.ui.components.profile.EditProfileActivity
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) ,
    OnLostConnection {

    private var lastPosition = FilterBy.TODAY
    private var startDate = Calendar.getInstance().timeInMillis
    private var endDate = Calendar.getInstance().timeInMillis

    private var latitude = 0.0
    private var longitude = 0.0

    private val editProfileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.viewModel?.apply { this.loadProfile() }
        }
    }

    private val mFusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this@MainActivity)
    }

    private val locationRequest by lazy {
        LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        break
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        when {
            isPermissionGranted() -> {
                startLocationUpdates()
                mFusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        userPreferences.latitude = latitude.toString()
                        userPreferences.longitude = longitude.toString()
                    } else {
                        startLocationUpdates()
                        getLastLocation()
                    }
                }
            }
            else -> {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.isNotEmpty() && permissions.entries.first().value) {
                startLocationUpdates()
                getLastLocation()
            } else {
                showSnackbar(R.string.allow_permission_in_setting, R.string.settings) {
                    Tools.openSettings(this@MainActivity)
                }
            }
        }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        binding.root.let {
            Snackbar.make(it, mainTextStringId, Snackbar.LENGTH_LONG)
                .setAction(actionStringId,listener)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun ActivityMainBinding.onCreate(savedInstanceState: Bundle?) {
        val statusList = resources.getStringArray(R.array.filters).toList()
        val viewModel: MainViewModel by viewModels()
        binding.viewModel = viewModel
        binding.viewModel?.loadProfile()
        binding.presenter = Presenter()

        //set tab item
        binding.statusTab.apply {
            statusList.forEach { this.addTab(this.newTab().setText(it)) }
            this.setMarginEachItems()
            this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    openFragmentByLastPosition(tab?.position ?: 0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) { }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: 0
                    if (position == FilterBy.CUSTOM_DATE) { setDateRangePicker() }
                }
            })
        }

        backToPreviousTabSelected()
        onNewIntent(intent)
        onClickListener()
        getLastLocation()
        observeViewModel()
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.logoutLiveData, ::handleResponseLogout)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun handleResponseLogout(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                response.data?.let {
                    logout()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    override fun onRetry(url: String?) {
        if (url == Url.LOGOUT) binding.viewModel?.logout()
    }

    private fun onClickListener(){
        binding.drawerProfile.edit.setOnClickListener {
            val editProfileActivity = Intent(this,EditProfileActivity::class.java)
            editProfileResult.launch(editProfileActivity)
        }
        binding.drawerProfile.changePassword.setOnClickListener {
            this.startNewActivity(ChangePasswordActivity::class.java)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var jobId = -1
        val extras = intent?.extras

        if (extras != null && extras.containsKey(MyFirebaseMessagingService.JOB_ID)) {
            jobId = extras.getString(MyFirebaseMessagingService.JOB_ID, "-1").toInt()
        }

        if (jobId != -1) {
            JobDetailActivity.open(this@MainActivity, jobId)
        }
    }

    private fun openFragmentByLastPosition(position: Int) {
        if (position == FilterBy.CUSTOM_DATE) {
            setDateRangePicker()
        } else {
            this.lastPosition = position
            openJobFragment(JobFragment.newInstance(lastPosition))
        }
    }

    private fun openJobFragment(fragment: JobFragment) {
        supportFragmentManager.openFragment(R.id.container, fragment)
    }

    private fun setDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        builder.setSelection(Pair(startDate, endDate))

        //set date picker
        val picker = builder.build()
        picker.addOnNegativeButtonClickListener { backToPreviousTabSelected() }
        picker.addOnPositiveButtonClickListener { setJobsByCustomDate(it) }
        picker.show(supportFragmentManager, picker.javaClass.simpleName)
    }

    private fun setJobsByCustomDate(dateRange: Pair<Long, Long>?) {
        dateRange?.let {
            if (it.isNull() || it.first.isNull() || it.second.isNull()) {
                backToPreviousTabSelected()
                this.showToast(getString(R.string.err_failed_to_select_date), Toast.LENGTH_SHORT)
            } else {
                this.lastPosition = FilterBy.CUSTOM_DATE
                this.startDate = it.first!!
                this.endDate = it.second!!
                this.binding.statusTab.getTabAt(FilterBy.CUSTOM_DATE)?.text =
                        it.first.getDateInString(DateFormat.FORMAT_DATE_LONG) +
                                " - " +
                                it.second.getDateInString(DateFormat.FORMAT_DATE_LONG)
                this.openJobFragment(JobFragment.newInstance(
                        indexTab = FilterBy.CUSTOM_DATE,
                        startDate = startDate.getDateInString(),
                        endDate = endDate.getDateInString())
                )
            }
        }
    }

    private fun backToPreviousTabSelected() {
        val tabItem = binding.statusTab.getTabAt(lastPosition)
        tabItem?.select()
        openJobFragment(JobFragment.newInstance(lastPosition))
    }

    class Presenter {
        fun openOrCloseDrawer(context: Context) {
            if (context is MainActivity) context.binding.drawer.openOrClose()
        }
    }

    companion object {
//        const val REQUEST_CODE_EDIT_PROFILE = 301
    }
}