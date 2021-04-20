package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class TimeSeries {
	
	private ArrayList<String> featureName;
	private ArrayList<ArrayList<Float>> valuesTable;
	
	public TimeSeries(String csvFileName) {
		BufferedReader reader = null;
		featureName = new ArrayList<String>();
		valuesTable = new ArrayList<ArrayList<Float>>();
		try {
			reader = new BufferedReader(new FileReader(csvFileName));
			String [] values = reader.readLine().split(",");
			featureName.addAll(Arrays.asList(values));
			for (int i = 0; i < values.length; i++) {
				valuesTable.add(new ArrayList<Float>());
			}
			String line = reader.readLine();
			while(line != null) {
				values = line.split(",");
				for (int i = 0; i < values.length; i++) {
					valuesTable.get(i).add(Float.parseFloat(values[i]));;
				}
				line = reader.readLine();
			}		
		}
		catch(FileNotFoundException e) {
			return;
		}
		catch(IOException e) {
			return;
		}
	}

	public ArrayList<String> getFeatureName() {
		return featureName;
	}

	public ArrayList<ArrayList<Float>> getValuesTable() {
		return valuesTable;
	}
	
	public static float[][] getArrayTs(TimeSeries ts){
		float[][] result = new float[ts.valuesTable.size()][ts.valuesTable.get(0).size()];
		for (int i = 0; i < ts.valuesTable.size(); i++) {
			for (int j = 0; j < ts.valuesTable.get(i).size(); j++) {
				result[i][j] = ts.valuesTable.get(i).get(j);
			}
		}
		return result;
	}
}
