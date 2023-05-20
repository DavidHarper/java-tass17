package com.obliquity.astronomy.tass17;

import java.io.IOException;

public class TASSTheory {
	private static final double TWO_PI = 2.0 * Math.PI;

	private TASSElementSeries[][] elementSeries = new TASSElementSeries[8][4];

	public TASSTheory() throws IOException {
		loadElements();
	}
	
	private void loadElements() throws IOException {
    	TASSDataFileReader tdfr = new TASSDataFileReader();

    	for (int iSat = 0; iSat < 8; iSat++) {
    		for (int iElem = 0; iElem < 4; iElem++) {
    			String filename = String.format("/tass17/S%02d_%02d.dat", iSat+1, iElem+1);
    			    			
    			elementSeries[iSat][iElem] = tdfr.readTerms(filename);
    		}
    	}
	}
	
	public void calculateElements(double jd, TASSElements[] elements) {
		if (elements == null || elements.length < 8)
			throw new IllegalArgumentException("Elements array is null or too small");
		
    	double[] deltaLambda = new double[8];

		for (int iSat = 0; iSat < 8; iSat++) {
			double t = (jd - TASSConstants.EPOCH)/365.25;
			
			if (iSat == 6)
				deltaLambda[iSat] = 0.0;
			else
				deltaLambda[iSat] = elementSeries[iSat][1].calculateCriticalTermsInSine(t, null);			
		}

		for (int iSat = 0; iSat < 8; iSat++) {
			double t;
			
			if (iSat == 6) {
				t = jd - TASSConstants.EPOCH_HYPERION;
			} else {
				t = (jd - TASSConstants.EPOCH)/365.25;
			}
			
			elements[iSat].meanMotion = elementSeries[iSat][0].getConstantTerm() + elementSeries[iSat][0].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].lambda = (elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] +
					elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda)) % TWO_PI;
			
			elements[iSat].h = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].k = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
			
			elements[iSat].p = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].q = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);			
		}
	}
}
