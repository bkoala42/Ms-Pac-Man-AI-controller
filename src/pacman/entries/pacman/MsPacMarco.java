package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/*
 */
public class MsPacMarco extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=40;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	private MsPacManStrategy strategy;
	
	public MsPacMarco() {
		this.strategy = new MsPacManStrategy();
	}
	
	
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
			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
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
		int m = 0;
		int[] returnValues = new int[2];
		int bestMove = -1, shortestDist = Integer.MAX_VALUE;
		ArrayList<Integer> cumulativePoints = new ArrayList<Integer>(moves.length);
		ArrayList<Integer> moveSafeJunctions =new ArrayList<Integer>();
		
		int status = 0;
		int[] pills = null;
		int safeClosestIndex = -1, minDistance = Integer.MAX_VALUE, dist = 0;
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		ArrayList<Integer> safeClosestJunction = new ArrayList<Integer>();
		
		for(MOVE move: moves) {
			pills = strategy.pillTargets(game, false);
			// if there are pills use them as pivot for safe junctions
			if(pills.length > 0) {
				//System.out.println("Ci sono pill");
				safeJunctions = strategy.getSafeJuctions(game, current, pills);
			}
			
			//System.out.println(safeJunctions.size());
			// for all the targets find the best one in terms of distance
			for(Integer target: safeJunctions.keySet()) {
//				System.out.println(safeJunctions.get(0));
				if(safeJunctions.get(target).size() > 0) {
					dist = game.getShortestPathDistance(current, target);
					if(dist < minDistance) {
						safeClosestIndex = target;
						minDistance = dist;
						safeClosestJunction = safeJunctions.get(target);
					}
				}
			}
			
			// There is a safe way to follow
			if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
				status = 0;
				cumulativePoints.add(50);
				GameView.addPoints(game,Color.lightGray, game.getShortestPath(current, safeClosestIndex));
			}
			// MsPacman was not able to find a safe path, reward the moves that take away from the closest ghost
			else {
//				GHOST closestGhost = strategy.getCloserGhost(game, current);
//				if(closestGhost != null && 
//						move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
//					status = 1;
//					cumulativePoints.add(25);
//					//GameView.addPoints(game,Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
//				}
//				else {
					status = 2;
					cumulativePoints.add(-25);
//				}
			}
			m++;
			//System.out.println("min distance pacman =  " + minDistance + " pill index: " + safeClosestIndex + " move: " + move.toString());
		}
		
		// compare the results of the moves
		bestMove = cumulativePoints.indexOf(Collections.max(cumulativePoints));
		
		// Visualize safe junctions
		int[] safeNodes=new int[safeClosestJunction.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunction.get(i);
		GameView.addPoints(game,Color.pink, safeNodes);
		
		// FOR DEBUG
//		switch(status) {
//		case 0:
//			System.out.println("Seguo la via sicura verso i pill");
////			System.out.println(safeClosestIndex);
//			break;
//		case 1:
////			GameView.addPoints(game,Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
//			System.out.println("Mi allontano dal ghost");
//			break;
//		case 2:
//			System.out.println("Non so che fare");
//			break;
//		case 4:
//			System.out.println("Sono inseguito cerco una via di fuga");
//			break;
//		case 5:
//			System.out.println("Sono inseguito ma non so che fare");
//			break;
//		}
		
		returnValues[0] = cumulativePoints.get(bestMove);
		returnValues[1] = bestMove;
		return returnValues;
	}
		
	
	// Heuristics 1 -> run away from ghosts or try to eat them
	// The path must foresee also the worst case (i.e: the most dangerous move that each ghost can do) 
	// up to X steps in the future, where X can be the distance between Ms Pacman and the ghost
	// NB each ghost should be taken into account before deciding the move
	// Astar path based on the current direction of the ghost, in case of crossing, the worst case is considered 
	private int[] heuristic1(Game game, MOVE[] moves) {
		
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		ArrayList<Integer> cumulativePoints = new ArrayList<Integer>(moves.length);
		
		int current = game.getPacmanCurrentNodeIndex();
		int safeClosestIndex = -1, minDistance = Integer.MAX_VALUE, dist = 0;
		ArrayList<Integer> safeClosestJunction = new ArrayList<Integer>();
		ArrayList<Integer> moveSafeJunctions =new ArrayList<Integer>();
		int[] powerPills = null, pills = null;
		boolean areTherePowerPills;
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		
		int status = 0;
		
		for(MOVE move: moves) {
			powerPills = strategy.pillTargets(game, true);
			//pills = game.getPillIndices();
			// if there are power pills use them as pivot for safe junctions, otherwise just find a safe place
			if(powerPills.length > 0) {
				//System.out.println("Ci sono powerpill");
				safeJunctions = strategy.getSafeJuctions(game, current, powerPills);
				areTherePowerPills = true;
			}
			// use pills (all pills not the available) as pivots for safe junctions
//			else {
//				//System.out.println("Non ci sono powerpill");
//				safeJunctions = strategy.getSafeJuctions(game, current, pills);
//				areTherePowerPills = false;
//			}
			
			// for all the targets find the best one in terms of distance
			if(safeJunctions != null) {
				for(Integer target: safeJunctions.keySet()) {
					if(safeJunctions.get(target).size() > 0) {
						dist = game.getShortestPathDistance(current, target);
						if(dist < minDistance) {
							safeClosestIndex = target;
							minDistance = dist;
							safeClosestJunction = safeJunctions.get(target);
						}
					}
				}
			}
			
			// find the most "interesting" ghosts that are available
			GHOST closestGhost = strategy.getCloserGhost(game, current);
			GHOST edibleGhost = strategy.isThereEdibleGhost(game, current);
			
			// check if MsPacman is chased, this is important to change strategy and fool ghosts
			boolean chased = false;
			if(strategy.isMsPacManChased(current, game) >= 2) {
				chased = true;
			}
			
			// MsPacman follows two simple rules: eat ghosts if possible, maybe trap them, or go away
			// when there is no chance of eating, possibly staying around power pills
			// MsPacman is chased and tries to fool the ghosts
			if(chased && edibleGhost == null) {
				int trapPowerPill = strategy.trapTheGhosts(game, current, powerPills);
				if(trapPowerPill != -1 && move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
					GameView.addPoints(game, Color.cyan, game.getShortestPath(current, trapPowerPill));
					status = 0;
					cumulativePoints.add(100);
				}
			}
			// There is a safely edible ghost, go and catch it
			else if(edibleGhost != null && strategy.checkSafeChase(edibleGhost, current, game) &&
					move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
				status = 1;
				cumulativePoints.add(200);
				GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
			}
			// no edible ghosts, reward the move that leads in a safe zone around power pills
			else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
				status = 2;
				cumulativePoints.add(80);
				GameView.addPoints(game, Color.orange, game.getShortestPath(current, safeClosestIndex));
			}
			// no good moves available, just try to get away from the closest ghost
			else if(closestGhost != null && move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)){
				status = 3;
				cumulativePoints.add(40);
				//GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
			}
			// don't know what to do, worst situation
			else {
				status = 4;
				cumulativePoints.add(-100);
			}
			
