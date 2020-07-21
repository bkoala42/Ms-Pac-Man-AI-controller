package pacman.entries.pacman;

import pacman.game.Game;
import pacman.game.GameView;

import java.awt.Color;
import java.util.ArrayList;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

public class GreedyMsPacManStrategyBan {
	
	private static final int CHASE_DISTANCE = 50;
	private static final int GUARD_DISTANCE = 5;
	private static final int MIN_DISTANCE = 25;
	private static final int CLEAN_DISTANCE = 11;
	private static final int EAT_DISTANCE_HIGH = 30;
	private static final int EAT_DISTANCE_MEDIUM = 15;
	private static final int EAT_DISTANCE_LOW = 10;
	private static final int FLASH_TIME_HIGH = 30;
	private static final int FLASH_TIME_MEDIUM = 15;
	private static final int FLASH_TIME_LOW = 10;
	
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
			// the last visited junction to avoid turning around on place in dangerous situations
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
					if(dist+GUARD_DISTANCE > 
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
//		// Visualize safe junctions
//		int[] safeNodes=new int[safeJunctions.size()];		
//		for(int i=0;i<safeNodes.length;i++)
//			safeNodes[i]=safeJunctions.get(i);
//		GameView.addPoints(game,Color.pink, safeNodes);
		
		return safeJunctions;
	}
	
	public int getSafeEscapeToPillWithJunction(Game game, int pos, int[] targets, int bannedNode) {
		int dist = 0, j = 0; int returnValue = -1;
		boolean safeJunct = true;
		int minDistance = Integer.MAX_VALUE, minDistancePill = Integer.MAX_VALUE;
		int maxDistance = Integer.MIN_VALUE, maxDistancePill = Integer.MIN_VALUE;
		int safePillWithJuncts = -1, safeClosestJunction = -1;
		
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, false);
		ArrayList<GHOST> chasers = getGhostChasingPacman(pos, game);
		boolean enterCorner = true;
		
