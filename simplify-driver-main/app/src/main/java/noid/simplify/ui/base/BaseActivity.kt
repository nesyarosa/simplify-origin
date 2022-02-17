package noid.simplify.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import noid.simplify.R
import noid.simplify.ui.components.others.LoadingDialog

abstract class BaseActivity<B : ViewBinding>(val viewBinder: (LayoutInflater) -> B) : AppCompatActivity() {

    protected lateinit var binding: B

    //set variable for views
    private var loadingDialog: LoadingDialog? = null
    private var menuRes: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = viewBinder(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        binding.onCreate(savedInstanceState)
        prepareToolbar(buildToolbar())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menuRes != -1) menuInflater.inflate(menuRes, menu)
        return true
    }

    private fun prepareToolbar(builder: ToolbarBuilder) {
        builder.toolbar?.title = builder.title
        setSupportActionBar(builder.toolbar)
        this.menuRes = builder.menuId
        if (builder.isCanGoBack) {
            builder.toolbar?.setNavigationIcon(R.drawable.ic_back)
            builder.toolbar?.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    protected fun showLoading() {
        loadingDialog?.show()
    }

    protected fun hideLoading() {
        loadingDialog?.dismiss()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    protected abstract fun buildToolbar(): ToolbarBuilder
    protected abstract fun B.onCreate(savedInstanceState: Bundle?)
}