package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Collections;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.io.FileWriter;
import java.io.IOException;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class MyPacMan extends Controller<MOVE>
{
	private MOVE myMove=MOVE.NEUTRAL;
	
	// ghost safe distance
	private int minDistance;
	// utility functions hyperparamenters
	private float[] hypParam0;
	private float[] hypParam1;
	private FileWriter averageGhostDistance = null;
	private FileWriter pillDistance = null;
	private FileWriter powerPillDistance = null;
	
	public MyPacMan(float[] hypParam0, float[] hypParam1, int minDistance) {
		this.hypParam0 = hypParam0;
		this.hypParam1 = hypParam1;
		this.minDistance = minDistance;
	}
	
	public MyPacMan(float[] hypParam0, float[] hypParam1, int minDistance, FileWriter[] files) {
		this.hypParam0 = hypParam0;
		this.hypParam1 = hypParam1;
		this.minDistance = minDistance;
		this.averageGhostDistance = files[0];
		this.pillDistance = files[2];
		this.powerPillDistance = files[1];
	}

	public MOVE getMove(Game game, long timeDue) 
	{
		//Place your game logic here to play the game as Ms Pac-Man
		
		// Get current MsPacman position
		int posMsPacman = game.getPacmanCurrentNodeIndex();
		
		// check if all ghosts are far enough
		boolean safe = true;
		for(GHOST ghost : GHOST.values()) {
			if(game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)) < minDistance) {
				safe = false;
				break;
			}
		}
		
		float[] distances = new float[3];
		double bestMove = 0, utility0 = 0, utility1 = 0, utility = 0;
		int newPosMsPacman = -1;
		GHOST edibleGhost = null;
		boolean hotPursuit = false;
		MOVE moves[] = game.getPossibleMoves(posMsPacman);
		myMove = MOVE.NEUTRAL;
		for(MOVE move : moves) {
			newPosMsPacman = game.getNeighbour(posMsPacman, move);
			if(newPosMsPacman != -1) {
				distances = getSensorData(game, newPosMsPacman);
				if(safe) {
					edibleGhost = isThereEdibleGhost(game, posMsPacman);
					if(edibleGhost !=  null) {
						myMove = chaseTheGhost(game, posMsPacman, edibleGhost);
						hotPursuit = true;
						//System.out.println("Ghost chase 0");
					}
					else {
						//utility = utility1(distances, hypParam1);
						utility = utility0(distances, hypParam0);
						//System.out.println("0 "+utility);
					}
				}
				else {
					edibleGhost = isThereEdibleGhost(game, posMsPacman);
					if(edibleGhost !=  null) {
						myMove = chaseTheGhost(game, posMsPacman, edibleGhost);
						hotPursuit = true;
						//System.out.println("Ghost chase 1");
					}
					else {
						//utility = utility1(distances, hypParam0);
						utility = utility1(distances, hypParam1);
						//System.out.println("1 "+utility);
					}
				}
			}
			if(!hotPursuit && utility > bestMove) {
				bestMove = utility;
				myMove = move;
			}
		}
		
		return myMove;
		
	}
	
	private GHOST isThereEdibleGhost(Game game, int posMsPacman) {
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>2)
			{
				int distance=game.getShortestPathDistance(posMsPacman,game.getGhostCurrentNodeIndex(ghost));
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		return minGhost;
	}
	
	private MOVE chaseTheGhost(Game game, int posMsPacman, GHOST edibleGhost) {
		int d = 1000000;
		// search for the nearest non edible ghost
		GHOST nonEdibleGhost = null;
		for(GHOST g : GHOST.values()) {
			if(game.getGhostEdibleTime(g)<=0 && game.getShortestPathDistance(posMsPacman,game.getGhostCurrentNodeIndex(g)) < d ||
			        game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(edibleGhost)) < minDistance) {
				d = game.getShortestPathDistance(posMsPacman,game.getGhostCurrentNodeIndex(g));
				nonEdibleGhost = g;
			}
		}
		if(nonEdibleGhost != null && d < game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(edibleGhost))) {
			//System.out.println("Panic");
			return game.getNextMoveAwayFromTarget(posMsPacman, game.getGhostCurrentNodeIndex(nonEdibleGhost), DM.PATH);
		}
		else
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(edibleGhost),DM.PATH);
	}
	
	
	
	/**
	 * Retrieves sensor information for MsPacman agent
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @return array of distances information, in order: average ghosts distance, nearest power pill, nearest pill
	 */
	private float[] getSensorData(Game game, int posMsPacman) {
		float[] distances = new float[3];
		distances[0] = weightedGhostsDistance(game, posMsPacman);
		distances[1] = nearestPillDistance(game, posMsPacman, true);
		distances[2] = nearestPillDistance(game, posMsPacman, false);
		
//		try {
//			this.averageGhostDistance.write(distances[0]+"\n");
//			this.pillDistance.write(distances[1]+"\n");
//			this.powerPillDistance.write(distances[2]+"\n");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		return distances;
	}
	
	/**
	 * Computes a weighted average of the shortest path distance of MsPacman from ghosts
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @return weighted average distance from ghosts
	 */
	private float weightedGhostsDistance(Game game, int posMsPacman) {
		ArrayList<Float> distances = new ArrayList<Float>(4);
		for(GHOST ghost : GHOST.values()) {
			if(game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)) == -1)
				distances.add((float)100);
			else
				distances.add((float)game.getShortestPathDistance(posMsPacman, game.getGhostCurrentNodeIndex(ghost)));
		}
		float maxDistance = Collections.max(distances);
		float meanDistance = 0;
		for(Float dist : distances) {
			meanDistance += (1-(dist/maxDistance))*dist;
		}
		return meanDistance/4;
	}
	
	/**
	 * Computes the distance of the nearest active power pill or simple pill from MsPacman
	 * @param  game game instance
	 * @param  posMsPacman current index of MsPacman
	 * @param  isPowerPill true if power pill, else simple pill
	 * @return distance from the nearest power pill or simple pill
	 */
	private int nearestPillDistance(Game game, int posMsPacman, boolean isPowerPill) {
		int[] pills=game.getPillIndices();
		int[] powerPills=game.getPowerPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
//		for(i=0;i<targetsArray.length;i++)
//			System.out.println(targetsArray[i]);
		//System.out.println(game.getClosestNodeIndexFromNodeIndex(posMsPacman,targetsArray,DM.PATH));
		return game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), 
				game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH));
