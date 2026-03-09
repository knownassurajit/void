package com.voidlauncher.app.ui

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
//import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.voidlauncher.app.MainViewModel
import com.voidlauncher.app.R
import com.voidlauncher.app.data.AppModel
import com.voidlauncher.app.data.Constants
import com.voidlauncher.app.data.Prefs
import com.voidlauncher.app.databinding.FragmentAppDrawerBinding
import com.voidlauncher.app.helper.deletePinnedShortcut
import com.voidlauncher.app.helper.hideKeyboard
import com.voidlauncher.app.helper.isEinkDisplay
import com.voidlauncher.app.helper.isSystemApp
import com.voidlauncher.app.helper.openAppInfo
import com.voidlauncher.app.helper.openSearch
import com.voidlauncher.app.helper.openUrl
import com.voidlauncher.app.helper.showKeyboard
import com.voidlauncher.app.helper.showToast
import com.voidlauncher.app.helper.uninstall
import com.google.android.material.transition.MaterialSharedAxis

class AppDrawerFragment : Fragment() {

    private lateinit var prefs: Prefs
    private lateinit var adapter: AppDrawerAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var itemTouchHelper: androidx.recyclerview.widget.ItemTouchHelper? = null

    private var flag = Constants.FLAG_LAUNCH_APP
    private var canRename = false

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentAppDrawerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gentle, consistent Material motion keeps navigation feeling natural.
        applyMaterialScreenTransitions(MaterialSharedAxis.Y)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAppDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())
        arguments?.let {
            flag = it.getInt(Constants.Key.FLAG, Constants.FLAG_LAUNCH_APP)
            canRename = it.getBoolean(Constants.Key.RENAME, false)
        }

        initViews()
        initSearch()
        initAdapter()
        initObservers()
        initClickListeners()
    }

    private fun initViews() {
        if (flag == Constants.FLAG_HIDDEN_APPS)
            binding.search.queryHint = getString(R.string.hidden_apps)
        else if (flag in Constants.FLAG_SET_HOME_APP_1..Constants.FLAG_SET_CALENDAR_APP)
            binding.search.queryHint = "Please select an app"
        // Fix SearchView internal padding — the AutoComplete inside it has its own padding
        // that pushes the text/hint right; set to 0 so it aligns with the card start.
        try {
            val searchAutoComplete = binding.search
                .findViewById<android.widget.AutoCompleteTextView>(
                    androidx.appcompat.R.id.search_src_text
                )
            searchAutoComplete?.apply {
                val density = resources.displayMetrics.density
                val startPadding = (28 * density).toInt() // 20dp icon + 8dp gap
                val verticalPadding = (8 * density).toInt()

                setPadding(startPadding, verticalPadding, paddingRight, verticalPadding)
                textSize = (prefs.appDrawerTextSizeScale * 20).toFloat()
                gravity = prefs.appLabelAlignment or android.view.Gravity.CENTER_VERTICAL
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ── Search icon slide animation on focus ──
        initSearchIconAnimation()
    }

    /**
     * Animates the search icon from left → right when the search bar gains focus,
     * and back from right → left when it loses focus (if the text is empty).
     */
    private fun initSearchIconAnimation() {
        try {
            val searchAutoComplete = binding.search
                .findViewById<android.widget.AutoCompleteTextView>(
                    androidx.appcompat.R.id.search_src_text
                ) ?: return

            val density = resources.displayMetrics.density
            val initialStartPadding = (28 * density).toInt()
            val verticalPadding = (8 * density).toInt()
            
            searchAutoComplete.setOnFocusChangeListener { _, hasFocus ->
                // If losing focus but text is not empty, keep it translated
                if (!hasFocus && searchAutoComplete.text.isNotEmpty()) {
                    return@setOnFocusChangeListener
                }

                val searchIcon = binding.searchIcon
                val parent = searchIcon.parent as? ViewGroup ?: return@setOnFocusChangeListener
                // Wait for layout to know real width
                parent.post {
                    val maxTranslation = (parent.width - searchIcon.width - parent.paddingStart - parent.paddingEnd).toFloat()
                    val targetX = if (hasFocus) maxTranslation else 0f
                    
                    ObjectAnimator.ofFloat(searchIcon, "translationX", targetX).apply {
                        duration = 300
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }

                    // Remove padding when focused so text can use the space
                    searchAutoComplete.setPadding(
                        if (hasFocus) 0 else initialStartPadding,
                        verticalPadding,
                        searchAutoComplete.paddingRight,
                        verticalPadding
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Search interception ────────────────────────────────────────────────────

    private fun initSearch() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query?.startsWith("!") == true)
                    requireContext().openUrl(Constants.URL_DUCK_SEARCH + query.replace(" ", "%20"))
                else if (adapter.itemCount == 0 || adapter.appFilteredList.isEmpty())
                    requireContext().openSearch(query?.trim())
                else
                    adapter.launchFirstInList()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                try {
                    adapter.filter.filter(newText)
                    binding.appDrawerTip.visibility = View.GONE
                    binding.appRename.visibility =
                        if (canRename && newText.isNotBlank()) View.VISIBLE else View.GONE
                        
                    // If text is cleared, and we don't have focus, revert animation
                    if (newText.isEmpty() && !binding.search.hasFocus()) {
                        val searchIcon = binding.searchIcon
                        ObjectAnimator.ofFloat(searchIcon, "translationX", 0f).apply {
                            duration = 300
                            interpolator = AccelerateDecelerateInterpolator()
                            start()
                        }
                        try {
                            val searchAutoComplete = binding.search.findViewById<android.widget.AutoCompleteTextView>(androidx.appcompat.R.id.search_src_text)
                            val initialPadding = (28 * resources.displayMetrics.density).toInt()
                            searchAutoComplete?.setPadding(
                                initialPadding,
                                searchAutoComplete.paddingTop,
                                searchAutoComplete.paddingRight,
                                searchAutoComplete.paddingBottom
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }
        })
    }

    private fun initAdapter() {
        adapter = AppDrawerAdapter(
            flag,
            prefs.appLabelAlignment,
            appClickListener = { appModel ->
                viewModel.selectedApp(appModel, flag)
                if (flag == Constants.FLAG_LAUNCH_APP || flag == Constants.FLAG_HIDDEN_APPS)
                    findNavController().popBackStack(R.id.mainFragment, false)
                else
                    findNavController().popBackStack()
            },
            appInfoListener = {
                openAppInfo(requireContext(), it.user, it.appPackage)
                findNavController().popBackStack(R.id.mainFragment, false)
            },
            appDeleteListener = { appModel ->
                when (appModel) {
                    is AppModel.PinnedShortcut ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                            requireContext().deletePinnedShortcut(
                                packageName = appModel.appPackage,
                                shortcutIdToDelete = appModel.shortcutId,
                                user = appModel.user,
                            )
                        }
                    is AppModel.App -> {
                        requireContext().apply {
                            if (isSystemApp(appModel.appPackage))
                                showToast(getString(R.string.system_app_cannot_delete))
                            else
                                uninstall(appModel.appPackage)
                        }
                    }
                }
                viewModel.getAppList()
            },
            appHideListener = { appModel: AppModel, position: Int ->
                if (appModel is AppModel.PinnedShortcut) {
                    requireContext().showToast("Hiding pinned shortcuts is not supported")
                } else {
                    adapter.appFilteredList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.appsList.remove(appModel)

                    val newSet = mutableSetOf<String>()
                    newSet.addAll(prefs.hiddenApps)
                    if (flag == Constants.FLAG_HIDDEN_APPS) {
                        newSet.remove(appModel.appPackage)
                        newSet.remove(appModel.appPackage + "|" + appModel.user.toString())
                    } else
                        newSet.add(appModel.appPackage + "|" + appModel.user.toString())

                    prefs.hiddenApps = newSet
                    if (newSet.isEmpty()) findNavController().popBackStack()
                    if (prefs.firstHide) {
                        binding.search.hideKeyboard()
                        prefs.firstHide = false
                        viewModel.showDialog.postValue(Constants.Dialog.HIDDEN)
                        findNavController().navigate(R.id.action_appListFragment_to_settingsFragment2)
                    }
                    viewModel.getAppList()
                    viewModel.getHiddenApps()
                }
            },
            appRenameListener = { appModel, renameLabel ->
                val identifier = when (appModel) {
                    is AppModel.PinnedShortcut -> appModel.shortcutId
                    is AppModel.App -> appModel.appPackage
                }
                prefs.setAppRenameLabel(identifier, renameLabel)
                viewModel.getAppList()
            },
            dragStartListener = { viewHolder ->
                itemTouchHelper?.startDrag(viewHolder)
            }
        )

        linearLayoutManager = object : LinearLayoutManager(requireContext()) {
            override fun scrollVerticallyBy(
                dx: Int, recycler: Recycler, state: RecyclerView.State,
            ): Int {
                val scrollRange = super.scrollVerticallyBy(dx, recycler, state)
                val overScroll = dx - scrollRange
                if (overScroll < -10 && binding.recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING)
                    checkMessageAndExit()
                return scrollRange
            }
        }

        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(getRecyclerViewOnScrollListener())
        binding.recyclerView.itemAnimator = null

        setupItemTouchHelper()
    }

    private fun setupItemTouchHelper() {
        val callback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
            androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN, 0
        ) {
            override fun isLongPressDragEnabled(): Boolean = false
            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false

                val list = adapter.appFilteredList
                val item = list.removeAt(fromPos)
                list.add(toPos, item)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                // Save the new order
                val newOrder = adapter.appFilteredList.map { 
                    it.appPackage + "|" + it.user.toString() 
                }
                
                // Merge with existing full custom order so we don't drop apps not currently filtered
                val existingOrder = prefs.customAppOrder.toMutableList()
                val orderedSet = mutableSetOf<String>()
                
                // First add the currently visible ordered items
                orderedSet.addAll(newOrder)
                // Then append any existing items that weren't in the current filter
                orderedSet.addAll(existingOrder.filter { it !in newOrder })
                
                prefs.customAppOrder = orderedSet.toList()
                
                // Also update the unfiltered list
                val newFullOrderIds = prefs.customAppOrder
                adapter.appsList.sortWith(Comparator { app1, app2 ->
                    val id1 = app1.appPackage + "|" + app1.user.toString()
                    val id2 = app2.appPackage + "|" + app2.user.toString()
                    val index1 = newFullOrderIds.indexOf(id1)
                    val index2 = newFullOrderIds.indexOf(id2)
                    
                    if (index1 != -1 && index2 != -1) {
                        index1.compareTo(index2)
                    } else if (index1 != -1) {
                        -1
                    } else if (index2 != -1) {
                        1
                    } else {
                        val collator = java.text.Collator.getInstance()
                        collator.compare(app1.appLabel, app2.appLabel)
                    }
                })
            }
        }
        itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)
    }

    private fun initObservers() {
        viewModel.firstOpen.observe(viewLifecycleOwner) {
            if (it && flag == Constants.FLAG_LAUNCH_APP) {
                binding.appDrawerTip.visibility = View.VISIBLE
                binding.appDrawerTip.isSelected = true
            }
        }
        if (flag == Constants.FLAG_HIDDEN_APPS) {
            viewModel.hiddenApps.observe(viewLifecycleOwner) {
                it?.let { adapter.setAppList(it.toMutableList()) }
            }
        } else {
            viewModel.appList.observe(viewLifecycleOwner) {
                it?.let { appModels ->
                    adapter.setAppList(appModels.toMutableList())
                }
            }
        }
    }

    private fun initClickListeners() {
        binding.appDrawerTip.setOnClickListener {
            binding.appDrawerTip.isSelected = false
            binding.appDrawerTip.isSelected = true
        }
        binding.appRename.setOnClickListener {
            val name = binding.search.query.toString().trim()
            if (name.isEmpty()) {
                requireContext().showToast(getString(R.string.type_a_new_app_name_first))
                binding.search.showKeyboard()
                return@setOnClickListener
            }
            when (flag) {
                Constants.FLAG_SET_HOME_APP_1 -> prefs.appName1 = name
                Constants.FLAG_SET_HOME_APP_2 -> prefs.appName2 = name
                Constants.FLAG_SET_HOME_APP_3 -> prefs.appName3 = name
                Constants.FLAG_SET_HOME_APP_4 -> prefs.appName4 = name
                Constants.FLAG_SET_HOME_APP_5 -> prefs.appName5 = name
                Constants.FLAG_SET_HOME_APP_6 -> prefs.appName6 = name
                Constants.FLAG_SET_HOME_APP_7 -> prefs.appName7 = name
                Constants.FLAG_SET_HOME_APP_8 -> prefs.appName8 = name
                Constants.FLAG_SET_HOME_APP_9 -> prefs.appName9 = name
                Constants.FLAG_SET_HOME_APP_10 -> prefs.appName10 = name
            }
            findNavController().popBackStack()
        }
    }

    private fun getRecyclerViewOnScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            var onTop = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        onTop = !recyclerView.canScrollVertically(-1)
                        if (onTop) binding.search.hideKeyboard()
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (!recyclerView.canScrollVertically(1))
                            binding.search.hideKeyboard()
                        else if (!recyclerView.canScrollVertically(-1))
                            if (!onTop && isRemoving.not())
                                binding.search.showKeyboard(prefs.autoShowKeyboard)
                    }
                }
            }
        }
    }

    private fun checkMessageAndExit() {
        findNavController().popBackStack()
        if (flag == Constants.FLAG_LAUNCH_APP) viewModel.checkForMessages.call()
    }

    override fun onStart() {
        super.onStart()
        view?.postDelayed({
            if (isResumed && _binding != null) {
                binding.search.showKeyboard(prefs.autoShowKeyboard)
            }
        }, 250)
    }

    override fun onStop() {
        binding.search.hideKeyboard()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
