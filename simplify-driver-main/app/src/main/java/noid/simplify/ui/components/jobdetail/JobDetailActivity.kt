package noid.simplify.ui.components.jobdetail

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isInvisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ncorti.slidetoact.SlideToActView
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.BuildConfig
import noid.simplify.R
import noid.simplify.constants.*
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.*
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityJobDetailBinding
import noid.simplify.interfaces.OnConfirmationListener
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.addtionalitem.AdditionalItemsSheet
import noid.simplify.ui.components.clientremarks.ClientRemarksSheet
import noid.simplify.ui.components.clientremarks.JobRemarkSheet
import noid.simplify.ui.components.equipment.EquipmentDetailActivity
import noid.simplify.ui.components.others.DocumentsSheet
import noid.simplify.ui.components.others.ListMapAppSheet
import noid.simplify.ui.components.others.SignatureDialog
import noid.simplify.ui.components.others.TakeOrSelectPictureSheet
import noid.simplify.ui.components.payment.PaymentActivity
import noid.simplify.ui.components.payment.PaymentSuccessActivity
import noid.simplify.utils.FileUtil
import noid.simplify.utils.RecyclerUtil
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class JobDetailActivity : BaseActivity<ActivityJobDetailBinding>({
    ActivityJobDetailBinding.inflate(
            it
    )
}), OnLostConnection{
    private val shareViewModel: ShareAdditionalItemViewModel by viewModels()
    private val additionalAdapter by lazy { JobServiceItemAdapter() }
    private val checklistJobAdapter by lazy { JobSubtaskAdapter(isEnable = false) }
    private val noteAdapter by lazy { JobNoteAdapter() }
    private val permissionsTakePicture = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var jobId = 0
    private var currentPhotoPath: String? = null
    private var jobToComplete: JobToComplete? = null
    private var jobToPaused: JobToPaused? = null
    private var service: AdditionalService? = null
    private var notePosToDelete = -1
    private var noteDelete = NoteDelete()
    private var isPayment = false
    private var jobDetail: JobDetail? = null
    private val itemTemplates = mutableListOf<ServiceItemTemplate>()
    private var newNote: Note? = null
    private var signature: Bitmap? = null
    private var signatureFilePath: String? = null
    private var signatureFileName = Tools.getImageName()
    private var signatureUploadUrl: String? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.job_detail))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityJobDetailBinding.onCreate(savedInstanceState: Bundle?) {
        val viewModel: JobDetailViewModel by viewModels()
        this@JobDetailActivity.apply {
            this.jobId = intent.getIntExtra(EXTRA_JOB_ID, 0)
            RecyclerUtil.setRecyclerLinear(this, binding.listNotes, noteAdapter, isHasFixedSize = false)
            RecyclerUtil.setRecyclerLinear(this, binding.listAdditional, additionalAdapter)
            RecyclerUtil.setRecyclerLinear(this, binding.listCheckListJob, checklistJobAdapter)
        }

        binding.viewModel = viewModel
        binding.viewModel?.apply {
            this.fetchJob(jobId)
            this.fetchServiceItemTemplates()
        }
        observeViewModel()
        setOnClickListenerOnView()
    }

    override fun onRetry(url: String?) {
        val jobUrl = "${Url.JOBS}/$jobId"
        val getSignatureUrl  = "${Url.GET_URL_UPLOAD_SIGNATURE}/$signatureFileName"

        binding.viewModel?.run {
            when (url) {
                jobUrl -> {
                    if (isPayment) this.doCompleteJob(jobId, jobToComplete) else this.fetchJob(jobId)
                }

                Url.SERVICE_ITEM_TEMPLATES -> this.fetchServiceItemTemplates()

                Url.JOB_NOTES -> this.doAddNote()

                getSignatureUrl -> this.getUrlUploadSignature(jobId, signatureFileName)

                signatureUploadUrl -> uploadSignature()

                else -> this.uploadNoteImage(url.toString(), currentPhotoPath)
            }
        }
    }

    private var resultCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setImageViewResource()
        }
    }

    private var resultGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            currentPhotoPath = FileUtil.getPath(this, intent?.data)
            setImageViewResource()
        }
    }

    private fun setImageViewResource() {
        binding.noteImage.showImage(currentPhotoPath.toBitmap())
        binding.viewModel?.setNewNoteImage(currentPhotoPath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PermissionCode.REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchSelectPictureIntent()
                } else {
                    this.showToast(getString(R.string.err_permission_read_need))
                }
            }
            PermissionCode.REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    this.showToast(getString(R.string.err_permission_camera_need))
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun setOnClickListenerOnView() {
        checklistJobAdapter.taskStatusListener = {
            jobDetail?.progressPercentageInt()?.let{ taskProgress ->
                binding.progressBar1.progress = taskProgress

            }
            binding.tvJobProgress.text = getString(R.string.task_progress,jobDetail?.doneSubtask().toString(),jobDetail?.totalSubtask().toString())
        }

        binding.editRemarks.setOnClickListener {
            val sheet = ClientRemarksSheet.newInstance(jobDetail?.clientId, jobDetail?.clientRemarks)
            sheet.listener = {
                binding.remarks.text = it
                jobDetail?.clientRemarks = it
            }
            sheet.show(supportFragmentManager)
        }
        binding.addItem.setOnClickListener {
            val sheet = AdditionalItemsSheet.newInstance(additionalAdapter.items)
            sheet.listener = { this.reloadActivity() }
            sheet.show(supportFragmentManager)
        }
        binding.showPreviousNotes.setOnClickListener {
            PreviousNotesSheet.newInstance(jobId).show(supportFragmentManager)
        }
        binding.addNote.setOnClickListener { addNote() }
        binding.cancelNote.setOnClickListener { clearAddNoteField() }
        binding.noteImage.setOnClickListener { openTakePictureDialog() }
        binding.choosePayment.setOnClickListener {
            if(jobDetail?.getChecklistJobDone()==true){
                PaymentActivity.open(
                    context = this,
                    jobDetail = jobDetail,
                    jobToComplete = jobToComplete,
                    service = service
                )
            }else{
                val sheet = JobRemarkSheet.newInstance()
                sheet.listener = {
                    jobToComplete?.remarks = it
                    PaymentActivity.open(
                        context = this,
                        jobDetail = jobDetail,
                        jobToComplete = jobToComplete,
                        service = service
                    )
                }
                sheet.show(supportFragmentManager)
            }
        }
        binding.updateJob.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                val inProgress = jobDetail?.jobStatus == General.STATUS__IN_PROGRESS
                if (inProgress) {
                    if(jobDetail?.getChecklistJobDone() == true) openSignatureDialog()
                    else{
                        val sheet = JobRemarkSheet.newInstance()
                        sheet.listener = {
                            jobToComplete?.remarks = it
                            openSignatureDialog()
                        }
                        sheet.show(supportFragmentManager)
                    }
                } else {
                    jobToPaused = JobToPaused(General.STATUS__IN_PROGRESS, "${userPreferences.latitude},${userPreferences.longitude}")
                    binding.viewModel?.doUpdateJob(jobId, jobToPaused)
                }
            }
        }
        noteAdapter.deleteNoteListener = {
            confirmationDeleteNote(it)
        }
        noteAdapter.downloadNoteListener ={
            downloadImage(it.imageUrl)
        }
        additionalAdapter.itemListener = {
            DescriptionServiceItemSheet.newInstance(it).show(fm = supportFragmentManager)
        }

        checklistJobAdapter.itemListener = {
            DescriptionServiceItemSheet.newInstance(it).show(fm = supportFragmentManager)
        }

        binding.btnDocumentDetails.setOnClickListener {
            val sheet = jobDetail?.jobDocuments?.let { DocumentsSheet(it) }
            sheet?.show(supportFragmentManager)
        }

        binding.btnPause.setOnClickListener {
            this.showConfirmationDialog(R.string.confirmation_pause_job,
                object : OnConfirmationListener {
                    override fun onConfirm() {
                        jobToPaused = JobToPaused(General.STATUS__PAUSED, "${userPreferences.latitude},${userPreferences.longitude}")
                        binding.viewModel?.doUpdateJob(jobId,jobToPaused)
                    }
                }
            )
        }

        binding.btnResume.setOnClickListener {
            this.showConfirmationDialog(R.string.confirmation_resume_job,
                object : OnConfirmationListener {
                    override fun onConfirm() {
                        jobToPaused = JobToPaused(General.STATUS__IN_PROGRESS, "${userPreferences.latitude},${userPreferences.longitude}")
                        binding.viewModel?.doUpdateJob(jobId,jobToPaused)
                    }
                }
            )
        }

        binding.openMap.setOnClickListener {
            val sheet = jobDetail?.postalCode?.let { ListMapAppSheet.newInstance(it) }
            sheet?.show(supportFragmentManager)
        }

        //btn intent ke halaman equipment details
        binding.btnEquipmentDetails.setOnClickListener{
            startActivity(Intent(this, EquipmentDetailActivity::class.java))
        }
    }

    private fun observeViewModel() {
        binding.viewModel?.run {
            observeData(this.noteMediator) { binding.saveNote.isEnabled = it }
            observeData(this.jobDetailLiveData, ::responseGetJobDetail)
            observeData(this.itemTemplatesLiveData, ::responseItemTemplates)
            observeData(this.addNoteLiveData, ::responseAddNote)
            observeData(this.uploadNoteImgLiveData, ::responseUploadNoteImg)
            observeData(this.updateJobStatusLiveData, ::responseUpdateJobStatus)
            observeData(this.deleteNoteLiveData, ::responseDeleteNote)
            observeData(this.getSignatureUrlLiveData, ::responseGetUrlSignature)
            observeData(this.uploadSignatureLiveData, ::responseUploadSignature)

            //observe error code to show message
            observeError(
                    event = this.errorLiveData,
                    fragmentManager = supportFragmentManager,
                    onLostConnection = this@JobDetailActivity
            )
        }
    }

    private fun checkJobActive() {
        jobDetail?.let {

            val totalAmount = it.totalAmountService ?: 0.0
            val inPaused = it.jobStatus == General.STATUS__PAUSED
            val inProgress = it.jobStatus == General.STATUS__IN_PROGRESS
            val unCompleted = it.jobStatus != General.STATUS__COMPLETED
            val inCompleted = it.jobStatus == General.STATUS__COMPLETED
            checklistJobAdapter.setEnable(inProgress)
            binding.choosePayment.visible(isVisible = inProgress && totalAmount > 0.0)
            binding.addItem.visible(inProgress)
            binding.addNote.visible(isVisible = inProgress)
            binding.btnPause.visible(isVisible = inProgress || inPaused)
            binding.progressLayout.visible(checklistJobAdapter.items.isNotEmpty())
            binding.lyJobNotes.visible(isVisible = inProgress || inCompleted || inPaused)
            binding.updateJob.visible(isVisible = unCompleted)
            binding.editRemarks.visible(isVisible = inProgress)
            binding.textSubtaskMessage.visible(checklistJobAdapter.items.isEmpty())
            binding.listCheckListJob.visible(checklistJobAdapter.items.isNotEmpty())
            if(inPaused) binding.btnPause.isInvisible = inPaused
            binding.btnResume.visible(isVisible = inPaused)

            binding.btnDocumentDetails.visible(jobDetail?.jobDocuments?.isEmpty()==false)
            binding.tvDocument.visible(jobDetail?.jobDocuments?.isEmpty()==false)

            val addItemText = if (additionalAdapter.itemCount > 0) {
                getString(R.string.edit_items)
            } else getString(R.string.add_item)

            binding.addItem.text = addItemText

            if (inProgress) {
                if (totalAmount == 0.0) {
                    binding.updateJob.text = getString(R.string.swipe_to_complete)
                } else binding.updateJob.visible(false)
            } else {
                binding.updateJob.text = getString(R.string.swipe_to_start)
            }
            if(inPaused){
                binding.choosePayment.visible(false)
                binding.updateJob.visible(false)
        }
        }
    }


    private fun takeOrSelectPicture(requestCode: Int) {
        if (requestCode == PermissionCode.REQUEST_GALLERY) {
            if (!hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionCode.REQUEST_GALLERY)
            } else {
                dispatchSelectPictureIntent()
            }
        } else {
            if (!hasPermissions(*permissionsTakePicture)) {
                ActivityCompat.requestPermissions(this, permissionsTakePicture, PermissionCode.REQUEST_CAMERA)
            } else {
                dispatchTakePictureIntent()
            }
        }
    }

    private fun setJobToCompleteAndAdditionalService() {
        jobDetail?.let {
            jobToComplete = JobToComplete(
                    startTime = it.startDateTime ?: "",
                    endTime = it.endDateTime ?: "",
                    vehicle = it.vehicleJobs
            )
            val service = AdditionalService(
                needGST = it.needGST ?: false,
                additionalServiceId = it.additionalServiceId ?: 0,
                serviceTitle = it.additionalServiceTitle ?: "",
                serviceAddressId = it.serviceAddressId ?: 0,
                serviceItems = it.additionalServiceItem,
                gstAmount = it.getGstAmountForAdditional(),
                clientId = it.clientId ?: 0,
                entityId = it.entityId ?: 0,
                jobId = it.jobId ?: 0,
                totalAmount = it.getGrandTotalAdditionalItems(),
                originalAmount = it.getSubTotalAdditionalItems()
            )
            shareViewModel.setAdditionalService(service)
            this.handleAdditionalService(service)
        }
    }

    private fun addNote() {
        binding.actionLayout.visible(false)
        binding.addNoteLayout.visible(true)
        binding.addNote.visible(false)
        scrollToBottom()
        binding.noteField.editText?.requestFocus()
    }

    private fun clearAddNoteField() {
        newNote?.let {
            it.imageUrl = currentPhotoPath
            noteAdapter.addItem(it)
        }
        binding.textNoteMessage.visible(noteAdapter.itemCount == 0)
        binding.listNotes.visible(noteAdapter.itemCount > 0)
        binding.addNoteLayout.visible(false)
        binding.addNote.visible(true)
        binding.actionLayout.visible(true)
        binding.noteImage.setImageResource(R.drawable.add_holder)
        binding.noteField.editText?.clearFocus()
        binding.noteField.editText?.text?.clear()
        binding.noteField.error = null
        newNote = null
        currentPhotoPath = null
        scrollToBottom()
    }

    private fun dispatchSelectPictureIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.type = "image/*"
            resultGallery.launch(it)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    this.showToast(getString(R.string.err_take_picture_failed))
                    null
                }

                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultCamera.launch(takePictureIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(DateFormat.FORMAT_DATE_FOR_IMAGE, Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_NOTE_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun scrollToBottom() {
        binding.nestedScroll.post {
            binding.nestedScroll.smoothScrollTo(0, binding.border3.y.toInt())
        }
    }

    private fun handleAdditionalService(service: AdditionalService?) {
        if (service != null) {
            this.additionalAdapter.setItems(service.serviceItems)
            this.jobDetail?.let { job ->
                job.additionalServiceItem = service.serviceItems
                val gstTotal = job.getGstAmountForAdditional()
                val totalAdditional = job.getGrandTotalAdditionalItems()
                val contractBalance = jobDetail?.contractBalance ?: 0.0
                val balance = if (contractBalance < 0) 0.0 else contractBalance
                val grandTotal = balance + totalAdditional
                job.totalAmountService = grandTotal

                binding.gstAmount.text = gstTotal.toPriceAmount()
            }
            this.checkJobActive()
        }

        // set additional items adapter
        binding.textAdditionalMessage.visible(additionalAdapter.itemCount == 0)
        binding.listAdditional.visible(additionalAdapter.itemCount > 0)
    }

    private fun uploadSignature() {
        val imgFile = Tools.bitmapToFile(signature, signatureFileName)
        signatureFilePath = imgFile?.absolutePath
        if (signatureUploadUrl != null && imgFile != null) {
            binding.viewModel?.uploadSignature(signatureUploadUrl!!, imgFile)
        } else {
            this.showToast(getString(R.string.err_signature_image))
        }
    }

    private fun openSignatureDialog() {
        val signatureDialog = SignatureDialog.newInstance(object : SignatureDialog.OnSignatureCompleted {
            override fun onSignature(bmp: Bitmap?) {
                binding.updateJob.resetSlider()
                if (bmp == null) return
                signature = bmp
                jobToComplete?.signature = signatureFileName
                jobToComplete?.CheckListJob = jobDetail?.ChecklistJob
                jobToComplete?.employee = jobDetail?.employee
                jobToComplete?.location = "${userPreferences.latitude},${userPreferences.longitude}"
                binding.viewModel?.getUrlUploadSignature(jobId, signatureFileName)
            }
        })
        signatureDialog.showModal(supportFragmentManager)
    }

    private fun confirmationDeleteNote(position: Int) {
        this.showConfirmationDialog(R.string.confirmation_delete_item,
                object : OnConfirmationListener {
                    override fun onConfirm() {
                        noteDelete.id = noteAdapter.items[position].id
                        notePosToDelete = position
                        binding.viewModel?.doDeleteNote(noteDelete)
                    }
                }
        )
    }

    private fun openTakePictureDialog() {
        val sheet = TakeOrSelectPictureSheet.newInstance(
                object : TakeOrSelectPictureSheet.OnSelectListener {
                    override fun onSelect(requestCode: Int) {
                        takeOrSelectPicture(requestCode)
                    }
                }
        )
        sheet.show(supportFragmentManager)
    }

    /**
     * Handle response from database
     */
    private fun responseGetJobDetail(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.isPayment = false
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                response.data?.let { json ->
                    val result = Tools.getResponseStringByKey(data = json, key = "job")
                    jobDetail = DataUtil.toModel<JobDetail>(result)
                    jobDetail?.let {
                        noteAdapter.setJobStatus(it.jobStatus)
                        binding.viewModel?.setJob(it)
                        setJobToCompleteAndAdditionalService()
                        it.jobNotes?.let { notes -> noteAdapter. setItems(notes) }
                        it.ChecklistJob?.let { tasks -> checklistJobAdapter.setItems(tasks) }
                    }
                    this.checkJobActive()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                response.error?.let {
                    binding.viewModel?.setErrorResponse(it)
                    if (it.code == ErrorCode.RUNTIME_ERROR_CODE) finish()
                    if (it.code == ErrorCode.JOB_NOT_FOUND_ERROR_CODE) finish()
                }
            }
        }
    }

    private fun responseItemTemplates(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
            }
            is Resource.Success -> {
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key = "serviceItemTemplates")
                    val list = DataUtil.toList<ServiceItemTemplate>(result)
                    itemTemplates.addAll(list)
                    shareViewModel.setItemTemplates(list)
                }
            }
            is Resource.DataError -> {
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseAddNote(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                response.data?.let {
                    newNote = DataUtil.toModel(data = it)
                    binding.viewModel?.let { vm ->
                        vm.setNoteId(newNote?.id ?: 0)
                        val imageUrl = newNote?.imageUrl.toString()
                        if (imageUrl.isNotBlank()) {
                            vm.uploadNoteImage(imageUrl, currentPhotoPath)
                        } else {
                            this.hideLoading()
                            this.clearAddNoteField()
                        }
                    }
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseUploadNoteImg(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                binding.viewModel?.setNewNoteImage(null)
                this.hideLoading()
                this.clearAddNoteField()
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseUpdateJobStatus(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.isPayment = true
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                binding.updateJob.resetSlider()
                if (jobToComplete?.jobStatus == General.STATUS__COMPLETED) {
                    jobDetail?.collectedAmount = 0.0
                    jobDetail?.signatureUrl = signatureFilePath
                    PaymentSuccessActivity.open(this, jobDetail)
                    this.finish()
                } else {
                    binding.viewModel?.fetchJob(jobId)
                    this.setResult(Activity.RESULT_OK, intent)
                    this.checkJobActive()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseDeleteNote(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                noteAdapter.deleteItem(notePosToDelete)
                binding.listNotes.visible(noteAdapter.itemCount > 0)
                binding.textNoteMessage.visible(noteAdapter.itemCount == 0)
                notePosToDelete = -1
                noteDelete.id = -1
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseGetUrlSignature(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                response.data?.let {
                    signatureUploadUrl = Tools.getResponseStringByKey(data = it, key = "signatureUrl")
                    uploadSignature()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun responseUploadSignature(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                jobToComplete?.jobStatus = General.STATUS__COMPLETED
                jobToComplete?.signature = signatureFileName
                binding.viewModel?.doCompleteJob(jobId, jobToComplete)
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    fun reloadActivity() {
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    companion object {
        const val EXTRA_JOB_ID = "key.EXTRA_JOB_ID"

        fun open(context: Context, jobId: Int) {
            Intent(context, JobDetailActivity::class.java).also {
                it.putExtra(EXTRA_JOB_ID, jobId)
                context.startActivity(it)
            }
        }
        fun getIntent(context: Context, jobId: Int): Intent {
            return Intent(context, JobDetailActivity::class.java).apply {
                putExtra(EXTRA_JOB_ID, jobId)
            }
        }
    }

    private fun downloadImage(imageUrl : String?){
        if (!hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
        } else {
            imageUrl?.let {
                try {
                    Glide.with(this)
                        .load(it)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                @Nullable transition: Transition<in Drawable?>?
                            ) {
                                val bitmap = (resource as BitmapDrawable).bitmap
                                Toast.makeText(this@JobDetailActivity, getString(R.string.saving_image), Toast.LENGTH_SHORT)
                                    .show()

                                saveMediaToStorage(bitmap)
                            }

                            override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                            override fun onLoadFailed(@Nullable errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                Toast.makeText(
                                    this@JobDetailActivity,
                                    getString(R.string.failed_download_image),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        try {
            val filename = "${System.currentTimeMillis()}.jpg"

            //Output stream
            var fos: OutputStream? = null

            //For devices running android >= Q
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //getting the contentResolver
                this.contentResolver?.also { resolver ->

                    //Content resolver will process the contentvalues
                    val contentValues = ContentValues().apply {

                        //putting file information in content values
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    //Inserting the contentValues to contentResolver and getting the Uri
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    //Opening an outputstream with the Uri that we got
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                //These for devices running on android < Q
                //So I don't think an explanation is needed here
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                //Finally writing the bitmap to the output stream that we opened
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                showToast(getString(R.string.image_saved))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}