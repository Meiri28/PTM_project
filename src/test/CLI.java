package test;

import java.util.ArrayList;
import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<Command>();
		commands.add(c.new UploadTimeSeriesCsvCommand());
		commands.add(c.new AlgorithmSettingsCommand());
		commands.add(c.new DetectAnomaliesCommand());
		commands.add(c.new DisplayResultsCommand());
		commands.add(c.new UploadAnomaliesAndAnalyzeResultCommand());
		commands.add(c.new ExitCommand());
	}
	
	public void start() {
		int currentCommand =  0;
		String s;
		while(commands.get(currentCommand).getClass() != Commands.ExitCommand.class) {
			dio.write("Welcome to the Anomaly Detection Server." +  System.lineSeparator()); 
			dio.write("Please choose an option:" + System.lineSeparator());
			for (int i = 0; i < commands.size(); i++) {
				dio.write( (i + 1) + ". " + commands.get(i).description + System.lineSeparator());
			}
			s = dio.readText();
			currentCommand = Integer.parseInt(s) - 1;
			commands.get(currentCommand).execute();
		}
	}
}
