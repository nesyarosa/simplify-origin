package noid.simplify.interfaces

interface OnLostConnection {
    fun onRetry(url: String?)
}