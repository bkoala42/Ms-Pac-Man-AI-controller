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
public class GreedyMsPacMan extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	private static final int LAIR_DISTANCE=10;	//if a ghost is this close, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=10; // distance before getting eaten
	
	private GreedyMsPacManStrategy strategy;
	private int greedySafePill, greedyPill, greedySafeIndex, greedyIndex, safePillWithJunction, safeEscapeNode;
	private int[] aStarPills, aStarJunctions, aStarIndices;
	
	public GreedyMsPacMan() {
		this.strategy = new GreedyMsPacManStrategy();
	}
	
	public MOVE getMove(Game game, long timeDue) {
		// COLOR LEGEND:
		// - light gray, path to the "greedily safe" closest pill in heuristic0
		// - orange, path to the trap power pill in heuristic1
		// - cyan, path to the "greedily safe" closest pill in heuristic1 when chased
		// - green, path to the ghost to eat
		// - red, path to the "greedily safe" closest pill in heuristic1 when running away
		// - yellow, path to the closest safe junction in heuristic1 when running away
		// - magenta, path to the emergency index in heuristic1 when running away
		int posMsPacman=game.getPacmanCurrentNodeIndex();	
		
		myMove = MOVE.NEUTRAL;
		
		MOVE[] moves = null;
		moves = game.getPossibleMoves(posMsPacman);
		
		// get all maze indices reachable by pacman
		int[] pills = strategy.getPillTargets(game, true);
		int[] powerPills = strategy.getPowePillTargets(game, true);
		// put them all together
		List<Integer> pillsList  = Arrays.stream(pills).boxed().collect(Collectors.toList());
		List<Integer> powerPillsList  = Arrays.stream(powerPills).boxed().collect(Collectors.toList());
		pillsList.addAll(powerPillsList);
		aStarPills = strategy.getAStarSafeTargets(game, posMsPacman, pillsList);
		
		aStarJunctions = strategy.getAStarSafeTargets(game, posMsPacman, Arrays.stream(game.getJunctionIndices()).boxed().collect(Collectors.toList()));
		
		// get all maze indices reachable by pacman
		pills = strategy.getPillTargets(game, false);
		powerPills = strategy.getPowePillTargets(game, false);
		// put them all together
		pillsList  = Arrays.stream(pills).boxed().collect(Collectors.toList());
		powerPillsList  = Arrays.stream(powerPills).boxed().collect(Collectors.toList());
		pillsList.addAll(powerPillsList);
		aStarIndices = strategy.getAStarSafeTargets(game, posMsPacman, pillsList);
		
		// given the current position find greedily the best pill, that is a pill maximally far from the closest ghost near to it
		// and maximally near to pacman. Caution: this pill may not exist 
		greedySafePill = strategy.getClosestPill(game, posMsPacman, true);
		// if a safe greedy pill was not found pick another pill that is maximally far from the closest ghost near to it
		// and maximally near to pacman without the constraint that MsPacMan reaches it before the ghosts. This MUST exist
		greedyPill = strategy.getClosestPill(game, posMsPacman, false);
		
		safePillWithJunction = strategy.getSafePillWithJunction(game, posMsPacman, strategy.getPillTargets(game, true));
		safeEscapeNode = strategy.getSafePillWithJunction(game, posMsPacman, strategy.getPillTargets(game, false));
		
//		safePills = strategy.getSafePill(game, posMsPacman);
//		safeJunctions = strategy.getSafeJunction(game, posMsPacman);
//		safeIndices = strategy.getSafeIndex(game, posMsPacman);
		
		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
		utility1 = heuristic1(game, moves);
		utility0 = heuristic0(game, moves);	
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
		// WELL NOTE: at the moment all computation is carried on using the current position and not the new position given the move.
		// Is it useful to consider the next position supposing that the ghosts are still? (because at any point we use their current
		// position)
		int current = game.getPacmanCurrentNodeIndex();
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();

		GHOST closestGhost = strategy.getCloserGhost(game, current);
		int closestGhostDistance = -1;
		if(closestGhost != null) {
			closestGhostDistance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(closestGhost), current, 
					game.getGhostLastMoveMade(closestGhost));
		}
		
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>(moves.length);
			
			// When MsPacman is too close to the lair and ghosts are in it just go away, they could come out suddenly and kill you
			// CAUTION: MS PACMAN OFTEN GETS STUCKED IN THE SAME PLACE FOLLOWING THIS RULE
//			if(strategy.isThereGhostInLair(game) && 
//					game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LAIR_DISTANCE &&
//					move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
//				score.add(75);
//			}
			if(safePillWithJunction != -1 && closestGhostDistance > 2*MIN_DISTANCE &&
					move == game.getNextMoveTowardsTarget(current, safePillWithJunction, DM.PATH)) {
				score.add(50);
//				GameView.addPoints(game,Color.white, game.getShortestPath(current, safePillWithJunction));
			}
			// Case 1: go to the best pill chosen greedily, always going further from ghosts (PILL + POWER PILL)
			if(greedySafePill != -1 && closestGhostDistance > 2*MIN_DISTANCE &&
					move == game.getNextMoveTowardsTarget(current, greedySafePill, DM.PATH)) {
				score.add(40);
//				GameView.addPoints(game,Color.lightGray, game.getShortestPath(current, greedySafePill));
			}
