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
import static java.lang.Math.*;

public class TASSTheory {
	private static final double TWO_PI = 2.0 * Math.PI;
	private static final double ONE_THIRD = 1.0/3.0;

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
	
	private void calculateCriticalTermsInLongitude(double jd, double[] deltaLambda) {
		if (deltaLambda == null || deltaLambda.length < 8)
			throw new IllegalArgumentException("deltaLambda array is null or too small");
		
		double t = (jd - TASSConstants.EPOCH)/365.25;
		
		for (int iSat = 0; iSat < 8; iSat++) {	
			if (iSat == 6)
				deltaLambda[iSat] = 0.0;
			else
				deltaLambda[iSat] = elementSeries[iSat][1].calculateCriticalTermsInSine(t, null);			
		}
	}
	
	public void calculateElements(double jd, int iSat, TASSElements elements) {
		double[] deltaLambda = new double[8];
		
		calculateCriticalTermsInLongitude(jd, deltaLambda);
		
		double t = (iSat == 6) ? jd - TASSConstants.EPOCH_HYPERION : (jd - TASSConstants.EPOCH)/365.25;
		
		elements.meanMotionAdjustment = elementSeries[iSat][0].getConstantTerm() + elementSeries[iSat][0].calculateAllTermsInCosine(t, deltaLambda);
		
		double lambda = (elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] +
				elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda)) % TWO_PI;
		
		if (lambda > Math.PI)
			lambda -= TWO_PI;
		
		if (lambda < -Math.PI)
			lambda += TWO_PI;

		elements.lambda = lambda;
		
		elements.k = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
		
		elements.h = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
		
		elements.q = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
		
		elements.p = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);			
	}
	
	public TASSElements calculateElements(double jd, int iSat) {
		TASSElements elements = new TASSElements();
		
		calculateElements(jd, iSat, elements);
		
		return elements;
	}
	
	public void calculateElementsForAllSatellites(double jd, TASSElements[] elements) {
		if (elements == null || elements.length < 8)
			throw new IllegalArgumentException("Elements array is null or too small");
		
		for (int iSat = 0; iSat < 8; iSat++) {
			if (elements[iSat] == null)
				elements[iSat] = new TASSElements();
		}
		
    	double[] deltaLambda = new double[8];
    	
    	calculateCriticalTermsInLongitude(jd, deltaLambda);

		for (int iSat = 0; iSat < 8; iSat++) {
			double t= (iSat == 6) ? jd - TASSConstants.EPOCH_HYPERION : (jd - TASSConstants.EPOCH)/365.25;
			
			elements[iSat].meanMotionAdjustment = elementSeries[iSat][0].getConstantTerm() + elementSeries[iSat][0].calculateAllTermsInCosine(t, deltaLambda);
			
			double lambda = (elementSeries[iSat][1].calculateLinearTerm(t) + deltaLambda[iSat] +
					elementSeries[iSat][1].calculateShortPeriodTermsInSine(t, deltaLambda)) % TWO_PI;
			
			if (lambda > Math.PI)
				lambda -= TWO_PI;
			
			if (lambda < -Math.PI)
				lambda += TWO_PI;

			elements[iSat].lambda = lambda;
			
			elements[iSat].k = elementSeries[iSat][2].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].h = elementSeries[iSat][2].calculateAllTermsInSine(t, deltaLambda);
			
			elements[iSat].q = elementSeries[iSat][3].calculateAllTermsInCosine(t, deltaLambda);
			
			elements[iSat].p = elementSeries[iSat][3].calculateAllTermsInSine(t, deltaLambda);			
		}
	}
	
	private static final double EPSILON = 1.0e-10;
	
	/*
	 * This method is a direct transcription of the FORTRAN subroutine EDERED. 
	 */

	public void calculatePositionAndVelocity(int iSat, TASSElements elements, double[] position, double[] velocity) {
		if (position == null || position.length != 3)
			position = new double[3];
		
		double am0 = 365.25 * TASSConstants.MEAN_MOTIONS[iSat] * (1.0 + elements.meanMotionAdjustment);
		double rmu = TASSConstants.GK1 * (1.0 + TASSConstants.MASSES[iSat]);
		double dga = pow(rmu/(am0 * am0), ONE_THIRD);
		
		double rl = elements.lambda;
		double rk = elements.k;
		double rh = elements.h;
		
		double fle = rl - rk * sin(rl) + rh * cos(rl);
		
		double corf = 1.0, cf, sf;
		
		do {
			cf = cos(fle);
			sf = sin(fle);
			corf = (rl - fle + rk * sf - rh * cf)/(1.0 - rk * cf - rh * sf);
			fle = fle + corf;
		} while (abs(corf) > EPSILON);
	
		cf = cos(fle);
		sf = sin(fle);
		
		double dlf = -rk * sf + rh * cf;
		double rsam1 = -rk * cf -rh * sf;
		double asr = 1.0/(1.0 + rsam1);
		double phi = sqrt(1.0 - rk * rk - rh * rh);
		double psi = 1.0/(1.0 + phi);
		
		double x1 = cf - rk - psi * rh * dlf;
		double y1 = sf - rh + psi * rk * dlf;
		
		double p = elements.p;
		double q = elements.q;
		
		double dwho = 2.0 * sqrt(1.0 - p * p - q * q);
		double rtp = 1.0 - 2.0 * p * p;
		double rtq = 1.0 - 2.0 * q * q;
		double rdg = 2.0 * p * q;
		
		double x2 = x1 * rtp + y1 * rdg;
		double y2 = x1 * rdg + y1 * rtq;
		double z2 = (-x1 * p + y1 * q) * dwho;

		double CO = TASSConstants.CO;
		double SO = TASSConstants.SO;
		double CI = TASSConstants.CI;
		double SI = TASSConstants.SI;
		
		double x3 = CO * x2 - SO * CI * y2 + SO * SI * z2;
		double y3 = SO * x2 + CO * CI * y2 - CO * SI * z2;
		double z3 =                SI * y2 + CI * z2;
	
		position[0] = x3 * dga;
		position[1] = y3 * dga;
		position[2] = z3 * dga;
		
		if (velocity != null && velocity.length >= 3) {		
			double vx1 = am0 * asr * dga * (-sf - psi * rh * rsam1);
			double vy1 = am0 * asr * dga * (cf + psi * rh * rsam1);

			double vx2 = vx1 * rtp + vy1 * rdg;
			double vy2 = vx1 * rdg + vy1 * rtq;
			double vz2 = (-vx1 * p + vy1 * q) * dwho;
			
			double vx3 = CO * vx2 - SO * CI * vy2 + SO * SI * vz2;
			double vy3 = SO * vx2 + CO * CI * vy2 - CO * SI * vz2;
			double vz3 =                 SI * vy2 + CI * vz2;
			
			velocity[0] = vx3 * dga;
			velocity[1] = vy3 * dga;
			velocity[2] = vz3 * dga;
		}
	}
	
	public void calculatePosition(int iSat, TASSElements elements, double[] position) {
		calculatePositionAndVelocity(iSat, elements, position, null);
	}
}
