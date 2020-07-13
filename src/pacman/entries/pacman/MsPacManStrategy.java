package pacman.entries.pacman;

import pacman.game.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MsPacManStrategy {
	
	private static final int GUARD_DISTANCE = 10;
	private static final int MIN_DISTANCE = 40;
	
	
	public int getSafeNotShortestPill(Game game, int current, int[] targets) {
		int minDistance = Integer.MAX_VALUE, maxDistance = Integer.MIN_VALUE;
		int bestPill = -1;	
		
		for(int node: targets) {
			
		}
		return bestPill;
	}
	
	public int getNearestSafeJunction(Game game, int pos) {
		int minDistance = Integer.MAX_VALUE;
		int safeJunction = -1;	
		int[] junctions = game.getJunctionIndices();
		int[] safeJunctions = new int[junctions.length];
		int j = 0;
		for(int junct : junctions) {
			
			boolean safeJunct = true; 
			
			// Judgement of the goodness of the junction
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost)<30 && game.getGhostLairTime(ghost)==0) {
					if(game.getShortestPathDistance(pos, junct)+GUARD_DISTANCE > // if Ms Pacman DOESN'T reach the junction before a ghost
					game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost))) 
					{
						safeJunct = false;
					}
					else {
						int[] path = game.getShortestPath(pos, junct);
						for(int elem: path) {
							// check if in the path there is a ghost
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
//				// it's safe
				safeJunctions[j] = junct;
				j++;
			}
		}
		ArrayList<Integer> realSafeJunction=new ArrayList<Integer>();
		// 
		// The array was initialized with length of all junctions
		// Consider only those != 0
		for(int node: safeJunctions) {
			for(int nodeOth: safeJunctions) {
				if(node != 0 && nodeOth !=0 && node != nodeOth) {
						realSafeJunction.add(node);
				}
			}
		}
		// Choose the closest safe junction
		for(Integer junct : realSafeJunction) {
			if(game.getShortestPathDistance(pos, junct) < minDistance) {
				minDistance = game.getShortestPathDistance(pos, junct);
				safeJunction = junct;
			}
		}
		
		return safeJunction;
	}
	
	
	/**
	 * Gets a close index from a set of targets when no safe indices can be found
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available pill indices
	 * @return the closest pill index
	 */
	public int getClosestPill(Game game, int pos, int[] targets) {
		int minDistance = Integer.MAX_VALUE, maxDistance = Integer.MIN_VALUE;
		int bestPill = -1;
//		int closestGhostindex = -1;
//		if(getCloserGhost(game, pos) != null) 
//			closestGhostindex = game.getGhostCurrentNodeIndex(getCloserGhost(game, pos));
		
		// BISOGNA DECIDERE LA LOGICA CON CUI SCEGLIERE I TARGET
		// La parte seguente è una possibile implementazione, not safe -> pill più vicino sufficientemente lontano dal ghost più vicino
		//														  safe -> pill massimamente lontano dai ghost più vicino possibile
		
		// Sceglie il pill più vicino a pacman e massimamente lontano dai ghost
		for(int pill: targets) {
			ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
			int dist = game.getShortestPathDistance(pos, pill);
			for(GHOST ghost: GHOST.values()) {
				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
					ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pill));
				}
				if(ghostDistance.isEmpty()) {
					if(dist < minDistance) {
						bestPill = pill;
						minDistance = dist;
					}
				}
				else {
					if(dist < minDistance && !ghostDistance.isEmpty() && Collections.min(ghostDistance) > maxDistance) {
						bestPill = pill;
						minDistance = dist;
						maxDistance = Collections.min(ghostDistance);
					}
				}
				ghostDistance.clear();
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
	public GHOST getClosestGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE, distance = 0;
		GHOST minGhost=null;	
		
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
				distance = game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
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
		// sort by distance AVAILABLE power pills
		for(int target: targets) {
			powerPillsDistance.put(game.getShortestPathDistance(pos, target), target);
		}
		// sort ghosts by distance from pacman
		for(GHOST ghost: GHOST.values()) {
			if(game.getGhostLairTime(ghost) <= 0)
				ghostDistance.add(game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost)));
		}
		Collections.sort(ghostDistance);
		// check if there is a power pill reachable with no ghosts in the path and enough far from the nearest ghost
		int bestPill = -1;
		if(!ghostDistance.isEmpty() ) {
			for(Integer target: powerPillsDistance.values()) {
				if(checkSafeChase(target, pos, game) && ghostDistance.getFirst() <= MIN_DISTANCE) {
					bestPill = target;
					break;
				}
			}
		}
		return bestPill;
	}

	/**
	 * Verifies if a ghost can be safely eaten
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param edibleGhost 
	 * @param pacmanMove current pacman move
	 * @return true if edibleGhost can be safely eaten, false otherwise
	 */
	public boolean checkSafeEat(Game game, int pos, GHOST edibleGhost) {
		boolean safeEat = true;
		int edibleGhostIndex = game.getGhostCurrentNodeIndex(edibleGhost);
		int otherGhostIndex, otherGhostDistance;
		
		GHOST otherGhost = getClosestGhost(game, game.getGhostCurrentNodeIndex(edibleGhost));
		if(otherGhost != null) {
			otherGhostIndex = game.getGhostCurrentNodeIndex(otherGhost);
			otherGhostDistance = game.getShortestPathDistance(edibleGhostIndex, otherGhostIndex);
			if(otherGhostDistance < GUARD_DISTANCE) {
				safeEat = false;
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
	 * Checks if there is an edible ghost
	 * @param game game manager instance
	 * @return true/false, false if all ghosts are non edible
	 */
	public boolean isThereEdibleGhost(Game game) {
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost) > 30) {
				return true;
			}
		return false;
	}
	
	/**
	 * Checks if at least a ghost is in the lair
	 * @param game game manager instance
	 * @return true if there is a ghost in lair, false otherwise
	 */
	public boolean isThereGhostInLair(Game game) {
		boolean ghostInLair = false;
		for(GHOST ghost : GHOST.values()) { 
			if(game.getGhostLairTime(ghost) > 0) {
				ghostInLair = true;
				break;
			}
		}
		return ghostInLair;
	}
	
	public int[] getAllNodes(Game game) {
		int[] pills = null;
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		pills = game.getPillIndices();
		for(int i=0; i<pills.length; i++) {
			targets.add(pills[i]);	
		}
		int[] targetsArray=new int[targets.size()];		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		return targetsArray;
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
//			// no power pills left
//			if(targets.size() == 0) {
//				pills = game.getPillIndices();
//				for(int i=0;i<pills.length;i++)								
//					if(game.isPillStillAvailable(i))
//						targets.add(pills[i]);
//			}
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
	 * Get the indices of pills in the map
	 * @param game game manager instance
	 * @param available if true get map indices of availble pills, oterwise any pill
	 * @return pill indices
	 */
	public int[] getPillTargets(Game game, boolean available) {
		int[] pills = null;
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		pills = game.getPillIndices();
		for(int i=0; i<pills.length; i++) {
			if(available) {
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
			}
			else {
				targets.add(pills[i]);
			}	
		}
		int[] targetsArray=new int[targets.size()];		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		return targetsArray;
	}
	
	/**
	 * Get the indices of power pills in the map
	 * @param game game manager instance
	 * @param available if true get map indices of availble power pills, oterwise any power pill
	 * @return power pill indices
	 */
	public int[] getPowePillTargets(Game game, boolean available) {
		int[] pills = null;
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		pills = game.getPowerPillIndices();
		for(int i=0; i<pills.length; i++) {
			if(available) {
				if(game.isPowerPillStillAvailable(i))
					targets.add(pills[i]);
			}
			else {
				targets.add(pills[i]);
			}	
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
		
		if(targets.length != 0) {
			for(int target: targets) {
				j = 0;
				int[] junctions = game.getJunctionIndices();
				int[] safeJunctions = new int[junctions.length];
				for(int junct : junctions) {
					safeJunct = true;
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
	
	/**
	 * Finds safe junctions with respect to a set of target points when pacman is chased
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets set of target maze indices
	 * @return map associating safe junctions to a given maze index
	 */
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
							// a non edible ghost is too close to the considered juction (no need of guard distance, ghosts are
							// behind pacman. Do not allow reverse move, ghosts behind could eat pacman
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
	
	/**
	 * Picks the closest safe index and the reachable junctions from it
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets set of target maze indices
	 * @param chased true if pacman is chased, false otherwise
	 * @return a list in which the last element is the safe index, the rest of the list are the reachable junctions indices
	 */
	public ArrayList<Integer> getClosestSafeJunction(Game game, int pos, int[] targets, boolean chased) {
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		int minDistance = Integer.MAX_VALUE, dist = 0, safeClosestIndex = -1;
		ArrayList<Integer> safeClosestJunction = new ArrayList<Integer>();
		
		if(targets.length > 0) {
			if(!chased) {
				safeJunctions = getSafeJuctions(game, pos, targets);
			}
			else {
				safeJunctions = getSafeJuctionsWhenChased(game, pos, targets);
//				System.out.println("Else chased reached");
			}
		}
		
		ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
		int maxDistance = Integer.MIN_VALUE;
		int closestGhostindex = -1;
		if(safeJunctions != null) {
			for(Integer target: safeJunctions.keySet()) {
				if(safeJunctions.get(target).size() > 0) {
					dist = game.getShortestPathDistance(pos, target);
					
					if(!chased) {
						if(getClosestGhost(game, pos) != null) 
							closestGhostindex = game.getGhostCurrentNodeIndex(getClosestGhost(game, pos));
						if(closestGhostindex != -1) {
							if(dist < minDistance &&	
								dist+GUARD_DISTANCE < game.getShortestPathDistance(closestGhostindex, target)) {
								minDistance = dist;
								safeClosestIndex = target;
								safeClosestJunction = safeJunctions.get(target);
							}
						}
						else {
							if(dist < minDistance) {
								minDistance = dist;
								safeClosestIndex = target;
								safeClosestJunction = safeJunctions.get(target);
							}
						}
					}
					else {
						for(GHOST ghost: GHOST.values()) {
							if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
								ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target));
							}
						}
						
						if(ghostDistance.isEmpty()) {
							if(dist < minDistance) {
								safeClosestIndex = target;
								minDistance = dist;
								safeClosestJunction = safeJunctions.get(target);
							}
						}
						else {
							if(dist < minDistance && Collections.min(ghostDistance) >= maxDistance) {
								safeClosestIndex = target;
								minDistance = dist;
								maxDistance = Collections.min(ghostDistance);
								safeClosestJunction = safeJunctions.get(target);
							}
						}
						ghostDistance.clear();
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
	public boolean checkSafeChase(int target, int pos, Game game) {
		// check if the non edible ghost is in the path for going to the minGhost
		int[] path = game.getShortestPath(pos, target);
		// first check if a non edible ghost is in the path
		for(int gIndex: path)
		{
			for(GHOST ghost : GHOST.values()) {
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost) == 0) {
					if(gIndex == game.getGhostCurrentNodeIndex(ghost))
					{
						return false;
					}
				}
			}
		}
		// then check if a non edible ghost path to pacman intersects the path to the edible one
		// if so check if pacman is "faster" than the ghost
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost) == 0) {
				int[] ghostPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), pos);
				for(int gElem: ghostPath) {
					for(int pElem: path) {
						if(gElem == pElem) {
							// check if the ghost arrives there before pacman, then it is not safe
							if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), gElem, game.getGhostLastMoveMade(ghost)) < 
									game.getShortestPathDistance(pos, gElem)+GUARD_DISTANCE) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
//	/**
//	 * Checks if MsPacman can safely chase a ghost
//	 * @param minGhost ghost index to chase
//	 * @param pos current MsPacman position in the maze
//	 * @param game game manager instance
//	 * @return true if MsPacman can safely chase ghost, false otherwise
//	 */
//	public boolean checkSafeChase(int target, int pos, Game game) {
//		// check if the non edible ghost ghost is in the path for going to the minGhost
//		// THIS MAY FAIL BECAUSE OF GHOSTS INCOMING FROM JUNCTIONS ADJOINING THE PATH CONSIDERED
//		boolean safeChase = true;
//		
//		int[] path = game.getShortestPath(pos, target);
//		List<Integer> pathList  = Arrays.stream(path).boxed().collect(Collectors.toList());
//		List<Integer> ghostDistance = new ArrayList<Integer>();
//		for(GHOST ghost : GHOST.values()) {
//			if(game.getGhostEdibleTime(ghost) <=0 && game.getGhostLairTime(ghost) <= 0) {
//				if(pathList.contains(game.getGhostCurrentNodeIndex(ghost))) {
//					safeChase = false;
//				}
//				else {
//					ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target));
//				}
//			}
//		}
//		if(safeChase && !ghostDistance.isEmpty()) {
//			if(game.getShortestPathDistance(pos, target)+GUARD_DISTANCE > Collections.min(ghostDistance))
//				safeChase = false;
//		}
//		return safeChase;
//	}
	
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
			if(distance > maxGhostDistance && distance < 2*MIN_DISTANCE &&
					game.getGhostLairTime(ghost) <= 0 && game.getGhostEdibleTime(ghost) <= 0) {
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
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {}
		return chasers+1;
	}

	/**
	 * Checks if there is a cluster of ghosts
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @param clusterDistance max distance used to define a cluster
	 * @return true if a cluster is found, false otherwise
	 */
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
