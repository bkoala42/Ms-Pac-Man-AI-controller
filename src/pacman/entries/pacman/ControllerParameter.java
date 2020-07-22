package pacman.entries.pacman;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ControllerParameter {
	private int maxValue;
	private int minValue;
	private String name;
	private List<Integer> values;
	
	public ControllerParameter(String name, List<Integer> values) {
		this.values = values;
		this.name = name;
		this.maxValue = Collections.max(values);
		this.minValue = Collections.min(values);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public String getName() {
		return name;
	}

	public List<Integer> getValues() {
		return values;
	}
	
	public Integer getNextValue(Integer state) {
		int nextState = -1;
		int stateIndex = values.indexOf(state);
		if(stateIndex < values.size()-1) {
			nextState = values.get(stateIndex + 1);
		}
		else {
			nextState = maxValue;
		}
		return nextState;
	}
	
	public Integer getPreviousValue(Integer state) {
		int previousState = -1;
		int stateIndex = values.indexOf(state);
		if(stateIndex > 0) {
			previousState = values.get(stateIndex - 1);
		}
		else {
			previousState = minValue;
		}
		return previousState;
	}	
	
	public String toString() {
		return name+" values: "+values.toString();
	}
	
//	public static void main(String [] args) {
//		Integer[] par = new Integer[4];
//		par[0] = 1;
//		par[1] = 2;
//		par[2] = 3;
//		par[3] = 4;
//		ControllerParameter cPar = new ControllerParameter("Prova", Arrays.asList(par));
//		
//		System.out.println(cPar.getMaxValue());
//		System.out.println(cPar.getMinValue());
//		System.out.println(cPar.getName());
//		System.out.println(cPar.getValues());
//		System.out.println(cPar);
//		System.out.println(cPar.getNextValue(2));
//		System.out.println(cPar.getPreviousValue(2));
//		System.out.println(cPar.getNextValue(4));
//		System.out.println(cPar.getPreviousValue(1));
//	}
}
