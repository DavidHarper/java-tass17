package com.obliquity.astronomy.tass17.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.obliquity.astronomy.tass17.TASSConstants;
import com.obliquity.astronomy.tass17.TASSDataFileReader;
import com.obliquity.astronomy.tass17.TASSElementSeries;

public class TestReader {
	private static final double TWO_PI = 2.0 * Math.PI;
	
    public static void main(String[] args) {
    	TestReader reader = new TestReader();
    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void run() throws IOException {
    	TASSDataFileReader tdfr = new TASSDataFileReader();
    	
    	TASSElementSeries[][] elementSeries = new TASSElementSeries[8][4];
    	
    	for (int iSat = 0; iSat < 8; iSat++) {
    		for (int iElem = 0; iElem < 4; iElem++) {
    			String filename = String.format("/tass17/S%02d_%02d.dat", iSat+1, iElem+1);
    			
    			System.out.println("Reading resource " + filename);
    			
    			TASSElementSeries result = tdfr.readTerms(filename);
    			
    			System.out.println(result);
    			
    			elementSeries[iSat][iElem] = result;
    		}
    	}
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	double[] deltaLambda = new double[8];
    	double[] elements = new double[6];
    	    	
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null)
    			System.exit(0);
    		
    		double jd = Double.parseDouble(line);
    		
    		System.out.printf(" %13.5f  ", jd);
  		
    		for (int iSat = 0; iSat < 8; iSat++) {
    			double t = (jd - (iSat == 6 ? TASSConstants.EPOCH_HYPERION : TASSConstants.EPOCH))/365.25;
    			
    			deltaLambda[iSat] = elementSeries[iSat][1].calculateCriticalTermsInSine(t, null);
    			
    			System.out.printf(" %13.8f", deltaLambda[iSat]);
    		}
    		
    		System.out.println();
    		
    		System.out.printf(" %13.5f  ", jd);
    		
    		for (int iSat = 0; iSat < 0; iSat++) {
    			double t = (jd - (iSat == 6 ? TASSConstants.EPOCH_HYPERION : TASSConstants.EPOCH))/365.25;
    			
    			double np = elementSeries[iSat][0].getConstantTerm() + elementSeries[iSat][0].calculateAllTermsInSine(t, deltaLambda);
    			
    			double lambda = elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] + elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda);
    			
    			double h = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
    			
    			double k = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
    			
    			double p = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
    			
    			double q = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);
    			
    			System.out.printf(" %13.5f %d %13.8f %13.8f %13.8f %13.8f %13.8f %13.8f\n", jd, iSat+1, np, lambda, h, k, p, q);
    		}
    	}
    }
}
