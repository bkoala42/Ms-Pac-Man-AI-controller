package pacman.entries.pacman;

import pacman.game.Game;
import pacman.game.GameView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

public class MsPacManStrategy {
	
	private static final int GUARD_DISTANCE=10;
	private static final int MIN_DISTANCE = 50;
	
	public GHOST getCloserGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE, distance = 0;
		GHOST minGhost=null;	
		
		for(GHOST ghost : GHOST.values()) {
			distance=game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
			if(distance<minDistance) {
				minDistance=distance;
				minGhost=ghost;
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
		// sort by distance available power pills
		for(int target: targets) {
			powerPillsDistance.put(game.getShortestPathDistance(pos, target), target);
		}
		
		// check if there is a power pill reachable with no ghosts in the path
		int[] path = null;
		boolean safePath = true;
		int bestPill = -1;
		for(Integer target: powerPillsDistance.values()) {
			path = game.getShortestPath(pos, target);
			for(int gIndex: path) {
				for(GHOST ghost : GHOST.values()) {
					if(gIndex == game.getGhostCurrentNodeIndex(ghost)) {
						safePath = false;
					}
				}
				if(!safePath)
					break;
			}
			if(safePath) {
				bestPill = target;
				break;
			}
		}
//		int minGhostDistance = Integer.MAX_VALUE;
//		int bestPill = -1;
////		int[] distance=new int[targets.length];		    //shortest path distances of MsPacman from power pills 
//		int[] distanceGhost=new int[targets.length]; 	//shortest path distances of ghosts from power pills 
//		int d = Integer.MAX_VALUE;
//		
//		
	
//			path = game.getShortestPath(pos, d);
//			for(int gIndex: path) {
//				for(GHOST ghost : GHOST.values()) {
//					if(game.getGhostEdibleTime(ghost)==0) {
//						if(gIndex == game.getGhostCurrentNodeIndex(ghost)) {
//							//System.out.println("ghost inbetween");
//							return false;
//						}
//					}
//				}
//			}
//		}
		
//		// compute shortest path distance to power pills for MsPacman and the ghosts
//		for(int i=0; i<distance.length; i++) {
//			for(GHOST ghost: GHOST.values()) {
//				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), targets[i]) < minGhostDistance)
//					minGhostDistance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), targets[i]);
//			}
//			distanceGhost[i] = minGhostDistance;
//			minGhostDistance = Integer.MAX_VALUE;
//			distance[i]=game.getShortestPathDistance(pos, targets[i]);
//		}
//		
//		// if exists, pick as target a power pill nearer to MsPacman and far from ghosts
//		int diff = 0;
//		for(int i = 0; i < distance.length; i++) {
//			if(distance[i] < distanceGhost[i]) {
//				bestPill = targets[i];
//				diff = distanceGhost[i] - distance[i];
//			}
//		}
		return bestPill;
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
			if(game.getGhostEdibleTime(ghost) > 10) {
				int distance=game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
				if(distance<minDistance) {
					minDistance=distance;
					minGhost=ghost;
				}
			}
		return minGhost;
	}
	
	/**
	 * Checks if all ghosts are in the liar
	 * @param game game manager instance
	 * @return true if all ghosts are in liar, false otherwise
	 */
	public boolean areGhostsInLiar(Game game) {
		boolean areAllGhostInLair = true;
		for(GHOST ghost : GHOST.values()) { 
			if(game.getGhostLairTime(ghost)==0) {
				areAllGhostInLair = false;
				break;
			}
		}
		return areAllGhostInLair;
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
//								System.out.println(dist+game.getShortestPathDistance(target, junct)+GUARD_DISTANCE);
//								System.out.println("  "+game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost)));
							}
							// this junction is a safe place, get the best way to reach it if no ghosts are in the path
							else {
								path = game.getShortestPath(pos, junct);
								for(int elem: path) {
									for(GHOST ghost2 : GHOST.values()) {
										if(elem == game.getGhostCurrentNodeIndex(ghost2)) {
											safeJunct = false;
										}
									}
								}
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
	
	/**
	 * Checks if MsPacman can safely chase a ghost
	 * @param minGhost ghost to chase
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return true if MsPacman can safely chase ghost, false otherwise
	 */
	public boolean checkSafeChase(GHOST minGhost, int pos, Game game) {
		// check if the non edible ghost ghostnot is in the path for going to the minGhost
		int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(minGhost));
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
				GameView.addPoints(game,Color.green, game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost)));
				//System.out.println("Sono inseguito da "+chasers);
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			//System.out.println("Rotto");
		}
		return chasers;
	}

}
