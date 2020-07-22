package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Subclasses HillClimb implementing the basic scheme of the hill climb algorithm. Selects the new node from
 * the neighborhood that has the maximum value
 */
public class BasicHillClimb extends HillClimb {

	public BasicHillClimb(Executor exec, MyMsPacMan msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, Map<String, ControllerParameter> parameters) {
		super(exec, msPacManController, ghostController, parameters);
	}

	@Override
	protected Map<Double, List<Integer>> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore, List<List<Integer>> alreadyVisitedNodes) {
		Map<Double, List<Integer>> returnValue = new HashMap<Double, List<Integer>>();
		double tmpScore = 0;
		List<Integer> nextNode = null;
		
		// loop over all the nodes of the neighborhood and find the one with the highest value
		for(List<Integer> node: neighborhood) {
			if(!alreadyVisitedNodes.contains(node)) {
				alreadyVisitedNodes.add(node);
				setControllerNewParameters(node);
				msPacManController.printParameters();
				tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
				if(logEnabled)
					log.append("Inside new node selection loop, node: "+node.toString()+" Value: "+tmpScore+"\r\n");
				
				if(tmpScore > currScore) {
					nextNode = node;
					currScore = tmpScore;
				}
			}
		}
		
		returnValue.put(currScore, nextNode);
		return returnValue;
	}

}
