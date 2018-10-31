package com.example.owner.dominos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by Owner on 11/30/2017.
 */
class DominoScreen:View{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var paint = Paint() // Paint Object used to draw.

    // Grid of dominoes, if first number is -1, the other number tells the game if you can place something there
    // Otherwise, the second number shows orientation of tile. 0: Vertical M, 1: Horizontal M, 2: Left, 3: Bottom,
    // 4: Right, 5: Top.
    var grid = Array<Array<Pair<Int, Int>>>(23, {i -> Array<Pair<Int, Int>>(23, {j -> Pair<Int, Int>(-1, -1)})})

    var state = -1 // Current state, changes what happens when tapped. 0: Tap will begin placement of tile
    // 1: Tap will end placement of tile; 2: Tap will do nothing (disabled).

    var selectedPiece = Pair(6, 6) // Currently selected game piece.
    var selectedSpace = Pair(-1, -1) // Currently selected space.
    var firstNum = -1 // Which number on the tile is being placed first.
    var gameMode = "Runout" // What gamemode is being played.
    var total = 0 // Current total for edge pieces (used in Points game)

    fun setTappable(x: Int = -1, y: Int = -1) {
        // Sets for each space whether it can be tapped to do something.
        var a = 0
        var b : Int
        while (a < grid.size) {
            b = 0
            while (b < grid[a].size) {
                // First turn, can only place best piece
                if (state == -1) {
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        if (a == 10 && b == 11)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 12 && b == 11)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 11 && b == 10)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 11 && b == 12)
                            grid[a][b] = Pair(-1, 0)
                    }
                }
                else if (state == 0) { // If you haven't chosen a place to place your tile yet...
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        // If a tile next to the one being checked is the same number as one of the selected tile's numbers...
                        if (a - 1 >= 0 && grid[a - 1][b].first != -1 && (grid[a - 1][b].first == x || grid[a - 1][b].first == y) && grid[a - 1][b].second == 4)
                            // If the remaining tiles next to the one being checked are empty...
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                // If the tile with the correct number is at the end of a branch...
                                if ((b - 1 < 0 || grid[a - 1][b - 1].first == -1) && (b + 1 > 22 || grid[a - 1][b + 1].first == -1)) {
                                    // You can tap on the tile and place the selected domino here.
                                    if (x == y) { // If it is a double, one more check
                                        if (b - 1 >= 0 && b + 1 < 23) // If tile is not at the edge you can place it.
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a-1][b].first == x)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (a + 1 < 22 && grid[a + 1][b].first != -1 && (grid[a + 1][b].first == x || grid[a + 1][b].first == y) && grid[a + 1][b].second == 2)
                            if ((a - 1 < 0 || grid[a - 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                if ((b - 1 < 0 || grid[a + 1][b - 1].first == -1) && (b + 1 > 22 || grid[a + 1][b + 1].first == -1)) {
                                    if (x == y) {
                                        if (b - 1 >= 0 && b + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a+1][b].first == x)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (b - 1 >= 0 && grid[a][b - 1].first != -1 && (grid[a][b - 1].first == x || grid[a][b - 1].first == y) && grid[a][b - 1].second == 3)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                if ((a - 1 < 0 || grid[a - 1][b - 1].first == -1) && (a + 1 > 22 || grid[a + 1][b - 1].first == -1)) {
                                    if (x == y) {
                                        if (a - 1 >= 0 && a + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a][b-1].first == x)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (b + 1 < 22 && grid[a][b + 1].first != -1 && (grid[a][b + 1].first == x || grid[a][b + 1].first == y) && grid[a][b + 1].second == 5)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1))
                                if ((a - 1 < 0 || grid[a - 1][b + 1].first == -1) && (a + 1 > 22 || grid[a + 1][b + 1].first == -1)) {
                                    if (x == y) {
                                        if (a - 1 >= 0 && a + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a][b+1].first == x)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                    }
                }
                else if (state == 1) { // If you have a spot chosen for your tile to begin...
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        if (a == x-2 && b == y)
                            if ((a - 1 < 0 || grid[a - 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x+2 && b == y)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y-2)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y+2)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y)
                            grid[a][b] = Pair(-1, 10)
                    }
                }
                else {
                    if (grid[a][b].first == -1)
                        grid[a][b] = Pair(-1, -1)
                }
                b++
            }
            a++
        }
        invalidate()
    }

    fun matTappable(x: Int = -1, y: Int = -1) { // setTappable for Matador gamemode.
        // Sets for each space whether it can be tapped to do something.
        var a = 0
        var b : Int
        while (a < grid.size) {
            b = 0
            while (b < grid[a].size) {
                // First turn, can only place best piece
                if (state == -1) {
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        if (a == 10 && b == 11)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 12 && b == 11)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 11 && b == 10)
                            grid[a][b] = Pair(-1, 0)
                        if (a == 11 && b == 12)
                            grid[a][b] = Pair(-1, 0)
                    }
                }
                else if (state == 0) { // If you haven't chosen a place to place your tile yet...
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        // If a tile next to the one being checked is the same number as one of the selected tile's numbers...
                        if (a - 1 >= 0 && grid[a - 1][b].first != -1 && (grid[a - 1][b].first + x == 7 || grid[a - 1][b].first + y == 7 ||
                                (grid[a-1][b].first == 0 && (x + y == 7 || (x == 0 && y == 0)))) && grid[a - 1][b].second == 4)
                        // If the remaining tiles next to the one being checked are empty...
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                            // If the tile with the correct number is at the end of a branch...
                                if ((b - 1 < 0 || grid[a - 1][b - 1].first == -1) && (b + 1 > 22 || grid[a - 1][b + 1].first == -1)) {
                                    // You can tap on the tile and place the selected domino here.
                                    if (x == y) { // If it is a double, one more check
                                        if (b - 1 >= 0 && b + 1 < 23) // If tile is not at the edge you can place it.
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a-1][b].first + x == 7)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (a + 1 < 22 && grid[a + 1][b].first != -1 && (grid[a + 1][b].first + x == 7 || grid[a + 1][b].first + y == 7 ||
                                (grid[a+1][b].first == 0 && (x + y == 7 || (x == 0 && y == 0)))) && grid[a + 1][b].second == 2)
                            if ((a - 1 < 0 || grid[a - 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                if ((b - 1 < 0 || grid[a + 1][b - 1].first == -1) && (b + 1 > 22 || grid[a + 1][b + 1].first == -1)) {
                                    if (x == y) {
                                        if (b - 1 >= 0 && b + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a+1][b].first + x == 7)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (b - 1 >= 0 && grid[a][b - 1].first != -1 && (grid[a][b - 1].first + x == 7 || grid[a][b - 1].first + y == 7 ||
                                (grid[a][b-1].first == 0 && (x + y == 7 || (x == 0 && y == 0)))) && grid[a][b - 1].second == 3)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                if ((a - 1 < 0 || grid[a - 1][b - 1].first == -1) && (a + 1 > 22 || grid[a + 1][b - 1].first == -1)) {
                                    if (x == y) {
                                        if (a - 1 >= 0 && a + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a][b-1].first + x == 7)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                        if (b + 1 < 22 && grid[a][b + 1].first != -1 && (grid[a][b + 1].first + x == 7 || grid[a][b + 1].first + y == 7 ||
                                (grid[a][b+1].first == 0 && (x + y == 7 || (x == 0 && y == 0)))) && grid[a][b + 1].second == 5)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1))
                                if ((a - 1 < 0 || grid[a - 1][b + 1].first == -1) && (a + 1 > 22 || grid[a + 1][b + 1].first == -1)) {
                                    if (x == y) {
                                        if (a - 1 >= 0 && a + 1 < 23)
                                            grid[a][b] = Pair(-1, x)
                                    }
                                    else if (grid[a][b+1].first + x == 7)
                                        grid[a][b] = Pair(-1, x)
                                    else
                                        grid[a][b] = Pair(-1, y)
                                }
                    }
                }
                else if (state == 1) { // If you have a spot chosen for your tile to begin...
                    if (grid[a][b].first == -1) {
                        grid[a][b] = Pair(-1, -1)
                        if (a == x-2 && b == y)
                            if ((a - 1 < 0 || grid[a - 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x+2 && b == y)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y-2)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (b - 1 < 0 || grid[a][b - 1].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y+2)
                            if ((a + 1 > 22 || grid[a + 1][b].first == -1) && (a - 1 < 0 || grid[a - 1][b].first == -1) && (b + 1 > 22 || grid[a][b + 1].first == -1))
                                grid[a][b] = Pair(-1, 0)
                        if (a == x && b == y)
                            grid[a][b] = Pair(-1, 10)
                    }
                }
                else {
                    if (grid[a][b].first == -1)
                        grid[a][b] = Pair(-1, -1)
                }
                b++
            }
            a++
        }
        invalidate()
    }

    fun touchGrid(x: Int, y: Int) { // When you touch a spot on the grid that can do something...
        if (state == -1){ // If it's the first turn
            state = 2 // End the turn
            if(x == 10 && y == 11){ // Place the piece based on touched location, vertical or horizontal.
                grid[10][11] = Pair(selectedPiece.first, 2)
                grid[11][11] = Pair(10, 1)
                grid[12][11] = Pair(selectedPiece.second, 4)
            }
            else if(x == 12 && y == 11){
                grid[10][11] = Pair(selectedPiece.second, 2)
                grid[11][11] = Pair(10, 1)
                grid[12][11] = Pair(selectedPiece.first, 4)
            }
            else if(x == 11 && y == 10){
                grid[11][10] = Pair(selectedPiece.first, 5)
                grid[11][11] = Pair(10, 0)
                grid[11][12] = Pair(selectedPiece.second, 3)
            }
            else if(x == 11 && y == 12) {
                grid[11][10] = Pair(selectedPiece.first, 5)
                grid[11][11] = Pair(10, 0)
                grid[11][12] = Pair(selectedPiece.second, 3)
            }
            total = selectedPiece.first + selectedPiece.second
            if (gameMode == "Matador")
                matTappable()
            else
                setTappable() // Reset to show new tappable area.
        }
        else if (state == 0){
            if (selectedPiece.first == selectedPiece.second) { // If you are placing a double
                state = 2 // Your turn is over
                if ((x+1 < 23 && grid[x+1][y].first != -1) || (x-1 >= 0 && grid[x-1][y].first != -1)) {
                    // Placing vertically
                    grid[x][y-1] = Pair(selectedPiece.first, 5)
                    grid[x][y] = Pair(10, 0)
                    grid[x][y+1] = Pair(selectedPiece.second, 3)
                }
                else {
                    // Placing horizontally
                    grid[x-1][y] = Pair(selectedPiece.first, 2)
                    grid[x][y] = Pair(10, 1)
                    grid[x+1][y] = Pair(selectedPiece.second, 4)
                }
                total += selectedPiece.second
                if (gameMode == "Matador")
                    matTappable()
                else
                    setTappable() // Reset to show new tappable area.
            }
            else { // Otherwise begin placement of normal tile.
                state = 1 // Begin second phase of placement.
                selectedSpace = Pair(x, y) // Selected tile for first number.
                firstNum = grid[x][y].second // Selected number being placed in selected space.
                if (gameMode == "Matador")
                    matTappable(x, y)
                else
                    setTappable(x, y) // Reset to show new tappable area.
            }
        }
        else if (state == 1) {
            state = 2 // End turn.
            if (x > selectedSpace.first){ // If you are placing the tile facing left
                grid[selectedSpace.first][selectedSpace.second] = Pair(firstNum, 2) // Place first tapped space
                grid[x-1][y] = Pair(10, 1) // Place middle space
                if(selectedPiece.first == firstNum) { // Place last tapped space with number that is not the first one.
                    grid[x][y] = Pair(selectedPiece.second, 4)
                    total += selectedPiece.second - selectedPiece.first
                }
                else {
                    grid[x][y] = Pair(selectedPiece.first, 4)
                    total += selectedPiece.first - selectedPiece.second
                }
            }
            else if (x < selectedSpace.first){
                grid[selectedSpace.first][selectedSpace.second] = Pair(firstNum, 4)
                grid[x+1][y] = Pair(10, 1)
                if(selectedPiece.first == firstNum) {
                    grid[x][y] = Pair(selectedPiece.second, 2)
                    total += selectedPiece.second - selectedPiece.first
                }
                else {
                    grid[x][y] = Pair(selectedPiece.first, 2)
                    total += selectedPiece.first - selectedPiece.second
                }
            }
            else if (y > selectedSpace.second){
                grid[selectedSpace.first][selectedSpace.second] = Pair(firstNum, 5)
                grid[x][y-1] = Pair(10, 0)
                if(selectedPiece.first == firstNum) {
                    grid[x][y] = Pair(selectedPiece.second, 3)
                    total += selectedPiece.second - selectedPiece.first
                }
                else {
                    grid[x][y] = Pair(selectedPiece.first, 3)
                    total += selectedPiece.first - selectedPiece.second
                }
            }
            else {
                grid[selectedSpace.first][selectedSpace.second] = Pair(firstNum, 3)
                grid[x][y+1] = Pair(10, 0)
                if(selectedPiece.first == firstNum) {
                    grid[x][y] = Pair(selectedPiece.second, 5)
                    total += selectedPiece.second - selectedPiece.first
                }
                else {
                    grid[x][y] = Pair(selectedPiece.first, 5)
                    total += selectedPiece.first - selectedPiece.second
                }
            }
            if (gameMode == "Matador")
                matTappable()
            else
                setTappable() // Reset to show new tappable area.
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event !is MotionEvent) {return false} // Smartcast

        if (state == 2) {return false}

        when (event.action) { // For each event:
            MotionEvent.ACTION_DOWN -> { // If it is touch down:
                val x = event.x.toInt() / (width / 23) // X value of tile touched
                val y = event.y.toInt() / (height / 23) // Y value of tile touched
                val s = grid[x][y]
                if (s.first == -1) {
                    if (s.second in 0..9)
                        touchGrid(x, y)
                    else
                        return false
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas !is Canvas) {return} // Smartcast

        paint.color = Color.BLACK
        paint.textSize = height.toFloat() / 28
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        // Set background to black.

        var x = 0 // X position on grid.
        var y : Int // Y position on grid.


        for (a in grid) {
            y = 0
            for (b in a) {
                if (b.first == -1) { // If there isn't a tile piece here...
                    if (b.second == -1)  // If you can't place selected tile here
                        paint.color = Color.DKGRAY  // Paint the tile Dark Gray.
                    else if (b.second in 0..9) // If you can place selected tile here
                        paint.color = Color.LTGRAY // Paint the tile Light Gray.
                    else if (b.second == 10) // If the tile is selected for primary placement
                        paint.color = Color.RED // Paint the tile Red.
                    // Draw Tile
                    canvas.drawRect(width.toFloat() / 23 * x + 2, height.toFloat() / 23 * y + 2,
                            width.toFloat() / 23 * (x+1) - 2, height.toFloat() / 23 * (y+1) - 2, paint)
                }
                else  {
                    // Displays each piece of the tiles on the board.
                    paint.color = Color.WHITE
                    when (b.second) {
                        0 -> canvas.drawRect(width.toFloat() / 23 * x + 8, height.toFloat() / 23 * y + 0,
                                width.toFloat() / 23 * (x+1) - 8, height.toFloat() / 23 * (y+1) - 0, paint)
                        1 -> canvas.drawRect(width.toFloat() / 23 * x + 0, height.toFloat() / 23 * y + 8,
                                width.toFloat() / 23 * (x+1) - 0, height.toFloat() / 23 * (y+1) - 8, paint)
                        2 -> canvas.drawRect(width.toFloat() / 23 * x + 8, height.toFloat() / 23 * y + 8,
                                width.toFloat() / 23 * (x+1) - 0, height.toFloat() / 23 * (y+1) - 8, paint)
                        3 -> canvas.drawRect(width.toFloat() / 23 * x + 8, height.toFloat() / 23 * y + 0,
                                width.toFloat() / 23 * (x+1) - 8, height.toFloat() / 23 * (y+1) - 8, paint)
                        4 -> canvas.drawRect(width.toFloat() / 23 * x + 0, height.toFloat() / 23 * y + 8,
                                width.toFloat() / 23 * (x+1) - 8, height.toFloat() / 23 * (y+1) - 8, paint)
                        5 -> canvas.drawRect(width.toFloat() / 23 * x + 8, height.toFloat() / 23 * y + 8,
                                width.toFloat() / 23 * (x+1) - 8, height.toFloat() / 23 * (y+1) - 0, paint)
                    }
                    // Displays the number on the tile.
                    if (b.first != 10) {
                        paint.color = Color.BLACK
                        canvas.drawText(b.first.toString(), width.toFloat() / 23 * x + width.toFloat() / 75,
                                height.toFloat() / 23 * y + height.toFloat() / 29, paint)
                    }
                    // Displays the divider between numbers when horizontal.
                    else if (b.second == 1){
                        paint.color = Color.BLACK
                        canvas.drawText("|", width.toFloat() / 23 * x + width.toFloat() / 75,
                                height.toFloat() / 23 * y + height.toFloat() / 29, paint)
                    }
                    // Displays the divider between numbers when vertical.
                    else{
                        paint.color = Color.BLACK
                        canvas.drawText("--", width.toFloat() / 23 * x + width.toFloat() / 75,
                                height.toFloat() / 23 * y + height.toFloat() / 29, paint)
                    }
                }
                y += 1
            }
            x += 1
        }

    }
}