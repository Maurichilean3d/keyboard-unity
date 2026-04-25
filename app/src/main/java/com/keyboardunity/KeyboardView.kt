package com.keyboardunity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    data class KeyDef(
        val label: String,
        val hidCode: Int,
        val weight: Float = 1f,
        val isModifier: Boolean = false,
        val modifierBit: Int = 0,
        val isToggle: Boolean = false  // Caps Lock
    )

    var onKeyDown: ((modifier: Int, keyCodes: List<Int>) -> Unit)? = null
    var onKeyUp: (() -> Unit)? = null

    // Modifier state
    private var modifiers = 0
    private var capsLock = false
    private val activeModifierBits = mutableSetOf<Int>()

    private val rows: List<List<KeyDef>> = listOf(
        // Row 0 – Function keys
        listOf(
            KeyDef("Esc",  HidDescriptor.KEY_ESCAPE, 1f),
            KeyDef("F1",   HidDescriptor.KEY_F1),
            KeyDef("F2",   HidDescriptor.KEY_F2),
            KeyDef("F3",   HidDescriptor.KEY_F3),
            KeyDef("F4",   HidDescriptor.KEY_F4),
            KeyDef("F5",   HidDescriptor.KEY_F5),
            KeyDef("F6",   HidDescriptor.KEY_F6),
            KeyDef("F7",   HidDescriptor.KEY_F7),
            KeyDef("F8",   HidDescriptor.KEY_F8),
            KeyDef("F9",   HidDescriptor.KEY_F9),
            KeyDef("F10",  HidDescriptor.KEY_F10),
            KeyDef("F11",  HidDescriptor.KEY_F11),
            KeyDef("F12",  HidDescriptor.KEY_F12),
            KeyDef("Del",  HidDescriptor.KEY_DELETE)
        ),
        // Row 1 – Number row
        listOf(
            KeyDef("`",    HidDescriptor.KEY_GRAVE),
            KeyDef("1",    HidDescriptor.KEY_1),
            KeyDef("2",    HidDescriptor.KEY_2),
            KeyDef("3",    HidDescriptor.KEY_3),
            KeyDef("4",    HidDescriptor.KEY_4),
            KeyDef("5",    HidDescriptor.KEY_5),
            KeyDef("6",    HidDescriptor.KEY_6),
            KeyDef("7",    HidDescriptor.KEY_7),
            KeyDef("8",    HidDescriptor.KEY_8),
            KeyDef("9",    HidDescriptor.KEY_9),
            KeyDef("0",    HidDescriptor.KEY_0),
            KeyDef("-",    HidDescriptor.KEY_MINUS),
            KeyDef("=",    HidDescriptor.KEY_EQUAL),
            KeyDef("Bksp", HidDescriptor.KEY_BACKSPACE, 1.8f)
        ),
        // Row 2 – QWERTY
        listOf(
            KeyDef("Tab",  HidDescriptor.KEY_TAB, 1.4f),
            KeyDef("Q",    HidDescriptor.KEY_Q),
            KeyDef("W",    HidDescriptor.KEY_W),
            KeyDef("E",    HidDescriptor.KEY_E),
            KeyDef("R",    HidDescriptor.KEY_R),
            KeyDef("T",    HidDescriptor.KEY_T),
            KeyDef("Y",    HidDescriptor.KEY_Y),
            KeyDef("U",    HidDescriptor.KEY_U),
            KeyDef("I",    HidDescriptor.KEY_I),
            KeyDef("O",    HidDescriptor.KEY_O),
            KeyDef("P",    HidDescriptor.KEY_P),
            KeyDef("[",    HidDescriptor.KEY_LEFT_BRACKET),
            KeyDef("]",    HidDescriptor.KEY_RIGHT_BRACKET),
            KeyDef("\\",   HidDescriptor.KEY_BACKSLASH, 1.4f)
        ),
        // Row 3 – ASDF
        listOf(
            KeyDef("Caps", HidDescriptor.KEY_CAPS_LOCK, 1.6f, isModifier = true, isToggle = true),
            KeyDef("A",    HidDescriptor.KEY_A),
            KeyDef("S",    HidDescriptor.KEY_S),
            KeyDef("D",    HidDescriptor.KEY_D),
            KeyDef("F",    HidDescriptor.KEY_F),
            KeyDef("G",    HidDescriptor.KEY_G),
            KeyDef("H",    HidDescriptor.KEY_H),
            KeyDef("J",    HidDescriptor.KEY_J),
            KeyDef("K",    HidDescriptor.KEY_K),
            KeyDef("L",    HidDescriptor.KEY_L),
            KeyDef(";",    HidDescriptor.KEY_SEMICOLON),
            KeyDef("'",    HidDescriptor.KEY_APOSTROPHE),
            KeyDef("Enter",HidDescriptor.KEY_ENTER, 2.2f)
        ),
        // Row 4 – ZXCV
        listOf(
            KeyDef("Shift", HidDescriptor.KEY_NONE, 2.0f, isModifier = true, modifierBit = HidDescriptor.MOD_LEFT_SHIFT),
            KeyDef("Z",    HidDescriptor.KEY_Z),
            KeyDef("X",    HidDescriptor.KEY_X),
            KeyDef("C",    HidDescriptor.KEY_C),
            KeyDef("V",    HidDescriptor.KEY_V),
            KeyDef("B",    HidDescriptor.KEY_B),
            KeyDef("N",    HidDescriptor.KEY_N),
            KeyDef("M",    HidDescriptor.KEY_M),
            KeyDef(",",    HidDescriptor.KEY_COMMA),
            KeyDef(".",    HidDescriptor.KEY_DOT),
            KeyDef("/",    HidDescriptor.KEY_SLASH),
            KeyDef("Shift", HidDescriptor.KEY_NONE, 2.6f, isModifier = true, modifierBit = HidDescriptor.MOD_RIGHT_SHIFT)
        ),
        // Row 5 – Bottom row
        listOf(
            KeyDef("Ctrl",  HidDescriptor.KEY_NONE, 1.3f, isModifier = true, modifierBit = HidDescriptor.MOD_LEFT_CTRL),
            KeyDef("Alt",   HidDescriptor.KEY_NONE, 1.3f, isModifier = true, modifierBit = HidDescriptor.MOD_LEFT_ALT),
            KeyDef("",      HidDescriptor.KEY_SPACE, 5.0f),
            KeyDef("AltGr", HidDescriptor.KEY_NONE, 1.3f, isModifier = true, modifierBit = HidDescriptor.MOD_RIGHT_ALT),
            KeyDef("←",     HidDescriptor.KEY_LEFT),
            KeyDef("↑",     HidDescriptor.KEY_UP),
            KeyDef("↓",     HidDescriptor.KEY_DOWN),
            KeyDef("→",     HidDescriptor.KEY_RIGHT)
        )
    )

    // Computed rects per key; rebuilt in onSizeChanged
    private val keyRects = mutableListOf<Triple<RectF, KeyDef, Int>>() // rect, key, rowIndex

    // Currently pressed key index
    private var pressedIndex = -1

    // Paint objects
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1E1E2E.toInt() }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF313244.toInt() }
    private val modActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF89B4FA.toInt() }
    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF585B70.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val textSmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val keyGap = 4f
    private val cornerRadius = 6f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildKeyRects(w.toFloat(), h.toFloat())
        textPaint.textSize = h / rows.size * 0.38f
        textSmPaint.textSize = h / rows.size * 0.30f
    }

    private fun rebuildKeyRects(w: Float, h: Float) {
        keyRects.clear()
        val rowHeight = h / rows.size
        rows.forEachIndexed { rowIdx, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val unitWidth = w / totalWeight
            var x = 0f
            row.forEach { key ->
                val rect = RectF(x + keyGap / 2, rowIdx * rowHeight + keyGap / 2,
                                 x + unitWidth * key.weight - keyGap / 2, (rowIdx + 1) * rowHeight - keyGap / 2)
                keyRects.add(Triple(rect, key, rowIdx))
                x += unitWidth * key.weight
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bgPaint.color)

        keyRects.forEachIndexed { idx, (rect, key, _) ->
            val isActive = key.isModifier && !key.isToggle && (modifiers and key.modifierBit) != 0
            val isCapsActive = key.isToggle && capsLock
            val isPressed = idx == pressedIndex
            val paint = when {
                isPressed             -> pressedPaint
                isActive || isCapsActive -> modActivePaint
                else                  -> keyPaint
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            val label = if (key.label.isEmpty()) "Space" else key.label
            val cx = rect.centerX()
            val cy = rect.centerY()
            val tp = if (label.length > 4) textSmPaint else textPaint
            val textY = cy - (tp.descent() + tp.ascent()) / 2
            val textColor = if (isActive || isCapsActive) 0xFF1E1E2E.toInt() else Color.WHITE
            tp.color = textColor
            canvas.drawText(label, cx, textY, tp)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val idx = findKey(event.x, event.y)
                if (idx >= 0) {
                    pressedIndex = idx
                    handleKeyDown(keyRects[idx].second)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasPressed = pressedIndex
                pressedIndex = -1
                if (wasPressed >= 0) {
                    val key = keyRects[wasPressed].second
                    if (!key.isModifier) {
                        // Release: send empty report after a short delay
                        postDelayed({ onKeyUp?.invoke() }, 50)
                    }
                }
                invalidate()
            }
        }
        return true
    }

    private fun handleKeyDown(key: KeyDef) {
        if (key.isToggle) {
            // Caps Lock toggle
            capsLock = !capsLock
            invalidate()
            return
        }
        if (key.isModifier) {
            // Toggle modifier bit
            modifiers = modifiers xor key.modifierBit
            invalidate()
            return
        }
        // Regular key
        var mod = modifiers
        if (capsLock) mod = mod or HidDescriptor.MOD_LEFT_SHIFT
        onKeyDown?.invoke(mod, if (key.hidCode != HidDescriptor.KEY_NONE) listOf(key.hidCode) else emptyList())
        // Clear one-shot modifiers (shift stays until user toggles again)
        // We keep modifiers persistent – user must tap again to deactivate
    }

    private fun findKey(x: Float, y: Float): Int {
        keyRects.forEachIndexed { idx, (rect, _, _) ->
            if (rect.contains(x, y)) return idx
        }
        return -1
    }

    fun resetModifiers() {
        modifiers = 0
        invalidate()
    }
}
