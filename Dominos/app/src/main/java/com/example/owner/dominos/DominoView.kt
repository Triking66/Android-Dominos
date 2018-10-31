package com.example.owner.dominos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by Owner on 12/1/2017.
 */
class DominoView:View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var dominos = ArrayList<Pair<Int, Int>>() // Set of dominos current player has.
    var selected = Pair(-1, -1) // Selected domino
    var selectIndex = -1 // Index of selected domino
    var paint = Paint()
    var scroll = 0f // How far the domino bar is scrolled.
    var touchPos = Pair(-1f, -1f) // Latest position known to be touched on the bar.
    var moved = false // If the bar has been scrolled, do not perform a click.
    var active = false // If you are able to change the selected tile.
    var win = ArrayList<Int>() // If it is not empty, the game is over with winners contained within.
    var turnChange = 0 // If the turn is changing, don't show numbers, and touching will advance turn.


    fun setUp() {
        paint.textSize = 40f // A small function to set the paint's text size to 40.
    }

    fun changeset(d: ArrayList<Pair<Int, Int>>) {
        dominos = d // Changing current player,
        invalidate()
    }

    fun setSelectedPiece(p: Pair<Int, Int>){ // For first turn, select piece they are forced to use.
        var i = 0
        while (i < dominos.size){
            if (dominos[i].first == p.first && dominos[i].second == p.second){
                selectIndex = i
                selected = Pair(p.first, p.second)
            }
            i++
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas !is Canvas) {return} // Smartcast
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint) // Fill background with light gray.
        if (win.size == 0) {
            for ((p, d) in dominos.withIndex()) { // p = Index, d = domino piece
                if (p == selectIndex) {
                    paint.color = Color.BLUE
                    canvas.drawRect((70f * p) + scroll + 15, 0f, (70f * (p + 1)) + 15 + scroll, height.toFloat(), paint)
                }
                if (turnChange == 0) { // If the turn is not changing, show dominos
                    paint.color = Color.WHITE
                    canvas.drawRect((70f * p) + scroll + 20, height.toFloat() / 10, (70f * (p + 1)) + 10 + scroll, height.toFloat() * 9 / 10, paint)
                    paint.color = Color.BLACK
                    canvas.drawText(d.first.toString(), 70f * p + scroll + 35, height.toFloat() * 3.5f / 10, paint)
                    canvas.drawText("---", 70f * p + scroll + 25, height.toFloat() * 5.5f / 10, paint)
                    canvas.drawText(d.second.toString(), 70f * p + scroll + 35, height.toFloat() * 7.5f / 10, paint)
                }
                else { // If the turn is changing, show text to pass to next player.
                    paint.color = Color.WHITE
                    canvas.drawText("Pass to player $turnChange and tap here.", 20f, height.toFloat() * 5.5f / 10, paint)
                }
            }
        }
        else {
            var fs = "Winners: Player " + win[0].toString()
            for (i in win){
                if (i != win[0])
                    fs += ", Player " + i.toString()
            }
            paint.textSize = 60f
            paint.color = Color.WHITE
            canvas.drawText(fs, 20f, height.toFloat()* 5.5f / 10, paint)
        }
        paint.color = Color.BLACK // Draw two edges of 20dp on each side for borders.
        canvas.drawRect(0f, 0f, 20f, height.toFloat(), paint)
        canvas.drawRect(width-20f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event !is MotionEvent) {return false} // Smartcast

        if (!active) {return false}

        if (turnChange != 0) {return true}

        when(event.action) {
            MotionEvent.ACTION_DOWN -> { // On touching the screen
                touchPos = Pair(event.x, event.y) // Set touchPos to where you tapped
                moved = false // You haven't moved to scroll yet.
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(touchPos.first - event.x) > 5) { // If you moved more than 5 pixels horizontally
                    scroll += event.x - touchPos.first  // The screen is scrolled
                    touchPos = Pair(event.x, event.y) // The new position of your finger is recorded
                    if (scroll < 0 - (70*(dominos.size) + 20)+width)
                        scroll = 0-(70f*(dominos.size) + 20)+width
                    if (scroll > 0)
                        scroll = 0f
                    moved = true // You have decided to scroll the dominos instead of selecting one.
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!moved) { // If you didn't move, aka you tapped the screen
                    if (event.x.toInt() + scroll.toInt() > 20) { // If you tapped on a tile
                        val temp = (event.x.toInt() - scroll.toInt() - 20) / 70
                        if (temp < dominos.size) { // If the tile you tapped exists
                            selectIndex = temp // Select tapped tile
                            selected = dominos[temp]
                        }
                    }
                }
            }
        }
        invalidate() // Refresh screen to show changes.
        return true
    }

}