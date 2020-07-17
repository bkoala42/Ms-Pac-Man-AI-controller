package pacman.entries.pacman;

import pacman.game.Game;
import pacman.game.GameView;

import java.awt.Color;
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

public class GreedyMsPacManStrategyBan {
	
	private static final int CHASE_DISTANCE = 50;
	private static final int GUARD_DISTANCE = 5;
	private static final int MIN_DISTANCE = 20;
	
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
	
	public ArrayList<Integer> getSafeJunctions(Game game, int pos, int bannedNode, boolean turn) {
		int dist;
		boolean safeJunct;
		ArrayList<Integer> safeJunctions = new ArrayList<Integer>();
		
		int[] junctions = game.getJunctionIndices();
		for(int junct : junctions) {
			safeJunct = true;
			if(!turn)
				dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
			else
				dist = game.getShortestPathDistance(pos, junct);
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
					// a non edible ghost is too close to the considered juction
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
//		Visualize safe junctions
//		int[] safeNodes=new int[safeJunctions.size()];		
//		for(int i=0;i<safeNodes.length;i++)
//			safeNodes[i]=safeJunctions.get(i);
//		GameView.addPoints(game,Color.pink, safeNodes);
		
		return safeJunctions;
	}
	
	public int getSafeEscapeToPillWithJunction(Game game, int pos, int[] targets, int bannedNode) {
		int dist = 0, j = 0;
		boolean safeJunct = true;
		int minDistance = Integer.MAX_VALUE, minDistancePill = Integer.MAX_VALUE;
		int maxDistance = Integer.MIN_VALUE, maxDistancePill = Integer.MIN_VALUE;
		int safePillWithJuncts = -1, safeClosestJunction = -1;
		
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, false);

		ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
		if(safeJunctions.size() > 0) {
			for(int junct: safeJunctions) {
				// reachableJunctions contains all the currently SAFELY REACHEABLE JUNCTIONS
				// choose the closest junction to ms pacman and farthest from the ghost closest to the junction
				dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
				
				// get ghosts distances from ms pacman
				for(GHOST ghost: GHOST.values()) {
					if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
						ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost)));
					}
				}
				if(ghostDistance.isEmpty()) {
					if(dist <= minDistance) {
						minDistance = dist;
						safeClosestJunction = junct;
					}
				}
				else if(dist < minDistance && Collections.min(ghostDistance) >= maxDistance) {
					safeClosestJunction = junct;
					minDistance = dist;
					maxDistance = Collections.min(ghostDistance);
				}
				ghostDistance.clear();
			}
		}
		
//		if(safeJunctions.size() > 0) {
//			for(int junct: safeJunctions) {
//				dist = game.getShortestPathDistance(pos, junct, game.getPacmanLastMoveMade());
//				if(dist < minDistance) {
//					minDistance = dist;
//					safeClosestJunction = junct;
//				}
//			}
//		}
		int chosenPath[] = null; boolean flag = true;
		if(safeClosestJunction != -1 && targets.length != 0) {
//			GameView.addPoints(game, Color.magenta, game.getShortestPath(pos, safeClosestJunction));
			for(int target: targets) {
				flag = true;
				dist = game.getShortestPathDistance(pos, safeClosestJunction, game.getPacmanLastMoveMade()) 
						+ game.getShortestPathDistance(safeClosestJunction, target, game.getPacmanLastMoveMade());

				int[] pathToJunct = game.getShortestPath(pos, safeClosestJunction, game.getPacmanLastMoveMade()); 
				int[] pathToPill = game.getShortestPath(safeClosestJunction, target, game.getPacmanLastMoveMade());
				int[] path = new int[pathToJunct.length + pathToPill.length];
				for (int i = 0; i < pathToJunct.length; ++i) {
					path[i]  = pathToJunct[i];
				}
				for (int i = 0; i < pathToPill.length; ++i) {
					path[pathToJunct.length + i] = pathToPill[i];
				}
				
				// check if a ghost is ambushing in the opposite direction
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
//										GameView.addPoints(game,Color.red, path);
//										GameView.addPoints(game,Color.blue, ghostPath);
										flag = false;
										break;
									}
								}
							}
						}
					}
				}
				
				
				if(dist < minDistancePill && flag) {
					safePillWithJuncts = target;
					minDistancePill = dist;
					chosenPath = path;
				}
			}
		}
		
		
		
		int pillWay = -1;
		if(safeClosestJunction != -1 && safePillWithJuncts != -1) {
			GameView.addPoints(game, Color.cyan, chosenPath);
			
			if(chosenPath.length != 0 && chosenPath[0] != -1)
				pillWay = chosenPath[0];
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
		}