		for(GHOST ghost: chasers) {
			if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos)>GUARD_DISTANCE) {
					enterCorner = false;
				}
			}
		}
		if(!enterCorner) {
//			System.out.println("caso ghost lontani");
			
		
			// Get the closest junction which can be safely reached
			if(safeJunctions.size() > 0) {
				for(int junct: safeJunctions) {
					dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
					if(dist < minDistance 
							&& checkSafeChase(junct, pos, game, true)
							) {

						minDistance = dist;
						safeClosestJunction = junct;
					}
				}
			}
		
//		safeClosestJunction = getSafeEscapeToClosestJunction(game, pos, bannedNode);
		
		int chosenPath[] = null; 
		boolean flag = true;
		if(safeClosestJunction != -1 && targets.length != 0) {
//			GameView.addPoints(game, Color.magenta, game.getShortestPath(pos, safeClosestJunction));
			chosenPath = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade());
			// for all the available targets pick the closest one which can be reached by MsPacman before than the ghosts
			// considering the composite path using the safe junction
			for(int target: targets) {
				flag = true;
				dist = game.getShortestPathDistance(pos, safeClosestJunction, game.getPacmanLastMoveMade()) 
						+ game.getShortestPathDistance(safeClosestJunction, target);
				
				for(GHOST ghost: GHOST.values()) {
					if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
						if(dist+GUARD_DISTANCE >
							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
//						   || dist+GUARD_DISTANCE >
//							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost))
						) {
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
			int[] pathToPill = game.getShortestPath(safeClosestJunction, safePillWithJuncts);
			int[] path = new int[pathToJunct.length + pathToPill.length];
			for (int i = 0; i < pathToJunct.length; ++i) {
				path[i]  = pathToJunct[i];
			}
			for (int i = 0; i < pathToPill.length; ++i) {
				path[pathToJunct.length + i] = pathToPill[i];
			}
//			GameView.addPoints(game, Color.red, pathToJunct);
//			GameView.addPoints(game, Color.cyan, path);
			
			returnValue = path[0];
//			returnValue = safePillWithJuncts;
		}
		}
		else {
//			System.out.println("caso ghost vicini");
			boolean flag = true;
			if(targets.length != 0) {
				// for all the available targets pick the closest one which can be reached by MsPacman before than the ghosts
				// all ghost chasing are very close, so we don't mind of a safe junction before the pill
				for(int target: targets) {
					flag = true;
					// Let mspacman turn on herself because of random move from ghost
					// the ghost may not follow her but ambush her in the opposite corner of a corridor
					// Then it would be too late to go back
//					dist = game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade());
					dist = game.getShortestPathDistance(pos, target);
					
					for(GHOST ghost: GHOST.values()) {
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							if(dist+GUARD_DISTANCE >
							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
							|| !checkSafeChase(target, pos, game, false)
//							|| dist+GUARD_DISTANCE > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos)
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

//		int[] path = null; int returnValue = -1;
//		if(safeClosestJunction != -1)
//			 path = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade());
//		if(path != null && path.length != 0) {
//			GameView.addPoints(game, Color.red, path[0]);
//			returnValue = path[0];
//		}
		
		return returnValue;
			
			

//				int[] pathToJunct = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade()); 
//				int[] pathToPill = game.getShortestPath(safeClosestJunction, target, game.getPacmanLastMoveMade());
//				int[] path = new int[pathToJunct.length + pathToPill.length];
//				for (int i = 0; i < pathToJunct.length; ++i) {
//					path[i]  = pathToJunct[i];
//				}
//				for (int i = 0; i < pathToPill.length; ++i) {
//					path[pathToJunct.length + i] = pathToPill[i];
//				}
				
//				// check if a ghost is ambushing in the opposite direction
//				for(GHOST ghost : GHOST.values()) {
//					if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost) == 0) {
//						int[] ghostPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost));
//						for(int gElem: ghostPath) {
//							for(int pElem: path) {
//								if(gElem == pElem) {
//									// check if the ghost arrives there before pacman, then it is not safe
//									if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), gElem, game.getGhostLastMoveMade(ghost)) < 
//											game.getShortestPathDistance(pos, gElem, game.getPacmanLastMoveMade())+GUARD_DISTANCE) {
//										// View the intersection
////										GameView.addPoints(game,Color.red, path);
////										GameView.addPoints(game,Color.blue, ghostPath);
//										flag = false;
//										break;
//									}
//								}
//							}
//							if(!flag)
//								break;
//						}
//					}
//				}
								
//				if(dist < minDistancePill && flag) {
//					safePillWithJuncts = target;
//					minDistancePill = dist;
//					chosenPath = path;
//				}
		
//		int pillWay = -1;
//		if(safeClosestJunction != -1 && safePillWithJuncts != -1) {
////			GameView.addPoints(game, Color.cyan, chosenPath);
//			
//			if(chosenPath.length != 0 && chosenPath[0] != -1)
//				pillWay = chosenPath[0];
//			int pathInit = -1;
//			if(pathToJunct.length > 0) 
//				pathInit = pathToJunct[0];
//			if(pathInit != -1 && )
//			int dist2 = game.getShortestPathDistance(game.getNeighbour(pos, move), safeClosestJunction, game.getPacmanLastMoveMade()) 
//					+ game.getShortestPathDistance(safeClosestJunction, safePillWithJuncts, game.getPacmanLastMoveMade());
//			if(dist2 < dist && 
//					pathInit != -1
//					&& move == game.getNextMoveTowardsTarget(pos, pathInit, DM.PATH)) {
//				pillFound = true;
//			}
//		}
		
//		return pillWay;
	}
	
	
	public int getSafeEscapeToClosestJunction(Game game, int pos, int bannedNode) {
		int dist = 0;
		int maxDistance = Integer.MIN_VALUE;
		int minDistance = Integer.MAX_VALUE;
		int safeFarthestJunction = -1;
		
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, false);
		
		GHOST closestGhost = getCloserGhost(game, pos);
		int closestGhostIndex = -1;
		if(closestGhost != null)
			closestGhostIndex = game.getGhostCurrentNodeIndex(getCloserGhost(game, pos));
		
		if(safeJunctions.size() > 0) {
			for(int junct: safeJunctions) {
				// safeJunctions contains all the currently SAFELY REACHEABLE JUNCTIONS
				// choose the farthest junction
				if(closestGhostIndex != -1)
					dist = game.getShortestPathDistance(closestGhostIndex, junct, game.getGhostLastMoveMade(closestGhost));
				else
					dist = game.getShortestPathDistance(pos, junct);
//				System.out.println("Junct "+junct+" "+checkSafeChase(junct, pos, game));
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
//			GameView.addPoints(game, Color.yellow, path);
			returnValue = path[0];
		}
//		else {
//			returnValue = safeFarthestJunction;
//		}

//		return safeFarthestJunction;
		return returnValue;
	}
	
	
	/**
	 * Get the index of a close pill with junctions around it
	 * @param game game manager instance
	 * @param pos current Ms PacMan position
	 * @param targets pill indices
	 * @return a pill index with junctions surrounding it
	 */
	public int getSafePillWithJunction(Game game, int pos, int[] targets) {
		int dist = 0, junctDist = 0; double radiusDistance=0, junctRadiusDist = 0;
		boolean safeJunct = true;
		int minDistance = Integer.MAX_VALUE;
		int safePillWithJuncts = -1;
		
		ArrayList<Integer> safeJunctions = new ArrayList<Integer>();
		ArrayList<Integer> chosenNodeSafeJunctions = new ArrayList<Integer>();
		int[] junctions = game.getJunctionIndices();
		
		for(int target: targets) {
			safeJunctions.clear();
			dist = game.getShortestPathDistance(pos, target);
			radiusDistance = game.getEuclideanDistance(pos, target);
			if(radiusDistance < MIN_DISTANCE) {
//				GameView.addLines(game, Color.magenta, pos, target);
				
			for(int junct : junctions) {
				safeJunct = true;
				
				junctDist = game.getShortestPathDistance(target, junct);
				junctRadiusDist = game.getEuclideanDistance(target, junct);
				
//				if(dist < MIN_DISTANCE && junctDist < MIN_DISTANCE) {
				// This limit is too strict
				if(junctRadiusDist > 2*MIN_DISTANCE)
					safeJunct = false;
				else {
					for(GHOST ghost : GHOST.values()) { 
						if(game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost)==0) {
							// 1. check if mspacman can reach the pill before a ghost
							// 2. check if a non edible ghost is too close to the considered junction
							if(dist+GUARD_DISTANCE > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
							   || dist+junctDist+GUARD_DISTANCE > 
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
			if(safeJunctions.size() >= 2) {
				if(dist < minDistance 
//						&& dist < MIN_DISTANCE
						) {
					safePillWithJuncts = target;
					minDistance = dist;
					chosenNodeSafeJunctions.clear();
					for(int e: safeJunctions)
						chosenNodeSafeJunctions.add(e);
				}	
			}
			}
		}
		
		// Visualize safe junctions
//		int[] safeNodes=new int[chosenNodeSafeJunctions.size()];		
//		for(int i=0;i<safeNodes.length;i++) {
//			safeNodes[i]=chosenNodeSafeJunctions.get(i);
//			GameView.addLines(game,Color.blue, pos, safeNodes[i]);
//		}
//		GameView.addPoints(game,Color.pink, safeNodes);
		
		
//		GameView.addPoints(game,Color.blue, safePillWithJuncts);
		
		int[] path = null; int returnValue = -1;
		if(safePillWithJuncts != -1)
			 path = game.getShortestPath(pos, safePillWithJuncts);
		if(path != null && path.length != 0) {
//			GameView.addPoints(game, Color.blue, path);
			returnValue = path[0];
//			GameView.addPoints(game, Color.magenta, safePillWithJuncts);
		}
//		else {
//			returnValue = safePillWithJuncts;
//		}
		
//		return returnValue;
//		int returnValue = -1;
//		if(safePillWithJuncts != -1) {
//			returnValue = game.getShortestPath(pos, safePillWithJuncts, game.getPacmanLastMoveMade())[0];
//			//				GameView.addPoints(game, Color.magenta, game.getShortestPath(pos, pill, game.getPacmanLastMoveMade()));
//			//				GameView.addPoints(game, Color.yellow, escapeJunct);
//		}

		return returnValue;
	}
	
	public int eatPills(Game game, int pos, int[] targets, int banned) {
		int pill = -1, closestJunct = -1, escapeJunct = -1;
		int dist = 0, minDistance = Integer.MAX_VALUE;
		int tmpJunctDist = 0, minDistanceJunction = Integer.MAX_VALUE;
		int[] junctions = game.getJunctionIndices(); 
		
		if(targets.length != 0) {
			for(int target: targets) {
				dist = game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade());
				for(GHOST ghost : GHOST.values()) { 
					if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
						// check if ms pacman reaches before the ghost
						if(dist > GUARD_DISTANCE && dist+GUARD_DISTANCE <
						   game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
//						   && dist+GUARD_DISTANCE < 
//						   game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost))
						   ) {
							minDistanceJunction = Integer.MAX_VALUE; closestJunct = -1;
							for(int junct: junctions) {
								tmpJunctDist = game.getShortestPathDistance(target, junct, game.getPacmanLastMoveMade());
								if(tmpJunctDist < minDistanceJunction && junct != banned) {
									minDistanceJunction = tmpJunctDist;
									closestJunct = junct;
								}
							}
							if(dist < minDistance 
									// check if after mspacman reaches the target, can reach the closest junction safely before a ghost reaches it
									&& game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), closestJunct, game.getGhostLastMoveMade(ghost)) > 
							game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade())+game.getShortestPathDistance(target, closestJunct)+GUARD_DISTANCE
							//										&& game.getShortestPathDistance(game.getGhostCurrentNodeIndex(getCloserGhost(game, pos)), target) >= maxDistance
																	&& checkSafeChase(target, pos, game, true)
							   ) {
								pill = target;
								minDistance = dist;
								escapeJunct = closestJunct;
							}
						}
					}
				}
			}
		}
		
//		System.out.println("pill: "+pill);
		int returnValue = -1;
		if(pill != -1) {
			returnValue = game.getShortestPath(pos, pill, game.getPacmanLastMoveMade())[0];
//			GameView.addPoints(game, Color.magenta, game.getShortestPath(pos, pill, game.getPacmanLastMoveMade()));
//			GameView.addPoints(game, Color.yellow, escapeJunct);
		}

		return returnValue;
	}
	
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
	
	public int cleanCorners(Game game, int pos, int[] targets, int indexPowerPill) {
		int dist = 0, pill = -1, maxPill = -1;
		double distToPowerpill = 0;
		double minDistance = Integer.MAX_VALUE, maxDistance = Integer.MIN_VALUE;
		
		if(targets.length != 0) {
			for(int target: targets) {
//				dist = game.getShortestPathDistance(pos, target);
				distToPowerpill = game.getEuclideanDistance(indexPowerPill, target);
				if(distToPowerpill < CLEAN_DISTANCE) {
//					for(GHOST ghost : GHOST.values()) { 
//						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							// NB NOT SURE IF WE NEED IT
							// check if ms pacman reaches before the ghost
//							if(dist+GUARD_DISTANCE <
//								    game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
//								) {
//								minDistanceJunction = Integer.MAX_VALUE; closestJunct = -1;
//								for(int junct: junctions) {
//									int w = game.getShortestPathDistance(target, junct, game.getPacmanLastMoveMade());
//									if(w < minDistanceJunction && junct != banned) {
//										minDistanceJunction = w;
//										closestJunct = junct;
//									}
//										
//								}
//								System.out.println("distance: "+distToPowerpill);
								if(distToPowerpill < minDistance 
//										&& game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), closestJunct) > 
//								game.getShortestPathDistance(pos, closestJunct, game.getPacmanLastMoveMade())+GUARD_DISTANCE
										) {
									pill = target;
									minDistance = distToPowerpill;
//									escapeJunct = closestJunct;
								}
								// only for visualization
								if(distToPowerpill > maxDistance) {
									maxDistance = distToPowerpill;
									maxPill = target;
								}
//							}
//						}
//					}
				}
			}
		}
//		if(maxPill != -1)
//			GameView.addLines(game,Color.CYAN,pos,maxPill);
		
		return pill;
	}
	
	
	
	
	/**
	 * Gets the target furthest from the closest ghost to Ms PacMan that she can reach before the other ghosts
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets possible targets of the map to reach
	 * @return target farthest from the closest ghost that Ms PacMan reaches before the other ghosts
	 */
	public int getGreedySafeTarget(Game game, int pos, int[] targets) {
		int maxDistance = Integer.MIN_VALUE;
		int safeFurthestIndex = -1;
		int dist = 0, dist2 = 0;
		boolean safeIndex = true;
		
		// check where is the closest ghost
		GHOST closestGhost = getCloserGhost(game, pos);
		int closestGhostIndex = -1;
		if(closestGhost != null)
			closestGhostIndex = game.getGhostCurrentNodeIndex(getCloserGhost(game, pos));

		// loop over all the targets 
		for(int index: targets) {
			safeIndex = true;
			// get the distance from the target, do not consider the last move, it may be good to reverse to stay safe
			dist = game.getShortestPathDistance(pos, index);
			// EDIT
			// Consider only targets at a certain distance in order to not get eaten or ambushed if this one is chosen
			if(dist > GUARD_DISTANCE) {
				// check that no non-edible ghost reaches the target before than Ms Pac-Man and that no ghost can intercept Ms Pac-Man
				// in the path
				for(GHOST ghost: GHOST.values()) {
					if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
						if(dist + GUARD_DISTANCE > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), index)  
								//	game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), index, game.getGhostLastMoveMade(ghost))  
//								|| !checkSafeChase(index, pos, game, false)
								) {
							safeIndex = false;
							break;
						}
					}
				}
			}
			else {
				// ELSE DO NOT CONSIDER THIS NODE AS A GOOD ESCAPE
				safeIndex = false;
			}
			// Update the chosen target picking the one farthest from the closest ghost
			if(safeIndex) {
//				GameView.addLines(game, Color.cyan, pos, index);
//				if(closestGhostIndex != -1)
//					dist2 = game.getShortestPathDistance(closestGhostIndex, index);
//				else {
//					dist2 = game.getShortestPathDistance(pos, index);
//				}
				
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
//			 System.out.println("path len: "+path.length+ "\t"+"index: "+safeFurthestIndex+ "\t"+"ghost: "+game.getGhostCurrentNodeIndex(closestGhost)+"\t"+"pacman: "+pos);
//			 GameView.addPoints(game, Color.yellow, safeFurthestIndex);
		}
		if(path != null && path.length != 0) {
//			GameView.addPoints(game, Color.red, path);
			returnValue = path[0];
		}
		else if(path != null && path.length == 0) {
//			System.out.println("path was len 0, safe index: "+safeFurthestIndex);
			returnValue = safeFurthestIndex;
		}
		else if(path == null) {
//			System.out.println("path is null");
		}
//		return safeFurthestIndex;
		return returnValue;

//		
//		
//		
//		for(int pill: targets) {
//			dist = game.getShortestPathDistance(pos, pill, game.getPacmanLastMoveMade());
//			// get ghosts distances from the pill
//			for(GHOST ghost: GHOST.values()) {
//				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
//					ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pill, game.getGhostLastMoveMade(ghost)));
//				}
//			}
//			// when there are no ghosts involved just pick the closest pill (eg all ghosts are in the lair)
//			if(ghostDistance.isEmpty()) {
//				if(dist < minDistance) {
//					bestPill = pill;
//					minDistance = dist;
//					bestPill = pill;
//				}
//			}
//			else {
//				// the pill is a good one only if MsPacman reaches it before than the closest ghost to it
//				if(safeSearch) {
//					if(dist > maxDistance2 
//							&& dist+GUARD_DISTANCE  < Collections.min(ghostDistance)
////							&& Collections.min(ghostDistance) > maxDistance
//							&& checkSafeChase(pill, pos, game)
//							) {
//						bestPill = pill;
////						minDistance = dist;
//						maxDistance2 = dist;
//						maxDistance = Collections.min(ghostDistance);
//					}
//				}
//				// take any pill as far as possible from ghosts and closest to MsPacman, without caring of who reaches it first
//				else {
//					if(dist < minDistance 
//							&& Collections.min(ghostDistance) > maxDistance
//							&& checkSafeChase(pill, pos, game)) {
//						bestPill = pill;
//						minDistance = dist;
//						maxDistance = Collections.min(ghostDistance);
//					}
//				}
//			}
//			ghostDistance.clear();
//		}
//		if(bestPill != -1) 
//			GameView.addPoints(game, Color.blue, game.getShortestPath(pos, bestPill));
//		return bestPill;
	}
	

	/**
	 * Get the closest ghost
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return the closest ghost 
	 */
	public GHOST getCloserGhost(Game game, int pos) {
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
	 * @return index of the power pill
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
					if(game.getShortestPathDistance(pos, pill)+GUARD_DISTANCE > 
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
			if(time > FLASH_TIME_HIGH && distance <= EAT_DISTANCE_HIGH
					|| (time > FLASH_TIME_MEDIUM && time <= FLASH_TIME_HIGH && distance <= EAT_DISTANCE_MEDIUM)
					|| (time >= FLASH_TIME_LOW && time <= FLASH_TIME_MEDIUM && distance <= EAT_DISTANCE_LOW)
					) 
			{
				if(distance<minDistance && checkSafeChase(game.getGhostCurrentNodeIndex(ghost), pos, game, false)) {
					// sometimes mspacman chases an edible ghost and a non edible ghost appears from the lair and she dies
					if(isThereGhostInLair(game)) {
						int[] pathToMinGhost = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(ghost));
						GameView.addPoints(game, Color.blue, pathToMinGhost);
						boolean safe = true;
						for(int node: pathToMinGhost) {
							if(node == game.getGhostInitialNodeIndex() && game.getShortestPathDistance(pos, game.getGhostInitialNodeIndex()) < GUARD_DISTANCE) {
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
									game.getShortestPathDistance(pos, gElem, game.getPacmanLastMoveMade())+GUARD_DISTANCE) {
								// View the intersection
//								GameView.addPoints(game,Color.red, path);
//								GameView.addPoints(game,Color.blue, ghostPath);
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
			distance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost));
			// if a ghost is too far do not consider it as a chaser
			if(distance > maxGhostDistance 
					&& distance <= CHASE_DISTANCE
					&& game.getGhostLairTime(ghost) <= 0 && game.getGhostEdibleTime(ghost) <= 0) {
				maxGhostDistance = distance;
				maxGhost = ghost;
			}
		}
		
		// check if ghosts are chasing MsPacman
		if(maxGhost != null) {
//			System.out.println("Max ghost chaser distance: "+game.getShortestPathDistance(game.getGhostCurrentNodeIndex(maxGhost), pos, game.getGhostLastMoveMade(maxGhost)));
			chasers++;
			// Careful: may happen some ghosts on the same index of the max ghost
			for(GHOST ghost: GHOST.values()){
				if(ghost != maxGhost && game.getGhostCurrentNodeIndex(ghost) == game.getGhostCurrentNodeIndex(maxGhost))
					chasers++;
			}
			int[] path = game.getShortestPath(game.getGhostCurrentNodeIndex(maxGhost), pos, game.getGhostLastMoveMade(maxGhost));
//			GameView.addPoints(game, Color.PINK, path);
			for(int i: path) {
				for(GHOST ghost: GHOST.values()) {
//					System.out.println(i+"\t"+game.getGhostCurrentNodeIndex(ghost)+"\t"+ghost);
					if(ghost != maxGhost && i == game.getGhostCurrentNodeIndex(ghost) 
							&& game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
//						System.out.println(ghost);
//						System.out.println(i+"\t"+game.getGhostCurrentNodeIndex(ghost)+"\t"+ghost);
						chasers++;
					}
				}
			}
		}
//		System.out.println(chasers);
		return chasers;
	}
	/*
	 * Returns the list of the chasing ghosts
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
					&& distance <= CHASE_DISTANCE
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
	
	
	public int getGreedySafeClosestTarget(Game game, int pos, int[] targets) {
		int minDistance = Integer.MAX_VALUE;
		int pill = -1;


		ArrayList<Integer> safeIndices = new ArrayList<Integer>();
		int dist = 0;
		boolean safeIndex = true;
		for(int index: targets) {
			safeIndex = true;
			dist = game.getShortestPathDistance(pos, index);
			if(dist < MIN_DISTANCE) {
				for(GHOST ghost: GHOST.values()) {
					if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
						if(
							dist + GUARD_DISTANCE >
						 	game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), index, game.getGhostLastMoveMade(ghost))
						  ) {
							safeIndex = false;
							break;
						}
					}
				}
				if(!checkSafeChase(index, pos, game, false))
					safeIndex = false;
				if(safeIndex) {
					safeIndices.add(index);
				}
			}
		}
		
		if(safeIndices.size() > 0) {
			for(int index: safeIndices) {
				dist = game.getShortestPathDistance(pos, index);
				if(dist < minDistance 
						) {
					pill = index;
					minDistance = dist;
				}
			}
		}
		return pill;

	}
	
}
