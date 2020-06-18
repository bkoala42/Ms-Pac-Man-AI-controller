package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/*
 */
public class MsPacManIlaria extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=40;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	
	public MOVE getMove(Game game, long timeDue) 
	{
		long startTime = java.lang.System.currentTimeMillis();
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
//		System.out.println(game.getPacmanNumberOfLivesRemaining());
		
		myMove = MOVE.NEUTRAL;
		int newPosMsPacman = -1;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
		utility0 = heuristic0(game, moves);		
		utility1 = heuristic1(game, moves);

//		int i = 0;
//		for(MOVE move : moves) {
//			newPosMsPacman = game.getNeighbour(posMsPacman, move);
//			if(newPosMsPacman != -1) {
////				utility0[i] = heuristic0(game, newPosMsPacman, move);
//				utility1[i] = heuristic1(game, newPosMsPacman, move);
//			}
//			i +=1;
//		}
		
		
		
//		int bestMoveIndex = 0;
//		double max0 = Collections.max(Arrays.asList(utility0));
//		double max1 = Collections.max(Arrays.asList(utility1));
//		if(max0>max1) {
//			for(int j=0; j < moves.length; j++) {
//				if(utility0[j] == max0) {
//					bestMoveIndex = j;
////					System.out.println("heur0 won " + max0 + " " + max1);
//					break;
//				}
//			}
//		}
//		else 
//		{
//			for(int j=0; j < moves.length; j++) {
//				if(utility1[j] == max1) {
//					bestMoveIndex = j;
////					System.out.println("heur1 won " + max0 + " " + max1);
//					break;
//				}
//			}
//		}
		
		if(utility0[0] > utility1[0]) {
			System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
			myMove = moves[utility0[1]];
		}
		else {
			System.out.println("heur1 won " + utility0[0] + " move: " + moves[utility0[1]]);
			myMove = moves[utility1[1]];
		}

		
//		System.out.println("Time: " + (java.lang.System.currentTimeMillis() - startTime));
		return myMove;
	}
	
	
	
	// Heuristic 0 -> safe path
	// If all ghosts are "far enough" and there are pills to eat, this heuristic gains a high value
	// Which pill to eat must be chosen ensuring that Ms Pacman is not closed in a path from ghosts, 
	// this means that there is always a junction reachable from the pill that Ms Pacman can reach 
	// before any ghost in their shortest path. To foresee the path of the ghosts, the last move
	// made by ghost must be taken into account and an A* is used to calculate all the paths to the
	// junctions and then estimate a safe one. From the junction, Ms Pacman must be able to reach a 
	// power pill with her ShortestPath before any ghost
	
	private int[] heuristic0(Game game, MOVE[] moves) {
		int current = game.getPacmanCurrentNodeIndex();
		int pos = 0;
		int m = 0;
		int[] returnValues = new int[2];
		int bestMove = -1, shortestDist = 10000000;
		int[] cumulativePoints = new int[moves.length];
		ArrayList<Integer> moveSafeJunctions =new ArrayList<Integer>();
		
		// evaluate for each move
		for(MOVE move : moves) {
			pos = game.getNeighbour(current, move);
		
			// if all ghosts are in the Lair move towards the farthest pill apart from lair
			boolean areAllGhostInLair = true;
			for(GHOST ghost : GHOST.values()) { 
				if(game.getGhostLairTime(ghost)==0) {
					areAllGhostInLair = false;
				}
			}
			if(areAllGhostInLair) {
				// if ghosts are still in Lair start to eat pills far from the Lair
				if(move == game.getNextMoveAwayFromTarget(current, game.getCurrentMaze().lairNodeIndex, DM.PATH))
				{
					cumulativePoints[m] += 200;
					GameView.addPoints(game,Color.cyan, game.getShortestPath(current, pos));
					returnValues[0] = cumulativePoints[m];
					returnValues[1] = m;
					return returnValues;
				}
			}
	
		
			// calculate an A* to predict the path of the ghost
			// check that there is a pill that mspacman can approach without being closed by any ghost
			// check that from that pill there is always a shortest path to a power pill that mspacman can
			// reach before any ghost if they chase
			
			int[] pills=game.getPillIndices();		
			ArrayList<Integer> targets=new ArrayList<Integer>();
			
			for(int i=0;i<pills.length;i++)					//check which pills are available			
//				if(game.isPillStillAvailable(i))			// if commented ALL POSITIONS CONSIDERED
					targets.add(pills[i]);				
				
			int[] targetsArray=new int[targets.size()];		
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			
			
			
	//		int i = 0;
	//		int[][] pathGhosts = new int[4][];
	//		for(GHOST ghost : GHOST.values()) { 
	//			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
	//				pathGhosts[i] = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), current, game.getGhostLastMoveMade(ghost));
	//				GameView.addPoints(game, Color.red, pathGhosts[i]);
	//				i++;
	//			}
	//		}
			
			int dist = 0;
			int safeClosestPill = -1, minDistance = 1000000;
			ArrayList<Integer> realSafeJunction=new ArrayList<Integer>();
			ArrayList<Integer> chosenNodeSafeJunctions =new ArrayList<Integer>();
			
			if(targetsArray.length != 0 && !areAllGhostInLair) {
				int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
				if(neighbouringPills.length != 0) {
					
					for(int pill: targetsArray) {
						
						int[] junctions = game.getJunctionIndices();
						int[] safeJunctions = new int[junctions.length];
						
						int j=0;
						
						for(int junct : junctions) {
//							GameView.addPoints(game,Color.pink, junct);
							
							boolean safeJunct = true;
							dist = game.getShortestPathDistance(pos, pill); 
							
							// Judgement of the goodness of the junction
							for(GHOST ghost : GHOST.values()) { 
								if(game.getGhostEdibleTime(ghost)<30 && game.getGhostLairTime(ghost)==0) {
									
									if(dist+game.getShortestPathDistance(pill, junct)+GUARD_DISTANCE > // if Ms Pacman DOESN'T reaches the junction before a ghost
									game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct, game.getGhostLastMoveMade(ghost))) {
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
											// ATTENZIONE PER IL MOMENTO LASCIAMO PERDERE
	//										// check if in the path there is a power pill
	//										if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
	//											// there is a powerpill in the path and it is elem
	//											System.out.println("weeeee");
	//											safeJunct = false;
	//										}
										}
									}
								}
							}
							if(safeJunct) {
	//							// it's safe
								safeJunctions[j] = junct;
								j++;
							}
						}
						
						//PEZZA
						//there is a problem in Java the array is initialized but not completely filled, it is filled up to j
						
//						int[] realSafeJunctions = new int[j+1];
//						int o = 0;
//						for(int k=0; k<safeJunctions.length; k++) {
//							if( safeJunctions[k] != 0) {
//								realSafeJunctions[o] = safeJunctions[k];
//								o++;
//							}
//						}
						
//						GameView.addPoints(game,Color.magenta, safeJunctions);
		
						
						realSafeJunction=new ArrayList<Integer>();
	
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
						
						if(realSafeJunction.size() >= 2) {
							for(int i=0;i<pills.length;i++) {			//check if the pill is still available		
								if(pills[i] == pill) {
									if(game.isPillStillAvailable(i)) {
										if(dist < minDistance) 
										{
											safeClosestPill = pill;
											minDistance = dist;
											chosenNodeSafeJunctions = realSafeJunction;
//											System.out.println("current pill: " + pill + " n° of safe junctions: " + closeSafeJunction.size());
										}
									}
								}
							}
						}
					}
				}
				else {
					System.out.println("(neighbouringPills.length == 0) ");
				}
				
				//System.out.println("min distance pacman =  " + minDistance + " pill index: " + safeClosestPill + " move: " + move.toString());
				
				if(safeClosestPill != -1) {
//						GameView.addPoints(game,Color.green, game.getShortestPath(current, safeClosestPill));
						cumulativePoints[m] += 50;
				}
			}

			
			// results for current move
			// compare the distance of pacman to the pill
			// the shortest one is the correct
			if(minDistance < shortestDist) {
				moveSafeJunctions = chosenNodeSafeJunctions;
				shortestDist = minDistance;
				bestMove = m;
			}
			
			m++;
		} // end for moves
		
		// Visualize safe junctions
		int[] safeNodes=new int[moveSafeJunctions.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=moveSafeJunctions.get(i);
//		GameView.addPoints(game,Color.magenta, safeNodes);
		
		returnValues[0] = cumulativePoints[bestMove];
		returnValues[1] = bestMove;
		
		return returnValues;
		
		
		
		
//		 neighbouring nodes wrt last move made acquire higher utility
//		 the objective is to clear one area first if ms pacman is safe
//		if(targetsArray.length != 0) {
//			int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH));
//			// se la mossa mi avvicina a dei pill nel vicinato acquista utilità
//			if(neighbouringPills.length != 0 && game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)) > 
//				game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)))
//				cumulativePoints += 50;
//			
//			// but if there is a power pill dont go there
//			int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH));
//			for(int elem : path) {
//				if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
//					// there is a powerpill in the path and it is elem
//					// great penalty for this move
//					System.out.println("weeeee");
//					cumulativePoints -= 400;
//				}
//			}
//		}
//		else {
//			// no more pills left!!!
//			// to complete the level eat all power pills
//			int[] ppills=game.getPowerPillIndices();		
//			
//			ArrayList<Integer> ptargets=new ArrayList<Integer>();
//			
//			for(int i=0;i<ppills.length;i++)					//check which pills are available			
//				if(game.isPillStillAvailable(i))
//					ptargets.add(ppills[i]);				
//				
//			int[] ptargetsArray=new int[ptargets.size()];		//convert from ArrayList to array
//			
//			for(int i=0;i<ptargetsArray.length;i++)
//				ptargetsArray[i]=ptargets.get(i);
//			if(ptargetsArray.length != 0) {
//				int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),ptargetsArray,DM.PATH));
//				
//				if(neighbouringPills.length != 0 && game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)) > 
//					game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)))
//					cumulativePoints += 50;
//			}
//			
//		}
		
		
		
		// penalty for taking power pill
//		targets.clear();
//		int[] powerPills=game.getPowerPillIndices(); // indices of the maze, 97,102 etc
//		
//		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
//			if(game.isPowerPillStillAvailable(i))
//				targets.add(powerPills[i]);	
//		
//		int[] targetsArray2=new int[targets.size()];		//convert from ArrayList to array
//		
//		for(int i=0;i<targetsArray2.length;i++)
//			targetsArray2[i]=targets.get(i);
//		// if the next move implies eating a power pill, great penalty
//		if (targetsArray2.length != 0 && game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <=5)
//		{
//			System.out.println("cieooo");
//			cumulativePoints -= 100;
//			
//		}
		
		// **********************
//		else {
//			// there may be that the closest pill is the one near the power pill
//			// we need a shortcut to go to the closest pill
//			// so get the path from the pos to the pill, if it includes a power pill avoid it
//			// use getShortestPath
//			
//			ArrayList<Integer> targetss=new ArrayList<Integer>();
//			
//			for(int i=0;i<pills.length;i++)					//check which pills are available			
//				if(game.isPillStillAvailable(i))
//					targetss.add(pills[i]);
//			
//			
//			int[] targetssArray=new int[targets.size()];		//convert from ArrayList to array
//			for(int i=0;i<targetssArray.length;i++)
//				targetssArray[i]=targetss.get(i);
//			
//			boolean goodPath = false;
//			
//			while(!goodPath) {
//				int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetssArray,DM.PATH));
//				
//				for(int elem : path) {
//					if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(elem)) {
////						// there is a powerpill in the path and it is elem
////						// delete it from the list and recompute the path
////						targetss.remove(game.getClosestNodeIndexFromNodeIndex(pos,targetssArray,DM.PATH));
////						goodPath = false;
////						System.out.println("Path con pp");
////						break;
//						// there is a powerpill in the path and it is elem
//						// great penalty for this move
//						cumulativePoints -= 200;
//						System.out.println("Path con pp");
//					}
//				}
//				goodPath = true;
//				System.out.println("Path buono");
//			}
//		}
		// **********************
//		}
		
	
	}
	
	// Heuristics 1 -> run away from ghosts or try to eat them
	// The path must foresee also the worst case (i.e: the most dangerous move that each ghost can do) 
	// up to X steps in the future, where X can be the distance between Ms Pacman and the ghost
	// NB each ghost should be taken into account before deciding the move
	// Astar path based on the current direction of the ghost, in case of crossing, the worst case is considered 
	private int[] heuristic1(Game game, MOVE[] moves) {
		int m = 0;
		int[] returnValues = new int[2];
		int bestMove = -1, shortestDist = 10000000;
		int[] cumulativePoints = new int[moves.length];
		int bestScore = Integer.MIN_VALUE;
		int current = game.getPacmanCurrentNodeIndex();
		
		for(MOVE move: moves) {
			boolean isMsPacManInDanger = true;
			// Am I pursued?
			if(isMsPacManChased(current, game) > 2) {
				System.out.println("Mi inseguono!!!");
				int bestPowerPill = bestPowerPill(current, game);
				if(bestPowerPill != -1) {
					if(game.getShortestPathDistance(current, bestPowerPill) < MIN_DISTANCE) {
						System.out.println("Vado ad intrappolare i ghost alla svelta");
						cumulativePoints[m] += 500;
					}
					else {
						System.out.println("Vado ad intrappolare i ghost");
						cumulativePoints[m] += 200;
					}
				}
			}
			
//			// if the next move allow me to escape from non edible and close ghosts gain utility
//			// addon: take into account last move of ghosts (i.e: direction) to find the better escape path
//			for(GHOST ghost : GHOST.values()) {
//				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) { // check if edible and not in lair
//					if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()) < MIN_DISTANCE) { // check if close
////						System.out.println("Valutazione mossa se mi avvicina o meno al ghost vicino");
//						if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), current) > 
//							game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()))
//						{
//							cumulativePoints[m] += 100;
//						}
//						else {
//							// penalty if mspacman tries to get closer
//							cumulativePoints[m] -= 100;
//						}
//					}
//				}
//				else if(game.getGhostEdibleTime(ghost)>0) {
//					isMsPacManInDanger = false;
//				}
//			}
//			
//			// pills
//			// still is a gain eating pills 
//			int[] pills=game.getPillIndices();		
//			
//			ArrayList<Integer> targets=new ArrayList<Integer>();
//			
//			for(int i=0;i<pills.length;i++)					//check which pills are available			
//				if(game.isPillStillAvailable(i))
//					targets.add(pills[i]);				
//				
//			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
//			
//			for(int i=0;i<targetsArray.length;i++)
//				targetsArray[i]=targets.get(i);
//			
//			int minDist = game.getShortestPathDistance(current, game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
//			if(minDist <2) {
//				cumulativePoints[m] += 2;
//			}
//			else {
//				int[] path = game.getShortestPath(current, game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
//				for(int elem : path) {
//					if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
////						// there is a powerpill in the path and it is elem
//						//System.out.println("Path con powerpill heuristic1");
//						cumulativePoints[m] -= -1000.0;
//					}
//				}
//				cumulativePoints[m] += 1;
//			}
//			
//			// if the ghosts get closer and are not edible try to eat a power pill
//			// this means that none of my moves get me further away from the ghosts
//			// so cumulativePoints up to now is negative
//			if(cumulativePoints[m] <= 0 && isMsPacManInDanger)
//			{	
//				
//				// great utility in eating the powerpill if it was not already eaten
//				targets.clear();
//				int[] powerPills=game.getPowerPillIndices();
//				
//				for(int i=0;i<powerPills.length;i++)			//check with power pills are available
//					if(game.isPowerPillStillAvailable(i))
//						targets.add(powerPills[i]);	
//				
//				int[] targetsArray2=new int[targets.size()];		//convert from ArrayList to array
//				
//				for(int i=0;i<targetsArray2.length;i++)
//					targetsArray2[i]=targets.get(i);
//				
//				// ghost are near, going closer to powerpill is a gain
//				if(targetsArray2.length != 0 && game.getShortestPathDistance(current, game.getClosestNodeIndexFromNodeIndex(current,targetsArray2,DM.PATH)) <
//						game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(current,targetsArray2,DM.PATH)))
//				{
//					cumulativePoints[m] += 10;
////					System.out.println("Sono in heur1 mi avvicino a powerpill");
//				}
//				
////				//only if ghosts are very close like aggressive
////				for(GHOST ghost : GHOST.values())
////					if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
////						if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) < 5)
////							// if the next move makes msPacman closer to a power pill, gain utility
////							if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <
////									game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)))
////							{
////								cumulativePoints += 200;
////							}
//				
//			}
//				
//			//chase ghosts
//			
//			int minDistance=Integer.MAX_VALUE;
//			GHOST minGhost=null;
//			
//			int i = 0;
//			for(GHOST ghost : GHOST.values()) {
//				if(game.getGhostEdibleTime(ghost)>0)
//				{
////					System.out.println(game.getGhostEdibleTime(ghost));
//					int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
//					
//					if(distance<minDistance)
//					{
//						minDistance=distance;
//						minGhost=ghost;
//					}
//				}
//			}
//			
//			if(minGhost!=null)	//we found an edible ghost
//			{
//				// this is the closest ghost to eat
//				cumulativePoints[m] += 100; //overpass heur0 that would be +50 eating pills
//				
//				// nb the ghost may be on a power pill
//				// we must avoid resetting the points 
//				int[] path = game.getShortestPath(current, game.getGhostCurrentNodeIndex(minGhost));
//				for(int elem : path) {
//					if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
//						// there is a powerpill in the path and it is elem
//						// don't go there
//						//System.out.println("ghost on a powerpill");
//						cumulativePoints[m] -= 500;
//					}
//				}
//				
//				// attack it = move in the direction that takes you closer to the ghost
//				if (game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(minGhost)) <
//						game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(minGhost))
//						&& checkSafeChase(minGhost, current, game))
//				{
//					if(checkSafeChase(minGhost, current, game))
//						cumulativePoints[m] += 400; //ATTACK!!
//					else
//						cumulativePoints[m] -= 400; //Be careful some non edible ghost is on the path
//				}
//				// but if pacman is too far to eat and the time gets short, run away
//				// 30 is when the ghosts start flashing
//				else if(game.getGhostEdibleTime(minGhost)<30 
//						&& game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(minGhost)) > 30 // NON SONO SICURA
//						)
//				{
//					cumulativePoints[m] -= 400;
//				}
//			}
			if(cumulativePoints[m] > bestScore) {
				bestScore = cumulativePoints[m];
				bestMove = m;
			}
		}
		returnValues[0] = cumulativePoints[bestMove];
		returnValues[1] = bestMove;
		return returnValues;
	}
	
	private boolean checkSafeChase(GHOST minGhost, int pos, Game game) {
		
			// check if the non edible ghost ghostnot is in the path for going to the minGhost
			int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(minGhost));
			for(int gIndex: path)
			{
				for(GHOST ghost : GHOST.values()) {
					if(game.getGhostEdibleTime(ghost)==0) {
						if(gIndex == game.getGhostCurrentNodeIndex(ghost))
						{
//							System.out.println("ghost inbetween");
							return false;
						}
					}
				}
			}
			return true;
	}
	
	private int isMsPacManChased(int pos, Game game) {
		int chasers = 0;

		// get the shortest path from the furthest ghost
		int maxGhostDistance=Integer.MIN_VALUE, distance;		
		GHOST maxGhost = null;
		for(GHOST ghost : GHOST.values()) {
			distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
			if(distance > maxGhostDistance) {
				maxGhostDistance = distance;
				maxGhost=ghost;
			}
		}
		
		// check if ghosts are chasing MsPacman
		//System.out.println(game.getGhostCurrentNodeIndex(maxGhost));
		try {
			int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(maxGhost));
			for(int i: path) {
				for(GHOST ghost: GHOST.values()) {
					if(i == game.getGhostCurrentNodeIndex(ghost) && game.getGhostLairTime(ghost) <= 0)
						chasers += 1;
				}
			}
			//System.out.println("Sono inseguito da "+chasers);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			//System.out.println("Rotto");
		}
		return chasers;
	}
	
	private int bestPowerPill(int pos, Game game) {
		int minGhostDistance = Integer.MAX_VALUE;
		int bestPill = -1;
		// check which power pill is the nearest
		ArrayList<Integer> targets=new ArrayList<Integer>();
		int[] powerPills=game.getPowerPillIndices();
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);	
		
		int[] distance=new int[targets.size()];		    //shortest path distances of MsPacman from power pills 
		int[] distanceGhost=new int[targets.size()]; 	//shortest path distances of ghosts from power pills 
		
		// compute shortest path distance to power pills for MsPacman and the ghosts
		for(int i=0;i<distance.length;i++) {
			for(GHOST ghost: GHOST.values()) {
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), targets.get(i)) < minGhostDistance)
					minGhostDistance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), targets.get(i));
			}
			distanceGhost[i] = minGhostDistance;
			minGhostDistance = Integer.MAX_VALUE;
			distance[i]=game.getShortestPathDistance(pos, targets.get(i));
		}
		
		// if exists, pick as target a power pill nearer to MsPacman and far from ghosts
		int diff = 0;
		for(int i = 0; i < distance.length; i++) {
			if(distance[i] < distanceGhost[i] && (distanceGhost[i] - distance[i]) > diff) {
				bestPill = targets.get(i);
				diff = distanceGhost[i] - distance[i];
			}
		}
		return bestPill;
	}
	
}