//		if(safePillWithJuncts != -1) {
//			GameView.addPoints(game, Color.cyan, safePillWithJuncts);
//			GameView.addPoints(game, Color.red, safeClosestJunction);
//			System.out.println("Junct: "+safeClosestJunction);
//			System.out.println("Pill: "+safePillWithJuncts);
//		}
		if(pillWay != -1)
			GameView.addPoints(game, Color.red, pillWay);
//		System.out.println(pillWay+"\t"+safeClosestJunction+"\t"+safePillWithJuncts);
		
		return pillWay;
	}
	
	public int getSafeEscapeToClosestJunction(Game game, int pos, int bannedNode) {
		int dist = 0;
		int maxDistance = Integer.MIN_VALUE;
		int minDistance = Integer.MAX_VALUE;
		int safeFarthestJunction = -1;
		
		ArrayList<Integer> safeJunctions = getSafeJunctions(game, pos, bannedNode, true);
		
		if(safeJunctions.size() > 0) {
			for(int junct: safeJunctions) {
				// reachableJunctions contains all the currently SAFELY REACHEABLE JUNCTIONS
				// choose the farthest junction
				dist = game.getShortestPathDistance(pos, junct);
				if(dist > maxDistance && checkSafeChase(junct, pos, game)) {
					safeFarthestJunction = junct;
					maxDistance = dist;
				}
			}
		}
		int[] path = null; int returnValue = -1;
		if(safeFarthestJunction != -1)
			 path = game.getShortestPath(pos, safeFarthestJunction);
		if(path != null && path.length != 0) {
			GameView.addPoints(game, Color.yellow, path);
			returnValue = path[0];
		}

		return returnValue;
	}
	
	public int getSafePillWithJunction(Game game, int pos, int[] targets) {
		int dist = 0, j = 0;
		int[] path = null;
		boolean safeJunct = true;
		int minDistance = Integer.MAX_VALUE;
		int safePillWithJuncts = -1;
		
		ArrayList<Integer> safeJunctions = new ArrayList<Integer>();
		ArrayList<Integer> chosenNodeSafeJunctions = new ArrayList<Integer>();
		
		if(targets.length != 0) {
			for(int target: targets) {
				int[] junctions = game.getJunctionIndices();
				for(int junct : junctions) {
					safeJunct = true;
					dist = game.getShortestPathDistance(pos, target);
					if(dist < MIN_DISTANCE) {
						for(GHOST ghost : GHOST.values()) { 
							if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
								// first check if mspacman can reach the pill before a ghost, then check if
								// a non edible ghost is too close to the considered juction
								if(dist+2*GUARD_DISTANCE > game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target, game.getGhostLastMoveMade(ghost))
								&& (dist+game.getShortestPathDistance(target, junct)+GUARD_DISTANCE > 
									game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost)))
									) {
									safeJunct = false;
								}
								// this junction is a safe place, get the best way to reach it if no ghosts are in the path
								else {
									path = game.getShortestPath(pos, junct);
									for(int elem: path) {
										// check if in the path there is a ghost
										for(GHOST ghost2 : GHOST.values()) {
											if(elem == game.getGhostCurrentNodeIndex(ghost2)) {
												safeJunct = false;
											}
										}
									}
								}
								if(!checkSafeChase(target, pos, game))
									safeJunct = false;
							}
						}
					}
					if(safeJunct) {
						safeJunctions.add(junct);
					}
				}
				
				if(safeJunctions.size() >= 2) {
					if(dist+GUARD_DISTANCE < minDistance) {
						safePillWithJuncts = target;
						minDistance = dist;
						chosenNodeSafeJunctions = safeJunctions;
					}	
				}
			}
		}
