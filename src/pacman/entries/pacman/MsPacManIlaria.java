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
import pacman.game.GameView;

/*
 */
public class MsPacManIlaria extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
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
		System.out.print("heur0: ");
		for(int x=0; x<moves.length; x++)
			System.out.print(utility0[x] + " ");
		System.out.println();
		
		System.out.print("heur1: ");
		for(int x=0; x<moves.length; x++)
			System.out.print(utility1[x] + " ");
		System.out.println();
		
		
		int bestMoveIndex = 0;
		double max0 = Collections.max(Arrays.asList(utility0));
		double max1 = Collections.max(Arrays.asList(utility1));
		if(max0>max1) {
			for(int j=0; j < moves.length; j++) {
				if(utility0[j] == max0) {
					bestMoveIndex = j;
					break;
				}
			}
		}
		else 
		{
			for(int j=0; j < moves.length; j++) {
				if(utility1[j] == max1) {
					bestMoveIndex = j;
					break;
				}
			}
		}

		myMove = moves[bestMoveIndex];
					
		
		
		
//		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
//		for(GHOST ghost : GHOST.values())
//			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
//				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), currentNodeIndex)<MIN_DISTANCE)
//					return game.getNextMoveAwayFromTarget(currentNodeIndex, game.getGhostCurrentNodeIndex(ghost),DM.PATH);
		
		//Strategy 2: find the nearest edible ghost and go after them 
//		int minDistance=Integer.MAX_VALUE;
//		GHOST minGhost=null;		
//		
//		for(GHOST ghost : GHOST.values())
//			if(game.getGhostEdibleTime(ghost)>0)
//			{
//				int distance=game.getShortestPathDistance(currentNodeIndex,game.getGhostCurrentNodeIndex(ghost));
//				
//				if(distance<minDistance)
//				{
//					minDistance=distance;
//					minGhost=ghost;
//				}
//			}
//		
//		if(minGhost!=null)	//we found an edible ghost
//			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
//		int[] pills=game.getPillIndices();
//		int[] powerPills=game.getPowerPillIndices();		
//		
//		ArrayList<Integer> targets=new ArrayList<Integer>();
//		
//		for(int i=0;i<pills.length;i++)					//check which pills are available			
//			if(game.isPillStillAvailable(i))
//				targets.add(pills[i]);
//		
//		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
//			if(game.isPowerPillStillAvailable(i))
//				targets.add(powerPills[i]);				
//		
//		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
//		
//		for(int i=0;i<targetsArray.length;i++)
//			targetsArray[i]=targets.get(i);
		
		
		
		//
		//add the path that Ms Pac-Man is following
//		GameView.addPoints(game,Color.CYAN,game.getShortestPath(currentNodeIndex,
//							game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,targetsArray,DM.PATH)));
		
				
		//return the next direction once the closest target has been identified
//		return game.getNextMoveTowardsTarget(currentNodeIndex, game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,targetsArray,DM.PATH),DM.PATH);
		
		return myMove;
	}
	
	
	
	// Heuristic 0 -> safe path
	// If all ghosts are "far enough" and there are pills to eat, this heuristic gains a high value
	private Double heuristic0(Game game, int pos) {
		Double cumulativePoints = 0.0;
		
		// 10 points for each ghost far that is not watching towards pacman because they are going away
		// else it is risky because they are approaching
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) > MIN_DISTANCE)
					cumulativePoints += 10;
				else {
					// penalty if one is too close
					cumulativePoints -= 100;
				}
		
		// points for each near pill
		int[] pills=game.getPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);				
			
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		// closest pills have higher utility
		int minDist = game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH));
		if(minDist>=0 && minDist <2) {
			cumulativePoints += 10;
		}
		else 
		{
			cumulativePoints += 1;
		}
		// neighbouring nodes wrt last move made acquire higher utility
		// the objective is to clear one area first if ms pacman is safe
		int[] neighbouringPills = game.getNeighbouringNodes(game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH));
		// se la mossa mi avvicina a dei pill nel vicinato acquista utilità
		if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)) > 
			game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,neighbouringPills,DM.PATH)))
			cumulativePoints += 50;
		
		// penalty for taking power pill
		targets.clear();
		int[] powerPills=game.getPowerPillIndices();
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);	
		
		targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		// if the next move implies eating a power pill, great penalty
		if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH)) <=1)
			cumulativePoints -= 100;
		
		
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
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pos) > 
					game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()))
					cumulativePoints += 100;
				else {
					// penalty if one is too close
					cumulativePoints -= 100;
				}
		
		// if the ghosts get closer try to eat a power pill
		// this means that none of my moves get me further away from the ghosts
		// so cumulativePoints up to now is negative
		if(cumulativePoints < 0)
		{
			// great utility in eating the powerpill
			ArrayList<Integer> targets=new ArrayList<Integer>();
			int[] powerPills=game.getPowerPillIndices();
			
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
				if(game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);	
			
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			// if the next move implies eating a power pill, great gain
			if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH)) <=1)
				cumulativePoints += 500;
			// if the next move makes msPacman closer to a power pill, gain utility
			if (game.getShortestPathDistance(pos, game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH)) <
					game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(pos,targetsArray,DM.PATH)))
				cumulativePoints += 100;
			
		}
			
//		int minDistance=Integer.MAX_VALUE;
//		GHOST minGhost=null;		
//		
//		for(GHOST ghost : GHOST.values())
//			if(game.getGhostEdibleTime(ghost)>0)
//			{
////				System.out.println(game.getGhostEdibleTime(ghost));
//				int distance=game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost));
//				
//				if(distance<minDistance)
//				{
//					minDistance=distance;
//					minGhost=ghost;
//				}
//			}
//			// but if pacman is too far to eat and the time gets short, run away
//			else if(game.getGhostEdibleTime(ghost)<50 && game.getShortestPathDistance(pos,game.getGhostCurrentNodeIndex(ghost)) <=2)
//			{
//				minGhost = null;
////				cumulativePoints -= 100;
//			}
//		if(minGhost!=null)	//we found an edible ghost
//			// this is the closest ghost to eat
//			cumulativePoints += 100;
		
		
		return cumulativePoints;
	}
	
	
	
}