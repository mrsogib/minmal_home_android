package com.sogib.minimallauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sogib.minimallauncher.databinding.ActivityAppDrawerBinding

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDrawerBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var repo: AppRepository
    private lateinit var adapter: AppDrawerAdapter

    /** Set when this screen was opened to let the user pick apps for a swipe gesture. */
    private var pickingForSwipe: String? = null
    private var showHiddenOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsHelper(this)
        repo = AppRepository(this)
        pickingForSwipe = intent.getStringExtra("pick_for_swipe")
        showHiddenOnly = intent.getBooleanExtra("show_hidden", false)

        binding.drawerList.layoutManager = LinearLayoutManager(this)

        applyTheme()
        setupSortSpinner()
        loadList()

        intent.getStringExtra("jump_to_letter")?.let { letter ->
            binding.drawerList.post {
                val pos = adapter.positionForLetter(letter[0])
                binding.drawerList.scrollToPosition(pos)
            }
        }
    }

    private var currentDivider: RecyclerView.ItemDecoration? = null

    private fun applyTheme() {
        binding.root.setBackgroundColor(prefs.getBgColor())
        val accent = if (prefs.isDynamicAccentEnabled()) prefs.getAccentColor() else null
        binding.drawerAlphabetIndex.textColor = accent ?: prefs.getTextColor()
        binding.drawerAlphabetIndex.textSizeSp = prefs.getAlphabetTextSizeSp()
    }

    private fun setupSortSpinner() {
        val modes = PrefsHelper.SortMode.values()
        val labels = listOf("Alphabetical", "Frequently used", "Manual order")
        binding.sortModeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.sortModeSpinner.setSelection(modes.indexOf(prefs.getSortMode()))
        binding.sortModeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.setSortMode(modes[position])
                loadList()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun loadList() {
        val all = repo.getAllApps(includeHidden = showHiddenOnly)
        val filtered = if (showHiddenOnly) all.filter { it.isHidden } else all
        val sorted = repo.sortedForDrawer(filtered)

        val paddingPx = (prefs.getRowPaddingDp() * resources.displayMetrics.density).toInt()
        adapter = AppDrawerAdapter(
            sorted,
            FontOptions.get(prefs.getFontIndex()),
            prefs.getRowTextSizeSp(),
            paddingPx,
            prefs.getTextColor(),
            prefs.isShowIcons(),
            onClick = { app ->
                if (pickingForSwipe != null) {
                    val pool = prefs.getSwipePool(pickingForSwipe!!).toMutableSet()
                    pool.add(app.packageName)
                    prefs.setSwipePool(pickingForSwipe!!, pool)
                    finish()
                } else {
                    repo.launchApp(app)
                }
            },
            onLongClick = { showAppOptions(it) }
        )
        binding.drawerList.adapter = adapter

        // Divider only between letter-group boundaries, only meaningful when sorted
        // alphabetically — other modes don't have a consistent letter grouping.
        currentDivider?.let { binding.drawerList.removeItemDecoration(it) }
        if (prefs.getSortMode() == PrefsHelper.SortMode.ALPHABETICAL) {
            val accent = if (prefs.isDynamicAccentEnabled()) prefs.getAccentColor() else null
            val divider = GroupDividerDecoration(adapter, accent ?: prefs.getTextColor())
            binding.drawerList.addItemDecoration(divider)
            currentDivider = divider
        } else {
            currentDivider = null
        }

        binding.drawerAlphabetIndex.letters = repo.lettersPresent(sorted)
        binding.drawerAlphabetIndex.onLetterSelected = { letter ->
            binding.drawerList.scrollToPosition(adapter.positionForLetter(letter))
        }
    }

    private fun showAppOptions(app: AppInfo) {
        val hideLabel = if (app.isHidden) "Unhide" else "Hide"
        val options = arrayOf("Rename", hideLabel, "Add to favorites", "Add to a group", "App info", "Uninstall")
        AlertDialog.Builder(this)
            .setTitle(app.displayLabel)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameApp(app)
                    1 -> toggleHidden(app)
                    2 -> addToFavorites(app)
                    3 -> addToGroup(app)
                    4 -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
                    5 -> startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}")))
                }
            }.show()
    }

    private fun renameApp(app: AppInfo) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(app.displayLabel)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                prefs.setCustomLabel(app.packageName, input.text.toString())
                loadList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleHidden(app: AppInfo) {
        val hidden = prefs.getHiddenApps().toMutableSet()
        if (app.isHidden) hidden.remove(app.packageName) else hidden.add(app.packageName)
        prefs.setHiddenApps(hidden)
        loadList()
    }

    private fun addToFavorites(app: AppInfo) {
        val favorites = prefs.getFavoriteOrder().toMutableList()
        if (!favorites.contains(app.packageName)) favorites.add(app.packageName)
        prefs.setFavoriteOrder(favorites)
    }

    private fun addToGroup(app: AppInfo) {
        val input = EditText(this).apply { hint = "Group name" }
        AlertDialog.Builder(this)
            .setTitle("Add \"${app.displayLabel}\" to group")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().ifBlank { return@setPositiveButton }
                val groups = prefs.getGroups().toMutableMap()
                val members = (groups[name] ?: emptyList()).toMutableList()
                if (!members.contains(app.packageName)) members.add(app.packageName)
                groups[name] = members
                prefs.setGroups(groups)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