//		// Visualize safe junctions
//		int[] safeNodes=new int[chosenNodeSafeJunctions.size()];		
//		for(int i=0;i<safeNodes.length;i++)
//			safeNodes[i]=chosenNodeSafeJunctions.get(i);
//		GameView.addPoints(game,Color.pink, safeNodes);
		
		return safePillWithJuncts;
	}
	
	public int[] getAStarSafeTargets(Game game, int pos, List<Integer> targets) {
		int minDistance = Integer.MAX_VALUE;
		int maxDistance = Integer.MIN_VALUE;
		int[] safeTargets = {-1, -1};
		boolean safeIndex = true;	
		
		int[] safety = new int[targets.size()];
		int j = 0;
		
		for(int i: targets) {
			safeIndex = true;	
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostEdibleTime(ghost)<30 && game.getGhostLairTime(ghost)==0) {
					if(game.getShortestPathDistance(pos, i, game.getPacmanLastMoveMade())+GUARD_DISTANCE > // if Ms Pacman DOESN'T reach the junction before a ghost
						game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), i, game.getGhostLastMoveMade(ghost))) {
						safeIndex = false;
					}
					else {
						int[] path = game.getShortestPath(pos, i);
						for(int elem: path) {
							// check if in the path there is a ghost
							for(GHOST ghost2 : GHOST.values()) {
								if(elem == game.getGhostCurrentNodeIndex(ghost2)) {
									safeIndex = false;
								}
							}
						}
					}
				}
			}
			if(safeIndex) {
				// it's safe
				safety[j] = i;
				j++;
			}
		}
		ArrayList<Integer> realSafeIndex = new ArrayList<Integer>();
		// The array was initialized with length of all junctions
		// Consider only those != 0
		for(int node: safety) {
			for(int nodeOth: safety) {
				if(node != 0 && nodeOth !=0 && node != nodeOth) {
					realSafeIndex.add(node);
				}
			}
		}
		
