package com.example.tictactoe

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var viewModel : MainViewModel
    private lateinit var binding: ActivityMainBinding
    var gameState = GameState.Waiting
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.oButton.isChecked = false
        binding.xButton.isChecked = true
        viewModel.Human = Player.PLAYER_X.mark
        viewModel.minMaxer = Player.PLAYER_O.mark

        binding.oButton.setOnClickListener {
            viewModel.Human = Player.PLAYER_O.mark
            viewModel.minMaxer = Player.PLAYER_X.mark
            binding.xButton.isChecked = false
        }
        binding.xButton.setOnClickListener {
            viewModel.Human = Player.PLAYER_X.mark
            viewModel.minMaxer = Player.PLAYER_O.mark
            binding.oButton.isChecked = false
        }

        binding.startGame.setOnClickListener {

            when (gameState) {
                GameState.Waiting -> {
                }
                GameState.UnderWay, GameState.ShowingDialog -> {
                    resetBoard()
                    cleanCells(binding.TTTBoard)
                    gameState = GameState.Waiting
                    binding.xButton.isClickable = true
                    binding.oButton.isClickable = true
                    binding.textView.text = getString(R.string.star_game_or_select_a_player)
                    binding.startGame.text = "Start Game"
                }
            }
        }
        viewModel.nextMove.observe(this, displayAiMoveObserver())
    }

    private fun resetBoard() {
        for (i in 0..2)
            for (j in 0..2) {
                viewModel.TTCboard[i][j] = '_'
            }
    }

    private fun cleanCells(view: TableLayout){
        var row: TableRow
        var cell: TextView
        for (i in 0.. view.childCount-1){
            row = view.getChildAt(i) as TableRow
            for (j in 0..row.childCount-1) {
                cell = row.getChildAt(j) as TextView
                cell.text = ""
                cell.isEnabled = true
                cell.isClickable = true
            }
        }
    }

    fun markMyMove(view: View) {
        if(gameState != GameState.ShowingDialog) {
            if (gameState == GameState.Waiting ) {
                gameState = GameState.UnderWay
                binding.oButton.isClickable = false
                binding.xButton.isClickable = false
            }

            var move: Move
            view.isClickable = false
            (view as TextView).text = viewModel.Human.toString()
            when (view.id) {
                R.id.cell00 -> {
                    viewModel.TTCboard[0][0] = viewModel.Human
                }
                R.id.cell01 -> {
                    viewModel.TTCboard[0][1] = viewModel.Human
                }
                R.id.cell02 -> {
                    viewModel.TTCboard[0][2] = viewModel.Human
                }

                R.id.cell10 -> {
                    viewModel.TTCboard[1][0] = viewModel.Human
                }
                R.id.cell11 -> {
                    viewModel.TTCboard[1][1] = viewModel.Human
                }
                R.id.cell12 -> {
                    viewModel.TTCboard[1][2] = viewModel.Human
                }

                R.id.cell20 -> {
                    viewModel.TTCboard[2][0] = viewModel.Human
                }
                R.id.cell21 -> {
                    viewModel.TTCboard[2][1] = viewModel.Human
                }
                R.id.cell22 -> {
                    viewModel.TTCboard[2][2] = viewModel.Human
                }
                else -> {
                    Toast.makeText(this, "unexpected error", Toast.LENGTH_LONG).show()
                }

            }

            val result = viewModel.evaluate(viewModel.TTCboard)
            binding.textView.text = "Game Over"
            if (result == 10) {
                gameState = GameState.ShowingDialog
                alertDialog("AI Win")?.show()

            } else if (result == -10) {
                gameState = GameState.ShowingDialog
                alertDialog("I Win")?.show()
            } else if (result == 0) {
                if (viewModel.isMovesLeft(viewModel.TTCboard)) {
                    viewModel.findBestMove(viewModel.TTCboard)
                    binding.textView.text = "${viewModel.minMaxer.toString()} turn"
                } else {
                    gameState = GameState.ShowingDialog
                    alertDialog("The game is tied")?.show()
                }
            }

        }
    }

    fun displayAiMoveObserver() = Observer<Move>{
        viewModel.TTCboard[it.row][it.col] = viewModel.minMaxer
        val row = this.binding.TTTBoard[it.row] as TableRow
        val cell = row.getVirtualChildAt(it.col) as TextView
        cell.text =viewModel.minMaxer.toString()
        cell.isEnabled = false
        val result = viewModel.evaluate(viewModel.TTCboard)
        binding.textView.text = "Game Over"
        if(result == 10)
        {
            alertDialog("Ai win AI")?.show()
            gameState = GameState.ShowingDialog
        }
        else if (result == -10){
            alertDialog("I win AI")?.show()
        }
        else if (result == 0) {
            if(viewModel.isMovesLeft(viewModel.TTCboard)) {
                binding.textView.text = "${viewModel.Human.toString()} Turn"
            }
            else {
                gameState = GameState.ShowingDialog
                alertDialog("this game is tied AI")?.show()
            }
        }
    }

    private fun markAIMove(it: Move) {
        val row = this.binding.TTTBoard[it.row] as TableRow
        val cell = row.getVirtualChildAt(it.col) as TextView
        cell.text =viewModel.minMaxer.toString()
        cell.isEnabled = false

    }
    fun alertDialog(text: String): AlertDialog? = this.let {
        val builder = AlertDialog.Builder(it)
        builder.apply {
            setPositiveButton(getString(R.string.button_ok),
                DialogInterface.OnClickListener { dialog, id ->
                    // User clicked OK button
                    binding.textView.text = getString(R.string.star_game_or_select_a_player)
                    binding.xButton.isClickable = true
                    binding.oButton.isClickable = true
                    cleanCells(binding.TTTBoard)
                    resetBoard()
                    gameState = GameState.Waiting
                })
        }
            .setCancelable(false)
            .setMessage(text)
        builder.create()
    }
}