package pacman.entries.pacman;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import com.google.common.collect.Lists;

/**
 * Abstract implementation of the hill climbing algorithm
 * Subclasses specialize the process of selection of the best node in the main loop of the algorithm
 */
public abstract class HillClimb {
	
	protected Executor exec;
	protected MyMsPacMan msPacManController;
	protected Controller<EnumMap<GHOST, MOVE>> ghostController;
	
	private Map<String, ControllerParameter> parameters;
	private List<List<Integer>> parametersSpace;
	
	protected int trials;
	
	private double bestValue;
	
	protected boolean logEnabled;
	private boolean randomStart;
	
	protected StringBuffer log;
	
	public HillClimb(Executor exec, MyMsPacMan msPacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, 
			Map<String, ControllerParameter> parameters) {
		this.exec = exec;
		this.msPacManController = msPacManController;
		this.ghostController = ghostController;
		this.parameters = parameters;
		this.logEnabled = false;
		this.randomStart = false;
		
		List<List<Integer>> parametersValues = new ArrayList<List<Integer>>();
		
		for(String par: this.parameters.keySet()) {
			parametersValues.add(this.parameters.get(par).getValues());
		}
		
		this.parametersSpace = Lists.cartesianProduct(parametersValues);
	}
	
	public void setRandomStart(boolean random) {
		this.randomStart = random;
	}
	
	public void setTrials(int trials) {
		this.trials = trials;
	}
	
	public void setLogEnabled(boolean enabled) {
		this.logEnabled = enabled;
	}
	
	public double getBestValue() {
		return this.bestValue;
	}
	
	public List<List<Integer>> getParametersSpace() {
		return this.parametersSpace; 
	}
	
	/**
	 * Finds the neighborhood of a state by considering all the possibe variations of the parameters
	 * @param state current hill climbing algorithm state
	 * @return all possible new states
	 */
	protected List<List<Integer>> getNeighborhood(List<Integer> state) {
		int negVariation, posVariation;
		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		int i = 0;
		
		for(String par: parameters.keySet()) {
			negVariation = parameters.get(par).getPreviousValue(state.get(i));
			posVariation = parameters.get(par).getNextValue(state.get(i));
			lists.add(Arrays.asList(new Integer[] {negVariation, posVariation}));
			i++;
		}
		
//		for(int i = 0; i < state.size(); i++) {
//			negVariation = parameters.get(i).getPreviousValue(state.get(i));
//			posVariation = parameters.get(i).getNextValue(state.get(i));
//			lists.add(Arrays.asList(new Integer[] {negVariation, posVariation}));
//		}
		
		return Lists.cartesianProduct(lists);
	}
	
	/**
	 * Get the initial state for the hill climbing loop
	 * @param randomized true if a random selection of the initial node has to be performed
	 * @return initial state 
	 */
	protected List<Integer> getInitialNode() {
		List<Integer> initialNode = null;
		Random rand = new Random();
		if(randomStart) {
			initialNode = parametersSpace.get(rand.nextInt(parametersSpace.size()));
		}	
		else {
			initialNode = parametersSpace.get(0);
		}
		return initialNode;
	}
	
	/**
	 * Get the new state from the neighborhood in the hill climbing loop
	 * @param neighborhood neighborhood of the current state
	 * @param currScore value of the current state
	 * @return new node for the hill climbing loop and the corresponding value
	 */
	protected abstract Map<Double, List<Integer>> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore, List<List<Integer>> alreadyVisitedNodes);
	
	/**
	 * Main loop of the hill climbing loop
	 * @param initialNode starting node of the local search
	 * @return node corresponding to maximum value found by local search
	 */
	public List<Integer> climbingLoop(List<Integer> initialNode) {
		double nextScore = 0, currScore = 0;
		boolean isImproving = true, endOfRun = false;
		List<List<Integer>> neighborhood = new LinkedList<List<Integer>>();
		Map<Double, List<Integer>> nodeSelectionResult;
		
		// always consider the already visited nodes to avoid computing on them again
		List<List<Integer>> alreadyVisitedNodes = new ArrayList<List<Integer>>();
		
		// check the time of computation of the hill climb
		long startTime = System.currentTimeMillis();
		
		if(logEnabled) {
			log = new StringBuffer("Hill climbing tuning: \r\n");
		}
		// initialize climbing loop taking the first node, set the node parameters for the controller
		List<Integer> currNode = getInitialNode();
		List<Integer> nextNode = null;
//		System.out.println("Iterating...");
		setControllerNewParameters(currNode);
		currScore = exec.runExperiment(msPacManController, ghostController, trials);
		msPacManController.printParameters();
		alreadyVisitedNodes.add(currNode);
//		System.out.println("Starting Node: "+currNode.toString()+" Value: "+currScore+"\r\n");
		if(logEnabled)
			log.append("Starting Node: "+currNode.toString()+" Value: "+currScore+"\r\n");
		
		while(isImproving) {
			System.out.println("Iterating...");
			neighborhood = getNeighborhood(currNode);
			nextScore = Double.NEGATIVE_INFINITY;
			
			// new node found by hill climbing
			nodeSelectionResult = getNextClimbingNode(neighborhood, currScore, alreadyVisitedNodes);
			// there is only one entry in the map
			for(Double score: nodeSelectionResult.keySet()) {
				nextScore = score;
				nextNode = nodeSelectionResult.get(score);
			}
//			System.out.println("Valori trovati "+nextNode+" "+nextScore);
			
			// update current best result
			if(nextNode == null) {
				isImproving = false;
//				System.out.println("Chiudo il loop "+isImproving);
			}
			else {
				currNode = nextNode;
				currScore = nextScore;
				
//				System.out.println("NEW BEST NODE: "+currNode.toString()+" Value: "+currScore);
				if(logEnabled)
					log.append("NEW BEST NODE: "+currNode.toString()+" Value: "+currScore+"\r\n");
			}
		}
		bestValue = currScore;
		if(logEnabled)
			log.append("Time: " + (System.currentTimeMillis() - startTime));
		
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
	
	protected void setControllerNewParameters(List<Integer> currentParameters) {
		int i = 0;
		for(String key: parameters.keySet()) {
			switch(key) {
				case "minGhostDistance":
					msPacManController.setMinGhostDistance(currentParameters.get(i));
					break;
				case "guardDistance":
					msPacManController.setGuardDistance(currentParameters.get(i));
					break;
				case "chaseDistance":
					msPacManController.setChaseDistance(currentParameters.get(i));
					break;
				case "eatDistance":
					msPacManController.seteatDistanceHigh(currentParameters.get(i));
					break;
				case "cleanDistance":
					msPacManController.setCleanDistance(currentParameters.get(i));
					break;
			}
			i++;
		}	
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