//		int[] targetsArray=new int[realSafeIndex.size()];		
//		for(int i=0; i<targetsArray.length; i++)
//			targetsArray[i] = realSafeIndex.get(i);
//		
//		// Choose the closest safe junction
//		int testIndex = -1;
//		testIndex = getGreedySafeTarget(game, pos, true, targetsArray);
//		if(testIndex != -1)
//			safeTargets[1] = testIndex;
		
		for(Integer i : realSafeIndex) {
			if(game.getShortestPathDistance(pos, i) < minDistance && checkSafeChase(pos, i, game)) {
				minDistance = game.getShortestPathDistance(pos, i);
				safeTargets[0] = i;
			}
			if(game.getShortestPathDistance(pos, i) > maxDistance && checkSafeChase(pos, i, game)) {
				maxDistance = game.getShortestPathDistance(pos, i);
				safeTargets[1] = i;
			}
		}
		return safeTargets;
	}
	
	
	 
	
	
	
	public int eatPills(Game game, int pos, int[] targets) {
		int dist = 0, minDist = Integer.MAX_VALUE, pill = -1;
		int minDistance = Integer.MAX_VALUE, maxDistance = Integer.MIN_VALUE;
		
		if(targets.length != 0) {
			for(int target: targets) {
				dist = game.getShortestPathDistance(pos, target, game.getPacmanLastMoveMade());
//				if(dist < MIN_DISTANCE) {
					for(GHOST ghost : GHOST.values()) { 
						if(game.getGhostEdibleTime(ghost) < 30 && game.getGhostLairTime(ghost)==0) {
							// check if ms pacman reaches before the ghost
							if(dist+GUARD_DISTANCE < 
									game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost))
								) {
								if(dist < minDistance
//										&& game.getShortestPathDistance(game.getGhostCurrentNodeIndex(getCloserGhost(game, pos)), target) >= maxDistance
//										&& checkSafeChase(target, pos, game)
										) {
									pill = target;
									minDistance = dist;
//									maxDistance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(getCloserGhost(game, pos)), target);
								}
							}
						}
					}
//				}
			}
		}
		
		int returnValue = -1;
		if(pill != -1) {
			returnValue = game.getShortestPath(pos, pill, game.getPacmanLastMoveMade())[0];
		}

		return returnValue;
		
	}
	
	
	
	
	/**
	 * Gets a close index from a set of targets when no safe indices can be found
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available pill indices
	 * @return the closest pill index
	 */
	public int getGreedySafeTarget(Game game, int pos, boolean safeSearch, int[] targets) {
		int minDistance = Integer.MAX_VALUE, maxDistance = Integer.MIN_VALUE;
		int bestPill = -1;

		// Pick the available pill which is maximally far from the closest ghost to it and closest to MsPacman. Given that
		// this pill may not exist the method could be improved returning an approximation of this pill (eg only the maximally
		// close to MsPacman and maximally far from the closest ghost to it, without imposing that MsPacMan reaches it before
		// than the ghosts
		for(int pill: targets) {
			ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
			int dist = game.getShortestPathDistance(pos, pill);
			// get ghosts distances from the pill
			for(GHOST ghost: GHOST.values()) {
				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
					ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos, game.getGhostLastMoveMade(ghost)));
				}
			}
			// when there are no ghosts involved just pick the closest pill (eg all ghosts are in the lair)
			if(ghostDistance.isEmpty()) {
				if(dist < minDistance) {
					bestPill = pill;
					minDistance = dist;
					bestPill = pill;
				}
			}
			else {
				// the pill is a good one only if MsPacman reaches it before than the closest ghost to it
				if(safeSearch) {
					if(dist < minDistance 
							&& dist+GUARD_DISTANCE  < Collections.min(ghostDistance)
							&& Collections.min(ghostDistance)+GUARD_DISTANCE > maxDistance
							&& checkSafeChase(pill, pos, game)
							) {
						bestPill = pill;
						minDistance = dist;
						maxDistance = Collections.min(ghostDistance);
					}
				}
				// take any pill as far as possible from ghosts and closest to MsPacman, without caring of who reaches it first
				else {
					if(dist < minDistance 
							&& Collections.min(ghostDistance) > maxDistance
							&& checkSafeChase(pill, pos, game)) {
						bestPill = pill;
						minDistance = dist;
						maxDistance = Collections.min(ghostDistance);
					}
				}
			}
			ghostDistance.clear();
		}
		return bestPill;
	}
	
	/**
	 * Get a junction unreachable by ghosts so that pacman can be safe
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return safe junction to run away from ghosts
	 */
	public int getEmergencyWay(Game game, int pos, boolean safeSearch) {
		// get all maze indices reachable by pacman
		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();
		// put them all together
		List<Integer> pillsList  = Arrays.stream(pills).boxed().collect(Collectors.toList());
		List<Integer> powerPillsList  = Arrays.stream(powerPills).boxed().collect(Collectors.toList());
		pillsList.addAll(powerPillsList);
		
		int emergencyIndex = -1;
		int maxDistance = Integer.MIN_VALUE, minDistance = Integer.MAX_VALUE;
		int dist = 0;
		
		// The maze index corresponding to the emergency way is computed using the same logic of getClosestPill
		ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
		for(int i: pillsList) {
			dist = game.getShortestPathDistance(pos, i, game.getPacmanLastMoveMade());
			for(GHOST ghost: GHOST.values()) {
				if(game.getGhostEdibleTime(ghost) <= 0 && game.getGhostLairTime(ghost) <= 0) {
					ghostDistance.add(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), i));
				}
			}
			if(ghostDistance.isEmpty()) {
				if(dist < minDistance) {
					emergencyIndex = i;
					minDistance = dist;
				}
			}
			else {
				if(safeSearch) {
					if(dist < minDistance && dist <= Collections.min(ghostDistance)+GUARD_DISTANCE && Collections.min(ghostDistance) > maxDistance) {
						emergencyIndex = i;
						minDistance = dist;
						maxDistance = Collections.min(ghostDistance);
					}
				}
				// take any pill as far as possible from ghosts and closest to MsPacman, without caring of who reaches it first
				else {
					if(dist < minDistance && Collections.min(ghostDistance) > maxDistance) {
						emergencyIndex = i;
						minDistance = dist;
						maxDistance = Collections.min(ghostDistance);
					}
				}
			}
			ghostDistance.clear();
		}
		return emergencyIndex;
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
	 * Finds the nearest power pill from MsPacman furthest from ghosts
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @param targets available power pill indices
	 * @return index of the best power pill
	 */
	public int trapTheGhosts(Game game, int pos, int[] targets) {
		int minDistance = Integer.MAX_VALUE;
		int bestPill = -1; int returnValue = -1;
		boolean safe = false;
		
		for(int pill: targets) {
			safe = true;
			if(game.getShortestPathDistance(pos, pill) < minDistance) {
				for(GHOST ghost: GHOST.values()) {
					if(game.getShortestPathDistance(pos, pill)+GUARD_DISTANCE > 
						game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pill)
						|| !checkSafeChase(pill, pos, game)
					  ) {
						safe = false;
						break;
					}
				}
				if(safe) {
					minDistance = game.getShortestPathDistance(pos, pill);
					bestPill = pill;
				}
			}
		}
		if(bestPill != -1) {
			returnValue = game.getShortestPath(pos, bestPill)[0];
		}

		return returnValue;
	}
