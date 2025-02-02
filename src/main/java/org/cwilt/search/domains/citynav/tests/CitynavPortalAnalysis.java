package org.cwilt.search.domains.citynav.tests;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CitynavPortalAnalysis {
	public static void main(String[] args) {
		ArrayList<Double> initialTaus = new ArrayList<Double>();
		ArrayList<Double> initialExps = new ArrayList<Double>();
		ArrayList<Double> hcTaus = new ArrayList<Double>();
		ArrayList<Double> hcExps = new ArrayList<Double>();
		ArrayList<Double> hubTaus = new ArrayList<Double>();
		ArrayList<Double> hubExps = new ArrayList<Double>();
		
		for (int i = 1; i < 16; i++) {
			String fileName = "citynav_results/ai" + i + "_output";

			try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

				String nextLine = br.readLine();
				while(nextLine != null){
					if(nextLine.isEmpty()){
						continue;
					}
					String[] tokens = nextLine.split("\t");
					initialTaus.add(Double.parseDouble(tokens[0]));
					initialExps.add(Double.parseDouble(tokens[1]));
					hcTaus.add(Double.parseDouble(tokens[2]));
					hcExps.add(Double.parseDouble(tokens[3]));
					hubTaus.add(Double.parseDouble(tokens[4]));
					hubExps.add(Double.parseDouble(tokens[5]));
					nextLine = br.readLine();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.err.println(initialTaus.size());
		System.err.println(meanArray(initialTaus));
		System.err.println(meanArray(initialExps));
		System.err.println(meanArray(hcTaus));
		System.err.println(meanArray(hcExps));
		System.err.println(meanArray(hubTaus));
		System.err.println(meanArray(hubExps));
	}
	
	private static double meanArray(ArrayList<Double> v){
		double sum = 0;
		for(Double d : v){
			sum += d;
		}
		return sum / v.size();
	}
}
