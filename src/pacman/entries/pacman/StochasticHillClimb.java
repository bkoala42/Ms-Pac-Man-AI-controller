package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Subclasses HillClimb selecting the new node in the main loop of the algorithm picking randomly a node with higher value
 * than the current one
 */
public class StochasticHillClimb extends HillClimb {

	public StochasticHillClimb(Executor exec, MyMsPacMan msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, Map<String, ControllerParameter> parameters) {
		super(exec, msPacManController, ghostController, parameters);
	}

	@Override
	protected Map<Double, List<Integer>> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore, List<List<Integer>> alreadyVisitedNodes) {
		List<List<Integer>> uphillNodes = new ArrayList<List<Integer>>();
		List<Double> uphillScores = new ArrayList<Double>();
		Map<Double, List<Integer>> returnValue = new HashMap<Double, List<Integer>>();
		
		double tmpScore = 0;
		Random rand = new Random();
		// loop over all the nodes of the neighborhood and store all those having an improvement with respect to the current 
		// result. Then pick randomly one of them as new node
		for(List<Integer> node: neighborhood) {
			if(!alreadyVisitedNodes.contains(node)) {
				alreadyVisitedNodes.add(node);
				setControllerNewParameters(node);
				tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
				if(logEnabled)
					log.append("Inside new node selection loop, node: "+node.toString()+" Value: "+tmpScore+"\r\n");
				
				if(tmpScore > currScore) {
					uphillNodes.add(node);
					uphillScores.add(tmpScore);
				}
			}
		}
		
		int stochasticSelection = rand.nextInt(uphillNodes.size());
		returnValue.put(uphillScores.get(stochasticSelection), uphillNodes.get(stochasticSelection));
		return returnValue;
	}

}