//			// if MsPacman is chased use a less conservative strategy. If there are power pills try to trap
//			// the ghosts and eat all them at once. Otherwise run away as fast as possible
//			if(strategy.isMsPacManChased(current, game) >= 2) {
//				// if there are no edible ghosts try to take them in the zone of a power pill to fool them
//				GHOST closestGhost = strategy.getCloserGhost(game, current);
//				GHOST edibleGhost = strategy.isThereEdibleGhost(game, current);
//				if(edibleGhost == null) {
//					// reward the move that leads in the direction of the ghosts trap
//					int trapPowerPill = strategy.trapTheGhosts(game, current, powerPills);
//					if(trapPowerPill != -1 && move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
////						GameView.addPoints(game, Color.cyan, game.getShortestPath(current, trapPowerPill));
//						status = 0;
//						cumulativePoints.add(100);
//					}
//					// reward the move that takes away from the closest ghost
//					else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
//						status = 1;
//						cumulativePoints.add(75);
//					}
//					else if(move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
//						status = 2;
//						cumulativePoints.add(75);
//					}
//					// dangerous situation, no way to save
//					else {
//						status = 3;
//						cumulativePoints.add(-100);
//					}
//				}
//				// there is a safely edible ghost, catch it
//				else if(strategy.checkSafeChase(edibleGhost, current, game) &&
//						move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
//						status = 4;
//						cumulativePoints.add(200);
//				}
//				// there are no ghosts catchable but a safe path for powerpills is known
//				else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
//					status = 5;
//					cumulativePoints.add(75);
//				}
//				// dangerous situation
//				else {
//					status = 6;
//					cumulativePoints.add(-200);
//				}
//			}
//			// if MsPacman is not chased and there are edible ghosts try to move towards them,
//			// otherwise just go away from near ghosts
//			else {
//				GHOST closestGhost = strategy.getCloserGhost(game, current);
//				// there is an edible ghost and no other non edible ghosts are in the path towards it
//				GHOST edibleGhost = strategy.isThereEdibleGhost(game, current);
//				if(edibleGhost != null && strategy.checkSafeChase(edibleGhost, current, game) &&
//						move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
//					GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
//					status = 7;
//					cumulativePoints.add(200);
//				}
//				// there is no safe edible ghost, but a safe path is available
//				else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
//					GameView.addPoints(game, Color.orange, game.getShortestPath(current, safeClosestIndex));
//					status = 8;
//					cumulativePoints.add(75);
//				}
//				// just try to get away from the closest ghost
//				else if(move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)){
//					//GameView.addPoints(game, Color.cyan, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
//					status = 9;
//					cumulativePoints.add(60);
//				}
//				// dangerous situation, dunno what to do
//				else {
//					status = 10;
//					cumulativePoints.add(-200);
//				}
//			}

			m++;
		}
		
