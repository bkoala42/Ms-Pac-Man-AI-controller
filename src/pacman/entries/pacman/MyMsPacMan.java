package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * This controller picks the next move that maximizes the utility value defined by two heuristic
 * functions. They represent a set of situations of the environment in which the agent has to react
 * going towards a target provided by the heuristic itself.
 * Heuristic0 manages "safe" cases where Ms Pac-Man is not chased, Heuristic1 is applied in all the
 * other situations (follow ghosts to eat them, run away from ghosts etc)
 */
public class MyMsPacMan extends Controller<MOVE> {
	
	private int minGhostDistance;
	private int guardDistance;
	private int chaseDistance;
	private int cleanDistance;
	private int eatDistanceHigh;
	
	private static final int CHASE_DISTANCE = 50;
	private static final int GUARD_DISTANCE = 5;
	private static final int MIN_DISTANCE = 25;
	private static final int CLEAN_DISTANCE = 11;
	private static final int EAT_DISTANCE_HIGH = 30;
	private static final int FINISHLEVEL_COUNTER = 5;	
	private MOVE myMove=MOVE.NEUTRAL;
	
	private MyMsPacManStrategy strategy;
	private int banned, eatenPowerPill;
	
	public MyMsPacMan() {
		this.minGhostDistance = MIN_DISTANCE;
		this.guardDistance = GUARD_DISTANCE;
		this.chaseDistance = CHASE_DISTANCE;
		this.cleanDistance = CLEAN_DISTANCE;
		this.eatDistanceHigh = EAT_DISTANCE_HIGH;
		this.strategy = new MyMsPacManStrategy(minGhostDistance, guardDistance, chaseDistance, cleanDistance, eatDistanceHigh);
	}
	
	public void setMinGhostDistance(int minGhostDistance) {
		if(minGhostDistance != -1)
			this.minGhostDistance = minGhostDistance;
		else
			this.minGhostDistance = MIN_DISTANCE;
	}
	
	public void setGuardDistance(int guardDistance) {
		if(guardDistance != -1)
			this.guardDistance = guardDistance;
		else
			this.guardDistance = GUARD_DISTANCE;
	}
	
	public void setChaseDistance(int chaseDistance) {
		if(chaseDistance != -1)
			this.chaseDistance = chaseDistance;
		else
			this.chaseDistance = CHASE_DISTANCE;
	}
	
	public void setCleanDistance(int cleanDistance) {
		if(cleanDistance != -1)
			this.cleanDistance = cleanDistance;
		else 
			this.cleanDistance = CLEAN_DISTANCE;
	}
	
	public void seteatDistanceHigh(int eatDistanceHigh) {
		if(eatDistanceHigh != -1)
			this.eatDistanceHigh = eatDistanceHigh;
		else 
			this.eatDistanceHigh = EAT_DISTANCE_HIGH;
	}
	
	public void printParameters() {
		System.out.println("minGhostDistance "+minGhostDistance+" "+" guardDistance "+guardDistance+" chaseDistance"+
				chaseDistance+" cleanDistance "+cleanDistance+" eatDistanceHigh "+eatDistanceHigh);
	}
	
	public MOVE getMove(Game game, long timeDue) {
		
		int posMsPacman = game.getPacmanCurrentNodeIndex();
		
		// update some status variables 
		if (game.isJunction(posMsPacman)) {
			banned = posMsPacman;
		}
		if(game.wasPowerPillEaten()) {
			eatenPowerPill = game.getNeighbour(posMsPacman,game.getPacmanLastMoveMade().opposite());
		}
		
		myMove = MOVE.NEUTRAL;
		
		MOVE[] moves = null;
		// always allow Ms Pac-Man to turn around getting all possible moves
		moves = game.getPossibleMoves(posMsPacman);
		
		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
		// query the two heuristic functions to find the most suitable action given the environment state
		utility1 = heuristic1(game, moves);
		utility0 = heuristic0(game, moves);	
		
		if(utility0[0] > utility1[0]) {
//			System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
			myMove = moves[utility0[1]];
		}
		else {
//			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
			myMove = moves[utility1[1]];
		}

		return myMove;
	}
		