//		SortedMap<Integer, Integer> powerPillsDistance = new TreeMap<Integer, Integer>();
//		LinkedList<Integer> ghostDistance = new LinkedList<Integer>();
//		// sort by distance AVAILABLE power pills
//		for(int target: targets) {
//			powerPillsDistance.put(game.getShortestPathDistance(pos, target), target);
//		}
//		// sort ghosts by distance from pacman
//		for(GHOST ghost: GHOST.values()) {
//			if(game.getGhostLairTime(ghost) <= 0)
//				ghostDistance.add(game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost)));
//		}
//		Collections.sort(ghostDistance);
//		// check if there is a power pill reachable with no ghosts in the path only if the furthest ghost is enough close to
//		// MsPacMan, to maximize the chance to make a successful trap (eg all chasing ghosts are near enough) 
//		int bestPill = -1;
//		if(!ghostDistance.isEmpty() && ghostDistance.getLast() <= 2*MIN_DISTANCE) {
//			for(Integer target: powerPillsDistance.values()) {
//				if(checkSafeChase(target, pos, game)) {
//					bestPill = target;
//					break;
//				}
//			}
//		}
//		return bestPill;
//	}
	
	/**
	 * Checks if there is an edible ghost
	 * @param game game manager instance
	 * @param pos current MsPacman position in the maze
	 * @return the nearest edible ghost if exists, null otherwise
	 */
	public GHOST isThereEdibleGhost(Game game, int pos) {
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values()) {
			int distance=game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(ghost));
			if(game.getGhostEdibleTime(ghost) > 30
					|| (game.getGhostEdibleTime(ghost) > 10 && game.getGhostEdibleTime(ghost) < 30 && distance < MIN_DISTANCE/2)
					|| (game.getGhostEdibleTime(ghost) > 5 && game.getGhostEdibleTime(ghost) < 10 && distance < GUARD_DISTANCE)
					) {
				if(distance<minDistance && checkSafeChase(game.getGhostCurrentNodeIndex(ghost), pos, game)) {
					minDistance=distance;
					minGhost=ghost;
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
//			System.out.println(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(maxGhost), pos, game.getGhostLastMoveMade(maxGhost)));
			chasers++;
			// Careful: may happen some ghosts on the same index of the max ghost
			for(GHOST ghost: GHOST.values()){
				if(ghost != maxGhost && game.getGhostCurrentNodeIndex(ghost) == game.getGhostCurrentNodeIndex(maxGhost))
					chasers++;
			}
			int[] path = game.getShortestPath(game.getGhostCurrentNodeIndex(maxGhost), pos, game.getGhostLastMoveMade(maxGhost));
			GameView.addPoints(game, Color.PINK, path);
			for(int i: path) {
				for(GHOST ghost: GHOST.values()) {
//					System.out.println(i+"\t"+game.getGhostCurrentNodeIndex(ghost)+"\t"+ghost);
					if(ghost != maxGhost && i == game.getGhostCurrentNodeIndex(ghost)) {
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
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++++++++++++++++++++++++++++++++++++++++ FROM THIS POINT NOT USED CODE +++++++++++++++++++++++++++++++++++++++++++++
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
		int closestGhostIndex = -1;
		
		if(targets.length > 0) {
			if(!chased) {
				safeJunctions = getSafeJuctions(game, pos, targets);
			}
			else {
				safeJunctions = getSafeJuctionsWhenChased(game, pos, targets);
//				System.out.println("Else chased reached");
			}
		}
		
//		if(safeJunctions != null) {
//			for(Integer target: safeJunctions.keySet()) {
//				if(safeJunctions.get(target).size() >= 2) {
//					dist = game.getShortestPathDistance(pos, target);
//					if(getCloserGhost(game, target) != null) 
//						closestGhostIndex = game.getGhostCurrentNodeIndex(getCloserGhost(game, target));
//					if(closestGhostIndex != -1) {
//						if(dist < game.getShortestPathDistance(closestGhostIndex, target) && dist < minDistance) {
//							minDistance = dist;
//							safeClosestIndex = target;
//							safeClosestJunction = safeJunctions.get(target);
//						}	
//					}
//					else {
//						if(dist < minDistance) {
//							minDistance = dist;
//							safeClosestIndex = target;
//							safeClosestJunction = safeJunctions.get(target);
//						}
//					}
//				}
//			}
//		}
		
		ArrayList<Integer> ghostDistance = new ArrayList<Integer>();
		int maxDistance = Integer.MIN_VALUE;
		int closestGhostindex = -1;
		if(getCloserGhost(game, pos) != null) 
			closestGhostindex = game.getGhostCurrentNodeIndex(getCloserGhost(game, pos));
		if(safeJunctions != null) {
			for(Integer target: safeJunctions.keySet()) {
				if(safeJunctions.get(target).size() >= 2) {
					dist = game.getShortestPathDistance(pos, target);
					if(chased) {
						if(closestGhostindex != -1) {
							if(dist < minDistance &&	
								dist < game.getShortestPathDistance(closestGhostindex, target)) {
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
	
	
	
	
}
