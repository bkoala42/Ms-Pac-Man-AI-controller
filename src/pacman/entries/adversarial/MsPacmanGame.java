package pacman.entries.adversarial;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.PacMan;

public class MsPacmanGame implements SearchGame<Integer[], MOVE, Integer>{
	
	private Game game;
	private MsPacManAlphaBetaStrategy strategy;
	private Random rand;
	private MOVE[] allMoves;
	private int[] w;
	private int[] mult;
	
	public MsPacmanGame(int[] w, int[] mult) {
		this.strategy = new MsPacManAlphaBetaStrategy();
		this.w = w;
		this.mult = mult;
		this.rand = new Random();
		this.allMoves = MOVE.values();
	}
	
	public void setBasicGame(Game game) {
		this.game = game;
	}

	@Override
	// The initial state is given by the positions of pacman and ghosts
	public Integer[] getInitialState() {
		Integer[] initialState = new Integer[5];
		initialState[0] = game.getPacmanCurrentNodeIndex();
		initialState[1] = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
		initialState[2] = game.getGhostCurrentNodeIndex(GHOST.PINKY);
		initialState[3] = game.getGhostCurrentNodeIndex(GHOST.INKY);
		initialState[4] = game.getGhostCurrentNodeIndex(GHOST.SUE);
		return initialState;
//		return game.getPacmanCurrentNodeIndex();
	}

	@Override
	public Integer[] getPlayers() {
		return null;
	}

	@Override
	// MsPacman is differentiated from ghosts because is null
	public Integer getPlayer(Integer[] state) {
		return 1;
	}

	@Override
	public List<MOVE> getActions(Integer[] state) {
		// allow reversal move
		MOVE[] moves = game.getPossibleMoves(state[0]);
		return Arrays.asList(moves);
	}

	@Override
	public Integer[] getResult(Integer[] state, MOVE action) {
		Integer[] result = new Integer[5];
		result[0] = game.getNeighbour(state[0], action);							// This is MsPacman
		for(int i = 1; i < 5; i++) {
			if(state[i] != -1)
//				result[i] = game.getNeighbour(state[i], allMoves[rand.nextInt(4)]);
				result[i] = game.getNeighbour(state[i], 
						game.getNextMoveTowardsTarget(state[i], state[0], DM.PATH));
			else
				result[i] = state[i];
		}
//		result[1] = game.getNeighbour(state[1], allMoves[rand.nextInt(4)]);		
//		result[2] = game.getNeighbour(state[2], allMoves[rand.nextInt(4)]);
//		result[3] = game.getNeighbour(state[3], allMoves[rand.nextInt(4)]);
//		result[4] = game.getNeighbour(state[4], allMoves[rand.nextInt(4)]);
		return result;
//		return game.getNeighbour(state, action);
	}

	@Override
	public boolean isTerminal(Integer[] state) {
		return game.gameOver();
	}

	@Override
	public double getUtility(Integer[] state, Integer player) {
		double utility = 0;
		if(game.wasPacManEaten()) {
			utility = Double.NEGATIVE_INFINITY;
		}
		else {
			utility = weightedUtilityFunction(state);
		}
		return utility;
	}
	
	private double weightedUtilityFunction(Integer[] state) {
		double totalUtility;
		Integer[] ghostState = {state[1], state[2], state[3], state[4]};
		
		double score = game.getScore();

//		double closestPillDistance = strategy.getClosestPillDistance(game, game.getPacmanCurrentNodeIndex(), false);
		// when no pills are available getClosestPillDistance returns zero, so minimize the value of this term
//		if(closestPillDistance < 0.000001) {
//			closestPillDistance = Integer.MAX_VALUE;
//		}
//
//		double closestPowerPillDistance = strategy.getClosestPillDistance(game, game.getPacmanCurrentNodeIndex(), true);
//		// when no power pills are available getClosestPillDistance returns zero, so minimize the value of this term
//		if(closestPowerPillDistance < 0.000001) {
//			closestPowerPillDistance = Integer.MAX_VALUE;
//		}
//
		double closestNonEdibleGhostDistance = strategy.getClosestGhostDistance(game, game.getPacmanCurrentNodeIndex(), false, ghostState);
		double closestEdibleGhostDistance = strategy.getClosestGhostDistance(game, game.getPacmanCurrentNodeIndex(), true, ghostState);
		// avoid zero division
//		if(closestEdibleGhostDistance < 0.000001) {
//			closestEdibleGhostDistance = 0.001;
//		}
//
//		if(strategy.getMinEdibleGhost() != null && strategy.checkSafeChase(game.getPacmanCurrentNodeIndex(), game)) {
//			closestEdibleGhostDistance = mult[0]*closestEdibleGhostDistance;
//		}
//		if(strategy.isMsPacManChased(game.getPacmanCurrentNodeIndex(), game) > 2) {
//			closestPowerPillDistance = mult[1]*closestPowerPillDistance;
//		}
//		totalUtility = w[0]*(1/closestPillDistance) + w[1]*(1/closestPowerPillDistance) + w[2]*closestNonEdibleGhostDistance + 
//				w[3]*(1/closestEdibleGhostDistance);
		//			System.out.println("Utilita: "+totalUtility);
		//			System.out.println(w[0]*closestPillDistance+" "+w[1]*closestPowerPillDistance+" "+w[2]*closestNonEdibleGhostDistance+" "+
		//					w[3]*closestEdibleGhostDistance);
		totalUtility = score - closestEdibleGhostDistance + closestNonEdibleGhostDistance;
		return totalUtility;
	}

	
}
