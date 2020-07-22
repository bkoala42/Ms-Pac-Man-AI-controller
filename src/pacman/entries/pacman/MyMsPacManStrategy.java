package pacman.entries.pacman;

import pacman.game.Game;
import pacman.game.GameView;

import java.awt.Color;
import java.util.ArrayList;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

// This class contains all the logic used by the agent to search a point in the map to move towards 
// according to environment state
public class MyMsPacManStrategy {
	
//	private static final int CHASE_DISTANCE = 50;
//	private static final int GUARD_DISTANCE = 5;
//	private static final int MIN_DISTANCE = 25;
//	private static final int CLEAN_DISTANCE = 11;
//	private static final int EAT_DISTANCE_HIGH = 30;
	
	private int minGhostDistance;
	private int guardDistance;
	private int chaseDistance;
	private int cleanDistance;
	private int eatDistanceHigh;
	
	private static final int EAT_DISTANCE_MEDIUM = 15;
	private static final int EAT_DISTANCE_LOW = 10;
	private static final int FLASH_TIME_HIGH = 30;
	private static final int FLASH_TIME_MEDIUM = 15;
	private static final int FLASH_TIME_LOW = 10;
	private static boolean GHOST_IN_THE_LAIR = false;
	
	public MyMsPacManStrategy(int minGhostDistance, int guardDistance, int chaseDistance, int cleanDistance, int eatDistanceHigh) {
		this.minGhostDistance = minGhostDistance;
		this.guardDistance = guardDistance;
		this.chaseDistance = chaseDistance;
		this.cleanDistance = cleanDistance;
		this.eatDistanceHigh = eatDistanceHigh;
	}

