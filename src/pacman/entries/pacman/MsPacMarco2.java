package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
public class MsPacMarco2 extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	private static final int LIAR_DISTANCE=10;	//if a ghost is this close, run away
	private static final int CLUSTER_DISTANCE = 30;
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	private MsPacManStrategy strategy;
	private ArrayList<Integer> safeClosestJunctionPills;
	private ArrayList<Integer> safeClosestJunctionPowerPills;
	private int safeClosestIndexPill, safeClosestIndexPowerPill;
	
	public MsPacMarco2() {
		this.strategy = new MsPacManStrategy();
	}
	
	
	public MOVE getMove(Game game, long timeDue) 
	{
//		long startTime = java.lang.System.currentTimeMillis();
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
		myMove = MOVE.NEUTRAL;
//		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		
		MOVE[] moves = null;
		moves = game.getPossibleMoves(posMsPacman);

		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
//		int heur0Max = 75;
		
//		utility1 = heuristic1(game, moves);
//		if(utility1[0] > heur0Max) {
//			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
//			myMove = moves[utility1[1]];
//		}
//		else {
//			utility0 = heuristic0(game, moves);	
//			if(utility0[0] > utility1[0]) {
//				System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
//				myMove = moves[utility0[1]];
//			}
//			else {
//				System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
//				myMove = moves[utility1[1]];
//			}
//		}
		
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
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		ArrayList<Integer> score = new ArrayList<Integer>(moves.length);
		
		int[] pills = null;
		
		pills = strategy.pillTargets(game, false);
		
		safeClosestJunctionPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), pills, false);
		safeClosestIndexPill = safeClosestJunctionPills.remove(safeClosestJunctionPills.size()-1);
		
		for(MOVE move: moves) {
			// the first check is on the presence of ghosts in lair to avoid instant kill
			if(strategy.isThereGhostInLair(game) && 
					game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LIAR_DISTANCE
					&& move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
				System.out.println("running from lair, distance: "+ game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()));
				score.add(85);
			}
			
			// There is a safe way to follow
			if(safeClosestIndexPill != -1) {
				score.add(50);
				GameView.addPoints(game,Color.lightGray, game.getShortestPath(current, safeClosestIndexPill));
			}
			else {
				score.add(0);
			}
			
			// MsPacman was not able to find a safe path, reward the moves that takes towards the next pill
