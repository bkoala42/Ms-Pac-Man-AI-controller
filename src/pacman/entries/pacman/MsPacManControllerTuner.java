package pacman.entries.pacman;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.RandomGhosts;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Program to start the hill climbing optimization procedure of the agent.
 *
 */
public class MsPacManControllerTuner {
	
	public static void main(String [] args) {
		Executor exec = new Executor();
		MyMsPacMan msPacManController = new MyMsPacMan();
		
		// select the ghost team against which tune the controller
		Controller<EnumMap<GHOST, MOVE>> ghostController = new RandomGhosts();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new AggressiveGhosts();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new Legacy();
//		Controller<EnumMap<GHOST, MOVE>> ghostController = new Legacy2TheReckoning();
		
		// define the set of parameters used by the controller. Their range is defined as equivalence classes
		// decided by experimental observation of the controller against the different teams. Five possible 
		// hyper parameters were considered
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
		
		// Put all hyper-parameters in a data structure for the hill climbing algorithm
		Map<String, ControllerParameter> controllerParameters = new LinkedHashMap<String, ControllerParameter>();
		controllerParameters.put(minGhostDistance.getName(), minGhostDistance);
		controllerParameters.put(guardDistance.getName(), guardDistance);
		controllerParameters.put(chaseDistance.getName(), chaseDistance);
		controllerParameters.put(eatDistance.getName(), eatDistance);
		controllerParameters.put(cleanDistance.getName(), cleanDistance);
		
		HillClimb hillClimb;
		
		// tune using basic hill climbing
//		hillClimb = new BasicHillClimb(exec, msPacManController, ghostController, controllerParameters);
		
		// tune using stochastic hill climbing
//		hillClimb = new StochasticHillClimb(exec, msPacManController, ghostController, controllerParameters);
//	
		// tune using first choice hill climbing
		hillClimb = new FirstChoiceHillClimb(exec, msPacManController, ghostController, controllerParameters);

		// Set hill climbing random restart
		hillClimb.setRandomStart(true);
		// enable/disable the log file to check the training procedure
		hillClimb.setLogEnabled(true);
		// set number of trials to assess the score
		hillClimb.setTrials(10);

		// Define a new log to check the entire training procedure
		StringBuffer log = new StringBuffer("Hill climbing tuning with epochs: \r\n");
		
		double maxScore = 0, tmpScore = 0;
		// set the number of hill climbing restarts
		int epochs = 5;
		List<Integer> optParametersValues = null; 
		while(epochs > 0) {
			optParametersValues = hillClimb.climbingLoop(hillClimb.getInitialNode());
			tmpScore = hillClimb.getBestValue();
			System.out.println("Hill climb loop result "+tmpScore);
			log.append(hillClimb.getLog().toString()+"\r\n");
			if(tmpScore > maxScore) {
				maxScore = tmpScore;
			}
			log.append("End of epoch "+epochs+"\r\n\r\n");
			System.out.println("End of epoch "+epochs);
			epochs--;
		}
		
		try {
			FileWriter logFile = new FileWriter("hill_climb_epochs.txt");
			logFile.write(log.toString());
			logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("The best parameters combination is "+optParametersValues+" leading to a score of "+maxScore);
		
	}
}
