package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);
		
		// you may add default methods here
		//public void connect();
		//public void disconnect();
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	// the shared state of all commands
	private class SharedState{
		// implement here whatever you need
		// need to be new SimpleAnomalyDetector TimeSeriesAnomalyDetector
		// but because i can't change TimeSeriesAnomalyDetector to have threshold i did it that way
		public SimpleAnomalyDetector AnomalyDetector = new SimpleAnomalyDetector();
		
		//need to be static but this class is not static
		public String trainFileName = "anomalyTrain.csv";
		public String testFileName = "anomalyTest.csv";
		public String endSign = "done";
		
		List<AnomalyReport> anomalyDetected = null;
	}
	
	private SharedState sharedState=new SharedState();

	
	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}
	
	// need to be static but because dio isn't static so it is
	private String readUntilEndSign() {
		String result = "";
		String line = dio.readText();
		while(!sharedState.endSign.equals(line)) {
			result += line + System.lineSeparator();
			line = dio.readText();
		}
		return result;
	}
	
	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("upload anomalies and analyze results");
		}

		@Override
		public void execute() {
			dio.write(description);
		}		
	}
	
	// implement here all other commands
	
	// 1
	public class UploadTimeSeriesCsvCommand extends Command{

		public UploadTimeSeriesCsvCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			try {
				FileWriter clientTrainFile = new FileWriter(sharedState.trainFileName);
				dio.write("Please upload local train CSV file");
				clientTrainFile.write(readUntilEndSign());
				clientTrainFile.close();
				FileWriter clientTestFile = new FileWriter(sharedState.testFileName);
				dio.write("Please upload local test CSV file");
				clientTestFile.write(readUntilEndSign());
				clientTestFile.close();
			} catch (IOException e) {
				
				dio.write("Server error while executing upload csv file");
				//e.printStackTrace();
			}
		}
	}
	
	// 2
	public class AlgorithmSettingsCommand extends Command{

		public AlgorithmSettingsCommand() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
			dio.write("The current correlation threshold is " + sharedState.AnomalyDetector.getPearsonThreshold());
			dio.write("Type a new threshold");
			sharedState.AnomalyDetector.setPearsonThreshold(Float.parseFloat(dio.readText()));
		}
	}
	
	//3
	public class DetectAnomaliesCommand extends Command{

		public DetectAnomaliesCommand() {
			super("detect anomalies");
		}

		@Override
		public void execute() {
			TimeSeries trainTimeSeires = new TimeSeries(sharedState.trainFileName); 
			sharedState.AnomalyDetector.learnNormal(trainTimeSeires);
			TimeSeries testTimeSeires = new TimeSeries(sharedState.testFileName);
			sharedState.anomalyDetected = sharedState.AnomalyDetector.detect(testTimeSeires);
			dio.write("anomaly detection complete");
		}
	}
	
	//4
	public class DisplayResultsCommand extends Command{

		public DisplayResultsCommand() {
			super("display results");
		}
		
		@Override
		public void execute() {
			for(AnomalyReport report: sharedState.anomalyDetected) {
				dio.write(report.timeStep + "\t" + report.description);
			}
			dio.write("Done");
		}
	}
	
	//4
	public class ExitCommand extends Command{
		
		public ExitCommand() {
			super("exit");
		}
		
		@Override
		public void execute() {
			//dio.disconeect();
		}
	}
}