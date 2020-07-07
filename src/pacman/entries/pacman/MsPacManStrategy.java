package pacman.entries.pacman;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.internal.Maze;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MsPacManStrategy {
	
	private static final int GUARD_DISTANCE = 10;
	private static final int MIN_DISTANCE = 40;
	
	public int liarIndex(Game game) {
		Maze maze = game.getCurrentMaze();
		return maze.lairNodeIndex;
	}
	
	/**
	 * Get the closest pill from the available in targets
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available pill indices
	 * @return the closest pill index
	 */
	public int getClosestPill(Game game, int pos, int[] targets) {
		int minDistance = Integer.MAX_VALUE;
		int bestPill = -1;
		for(int pill: targets) {
			if(game.getShortestPathDistance(pos, pill) < minDistance) {
				minDistance = game.getShortestPathDistance(pos, pill);
				bestPill = pill;
			}
		}
		return bestPill;
	}
	
	/**
	 * Get the index of the closest power pill
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return index of the closest power pill
	 */
	public int getClosestPowerPillIndex(Game game, int pos) {
		int[] pills = null;
		pills = game.getPowerPillIndices();
		int minDistance = Integer.MAX_VALUE;
		int bestPill = -1;
		
		for(int pill: pills) {
			if(game.getShortestPathDistance(pos, pill) < minDistance) {
				minDistance = game.getShortestPathDistance(pos, pill);
				bestPill = pill;
			}
		}
		return bestPill;
	}
	
	/**
	 * Get the closest ghost
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return the closest ghost 
	 */
	public GHOST getCloserGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE, distance = 0;
		int ghostIndex;
		GHOST minGhost=null;	
		
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
				distance = game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
				if(distance < minDistance) {
					minDistance=distance;
					minGhost=ghost;
				}
			}
		}
		return minGhost;
	}
	
	/**
	 * Finds the nearest power pill from MsPacman furthest from ghosts
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available power pill indices
	 * @return index of the best power pill
	 */
	public int trapTheGhosts(Game game, int pos, int[] targets) {
		SortedMap<Integer, Integer> powerPillsDistance = new TreeMap<Integer, Integer>();
		LinkedList<Integer> ghostDistance = new LinkedList<Integer>();
		// sort by distance available power pills
		for(int target: targets) {
			powerPillsDistance.put(game.getShortestPathDistance(pos, target), target);
		}
		for(GHOST ghost: GHOST.values()) {
			if(game.getGhostLairTime(ghost) <= 0)
				ghostDistance.add(game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost)));
		}
		Collections.sort(ghostDistance);
		// check if there is a power pill reachable with no ghosts in the path
		int bestPill = -1;
		if(!ghostDistance.isEmpty() ) {
			for(Integer target: powerPillsDistance.values()) {
				if(checkSafeChase(target, pos, game) && ghostDistance.getLast() <= MIN_DISTANCE) {
					bestPill = target;
					break;
				}
			}
		}
		return bestPill;
	}
	
	public boolean checkSafeEat(Game game, int pos, GHOST edibleGhost, MOVE pacmanMove) {
		boolean safeEat = true;
		int edibleGhostIndex = game.getGhostCurrentNodeIndex(edibleGhost);
		int otherGhostIndex, otherGhostDistance;
		int[] path;
		
		for(GHOST ghost: GHOST.values()) {
			if(ghost != edibleGhost && game.getGhostEdibleTime(ghost) <= 0) {
				otherGhostIndex = game.getGhostCurrentNodeIndex(ghost);
				otherGhostDistance = game.getShortestPathDistance(edibleGhostIndex, otherGhostIndex);
				if(otherGhostDistance < GUARD_DISTANCE && game.getGhostLastMoveMade(ghost) == pacmanMove.opposite()) {
					safeEat = false;
					break;
				}
//				path = game.getShortestPath(edibleGhostIndex, edibleGhostIndex + GUARD_DISTANCE/2);
//				for(int i: path) {
//					if(i == otherGhostIndex) {
//						safeEat = false;
//						break;
//					}
//				}
				if(!safeEat)
					break;
			}
		}
		return safeEat;
	}
	
	/**
	 * Checks if there is an edible ghost
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return the nearest edible ghost if exists, null otherwise
	 */
	public GHOST isThereEdibleGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost) > 30) {
				int distance=game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
				if(distance<minDistance) {
					minDistance=distance;
					minGhost=ghost;
				}
			}
		return minGhost;
	}
	
	/**
	 * Checks if at least a ghostis in the liar
	 * @param game game manager instance
	 * @return true if there is a ghost in liar, false otherwise
	 */
	public boolean isThereGhostInLiar(Game game) {
		boolean ghostInLair = false;
		for(GHOST ghost : GHOST.values()) { 
			if(game.getGhostLairTime(ghost) > 0) {
				ghostInLair = true;
				break;
			}
		}
		return ghostInLair;
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
	
	/**
	 * Finds safe junctions with respect to a set of target points
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets set of target maze indices
	 * @return map associating safe junctions to a given maze index
	 */
	public Map<Integer, ArrayList<Integer>> getSafeJuctions(Game game, int pos, int[] targets) {
		Map<Integer, ArrayList<Integer>> safeZones = new HashMap<Integer, ArrayList<Integer>>();
		int dist = 0, j = 0;
		int[] path = null;
		boolean safeJunct = true;
		int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(pos, targets, DM.PATH));
		if(neighbouringPills.length != 0) {
			for(int target: targets) {
				j = 0;
				int[] junctions = game.getJunctionIndices();
				int[] safeJunctions = new int[junctions.length];
				for(int junct : junctions) {
					safeJunct = true;
					//GameView.addPoints(game, Color.pink, junct);
					dist = game.getShortestPathDistance(pos, target); 
					for(GHOST ghost : GHOST.values()) { 
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							// a non edible ghost is too close to the considered juction
							if(dist+game.getShortestPathDistance(target, junct)+GUARD_DISTANCE > 
								game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost))) {
								safeJunct = false;
							}
							// this junction is a safe place, get the best way to reach it if no ghosts are in the path
							else {
								if(!checkSafeChase(junct, pos, game))
									safeJunct = false;
							}
						}
					}
					if(safeJunct) {
						safeJunctions[j] = junct;
						j++;
					}
				}
				
				ArrayList<Integer> reachableJunctions = new ArrayList<Integer>();
				for(int node: safeJunctions) {
					for(int nodeOth: safeJunctions) {
						if(node != 0 && nodeOth !=0 && node != nodeOth) {
								reachableJunctions.add(node);
						}
					}
				}
				safeZones.put(target, reachableJunctions);
			}
		}
		return safeZones;
	}
	
	public Map<Integer, ArrayList<Integer>> getSafeJuctionsWhenChased(Game game, int pos, int[] targets) {
		Map<Integer, ArrayList<Integer>> safeZones = new HashMap<Integer, ArrayList<Integer>>();
		int dist = 0, j = 0;
		int[] path = null;
		boolean safeJunct = true;
		int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(pos, targets, DM.PATH));
		if(neighbouringPills.length != 0) {
			for(int target: targets) {
				j = 0;
				int[] junctions = game.getJunctionIndices();
				int[] safeJunctions = new int[junctions.length];
				for(int junct : junctions) {
					safeJunct = true;
					//GameView.addPoints(game, Color.pink, junct);
					dist = game.getShortestPathDistance(pos, target); 
					for(GHOST ghost : GHOST.values()) { 
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							// a non edible ghost is too close to the considered juction
							if(dist+game.getShortestPathDistance(target, junct) > 
								game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost)) ||
								game.getNextMoveTowardsTarget(pos, target, DM.PATH) == game.getPacmanLastMoveMade().opposite()) {
								safeJunct = false;
							}
							// this junction is a safe place, get the best way to reach it if no ghosts are in the path
							else {
								if(!checkSafeChase(junct, pos, game))
									safeJunct = false;
							}
						}
					}
					if(safeJunct) {
						safeJunctions[j] = junct;
						j++;
					}
				}
				
				ArrayList<Integer> reachableJunctions = new ArrayList<Integer>();
				for(int node: safeJunctions) {
					for(int nodeOth: safeJunctions) {
						if(node != 0 && nodeOth !=0 && node != nodeOth) {
								reachableJunctions.add(node);
						}
					}
				}
				safeZones.put(target, reachableJunctions);
			}
		}
		return safeZones;
	}
	
	public ArrayList<Integer> getClosestSafeJunction(Game game, int pos, int[] targets, boolean chased) {
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		int minDistance = Integer.MAX_VALUE, dist = 0, safeClosestIndex = -1;
		ArrayList<Integer> safeClosestJunction = new ArrayList<Integer>();
		
		if(targets.length > 0) {
			if(!chased)
				safeJunctions = getSafeJuctions(game, pos, targets);
			else
				safeJunctions = getSafeJuctionsWhenChased(game, pos, targets);
		}
		if(safeJunctions != null) {
			for(Integer target: safeJunctions.keySet()) {
				if(safeJunctions.get(target).size() >= 2 ) {
					dist = game.getShortestPathDistance(pos, target);
					if(dist < minDistance) {
						safeClosestIndex = target;
						minDistance = dist;
						safeClosestJunction = safeJunctions.get(target);
					}
				}
			}
		}
		// The last element of the array is the index of the closest safe node
		safeClosestJunction.add(safeClosestIndex);
		return safeClosestJunction;
	}
	
	/**
	 * Checks if MsPacman can safely chase a ghost
	 * @param minGhost ghost index to chase
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return true if MsPacman can safely chase ghost, false otherwise
	 */
	public boolean checkSafeChase(int minGhost, int pos, Game game) {
		// check if the non edible ghost ghostnot is in the path for going to the minGhost
		int[] path = game.getShortestPath(pos, minGhost);
		for(int gIndex: path) {
			for(GHOST ghost : GHOST.values()) {
				if(game.getGhostEdibleTime(ghost)==0) {
					if(gIndex == game.getGhostCurrentNodeIndex(ghost)) {
						//System.out.println("ghost inbetween");
						return false;
					}
				}
			}
		}
		return true;
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
		int maxGhostDistance = Integer.MIN_VALUE, distance;		
		GHOST maxGhost = null;
		for(GHOST ghost : GHOST.values()) {
			distance = game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
			// if a ghost is too far do not consider it as a chaser
			if(distance > maxGhostDistance && game.getGhostLairTime(ghost) <= 0 && game.getGhostEdibleTime(ghost) <= 0) {
				maxGhostDistance = distance;
				maxGhost = ghost;
			}
		}
		
		// check if ghosts are chasing MsPacman
		try {
			if(maxGhost != null && game.getGhostLairTime(maxGhost) == 0) {
				int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost));
				for(int i: path) {
					for(GHOST ghost: GHOST.values()) {
						if(ghost != maxGhost && i == game.getGhostCurrentNodeIndex(ghost) && game.getGhostLairTime(ghost) <= 0)
							chasers += 1;
					}
				}
				//GameView.addPoints(game,Color.green, game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost)));
//				System.out.println("Sono inseguito da "+chasers);
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
//			System.out.println("Rotto");
		}
		return chasers+1;
	}

	public boolean isThereGhostsCluster(int pos, Game game, int clusterDistance) {
		boolean clusterFound = false;
		int clusterSize = 0;
		for(GHOST ghost: GHOST.values()) {
			if(game.getGhostLairTime(ghost) <= 0 && 
					game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost)) < clusterDistance) {
				clusterSize++;
			}
		}
		if(clusterSize >= 2)
			clusterFound = true;
		return clusterFound;
	}
}
