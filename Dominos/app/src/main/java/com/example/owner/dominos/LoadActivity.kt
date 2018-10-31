package com.example.owner.dominos

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_load.*
import java.io.File

class LoadActivity : AppCompatActivity() {

    var del = false // Whether or not you are currently deleting a game.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        var dir = true                      // First directory is skipped as it is not a file.
        var fileNum = 0                     // Number of file to create if making a new one.
        val games = ArrayList<String>()     // Array of games that have been loaded, in string form to be shown.
        val fileNames = ArrayList<String>() // Array of file names that could open when tapped.
        if(!filesDir.exists()) {
            filesDir.mkdir()
        }
        else {
            filesDir.walkTopDown().forEach {
                if(dir) {
                    dir = false
                }
                else {
                    var tempString = ""
                    for (line in it.readLines()) {
                        val parts = line.split(" ")
                        if (parts[0] == "Name") { // Piece ID = Name, Place save name in fileNames
                            if (fileNum <= parts[1].toInt())
                                fileNum = parts[1].toInt() + 1
                            fileNames.add(parts[1])
                        }
                        else if (parts[0] == "Mode") { // Piece ID = Mode, What gamemode is being played.
                            tempString += "Game Mode: " + parts[1] + '\n'
                        }
                        else if (parts[0] == "State") { // Piece ID = State, What state loaded game is in.
                            tempString += "Game " + parts[1] + " " + parts[2] + '\n'
                        }
                        else if (parts[0] == "Turn") { // Piece ID = Turn, Who's turn it is when loaded.
                            tempString += "It is Player " + parts[1] + "'s turn." + '\n'
                        }
                        else if (parts[0] == "Win") { // Piece ID = Win, Who won if the game is over.
                            for (i in parts) {
                                if (i == parts[0]) {
                                    tempString += "Winners: "
                                }
                                else if (i == parts[1]){
                                    tempString += "Player " + i
                                }
                                else {
                                    tempString += ", Player " + i
                                }
                            }
                            tempString += '\n'
                        }
                        else if (parts[0] == "AI") { // Piece ID = AI, How many AI players there are.
                            tempString += parts[1] + " AI players." + '\n'
                        }
                        else if (parts[0] == "Points") { // Piece ID = Points, How many points each player has.
                            tempString += "Player 1 : " + parts[1] + ", Player 2 : " + parts[2] + '\n'
                            tempString += "Player 3 : " + parts[3] + ", Player 4 : " + parts[4] + '\n'
                        }
                    }
                    games.add(tempString) // Loaded game added to list display.
                }
            }
        }
        val adapt = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, games)
        loadGame.adapter = adapt

        NewGame.setOnClickListener { // Create a new game
            val i = Intent(this, SettingsActivity::class.java)
            i.putExtra("SaveName", fileNum.toString())
            startActivity(i)
        }

        DeleteGame.setOnClickListener { // Start or stop deleting a game
            if(del) { // If delete is selected, deselect it.
                del = false
                DeleteGame.text = "Delete A Game"
            }
            else { // If delete is not selected, select it.
                del = true
                DeleteGame.text = "Deleting"
            }
        }

        loadGame.setOnItemClickListener { adapterView, view, i, l -> // Load a saved game
            if (del) { // If delete is selected, delete tapped game and reload activity.
                val f = File(filesDir.path + "/" + fileNames[i] + ".txt")
                f.delete()
                val inte = Intent(this, LoadActivity::class.java)
                startActivity(inte)
            }
            else { // If delete is not selected, start tapped game.
                val savename = fileNames[i]
                val inte = Intent(this, GameActivity::class.java)
                inte.putExtra("SaveName", savename)
                startActivity(inte)
            }
        }
    }
}
