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
	
	// ghost safe distance
	private int minDistance = 10;
	// utility functions hyperparamenters
	private float[] hypParam0;
	private float[] hypParam1;
	
	public MyPacMan(float[] hypParam0, float[] hypParam1, int minDistance) {
		this.hypParam0 = hypParam0;
		this.hypParam1 = hypParam1;
		this.minDistance = minDistance;
	}

	public MOVE getMove(Game game, long timeDue) 
	{
		//Place your game logic here to play the game as Ms Pac-Man
		
		// Get current MsPacman position
		int posMsPacman = game.getPacmanCurrentNodeIndex();
		
		// check if all ghosts are far enough
		boolean safe = true;
		for(GHOST ghost : GHOST.values()) {
			if(game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)) > minDistance) {
				safe = false;
				break;
			}
		}
		
		float[] distances = new float[3];
		double bestMove = 0, utility = 0;
		int newPosMsPacman = -1;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		myMove = MOVE.NEUTRAL;
		for(MOVE move : moves) {
			newPosMsPacman = game.getNeighbour(posMsPacman, move);
			if(newPosMsPacman != -1) {
				distances = getSensorData(game, newPosMsPacman);
				if(safe)
					utility = utility0(distances, hypParam0);
				else
					utility = utility1(distances, hypParam1);
				if(utility > bestMove) {
					bestMove = utility;
					myMove = move;
				}
			}
		}

		return myMove;
	}
	
	/**
	 * Retrieves sensor information for MsPacman agent
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @return array of distances information, in order: average ghosts distance, nearest power pill, nearest pill
	 */
	private float[] getSensorData(Game game, int posMsPacman) {
		float[] distances = new float[3];
		distances[0] = weightedGhostsDistance(game, posMsPacman);
		distances[1] = nearestPillDistance(game, posMsPacman, true);
		distances[2] = nearestPillDistance(game, posMsPacman, false);
		return distances;
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
			if(game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)) == -1)
				distances.add((float)100);
			else
				distances.add((float)game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)));
		}
		float maxDistance = Collections.max(distances);
		float meanDistance = 0;
		for(Float dist : distances) {
			meanDistance += (1-(dist/maxDistance))*dist;
		}
		return meanDistance/4;
	}
	
	/**
	 * Computes the distance of the nearest active power pill or simple pill from MsPacman
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @param  isPowerPill true if power pill, else simple pill
	 * @return distance from the nearest power pill or simple pill
	 */
	private int nearestPillDistance(Game game, int posMsPacman, boolean isPowerPill) {
		int pillIndices[], minDistance = 100000;
		boolean isPillActive[];
		int i = 0;
		if(isPowerPill) {
			pillIndices = game.getActivePowerPillsIndices();
		}
		else {
			pillIndices = game.getActivePillsIndices();
		}
		for(i = 0; i < pillIndices.length; i++) {
			if(game.getShortestPathDistance(posMsPacman, pillIndices[i]) < minDistance)
				minDistance = game.getShortestPathDistance(posMsPacman, pillIndices[i]);
		}
		return minDistance;
	}
	
	/**
	 * Objective function (utility) for "conservative" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility0(float[] distances, float[] hypParam) {
		return Math.pow(distances[0], hypParam[0]) + Math.pow(distances[1], hypParam[1]) + Math.pow(distances[2], -hypParam[2]);
	}
	
	/**
	 * Objective function (utility) for "hot pursuit" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility1(float[] distances, float[] hypParam) {
		return hypParam[0]*distances[0] + Math.pow(distances[1], -hypParam[1]) + Math.pow(distances[2], -hypParam[2]);
	}
}