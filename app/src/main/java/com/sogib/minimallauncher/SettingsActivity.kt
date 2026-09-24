package com.sogib.minimallauncher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sogib.minimallauncher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var repo: AppRepository

    private val folderPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            prefs.setWallpaperFolderUri(uri.toString())
            WallpaperHelper.scheduleRotation(this, prefs.getWallpaperIntervalHours())
            WallpaperHelper.setRandomWallpaperFromFolder(this, uri.toString())
            AccentColorHelper.extractAccentColor(this)?.let { prefs.setAccentColor(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsHelper(this)
        repo = AppRepository(this)

        setupThemeControls()
        setupSizeControls()
        setupGestureControls()
        setupClockStyleSpinner()
        setupFontSpinner()
        setupWallpaperButton()
        setupWallpaperIntervalSpinner()
        setupLockToggle()
        setupSwipePoolButtons()
        setupTapTargetButtons()
        setupGroupsButton()
        setupIconToggle()
        setupAccentControls()
        setupNotificationPreviewControls()
        setupPermissionsExplainer()
    }

    private fun setupIconToggle() {
        binding.showIconsSwitch.isChecked = prefs.isShowIcons()
        binding.showIconsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setShowIcons(isChecked)
        }
    }

    private fun setupAccentControls() {
        binding.dynamicAccentSwitch.isChecked = prefs.isDynamicAccentEnabled()
        binding.dynamicAccentSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDynamicAccentEnabled(isChecked)
        }
        binding.refreshAccentButton.setOnClickListener {
            val accent = AccentColorHelper.extractAccentColor(this)
            if (accent != null) {
                prefs.setAccentColor(accent)
                android.widget.Toast.makeText(this, "Accent color updated", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "Couldn't read a color from the current wallpaper", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNotificationPreviewControls() {
        binding.notificationPreviewSwitch.isChecked = prefs.isNotificationPreviewEnabled()
        binding.notificationPreviewSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setNotificationPreviewEnabled(isChecked)
            if (isChecked) {
                android.widget.Toast.makeText(
                    this,
                    "You'll also need to grant notification access below",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.openNotificationAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    // ---------------- Theme & colors ----------------

    private fun setupThemeControls() {
        binding.darkThemeSwitch.isChecked = prefs.isDarkTheme()
        binding.customBgColorInput.setText(prefs.getCustomBgColorRaw() ?: "")
        binding.customTextColorInput.setText(prefs.getCustomTextColorRaw() ?: "")

        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkTheme(isChecked)
        }

        binding.applyColorsButton.setOnClickListener {
            val bg = binding.customBgColorInput.text.toString().trim()
            val text = binding.customTextColorInput.text.toString().trim()
            try {
                if (bg.isNotEmpty()) android.graphics.Color.parseColor(bg)
                if (text.isNotEmpty()) android.graphics.Color.parseColor(text)
                prefs.setCustomBgColor(bg.ifEmpty { null })
                prefs.setCustomTextColor(text.ifEmpty { null })
            } catch (e: IllegalArgumentException) {
                android.widget.Toast.makeText(this, "Use hex like #101010", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------- Text sizes & spacing ----------------

    private fun setupSizeControls() {
        binding.clockScaleInput.setText(prefs.getClockSizeScale().toString())
        binding.rowTextSizeInput.setText(prefs.getRowTextSizeSp().toString())
        binding.rowPaddingInput.setText(prefs.getRowPaddingDp().toString())
        binding.alphabetTextSizeInput.setText(prefs.getAlphabetTextSizeSp().toString())

        binding.applySizesButton.setOnClickListener {
            binding.clockScaleInput.text.toString().toFloatOrNull()?.let { prefs.setClockSizeScale(it) }
            binding.rowTextSizeInput.text.toString().toFloatOrNull()?.let { prefs.setRowTextSizeSp(it) }
            binding.rowPaddingInput.text.toString().toFloatOrNull()?.let { prefs.setRowPaddingDp(it) }
            binding.alphabetTextSizeInput.text.toString().toFloatOrNull()?.let { prefs.setAlphabetTextSizeSp(it) }
            android.widget.Toast.makeText(this, "Sizes updated", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Gesture sensitivity ----------------

    private fun setupGestureControls() {
        binding.swipeThresholdInput.setText(prefs.getSwipeThresholdDp().toString())
        binding.doubleTapWindowInput.setText(prefs.getDoubleTapWindowMs().toString())

        binding.applyGesturesButton.setOnClickListener {
            binding.swipeThresholdInput.text.toString().toFloatOrNull()?.let { prefs.setSwipeThresholdDp(it) }
            binding.doubleTapWindowInput.text.toString().toLongOrNull()?.let { prefs.setDoubleTapWindowMs(it) }
            android.widget.Toast.makeText(this, "Gesture settings updated", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Wallpaper rotation interval + manual change ----------------

    private fun setupWallpaperIntervalSpinner() {
        val labels = listOf("Every hour", "Every 6 hours", "Every 12 hours", "Daily", "Weekly")
        val hoursForIndex = listOf(1, 6, 12, 24, 168)
        binding.wallpaperIntervalSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.wallpaperIntervalSpinner.setSelection(
            hoursForIndex.indexOf(prefs.getWallpaperIntervalHours()).coerceAtLeast(3)
        )
        binding.wallpaperIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.setWallpaperIntervalHours(hoursForIndex[position])
                if (prefs.getWallpaperFolderUri() != null) {
                    WallpaperHelper.scheduleRotation(this@SettingsActivity, hoursForIndex[position])
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.changeWallpaperNowButton.setOnClickListener {
            val folder = prefs.getWallpaperFolderUri()
            if (folder == null) {
                android.widget.Toast.makeText(this, "Pick a wallpaper folder first", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                val ok = WallpaperHelper.setRandomWallpaperFromFolder(this, folder)
                if (ok) AccentColorHelper.extractAccentColor(this)?.let { prefs.setAccentColor(it) }
                val msg = if (ok) "Wallpaper changed" else "Couldn't set wallpaper — check the folder has images"
                android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------- Swipe app pools (multi-select) ----------------

    private fun setupSwipePoolButtons() {
        binding.swipeLeftButton.setOnClickListener { pickSwipePool("left") }
        binding.swipeRightButton.setOnClickListener { pickSwipePool("right") }
        binding.swipeUpButton.setOnClickListener { pickSwipePool("up") }
    }

    private fun pickSwipePool(direction: String) {
        val apps = repo.getAllApps()
        val labels = apps.map { it.displayLabel }.toTypedArray()
        val current = prefs.getSwipePool(direction)
        val checked = apps.map { current.contains(it.packageName) }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("Apps for swipe ${direction}")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Save") { _, _ ->
                val selected = apps.filterIndexed { i, _ -> checked[i] }.map { it.packageName }.toSet()
                prefs.setSwipePool(direction, selected)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- Clock / calendar tap targets (single-select) ----------------

    private fun setupTapTargetButtons() {
        binding.clockTapButton.setOnClickListener {
            pickSingleApp("App to open on clock tap") { prefs.setClockTapApp(it) }
        }
        binding.calendarTapButton.setOnClickListener {
            pickSingleApp("App to open on calendar tap") { prefs.setCalendarTapApp(it) }
        }
    }

    private fun pickSingleApp(title: String, onPicked: (String) -> Unit) {
        val apps = repo.getAllApps()
        val labels = apps.map { it.displayLabel }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(labels) { _, which -> onPicked(apps[which].packageName) }
            .show()
    }

    // ---------------- Groups management ----------------

    private fun setupGroupsButton() {
        binding.manageGroupsButton.setOnClickListener { showGroupsList() }
    }

    private fun showGroupsList() {
        val groups = prefs.getGroups()
        if (groups.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Groups")
                .setMessage("No groups yet. Create one from an app's long-press menu in the drawer or favorites list.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        val names = groups.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Groups")
            .setItems(names) { _, which -> showGroupOptions(names[which]) }
            .show()
    }

    private fun showGroupOptions(name: String) {
        val groups = prefs.getGroups()
        val members = groups[name] ?: emptyList()
        val memberLabels = repo.getAllApps(includeHidden = true)
            .filter { members.contains(it.packageName) }
            .joinToString(", ") { it.displayLabel }

        AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage(if (memberLabels.isEmpty()) "No apps in this group." else memberLabels)
            .setPositiveButton("Rename") { _, _ -> renameGroup(name) }
            .setNegativeButton("Delete") { _, _ -> deleteGroup(name) }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun renameGroup(oldName: String) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(oldName)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename group")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().ifBlank { return@setPositiveButton }
                val groups = prefs.getGroups().toMutableMap()
                val members = groups.remove(oldName) ?: emptyList()
                groups[newName] = members
                prefs.setGroups(groups)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGroup(name: String) {
        val groups = prefs.getGroups().toMutableMap()
        groups.remove(name)
        prefs.setGroups(groups)
    }

    private fun setupClockStyleSpinner() {
        val labels = listOf("Regular", "Big hour / small minute + battery")
        binding.clockStyleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.clockStyleSpinner.setSelection(prefs.getClockStyle())
        binding.clockStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.setClockStyle(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFontSpinner() {
        binding.fontSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, FontOptions.names)
        binding.fontSpinner.setSelection(prefs.getFontIndex())
        binding.fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.setFontIndex(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupWallpaperButton() {
        binding.pickWallpaperFolderButton.setOnClickListener {
            folderPicker.launch(null)
        }
    }

    private fun setupLockToggle() {
        binding.lockToggle.isChecked = prefs.isLockOnDoubleTapEnabled()
        binding.lockToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.setLockOnDoubleTapEnabled(isChecked)
            if (isChecked) {
                val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val admin = ComponentName(this, LauncherDeviceAdminReceiver::class.java)
                if (!dpm.isAdminActive(admin)) {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Needed only to lock the screen on double-tap. No data is read or sent.")
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupPermissionsExplainer() {
        binding.permissionsExplainer.text = """
            • EXPAND_STATUS_BAR — lets swipe-down open notifications/quick settings. Automatic, no prompt, no data access.
            • SET_WALLPAPER — lets the app apply the wallpaper you choose. Automatic, no prompt.
            • Device admin (optional, only if you enable double-tap lock) — used solely to lock the screen. No data read or sent.
            • Notification access (optional, only if you enable notification previews) — a sensitive system permission granted separately in Android's own settings. Lets this app read notification text, but only to show a snippet under favorites you've chosen.
            • No INTERNET, no location, no storage permission: the wallpaper folder is accessed only through the folder you explicitly pick. Icons and accent colors are read from apps/wallpaper already on your device, nothing external.
        """.trimIndent()
    }
}