	// Heuristic 0 -> safe path
	// If all ghosts are "far enough" and there are pills to eat, this heuristic gains a high value. Which pill to eat 
	// must be chosen ensuring that Ms Pacman is not closed in a path from ghosts, this means that there is always a 
	// junction reachable from the pill that Ms Pacman can reach before any ghost in their shortest path. To foresee 
	// the path of the ghosts, the last move made by ghost must be taken into account and an A* is used to calculate 
	// all the paths to the junctions and then estimate a safe one
	private int[] heuristic0(Game game, MOVE[] moves) {
		
		int current = game.getPacmanCurrentNodeIndex();
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		
		// Get the available pill which is close to at least two junctions that Ms Pac-Man reaches before the ghosts.
		// Ms Pac-Man has also to be able to reach these junctions before ghosts. This gives her safety to eat and run away
		int safePillWithJunction = strategy.getSafePillCloseToJunctions(game, current, strategy.getPillTargets(game, true));

		// for each possible action verify if it generates utility
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>();
			if(safePillWithJunction != -1 && move == game.getNextMoveTowardsTarget(current, safePillWithJunction, DM.PATH)) {
				score.add(50);
			}
			else {
				// Get a pill which Ms Pac-Man reaches before the ghost which is close to a junction that Ms Pac-Man 
				// reaches before the ghosts when passing for the pill
				int eatPill = strategy.getNextSafePill(game, current, strategy.getAllTargets(game, true), banned);
				if(eatPill != -1 && move == game.getNextMoveTowardsTarget(current, eatPill, DM.PATH)) {
					score.add(45);
				}
				else {
					// Just get the closest pill
					int closestPill = strategy.getClosestPill(game, current, strategy.getPillTargets(game, true));
					if(closestPill != -1 && move == game.getNextMoveTowardsTarget(current, closestPill, DM.PATH)) {
						score.add(40);
					}
				}
			}
			if(score.isEmpty()) {
				score.add(0);
			}
			movesScore.put(m, score.get(score.indexOf(Collections.max(score))));
			m++;
		}
		
		// Pick the move with the highest utility value
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
	// If there is an edible ghost hunt it, otherwise if ghosts are too close try to run away. If possible, when escaping,
	// try also to eat pills or power pills ensuring that Ms Pacman is not closed in a path from ghosts. This condition can be 
	// met using different escape paths with variable utility reward. In any case A* is used to calculate the paths to the
	// targets (junctions, pills, generic maze indices) and then estimate a safe one
	private int[] heuristic1(Game game, MOVE[] moves) {
		
		int m = 0, bestMove = -1;
		int[] returnValues = new int[2];
		Map<Integer, Integer> movesScore = new HashMap<Integer, Integer>();
		int current = game.getPacmanCurrentNodeIndex();
		
		// ghost targets to take care of
		GHOST closestGhost, edibleGhost;
		closestGhost = strategy.getClosestGhost(game, current);
		edibleGhost = strategy.nearestEdibleGhost(game, current);
	
		// check if MsPacMan is chased
		int chasers = strategy.getGhostChasingPacman(current, game).size();

		// if there is a too close ghost find the safe targets in the map that can be reached by Ms Pac-Man
		int trapPowerPill = -1, safePill = -1, farthestJunction = -1, nextPill = -1, safeIndex = -1;
		if(closestGhost != null && game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < minGhostDistance) {
			trapPowerPill = strategy.trapTheGhosts(game, current, strategy.getPowePillTargets(game, true));
			safePill = strategy.getSafePillPassingForSafeJunction(game, current, strategy.getAllTargets(game, true), banned);
			farthestJunction = strategy.getSafeJunctionFartherToClosestGhost(game, current, banned);
			safeIndex = strategy.getSafeIndexFarthestToClosestGhost(game, current, strategy.getAllTargets(game, false));
			nextPill = strategy.getNextSafePill(game, current, strategy.getAllTargets(game, true), banned);
		}
		
		// for each possible action verify if it generates utility
		for(MOVE move: moves) {
			ArrayList<Integer> score = new ArrayList<Integer>();
			
			// try to eat the nearest edible ghost
			int[] activePills = null;
			if(edibleGhost != null) {			
				int pillLeft = strategy.cleanCorners(game, current, strategy.getPillTargets(game, true), eatenPowerPill);
				activePills = strategy.getAllTargets(game, true);
				if(pillLeft != -1 && move == game.getNextMoveTowardsTarget(current, pillLeft, DM.PATH))
					score.add(202);
				else if(activePills.length > FINISHLEVEL_COUNTER && move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH))
					score.add(200);
			}
				
			// Escape from ghosts
			if(closestGhost != null && game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < minGhostDistance) {
				// Only the aggressive team chases Ms Pac-Man in 4, so go to walk them while eating pills
				// without getting stuck using junctions as escape points
				if(chasers > 3) {
					if(nextPill != -1 && move == game.getNextMoveTowardsTarget(current, nextPill, DM.PATH)) {
						score.add(199);
					}
				}
				
				// Run away in the direction of a pill that can be reached passing for a safe junction. Ms Pac-Man 
				// must reach the pill, passing for the junction, before than a ghost can reach that pill
				if(safePill != -1 && move == game.getNextMoveTowardsTarget(current, safePill, DM.PATH)) {
					score.add(198);
				}

				// Go to the next power pill that can be safely reached
				if(trapPowerPill != -1 && move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
					score.add(195);
				}
				
				// Go to the junction that Ms Pac-Man reaches before the ghosts and that is farthest from the closest ghost
				if(farthestJunction != -1 && move == game.getNextMoveTowardsTarget(current, farthestJunction, DM.PATH)) {
					score.add(191);
				}
				
				// There is no better escape way than going to maze index that Ms Pac-Man reaches before the ghosts
				if(safeIndex != -1 && move == game.getNextMoveTowardsTarget(current, safeIndex, DM.PATH)) {
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
		
		// Pick the move with the highest utility value
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