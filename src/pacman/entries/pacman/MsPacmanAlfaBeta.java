package pacman.entries.pacman;

import pacman.entries.adversarial.SearchGame;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.entries.adversarial.IterativeDeepeningAlphaBetaSearch;
import pacman.entries.adversarial.MsPacmanGame;

public class MsPacmanAlfaBeta extends IterativeDeepeningAlphaBetaSearch<Integer[], MOVE, Integer>{
	
    public MsPacmanAlfaBeta(SearchGame<Integer[], MOVE, Integer> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
	}

	@Override
	protected double eval(Integer[] state, Integer player) {
//		super.eval(state, player);
		return game.getUtility(state, player);
		//return super.eval(state, player);
	}
}
