package noid.simplify.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import noid.simplify.R
import noid.simplify.ui.components.others.LoadingDialog

abstract class BaseDialog<B : ViewBinding>(val viewBinder: (LayoutInflater) -> B) : DialogFragment() {

    private var _binding: B? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return viewBinder(inflater).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onViewCreated(savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        prepareToolbar(buildToolbar())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected fun showLoading() {
        loadingDialog?.show()
    }

    protected fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun prepareToolbar(builder: ToolbarBuilder) {
        builder.toolbar?.title = builder.title
        if (builder.isCanGoBack) {
            builder.toolbar?.setNavigationIcon(R.drawable.ic_back)
            builder.toolbar?.setNavigationOnClickListener {
                dismiss()
            }
        }
    }

    protected val binding get() = _binding!!

    protected abstract fun buildToolbar(): ToolbarBuilder

    protected abstract fun B.onViewCreated(savedInstanceState: Bundle?)
}