package pacman.entries.pacman;

import java.awt.Color;
import java.util.ArrayList;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MsPacMan extends Controller<MOVE>{
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	private static final int LIAR_DISTANCE=3;	//if mspacman is this close to lair, run away
	private MOVE myMove=MOVE.NEUTRAL;
	private static final int GUARD_DISTANCE=3; // distance before getting eaten
	
	private MsPacManStrategy strategy;
	private ArrayList<Integer> safeClosestJunctionPills;
	private ArrayList<Integer> safeClosestJunctionPowerPills;
	private int safeClosestIndexPill, safeClosestIndexPowerPill;
	private int[] pills, powerPills;
	
	public MsPacMan() {
		this.strategy = new MsPacManStrategy();
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) {		
		MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		
		// Get all available pills and powerpills
		pills = strategy.getPillTargets(game, true);
		powerPills = strategy.getPowePillTargets(game, true);
		
		int utility0[] = new int[2];
		int utility1[] = new int[2];
		
//		try {
//			GameView.addPoints(game,Color.red, game.getShortestPath(posMsPacman, game.getGhostCurrentNodeIndex(GHOST.BLINKY)));
//			GameView.addPoints(game,Color.blue, game.getShortestPath(posMsPacman, game.getGhostCurrentNodeIndex(GHOST.INKY)));
//			GameView.addPoints(game,Color.magenta, game.getShortestPath(posMsPacman, game.getGhostCurrentNodeIndex(GHOST.PINKY)));
//			GameView.addPoints(game,Color.orange, game.getShortestPath(posMsPacman, game.getGhostCurrentNodeIndex(GHOST.SUE)));
//		}
//		catch(Exception e) {}
		
//		utility1 = heuristic1(game, moves);
		utility0 = heuristic0(game, moves);
//		if(utility0[1] == -1 && utility1[1] == -1)
//			return myMove;
//		if(utility0[0] > utility1[0]) {
//			System.out.println("heur0 won " + utility0[0]);
////			System.out.println("heur0 won " + utility0[0] + " move: " + moves[utility0[1]]);
//			myMove = moves[utility0[1]];
//		}
//		else {
//			System.out.println("heur1 won " + utility1[0]);
////			System.out.println("heur1 won " + utility1[0] + " move: " + moves[utility1[1]]);
//			myMove = moves[utility1[1]];
//		}
		
		// Test only heur0
		System.out.println("heur0 won " + utility0[0]);
		myMove = moves[utility0[1]];

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
			int[] cumulativePoints = new int[moves.length];
			
			// NB DA CAMBIARE FA SCHIFO E PROBABILMENTE BUGGATO
			safeClosestJunctionPills = strategy.getClosestSafeJunction(game, game.getPacmanCurrentNodeIndex(), pills, false);
			safeClosestIndexPill = safeClosestJunctionPills.remove(safeClosestJunctionPills.size()-1);
			
			for(MOVE move: moves) {
				int pos = game.getNeighbour(current, move);
				// the first check is on the presence of ghosts in liar to avoid instant kill
				if(strategy.isThereGhostInLair(game) && 
						game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LIAR_DISTANCE &&
						move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
					returnValues[0] = 200;
					returnValues[1] = m;
					return returnValues;
				}
				else {
					// Eat the shortest distant pill safe
					if(safeClosestIndexPill != -1 && strategy.checkSafeChase(safeClosestIndexPill, game.getNeighbour(current, move), game) &&
							move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)) {
						GameView.addPoints(game,Color.cyan, game.getShortestPath(current, safeClosestIndexPill));
						cumulativePoints[m] += 50;
					}
					// Ms Pacman cannot reach safely a pill with shortest path before a ghost, so go to the nearest junction safe
					// in forecast to find a safe pill to eat from there
					else if(strategy.getNearestSafeJunction(game, pos) != -1) {
						GameView.addPoints(game,Color.red, strategy.getNearestSafeJunction(game, pos));
						cumulativePoints[m] += 20;
					}
					else {
						GHOST closestGhost = strategy.getClosestGhost(game, current);
						if(closestGhost != null &&
								game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < MIN_DISTANCE)
						{
							// heur1 must be used
							cumulativePoints[m] -= 200;
						}
						else {
							cumulativePoints[m] += 0;
						}
						if(closestGhost != null &&
								game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(closestGhost)) < game.getShortestPathDistance(pos, game.getGhostCurrentNodeIndex(closestGhost))) {
							cumulativePoints[m] += 10;
						}
						else {
							cumulativePoints[m] += 0;
						}
					}
				}
				m++;	
			}
			
			// Visualize safe junctions
			int[] safeNodes=new int[safeClosestJunctionPills.size()];		
			for(int i=0;i<safeNodes.length;i++)
				safeNodes[i]=safeClosestJunctionPills.get(i);
			GameView.addPoints(game,Color.pink, safeNodes);
						
			
			// Choose the move with highest utility
			int tmp = Integer.MIN_VALUE;
			for(int i=0; i<moves.length; i++) {
				if(cumulativePoints[i] >= tmp) {
					tmp = cumulativePoints[i];
					bestMove = i;
				}
			}
			
			
			returnValues[0] = cumulativePoints[bestMove];
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
			int[] cumulativePoints = new int[moves.length];
			int current = game.getPacmanCurrentNodeIndex();
		
			
			boolean chased = false;
			if(strategy.isMsPacManChased(game.getPacmanCurrentNodeIndex(), game) >= 2) {
				chased = true;
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
				GHOST closestGhost = strategy.getClosestGhost(game, current);
				GHOST edibleGhost = strategy.isThereEdibleGhost(game, current);
				int trapPowerPill = strategy.trapTheGhosts(game, current, powerPills);
				
				// MsPacman follows two simple rules: eat ghosts if possible, maybe trap them, or go away
				// when there is no chance of eating, possibly staying around power pills
				// the first check is on the presence of ghosts in liar to avoid instant kill
				if(strategy.isThereGhostInLair(game) && 
						game.getShortestPathDistance(current, game.getGhostInitialNodeIndex()) < LIAR_DISTANCE &&
						move == game.getNextMoveAwayFromTarget(current, game.getGhostInitialNodeIndex(), DM.PATH)) {
					cumulativePoints[m] += 200;
				}
				// MsPacman is chased and tries to fool the ghosts
				if(chased) {
					if(trapPowerPill != -1 && strategy.checkSafeChase(trapPowerPill, current, game) &&
						move == game.getNextMoveTowardsTarget(current, trapPowerPill, DM.PATH)) {
						GameView.addPoints(game, Color.ORANGE, game.getShortestPath(current, trapPowerPill));
						cumulativePoints[m] += 100;
					}
					else if(safeClosestIndexPill != -1 && strategy.checkSafeChase(safeClosestIndexPill, game.getNeighbour(current, move), game) &&
							move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)){
						cumulativePoints[m] += 90;
						GameView.addPoints(game, Color.cyan, game.getShortestPath(current, safeClosestIndexPill));
					}
				}
				// There is a safely edible ghost, go and catch it
				if(edibleGhost != null && strategy.checkSafeChase(game.getGhostCurrentNodeIndex(edibleGhost), game.getNeighbour(current, move), game)
						&& move == game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(edibleGhost), DM.PATH)
						) {
					cumulativePoints[m] += 200;
					GameView.addPoints(game, Color.green, game.getShortestPath(current, game.getGhostCurrentNodeIndex(edibleGhost)));
				}
				// If all ghosts move in opposite direction than ms pacman, just eat pills
				if(safeClosestIndexPill != -1 && strategy.checkSafeChase(safeClosestIndexPill, game.getNeighbour(current, move), game) &&
						move == game.getNextMoveTowardsTarget(current, safeClosestIndexPill, DM.PATH)) {
					GameView.addPoints(game,Color.cyan, game.getShortestPath(current, safeClosestIndexPill));
					cumulativePoints[m] += 5;
				}
//				if(strategy.getCloserGhost(game, current) != null && 
//						game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(strategy.getCloserGhost(game, current)), DM.PATH) == move)
//				{
//					GameView.addPoints(game, Color.yellow, game.getShortestPath(current,game.getGhostCurrentNodeIndex(strategy.getCloserGhost(game, current))));
//					cumulativePoints[m] += 10;
//				}
				else {
					cumulativePoints[m] -= 100;
				}
				m++;
			}
			
			
			// Choose the move with highest utility
			int tmp = 0;
			for(int i=0; i<moves.length; i++) {
				if(cumulativePoints[i] > tmp) {
					tmp = cumulativePoints[i];
					bestMove = i;
				}
			}
			
			if(bestMove == -1)
				returnValues[0] = 0;
			else
				returnValues[0] = cumulativePoints[bestMove];
			returnValues[1] = bestMove;
			return returnValues;
		}
}
