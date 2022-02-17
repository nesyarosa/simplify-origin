package noid.simplify.ui.components.forgot

import android.animation.Animator
import android.content.DialogInterface
import android.os.Bundle
import noid.simplify.R
import noid.simplify.databinding.DialogRegisterStateBinding
import noid.simplify.interfaces.OnDismissListener
import noid.simplify.ui.base.BaseDialog
import noid.simplify.ui.base.ToolbarBuilder

class ChangePasswordStateDialog (
    private val changePasswordSucceed: Boolean,
    private val onDismissListener: OnDismissListener
    ) : BaseDialog<DialogRegisterStateBinding>({ DialogRegisterStateBinding.inflate(it) }) {
        override fun buildToolbar(): ToolbarBuilder = ToolbarBuilder.Builder().build()

        override fun DialogRegisterStateBinding.onViewCreated(savedInstanceState: Bundle?) {
            binding.apply {

                if (changePasswordSucceed) {
                    animationView.setAnimation(R.raw.check)
                    tvTitle.text = getString(R.string.link_forgot_password_sent_to_email)
                } else {
                    animationView.setAnimation(R.raw.uncheck)
                    tvTitle.text = getString(R.string.email_not_registered)
                }

                animationView.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {}

                    override fun onAnimationEnd(p0: Animator?) {
                        dismiss()
                    }

                    override fun onAnimationCancel(p0: Animator?) {}

                    override fun onAnimationRepeat(p0: Animator?) {}
                })
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            onDismissListener.onDismiss()
            super.onDismiss(dialog)
        }

        companion object {
            fun newInstance(changePasswordSucceed: Boolean, onDismissListener: OnDismissListener) =
                    ChangePasswordStateDialog(changePasswordSucceed, onDismissListener).apply {
                        this.setStyle(
                                STYLE_NO_FRAME,
                                R.style.ThemeOverlay_AppCompat_Dialog_Alert
                        )
                    }
        }

    }