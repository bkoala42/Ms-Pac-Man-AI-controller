package pacman.entries.adversarial;

import java.awt.Color;
import java.util.ArrayList;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;

public class MsPacManAlphaBetaStrategy {
	
	private GHOST minEdibleGhost;
	private GHOST minNonEdibleGhost;
	private static final int MIN_DISTANCE = 50;
	
	public GHOST getMinEdibleGhost() {
		return minEdibleGhost;
	}
	
	public GHOST getMinNonEdibleGhost() {
		return minNonEdibleGhost;
	}
	
	/**
	 * Gets the available pills in the maze
	 * @param game game manager instance
	 * @param isPowerPill true if available power pills are required, false otherwise
	 * @return array of available pills
	 */
	public int[] pillTargets(Game game, boolean isPowerPill) {
		int[] pills = null;
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		if(isPowerPill) {
			pills = game.getPowerPillIndices();
			for(int i=0;i<pills.length;i++)								
				if(game.isPowerPillStillAvailable(i))
					targets.add(pills[i]);	
		}
		else {
			pills = game.getPillIndices();
			for(int i=0;i<pills.length;i++)								
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);	
		}
		int[] targetsArray=new int[targets.size()];		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		return targetsArray;
	}
	
	public double getClosestPillDistance(Game game, int pos, boolean isPowerPill) {
		int minDistance = Integer.MAX_VALUE;
		int bestPill = -1;
		int[] targets = pillTargets(game, isPowerPill);
		for(int pill: targets) {
			if(game.getShortestPathDistance(pos, pill) < minDistance) {
				minDistance = game.getShortestPathDistance(pos, pill);
				bestPill = pill;
			}
		}
		if(bestPill != -1) {
			return minDistance;
		}
		else
			return 0;
	}
	
	public double getClosestGhostDistance(Game game, int pos, boolean isEdible, Integer[] ghostPosition) {
		int minDistance=Integer.MAX_VALUE, distance = 0, i = 0;
		double retDistance;
		
		for(GHOST ghost : GHOST.values()) {
			if(!isEdible) {
				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
					distance = game.getShortestPathDistance(pos, ghostPosition[i]);
					if(distance < minDistance) {
						minDistance = distance;
						minNonEdibleGhost = ghost;
					}
				}
			}
			else {
				if(game.getGhostEdibleTime(ghost) > 0 && game.getGhostLairTime(ghost) <= 0) {
					distance = game.getShortestPathDistance(pos, ghostPosition[i]);
					if(distance < minDistance) {
						minDistance = distance;
						minEdibleGhost = ghost;
					}
				}	
			}
			i++;
		}
		if(minDistance == Integer.MAX_VALUE) {
			retDistance = 0;
		}
		else {
			retDistance = minDistance;
		}
//		System.out.println("Distance "+retDistance+ " "+isEdible);
		return retDistance;
	}

	/**
	 * Checks if MsPacman can safely chase a ghost
	 * @param minGhost ghost index to chase
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return true if MsPacman can safely chase ghost, false otherwise
	 */
	public boolean checkSafeChase(int pos, Game game) {
		boolean safe = true;
		// check if the non edible ghost ghostnot is in the path for going to the minGhost
		int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(minEdibleGhost));
		for(int gIndex: path) {
			for(GHOST ghost : GHOST.values()) {
				if(game.getGhostEdibleTime(ghost)==0) {
					if(gIndex == game.getGhostCurrentNodeIndex(ghost)) {
						//System.out.println("ghost inbetween");
						safe = false;
					}
				}
			}
		}
		return safe;
	}
	
	/**
	 * Check if MsPacman is pursued by ghosts
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return	number of ghosts that are chasing MsPacman 
	 */
	public int isMsPacManChased(int pos, Game game) {
		int chasers = 0;

		// get the shortest path from the furthest ghost
		int maxGhostDistance=Integer.MIN_VALUE, distance;		
		GHOST maxGhost = null;
		for(GHOST ghost : GHOST.values()) {
			distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
			// if a ghost is too far do not consider it as a chaser
			if(distance > maxGhostDistance && distance < MIN_DISTANCE) {
				maxGhostDistance = distance;
				maxGhost=ghost;
			}
		}
		
		// check if ghosts are chasing MsPacman
		try {
			if(maxGhost != null) {
				int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost));
				for(int i: path) {
					for(GHOST ghost: GHOST.values()) {
						if(i == game.getGhostCurrentNodeIndex(ghost) && game.getGhostLairTime(ghost) <= 0)
							chasers += 1;
					}
				}
				//GameView.addPoints(game,Color.green, game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost)));
				//System.out.println("Sono inseguito da "+chasers);
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			//System.out.println("Rotto");
		}
		return chasers;
	}

}
