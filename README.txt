In the following the code that has been updated or added to the original Ms Pac-Man package is specified. All other files 
do not change with respect to the starting code repository.
-- indicates directories
++ indicates files

-- src		                 
   -- pacman
      ++ Executor.java			  contains main, all the game modalities are provided commented with the  
					  default implemented controller ready and, as an example, the asynchronous 
					  mode is left uncommented
      -- entries/pacman
         ++ MyMsPacMan.java		  contains the utility-based agent program
         ++ MyMsPacManStrategy.java	  contains the search strategies adopted in the evaluation of the utility
	 ++ HillClimb.java		  abstract implementation of the hill climb algorithm
	 ++ BasicHillClimb.java		  implementation of the basic hill climb algorithm
	 ++ StochasticHillClimb.java      implementation of the stochastic version of the hill climb algorithm
	 ++ FirstChoiceHillClimb.java	  implementation of the first choice hill climb algorithm
	 ++ ControllerParameter.java	  class abstracting the concept of hyper-parameter
	 ++ MsPacManControllerTuner.java  contains the learning procedure for the agent using a hill climb algorithm implementation

NB: The code has dependency on guava library, download at https://github.com/google/guava/wiki/Release23. For security concerns, .jar
files can not be attached in e-mails

To verify the agent abilities run games in visual mode or batch mode following the instructions in Executor.java. To start a learning 
procedure follow the instructions in MsPacManControllerTuner.java and add to the build path of the project the guava library jar file. 
Be careful, training process may be very time demanding.
