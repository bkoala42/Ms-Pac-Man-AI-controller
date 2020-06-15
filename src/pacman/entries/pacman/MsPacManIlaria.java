package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 */
public class MsPacManIlaria extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=30;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	
	public MOVE getMove(Game game, long timeDue) 
	{
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
		myMove = MOVE.NEUTRAL;
		int newPosMsPacman = -1;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		Double[] utility0 = new Double[moves.length];
		Double[] utility1 = new Double[moves.length];

		int i = 0;
		for(MOVE move : moves) {
			newPosMsPacman = game.getNeighbour(posMsPacman, move);
			if(newPosMsPacman != -1) {
				utility0[i] = heuristic0(game, newPosMsPacman);
				utility1[i] = heuristic1(game, newPosMsPacman);
				
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
	private Double heuristic0(Game game, int pos) {
		Double cumulativePoints = 0.0;
		boolean safe = true;
		// 10 points for each ghost far that is not watching towards pacman because they are going away
		// else it is risky because they are approaching
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) >= MIN_DISTANCE)
					cumulativePoints += 10;
				else {
					// penalty if one is too close
					cumulativePoints -= 400;
					safe = false;
				}
//			else if (game.getGhostLairTime(ghost)==0)
//			// if ghost are still in the lair 
//				cumulativePoints += 100;
		
		
		if(safe) {
		// points for each near pill
		int[] pills=game.getPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);				
			
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		if(targetsArray.length != 0)
		{
			// careful! there might be that the closest pill is next to a power pill and we don't want to eat them in heuristic0
			// avoid that path
			boolean goodPath = true; // assuming it is true
			int[] path = game.getShortestPath(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
			for(int elem : path) {
				if(game.getPowerPillIndex(elem) != -1) {
					// there is a powerpill in the path and it is elem
					// great penalty for this move
					cumulativePoints -= 400;
					goodPath = false;
				}
			}
			if(goodPath) {
				cumulativePoints += 100;
//				// closest pills have higher utility
//				int minDist = game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
//				if(minDist>=0 && minDist <=5) {
//									
//					cumulativePoints += 100;
//				}
//				else
//				{
//					System.out.println("pill lontano");
//					cumulativePoints += 10;
//				}
			}
			
		}
		
		// penalty for taking power pill
		targets.clear();
		int[] powerPills=game.getPowerPillIndices(); // indices of the maze, 97,102 etc
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);	
		
		int[] targetsArray2=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray2.length;i++)
			targetsArray2[i]=targets.get(i);
		// if the next move implies eating a power pill, great penalty
		if (targetsArray2.length != 0 && game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <=5)
		{
			System.out.println("cieooo");
			cumulativePoints -= 500;
			
		}
		else {
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
//					if(game.getPowerPillIndex(elem) != -1) {
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
		}
		}
		
		
		return cumulativePoints;
	}
	
	// Heuristics 1 -> run away from ghosts or try to eat them
	// The path must foresee also the worst case (i.e: the most dangerous move that each ghost can do) 
	// up to X steps in the future, where X can be the distance between Ms Pacman and the ghost
	// NB each ghost should be taken into account before deciding the move
	// Astar path based on the current direction of the ghost, in case of crossing, the worst case is considered 
	private Double heuristic1(Game game, int pos) {
		Double cumulativePoints = 0.0;
		
		// if the next move allow me to escape from all ghosts gain utility
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()) < MIN_DISTANCE) {
					System.out.println("fin qui tutto ok");
					if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) > 
						game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()))
					{
						cumulativePoints += 100;
					}
					else {
						// penalty if mspacman tries to get closer
						cumulativePoints -= 100;
					}
				}
			}
		}
		
//		System.out.println("bbbb: " + cumulativePoints);
		
		// if the ghosts get closer try to eat a power pill
		// this means that none of my moves get me further away from the ghosts
		// so cumulativePoints up to now is negative
		if(cumulativePoints <= 0)
		{
			// still is a gain eating pills 
			int[] pills=game.getPillIndices();		
			
			ArrayList<Integer> targets=new ArrayList<Integer>();
			
			for(int i=0;i<pills.length;i++)					//check which pills are available			
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);				
				
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			
			int minDist = game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
			if(minDist <5) {
				cumulativePoints += 10;
			}
			
			// great utility in eating the powerpill
			targets.clear();
			int[] powerPills=game.getPowerPillIndices();
			
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
				if(game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);	
			
			int[] targetsArray2=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray2.length;i++)
				targetsArray2[i]=targets.get(i);
			// if the next move implies eating a power pill, great gain
			if (targetsArray2.length != 0 && game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <=1)
				cumulativePoints += 500;
			
			// only if ghosts are very close like aggressive
//			for(GHOST ghost : GHOST.values())
//				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
//					if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) < 5)
//						// if the next move makes msPacman closer to a power pill, gain utility
//						if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)) <
//								game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,targetsArray2,DM.PATH)))
//						{
//							cumulativePoints += 200;
//						}
			
		}
			
		//chase ghosts
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>0)
			{
//				System.out.println(game.getGhostEdibleTime(ghost));
				int distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
			// but if pacman is too far to eat and the time gets short, run away
			else if(game.getGhostEdibleTime(ghost)<50 && game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost)) <=2)
			{
				minGhost = null;
//				cumulativePoints -= 100;
			}
		if(minGhost!=null)	//we found an edible ghost
			// this is the closest ghost to eat
			cumulativePoints += 100;
		
		
		return cumulativePoints;
	}
	
	
	
}