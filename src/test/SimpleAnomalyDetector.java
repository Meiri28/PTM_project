package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	
	private List<CorrelatedFeatures> NormalModel;
	private float pearsonThreshold;
	private float addedPresentageThreshold;
	
	public SimpleAnomalyDetector() {
		NormalModel = new ArrayList<CorrelatedFeatures>();
		addedPresentageThreshold= 0.1f;
		pearsonThreshold = 0.9f;
	}

	private void addFeatursToModel(int feature1Index,int feature2Index, float[][] table, ArrayList<String> featusName)
	{
		String feature1 = featusName.get(feature1Index);
		String feature2 = featusName.get(feature2Index);;
		float corrlation = StatLib.pearson(table[feature1Index], table[feature2Index]);
		Point [] points = StatLib.makePoint(table[feature1Index], table[feature2Index]);
		Line lin_reg = StatLib.linear_reg(points);
		float threshold = 0;
		for (int i = 0; i < points.length; i++) {
			threshold = Math.max(threshold, (1 + addedPresentageThreshold) * StatLib.dev(points[i],lin_reg));
		}
		NormalModel.add(new CorrelatedFeatures(feature1,feature2,corrlation,lin_reg,threshold));
	}
	
	@Override
	public void learnNormal(TimeSeries ts) {
		float max,curr;
		int featureIndex = 0;
		float[][] values = TimeSeries.getArrayTs(ts);
		for (int i = 0; i < ts.getFeatureName().size() - 1; i++) {
			max = 0;
			for (int j = i+1; j < ts.getFeatureName().size(); j++) {
				curr = Math.abs(StatLib.pearson(values[i],values[j]));
				if(curr > max) {
					max = curr;
					featureIndex = j;
				}
			}
			if(max > pearsonThreshold)
				addFeatursToModel(i, featureIndex, values, ts.getFeatureName());
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		List<AnomalyReport> result = new ArrayList<AnomalyReport>();
		int feature1,feature2;
		float prediction,realety;
		for (int i = 0; i < NormalModel.size(); i++) {
			feature1 = ts.getFeatureName().indexOf(NormalModel.get(i).feature1);
			feature2 = ts.getFeatureName().indexOf(NormalModel.get(i).feature2);
			for (int j = 0; j < ts.getValuesTable().get(0).size(); j++) {
				prediction = NormalModel.get(i).lin_reg.f(ts.getValuesTable().get(feature1).get(j));
				realety = ts.getValuesTable().get(feature2).get(j);
				if(Math.abs(prediction - realety) > NormalModel.get(i).threshold)
				{
					result.add(new AnomalyReport(NormalModel.get(i).feature1 + "-" + NormalModel.get(i).feature2, j + 1));
				}
			}
		}
		return result;
	}
	
	public List<CorrelatedFeatures> getNormalModel(){
		return NormalModel;
	}
	
	public void setPearsonThreshold(float newVal) {
		pearsonThreshold = newVal;
	}
	
	public float getPearsonThreshold() {
		return pearsonThreshold;
	}
	
}