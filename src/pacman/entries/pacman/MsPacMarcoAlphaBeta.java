package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.entries.adversarial.MsPacmanGame;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacMarcoAlphaBeta extends Controller<MOVE> {
	
	private MsPacmanGame alphaBetaGame;
	private MsPacmanAlfaBeta alphaBetaPlayer;
	private boolean gameSet = false;
	
	public MsPacMarcoAlphaBeta(double utilMin, double utilMax, int time, int[] w, int[] mult) {
		this.alphaBetaGame = new MsPacmanGame(w, mult);
		this.alphaBetaPlayer = new MsPacmanAlfaBeta(alphaBetaGame, utilMin, utilMax, time);
		this.alphaBetaPlayer.setLogEnabled(true);
	}

	@Override
	public MOVE getMove(Game game, long timeDue) {
		if(!gameSet)
			alphaBetaGame.setBasicGame(game);
		else
			gameSet = true;
		MOVE newMove = alphaBetaPlayer.makeDecision(alphaBetaGame.getInitialState());
//		System.out.println(newMove.toString());
		return newMove;
	}

}
