package com.example.owner.dominos

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Modes.check(Runout.id) // Default gamemode: Runout
        PieceNum.check(Six.id) // Default pieces: 0-6
        NumAI.check(ZeroAI.id) // Default AI number: Zero
        val saveName = intent.getStringExtra("SaveName")
        if (intent.getBooleanExtra("Load", false)) {
            val f = File(filesDir.path + "/" + saveName + ".txt") // File being loaded
            for (line in f.readLines()) {
                val parts = line.split(" ")
                when {
                    parts[0] == "Mode" -> { // Piece ID = Mode, What gamemode is being played.
                        if (parts[1] == "Runout")
                            Modes.check(Runout.id)
                        else if(parts[1] == "Points")
                            Modes.check(Points.id)
                        else if (parts[1] == "Matador")
                            Modes.check(Matador.id)
                    }
                    parts[0] == "NumPiece" -> { // Piece ID = NumPiece, Either 0-6 or 0-9 dominos.
                        if (parts[1] == "6")
                            PieceNum.check(Six.id)
                        else if(parts[1] == "9")
                            PieceNum.check(Nine.id)
                    }
                    parts[0] == "AI" -> { // Piece ID = AI, How many AI players there are.
                        if (parts[1] == "0")
                            NumAI.check(ZeroAI.id)
                        else if (parts[1] == "1")
                            NumAI.check(OneAI.id)
                        else if (parts[1] == "2")
                            NumAI.check(TwoAI.id)
                        else if (parts[1] == "3")
                            NumAI.check(ThreeAI.id)
                    }
                }
            }
        }

        Modes.setOnCheckedChangeListener {radioGroup, i ->
            if (i == Matador.id) // If you selected Matador gamemode,
                PieceNum.check(Six.id) // Number of pieces is 6, Matador can't be played with 0-9 pieces.
            radioGroup.check(i)
            save(saveName)
        }

        PieceNum.setOnCheckedChangeListener {radioGroup, i ->
            if (i == Nine.id){ // If you selected 0-9 pieces
                if (Modes.checkedRadioButtonId == Matador.id) // If matador gamemode is selected
                    Modes.check(Runout.id) // Select Runout gamemode, Matador cannot be played with 0-9 pieces.
            }
            radioGroup.check(i)
            save(saveName)
        }

        NumAI.setOnCheckedChangeListener {radioGroup, i ->
            radioGroup.check(i)
            save(saveName)
        }

        Begin.setOnClickListener { // Begin game with selected settings.
            save(saveName, true)
            val i = Intent(this, GameActivity::class.java)
            i.putExtra("SaveName", saveName.toString())
            startActivity(i)
        }

        ExitSet.setOnClickListener { // Return to Loading screen, saving settings.
            save(saveName)
            val i = Intent(this, LoadActivity::class.java)
            startActivity(i)
        }
    }

    fun save(fileName : String, done : Boolean = false) {
        val file = File(filesDir.path + "/" + fileName + ".txt") // Open file to write.
        file.writeText("Name " + fileName + '\n') // Write save name first
        when (Modes.checkedRadioButtonId) {// Write game mode
            Runout.id -> {file.appendText("Mode Runout" + '\n') }
            Points.id -> {file.appendText("Mode Points" + '\n') }
            Matador.id -> {file.appendText("Mode Matador" + '\n') }
        }
        if (done)
            file.appendText("State in progress." + '\n') // Write that game is in progress
        else
            file.appendText("State setting up." + '\n')
        when (NumAI.checkedRadioButtonId) { // Write how many AI players there are
            ZeroAI.id -> {file.appendText("AI 0" + '\n')}
            OneAI.id -> {file.appendText("AI 1" + '\n')}
            TwoAI.id -> {file.appendText("AI 2" + '\n')}
            ThreeAI.id -> {file.appendText("AI 3" + '\n')}
        }
        when (PieceNum.checkedRadioButtonId) {
            Six.id -> {file.appendText("NumPiece 6" + '\n') }
            Nine.id -> {file.appendText("NumPiece 9" + '\n') }
        }
    }
}
