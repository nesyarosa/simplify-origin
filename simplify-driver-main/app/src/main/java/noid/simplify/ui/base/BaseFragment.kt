package noid.simplify.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding>(val viewBinder: (LayoutInflater) -> B) : Fragment() {

    private var _binding: B? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinder(inflater).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onViewCreated(savedInstanceState)
        prepareToolbar(buildToolbar())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun prepareToolbar(builder: ToolbarBuilder) {
        builder.toolbar?.title = builder.title
    }

    protected val binding get() = _binding!!

    protected abstract fun buildToolbar(): ToolbarBuilder

    protected abstract fun B.onViewCreated(savedInstanceState: Bundle?)
}