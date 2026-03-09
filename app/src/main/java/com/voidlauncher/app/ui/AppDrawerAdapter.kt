package com.voidlauncher.app.ui

import android.content.Context
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voidlauncher.app.R
import com.voidlauncher.app.data.AppModel
import com.voidlauncher.app.data.Constants
import com.voidlauncher.app.databinding.ItemAppDrawerBinding
import com.voidlauncher.app.helper.hideKeyboard
import com.voidlauncher.app.helper.isSystemApp
import com.voidlauncher.app.helper.showKeyboard
import java.text.Normalizer

/** Sealed item for the drawer list. */
sealed class DrawerItem {
    data class AppItem(val appModel: AppModel) : DrawerItem()
}

class AppDrawerAdapter(
    private var flag: Int,
    private val appLabelGravity: Int,
    private val appClickListener: (AppModel) -> Unit,
    private val appInfoListener: (AppModel) -> Unit,
    private val appDeleteListener: (AppModel) -> Unit,
    private val appHideListener: (AppModel, Int) -> Unit,
    private val appRenameListener: (AppModel, String) -> Unit,
    private val dragStartListener: (RecyclerView.ViewHolder) -> Unit,
) : ListAdapter<DrawerItem, RecyclerView.ViewHolder>(DIFF_CALLBACK), Filterable {

    companion object {
        const val VIEW_TYPE_APP = 0

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DrawerItem>() {
            override fun areItemsTheSame(oldItem: DrawerItem, newItem: DrawerItem): Boolean =
                when {
                    oldItem is DrawerItem.AppItem && newItem is DrawerItem.AppItem -> {
                        val o = oldItem.appModel; val n = newItem.appModel
                        when {
                            o is AppModel.App && n is AppModel.App ->
                                o.appPackage == n.appPackage && o.user == n.user
                            o is AppModel.PinnedShortcut && n is AppModel.PinnedShortcut ->
                                o.shortcutId == n.shortcutId && o.user == n.user
                            else -> false
                        }
                    }
                    else -> false
                }

            override fun areContentsTheSame(oldItem: DrawerItem, newItem: DrawerItem) =
                oldItem == newItem
        }
    }

    private var isBangSearch = false
    private val appFilter = createAppFilter()
    private val myUserHandle = android.os.Process.myUserHandle()
    
    // Remembers the last query to re-filter gracefully when private space state changes
    private var currentQuery: CharSequence = ""

    var appsList: MutableList<AppModel> = mutableListOf()
    var appFilteredList: MutableList<AppModel> = mutableListOf()

    override fun getItemViewType(position: Int): Int = VIEW_TYPE_APP

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        AppViewHolder(
            ItemAppDrawerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DrawerItem.AppItem -> {
                try {
                    (holder as AppViewHolder).bind(
                        flag, appLabelGravity, myUserHandle, item.appModel,
                        appClickListener, appDeleteListener, appInfoListener,
                        appHideListener, appRenameListener, dragStartListener
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getFilter(): Filter = appFilter

    private fun createAppFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSearch: CharSequence?): FilterResults {
                currentQuery = charSearch ?: ""
                isBangSearch = charSearch?.startsWith("!") ?: false

                val filteredMain: MutableList<AppModel> = if (charSearch.isNullOrBlank()) appsList.toMutableList()
                else appsList.filter { app ->
                    appLabelMatches(app.appLabel, charSearch)
                } as MutableList<AppModel>

                return FilterResults().apply {
                    values = filteredMain
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val filteredMain = it as MutableList<AppModel>
                    appFilteredList = filteredMain

                    // Build DrawerItem list
                    val drawerItems = mutableListOf<DrawerItem>()
                    
                    // Add main apps
                    filteredMain.forEach { app -> drawerItems.add(DrawerItem.AppItem(app)) }

                    submitList(drawerItems)
                }
            }
        }
    }

    private fun appLabelMatches(appLabel: String, charSearch: CharSequence): Boolean {
        return (appLabel.contains(charSearch.trim(), true) ||
                Normalizer.normalize(appLabel, Normalizer.Form.NFD)
                    .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                    .replace(Regex("[-_+,. ]"), "")
                    .contains(charSearch, true))
    }

    fun setAppList(appsList: MutableList<AppModel>) {
        this.appsList = appsList
        appFilter.filter(currentQuery)
    }

    fun launchFirstInList() {
        val firstApp = getCurrentList()
            .firstOrNull { it is DrawerItem.AppItem && (it as DrawerItem.AppItem).appModel.appPackage.isNotEmpty() }
            as? DrawerItem.AppItem
        firstApp?.let { appClickListener(it.appModel) }
    }

    // ─── ViewHolder: real app item ────────────────────────────────────────────
    class AppViewHolder(private val binding: ItemAppDrawerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            flag: Int,
            appLabelGravity: Int,
            myUserHandle: UserHandle,
            appModel: AppModel,
            clickListener: (AppModel) -> Unit,
            appDeleteListener: (AppModel) -> Unit,
            appInfoListener: (AppModel) -> Unit,
            appHideListener: (AppModel, Int) -> Unit,
            appRenameListener: (AppModel, String) -> Unit,
            dragStartListener: (RecyclerView.ViewHolder) -> Unit,
        ) = with(binding) {
            appHideLayout.visibility = View.GONE
            renameLayout.visibility = View.GONE
            appTitle.visibility = View.VISIBLE

            appTitle.text = buildString {
                append(appModel.appLabel)
                if (appModel.isNew) append(" ✦")
            }
            appTitle.gravity = appLabelGravity
            otherProfileIndicator.isVisible = appModel.user != myUserHandle

            root.setOnLongClickListener {
                if (appModel.appPackage.isEmpty()) return@setOnLongClickListener true
                root.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                
                // start drag
                dragStartListener(this@AppViewHolder)
                
                // show popup menu
                val popup = androidx.appcompat.widget.PopupMenu(root.context, appTitle)
                popup.menu.add(0, 1, 0, "Rename")
                val hideToggleStr = if (flag == Constants.FLAG_HIDDEN_APPS) "Show" else "Hide"
                popup.menu.add(0, 2, 0, hideToggleStr)
                popup.menu.add(0, 3, 0, "Remove")
                
                popup.menu.getItem(0).isEnabled = flag != Constants.FLAG_HIDDEN_APPS
                val isSystem = root.context.isSystemApp(appModel.appPackage)
                val isPinned = appModel is AppModel.PinnedShortcut
                if (isPinned || isSystem) {
                    popup.menu.getItem(2).isEnabled = false
                }
                if (isPinned) {
                    popup.menu.getItem(1).isEnabled = false
                }
                
                popup.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        1 -> {
                            etAppRename.hint = getAppName(etAppRename.context, appModel.appPackage)
                            etAppRename.setText(appModel.appLabel)
                            etAppRename.setSelectAllOnFocus(true)
                            renameLayout.visibility = View.VISIBLE
                            appTitle.visibility = View.INVISIBLE
                            appHideLayout.visibility = View.GONE
                            etAppRename.showKeyboard()
                            etAppRename.imeOptions = EditorInfo.IME_ACTION_DONE
                            true
                        }
                        2 -> {
                            appHideListener(appModel, bindingAdapterPosition)
                            true
                        }
                        3 -> {
                            appDeleteListener(appModel)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }

            root.setOnClickListener {
                clickListener(appModel)
            }
            
            // Ensure no conflicting long click listener on title
            appTitle.isClickable = false
            appTitle.isLongClickable = false

            appRename.setOnClickListener {
                if (appModel.appPackage.isNotEmpty()) {
                    etAppRename.hint = getAppName(etAppRename.context, appModel.appPackage)
                    etAppRename.setText(appModel.appLabel)
                    etAppRename.setSelectAllOnFocus(true)
                    renameLayout.visibility = View.VISIBLE
                    appHideLayout.visibility = View.GONE
                    etAppRename.showKeyboard()
                    etAppRename.imeOptions = EditorInfo.IME_ACTION_DONE
                }
            }
            etAppRename.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    appTitle.visibility = if (hasFocus) View.INVISIBLE else View.VISIBLE
                }
            etAppRename.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    etAppRename.hint = getAppName(etAppRename.context, appModel.appPackage)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    etAppRename.hint = ""
                }
            })
            etAppRename.setOnEditorActionListener { _, actionCode, _ ->
                if (actionCode == EditorInfo.IME_ACTION_DONE) {
                    val renameLabel = etAppRename.text.toString().trim()
                    if (renameLabel.isNotBlank() && appModel.appPackage.isNotBlank()) {
                        appRenameListener(appModel, renameLabel)
                        renameLayout.visibility = View.GONE
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
            tvSaveRename.setOnClickListener {
                etAppRename.hideKeyboard()
                val renameLabel = etAppRename.text.toString().trim()
                if (renameLabel.isNotBlank() && appModel.appPackage.isNotBlank()) {
                    appRenameListener(appModel, renameLabel)
                    renameLayout.visibility = View.GONE
                } else {
                    val pm = etAppRename.context.packageManager
                    appRenameListener(
                        appModel,
                        pm.getApplicationLabel(pm.getApplicationInfo(appModel.appPackage, 0)).toString()
                    )
                    renameLayout.visibility = View.GONE
                }
            }
            appInfo.setOnClickListener { appInfoListener(appModel) }
            appDelete.setOnClickListener { appDeleteListener(appModel) }
            appMenuClose.setOnClickListener {
                appHideLayout.visibility = View.GONE
                appTitle.visibility = View.VISIBLE
            }
            appRenameClose.setOnClickListener {
                renameLayout.visibility = View.GONE
                appTitle.visibility = View.VISIBLE
            }
            appHide.setOnClickListener { appHideListener(appModel, bindingAdapterPosition) }
        }

        private fun getAppName(context: Context, appPackage: String): String {
            val pm = context.packageManager
            return pm.getApplicationLabel(pm.getApplicationInfo(appPackage, 0)).toString()
        }
    }
}
