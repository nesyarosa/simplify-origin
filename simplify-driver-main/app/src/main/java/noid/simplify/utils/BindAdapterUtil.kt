package noid.simplify.utils

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import noid.simplify.R
import noid.simplify.constants.ValidationFlags
import noid.simplify.data.dto.ServiceItem
import noid.simplify.ui.components.jobdetail.DescriptionServiceItemSheet
import noid.simplify.ui.components.jobdetail.JobDetailActivity
import noid.simplify.ui.components.jobdetail.JobServiceItemAdapter
import noid.simplify.ui.components.serviceitem.EditServiceItemDialog
import noid.simplify.utils.extensions.*

@BindingAdapter("validationRule")
fun setValidationRule(view: TextInputLayout, rule: String?) {
    view.editText?.doOnTextChanged { text, _, _, _ ->
        if (text.isNullOrBlank()) {
            view.error = view.resources.getString(R.string.field_required)
            return@doOnTextChanged
        }
        if (rule == ValidationFlags.PASSWORD_RULE && !text.isNullOrBlank() && !text.isValidPassword()) {
            view.error = view.resources.getString(R.string.password_required_valid_format)
            return@doOnTextChanged
        }
        if (rule == ValidationFlags.EMAIL_RULE && !text.isNullOrBlank() && !text.isValidEmail()) {
            view.error = view.resources.getString(R.string.email_not_valid)
            return@doOnTextChanged
        }
        if (rule == ValidationFlags.PHONE_NUMBER_RULE && !text.isNullOrBlank() && !text.isValidContactNumber()) {
            view.error = view.resources.getString(R.string.contact_number_not_valid)
            return@doOnTextChanged
        }
        view.error = null
    }
}

@BindingAdapter("serviceItems", "isCanEdit", "isHidePrice")
fun setServiceItems(
        view: RecyclerView,
        items: List<ServiceItem>? = emptyList(),
        isCanEdit: Boolean,
        isHidePrice: Boolean
) {
    if(items?.isNotEmpty() == true){
        Tools.log("BindAdapterUtil", "setServiceItems", "iscanedit $isCanEdit")

        val adapter by lazy { JobServiceItemAdapter() }
        RecyclerUtil.setRecyclerLinear(view.context, view, adapter)
        adapter.setHidePrice(isHidePrice)
        adapter.setEditable(isCanEdit)
        adapter.setItems(items)

        val activity = view.context as AppCompatActivity
        adapter.itemListener = {
            val sheet = DescriptionServiceItemSheet.newInstance(it)
            sheet.show(fm = activity.supportFragmentManager)
        }
        if (activity is JobDetailActivity) {
            adapter.editListener = { _, item ->
                val modalEdit = EditServiceItemDialog.newInstance(
                    item = item,
                    onDismissListener = object : EditServiceItemDialog.OnDismissInputAdditionalListener {
                        override fun onDismiss(item: ServiceItem) {
                            activity.reloadActivity()
                        }
                    }
                )
                modalEdit.showModal(activity.supportFragmentManager)
            }
        }
    }
}

@BindingAdapter("imageNotes")
fun setImageNotes(view: ImageView, imageUrl: Any?) {
    view.showImage(imageUrl)
}