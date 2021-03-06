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
 * Subclasses specialize the process of selection of the best node in the main loop of the algorithm,
 * according to the Template Method pattern
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
		
		// build the parameters space computing the cartesian product of the domains of the parameters
		List<List<Integer>> parametersValues = new ArrayList<List<Integer>>();
		
		for(String par: this.parameters.keySet()) {
			parametersValues.add(this.parameters.get(par).getValues());
		}
		
		this.parametersSpace = Lists.cartesianProduct(parametersValues);
	}
	
	/**
	 * Setter to define random restart
	 * @param random true if random restart is required, false otherwise
	 */
	public void setRandomStart(boolean random) {
		this.randomStart = random;
	}
	
	/**
	 * Setter for the number of games to be played to compute the value of the current node
	 * @param trials number of games
	 */
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
	
	public StringBuffer getLog() {
		return this.log;
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
		boolean isImproving = true;
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
		
		// prepare the agent to the experiment setting the current parameters of the node under examination
		setControllerNewParameters(currNode);
		msPacManController.printParameters();
		currScore = exec.runExperiment(msPacManController, ghostController, trials);
		alreadyVisitedNodes.add(currNode);
		if(logEnabled)
			log.append("Starting Node: "+currNode.toString()+" Value: "+currScore+"\r\n");
		
		while(isImproving) {
			neighborhood = getNeighborhood(currNode);
			nextScore = Double.NEGATIVE_INFINITY;
			
			// new node found by hill climbing
			nodeSelectionResult = getNextClimbingNode(neighborhood, currScore, alreadyVisitedNodes);
			// there is only one entry in the map
			for(Double score: nodeSelectionResult.keySet()) {
				nextScore = score;
				nextNode = nodeSelectionResult.get(score);
			}
			
			// update current best result
			if(nextNode == null) {
				isImproving = false;
			}
			else {
				currNode = nextNode;
				currScore = nextScore;
				
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
	
	/**
	 * Updates the agent with the new parameters
	 * @param currentParameters parameters values to update
	 */
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
	
}

