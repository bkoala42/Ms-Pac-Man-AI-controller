package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Collections;

import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class MyPacMan extends Controller<MOVE>
{
	private MOVE myMove=MOVE.NEUTRAL;
	
	public MOVE getMove(Game game, long timeDue) 
	{
		//Place your game logic here to play the game as Ms Pac-Man
		
		return myMove;
	}
	
	/**
	 * Computes a weighted average of the shortest path distance of MsPacman from ghosts
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @return weighted average distance from ghosts
	 */
	private float weightedGhostsDistance(Game game, int posMsPacman) {
		ArrayList<Float> distances = new ArrayList<Float>(4);
		for(GHOST ghost : GHOST.values()) {
			distances.add((float)game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)));
		}
		float maxDistance = Collections.min(distances);
		float meanDistance = 0;
		for(Float dist : distances) {
			meanDistance += (1-(dist/maxDistance))*dist;
		}
		return meanDistance/4;
	}
	
	/**
	 * Objective function (utility) for "conservative" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility0(float[] distances, int[] hypParam) {
		return Math.pow(distances[0], hypParam[0]) + Math.pow(distances[1], hypParam[1]) + Math.pow(distances[2], -hypParam[2]);
	}
	
	/**
	 * Objective function (utility) for "hot pursuit" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility1(float[] distances, int[] hypParam) {
		return hypParam[0]*distances[0] + Math.pow(distances[1], -hypParam[1]) - hypParam[2]*distances[2];
	}
}