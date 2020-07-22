package pacman.entries.pacman;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Subclasses HillClimb selecting the new node in the main loop of the algorithm picking the first node with higher value than 
 * the current one
 */
public class FirstChoiceHillClimb extends HillClimb {
	
	public FirstChoiceHillClimb(Executor exec, MyMsPacMan msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, Map<String, ControllerParameter> parameters) {
		super(exec, msPacManController, ghostController, parameters);
	}

	@Override
	protected Map<Double, List<Integer>> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore, List<List<Integer>> alreadyVisitedNodes) {
		Map<Double, List<Integer>> returnValue = new HashMap<Double, List<Integer>>();
		double tmpScore = 0;
		List<Integer> nextNode = null;
		
		// Shuffle the neighborhood and pick the first node that improves the current node value	
		Collections.shuffle(neighborhood);
		for(List<Integer> node: neighborhood) {
			if(!alreadyVisitedNodes.contains(node)) {
				alreadyVisitedNodes.add(node);
				setControllerNewParameters(node);
				tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
				if(logEnabled)
					log.append("Inside new node selection loop, node: "+node.toString()+" Value: "+tmpScore+"\r\n");

				if(tmpScore > currScore) {
					nextNode = node;
					currScore = tmpScore;
					break;
				}
			}
		}
		
		returnValue.put(currScore, nextNode);
		return returnValue;
	}
}
