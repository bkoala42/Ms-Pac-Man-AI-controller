package pacman;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.NearestPillPacManVS;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;

import pacman.entries.pacman.*;

import java.io.FileWriter;
import java.io.IOException;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Executor
{	
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	
	// define hyperparameters range of variation
	static float maxGamma0 = 6;
	static float maxGamma1 = 6;
	static float minGamma0 = 2;
	static float minGamma1 = 1;
	static float maxAlfa0 = 2;
	static float maxAlfa1 = 2;
	static float minAlfa0 = 1;
	static float minAlfa1 = 1;
	static float maxBeta0 = 2;
	static float maxBeta1 = 4;
	static float minBeta0 = 1;
	static float minBeta1 = 1;
	static int minGhostDistance = 5;
	static int maxGhostDistance = 10;
	
	// values for simulations assessments
	static int numTrials = 100;
			
	public static void main(String[] args)
	{
		Executor exec=new Executor();
		
		// files for distances management
		FileWriter ghostsDistancesFile = null;
		FileWriter powerPillDistancesFile = null;
		FileWriter pillDistancesFile = null;
		try {
			ghostsDistancesFile = new FileWriter("ghostDistances.txt");
			powerPillDistancesFile = new FileWriter("powerPillDistances.txt");
			pillDistancesFile = new FileWriter("pillDistances.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter[] files = {ghostsDistancesFile, powerPillDistancesFile, pillDistancesFile};
		
		/*
		// start best parameters search with min values
		float[] hypParam0 = {minAlfa0, minBeta0, maxGamma0};
		float[] hypParam1 = {minAlfa1, maxBeta1, minGamma1};
		int ghostDistance = maxGhostDistance;
		
		
		int i = 0, lateralMovesPlateau = 15;
		double prevScore = 0, currScore = 0;
		boolean betterSolution = false;
		// compute initial score with conservative values
		prevScore = 0;
		currScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance, files),new AggressiveGhosts(),numTrials);
		System.out.println("Starting score: "+currScore);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		while(currScore > prevScore || lateralMovesPlateau > 0) {
			prevScore = currScore;
			currScore = adjustHyperParameter(exec, hypParam0, hypParam1, ghostDistance, prevScore);
			if(currScore == prevScore)
				lateralMovesPlateau--;
			else
				lateralMovesPlateau = 15;
			System.out.println("Iteration "+i+" new score: "+currScore);
			i++;
		}
		for (float element: hypParam0) {
            System.out.println(element);
        }
		System.out.println();
		for (float element: hypParam1) {
            System.out.println(element);
        }
		System.out.println(ghostDistance);
		*/
		
		//run multiple games in batch mode - good for testing.
		//int numTrials=200;
		//exec.runExperiment(new MyPacMan(),new RandomGhosts(),numTrials);
		//exec.runExperiment(new MyPacMan(),new AggressiveGhosts(),numTrials);
		//exec.runExperiment(new MyPacMan(),new Legacy(),numTrials);
		//exec.runExperiment(new MyPacMan(),new Legacy2TheReckoning(),numTrials);
		
		
		/*
		//run a game in synchronous mode: game waits until controllers respond.
		int delay=5;
		boolean visual=true;
		exec.runGame(new MyPacMan(),new RandomGhosts(),visual,delay);
  		*/
		
		
		//run the game in asynchronous mode.
		float[] hypParam0 = {1, 1, 5.96f};
		float[] hypParam1 = {1, 3.94f, 1.149f};
		int ghostDistance = 10;
		boolean visual=true;
//		exec.runGameTimed(new NearestPillPacMan(),new AggressiveGhosts(),visual);
//		exec.runGameTimed(new StarterPacMan(),new StarterGhosts(),visual);
//		exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);	
		exec.runGameTimed(new MyPacMan(hypParam0, hypParam1, ghostDistance, files),new AggressiveGhosts(),visual);
		
		/*
		try {
			ghostsDistancesFile.close();
			powerPillDistancesFile.close();
			pillDistancesFile.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		/*
		//run the game in asynchronous mode but advance as soon as both controllers are ready  - this is the mode of the competition.
		//time limit of DELAY ms still applies.
		boolean visual=true;
		boolean fixedTime=false;
		exec.runGameTimedSpeedOptimised(new MyPacMan(hypParam0, hypParam1, ghostDistance),new RandomGhosts(),fixedTime,visual);
		*/
		
		/*
		//run game in asynchronous mode and record it to file for replay at a later stage.
		boolean visual=true;
		String fileName="replay.txt";
		exec.runGameTimedRecorded(new HumanController(new KeyBoardInput()),new RandomGhosts(),visual,fileName);
		//exec.replayGame(fileName,visual);
		 */
	}
	
	/**
	 * Tries to change one hyperparameter at time, following the direction of best score improvement.
	 * VERY SILLY IMPLEMENTATION, NO CARE ABOUT GHOST TEAM STRATEGY, MANUALLY CHANGE IT
	 * @param exec	game executor
	 * @param hypParam0  utility0 hyperparameters
	 * @param hypParam1  utility1 hyperparameters
	 * @param minDistance  minimum sasfe distance from ghosts
	 * @param score current score reached
	 * @return new score if a hyperparameter was changed, score otherwise
	 */
	public static double adjustHyperParameter(Executor exec, float[] hypParam0, float[] hypParam1, int minDistance, double score) {
		int delta = 100;
		double newScore = 0;
		int newMinDistance = 0;
		float[] testParam = new float[3];
		ArrayList<Double> scoreVariation = new ArrayList<>(7);
		ArrayList<Double> scoreDifference = new ArrayList<>(7);
		// parameter after parameter evaluate which one gives the best results
		// adjust alfa0
		float newAlfa0 = hypParam0[0] + (maxAlfa0 - minAlfa0)/delta;
		testParam[0] = newAlfa0;  testParam[1] = hypParam0[1]; testParam[2] = hypParam0[2];
		newScore = exec.runExperiment(new MyPacMan(testParam, hypParam1, minDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// adjust alfa1
		float newAlfa1 = hypParam1[0] + (maxAlfa1 - minAlfa1)/delta;
		testParam[0] = newAlfa1;  testParam[1] = hypParam1[1]; testParam[2] = hypParam1[2];
		newScore = exec.runExperiment(new MyPacMan(hypParam0, testParam, minDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// adjust beta0
		float newBeta0 = hypParam0[1] + (maxBeta0 - minBeta0)/delta;
		testParam[0] = hypParam0[0];  testParam[1] = newBeta0; testParam[2] = hypParam0[2];
		newScore = exec.runExperiment(new MyPacMan(testParam, hypParam1, minDistance),new AggressiveGhosts(),numTrials);
		scoreVariation.add(newScore);
		// adjust beta1
		float newBeta1 = hypParam1[1] - (maxBeta1 - minBeta1)/delta;
		testParam[0] = hypParam1[0];  testParam[1] = newBeta1; testParam[2] = hypParam1[2];
		newScore = exec.runExperiment(new MyPacMan(hypParam0, testParam, minDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// adjust gamma0
		float newGamma0 = hypParam0[2] - (maxGamma0 - minGamma0)/delta;
		testParam[0] = hypParam0[0];  testParam[1] = hypParam0[1]; testParam[2] = newGamma0;
		newScore = exec.runExperiment(new MyPacMan(testParam, hypParam1, minDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// adjust gamma1
		float newGamma1 = hypParam1[2] + (maxGamma1 - minGamma1)/delta;
		testParam[0] = hypParam1[0];  testParam[1] = hypParam1[1]; testParam[2] = newGamma1;
		newScore = exec.runExperiment(new MyPacMan(hypParam0, testParam, minDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// adjust minDistance
		if(minDistance > minGhostDistance)
			newMinDistance = minDistance - 1;
		newScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, newMinDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new AggressiveGhosts(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy(),numTrials);
		//prevScore = exec.runExperiment(new MyPacMan(hypParam0, hypParam1, maxGhostDistance),new Legacy2TheReckoning(),numTrials);
		scoreVariation.add(newScore);
		// pick the best hyperparamter variation
		for(Double d : scoreVariation) {
			scoreDifference.add(d - score);
		}
		if(Collections.max(scoreDifference) > 0) {
			int bestAdjustment = scoreDifference.indexOf(Collections.max(scoreDifference));
			switch(bestAdjustment) {
				case 0:
					// alfa0 adjustment was the best
					System.out.println("Adjusted alfa0");
					hypParam0[0] = newAlfa0;
					break;
				case 1:
					System.out.println("Adjusted alfa1");
					hypParam1[0] = newAlfa1;
					// alfa1 adjustment was the best
					break;
				case 2:
					System.out.println("Adjusted beta0");
					hypParam0[1] = newBeta0;
					// beta0 adjustment was the best
					break;
				case 3:
					System.out.println("Adjusted beta1");
					hypParam1[1] = newBeta1;
					// beta1 adjustment was the best
					break;
				case 4:
					// gamma0 adjustment was the best
					System.out.println("Adjusted gamma0");
					hypParam0[2] = newGamma0;
					break;
				case 5:
					hypParam1[2] = newGamma1;
					System.out.println("Adjusted gamma1");
					// gamma1 adjustment was the best
					break;
				case 6:
					// minDistance adjustment was the best
					System.out.println("Adjusted min distance");
					minDistance = newMinDistance;
					break;
			}
			return scoreVariation.get(bestAdjustment);
		}
		else
			return score;
	}
	
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public double runExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	double avgScore=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
			//System.out.println(i+"\t"+game.getScore());
		}
		
		//System.out.println(avgScore/trials);
		return avgScore/trials;
    }
	
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public void runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
	}
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        	gv.repaint();
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
	
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
 			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

 			try
			{
				int waited=DELAY/INTERVAL_WAIT;
				
				for(int j=0;j<DELAY/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((DELAY/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
 	        if(visual)
 	        	gv.repaint();
 		}
 		
 		pacManController.terminate();
 		ghostController.terminate();
 	}
    
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,String fileName)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}