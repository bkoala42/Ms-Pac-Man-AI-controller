package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.List;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Subclasses HillClimb implementing the basic scheme of the hill climb algorithm. Selects the new node from
 * the neighborhood that has the maximum value
 */
public class BasicHillClimb extends HillClimb {

	public BasicHillClimb(Executor exec, Controller<MOVE> msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, List<ControllerParameter> parameters,
			List<Integer> initialParams) {
		super(exec, msPacManController, ghostController, parameters, initialParams);
	}

	@Override
	protected List<Object> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore) {
		List<Object> returnValue = null;
		double tmpScore = 0;
		List<Integer> nextNode = null;
		
		for(List<Integer> node: neighborhood) {
			// QUA VANNO NUOVAMENTE MESSI I PARAMETRI DEL NODO
			tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
			if(logEnabled)
				log.append("Node: "+node.toString()+" Value: "+tmpScore+"\r\n");
			
			if(tmpScore > currScore) {
				nextNode = node;
				currScore = tmpScore;
			}
		}
		
		returnValue.add(nextNode);
		returnValue.add(currScore);
		return returnValue;
	}

}
