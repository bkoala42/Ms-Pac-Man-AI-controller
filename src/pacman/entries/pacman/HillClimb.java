package pacman.entries.pacman;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
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
	protected Controller<MOVE> msPacManController;
	protected Controller<EnumMap<GHOST, MOVE>> ghostController;
	
	private List<ControllerParameter> parameters;
	private List<Integer> initialParams;
	private List<List<Integer>> parametersSpace;
	
	protected int trials;
	
	private double bestValue;
	
	protected boolean logEnabled;
	private boolean randomStart;
	
	protected StringBuffer log;
	
	public HillClimb(Executor exec, Controller<MOVE> msPacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, 
			List<ControllerParameter> parameters, List<Integer> initialParams) {
		this.exec = exec;
		this.msPacManController = msPacManController;
		this.ghostController = ghostController;
		this.parameters = parameters;
		this.initialParams = initialParams;
		this.logEnabled = false;
		this.randomStart = false;
		
		List<List<Integer>> parametersValues = new ArrayList<List<Integer>>();
		
		for(ControllerParameter par: this.parameters) {
			parametersValues.add(par.getValues());
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
	
	/**
	 * Finds the neighborhood of a state by considering all the possibe variations of the parameters
	 * @param state current hill climbing algorithm state
	 * @return all possible new states
	 */
	private List<List<Integer>> getNeighborhood(List<Integer> state) {
		int negVariation, posVariation;
		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		
		for(int i = 0; i < state.size(); i++) {
			negVariation = parameters.get(i).getPreviousValue(state.get(i));
			posVariation = parameters.get(i).getNextValue(state.get(i));
			lists.add(Arrays.asList(new Integer[] {negVariation, posVariation}));
		}
		
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
			initialNode = initialParams;
		}
		return initialNode;
	}
	
	/**
	 * Get the new state from the neighborhood in the hill climbing loop
	 * @param neighborhood neighborhood of the current state
	 * @param currScore value of the current state
	 * @return new node for the hill climbing loop
	 */
	protected abstract List<Object> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore);
	
	/**
	 * Main loop of the hill climbing loop
	 * @param initialNode starting node of the local search
	 * @return node corresponding to maximum value found by local search
	 */
	public List<Integer> climbingLoop(List<Integer> initialNode) {
		double nextScore = 0, currScore = 0;
		boolean isImproving = true;
		List<List<Integer>> neighborhood = new LinkedList<List<Integer>>();
		List<Object> nodeSelectionResult;
		
		// check the time of computation of the hill climb
		long startTime = System.currentTimeMillis();
		
		if(logEnabled) {
			log = new StringBuffer("Hill climbing tuning: \r\n");
		}
		// initialize climbing loop taking the first node, set the node parameters for the controller
		List<Integer> currNode = getInitialNode();
		List<Integer> nextNode = null;
		// QUA VA CHIAMATO UN SETTER PER IL CONTROLLER CHE METTE I PARAMETRI
		currScore = exec.runExperiment(msPacManController, ghostController, trials);
		if(logEnabled)
			log.append("Starting Node: "+currNode.toString()+" Value: "+currScore);
		
		while(isImproving) {
			neighborhood = getNeighborhood(currNode);
			nextScore = Double.NEGATIVE_INFINITY;
			// check the score using the distance parameters of the neighborhood
			
			nodeSelectionResult = getNextClimbingNode(neighborhood, currScore);
			nextNode = (List<Integer>) nodeSelectionResult.get(0);
			nextScore = (double)nodeSelectionResult.get(1);
//			nextScore = exec.runExperiment(msPacManController, ghostController, trials);
			
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

