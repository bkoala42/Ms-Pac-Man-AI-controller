package pacman.entries.pacman;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import com.google.common.collect.Lists;

public class HillClimb {
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 50;
	
	private Executor exec;
	private Controller<MOVE> msPacManController;
	private Controller<EnumMap<GHOST, MOVE>> ghostController;
	private Integer[] distParams;
	private int delta;
	private int trials;
	private boolean logEnabled;
	
	public HillClimb(Executor exec, Controller<MOVE> msPacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, 
			Integer[] distParams) {
		this.exec = exec;
		this.msPacManController = msPacManController;
		this.ghostController = ghostController;
		this.distParams = distParams;
		this.logEnabled = false;
	}
	
	public void setDelta(int delta) {
		this.delta = delta;
	}
	
	public void setTrials(int trials) {
		this.trials = trials;
	}
	
	public void setLogEnabled(boolean enabled) {
		this.logEnabled = enabled;
	}
	
	/**
	 * Hill climb algorithm to optimize the distance measures used by MsPacMan controller
	 * @return array of best parameters
	 */
	public Integer[] tuneDistParams() {
		double nextScore = 0, currScore = 0, tmpScore = 0;
		boolean isImproving = true;
		List<List<Integer>> neighborhood = new LinkedList<List<Integer>>();
		
		StringBuffer log = null;
		if(logEnabled) {
			log = new StringBuffer("Hill climbing tuning: \r\n");
		}
		
		Integer[] currNode = distParams, nextNode = null;
		currScore = exec.runExperiment(msPacManController, ghostController, trials);
		if(logEnabled)
			log.append("Node: "+currNode.toString()+" Value: "+currScore);
		
		while(isImproving) {
			neighborhood = getNeighborhood(currNode);
			nextScore = Double.NEGATIVE_INFINITY;
			nextNode = null;
			// check the score using the distance parameters of the neighborhood
			for(List<Integer> node: neighborhood) {
				tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
				if(logEnabled)
					log.append("Node: "+node.toString()+" Value: "+tmpScore+"\r\n");
				
				if(tmpScore > currScore) {
					nextNode = node.toArray(new Integer[0]);
					nextScore = tmpScore;
				}
			}
			// update current best result
			if(nextScore <= currScore)
				isImproving = false;
			else {
				currNode = nextNode;
				currScore = nextScore;
				neighborhood.clear();
				
				if(logEnabled)
					log.append("NEW BEST NODE: "+currNode.toString()+" Value: "+currScore+"\r\n");
			}
		}
		
		if(logEnabled) {
			try {
				FileWriter logFile = new FileWriter("hill_climb_log.txt");
				logFile.write(log.toString());
				logFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return currNode;
	}
	
	/**
	 * Finds the neighborhood of a state by considering all the possibe variations of the parameters
	 * @param state current hill climbing algorithm state
	 * @param delta	step ov variation of the distance
	 * @return all possible new states
	 */
	private List<List<Integer>> getNeighborhood(Integer[] state) {
		int negVariation, posVariation;
		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		for(Integer i: state) {
			if(i - delta <= MIN_VALUE) {
				negVariation = MIN_VALUE;
			}
			else {
				negVariation = i - delta;
			}
			
			if(i + delta >= MAX_VALUE) {
				posVariation = MAX_VALUE;
			}
			else {
				posVariation = i + delta;
			}
			lists.add(Arrays.asList(new Integer[] {negVariation, posVariation}));
		}
		
		return Lists.cartesianProduct(lists);
	}
	
//	public static void main(String [] args) {
//		Integer[] state = {3, 4, 5, 6};
//		int delta = 1;
//		
//		List<List<Integer>> lists = new ArrayList<List<Integer>>();
//		for(Integer i: state) {
//			lists.add(Arrays.asList(new Integer[] {i-delta, i+delta}));
//		}
//		
//		List<List<Integer>> res = Lists.cartesianProduct(lists);
//		for(List<Integer> l: res)
//			System.out.println(l);
//	}
}

