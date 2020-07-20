package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/*
 */
public class GreedyMsPacManBan extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=25;	//if a ghost is this close, run away
	private static final int LAIR_DISTANCE=10;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	
	private GreedyMsPacManStrategyBan strategy;
	private int greedySafePill, greedyPill, greedyIndex, safeEscapeNode;
	private int banned, eatenPowerPill;
	
	public GreedyMsPacManBan() {
		this.strategy = new GreedyMsPacManStrategyBan();
	}
	
	public MOVE getMove(Game game, long timeDue) {
		long startTime = java.lang.System.currentTimeMillis();
		
		
//		GameView.addPoints(game, Color.gray, banned);
		if(game.wasPacManEaten()) 
			System.out.println("++++++++++++++++ SONO CREPATO ++++++++++++++++");
		int posMsPacman=game.getPacmanCurrentNodeIndex();
		
		// UPDATE GLOBAL VARIABLES
		if (game.isJunction(posMsPacman)) {
			banned = posMsPacman;
		}
		if(game.wasPowerPillEaten()) {
			eatenPowerPill = game.getNeighbour(posMsPacman,game.getPacmanLastMoveMade().opposite());
		}
		
		myMove = MOVE.NEUTRAL;
		
		MOVE[] moves = null;
		moves = game.getPossibleMoves(posMsPacman);
		
		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
		utility1 = heuristic1(game, moves);
		// OTTIMIZZAZIONE *****************
//		if(utility1[0] < 50)
//			utility0 = heuristic0(game, moves);	
//		else
//			utility0[0] = 0;
		// ********************************
		utility0 = heuristic0(game, moves);	
		if(utility0[0] > utility1[0]) {
			System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
			myMove = moves[utility0[1]];
		}
		else {
			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
			myMove = moves[utility1[1]];
		}

		System.out.println("Time: " + (java.lang.System.currentTimeMillis() - startTime));
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
		// WELL NOTE: at the moment all computation is carried on using the current position and not the new position given the move.
		// Is it useful to consider the next position supposing that the ghosts are still? (because at any point we use their current
		// position)
		int current = game.getPacmanCurrentNodeIndex();
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		
		// Go to the available pill/power pill which is the farthest from the closest ghost and not reachable by the other ghosts.
		// When this choice is not possible then get towards any safe point in the map that Ms PacMan can reach before ghosts
		int greedyPill = -1;
//		greedyPill = strategy.getGreedySafeTarget(game, current, strategy.getAllTargets(game, true));
//		if(greedyPill == -1) 
//			greedyIndex = strategy.getGreedySafeTarget(game, current, strategy.getAllTargets(game, false));
		// Go to the available pill/power pill which is close to at least two junctions that Ms Pac-Man reaches before the ghosts.
		// Ms Pac-Man has also to be able to reach these junctions before ghosts. This gives her safety to eat and run away
		int safePillWithJunction = strategy.getSafePillWithJunction(game, current, strategy.getPillTargets(game, true));
//		int safePillWithJunction = strategy.getGreedySafeClosestTarget(game, current, strategy.getPillTargets(game, true));
//		// Prunare heur0 se sono abb lontani
//		GHOST closestGhost = strategy.getCloserGhost(game, current);
//		int closestGhostDistance = -1;
//		if(closestGhost != null) {
//			closestGhostDistance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(closestGhost), current);
//			//			GameView.addLines(game, Color.magenta, current, game.getGhostCurrentNodeIndex(closestGhost));
//			//			System.out.println("distance: "+closestGhostDistance);
//		}
		
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>();
			// ATTENZIONE MANCA LA FUGA DAL LAIR
			if(safePillWithJunction != -1 && 
					move == game.getNextMoveTowardsTarget(current, safePillWithJunction, DM.PATH)
					) {
				score.add(50);
//				GameView.addPoints(game,Color.white, safePillWithJunction);
			}
			else {
				int eatPill = strategy.eatPills(game, current, strategy.getAllTargets(game, true), banned);
				if(eatPill != -1 && move == game.getNextMoveTowardsTarget(current, eatPill, DM.PATH)) {
					score.add(45);
				}
				else {
					int closestPill = strategy.getClosestPill(game, current, strategy.getPillTargets(game, true));
					if(closestPill != -1 && move == game.getNextMoveTowardsTarget(current, closestPill, DM.PATH)) {
						score.add(40);
					}
				}
			}
//			if(greedyPill != -1 &&
//					move == game.getNextMoveTowardsTarget(current, greedyPill, DM.PATH)) {
//				score.add(40);
////				GameView.addPoints(game,Color.lightGray, greedyPill);
//			}
			// Escape to a node with 2 junctions available to reach
//			else if(greedyIndex != -1 
//					&& move == game.getNextMoveTowardsTarget(current, greedyIndex, DM.PATH)) {
//				score.add(30);
//				GameView.addPoints(game,Color.gray, greedyIndex);
//			}
//			else if(greedyPill != -1 && move == game.getNextMoveTowardsTarget(current, greedyPill, DM.PATH)) {
//				score.add(20);
////				GameView.addPoints(game,Color.gray, game.getShortestPath(current, greedyPill));
//			}
			if(score.isEmpty()) {
				score.add(0);
			}
			movesScore.put(m, score.get(score.indexOf(Collections.max(score))));
			m++;
		}
		
		int tmp = Integer.MIN_VALUE;
		for(Integer moveIndex: movesScore.keySet()) {
			if(movesScore.get(moveIndex) > tmp) {
				tmp = movesScore.get(moveIndex);
				bestMove = moveIndex;
			}
		}
		
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
		// WELL NOTE: at the moment all computation is carried on using the current position and not the new position given the move.
		// Is it useful to consider the next position supposing that the ghosts are still? (because at any point we use their current
		// position)
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		int current = game.getPacmanCurrentNodeIndex();
		
		// ghost targets to take care of
		GHOST closestGhost, edibleGhost;
		closestGhost = strategy.getCloserGhost(game, current);
		edibleGhost = strategy.nearestEdibleGhost(game, current);
