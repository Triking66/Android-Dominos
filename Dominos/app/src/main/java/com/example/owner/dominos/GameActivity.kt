package com.example.owner.dominos

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_game.*
import java.io.File
import java.util.Random

class GameActivity : AppCompatActivity() {
    val rand = Random()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val dm: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        if (dm.heightPixels > dm.widthPixels) {
            Domino_Screen.layoutParams.width = (dm.heightPixels * 0.58).toInt()
            Domino_Screen.layoutParams.height = (dm.heightPixels * 0.58).toInt()
        }
        else {
            Domino_Screen.layoutParams.width = (dm.widthPixels * 0.49).toInt()
            Domino_Screen.layoutParams.height = (dm.widthPixels * 0.49).toInt()
            Pieces.layoutParams.width = (dm.widthPixels * 0.5).toInt()
        }
        val saveName = intent.getStringExtra("SaveName") // Name of save without path or extension.
        var gamemode = "Runout" // Game mode being played
        var load = false // Whether you loaded a saved game or not.
        var total = 6 // Number of Pieces 0-6 or 0-9
        var pass = 0 // Number of times the turn has been passed in a row.
        var ai = 0 // How many AI players there are.
        val given : Int // How many pieces each player is given at the beginning of the game.
        var turn = 0 // Which player's turn it is, by index.
        val playerTiles = Array<ArrayList<Pair<Int, Int>>>(4, {i-> ArrayList()}) // Array of tiles in each players' possession.
        val points = Array<Int>(4, {i->0}) // Array of points each player has, by index.
        var unclaimedTiles : ArrayList<Pair<Int, Int>> = arrayListOf()
        var index: Int // Index of tile used in many places.
        TotalText.visibility = View.INVISIBLE // Total text made invisible unless Points game.

        val f = File(filesDir.path + "/" + saveName + ".txt") // File being loaded
        for (line in f.readLines()) {
            val parts = line.split(" ")
            when {
                parts[0] == "Mode" -> // Piece ID = Mode, What gamemode is being played.
                    gamemode = parts[1]
                parts[0] == "State" -> // Piece ID = State, What state loaded game is in.
                    if (parts[2] == "up.") {
                        val i = Intent(this, SettingsActivity::class.java)
                        i.putExtra("SaveName", saveName)
                        i.putExtra("Load", true)
                        startActivity(i)
                    }
                parts[0] == "Turn" -> // Piece ID = Turn, Who's turn it is when loaded.
                    turn = parts[1].toInt() - 1
                parts[0] == "Win" -> // Piece ID = Win, Who won if the game is over.
                    for (i in parts) {
                        if (i != parts[0]) {
                            Pieces.win.add(i.toInt())
                        }
                    }
                parts[0] == "AI" -> // Piece ID = AI, How many AI players there are.
                    ai = parts[1].toInt()
                parts[0] == "Points" -> { // Piece ID = Points, How many points each player has.
                    points[0] = parts[1].toInt()
                    points[1] = parts[2].toInt()
                    points[2] = parts[3].toInt()
                    points[3] = parts[4].toInt()
                    Domino_Screen.total = parts[5].toInt()
                }
                parts[0] == "Board" -> { // Piece ID = Board, One column of the board to be loaded.
                    val x = parts[1].toInt()
                    var a = 2
                    while(a < parts.size) {
                        Domino_Screen.grid[x][(a-2)/2] = Pair(parts[a].toInt(), parts[a+1].toInt())
                        a += 2
                    }
                }
                parts[0] == "Player" -> { // Piece ID = Player, One player's pieces to be loaded
                    val x = parts[1].toInt()
                    var a = 2
                    while(a < parts.size) {
                        playerTiles[x].add(Pair(parts[a].toInt(), parts[a+1].toInt()))
                        a += 2
                    }
                }
                parts[0] == "Unused" -> { // Piece ID = Unused, Unclaimed tiles to be loaded.
                    var a = 2
                    load = true
                    while (a < parts.size) {
                        unclaimedTiles.add(Pair(parts[a].toInt(), parts[a+1].toInt()))
                        a += 2
                    }
                }
                parts[0] == "NumPiece" -> total = parts[1].toInt()
            }
        }

        if (load){ // If you loaded a saved game
            Domino_Screen.state = 2 // Can't tap on domino screen
            Pieces.active = true // Can tap on Pieces screen
            Pieces.turnChange = turn+1 // Turn is changing to the person who's turn it is.
            if (gamemode == "Points")
                TotalText.visibility = View.VISIBLE
            Pieces.dominos = playerTiles[turn]
            Pieces.setUp()
            Pieces.invalidate()
        }