//		if(isPowerPill) {
//			pillIndices = game.getActivePowerPillsIndices();
//		}
//		else {
//			pillIndices = game.getActivePillsIndices();
//		}
//		for(i = 0; i < pillIndices.length; i++) {
//			if(game.getShortestPathDistance(posMsPacman, pillIndices[i]) < minDistance)
//				minDistance = game.getShortestPathDistance(posMsPacman, pillIndices[i]);
//		}
//		return minDistance;
	}
	
	/**
	 * Objective function (utility) for "conservative" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility0(float[] distances, float[] hypParam) {
		double eps = 0.001;
		return 1-Math.exp(-hypParam[0]*distances[0]) + 1-Math.exp(-hypParam[1]*distances[1]) + Math.exp(-hypParam[2]*distances[2]);
	}
	
	/**
	 * Objective function (utility) for "hot pursuit" strategy
	 * @param  distances array containing, in order, average distance from ghosts, distance from nearest power pill, 
	 *                   distance from nearest pill
	 * @param  hypParam hyperparameters for the utility function
	 * @return utility value
	 */
	private double utility1(float[] distances, float[] hypParam) {
		double eps = 0.001;
		return 1-Math.exp(-hypParam[0]*distances[0]) + Math.exp(-hypParam[1]*distances[1]) + 1-Math.exp(-hypParam[2]*distances[2]);
	}
}