//			// Case 2: go to the optimal pill chosen according A* (PILL + POWER PILL)
//			if(furthestSafePill != -1 && move == game.getNextMoveTowardsTarget(current, furthestSafePill, DM.PATH)) {
//				score.add(40);
////				GameView.addPoints(game,Color.white, game.getShortestPath(current, furthestSafePill));
//			}
			// Case 3: no better choice than moving towards an apparently good pill (PILL + POWER PILL)
//			if(greedyPill != -1 && move == game.getNextMoveTowardsTarget(current, greedyPill, DM.PATH)) {
//				score.add(30);
////				GameView.addPoints(game,Color.gray, game.getShortestPath(current, greedyPill));
//			}
			// Escape to a node with 2 junctions available to reach
			if(safeEscapeNode != -1 && move == game.getNextMoveTowardsTarget(current, safeEscapeNode, DM.PATH)) 
			{
				score.add(30);
			}
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
		
		// check if MsPacMan is chased
		boolean chased = false;
		if(strategy.isMsPacManChased(game.getPacmanCurrentNodeIndex(), game) >= 2) {
			chased = true;
		} 
		
		int closestSafePill = aStarPills[0];
		int furthestSafePill = aStarPills[1];
		// find a maze index to use as emergency way to run away (this always exists, in the worst case it won't save your life)
//		safeClosestIndex = strategy.getEmergencyWay(game, current);
		int closestSafeIndex = aStarIndices[0];
		int furthestSafeIndex = aStarIndices[1];
		// find a safe junction to use when running away. A safe junction is a better choice than the emergency way because leads
		// to a point of the map where at least three moves are admissible (the emergency way could lead pacman in an aisle)
		int closestSafeJunction = aStarJunctions[0];
		int furthestSafeJunction = aStarJunctions[1];
		
		int greedySafeIndex = strategy.getEmergencyWay(game, current);
		
		// power pill to chase to trap the ghosts and eat them in sequence
		int trapPowerPill = strategy.trapTheGhosts(game, current, strategy.getPowePillTargets(game, true));
		
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>();
			// find the most "interesting" ghosts that are available
			closestGhost = strategy.getCloserGhost(game, current);
			edibleGhost = strategy.isThereEdibleGhost(game, current);
			
			// When MsPacman is too close to the lair and ghosts are in it just go away, they could come out suddenly and kill you
			// CAUTION: MS PACMAN OFTEN GETS STUCKED IN THE SAME PLACE FOLLOWING THIS RULE
//			if(strategy.isThereGhostInLair(game) && 
//					game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LAIR_DISTANCE &&
//					move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
//				score.add(220);
//			}
			// MsPacman is chased and tries to fool the ghosts with the trap
//			if(chased) {
				// Sto checksafechase vediamo se lo possiamo eliminare definitivamente
				if(chased && trapPowerPill != -1 && strategy.checkSafeChase(trapPowerPill, current, game) &&
					move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
//					GameView.addPoints(game, Color.orange, game.getShortestPath(current, trapPowerPill));
					score.add(235);
				}
////				THIS MAY BE REDUNDANT, PARTIALLY COVERED IN RUNAWAY, BUT ONLY WHEN THE CLOSEST GHOST IS CLOSE
//				// MsPacMan is chased, no need to go towards safe or greedily safe pills, it is enough to stay far from
//				// ghosts (???)
//				else if(greedySafePill != -1 && move == game.getNextMoveTowardsTarget(current, greedySafePill, DM.PATH)){
//					score.add(90);
////					GameView.addPoints(game, Color.cyan, game.getShortestPath(current, greedySafePill));
//				}
//			}
			// There is a safely edible ghost, go and catch it
			if(edibleGhost != null && strategy.checkSafeChase(game.getGhostCurrentNodeIndex(edibleGhost), current, game) &&
					move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)) {
				score.add(200);
//				GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
			}
			// no good moves available, just try to get away from the closest ghost
			if(closestGhost != null && game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE) {	
				// se usiamo il pill safe di A* (quello più lontano, inizia a fare giro giro tondo contro gli aggressive)
				// usare invece il più vicino potrebbe essere pericoloso per fuggire (???)
				if(greedySafePill != -1 && strategy.checkSafeChase(greedySafePill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, greedySafePill, DM.PATH)) {
//					GameView.addPoints(game, Color.yellow, game.getShortestPath(current, greedySafePill));
					score.add(195);
				}
				// use as anchor point to run away a safe junction
				// usare la junct più vicina o la più lontana???
				else if(furthestSafeJunction != -1 && 
						move == game.getNextMoveTowardsTarget(current, furthestSafeJunction, DM.PATH)) {
//					GameView.addPoints(game, Color.orange, game.getShortestPath(current, furthestSafeJunction));
					score.add(193);
				}
				// use as anchor point to run away an emergency point in the map
				// Questo è il punto più delicato, sia con A* che con implementazione greedy (getEmergencyWay) spesso 
				// non si riesce a fuggire, o perché prende una strada che va nei ghost (il che può anche verificarsi) o perché
				// la strada c'è e ti salverebbe ma comincia a rincoglionirsi sul posto anziché seguirla
//				else if(greedySafeIndex != -1 &&
//						move == game.getNextMoveTowardsTarget(current, greedySafeIndex, DM.PATH)) {
//					score.add(191);
////					GameView.addPoints(game, Color.magenta, game.getShortestPath(current, greedySafeIndex));
//				}
				// there is no better choice than just going away from the closest ghost (very likely to die in this case)
				// Sta roba va evitata come la peste, ci serve un caso bucket come quello del pill nella heur0
				else if(move == game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(closestGhost), DM.PATH)) {
					score.add(170);
				}
				// don't know where to go
				else {
					score.add(0);
				}
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