        else { // Set up default game.
            if (gamemode == "Matador") {
                given = 5
                total = 6
            }
            else if (gamemode == "Points") {
                given = 7
                TotalText.visibility = View.VISIBLE
            }
            else if (total == 9) {
                given = 13
            }
            else {
                given = 7
            }
            // All possible pieces, removed when taken by a player.
            if (total == 9) {
                unclaimedTiles = arrayListOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4), Pair(0, 5), Pair(0, 6),
                        Pair(1, 1), Pair(1, 2), Pair(1, 3), Pair(1, 4), Pair(1, 5), Pair(1, 6), Pair(2, 2), Pair(2, 3), Pair(2, 4), Pair(2, 5), Pair(2, 6),
                        Pair(3, 3), Pair(3, 4), Pair(3, 5), Pair(3, 6), Pair(4, 4), Pair(4, 5), Pair(4, 6), Pair(5, 5), Pair(5, 6), Pair(6, 6),
                        Pair(0, 7), Pair(0, 8), Pair(0, 9), Pair(1, 7), Pair(1, 8), Pair(1, 9), Pair(2, 7), Pair(2, 8), Pair(2, 9), Pair(3, 7), Pair(3, 8), Pair(3, 9),
                        Pair(4, 7), Pair(4, 8), Pair(4, 9), Pair(5, 7), Pair(5, 8), Pair(5, 9), Pair(6, 7), Pair(6, 8), Pair(6, 9), Pair(7, 7), Pair(7, 8), Pair(7, 9),
                        Pair(8, 8), Pair(8, 9), Pair(9, 9))
            } else {
                unclaimedTiles = arrayListOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4), Pair(0, 5), Pair(0, 6),
                        Pair(1, 1), Pair(1, 2), Pair(1, 3), Pair(1, 4), Pair(1, 5), Pair(1, 6), Pair(2, 2), Pair(2, 3), Pair(2, 4), Pair(2, 5), Pair(2, 6),
                        Pair(3, 3), Pair(3, 4), Pair(3, 5), Pair(3, 6), Pair(4, 4), Pair(4, 5), Pair(4, 6), Pair(5, 5), Pair(5, 6), Pair(6, 6))
            }
            var curBest = Pair(-1, 0) // Best piece given to a player, used to determine turn order.
            for (i in 1..given) {
                for (j in 0..3) {
                    if (unclaimedTiles.size > 1)
                        index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                    else
                        index = 0 // Select last remaining piece
                    playerTiles[j].add(unclaimedTiles[index]) // Add piece to hand
                    if (curBest.first == curBest.second) { // If best piece is a double
                        if (unclaimedTiles[index].first == unclaimedTiles[index].second) { // Selected piece must be a double to be better
                            if (unclaimedTiles[index].first > curBest.first) { // Selected piece must have a higher value to be better.
                                turn = j
                                curBest = Pair(unclaimedTiles[index].first, unclaimedTiles[index].second)
                            }
                        }
                    } else { // If best piece is not a double
                        if (unclaimedTiles[index].first == unclaimedTiles[index].second) { // If selected piece is a double, it is the best piece.
                            turn = j
                            curBest = Pair(unclaimedTiles[index].first, unclaimedTiles[index].second)
                        } else { // If selected piece is not a double, it must have a higher total on the domino to be better.
                            if (unclaimedTiles[index].first + unclaimedTiles[index].second > curBest.first + curBest.second) {
                                turn = j
                                curBest = Pair(unclaimedTiles[index].first, unclaimedTiles[index].second)
                            }
                        }
                    }
                    unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                }
            }
            // Set text to show who's turn it is, with correct amount of points.
            if (gamemode == "Runout" || gamemode == "Matador") {
                var i = 0
                while (i < playerTiles.size) {
                    points[i] = playerTiles[i].size
                    i++
                }
            }
            // Set up text to show points and current player.
            Playing.text = "Player ${(turn + 1)} : ${points[turn]}"
            Unplaying1.text = "Player ${((turn + 1)) % 4 + 1} : ${points[(turn + 1) % 4]}"
            Unplaying2.text = "Player ${((turn + 2) % 4 + 1)} : ${points[(turn + 2) % 4]}"
            Unplaying3.text = "Player ${((turn + 3) % 4 + 1)} : ${points[(turn + 3) % 4]}"
            TotalText.text = "Total: ${Domino_Screen.total}"
            if (gamemode == "Matador")
                Domino_Screen.matTappable()
            else
                Domino_Screen.setTappable() // Reset to show new tappable area.
            Domino_Screen.gameMode = gamemode // Sync gamemode of Domino Screen
            Pieces.setUp() // Set up pieces default settings.
            Pieces.changeset(playerTiles[turn]) // Set current player's tiles to be visible in the pieces section.
            Pieces.setSelectedPiece(curBest)
            Domino_Screen.selectedPiece = Pieces.selected

            // If the first turn goes to an AI, let them take their turn.
            while (4 - turn <= ai) {
                if (gamemode == "Runout") {
                    if (!runoutAi(playerTiles[turn])) { // If the ai was unable to place a piece.
                        pass += 1 // They passed their turn.
                    }
                } else if (gamemode == "Points") {
                    if (!pointsAi(playerTiles[turn])) { // If the ai was unable to place a piece
                        pass += 1 // They passed their turn.
                    }
                } else if (gamemode == "Matador") {
                    if (!matadorAi(playerTiles[turn])) {
                        pass += 1
                        // If there are tiles left unclaimed, give current player a penalty.
                        if (unclaimedTiles.size > 0) {
                            if (unclaimedTiles.size > 1)
                                index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                            else
                                index = 0 // Select last remaining piece
                            playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                            unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                            points[turn] = playerTiles[turn].size
                        }
                    }
                }
                Pieces.active = true
                // Fix Points and check for win, otherwise advance turn.
                if (gamemode == "Runout" || gamemode == "Matador") {
                    points[turn] = playerTiles[turn].size
                } else if (gamemode == "Points" && pass == 0) {
                    if (Domino_Screen.total % 5 == 0) // If total is divisible by 5, a point is awarded.
                        points[turn] += 1
                    if (Domino_Screen.total % 3 == 0) // If total is divisible by 3, a point is awarded.
                        points[turn] += 1
                    // If there are tiles left unclaimed, give current player a replacement.
                    if (unclaimedTiles.size > 0) {
                        if (unclaimedTiles.size > 1)
                            index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                        else
                            index = 0 // Select last remaining piece
                        playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                        unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                    }
                }
                if ((gamemode == "Runout" || gamemode == "Matador") && points[turn] == 0) {
                    var winners = ArrayList<Int>()
                    var min = 15
                    var i = 0
                    while (i < points.size) {
                        if (points[i] < min) {
                            winners = arrayListOf(i + 1)
                            min = points[i]
                        } else if (points[i] == min) {
                            winners.add(i + 1)
                        }
                        i++
                    }
                    Pieces.win = winners
                    Pieces.active = false
                    turn = -1 // End Ai turn so things become visible.
                } else {
                    turn += 1 // Advance the turn
                    if (turn >= 4) // Turn reset to player 1
                        turn = 0
                    Pieces.dominos = playerTiles[turn] // Set Pieces showing to correct player
                }
            }
        }
        Playing.text = "Player ${(turn + 1)} : ${points[turn]}"
        Unplaying1.text = "Player ${((turn + 1)) % 4 + 1} : ${points[(turn + 1) % 4]}"
        Unplaying2.text = "Player ${((turn + 2) % 4 + 1)} : ${points[(turn + 2) % 4]}"
        Unplaying3.text = "Player ${((turn + 3) % 4 + 1)} : ${points[(turn + 3) % 4]}"
        TotalText.text = "Total: ${Domino_Screen.total}"

        Pieces.setOnTouchListener{ v : View, e : MotionEvent ->
            if (Pieces.onTouchEvent(e))
                if(Pieces.turnChange != 0) { // If the turn is changing
                    Pieces.turnChange = 0 // End the changing of the turn.
                }
                else if(Pieces.selected.first != -1) { // If a piece has been selected.
                    Domino_Screen.selectedPiece = Pieces.selected // Select the piece on the Domino Screen
                    Domino_Screen.state = 0 // Reset the state in case they were about to place a piece
                    if (gamemode == "Matador")
                        Domino_Screen.matTappable(Pieces.selected.first, Pieces.selected.second)
                    else
                        Domino_Screen.setTappable(Pieces.selected.first, Pieces.selected.second) // Show where selected piece can be placed
                }
            true
        }

        Domino_Screen.setOnTouchListener { view, event ->
            if (Domino_Screen.onTouchEvent(event)){ // Returns false if state did not change.
                if (Domino_Screen.state == 2) { // If a piece has been successfully placed
                    pass = 0
                    Pieces.active = true
                    playerTiles[turn].removeAt(Pieces.selectIndex)
                    Pieces.selected = Pair(-1, -1) // Resets selected tile to -1, -1
                    Pieces.selectIndex = -1 // Resets selected index to -1
                    if (gamemode == "Runout" || gamemode == "Matador") {
                        points[turn] = playerTiles[turn].size
                    }
                    else if (gamemode == "Points") {
                        if (Domino_Screen.total % 5 == 0) // If total is divisible by 5, a point is awarded.
                            points[turn] += 1
                        if (Domino_Screen.total % 3 == 0) // If total is divisible by 3, a point is awarded.
                            points[turn] += 1
                        // If there are tiles left unclaimed, give current player a replacement.
                        if (unclaimedTiles.size > 0) {
                            if (unclaimedTiles.size > 1)
                                index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                            else
                                index = 0 // Select last remaining piece
                            playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                            unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                        }
                    }
                    if ((gamemode == "Runout" || gamemode == "Matador") && points[turn] == 0) {
                        var winners = ArrayList<Int>()
                        var min = 15
                        var i = 0
                        while (i < points.size){
                            if (points[i] < min){
                                winners = arrayListOf(i+1)
                                min = points[i]
                            }
                            else if (points[i] == min){
                                winners.add(i+1)
                            }
                            i++
                        }
                        Pieces.win = winners
                        Pieces.active = false
                    }
                    else {
                        turn += 1 // Advance the turn
                        if (turn >= 4) // Turn reset to player 1
                            turn = 0
                        Pieces.dominos = playerTiles[turn] // Set Pieces showing to correct player
                    }
                    // Do Ai turns.
                    while (4-turn <= ai){
                        if (gamemode == "Runout") {
                            if (!runoutAi(playerTiles[turn])) { // If the ai was unable to place a piece.
                                pass += 1 // They passed their turn.
                            }
                        }
                        else if (gamemode == "Points") {
                            if (!pointsAi(playerTiles[turn])) { // If the ai was unable to place a piece
                                pass += 1 // They passed their turn.
                            }
                        }
                        else if (gamemode == "Matador") {
                            if (!matadorAi(playerTiles[turn])) {
                                pass += 1
                                // If there are tiles left unclaimed, give current player a penalty.
                                if (unclaimedTiles.size > 0) {
                                    if (unclaimedTiles.size > 1)
                                        index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                                    else
                                        index = 0 // Select last remaining piece
                                    playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                                    unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                                    points[turn] = playerTiles[turn].size
                                }
                            }
                        }

                        // Fix Points and check for win, otherwise advance turn.
                        if (gamemode == "Runout" || gamemode == "Matador") {
                            points[turn] = playerTiles[turn].size
                        }
                        else if (gamemode == "Points" && pass == 0) {
                            if (Domino_Screen.total % 5 == 0) // If total is divisible by 5, a point is awarded.
                                points[turn] += 1
                            if (Domino_Screen.total % 3 == 0) // If total is divisible by 3, a point is awarded.
                                points[turn] += 1
                            // If there are tiles left unclaimed, give current player a replacement.
                            if (unclaimedTiles.size > 0) {
                                if (unclaimedTiles.size > 1)
                                    index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                                else
                                    index = 0 // Select last remaining piece
                                playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                                unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                            }
                        }
                        if ((gamemode == "Runout" || gamemode == "Matador") && points[turn] == 0) {
                            var winners = ArrayList<Int>()
                            var min = 15
                            var i = 0
                            while (i < points.size){
                                if (points[i] < min){
                                    winners = arrayListOf(i+1)
                                    min = points[i]
                                }
                                else if (points[i] == min){
                                    winners.add(i+1)
                                }
                                i++
                            }
                            Pieces.win = winners
                            Pieces.active = false
                            turn = 0 // End Ai turn so things become visible.
                        }
                        else {
                            turn += 1 // Advance the turn
                            if (turn >= 4) // Turn reset to player 1
                                turn = 0
                            Pieces.dominos = playerTiles[turn] // Set Pieces showing to correct player
                        }
                    }
                    // Reset text to new player order

                    Pieces.turnChange = turn + 1
                    Playing.text = "Player ${(turn + 1)} : ${points[turn]}"
                    Unplaying1.text = "Player ${((turn + 1)) % 4 + 1} : ${points[(turn + 1) % 4]}"
                    Unplaying2.text = "Player ${((turn + 2) % 4 + 1)} : ${points[(turn + 2) % 4]}"
                    Unplaying3.text = "Player ${((turn + 3) % 4 + 1)} : ${points[(turn + 3) % 4]}"
                    TotalText.text = "Total: ${Domino_Screen.total}"
                    Pieces.invalidate()
                    Domino_Screen.invalidate()
                    save(saveName, turn, ai, gamemode, playerTiles, unclaimedTiles, points, Domino_Screen.total)
                }
            }
            true
        }

        Exit.setOnClickListener {
            save(saveName, turn, ai, gamemode, playerTiles, unclaimedTiles, points, Domino_Screen.total)
            val i = Intent(this, LoadActivity::class.java)
            startActivity(i)
        }

        Skip.setOnClickListener {
            if(Pieces.win.size == 0) { // If the game is not over yet (0 winners)
                pass += 1
                if (gamemode == "Matador") {
                    // If there are tiles left unclaimed, give current player a penalty.
                    if (unclaimedTiles.size > 0) {
                        pass = 0
                        if (unclaimedTiles.size > 1)
                            index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                        else
                            index = 0 // Select last remaining piece
                        playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                        unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                        points[turn] = playerTiles[turn].size
                    }
                }

                turn += 1
                if (turn >= 4)
                    turn = 0
                Pieces.selected = Pair(-1, -1)
                Pieces.selectIndex = -1
                Pieces.turnChange = turn
                Pieces.dominos = playerTiles[turn]
                Playing.text = "Player ${(turn + 1)} : ${points[turn]}"
                Unplaying1.text = "Player ${((turn + 1)) % 4 + 1} : ${points[(turn + 1) % 4]}"
                Unplaying2.text = "Player ${((turn + 2) % 4 + 1)} : ${points[(turn + 2) % 4]}"
                Unplaying3.text = "Player ${((turn + 3) % 4 + 1)} : ${points[(turn + 3) % 4]}"
                TotalText.text = "Total: ${Domino_Screen.total}"

                // Do AI turns, then check if game is over.
                while (4 - turn <= ai) {
                    if (gamemode == "Runout") {
                        if (!runoutAi(playerTiles[turn])) { // If the ai was unable to place a piece.
                            pass += 1 // They passed their turn.
                        } else {
                            pass = 0
                        }
                    } else if (gamemode == "Points") {
                        if (!pointsAi(playerTiles[turn])) { // If the ai was unable to place a piece
                            pass += 1 // They passed their turn.
                        } else {
                            pass = 0
                        }
                    } else if (gamemode == "Matador") {
                        if (!matadorAi(playerTiles[turn])) {
                            pass += 1
                            // If there are tiles left unclaimed, give current player a penalty.
                            if (unclaimedTiles.size > 0) {
                                pass = 0
                                if (unclaimedTiles.size > 1)
                                    index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                                else
                                    index = 0 // Select last remaining piece
                                playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                                unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                                points[turn] = playerTiles[turn].size
                            }
                        } else {
                            pass = 0
                        }
                    }

                    // Fix Points and check for win, otherwise advance turn.
                    if (gamemode == "Runout" || gamemode == "Matador") {
                        points[turn] = playerTiles[turn].size
                    } else if (gamemode == "Points" && pass == 0) {
                        if (Domino_Screen.total % 5 == 0) // If total is divisible by 5, a point is awarded.
                            points[turn] += 1
                        if (Domino_Screen.total % 3 == 0) // If total is divisible by 3, a point is awarded.
                            points[turn] += 1
                        // If there are tiles left unclaimed, give current player a replacement.
                        if (unclaimedTiles.size > 0) {
                            if (unclaimedTiles.size > 1)
                                index = rand.nextInt(unclaimedTiles.size) // Randomly select a piece
                            else
                                index = 0 // Select last remaining piece
                            playerTiles[turn].add(unclaimedTiles[index]) // Add piece to hand
                            unclaimedTiles.removeAt(index) // Remove piece from unclaimed.
                        }
                    }
                    if ((gamemode == "Runout" || gamemode == "Matador") && points[turn] == 0) {
                        var winners = ArrayList<Int>()
                        var min = 15
                        var i = 0
                        while (i < points.size) {
                            if (points[i] < min) {
                                winners = arrayListOf(i + 1)
                                min = points[i]
                            } else if (points[i] == min) {
                                winners.add(i + 1)
                            }
                            i++
                        }
                        Pieces.win = winners
                        Pieces.active = false
                        turn = -1 // End Ai turn so things become visible.
                    } else {
                        turn += 1 // Advance the turn
                        if (turn >= 4) // Turn reset to player 1
                            turn = 0
                        Pieces.dominos = playerTiles[turn] // Set Pieces showing to correct player
                    }
                }
                Playing.text = "Player ${(turn + 1)} : ${points[turn]}"
                Unplaying1.text = "Player ${((turn + 1)) % 4 + 1} : ${points[(turn + 1) % 4]}"
                Unplaying2.text = "Player ${((turn + 2) % 4 + 1)} : ${points[(turn + 2) % 4]}"
                Unplaying3.text = "Player ${((turn + 3) % 4 + 1)} : ${points[(turn + 3) % 4]}"
                TotalText.text = "Total: ${Domino_Screen.total}"
                Pieces.invalidate()
                Domino_Screen.invalidate()

                if (pass >= 4) { // Everyone passed their turn in a row.
                    var winners = ArrayList<Int>() // Game over, set up winners.
                    if (gamemode == "Points") { // If playing a Points game,
                        var max = -1 // Set max Score
                        var i = 0 // Iterate through each player, check
                        while (i < points.size) {
                            if (points[i] > max) { // Points higher than max?
                                winners = arrayListOf(i + 1) // They are now the only winner so far and
                                max = points[i] // max is now their points.
                            } else if (points[i] == max) { // Points equal to max?
                                winners.add(i + 1) // They are also a winner now.
                            }
                            i++
                        }
                    } else { // If playing Runout or Matador
                        var min = 15 // Minimum "Score" or how many pieces they have left...
                        var i = 0 // Iterate through each player, check
                        while (i < points.size) {
                            if (points[i] < min) { // Points less than min?
                                winners = arrayListOf(i + 1) // They are now the only winner so far and
                                min = points[i] // Min is now equal to their points.
                            } else if (points[i] == min) { // Points equal to min?
                                winners.add(i + 1) // They are also a winner now.
                            }
                            i++
                        }
                    }
                    Log.e("Win[0]", winners[0].toString())
                    Pieces.win = winners // Send the winners over to Pieces screen.
                    Pieces.active = false // Can no longer tap on the pieces, and they aren't visible.
                }
                save(saveName, turn, ai, gamemode, playerTiles, unclaimedTiles, points, Domino_Screen.total)
                Pieces.invalidate()
                Domino_Screen.invalidate()
            }
        }
    }

    fun runoutAi(dominos : ArrayList<Pair<Int, Int>>) : Boolean {
        if (Domino_Screen.state == -1) { // First turn, place only acceptable piece.
            Domino_Screen.touchGrid(10, 11)
            dominos.removeAt(Pieces.selectIndex)
            return true
        }
        var done = false // True if placed a double.
        var selected = Pair(-1, -1) // Which tile in inventory is selected.
        var pos = Pair(-1, -1) // Position selected tile can be placed at.
        var x = 0 // X Position currently being checked on grid.
        var y : Int // Y Position currently being checked on grid.
        for (p in dominos) {
            Domino_Screen.state = 0
            Domino_Screen.selectedPiece = p // Select piece being checked.
            Domino_Screen.setTappable(p.first, p.second) // Check where on grid selected tile can be placed.
            x = 0
            while (x < Domino_Screen.grid.size && !done) {
                y = 0
                while (y < Domino_Screen.grid[x].size && !done) {
                    if (Domino_Screen.grid[x][y].first == -1 && Domino_Screen.grid[x][y].second != -1) {
                        if (p.first == p.second) { // If selected piece is a double
                            Domino_Screen.touchGrid(x, y) // Place the piece
                            selected = p // Select piece to be removed from inventory
                            done = true // Escape loop and remove tile.
                        }
                        else { // Selected piece is not a double
                            Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                            if (x-2 >= 0 && Domino_Screen.grid[x-2][y].first == -1 && Domino_Screen.grid[x-2][y].second == 0) {
                                selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                            }
                            else if (x+2 < 23 && Domino_Screen.grid[x+2][y].first == -1 && Domino_Screen.grid[x+2][y].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            else if (y-2 >= 0 && Domino_Screen.grid[x][y-2].first == -1 && Domino_Screen.grid[x][y-2].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            else if (y+2 < 23 && Domino_Screen.grid[x][y+2].first == -1 && Domino_Screen.grid[x][y+2].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            Domino_Screen.state = 0
                            Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                        }
                    }
                    y++
                }
                x++
            }
        }
        if (!done) {
            if (selected.first == -1) {
                Domino_Screen.state = 2
                Domino_Screen.setTappable()
                return false // If there is no piece to place, skip turn.
            }
            else {
                Domino_Screen.state = 0
                Domino_Screen.selectedPiece = selected // Select piece we know we can place
                Domino_Screen.setTappable(selected.first, selected.second) // Set where tappable so we can tap there.
                Domino_Screen.touchGrid(pos.first, pos.second) // Begin placement of piece.
                // Find all possible orientations of piece and randomly pick one.
                val possiblePlace = ArrayList<Pair<Int, Int>>()
                if (pos.first-2 >= 0 && Domino_Screen.grid[pos.first-2][pos.second].first == -1 && Domino_Screen.grid[pos.first-2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first-2, pos.second))
                if (pos.first+2 < 23 && Domino_Screen.grid[pos.first+2][pos.second].first == -1 && Domino_Screen.grid[pos.first+2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first+2, pos.second))
                if (pos.second-2 >= 0 && Domino_Screen.grid[pos.first][pos.second-2].first == -1 && Domino_Screen.grid[pos.first][pos.second-2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second-2))
                if (pos.second+2 < 23 && Domino_Screen.grid[pos.first][pos.second+2].first == -1 && Domino_Screen.grid[pos.first][pos.second+2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second+2))
                // Remove placed piece from inventory.
                val i = rand.nextInt(possiblePlace.size)
                Domino_Screen.touchGrid(possiblePlace[i].first, possiblePlace[i].second)
                dominos.remove(selected)
                Domino_Screen.state = 2
                Domino_Screen.setTappable()
                return true
            }
        }
        else {
            // Remove placed double from inventory.
            dominos.remove(selected)
            Domino_Screen.state = 2
            Domino_Screen.setTappable()
            return true
        }
    }


    fun pointsAi(dominos : ArrayList<Pair<Int, Int>>) : Boolean {
        if (Domino_Screen.state == -1) { // First turn, place only acceptable piece.
            Domino_Screen.touchGrid(10, 11)
            dominos.removeAt(Pieces.selectIndex)
            return true
        }
        var doub = false // True if placed a double.
        var fifteen = false // True if found a tile to become total of 15.
        var threeorfive = false // True if found a tile to become total of 5 or 3.
        var selected = Pair(-1, -1) // Which tile in inventory is selected.
        var pos = Pair(-1, -1) // Position selected tile can be placed at.
        var x = 0 // X Position currently being checked on grid.
        var y : Int // Y Position currently being checked on grid.
        for (p in dominos) {
            Domino_Screen.state = 0
            Domino_Screen.selectedPiece = p // Select piece being checked.
            Domino_Screen.setTappable(p.first, p.second) // Check where on grid selected tile can be placed.
            x = 0
            while (x < Domino_Screen.grid.size) {
                y = 0
                while (y < Domino_Screen.grid[x].size) {
                    if (Domino_Screen.grid[x][y].first == -1 && Domino_Screen.grid[x][y].second != -1) {
                        if (p.first == p.second) { // If piece is a double
                            if ((Domino_Screen.total + p.first)%15 == 0) {
                                selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                fifteen = true
                                doub = true
                            } else if (((p.first+Domino_Screen.total)%5 == 0 || (p.first+Domino_Screen.total)%3 == 0) && !fifteen) {
                                selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                threeorfive = true
                                doub = true
                            } else if (!threeorfive) {
                                selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                doub = true
                            }
                        } else if (p.first == Domino_Screen.grid[x][y].second) { // If first number on piece is the one to be placed first
                            if ((p.second - p.first + Domino_Screen.total) % 15 == 0) { // Selected piece makes total of 15
                                Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    fifteen = true
                                    doub = false
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            } else if (((p.second-p.first+Domino_Screen.total)%5 == 0 || (p.second-p.first+Domino_Screen.total)%3 == 0) && !fifteen){ // Selected piece makes total 3 or 5
                                Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    threeorfive = true
                                    doub = false
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                    doub = false
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                    doub = false
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                    doub = false
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            }
                            else if(!threeorfive) {
                                Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    doub = false
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            }
                        } else { // If second number on piece is the one to be placed first
                            if ((p.first - p.second + Domino_Screen.total) % 15 == 0) { // Selected piece makes total of 15
                                Domino_Screen.touchGrid(x, y) // Place the piece
                                selected = p // Select piece to be removed from inventory
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    fifteen = true
                                    doub = false
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    fifteen = true
                                    doub = false
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            } else if (((p.first - p.second + Domino_Screen.total)%5 == 0 || (p.first - p.second + Domino_Screen.total)%3 == 0) && !fifteen){ // Selected piece makes total 3 or 5
                                Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    threeorfive = true
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    threeorfive = true
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            } else if(!threeorfive) {
                                Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                                if (x - 2 >= 0 && Domino_Screen.grid[x - 2][y].first == -1 && Domino_Screen.grid[x - 2][y].second == 0) {
                                    selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                    pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                                    doub = false
                                } else if (x + 2 < 23 && Domino_Screen.grid[x + 2][y].first == -1 && Domino_Screen.grid[x + 2][y].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                } else if (y - 2 >= 0 && Domino_Screen.grid[x][y - 2].first == -1 && Domino_Screen.grid[x][y - 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                } else if (y + 2 < 23 && Domino_Screen.grid[x][y + 2].first == -1 && Domino_Screen.grid[x][y + 2].second == 0) {
                                    selected = p
                                    pos = Pair(x, y)
                                    doub = false
                                }
                                Domino_Screen.state = 0
                                Domino_Screen.setTappable(p.first, p.second) // Reset tappable for more checks.
                            }
                        }
                    }
                    y++
                }
                x++
            }
        }
        if (!doub) {
            if (selected.first == -1) {
                Domino_Screen.state = 2
                Domino_Screen.setTappable()
                return false // If there is no piece to place, skip turn.
            }
            else {
                Domino_Screen.state = 0
                Domino_Screen.selectedPiece = selected // Select piece we know we can place
                Domino_Screen.setTappable(selected.first, selected.second) // Set where tappable so we can tap there.
                Domino_Screen.touchGrid(pos.first, pos.second) // Begin placement of piece.
                // Find all possible orientations of piece and randomly pick one.
                val possiblePlace = ArrayList<Pair<Int, Int>>()
                if (pos.first-2 >= 0 && Domino_Screen.grid[pos.first-2][pos.second].first == -1 && Domino_Screen.grid[pos.first-2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first-2, pos.second))
                if (pos.first+2 < 23 && Domino_Screen.grid[pos.first+2][pos.second].first == -1 && Domino_Screen.grid[pos.first+2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first+2, pos.second))
                if (pos.second-2 >= 0 && Domino_Screen.grid[pos.first][pos.second-2].first == -1 && Domino_Screen.grid[pos.first][pos.second-2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second-2))
                if (pos.second+2 < 23 && Domino_Screen.grid[pos.first][pos.second+2].first == -1 && Domino_Screen.grid[pos.first][pos.second+2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second+2))
                // Remove placed piece from inventory.
                val i = rand.nextInt(possiblePlace.size)
                Domino_Screen.touchGrid(possiblePlace[i].first, possiblePlace[i].second)
                dominos.remove(selected)
                Domino_Screen.state = 2
                Domino_Screen.setTappable()
                return true
            }
        }
        else {
            Domino_Screen.state = 0
            Domino_Screen.selectedPiece = selected // Select piece we know we can place
            Domino_Screen.setTappable(selected.first, selected.second) // Set where tappable so we can tap there.
            Domino_Screen.touchGrid(pos.first, pos.second) // Place piece.
            // Remove placed double from inventory.
            dominos.remove(selected)
            Domino_Screen.state = 2
            Domino_Screen.setTappable()
            return true
        }
    }

    fun matadorAi(dominos : ArrayList<Pair<Int, Int>>) : Boolean {
        if (Domino_Screen.state == -1) { // First turn, place only acceptable piece.
            Domino_Screen.touchGrid(10, 11)
            dominos.removeAt(Pieces.selectIndex)
            return true
        }
        var done = false // True if placed a double.
        var selected = Pair(-1, -1) // Which tile in inventory is selected.
        var pos = Pair(-1, -1) // Position selected tile can be placed at.
        var x = 0 // X Position currently being checked on grid.
        var y : Int // Y Position currently being checked on grid.
        for (p in dominos) {
            Domino_Screen.state = 0
            Domino_Screen.selectedPiece = p // Select piece being checked.
            Domino_Screen.matTappable(p.first, p.second) // Check where on grid selected tile can be placed.
            x = 0
            while (x < Domino_Screen.grid.size && !done) {
                y = 0
                while (y < Domino_Screen.grid[x].size && !done) {
                    if (Domino_Screen.grid[x][y].first == -1 && Domino_Screen.grid[x][y].second != -1) {
                        if (p.first == p.second) { // If selected piece is a double
                            Domino_Screen.touchGrid(x, y) // Place the piece
                            selected = p // Select piece to be removed from inventory
                            done = true // Escape loop and remove tile.
                        }
                        else { // Selected piece is not a double
                            Domino_Screen.touchGrid(x, y) // Tap screen to check if it is possible to place piece in location.
                            if (x-2 >= 0 && Domino_Screen.grid[x-2][y].first == -1 && Domino_Screen.grid[x-2][y].second == 0) {
                                selected = p // Possible to place piece, select for placement and removal in case we don't find a double.
                                pos = Pair(x, y) // Select position to place piece in case we don't find a double.
                            }
                            else if (x+2 < 23 && Domino_Screen.grid[x+2][y].first == -1 && Domino_Screen.grid[x+2][y].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            else if (y-2 >= 0 && Domino_Screen.grid[x][y-2].first == -1 && Domino_Screen.grid[x][y-2].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            else if (y+2 < 23 && Domino_Screen.grid[x][y+2].first == -1 && Domino_Screen.grid[x][y+2].second == 0) {
                                selected = p
                                pos = Pair(x, y)
                            }
                            Domino_Screen.state = 0
                            Domino_Screen.matTappable(p.first, p.second) // Reset tappable for more checks.
                        }
                    }
                    y++
                }
                x++
            }
        }
        if (!done) {
            if (selected.first == -1) {
                Domino_Screen.state = 2
                Domino_Screen.matTappable()
                return false // If there is no piece to place, skip turn.
            }
            else {
                Domino_Screen.state = 0
                Domino_Screen.selectedPiece = selected // Select piece we know we can place
                Domino_Screen.matTappable(selected.first, selected.second) // Set where tappable so we can tap there.
                Domino_Screen.touchGrid(pos.first, pos.second) // Begin placement of piece.
                // Find all possible orientations of piece and randomly pick one.
                val possiblePlace = ArrayList<Pair<Int, Int>>()
                if (pos.first-2 >= 0 && Domino_Screen.grid[pos.first-2][pos.second].first == -1 && Domino_Screen.grid[pos.first-2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first-2, pos.second))
                if (pos.first+2 < 23 && Domino_Screen.grid[pos.first+2][pos.second].first == -1 && Domino_Screen.grid[pos.first+2][pos.second].second == 0)
                    possiblePlace.add(Pair(pos.first+2, pos.second))
                if (pos.second-2 >= 0 && Domino_Screen.grid[pos.first][pos.second-2].first == -1 && Domino_Screen.grid[pos.first][pos.second-2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second-2))
                if (pos.second+2 < 23 && Domino_Screen.grid[pos.first][pos.second+2].first == -1 && Domino_Screen.grid[pos.first][pos.second+2].second == 0)
                    possiblePlace.add(Pair(pos.first, pos.second+2))
                // Remove placed piece from inventory.
                val i = rand.nextInt(possiblePlace.size)
                Domino_Screen.touchGrid(possiblePlace[i].first, possiblePlace[i].second)
                dominos.remove(selected)
                Domino_Screen.state = 2
                Domino_Screen.matTappable()
                return true
            }
        }
        else {
            // Remove placed double from inventory.
            dominos.remove(selected)
            Domino_Screen.state = 2
            Domino_Screen.matTappable()
            return true
        }
    }

    fun save(fileName: String, turn : Int, ai : Int, gamemode: String, playerTiles : Array<ArrayList<Pair<Int, Int>>>,
             unusedTiles : ArrayList<Pair<Int, Int>>, points: Array<Int>, total: Int) {
        val file = File(filesDir.path + "/" + fileName + ".txt") // Open file to write.
        file.writeText("Name " + fileName + '\n') // Write save name first
        file.appendText("Mode " + gamemode + '\n') // Write game mode
        if (Pieces.win.size > 0) {
            file.appendText("State is over." + '\n') // Write that game is over.

            file.appendText("Win") // Write who won the game.
            for (w in Pieces.win)
                file.appendText(" " + (w).toString())
            file.appendText("" + '\n')
        }
        else {
            file.appendText("State in progress." + '\n') // Write that game is in progress
            file.appendText("Turn " + (turn+1).toString() + '\n') // Write who's turn it is.
        }
        file.appendText("AI " + ai.toString() + '\n') // Write how many AI players there are.
        file.appendText("Points " + points[0].toString() + " " + points[1].toString() + " " +
                        points[2].toString() + " " + points[3].toString() + " " + total.toString() + '\n') // Write how many points each player has.

        var x = 0 // X position on grid as well as player index for player tiles.
        var y : Int // Y position on grid as well as domino index for player and unused tiles.

        while (x < Domino_Screen.grid.size) { // Write all spaces on play grid.
            y = 0
            file.appendText("Board " + x.toString())
            while (y < Domino_Screen.grid[x].size) {
                file.appendText(" " +  Domino_Screen.grid[x][y].first.toString() + " " + Domino_Screen.grid[x][y].second.toString())
                y++
            }
            file.appendText("" + '\n')
            x++
        }

        x = 0
        while (x < playerTiles.size){ // Write all tiles in players inventories.
            y = 0
            file.appendText("Player " + x.toString())
            while (y < playerTiles[x].size) {
                file.appendText(" " + playerTiles[x][y].first.toString() + " " + playerTiles[x][y].second.toString())
                y++
            }
            file.appendText("" + '\n')
            x++
        }

        y = 0
        file.appendText("Unused -1")
        while (y < unusedTiles.size) { // Write all tiles in unused tiles.
            file.appendText(" " + unusedTiles[y].first.toString() + " " + unusedTiles[y].second.toString())
            y += 2
        }
        file.appendText("" + '\n')
    }
}
