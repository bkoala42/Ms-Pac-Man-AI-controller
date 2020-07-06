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
	private static final int CLUSTER_DISTANCE = 30;
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	private MsPacManStrategy strategy;
	private ArrayList<Integer> safeClosestJunctionPills;
	private ArrayList<Integer> safeClosestJunctionPowerPills;
	private int safeClosestIndexPill, safeClosestIndexPowerPill;
	
	public MsPacMarco() {
		this.strategy = new MsPacManStrategy();
	}
	
	
	public MOVE getMove(Game game, long timeDue) 
	{
//		long startTime = java.lang.System.currentTimeMillis();
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
		myMove = MOVE.NEUTRAL;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		
		int[] pills = strategy.pillTargets(game, false);
		int[] powerPills = strategy.pillTargets(game, true);
		
		safeClosestJunctionPills = strategy.getClosestSafeJunction(game, posMsPacman, pills);
		safeClosestIndexPill = safeClosestJunctionPills.remove(safeClosestJunctionPills.size()-1);
		
		safeClosestJunctionPowerPills = strategy.getClosestSafeJunction(game, posMsPacman, powerPills);
		safeClosestIndexPowerPill = safeClosestJunctionPowerPills.remove(safeClosestJunctionPowerPills.size()-1);
		
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
		
		int[] pills = null;
		
		pills = strategy.pillTargets(game, false);
		
		for(MOVE move: moves) {
			// the first check is on the presence of ghosts in liar to avoid instant kill
			if(strategy.isThereGhostInLiar(game) && 
					game.getShortestPathDistance(current, strategy.liarIndex(game)-1) < LIAR_DISTANCE &&
					move == game.getNextMoveAwayFromTarget(current, strategy.liarIndex(game)-1, DM.PATH)) {
				cumulativePoints.add(75);
			}
			// There is a safe way to follow
			else if(safeClosestIndexPill != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)) {
				cumulativePoints.add(50);
				GameView.addPoints(game,Color.lightGray, game.getShortestPath(current, safeClosestIndexPill));
			}
			// MsPacman was not able to find a safe path, reward the moves that takes towards the next pill
			else {
				int closestPill = strategy.getClosestPill(game, current, pills);
				if(closestPill != -1 && move == game.getNextMoveTowardsTarget(current, closestPill, DM.PATH)) {
					cumulativePoints.add(30);
				}
				// situation to avoid, MsPacman does not know what to do
				else {
					cumulativePoints.add(-25);
				}
			}
			m++;
		}
		
		// compare the results of the moves
		bestMove = cumulativePoints.indexOf(Collections.max(cumulativePoints));
		
		// Visualize safe junctions
		int[] safeNodes=new int[safeClosestJunctionPills.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunctionPills.get(i);
		GameView.addPoints(game,Color.pink, safeNodes);
		
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
		int[] powerPills = null;
		
		// ghost targets to take care of
		GHOST closestGhost, edibleGhost;
		
		powerPills = strategy.pillTargets(game, true);
		
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
					move == game.getNextMoveAwayFromTarget(current, strategy.liarIndex(game)-1, DM.PATH)) {
				System.out.println(game.getShortestPathDistance(current, strategy.liarIndex(game)));
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
			else if(safeClosestIndexPowerPill != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndexPowerPill, DM.PATH)
					&& closestGhost != null) {
//				// there is a ghost in the neighborhood
//				if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {
//					System.out.println("Edible ghost around");
//					cumulativePoints.add(80);
//				}
				// there is a cluster of ghosts in the neighborhood
				if(strategy.isThereGhostsCluster(current, game, CLUSTER_DISTANCE)) {
					cumulativePoints.add(120);
				}
				// no ghosts around, better not eat the power pill
				else {
					cumulativePoints.add(-50);
				}
				GameView.addPoints(game, Color.orange, game.getShortestPath(current, safeClosestIndexPowerPill));
			}
			// no good moves available, just try to get away from the closest ghost
			else if(closestGhost != null) {
				// run away using a safe point in the map
				if(safeClosestIndexPill != -1 && move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)) {
					if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE/2) {
						cumulativePoints.add(230);
					}
					else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE){
						cumulativePoints.add(70);
					}
					else {
						cumulativePoints.add(-100);
					}
					try {
						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
					}
					catch(Exception e) {}
				}
				// no better choice than just running away
				else if(move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)){
					if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE/2) {
						cumulativePoints.add(60);
					}
					else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE){
						cumulativePoints.add(20);
					}
					else {
						cumulativePoints.add(-100);
					}
					try {
						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
					}
					catch(Exception e) {}
				}
				else {
					cumulativePoints.add(-100);
				}
			}
//			else if(closestGhost != null && 
//					move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
//				if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE/2) {
//					cumulativePoints.add(230);
//					try {
//						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
//					}
//					catch(Exception e) {}
//				}
//				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {
//					cumulativePoints.add(40);
//					try {
//						GameView.addPoints(game, Color.red, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
//					}
//					catch(Exception e) {}
//				}
//				else {
//					cumulativePoints.add(-100);
//				}
//			}
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
		int[] safeNodes=new int[safeClosestJunctionPowerPills.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunctionPowerPills.get(i);
		GameView.addPoints(game, Color.magenta, safeNodes);
		
		returnValues[0] = cumulativePoints.get(bestMove);
		returnValues[1] = bestMove;
		return returnValues;
	}
	
}