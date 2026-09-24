package com.sogib.minimallauncher

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
import android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager as RvLinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sogib.minimallauncher.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var repo: AppRepository
    private lateinit var adapter: FavoritesAdapter
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsHelper(this)
        repo = AppRepository(this)

        applyTheme()
        setupClock()
        setupFavorites()
        setupAlphabetIndex()
        setupGestures()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        // Update in place rather than rebuild — rebuilding the adapter/list every
        // resume (e.g. every time the Home button is pressed while already home)
        // forces the RecyclerView to detach and redraw everything, which flickers.
        refreshFavorites()
        setupClock()
    }

    /** Home category intent re-delivered when Home is pressed while already on this
     *  screen. Deliberately does no rebuilding — just accept it, no visual reset. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private fun applyTheme() {
        binding.rootLayout.setBackgroundColor(prefs.getBgColor())
        binding.calendarWidget.setTextColor(prefs.getTextColor())
        binding.clockView.textColor = prefs.getTextColor()
        binding.clockView.sizeScale = prefs.getClockSizeScale()
        binding.alphabetIndex.textSizeSp = prefs.getAlphabetTextSizeSp()

        // Accent: a touch of color pulled from the current wallpaper, used sparingly
        // (just the alphabet index here) rather than repainting the whole minimalist UI.
        val accent = if (prefs.isDynamicAccentEnabled()) resolveAccent() else null
        binding.alphabetIndex.textColor = accent ?: prefs.getTextColor()
    }

    private fun resolveAccent(): Int? {
        var accent = prefs.getAccentColor()
        if (accent == null) {
            accent = AccentColorHelper.extractAccentColor(this)
            if (accent != null) prefs.setAccentColor(accent)
        }
        return accent
    }

    // ---------------- Clock + calendar widget ----------------

    private fun setupClock() {
        binding.clockView.style = prefs.getClockStyle()
        binding.clockView.typeface_ = FontOptions.get(prefs.getFontIndex())
        binding.clockView.setOnClickListener { openTapTarget(prefs.getClockTapApp()) }

        val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance().time)
        binding.calendarWidget.text = dateStr
        binding.calendarWidget.typeface = FontOptions.get(prefs.getFontIndex())
        binding.calendarWidget.setOnClickListener { openTapTarget(prefs.getCalendarTapApp()) }
    }

    private fun openTapTarget(packageName: String?) {
        if (packageName == null) return
        packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
    }

    // ---------------- Favorites (home screen apps) ----------------

    private fun setupFavorites() {
        binding.favoritesList.layoutManager = RvLinearLayoutManager(this)
        refreshFavorites()

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.moveItem(vh.adapterPosition, target.adapterPosition)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {}
            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                prefs.setFavoriteOrder(adapter.currentOrder())
            }
        })
        touchHelper.attachToRecyclerView(binding.favoritesList)
    }

    /** Tracks display-affecting settings so we know when the adapter (not just its
     *  data) needs to be rebuilt — e.g. the user just toggled icons or previews on. */
    private var lastDisplaySignature: String = ""

    private fun refreshFavorites() {
        val all = repo.getAllApps().associateBy { it.packageName }
        val order = prefs.getFavoriteOrder().ifEmpty {
            // First run: seed favorites with up to 12 apps so the screen isn't empty.
            all.keys.take(12).also { prefs.setFavoriteOrder(it) }
        }
        val favorites = order.mapNotNull { all[it] }.toMutableList()

        val signature = "${prefs.isShowIcons()}|${prefs.isNotificationPreviewEnabled()}|" +
            "${prefs.getRowTextSizeSp()}|${prefs.getRowPaddingDp()}|${prefs.getTextColor()}|${prefs.getFontIndex()}"
        val displayChanged = signature != lastDisplaySignature
        lastDisplaySignature = signature

        if (!::adapter.isInitialized || displayChanged) {
            val paddingPx = (prefs.getRowPaddingDp() * resources.displayMetrics.density).toInt()
            adapter = FavoritesAdapter(
                favorites,
                FontOptions.get(prefs.getFontIndex()),
                prefs.getRowTextSizeSp(),
                paddingPx,
                prefs.getTextColor(),
                prefs.isShowIcons(),
                prefs.isNotificationPreviewEnabled(),
                onClick = { repo.launchApp(it) },
                onLongClick = { showFavoriteOptions(it) }
            )
            binding.favoritesList.adapter = adapter
        } else if (favorites.map { it.packageName } != adapter.currentOrder()) {
            // Only touches the RecyclerView when the favorites actually changed
            // (renamed, reordered, added, removed) — not on every resume.
            adapter.replaceAll(favorites)
        } else {
            // Data unchanged, but notification previews may have — cheap refresh.
            adapter.refreshPreviewsOnly()
        }
    }

    private fun showFavoriteOptions(app: AppInfo) {
        val options = arrayOf("Rename", "Remove from favorites", "Add to a group", "App info", "Uninstall")
        AlertDialog.Builder(this)
            .setTitle(app.displayLabel)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameApp(app)
                    1 -> {
                        val remaining = prefs.getFavoriteOrder().filter { it != app.packageName }
                        prefs.setFavoriteOrder(remaining)
                        refreshFavorites()
                    }
                    2 -> addToGroupDialog(app)
                    3 -> openAppInfo(app)
                    4 -> uninstallApp(app)
                }
            }.show()
    }

    private fun openAppInfo(app: AppInfo) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
    }

    private fun uninstallApp(app: AppInfo) {
        startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}")))
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
                refreshFavorites()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addToGroupDialog(app: AppInfo) {
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

    // ---------------- Alphabet index -> opens drawer at that letter ----------------

    private fun setupAlphabetIndex() {
        val letters = repo.lettersPresent(repo.getAllApps())
        binding.alphabetIndex.letters = letters
        binding.alphabetIndex.onLetterSelected = { letter ->
            startActivity(Intent(this, AppDrawerActivity::class.java).putExtra("jump_to_letter", letter.toString()))
        }
    }

    // ---------------- Gestures ----------------

    private var lastTapTime = 0L

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            // Double-tap is detected manually below (onSingleTapUp) so the timing
            // window is configurable instead of locked to the OS default (~300ms).
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val now = System.currentTimeMillis()
                if (now - lastTapTime <= prefs.getDoubleTapWindowMs()) {
                    tryLockScreen()
                    lastTapTime = 0L
                } else {
                    lastTapTime = now
                }
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                val thresholdPx = prefs.getSwipeThresholdDp() * resources.displayMetrics.density

                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > thresholdPx) {
                    if (dx < 0) openRandomFromPool("left") else openRandomFromPool("right")
                    return true
                }
                if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > thresholdPx) {
                    if (dy < 0) {
                        // swipe up -> random app too, per spec
                        openRandomFromPool("up")
                    } else {
                        // swipe down: right half of screen -> notifications, left half -> quick settings
                        val screenWidth = resources.displayMetrics.widthPixels
                        if (e1.x > screenWidth / 2) expandStatusBarPanel(quickSettings = false)
                        else expandStatusBarPanel(quickSettings = true)
                    }
                    return true
                }
                return false
            }
        })

        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                if (detector.scaleFactor < 1f) {
                    // pinch in -> settings
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                } else if (detector.scaleFactor > 1f) {
                    // pinch out -> hidden apps
                    startActivity(Intent(this@MainActivity, AppDrawerActivity::class.java).putExtra("show_hidden", true))
                }
            }
        })

        binding.rootLayout.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun openRandomFromPool(direction: String) {
        val pool = prefs.getSwipePool(direction)
        val all = repo.getAllApps().associateBy { it.packageName }
        val candidates = pool.mapNotNull { all[it] }
        if (candidates.isEmpty()) {
            // Not configured yet: send the user to pick apps for this gesture.
            startActivity(Intent(this, AppDrawerActivity::class.java).putExtra("pick_for_swipe", direction))
            return
        }
        repo.launchApp(candidates.random())
    }

    /** Uses the long-standing EXPAND_STATUS_BAR reflection trick most launchers rely on. */
    private fun expandStatusBarPanel(quickSettings: Boolean) {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val methodName = if (quickSettings) "expandSettingsPanel" else "expandNotificationsPanel"
            val method = statusBarManager.getMethod(methodName)
            method.invoke(statusBarService)
        } catch (e: Exception) {
            // Reflection can fail on some OEM builds/Android versions; fail silently
            // rather than crash — there is no public API for this.
        }
    }

    private fun tryLockScreen() {
        if (!prefs.isLockOnDoubleTapEnabled()) return
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, LauncherDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        } else {
            val intent = Intent(ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(EXTRA_DEVICE_ADMIN, admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Needed only to lock the screen on double-tap. No data is read or sent.")
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Home screen: back does nothing, same as every other launcher.
    }
}