	/**
	 * Get the indices of pills in the map
	 * @param game game manager instance
	 * @param available if true get map indices of available pills, otherwise any pill
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
	 * @param available if true get map indices of available power pills, otherwise any power pill
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
	 * Get the indices of the targets in the map
	 * @param game game manager instance
	 * @param available if true get map indices of available pills and power pills, otherwise any map index
	 * @return all available/not available targets
	 */
	public int[] getAllTargets(Game game, boolean available) {
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
		// append also power pills
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
	 * Get the indices of map junctions that Ms PacMan can reach before than ghosts
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param bannedNode last visited junction
	 * @param oriented true if Ms PacMan current move must be considered in paths computation, false otherwise
	 * @return indices of safe map junctions
	 */
	public ArrayList<Integer> getSafeJunctions(Game game, int pos, int bannedNode, boolean oriented) {
		int dist;
		boolean safeJunct;
		ArrayList<Integer> safeJunctions = new ArrayList<Integer>();
		int[] junctions = game.getJunctionIndices();
		
		// loop over all map junctions
		for(int junct : junctions) {
			safeJunct = true;
			// compute Ms PacMan distance from the junction
			if(oriented)
				dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
			else
				dist = game.getShortestPathDistance(pos, junct);
			// check that no non-edible, or soon non-edible, ghost is able to reach the junction before than Ms PacMan, excluding
			// the last visited junction to avoid turning around on place in dangerous situations (eg when chased by a close ghost)
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
					if(dist+guardDistance > 
					   	game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost))
						|| junct == bannedNode
					   ) {
						safeJunct = false;
					}
				}
			}
			if(safeJunct) {
				safeJunctions.add(junct);
			}
		}
		
		return safeJunctions;
	}
	
	/**
	 * Get the first index of the path leading to a pill passing for a safe junction
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets all available pills/power pills in the map
	 * @param bannedNode last visited junction
	 * @param safeJunction junctions safely reachable by Ms Pac-Man
	 * @return first index of the safe path
	 */
	public int getSafePillPassingForSafeJunction(Game game, int pos, int[] targets, int bannedNode) {
		int dist = 0, returnValue = -1;
		int minDistance = Integer.MAX_VALUE, minDistancePill = Integer.MAX_VALUE;
		int safePillWithJuncts = -1, safeClosestJunction = -1;

		ArrayList<GHOST> chasers = getGhostChasingPacman(pos, game);
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, false);
		boolean enterCorner = true;

		for(GHOST ghost: chasers) {
			if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost) == 0) {
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) > guardDistance) {
					enterCorner = false;
				}
			}
		}
		if(!enterCorner) {
			// Get the closest junction which can be safely reached
			if(safeJunctions.size() > 0) {
				for(int junct: safeJunctions) {
					dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
					if(dist < minDistance && checkSafeChase(junct, pos, game, true)) {
						minDistance = dist;
						safeClosestJunction = junct;
					}
				}
			}

			int chosenPath[] = null; 
			boolean flag = true;
			if(safeClosestJunction != -1) {
				chosenPath = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade());
				// for all the available targets pick the closest one which can be reached by MsPacman before than the ghosts
				// considering the composite path using the safe junction
				for(int target: targets) {
					flag = true;
					dist = game.getShortestPathDistance(pos, safeClosestJunction, game.getPacmanLastMoveMade()) 
							+ game.getShortestPathDistance(safeClosestJunction, target);

					for(GHOST ghost: GHOST.values()) {
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							if(dist+guardDistance >
							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))) {
								flag = false;
								break;
							}
						}
					}

					if(flag && dist < minDistancePill) {
						safePillWithJuncts = target;
						minDistancePill = dist;
					}
				}
			}

			if(safeClosestJunction != -1 && safePillWithJuncts != -1) {
				int[] pathToJunct = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade()); 

				returnValue = pathToJunct[0];
			}
		}
		else {
			boolean flag = true;
			if(targets.length != 0) {
				// for all the available targets pick the closest one which can be reached by MsPacman before than the ghosts
				// all ghost chasing are very close, so we don't mind of a safe junction before the pill
				for(int target: targets) {
					flag = true;
					// Let mspacman turn on herself because of random move from ghost
					// the ghost may not follow her but ambush her in the opposite corner of a corridor
					// Then it would be too late to go back
					dist = game.getShortestPathDistance(pos, target);

					for(GHOST ghost: GHOST.values()) {
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							if(dist+guardDistance >
							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
							|| !checkSafeChase(target, pos, game, false)
									) {
								flag = false;
								break;
							}
						}
					}

					if(flag && dist < minDistancePill ) {
						safePillWithJuncts = target;
						minDistancePill = dist;
					}
				}
			}
			returnValue = safePillWithJuncts;
		}

		return returnValue;
	}
	
	
	/**
	 * Get the safe junction which is further from the closest non edible ghost
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param bannedNode last visited junction
	 * @param safeJunction junctions safely reachable by Ms Pac-Man
	 * @return first index of the safe path leading to the junction
	 */
	public int getSafeJunctionFartherToClosestGhost(Game game, int pos, int bannedNode) {
		int dist = 0;
		int maxDistance = Integer.MIN_VALUE;
		int safeFarthestJunction = -1;
		
//		// get all safe junctions
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, false);
		
		GHOST closestGhost = getClosestGhost(game, pos);
		int closestGhostIndex = -1;
		if(closestGhost != null)
			closestGhostIndex = game.getGhostCurrentNodeIndex(getClosestGhost(game, pos));
		
		if(safeJunctions.size() > 0) {
			for(int junct: safeJunctions) {
				// choose the farthest junction
				if(closestGhostIndex != -1)
					dist = game.getShortestPathDistance(closestGhostIndex, junct, game.getGhostLastMoveMade(closestGhost));
				else
					dist = game.getShortestPathDistance(pos, junct);
				if(dist > maxDistance 
						&& checkSafeChase(junct, pos, game, false)
				   ) {
					safeFarthestJunction = junct;
					maxDistance = dist;
				}
			}
		}
		
		int[] path = null; int returnValue = -1;
		if(safeFarthestJunction != -1)
			 path = game.getShortestPath(pos, safeFarthestJunction);
		if(path != null && path.length != 0) {
			returnValue = path[0];
		}

		return returnValue;
	}
	
	
	/**
	 * Get the index of a close pill with junctions around it
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets pill indices
	 * @return first index of the safe path leading to the pill
	 */
	public int getSafePillCloseToJunctions(Game game, int pos, int[] targets) {				
		int dist = 0, junctDist = 0; double radiusDistance=0, junctRadiusDist = 0;
		boolean safeJunct = true;
		int minDistance = Integer.MAX_VALUE;
		int safePillWithJuncts = -1;
		
		ArrayList<Integer> safeJunctions = new ArrayList<Integer>();
		int[] junctions = game.getJunctionIndices();
		
		// loop over all targets, consider only targets near enough to stay in the zone and clear it
		for(int target: targets) {
			safeJunctions.clear();
			dist = game.getShortestPathDistance(pos, target);
			radiusDistance = game.getEuclideanDistance(pos, target);
			if(radiusDistance < minGhostDistance) {

				// loop over all junctions, consider only junctions near enough to the target
				for(int junct : junctions) {
					safeJunct = true;

					junctDist = game.getShortestPathDistance(target, junct);
					junctRadiusDist = game.getEuclideanDistance(target, junct);

					if(junctRadiusDist > 2*minGhostDistance)
						safeJunct = false;
					else {
						for(GHOST ghost : GHOST.values()) { 
							if(game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost)==0) {
								// 1. check if mspacman can reach the pill before a ghost
								// 2. check if a non edible ghost is too close to the considered junction
								if(dist+guardDistance > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
										|| dist+junctDist+guardDistance > 
								game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost))
								|| !checkSafeChase(target, pos, game, false)
										) {
									safeJunct = false;
									break;
								}
							}
						}	
					}
					if(safeJunct) {
						safeJunctions.add(junct);
					}
				}
				// pick only targets close to at least two safe junctions
				if(safeJunctions.size() >= 2) {
					if(dist < minDistance) {
						safePillWithJuncts = target;
						minDistance = dist;
					}	
				}
			}
		}
		
		int[] path = null; int returnValue = -1;
		if(safePillWithJuncts != -1)
			 path = game.getShortestPath(pos, safePillWithJuncts);
		if(path != null && path.length != 0) {
			returnValue = path[0];
		}

		return returnValue;
	}

	/**
	 * Get a close pill that can be safely eaten
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets available pill indices
	 * @param banned last visited junction
	 * @return first index of the safe path leading to the pill
	 */
	public int getNextSafePill(Game game, int pos, int[] targets, int banned) {
		int pill = -1, closestJunct = -1;
		int dist = 0, minDistance = Integer.MAX_VALUE;
		int tmpJunctDist = 0, minDistanceJunction = Integer.MAX_VALUE;
		int[] junctions = game.getJunctionIndices(); 
		
		// loop over all the targets
		for(int target: targets) {
			// get the distance from the target
			dist = game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade());
			// check if ms pacman reaches the target before the ghost
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
					if(dist > guardDistance && dist+guardDistance <
							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
							) {
						// get the closest junction to the considered pill
						minDistanceJunction = Integer.MAX_VALUE; closestJunct = -1;
						for(int junct: junctions) {
							tmpJunctDist = game.getShortestPathDistance(target, junct, game.getPacmanLastMoveMade());
							if(tmpJunctDist < minDistanceJunction && junct != banned) {
								minDistanceJunction = tmpJunctDist;
								closestJunct = junct;
							}
						}
						// check if after mspacman reaches the target, can reach the closest junction safely before a ghost reaches it
						if(dist < minDistance 
								&& game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), closestJunct, game.getGhostLastMoveMade(ghost)) > 
						game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade())+game.getShortestPathDistance(target, closestJunct)+guardDistance
						&& checkSafeChase(target, pos, game, true)
								) {
							pill = target;
							minDistance = dist;
						}
					}
				}
			}
		}

		int returnValue = -1;
		if(pill != -1) {
			returnValue = game.getShortestPath(pos, pill, game.getPacmanLastMoveMade())[0];
		}

		return returnValue;
	}

	/**
	 * Get the closest available pill to Ms Pac-Man
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets available pill indices
	 * @return index of the closest pill to Ms Pac-Man
	 */
	public int getClosestPill(Game game, int pos, int[] targets) {
	    int minDistance = Integer.MAX_VALUE;
	    int pill = -1;
	    
	    for(int target: targets) {
	      if(game.getShortestPathDistance(pos, target) < minDistance) {
	        minDistance = game.getShortestPathDistance(pos, target);
	        pill = target;
	      }
	    }
	    
	    return pill;
	  }
	
	/**
	 * Get the pills to be eaten after a power pill is eaten
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets available pill indices
	 * @param indexPowerPill index of the last eaten power pill
	 * @return index of the next pill to eat to clean the zone
	 */
	public int cleanCorners(Game game, int pos, int[] targets, int indexPowerPill) {
		int pill = -1;
		double distToPowerpill = 0;
		double minDistance = Integer.MAX_VALUE;
		
		// loop over all the targets and get the closest one in the zone to clean
		for(int target: targets) {
			distToPowerpill = game.getEuclideanDistance(indexPowerPill, target);
			if(distToPowerpill < cleanDistance) {
				if(distToPowerpill < minDistance) {
					pill = target;
					minDistance = distToPowerpill;
				}
			}
		}
		
		return pill;
	}
	
	/**
	 * Gets the target furthest from the closest ghost to Ms PacMan that she can reach before the other ghosts
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets possible targets of the map to reach
	 * @return target first index of the safe path leading to the safe index
	 */
	public int getSafeIndexFarthestToClosestGhost(Game game, int pos, int[] targets) {
		int maxDistance = Integer.MIN_VALUE;
		int safeFurthestIndex = -1;
		int dist = 0, dist2 = 0;
		boolean safeIndex = true;
		
		// check where is the closest ghost
		GHOST closestGhost = getClosestGhost(game, pos);
		int closestGhostIndex = -1;
		if(closestGhost != null)
			closestGhostIndex = game.getGhostCurrentNodeIndex(getClosestGhost(game, pos));

		// loop over all the targets 
		for(int index: targets) {
			safeIndex = true;
			// get the distance from the target, do not consider the last move, it may be good to reverse to stay safe
			dist = game.getShortestPathDistance(pos, index);
			// Consider only targets at a certain distance in order to not get eaten or ambushed if this one is chosen
			if(dist > guardDistance) {
				// check that no non-edible ghost reaches the target before than Ms Pac-Man and that no ghost can intercept Ms Pac-Man
				// in the path
				for(GHOST ghost: GHOST.values()) {
					if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
						if(dist + guardDistance > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), index)) {
							safeIndex = false;
							break;
						}
					}
				}
			}
			else {
				safeIndex = false;
			}
			// Update the chosen target picking the one farthest from the closest ghost
			if(safeIndex) {
				dist2 = game.getShortestPathDistance(pos, index);
				if(dist2 > maxDistance) {
					safeFurthestIndex = index;
					maxDistance = dist2;
				}
			}
		}

		// Return the first index of the shortest path to follow to reach the found target
		int[] path = null; int returnValue = -1;
		if(safeFurthestIndex != -1) {
			 path = game.getShortestPath(pos, safeFurthestIndex);
		}
		if(path != null && path.length != 0) {
			returnValue = path[0];
		}
		else if(path != null && path.length == 0) {
			returnValue = safeFurthestIndex;
		}
		return returnValue;
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
	 * Finds the nearest power pill from MsPacman not reachable by the ghosts
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available power pill indices
	 * @return first index of the safe path leading to the power pill
	 */
	public int trapTheGhosts(Game game, int pos, int[] targets) {
		int minDistance = Integer.MAX_VALUE;
		int bestPill = -1; int returnValue = -1;
		boolean safe = false;
		
		// loop over all power pills
		for(int pill: targets) {
			safe = true;
			// check if a ghost can reach the power pill before than Ms PacMan
			for(GHOST ghost: GHOST.values()) {
				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
					if(game.getShortestPathDistance(pos, pill)+guardDistance > 
					     game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pill, game.getGhostLastMoveMade(ghost))
					     || !checkSafeChase(pill, pos, game, false)
					   ) {
						safe = false;
						break;
					}
				}
			}
			// pick the power pill which is closest to Ms PacMan
			if(safe && game.getShortestPathDistance(pos, pill) < minDistance) {
				minDistance = game.getShortestPathDistance(pos, pill);
				bestPill = pill;
			}
		}
		if(bestPill != -1) {
			returnValue = game.getShortestPath(pos, bestPill)[0];
		}

		return returnValue;
	}
	
	/**
	 * Checks if there is an edible ghost
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return the nearest edible ghost if exists, null otherwise
	 */
	public GHOST nearestEdibleGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values()) {
			int distance=game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
			int time=game.getGhostEdibleTime(ghost);
			if(time > FLASH_TIME_HIGH && distance <= eatDistanceHigh
					|| (time > FLASH_TIME_MEDIUM && time <= FLASH_TIME_HIGH && distance <= EAT_DISTANCE_MEDIUM)
					|| (time >= FLASH_TIME_LOW && time <= FLASH_TIME_MEDIUM && distance <= EAT_DISTANCE_LOW)
					) 
			{
				if(distance<minDistance && checkSafeChase(game.getGhostCurrentNodeIndex(ghost), pos, game, false)) {
					// Be careful to not cross ghosts in the lair when chasing and edible ghost
					if(GHOST_IN_THE_LAIR) {
						int[] pathToMinGhost = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(ghost));
						GameView.addPoints(game, Color.blue, pathToMinGhost);
						boolean safe = true;
						for(int node: pathToMinGhost) {
							if(node == game.getGhostInitialNodeIndex() && game.getShortestPathDistance(pos, game.getGhostInitialNodeIndex()) < guardDistance) {
								safe = false;
								break;
							}
						}
						if(safe) {
							minDistance=distance;
							minGhost=ghost;
						}
					}
					else {
						minDistance=distance;
						minGhost=ghost;
					}
				}
			}
		}
		// code to update GHOST_IN_THE_LAIR
		// consider it in the lair already if distance is < GUARD_DISTANCE
		// NB This method is and must be called at each iteration in heuristic1
		if(!isThereGhostInLair(game))
			GHOST_IN_THE_LAIR = false;
		if(minGhost != null && minDistance < guardDistance)
			GHOST_IN_THE_LAIR = true;
		
		return minGhost;
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
	
	/**
	 * Checks if MsPacman can safely chase a ghost
	 * @param minGhost ghost index to chase
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return true if MsPacman can safely chase ghost, false otherwise
	 */
	public boolean checkSafeChase(int target, int pos, Game game, boolean oriented) {
		// check if the non edible ghost is in the path for going to the minGhost
		int[] path = null;
		if(oriented) {
			path = game.getShortestPath(pos, target, game.getPacmanLastMoveMade());
		}
		else {
			path = game.getShortestPath(pos, target);
		}
		
		// first check if a non edible ghost is in the path
		for(int gIndex: path) {
			for(GHOST ghost : GHOST.values()) {
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost) == 0) {
					if(gIndex == game.getGhostCurrentNodeIndex(ghost)) {
						return false;
					}
				}
			}
		}
		
		// then check if a non edible ghost path to pacman intersects the path to the target
		// if so check if pacman is "faster" than the ghost
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost) == 0) {
				int[] ghostPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost));
				for(int gElem: ghostPath) {
					for(int pElem: path) {
						if(gElem == pElem) {
							// check if the ghost arrives there before pacman, then it is not safe
							if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), gElem, game.getGhostLastMoveMade(ghost)) < 
									game.getShortestPathDistance(pos, gElem, game.getPacmanLastMoveMade())+guardDistance) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	
	/**
	 * Get the ghosts chasing Ms Pac-Man
	 * @param pos current MsPacman position in the maze
	 * @param game game manager instance
	 * @return list containing the chasing ghosts
	 */
	public ArrayList<GHOST> getGhostChasingPacman(int pos, Game game) {
		ArrayList<GHOST> chasers = new ArrayList<GHOST>();

		// get the shortest path from the furthest ghost
		int maxGhostDistance = Integer.MIN_VALUE, distance;		
		GHOST maxGhost = null;
		for(GHOST ghost : GHOST.values()) {
			distance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost));
			// if a ghost is too far do not consider it as a chaser
			if(distance > maxGhostDistance 
					&& distance <= chaseDistance
					&& game.getGhostLairTime(ghost) <= 0 && game.getGhostEdibleTime(ghost) <= 0) {
				maxGhostDistance = distance;
				maxGhost = ghost;
			}
		}
		
		// check if ghosts are chasing MsPacman
		if(maxGhost != null) {
			chasers.add(maxGhost);
			// Careful: may happen some ghosts on the same index of the max ghost
			for(GHOST ghost: GHOST.values()){
				if(ghost != maxGhost && game.getGhostCurrentNodeIndex(ghost) == game.getGhostCurrentNodeIndex(maxGhost))
					chasers.add(ghost);
			}
			int[] path = game.getShortestPath(game.getGhostCurrentNodeIndex(maxGhost), pos, game.getGhostLastMoveMade(maxGhost));
			for(int i: path) {
				for(GHOST ghost: GHOST.values()) {
					if(ghost != maxGhost && i == game.getGhostCurrentNodeIndex(ghost) 
							&& game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
						chasers.add(ghost);
					}
				}
			}
		}
		return chasers;
	}
	
}