//			else {
//				if(strategy.getClosestPill(game, current, pills, true) != -1 && 
//						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, pills, true), DM.PATH) &&
//						strategy.checkSafeChase(strategy.getClosestPill(game, current, pills, true), current, game)) {
//					score.add(30);
//					GameView.addPoints(game,Color.yellow, game.getShortestPath(current, strategy.getClosestPill(game, current, pills, true)));
//				}
//				else if(strategy.getClosestPill(game, current, pills, false) != -1 && 
//						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, pills, false), DM.PATH) &&
//						strategy.checkSafeChase(strategy.getClosestPill(game, current, pills, false), current, game)){
//					score.add(25);
//					GameView.addPoints(game,Color.magenta, game.getShortestPath(current, strategy.getClosestPill(game, current, pills, false)));
//				}
//				else {
//					score.add(-100);
//				}
//			}
//			if(score.isEmpty()) {
//				score.add(-100);
//			}
			movesScore.put(m, score.get(score.indexOf(Collections.max(score))));
			score.clear();
			m++;
		}
		
		int tmp = Integer.MIN_VALUE;
		for(Integer moveIndex: movesScore.keySet()) {
			if(movesScore.get(moveIndex) > tmp) {
				tmp = movesScore.get(moveIndex);
				bestMove = moveIndex;
			}
		}
		
		// Visualize safe junctions
		int[] safeNodes=new int[safeClosestJunctionPills.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunctionPills.get(i);
		GameView.addPoints(game,Color.pink, safeNodes);
		
		returnValues[0] = tmp;
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
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		ArrayList<Integer> score = new ArrayList<Integer>(moves.length);
		
		int current = game.getPacmanCurrentNodeIndex();
		int[] powerPills = null;
		int[] pills = null;
		
		// ghost targets to take care of
		GHOST closestGhost, edibleGhost;
		
		powerPills = strategy.pillTargets(game, true);
		pills = strategy.pillTargets(game, false);
		
		if(strategy.isMsPacManChased(game.getPacmanCurrentNodeIndex(), game) >= 2) {
			safeClosestJunctionPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), pills, true);
			safeClosestIndexPill = safeClosestJunctionPills.remove(safeClosestJunctionPills.size()-1);
			safeClosestJunctionPowerPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), powerPills, true);
			safeClosestIndexPowerPill = safeClosestJunctionPowerPills.remove(safeClosestJunctionPowerPills.size()-1);
		} 
		else {
			safeClosestJunctionPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), pills, false);
			safeClosestIndexPill = safeClosestJunctionPills.remove(safeClosestJunctionPills.size()-1);
			safeClosestJunctionPowerPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), powerPills, false);
			safeClosestIndexPowerPill = safeClosestJunctionPowerPills.remove(safeClosestJunctionPowerPills.size()-1);
		}
		
		for(MOVE move: moves) {
			// find the most "interesting" ghosts that are available
			closestGhost = strategy.getCloserGhost(game, current);
			edibleGhost = strategy.isThereEdibleGhost(game, current);
			int trapPowerPill = strategy.trapTheGhosts(game, current, powerPills);
			
//			if(closestGhost != null) 
//				GameView.addPoints(game, Color.white, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
			
			// check if MsPacman is chased, this is important to change strategy and fool ghosts
			boolean chased = false;
			if(strategy.isMsPacManChased(current, game) >= 2) {
				chased = true;
//				System.out.println("Sono inseguito, safe points "+safeClosestIndexPill+" "+safeClosestIndexPowerPill);
			}
//			else 
//				System.out.println("Non sono inseguito, safe points "+safeClosestIndexPill+" "+safeClosestIndexPowerPill);
			
			// MsPacman follows two simple rules: eat ghosts if possible, maybe trap them, or go away
			// when there is no chance of eating, possibly staying around power pills
			// the first check is on the presence of ghosts in liar to avoid instant kill
//			System.out.println(game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()));
			if(strategy.isThereGhostInLair(game) && 
					game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LIAR_DISTANCE &&
					move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
				score.add(220);
			}
			// MsPacman is chased and tries to fool the ghosts
			if(chased) {
				if(trapPowerPill != -1 && strategy.checkSafeChase(trapPowerPill, current, game) &&
					move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
					GameView.addPoints(game, Color.cyan, game.getShortestPath(current, trapPowerPill));
					score.add(235);
				}
				else if(safeClosestIndexPill != -1 && strategy.checkSafeChase(safeClosestIndexPill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)){
					score.add(90);
					GameView.addPoints(game, Color.cyan, game.getShortestPath(current, safeClosestIndexPill));
				}
			}
			// There is a safely edible ghost, go and catch it
			if(edibleGhost != null && strategy.checkSafeChase(game.getGhostCurrentNodeIndex(edibleGhost), current, game) &&
					strategy.checkSafeEat(game, current, edibleGhost, move) &&
					move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
				score.add(200);
				GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
			}
			// no edible ghosts, reward the move that leads in a safe zone around power pills
			if(safeClosestIndexPowerPill != -1 && strategy.checkSafeChase(safeClosestIndexPowerPill, current, game) &&
					move == game.getNextMoveTowardsTarget(current, safeClosestIndexPowerPill, DM.PATH) && closestGhost != null) {
				if(strategy.isThereGhostsCluster(current, game, MIN_DISTANCE)) {
					GameView.addPoints(game, Color.orange, game.getShortestPath(current, safeClosestIndexPowerPill));
					score.add(120);
				}
				// no ghosts around, better not eat the power pill
				else {
					score.add(-50);
				}
			}
			// no good moves available, just try to get away from the closest ghost
			if(closestGhost != null) {
				if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						safeClosestIndexPowerPill != -1 && strategy.checkSafeChase(safeClosestIndexPowerPill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, safeClosestIndexPowerPill, DM.PATH)) {
					GameView.addPoints(game, Color.red, game.getShortestPath(current, safeClosestIndexPowerPill));
					score.add(210);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						safeClosestIndexPill != -1 && strategy.checkSafeChase(safeClosestIndexPill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)) {
					GameView.addPoints(game, Color.red, game.getShortestPath(current, safeClosestIndexPill));
					score.add(195);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						strategy.getClosestPill(game, current, powerPills, true) != -1 && 
						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, powerPills, true), DM.PATH) &&
						strategy.checkSafeChase(strategy.getClosestPill(game, current, powerPills, true), current, game)) {
					GameView.addPoints(game, Color.yellow, game.getShortestPath(current, strategy.getClosestPill(game, current, powerPills, true)));
					score.add(193);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						strategy.getClosestPill(game, current, pills, true) != -1 && 
						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, pills, true), DM.PATH) &&
						strategy.checkSafeChase(strategy.getClosestPill(game, current, pills, true), current, game)) {
					GameView.addPoints(game, Color.yellow, game.getShortestPath(current, strategy.getClosestPill(game, current, pills, true)));
					score.add(192);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						strategy.getClosestPill(game, current, powerPills, false) != -1 && 
						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, powerPills, false), DM.PATH) &&
						strategy.checkSafeChase(strategy.getClosestPill(game, current, powerPills, false), current, game)) {
					GameView.addPoints(game, Color.yellow, game.getShortestPath(current, strategy.getClosestPill(game, current, powerPills, false)));
					score.add(191);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						strategy.getClosestPill(game, current, pills, false) != -1 && 
						move == game.getNextMoveTowardsTarget(current, strategy.getClosestPill(game, current, pills, false), DM.PATH) &&
						strategy.checkSafeChase(strategy.getClosestPill(game, current, pills, false), current, game)) {
					GameView.addPoints(game, Color.yellow, game.getShortestPath(current, strategy.getClosestPill(game, current, pills, false)));
					score.add(190);
				}
				else if(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE &&
						move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
					score.add(180);
				}
				else {
					score.add(-100);
				}
			}
			if(score.isEmpty()) {
				score.add(-100);
			}
			movesScore.put(m, score.get(score.indexOf(Collections.max(score))));
			score.clear();
			m++;
		}
	
		// compare the results of the moves
		int tmp = Integer.MIN_VALUE;
		for(Integer moveIndex: movesScore.keySet()) {
			if(movesScore.get(moveIndex) > tmp) {
				tmp = movesScore.get(moveIndex);
				bestMove = moveIndex;
			}
		}
		
		// Visualize safe junctions
		int[] safeNodes=new int[safeClosestJunctionPowerPills.size()];		
		for(int i=0;i<safeNodes.length;i++)
			safeNodes[i]=safeClosestJunctionPowerPills.get(i);
		GameView.addPoints(game, Color.magenta, safeNodes);
		
		returnValues[0] = tmp;
		returnValues[1] = bestMove;
		return returnValues;
	}
	
}