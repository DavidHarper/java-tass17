package com.obliquity.astronomy.tass17;

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
		
		for (int iSat = 0; iSat < 8; iSat++) {
			if (elements[iSat] == null)
				elements[iSat] = new TASSElements();
		}
		
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
			
			double lambda = (elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] +
					elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda)) % TWO_PI;
			
			if (lambda > Math.PI)
				lambda -= TWO_PI;
			
			if (lambda < -Math.PI)
				lambda += TWO_PI;

			elements[iSat].lambda = lambda;
			
			elements[iSat].h = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].k = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
			
			elements[iSat].p = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].q = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);			
		}
	}
}
