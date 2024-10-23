package com.movtery.zalithlauncher.ui.subassembly.account

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.event.single.SelectAuthMethodEvent
import com.movtery.zalithlauncher.feature.accounts.AccountsManager
import com.movtery.zalithlauncher.ui.dialog.AccountsDialog
import com.movtery.zalithlauncher.utils.skin.SkinLoader
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.value.MinecraftAccount
import org.greenrobot.eventbus.EventBus

class AccountViewWrapper(val mainView: View) {
    private val mContext: Context = mainView.context
    private val mUserIconView: ImageView = mainView.findViewById(R.id.user_icon)
    private val mUserNameView: TextView = mainView.findViewById(R.id.user_name)

    init {
        mainView.setOnClickListener {
            currentAccount ?: run {
                EventBus.getDefault().post(SelectAuthMethodEvent())
                return@setOnClickListener
            }
            AccountsDialog(mContext) { this.refreshAccountInfo() }.show()
        }
    }

    fun refreshAccountInfo() {
        val account = currentAccount
        account ?: run {
            mUserIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add))
            mUserNameView.setText(R.string.account_add)
            return
        }
        mUserIconView.setImageDrawable(SkinLoader.getAvatarDrawable(mainView.context, account, Tools.dpToPx(52f).toInt()))
        mUserNameView.text = account.username
    }

    private val currentAccount: MinecraftAccount?
        get() = AccountsManager.getInstance().currentAccount
}