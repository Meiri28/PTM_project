package test;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	// the shared state of all commands
	private class SharedState{
		// need to be new SimpleAnomalyDetector TimeSeriesAnomalyDetector
		// but because i can't change TimeSeriesAnomalyDetector to have threshold i did it that way
		public SimpleAnomalyDetector AnomalyDetector = new SimpleAnomalyDetector();
		
		//need to be static but this class is not static
		public String trainFileName = "anomalyTrain.csv";
		public String testFileName = "anomalyTest.csv";
		public String endSign = "done";
		
		List<AnomalyReport> anomalyDetected = null;
		
		public int numberofline = 0;
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
	
	// 1
	public class UploadTimeSeriesCsvCommand extends Command{

		public UploadTimeSeriesCsvCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			try {
				FileWriter clientTrainFile = new FileWriter(sharedState.trainFileName);
				dio.write("Please upload your local train CSV file." + System.lineSeparator());
				clientTrainFile.write(readUntilEndSign());
				clientTrainFile.close();
				dio.write("Upload complete." + System.lineSeparator());
				FileWriter clientTestFile = new FileWriter(sharedState.testFileName);
				dio.write("Please upload your local test CSV file." + System.lineSeparator());
				clientTestFile.write(readUntilEndSign());
				clientTestFile.close();
				dio.write("Upload complete." + System.lineSeparator());
			} catch (IOException e) {
				dio.write("Server error while executing upload csv file" + System.lineSeparator());
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
			dio.write("The current correlation threshold is " + sharedState.AnomalyDetector.getPearsonThreshold() + System.lineSeparator());
			dio.write("Type a new threshold" + System.lineSeparator());
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
			sharedState.numberofline = testTimeSeires.getValuesTable().get(0).size();
			sharedState.anomalyDetected = sharedState.AnomalyDetector.detect(testTimeSeires);
			dio.write("anomaly detection complete." + System.lineSeparator());
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
				dio.write(report.timeStep + "\t" + report.description + System.lineSeparator());
			}
			dio.write("Done" + System.lineSeparator());
		}
	}
	
	// 5
	public class UploadAnomaliesAndAnalyzeResultCommand extends Command{
		
		public UploadAnomaliesAndAnalyzeResultCommand() {
			super("upload anomalies and analyze results");
		}
		
		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file." + System.lineSeparator());
			ArrayList<String> calculatedTimeLineInput = calcanomalytimeline(sharedState.anomalyDetected);
			ArrayList<String> userTimeLineInput = new ArrayList<String>(Arrays.asList(readUntilEndSign().split(System.lineSeparator())));
			dio.write("Upload complete" + System.lineSeparator());
			sortTimeLine(calculatedTimeLineInput);
			sortTimeLine(userTimeLineInput);
			int calcIndex = 0 , UserIndex = 0;
			int tp=0, fp=0;
			while(calcIndex < calculatedTimeLineInput.size() && UserIndex < userTimeLineInput.size()) {
				switch(compereTimeLine(calculatedTimeLineInput.get(calcIndex),userTimeLineInput.get(UserIndex))) {
					case 0:
						calcIndex++;
						UserIndex++;
						tp++;
						break;
					case -1:
						calcIndex++;
						fp++;
						break;
					case 1:
						UserIndex++;
						break;
				}
			}
			fp += calculatedTimeLineInput.size() - calcIndex;
			int p = userTimeLineInput.size();
			int n = calcN(userTimeLineInput,sharedState.numberofline);
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(1);
			nf.setRoundingMode(RoundingMode.DOWN);
			String truePositiveRate = nf.format((float)tp/(float)p);
			String falsePositiveRate  = nf.format((float)fp/(float)n);
			dio.write("True Positive Rate: " + truePositiveRate + System.lineSeparator());
			dio.write("False Positive Rate: " + falsePositiveRate + System.lineSeparator());
		}
		
		private int calcN(ArrayList<String> userTimeLineInput,int numberofline) {
			int result = 0;
			for (String timeline: userTimeLineInput) {
				result += Integer.parseInt(timeline.split(",")[1])-Integer.parseInt(timeline.split(",")[0]) + 1;
			}
			return numberofline - result;
		}
		
		private void sortTimeLine(ArrayList<String> timeline) {
			timeline.sort((s1,s2)->Integer.parseInt(s1.split(",")[0])-Integer.parseInt(s2.split(",")[0]));
		}
		
		private ArrayList<String> calcanomalytimeline(List<AnomalyReport> anomalyDetected) {
			ArrayList<String> result = new ArrayList<String>();
			long start, end;
			for (int i = 0; i < anomalyDetected.size(); i++) {
				start = anomalyDetected.get(i).timeStep;
				while(i + 1 < anomalyDetected.size() &&
						anomalyDetected.get(i).description.equals(anomalyDetected.get(i+1).description) &&
						anomalyDetected.get(i).timeStep + 1 == anomalyDetected.get(i+1).timeStep)
					i++;
				end = anomalyDetected.get(i).timeStep;
				result.add(start+","+end);
			}
			return result;
		}
		
		private int compereTimeLine(String firstTimeline,String SeccondTimeline) {
			long firstStart = Long.parseLong(firstTimeline.split(",")[0]);
			long firstend = Long.parseLong(firstTimeline.split(",")[1]);
			long seccondStart = Long.parseLong(SeccondTimeline.split(",")[0]);
			long seccondend = Long.parseLong(SeccondTimeline.split(",")[1]);
			if(firstend  < seccondStart )
				return -1;
			if(firstStart > seccondend)
				return 1;
			return 0;
		}
	}
	
	//6
	public class ExitCommand extends Command{
		
		public ExitCommand() {
			super("exit");
		}
		
		@Override
		public void execute() {
			
		}
	}
}