//		// FOR DEBUG
		switch(status) {
//		case 0:
//			System.out.println("Vado ad intrappolare i ghost");
//			break;
//		case 1:
//			System.out.println("Vado a caccia di ghost");
//			break;
//		case 2:
//			System.out.println("Cerco una via di fuga sicura");
//			break;
//		case 3:
//			System.out.println("Mi allontano dal ghost più vicino");
//			break;
//		case 4:
//			System.out.println("Non so che fare");
//			break;
//		case 5:
//			System.out.println("Sono inseguito ma non so che fare");
//			break;
//		case 6:
//			System.out.println("Non sono inseguito cerco di mangiare un ghost");
//			break;
//		case 7:
//			System.out.println("Non sono inseguito cerco di mangiare un ghost");
//			break;
//		case 8:
//			System.out.println("Non sono inseguito cerco una via di fuga safe");
//			break;
//		case 9:
//			System.out.println("Non sono inseguito cerco di scappare dal ghost più vicino");
//			break;
//		case 10:
//			System.out.println("Non so che fare");
//			break;
		}
		
		// PEZZA PERCHE' A QUESTO PUNTO CUMULATIVEPOINTS A VOLTE E' VUOTO (????)
		if(cumulativePoints.isEmpty()) {
			cumulativePoints.add(-100);
		}
		// compare the results of the moves
		bestMove = cumulativePoints.indexOf(Collections.max(cumulativePoints));
		
		// Visualize safe junctions
		int[] safeNodes=new int[safeClosestJunction.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunction.get(i);
		GameView.addPoints(game, Color.magenta, safeNodes);
		
		returnValues[0] = cumulativePoints.get(bestMove);
		returnValues[1] = bestMove;
		return returnValues;
		
	}
	
}