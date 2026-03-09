package com.voidlauncher.app.ui

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.voidlauncher.app.R
import com.voidlauncher.app.data.NotificationGroup
import com.voidlauncher.app.databinding.FragmentNotificationsBinding
import com.voidlauncher.app.databinding.RowNotificationGroupBinding
import com.voidlauncher.app.helper.NotificationService
import com.voidlauncher.app.listener.OnSwipeTouchListener
import com.google.android.material.transition.MaterialSharedAxis

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gentle, consistent Material motion keeps navigation feeling natural.
        applyMaterialScreenTransitions(MaterialSharedAxis.X)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NotificationAdapter()
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        NotificationService.notificationsLiveData.observe(viewLifecycleOwner) { groups ->
            val hasNotifications = groups.isNotEmpty()
            binding.rvNotifications.visibility = if (hasNotifications) View.VISIBLE else View.GONE
            binding.tvNoNotifications.visibility = if (hasNotifications) View.GONE else View.VISIBLE
            binding.tvClearAll.visibility = if (hasNotifications) View.VISIBLE else View.GONE
            adapter.submitList(groups)
        }

        binding.tvClearAll.setOnClickListener {
            // Clear all notifications
            NotificationService.instance?.cancelAllNotifications()
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Notifications cleared", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }

        binding.tvNoNotifications.setOnClickListener {
            if (!hasNotificationAccess()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        // Reverse swipe: swipe left (finger right-to-left) → go back to home
        view.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeLeft() {
                findNavController().popBackStack()
            }
        })
        
        setupSwipeToDismiss(adapter)
    }

    private fun setupSwipeToDismiss(adapter: NotificationAdapter) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val group = adapter.getItemAt(position) ?: return

                for (sbn in group.notifications) {
                    NotificationService.instance?.cancelNotification(sbn.key)
                }
                
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Notification dismissed", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvNotifications)
    }

    override fun onResume() {
        super.onResume()
        if (!hasNotificationAccess()) {
            binding.rvNotifications.visibility = View.GONE
            binding.tvClearAll.visibility = View.GONE
            binding.tvNoNotifications.visibility = View.VISIBLE
            binding.tvNoNotifications.text = "Notification access required. Tap to enable."
        } else {
            binding.tvNoNotifications.text = getString(R.string.no_notifications)
        }
    }

    private fun hasNotificationAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(requireContext())
            .contains(requireContext().packageName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class NotificationAdapter : ListAdapter<NotificationGroup, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

        private val pm: PackageManager = requireContext().packageManager
        private val expandedGroups = mutableSetOf<String>()

        fun getItemAt(position: Int): NotificationGroup? {
            return if (position in 0 until itemCount) getItem(position) else null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RowNotificationGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(private val binding: RowNotificationGroupBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(group: NotificationGroup) {
                // App info
                try {
                    val appInfo = pm.getApplicationInfo(group.packageName, 0)
                    binding.tvAppName.text = pm.getApplicationLabel(appInfo)
                    binding.ivAppIcon.setImageDrawable(pm.getApplicationIcon(appInfo))
                } catch (e: Exception) {
                    binding.tvAppName.text = group.packageName
                }

                // Latest notification preview
                val latest = group.notifications.firstOrNull()?.notification
                binding.tvTitle.text = latest?.extras?.getString(Notification.EXTRA_TITLE) ?: ""
                binding.tvText.text = latest?.extras?.getString(Notification.EXTRA_TEXT) ?: ""

                val timeString = DateUtils.getRelativeTimeSpanString(
                    group.latestTimestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                )
                binding.tvTime.text = timeString

                // Count badge and expand arrow
                val isExpanded = expandedGroups.contains(group.groupKey)
                
                binding.tvCount.visibility = View.VISIBLE
                binding.tvCount.text = "${group.childCount}"
                
                if (group.childCount > 1) {
                    binding.ivExpand.visibility = View.VISIBLE
                    binding.ivExpand.rotation = if (isExpanded) 180f else 0f
                } else {
                    binding.ivExpand.visibility = View.GONE
                }

                // Expand/collapse children
                if (isExpanded && group.childCount > 1) {
                    binding.llLatestPreview.visibility = View.GONE
                    binding.llChildren.visibility = View.VISIBLE
                    populateChildren(binding.llChildren, group)
                } else {
                    binding.llLatestPreview.visibility = View.VISIBLE
                    binding.llChildren.visibility = View.GONE
                    binding.llChildren.removeAllViews()
                }

                // Click handler: toggle expand or open notification
                binding.llGroupHeader.setOnClickListener {
                    if (group.childCount > 1) {
                        if (isExpanded) {
                            expandedGroups.remove(group.groupKey)
                        } else {
                            expandedGroups.add(group.groupKey)
                        }
                        notifyItemChanged(bindingAdapterPosition)
                    } else {
                        try {
                            latest?.contentIntent?.send()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                // Click handler: open notification from preview
                binding.llLatestPreview.setOnClickListener {
                    try {
                        latest?.contentIntent?.send()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun populateChildren(container: LinearLayout, group: NotificationGroup) {
                container.removeAllViews()
                val inflater = LayoutInflater.from(container.context)
                for (sbn in group.notifications) {
                    val childView = inflater.inflate(R.layout.row_notification_child, container, false)
                    val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
                    val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: ""
                    val time = DateUtils.getRelativeTimeSpanString(
                        sbn.postTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                    )

                    childView.findViewById<TextView>(R.id.tvChildTitle).text = title
                    childView.findViewById<TextView>(R.id.tvChildText).text = text
                    childView.findViewById<TextView>(R.id.tvChildTime).text = time

                    childView.setOnClickListener {
                        try {
                            sbn.notification.contentIntent?.send()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    container.addView(childView)
                }
            }
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationGroup>() {
        override fun areItemsTheSame(oldItem: NotificationGroup, newItem: NotificationGroup): Boolean {
            return oldItem.groupKey == newItem.groupKey
        }

        override fun areContentsTheSame(oldItem: NotificationGroup, newItem: NotificationGroup): Boolean {
            return oldItem == newItem
        }
    }
}
