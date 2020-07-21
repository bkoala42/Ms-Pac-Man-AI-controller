package pacman.entries.pacman;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Subclasses HillClimb selecting the new node in the main loop of the algorithm picking the first node with higher value than 
 * the current one
 */
public class FirstChoiceHillClimb extends HillClimb {
	
	public FirstChoiceHillClimb(Executor exec, Controller<MOVE> msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, List<ControllerParameter> parameters,
			List<Integer> initialParams) {
		super(exec, msPacManController, ghostController, parameters, initialParams);
	}

	@Override
	protected List<Object> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore) {
		List<Object> returnValue = null;
		double tmpScore = 0;
		List<Integer> nextNode = null;
		
		Collections.shuffle(neighborhood);
		for(List<Integer> node: neighborhood) {
			// QUA VANNO NUOVAMENTE MESSI I PARAMETRI DEL NODO
			tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
			if(logEnabled)
				log.append("Node: "+node.toString()+" Value: "+tmpScore+"\r\n");
			
			if(tmpScore > currScore) {
				nextNode = node;
				currScore = tmpScore;
				break;
			}
		}
		
		returnValue.add(nextNode);
		returnValue.add(currScore);
		return returnValue;
	}
}
