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
	private static final int GUARD_DISTANCE=3; // distance before getting eaten
	
	public MOVE getMove(Game game, long timeDue) 
	{
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
//		System.out.println(game.getPacmanNumberOfLivesRemaining());
		
		myMove = MOVE.NEUTRAL;
		int newPosMsPacman = -1;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		Double[] utility0 = new Double[moves.length];
		Double[] utility1 = new Double[moves.length];

		int i = 0;
		for(MOVE move : moves) {
			newPosMsPacman = game.getNeighbour(posMsPacman, move);
			if(newPosMsPacman != -1) {
				utility0[i] = heuristic0(game, newPosMsPacman, move);
				utility1[i] = heuristic1(game, newPosMsPacman, move);
			}
			i +=1;
		}
		
		// prints for debug
//		System.out.print("heur0: ");
//		for(int x=0; x<moves.length; x++)
//			System.out.print(utility0[x] + " ");
//		System.out.println();
		
//		System.out.print("heur1: ");
//		for(int x=0; x<moves.length; x++)
//			System.out.print(utility1[x] + " ");
//		System.out.println();
		
		
		int bestMoveIndex = 0;
		double max0 = Collections.max(Arrays.asList(utility0));
		double max1 = Collections.max(Arrays.asList(utility1));
		if(max0>max1) {
			for(int j=0; j < moves.length; j++) {
				if(utility0[j] == max0) {
					bestMoveIndex = j;
					System.out.println("heur0 won " + max0 + " " + max1);
					break;
				}
			}
		}
		else 
		{
			for(int j=0; j < moves.length; j++) {
				if(utility1[j] == max1) {
					bestMoveIndex = j;
					System.out.println("heur1 won " + max0 + " " + max1);
					break;
				}
			}
		}

		myMove = moves[bestMoveIndex];
		
					
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
	
	private Double heuristic0(Game game, int pos, MOVE move) {
		Double cumulativePoints = 0.0;
		int current = game.getPacmanCurrentNodeIndex();
		
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
				cumulativePoints += 200;
				GameView.addPoints(game,Color.cyan, game.getShortestPath(current, pos));
				return cumulativePoints;
			}
		}
	
		
		// calculate an A* to predict the path of the ghost
		// check that there is a pill that mspacman can approach without being closed by any ghost
		// check that from that pill there is always a shortest path to a power pill that mspacman can
		// reach before any ghost if they chase
		
		int[] pills=game.getActivePillsIndices();		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
