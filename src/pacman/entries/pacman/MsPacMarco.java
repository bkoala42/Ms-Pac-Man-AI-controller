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
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	private static final int LIAR_DISTANCE=5;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	private MsPacManStrategy strategy;
	private int[] targetDistance;				// array storing the distance from: trap power pill, edible ghost, safe closest index,
												// non edible ghost to avoid undesired reversal in heuristic 1
	private int targetID;
	
	public MsPacMarco() {
		this.strategy = new MsPacManStrategy();
		this.targetDistance = new int[4];
		this.targetDistance[0] = Integer.MAX_VALUE;
		this.targetDistance[1] = Integer.MAX_VALUE;
		this.targetDistance[2] = Integer.MAX_VALUE;
//		this.targetDistance[3] = Integer.MIN_VALUE;
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
		
		if(utility0[0] > utility1[0]) {
			System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
			myMove = moves[utility0[1]];
		}
		else {
			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
			myMove = moves[utility1[1]];
		}
//		System.out.println(targetID);

		
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
		
		int status = 0;
		int[] pills = null;
		int safeClosestIndex = -1, minDistance = Integer.MAX_VALUE, dist = 0;
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		ArrayList<Integer> safeClosestJunction = new ArrayList<Integer>();
		
		pills = strategy.pillTargets(game, false);
		// if there are pills use them as pivot for safe junctions
		if(pills.length > 0) {
			safeJunctions = strategy.getSafeJuctions(game, current, pills);
		}
		
		// for all the targets find the best one in terms of distance
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
		
		
		for(MOVE move: moves) {
			// the first check is on the presence of ghosts in liar to avoid instant kill
			if(strategy.isThereGhostInLiar(game) && 
					game.getShortestPathDistance(current, strategy.liarIndex(game)) < LIAR_DISTANCE &&
					move == game.getNextMoveTowardsTarget(current, strategy.getClosestPowerPillIndex(game, current), DM.PATH)) {
//				System.out.println(game.getShortestPathDistance(current, strategy.liarIndex(game)-1));
				cumulativePoints.add(75);
			}
			// There is a safe way to follow
			else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
				status = 0;
				cumulativePoints.add(50);
				GameView.addPoints(game,Color.lightGray, game.getShortestPath(current, safeClosestIndex));
			}
			// MsPacman was not able to find a safe path, reward the moves that takes towards the next pill
			else {
				int closestPill = strategy.getClosestPill(game, current, pills);
				if(closestPill != -1 && move == game.getNextMoveTowardsTarget(current, closestPill, DM.PATH)) {
					status = 1;
					cumulativePoints.add(30);
				}
				// situation to avoid, MsPacman does not know what to do
				else {
					status = 2;
					cumulativePoints.add(-25);
				}
			}
			m++;
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
		int[] powerPills = null, pills = null;
		Map<Integer, ArrayList<Integer>> safeJunctions = null;
		
		// ghost targets to take care of
		GHOST closestGhost, edibleGhost;
		
		powerPills = strategy.pillTargets(game, true);
		// if there are power pills use them as pivot for safe junctions, otherwise just find a safe place
		if(powerPills.length > 0) {
			safeJunctions = strategy.getSafeJuctions(game, current, powerPills);
		}
		
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
		
		for(MOVE move: moves) {
			// find the most "interesting" ghosts that are available
			closestGhost = strategy.getCloserGhost(game, current);
			edibleGhost = strategy.isThereEdibleGhost(game, current);
			int trapPowerPill = strategy.trapTheGhosts(game, current, powerPills);
			
			// check if MsPacman is chased, this is important to change strategy and fool ghosts
			boolean chased = false;
			if(strategy.isMsPacManChased(current, game) >= 2) {
				chased = true;
			}
			
			// MsPacman follows two simple rules: eat ghosts if possible, maybe trap them, or go away
			// when there is no chance of eating, possibly staying around power pills
			// the first check is on the presence of ghosts in liar to avoid instant kill
			if(strategy.isThereGhostInLiar(game) && 
					game.getShortestPathDistance(current, strategy.liarIndex(game)-1) < LIAR_DISTANCE &&
					move == game.getNextMoveTowardsTarget(current, strategy.getClosestPowerPillIndex(game, current), DM.PATH)) {
				cumulativePoints.add(220);
			}
			// MsPacman is chased and tries to fool the ghosts
			else if(chased && edibleGhost == null && trapPowerPill != -1 && strategy.checkSafeChase(trapPowerPill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
					GameView.addPoints(game, Color.cyan, game.getShortestPath(current, trapPowerPill));
					cumulativePoints.add(100);
			}
			// There is a safely edible ghost, go and catch it
			else if(edibleGhost != null && strategy.checkSafeChase(game.getGhostCurrentNodeIndex(edibleGhost), current, game) &&
					move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
				cumulativePoints.add(200);
				GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
			}
			// no edible ghosts, reward the move that leads in a safe zone around power pills
			else if(safeClosestIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndex, DM.PATH)) {
				cumulativePoints.add(80);
				GameView.addPoints(game, Color.orange, game.getShortestPath(current, safeClosestIndex));
			}
			// no good moves available, just try to get away from the closest ghost
			else if(closestGhost != null && 
					move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
				if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE/2) {
					cumulativePoints.add(230);
					try {
						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
					}
					catch(Exception e) {}
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {
					cumulativePoints.add(40);
					try {
						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
					}
					catch(Exception e) {}
				}
				else {
					cumulativePoints.add(-100);
				}
			}
			// don't know what to do, worst situation
			else {
				cumulativePoints.add(-100);
			}
			m++;
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