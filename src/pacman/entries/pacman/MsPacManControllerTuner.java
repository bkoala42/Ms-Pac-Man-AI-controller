package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.RandomGhosts;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MsPacManControllerTuner {
	
	public static void main(String [] args) {
		Executor exec = new Executor();
		Controller<MOVE> msPacManController = new GreedyMsPacManBan();
		
		// select the ghost team against which tune the controller
		Controller<EnumMap<GHOST, MOVE>> ghostController = new RandomGhosts();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new AggressiveGhosts();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new Legacy();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new Legacy2TheReckoning();
		
		// define the set of parameters used by the controller. Their range is defined as equivalence classes
		// decided by experimental observation of the controller against the different teams
		List<Integer> minGhostDistanceValues = new ArrayList<Integer>();
		minGhostDistanceValues.add(20);
		minGhostDistanceValues.add(25);
		minGhostDistanceValues.add(30);
		minGhostDistanceValues.add(35);
		minGhostDistanceValues.add(40);
		ControllerParameter minGhostDistance = new ControllerParameter("minGhostDistance", minGhostDistanceValues);
		
		List<Integer> guardDistanceValues = new ArrayList<Integer>();
		guardDistanceValues.add(3);
		guardDistanceValues.add(4);
		guardDistanceValues.add(5);
		guardDistanceValues.add(6);
		guardDistanceValues.add(7);
		ControllerParameter guardDistance = new ControllerParameter("guardDistance", guardDistanceValues);
		
		List<Integer> chaseDistanceValues = new ArrayList<Integer>();
		chaseDistanceValues.add(35);
		chaseDistanceValues.add(40);
		chaseDistanceValues.add(45);
		chaseDistanceValues.add(50);
		ControllerParameter chaseDistance = new ControllerParameter("chaseDistance", chaseDistanceValues);
		
		List<Integer> eatDistanceValues = new ArrayList<Integer>();
		eatDistanceValues.add(25);
		eatDistanceValues.add(30);
		eatDistanceValues.add(35);
		ControllerParameter eatDistance = new ControllerParameter("eatDistance", eatDistanceValues);
		
		List<Integer> cleanDistanceValues = new ArrayList<Integer>();
		cleanDistanceValues.add(10);
		cleanDistanceValues.add(12);
		cleanDistanceValues.add(14);
		cleanDistanceValues.add(16);
		cleanDistanceValues.add(18);
		ControllerParameter cleanDistance = new ControllerParameter("cleanDistance", cleanDistanceValues);
		
		List<ControllerParameter> controllerParameters = new ArrayList<ControllerParameter>();
		controllerParameters.add(minGhostDistance);
		controllerParameters.add(guardDistance);
		controllerParameters.add(chaseDistance);
		controllerParameters.add(eatDistance);
		controllerParameters.add(cleanDistance);
		
		// the initial parameters can also be fixed to values decided a priori
		List<Integer> initialParams = new ArrayList<Integer>();
		initialParams.add(25);				// ghost distance
		initialParams.add(5);				// guard distance
		initialParams.add(50);				// chase distance
		initialParams.add(30);				// eat distance
		initialParams.add(12);				// clean distance
		
		HillClimb hillClimb;
		
		// tune using basic hill climbing
		hillClimb = new BasicHillClimb(exec, msPacManController, ghostController, controllerParameters, initialParams);
		
		// tune using stochastic hill climbing
		hillClimb = new StochasticHillClimb(exec, msPacManController, ghostController, controllerParameters, initialParams);
		
		// tune using first choice hill climbing
		hillClimb = new FirstChoiceHillClimb(exec, msPacManController, ghostController, controllerParameters, initialParams);
		
		// enable/disable the log file to check the training procedure
		hillClimb.setLogEnabled(true);
		// set randomized start of the process
		hillClimb.setRandomStart(true);
		
		double maxScore = 0, tmpScore = 0;
		
		int epochs = 10;
		List<Integer> optParametersValues = null; 
		while(epochs > 0) {
			optParametersValues = hillClimb.climbingLoop(hillClimb.getInitialNode());
			tmpScore = hillClimb.getBestValue();
			if(tmpScore > maxScore) {
				maxScore = tmpScore;
			}
		}
		
		System.out.println("The best parameters combination is "+optParametersValues+" leading to a score of"+maxScore);
		
	}
}
