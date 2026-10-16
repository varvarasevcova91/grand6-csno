package com.featherroad.quest.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.featherroad.quest.R
import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.domain.model.EntityKind
import com.featherroad.quest.domain.model.RoadEntity
import kotlin.math.abs
import kotlin.math.min

class RoadLaneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onTap: (() -> Unit)? = null
    var onLaneSwipe: ((Int) -> Unit)? = null

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val framePx = resources.getDimensionPixelSize(R.dimen.road_padding) +
        resources.getDimensionPixelSize(R.dimen.road_border)
    private val maxBoardPx = resources.getDimensionPixelSize(R.dimen.road_board_max)
    private val swipeThreshold = resources.displayMetrics.density * SWIPE_DP

    private val activeViews = HashMap<Long, ImageView>()
    private val pool = ArrayList<ImageView>()

    private val playerView = ImageView(context)
    private val boostAura = View(context)

    private var scrollPhase = 0f
    private var boardWidth = 0
    private var boardLeft = 0
    private var laneWidth = 0
    private var playerLane = 1
    private var boosting = false
    private var laneAnimating = false

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                onTap?.invoke()
                return true
            }

            override fun onFling(
                start: MotionEvent?,
                end: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val from = start ?: return false
                val dx = end.x - from.x
                if (abs(dx) < swipeThreshold || abs(dx) < abs(end.y - from.y)) return false
                onLaneSwipe?.invoke(if (dx > 0) 1 else -1)
                return true
            }
        }
    )

    init {
        setWillNotDraw(false)
        isClickable = true
        isFocusable = true

        dividerPaint.color = ContextCompat.getColor(context, R.color.lane_divider)
        dividerPaint.strokeWidth = resources.displayMetrics.density * 3f
        dividerPaint.style = Paint.Style.STROKE
        dividerPaint.pathEffect = DashPathEffect(floatArrayOf(DASH_ON, DASH_OFF), 0f)

        streakPaint.color = ContextCompat.getColor(context, R.color.cyber_cyan)
        streakPaint.alpha = 64
        streakPaint.strokeWidth = resources.displayMetrics.density * 2f
        streakPaint.style = Paint.Style.STROKE

        glowPaint.color = ContextCompat.getColor(context, R.color.neon_pink)
        glowPaint.alpha = 70
        glowPaint.style = Paint.Style.FILL

        sheenPaint.style = Paint.Style.FILL

        boostAura.setBackgroundResource(R.drawable.bg_neon_ring)
        boostAura.alpha = 0f
        addView(boostAura, LayoutParams(0, 0))

        playerView.scaleType = ImageView.ScaleType.FIT_CENTER
        playerView.setImageResource(R.drawable.sprite_car_hero)
        playerView.contentDescription = context.getString(R.string.cd_player_car)
        addView(
            playerView,
            LayoutParams(
                resources.getDimensionPixelSize(R.dimen.player_car_width),
                resources.getDimensionPixelSize(R.dimen.player_car_height)
            )
        )
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        boardWidth = min(width, maxBoardPx)
        boardLeft = (width - boardWidth) / 2
        laneWidth = (boardWidth - 2 * framePx) / GameConfig.LANE_COUNT
        sheenPaint.shader = LinearGradient(
            0f,
            height * 0.52f,
            0f,
            height * 0.72f,
            intArrayOf(0x00FFFFFF, 0x1AFFFFFF, 0x00FFFFFF),
            null,
            Shader.TileMode.CLAMP
        )
        positionPlayer()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setPlayerSprite(resId: Int) {
        playerView.setImageResource(resId)
    }

    fun setBoosting(active: Boolean) {
        boosting = active
        boostAura.alpha = if (active) 0.85f else 0f
    }

    fun renderFrame(entities: List<RoadEntity>, lane: Int, phase: Float) {
        val target = lane.coerceIn(0, GameConfig.LANE_COUNT - 1)
        val laneChanged = target != playerLane
        playerLane = target
        scrollPhase = phase
        syncEntities(entities)
        positionPlayer(laneChanged)
        invalidate()
    }

    fun clearEntities() {
        activeViews.values.forEach { view ->
            view.visibility = View.GONE
            pool.add(view)
        }
        activeViews.clear()
    }

    private fun syncEntities(entities: List<RoadEntity>) {
        if (laneWidth <= 0) return
        val seen = HashSet<Long>(entities.size)
        entities.forEach { entity ->
            seen.add(entity.id)
            val view = activeViews[entity.id] ?: obtainView(entity)
            activeViews[entity.id] = view
            place(view, entity.lane, entity.y)
        }
        val gone = activeViews.keys.filter { !seen.contains(it) }
        gone.forEach { id ->
            val view = activeViews.remove(id) ?: return@forEach
            view.visibility = View.GONE
            pool.add(view)
        }
    }

    private fun obtainView(entity: RoadEntity): ImageView {
        val view = if (pool.isNotEmpty()) pool.removeAt(pool.size - 1) else createEntityView()
        val width = resources.getDimensionPixelSize(SpriteCatalog.entityWidth(entity.kind))
        val height = resources.getDimensionPixelSize(SpriteCatalog.entityHeight(entity.kind))
        val params = view.layoutParams as LayoutParams
        params.width = min(width, laneWidth - entityInset())
        params.height = height
        view.layoutParams = params
        view.setImageResource(SpriteCatalog.entity(entity.kind))
        view.contentDescription = labelFor(entity.kind)
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        view.visibility = View.VISIBLE
        return view
    }

    private fun createEntityView(): ImageView {
        val view = ImageView(context)
        view.scaleType = ImageView.ScaleType.FIT_CENTER
        view.layoutParams = LayoutParams(laneWidth, laneWidth)
        addView(view, indexOfChild(playerView))
        return view
    }

    private fun entityInset(): Int = (resources.displayMetrics.density * 8f).toInt()

    private fun labelFor(kind: EntityKind): String = when (kind) {
        EntityKind.TRAFFIC -> context.getString(R.string.cd_hero_car)
        EntityKind.POLICE -> context.getString(R.string.cd_hero_car)
        EntityKind.CASH -> context.getString(R.string.row_cash)
        EntityKind.DIAMOND -> context.getString(R.string.row_diamonds)
        EntityKind.MULTIPLIER -> context.getString(R.string.row_multiplier)
    }

    private fun place(view: View, lane: Int, y: Float) {
        val params = view.layoutParams as ViewGroup.LayoutParams
        val centerX = laneCenterX(lane)
        view.translationX = centerX - params.width / 2f
        view.translationY = y * height - params.height / 2f
    }

    private fun positionPlayer(animated: Boolean = false) {
        if (laneWidth <= 0) return
        val params = playerView.layoutParams
        val target = laneCenterX(playerLane) - params.width / 2f
        if (animated) {
            laneAnimating = true
            playerView.animate().cancel()
            playerView.animate()
                .translationX(target)
                .setDuration(GameConfig.LANE_CHANGE_MS)
                .withEndAction {
                    try {
                        if (!isAttachedToWindow) return@withEndAction
                        laneAnimating = false
                    } catch (e: Exception) {
                    }
                }
                .start()
        } else if (!laneAnimating) {
            playerView.translationX = target
        }
        playerView.translationY = GameConfig.PLAYER_Y * height - params.height / 2f

        val auraParams = boostAura.layoutParams
        auraParams.width = params.width + entityInset() * 2
        auraParams.height = params.height + entityInset() * 2
        boostAura.layoutParams = auraParams
        boostAura.translationX = target - entityInset()
        boostAura.translationY = playerView.translationY - entityInset()
    }

    private fun laneCenterX(lane: Int): Float {
        val clamped = lane.coerceIn(0, GameConfig.LANE_COUNT - 1)
        return boardLeft + framePx + laneWidth * clamped + laneWidth / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (laneWidth <= 0) return
        val top = 0f
        val bottom = height.toFloat()
        val offset = (scrollPhase * (DASH_ON + DASH_OFF)) % (DASH_ON + DASH_OFF)
        dividerPaint.pathEffect = DashPathEffect(floatArrayOf(DASH_ON, DASH_OFF), offset)
        for (index in 1 until GameConfig.LANE_COUNT) {
            val x = boardLeft + framePx + laneWidth * index.toFloat()
            canvas.drawLine(x, top, x, bottom, dividerPaint)
        }
        val edgeLeft = boardLeft + framePx * 0.5f
        val edgeRight = boardLeft + boardWidth - framePx * 0.5f
        canvas.drawLine(edgeLeft, top, edgeLeft, bottom, streakPaint)
        canvas.drawLine(edgeRight, top, edgeRight, bottom, streakPaint)
        canvas.drawRect(0f, height * 0.52f, width.toFloat(), height * 0.72f, sheenPaint)
        val glowRadius = laneWidth * if (boosting) 0.68f else 0.55f
        val glowX = laneCenterX(playerLane)
        val glowY = GameConfig.PLAYER_Y * height
        glowPaint.alpha = 26
        canvas.drawCircle(glowX, glowY, glowRadius, glowPaint)
        glowPaint.alpha = 38
        canvas.drawCircle(glowX, glowY, glowRadius * 0.72f, glowPaint)
        glowPaint.alpha = 58
        canvas.drawCircle(glowX, glowY, glowRadius * 0.44f, glowPaint)
    }

    override fun onDetachedFromWindow() {
        playerView.animate().cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val DASH_ON = 26f
        private const val DASH_OFF = 22f
        private const val SWIPE_DP = 48f
    }
}
