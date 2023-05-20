package com.obliquity.astronomy.tass17.test;

/*
 *  java-tass17: a Java implementation of the TASS 1.7 model of the orbits of the major satellites of Saturn
 *  Copyright (C) 2023 David Harper at obliquity.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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
    	boolean verbose = Boolean.getBoolean("verbose");
    	
    	TASSDataFileReader tdfr = new TASSDataFileReader();
    	
    	TASSElementSeries[][] elementSeries = new TASSElementSeries[8][4];
    	
    	for (int iSat = 0; iSat < 8; iSat++) {
    		for (int iElem = 0; iElem < 4; iElem++) {
    			String filename = String.format("/tass17/S%02d_%02d.dat", iSat+1, iElem+1);
    			
    			if (verbose)
    				System.out.println("Reading resource " + filename);
    			
    			TASSElementSeries result = tdfr.readTerms(filename);
    			
    			if (verbose)
    				System.out.println(result);
    			
    			elementSeries[iSat][iElem] = result;
    		}
    	}
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	double[] deltaLambda = new double[8];
    	    	
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null)
    			System.exit(0);
    		
    		double jd = Double.parseDouble(line);
    		
    		System.out.printf(" %13.5f  ", jd);
  		
    		for (int iSat = 0; iSat < 8; iSat++) {
    			double t = (jd - TASSConstants.EPOCH)/365.25;
    			
    			if (iSat == 6)
    				deltaLambda[iSat] = 0.0;
    			else
    				deltaLambda[iSat] = elementSeries[iSat][1].calculateCriticalTermsInSine(t, null);
    			
    			System.out.printf(" %13.8f", deltaLambda[iSat]);
    		}
    		
    		System.out.println();
    		
    		for (int iSat = 0; iSat < 8; iSat++) {
    			double t;
    			
    			if (iSat == 6) {
    				t = jd - TASSConstants.EPOCH_HYPERION;
    			} else {
    				t = (jd - TASSConstants.EPOCH)/365.25;
    			}
    			
    			double np = elementSeries[iSat][0].getConstantTerm() + elementSeries[iSat][0].calculateAllTermsInCosine(t, deltaLambda);
    			
    			double lambda = (elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] +
    					elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda)) % TWO_PI;
    			
    			if (lambda > Math.PI)
    				lambda -= TWO_PI;
    			
    			if (lambda < -Math.PI)
    				lambda += TWO_PI;
    			
    			double h = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
    			
    			double k = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
    			
    			double p = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
    			
    			double q = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);
    			
    			System.out.printf(" %13.5f %d %13.8f %13.8f %13.8f %13.8f %13.8f %13.8f\n", jd, iSat+1, np, lambda, h, k, p, q);
    		}
    	}
    }
}
