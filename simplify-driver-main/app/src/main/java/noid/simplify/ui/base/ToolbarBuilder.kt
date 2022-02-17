package noid.simplify.ui.base

import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar

class ToolbarBuilder(
        val toolbar: Toolbar?,
        val title: String?,
        @MenuRes val menuId: Int,
        val isCanGoBack: Boolean)
{

    class Builder {
        private var toolbar: Toolbar? = null
        private var title: String? = null
        private var menuId = -1
        private var isCan = false

        fun withToolbar(toolbar: Toolbar?) = apply { this.toolbar = toolbar }
        fun withTitle(title: String?) = apply { this.title = title }
        fun withMenu(@MenuRes menuId: Int) = apply { this.menuId = menuId }
        fun withActionGoBack(isCan: Boolean) = apply { this.isCan = isCan }
        fun build() = ToolbarBuilder(toolbar, title, menuId, isCan)
    }


}