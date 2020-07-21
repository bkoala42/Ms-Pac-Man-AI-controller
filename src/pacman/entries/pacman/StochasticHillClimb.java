package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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

	public StochasticHillClimb(Executor exec, Controller<MOVE> msPacManController,
			Controller<EnumMap<GHOST, MOVE>> ghostController, List<ControllerParameter> parameters,
			List<Integer> initialParams) {
		super(exec, msPacManController, ghostController, parameters, initialParams);
	}

	@Override
	protected List<Object> getNextClimbingNode(List<List<Integer>> neighborhood, double currScore) {
		List<List<Integer>> uphillNodes = new ArrayList<List<Integer>>();
		List<Double> uphillScores = new ArrayList<Double>();
		List<Object> returnValue = null;
		
		double tmpScore = 0;
		Random rand = new Random();
		for(List<Integer> node: neighborhood) {
			// QUA VANNO NUOVAMENTE MESSI I PARAMETRI DEL NODO
			tmpScore = exec.runExperiment(msPacManController, ghostController, trials);
			if(logEnabled)
				log.append("Node: "+node.toString()+" Value: "+tmpScore+"\r\n");
			
			if(tmpScore > currScore) {
				uphillNodes.add(node);
				uphillScores.add(tmpScore);
			}
		}
		
		int stochasticSelection = rand.nextInt(uphillNodes.size());
		returnValue.add(uphillNodes.get(stochasticSelection));
		returnValue.add(uphillScores.get(stochasticSelection));
		return returnValue;
	}

}
