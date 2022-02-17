package noid.simplify.utils.view.signature

import android.annotation.SuppressLint
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener

object ViewTreeObserverCompat {
    /**
     * Remove a previously installed global layout callback.
     * @param observer the view observer
     * @param victim the victim
     */
    @SuppressLint("NewApi")
    fun removeOnGlobalLayoutListener(
        observer: ViewTreeObserver,
        victim: OnGlobalLayoutListener?
    ) {
        observer.removeOnGlobalLayoutListener(victim)
    }
}