//		if(closestGhost != null)
//			GameView.addPoints(game, Color.lightGray, game.getShortestPath(current, game.getGhostCurrentNodeIndex(closestGhost)));
		
		// check if MsPacMan is chased
		int chasers = strategy.isMsPacManChased(current, game);
//		System.out.println("N° of chasers: "+ chasers);
//		
		// power pill to chase to trap the ghosts and eat them in sequence
		int trapPowerPill = -1;
		int safePill = -1;
		int farthestJunction = -1;
		int eatPill = -1;
		int greedySafeIndex = -1;
		if(closestGhost != null && game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {
			trapPowerPill = strategy.trapTheGhosts(game, current, strategy.getPowePillTargets(game, true));
			safePill = strategy.getSafeEscapeToPillWithJunction(game, current, strategy.getAllTargets(game, true), banned);
			farthestJunction = strategy.getSafeEscapeToClosestJunction(game, current, banned);
			greedySafeIndex = strategy.getGreedySafeTarget(game, current, strategy.getAllTargets(game, false));
			eatPill = strategy.eatPills(game, current, strategy.getAllTargets(game, true), banned);
		}
//		System.out.println("greedySafeIndex "+greedySafeIndex);
//		// OTTIMIZZAZIONE ********
//		if(safePill == -1) {
//			greedySafeIndex = strategy.getGreedySafeTarget(game, current, true, strategy.getAllTargets(game, false));
////			safeNodeEscape = strategy.getSafePillWithJunction(game, current, strategy.getAllTargets(game, false));
////			farthestJunction = strategy.getSafeEscapeToClosestJunction(game, current, banned);
//		}
//		// ***********************
		
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>();
//			if(closestGhost != null)
//				System.out.println(game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)));
			
			// There is a safely edible ghost, go and catch it
			int[] activePills = null;
			if(edibleGhost != null) {			
				int pillLeft = strategy.cleanCorners(game, current, strategy.getPillTargets(game, true), eatenPowerPill);
				activePills = strategy.getAllTargets(game, true);
				if(pillLeft != -1 && move == game.getNextMoveTowardsTarget(current, pillLeft, DM.PATH))
					score.add(201);
				else if(activePills.length > 10 && move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH))
					score.add(200);
//				GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
			}
			
//			
			// Escape from ghosts
//			System.out.println("Eat pill "+eatPill+" safePill "+safePill+" junct "+farthestJunction+" index "+optSafeIndex+" last index "+farthestSafeIndex);
			if(closestGhost != null && game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {
				if(chasers > 3) {
					// it's the Aggressive ghost team, then go for a walk and eat pills in the zone
//					eatPill = strategy.eatPills(game, current, strategy.getAllTargets(game, true), banned);
//					System.out.println("eat pill: "+eatPill);
					if(eatPill != -1 && move == game.getNextMoveTowardsTarget(current, eatPill, DM.PATH)) {
						score.add(199);
					}
				}
				if(safePill != -1 && move == game.getNextMoveTowardsTarget(current, safePill, DM.PATH)) {
					score.add(198);
				}
				
				if(trapPowerPill != -1 && move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
//					GameView.addPoints(game, Color.orange, current, trapPowerPill);
					score.add(195);
				}
//				if(safeEscapeNode != -1
//						&& move == game.getNextMoveTowardsTarget(current, safeEscapeNode, DM.PATH)) {
//					GameView.addPoints(game,Color.orange, safeEscapeNode);
//					score.add(194);
//				}
//				if(greedySafePill != -1 && move == game.getNextMoveTowardsTarget(current, greedySafePill, DM.PATH)) {
//					score.add(192);
//				}
				if(farthestJunction != -1 && move == game.getNextMoveTowardsTarget(current, farthestJunction, DM.PATH)) {
					score.add(191);
				}
				if(greedySafeIndex != -1 && move == game.getNextMoveTowardsTarget(current, greedySafeIndex, DM.PATH)) {
					score.add(182);
				}
			}
			else {
				score.add(0);
			}
			
			if(score.isEmpty()) {
				score.add(0);
			}
			movesScore.put(m, score.get(score.indexOf(Collections.max(score))));
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
		
		returnValues[0] = tmp;
		returnValues[1] = bestMove;
		return returnValues;
	}
	
}