//			if(game.isPillStillAvailable(i))			// if commented ALL POSITIONS CONSIDERED
				targets.add(pills[i]);				
			
		int[] targetsArray=new int[targets.size()];		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		
		
		int i = 0;
		int[][] pathGhosts = new int[4][];
		for(GHOST ghost : GHOST.values()) { 
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
				pathGhosts[i] = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), current, game.getGhostLastMoveMade(ghost));
				GameView.addPoints(game, Color.red, pathGhosts[i]);
				i++;
			}
		}
		
		int dist = 0;
		int safeCloserPill = -1, safeClosestPill = -1, minDistance = 1000000;
		int shortestDist = 10000000;
		ArrayList<Integer> closeSafeJunction=new ArrayList<Integer>();
		
		if(targetsArray.length != 0 && !areAllGhostInLair) {
			int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
			if(neighbouringPills.length != 0) {
				
				for(int pill: targetsArray) {
					dist = game.getShortestPathDistance(pos, pill); 
					
					int[] junctions = game.getJunctionIndices();
					int[] safeJunctions = new int[junctions.length];
					
					int j=0;
					
					for(int junct : junctions) {
//						GameView.addPoints(game,Color.pink, junct);
						
						boolean safeJunct = true;
						
						// Judgement of the goodness of the junction
						for(GHOST ghost : GHOST.values()) { 
							if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
								
								if(dist+game.getShortestPathDistance(pill, junct)+GUARD_DISTANCE > // if Ms Pacman DOESN'T reaches the junction before a ghost
								game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), junct)) {
									safeJunct = false;
								}
								else {
									int[] path = game.getShortestPath(pill, junct);
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
					
					int[] realSafeJunctions = new int[j];
					for(int k=0; k<j; k++) {
						realSafeJunctions[k] = safeJunctions[k];
					}
					GameView.addPoints(game,Color.magenta, realSafeJunctions);
	
					
					closeSafeJunction=new ArrayList<Integer>();

//					// choose the pill nearest to pacman
					// if at least 2 escapes exists around pacman
					// NB PROBABLY BROKEN
//					for(int node: realSafeJunctions) {
//						for(int nodeOth: realSafeJunctions) {
//							if(node != nodeOth) {
//								int distanceJunc = Math.abs(node - nodeOth);
//								int pacmanDist = game.getShortestPathDistance(current, node);
//								if(distanceJunc < 40 && pacmanDist < 45) {
//									GameView.addPoints(game,Color.white, node);
//									closeSafeJunction.add(node);
//								}
//							}
//						}
//					}
					if(realSafeJunctions.length >1) {
//						if(game.getPillIndex(pill) != -1) {
							if(dist < minDistance) {
								safeClosestPill = pill;
								minDistance = dist;
							}
					}
				}
			}
			else {
				System.out.println("(neighbouringPills.length == 0) ");
			}
		}
		System.out.println("min distance pacman =  " + minDistance + " pill index: " + safeClosestPill);
		
		if(safeClosestPill != -1) {
				GameView.addPoints(game,Color.green, game.getShortestPath(current, safeClosestPill));
				cumulativePoints += 50;
		}
//		else {
//			System.out.println("boooooooo ");
//		}

		
		
		

		// points for each near pill
//		int[] pills=game.getPillIndices();		
//		
//		ArrayList<Integer> targets=new ArrayList<Integer>();
//		
//		for(int i=0;i<pills.length;i++)					//check which pills are available			
//			if(game.isPillStillAvailable(i))
//				targets.add(pills[i]);				
//			
//		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
//		
//		for(int i=0;i<targetsArray.length;i++)
//			targetsArray[i]=targets.get(i);
		
//		if(targetsArray.length != 0)
//		{
//			// careful! there might be that the closest pill is next to a power pill and we don't want to eat them in heuristic0
//			// avoid that path
//			boolean goodPath = true; // assuming it is true
////			int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
////			for(int elem : path) {
////				if(game.getPowerPillIndex(elem) != -1) {
////					// there is a powerpill in the path and it is elem
////					// great penalty for this move
////					cumulativePoints -= 400;
////					goodPath = false;
////				}
////			}
//			if(goodPath) {
//				cumulativePoints += 30;
//				// closest pills have higher utility
//				int current = game.getPacmanCurrentNodeIndex();
//				int minDist = game.getShortestPathDistance(current, game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
//				if(minDist>=0 && minDist <2) {
////					System.out.println("pill vicinoo");				
//					cumulativePoints += 10;
//				}
//				else
//				{
////					System.out.println("pill lontano");
//					cumulativePoints += 1;
//				}
//			}
//			
//		}
//		System.out.println("junction?: " + game.isJunction(game.getPacmanCurrentNodeIndex()));
		
//		 neighbouring nodes wrt last move made acquire higher utility
//		 the objective is to clear one area first if ms pacman is safe
//		if(targetsArray.length != 0) {
//			int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH));
//			// se la mossa mi avvicina a dei pill nel vicinato acquista utilit�
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
		
		
		return cumulativePoints;
	}
	
	// Heuristics 1 -> run away from ghosts or try to eat them
	// The path must foresee also the worst case (i.e: the most dangerous move that each ghost can do) 
	// up to X steps in the future, where X can be the distance between Ms Pacman and the ghost
	// NB each ghost should be taken into account before deciding the move
	// Astar path based on the current direction of the ghost, in case of crossing, the worst case is considered 
	private Double heuristic1(Game game, int pos, MOVE move) {
		Double cumulativePoints = 0.0;
		boolean isMsPacManInDanger = true;
		
		
//		// escape from ghosts
//		// if the next move allow me to escape from non edible and close ghosts gain utility
//		// addon: take into account last move of ghosts (i.e: direction) to find the better escape path
//		for(GHOST ghost : GHOST.values()) {
//			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) { // check if edible and not in lair
//				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()) < MIN_DISTANCE) { // check if close
////					System.out.println("Valutazione mossa se mi avvicina o meno al ghost vicino");
//					if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) > 
//						game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()))
//					{
//						cumulativePoints += 100;
//					}
//					else {
//						// penalty if mspacman tries to get closer
//						cumulativePoints -= 100;
//					}
//				}
//			}
//			else if(game.getGhostEdibleTime(ghost)>0) {
//				isMsPacManInDanger = false;
//			}
//		}
		
//		// pills
//		// still is a gain eating pills 
//		int[] pills=game.getPillIndices();		
//		
//		ArrayList<Integer> targets=new ArrayList<Integer>();
//		
//		for(int i=0;i<pills.length;i++)					//check which pills are available			
//			if(game.isPillStillAvailable(i))
//				targets.add(pills[i]);				
//			
//		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
//		
//		for(int i=0;i<targetsArray.length;i++)
//			targetsArray[i]=targets.get(i);
//		
//		int minDist = game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
//		if(minDist <2) {
//			cumulativePoints += 2;
//		}
//		else {
//			int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
//			for(int elem : path) {
//				if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
////					// there is a powerpill in the path and it is elem
//					System.out.println("Path con powerpill heuristic1");
//					cumulativePoints = -1000.0;
//				}
//			}
//			cumulativePoints += 1;
//		}
//		
//		int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
////		// careful, escape from ghosts but they are far so don't eat powerpills	
//		for(int elem : path) {
//			if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
////				// there is a powerpill in the path and it is elem
//				// undo every reward up to now
//				// let heur0 win
//				cumulativePoints = 1.0;
//				System.out.println("Path con pp");
//			}
//		}
//					
//
////		System.out.println("bbbb: " + cumulativePoints);
//		
//		// if the ghosts get closer and are not edible try to eat a power pill
//		// this means that none of my moves get me further away from the ghosts
//		// so cumulativePoints up to now is negative
//		if(cumulativePoints <= 0 && isMsPacManInDanger)
//		{	
//			
//			// great utility in eating the powerpill if it was not already eaten
//			targets.clear();
//			int[] powerPills=game.getPowerPillIndices();
//			
//			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
//				if(game.isPowerPillStillAvailable(i))
//					targets.add(powerPills[i]);	
//			
//			int[] targetsArray2=new int[targets.size()];		//convert from ArrayList to array
//			
//			for(int i=0;i<targetsArray2.length;i++)
//				targetsArray2[i]=targets.get(i);
//			
//			// ghost are near, going closer to powerpill is a gain
//			if(targetsArray2.length != 0 && game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <
//					game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)))
//			{
//				cumulativePoints += 10;
////				System.out.println("Sono in heur1 mi avvicino a powerpill");
//			}
//			
////			//only if ghosts are very close like aggressive
////			for(GHOST ghost : GHOST.values())
////				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
////					if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) < 5)
////						// if the next move makes msPacman closer to a power pill, gain utility
////						if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <
////								game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)))
////						{
////							cumulativePoints += 200;
////						}
//			
//		}
//			
//		
//		
//		
////		//chase ghosts
////		
////		int minDistance=Integer.MAX_VALUE;
////		GHOST minGhost=null;
////		
////		int i = 0;
////		for(GHOST ghost : GHOST.values()) {
////			if(game.getGhostEdibleTime(ghost)>0)
////			{
//////				System.out.println(game.getGhostEdibleTime(ghost));
////				int distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
////				
////				if(distance<minDistance)
////				{
////					minDistance=distance;
////					minGhost=ghost;
////				}
////			}
////		}
////		
////		// careful!!! check if there is a non edible (or almost) ghost between mspacman and the ghost being chased
////		if(minGhost!=null) {
////			while(!checkSafeChase(minGhost, minDistance, pos, game)) {
////				GHOST pastMinGhost = minGhost;
////				int newMinDistance=Integer.MAX_VALUE;
////				for(GHOST ghost : GHOST.values()) {
////					if(ghost != pastMinGhost && game.getGhostEdibleTime(ghost)>0) {
////						int distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
////						if(distance<newMinDistance)
////						{
////							newMinDistance = distance;
////							minGhost=ghost;
////						}
////					}
////				}
////				minDistance = newMinDistance;
////			}
////		}
////	
////		
////		if(minGhost!=null)	//we found an edible ghost
////		{
////
////			// this is the closest ghost to eat
////			cumulativePoints += 100; //overpass heur0 that would be +50 eating pills
////			
////			// nb the ghost may be on a power pill
////			// we must avoid resetting the points 
////			int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(minGhost));
////			for(int elem : path) {
////				if(game.getPowerPillIndex(elem) != -1 && game.isPowerPillStillAvailable(game.getPowerPillIndex(elem))) {
////					// there is a powerpill in the path and it is elem
////					// don't go there
////					System.out.println("ghost on a powerpill");
////					cumulativePoints -= 500;
////				}
////			}
////			
////			// attack it = move in the direction that takes you closer to the ghost
////			if (game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(minGhost)) <
////					game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(minGhost)))
////			{
////				cumulativePoints += 400; //ATTACK!!
////			}
////			// but if pacman is too far to eat and the time gets short, run away
////			// 30 is when the ghosts start flashing
////			else if(game.getGhostEdibleTime(minGhost)<30 
////					&& game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(minGhost)) > 30 // NON SONO SICURA
////					)
////			{
////				cumulativePoints -= 400;
////			}
////		}
//		
		
		return cumulativePoints;
	}
	
	private boolean checkSafeChase(GHOST minGhost, int minDistance, int pos, Game game) {
		
			// check if the non edible ghost ghostnot is in the path for going to the minGhost
			int[] path = game.getShortestPath(pos, game.getGhostCurrentNodeIndex(minGhost));
			for(int gIndex: path)
			{
				for(GHOST ghost : GHOST.values()) {
					if(game.getGhostEdibleTime(ghost)==0) {
						if(gIndex == game.getGhostCurrentNodeIndex(ghost))
						{
							System.out.println("ghost inbetween");
							return false;
						}
					}
				}
			}
			return true;
	}
	
}