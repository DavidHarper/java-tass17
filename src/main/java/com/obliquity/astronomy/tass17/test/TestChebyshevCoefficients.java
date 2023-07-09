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

import java.io.File;
import java.io.IOException;

import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.chebyshev.Chebyshev;
import com.obliquity.astronomy.tass17.TASSTheory;
import com.obliquity.astronomy.tass17.chebyshev.SatelliteOffset;

public class TestChebyshevCoefficients {
	public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("Arguments: satnum component ncoeffs jdstart jdend");
			System.exit(1);
		}
		
		int iSat = Integer.parseInt(args[0]);
		int iXYZ = Integer.parseInt(args[1]);
		int N = Integer.parseInt(args[2]);
		double jdstart = Double.parseDouble(args[3]);
		double jdend = Double.parseDouble(args[4]);
		
		String ephemerisHomeName = System.getProperty("testchebyshevcoefficients.ephemerishome");
		
		if (ephemerisHomeName == null) {
			System.err.println("Set property testchebyshevcoefficients.ephemerishome and re-run");
			System.exit(1);
		}
		
		File ephemerisHome = new File(ephemerisHomeName);
		
		if (!ephemerisHome.isDirectory()) {
			System.err.println("Not a directory: " + ephemerisHomeName);
			System.exit(2);
		}
		
		File de430 = new File(ephemerisHome, "de430/lnxp1550p2650.430");
		
		if (!de430.exists()) {
			System.err.println("File does not exist: " + de430.getAbsolutePath());
			System.exit(2);
		}
		
		try {
			JPLEphemeris ephemeris = new JPLEphemeris(de430);
			
			TASSTheory theory = new TASSTheory();			
			
			SatelliteOffset target = new SatelliteOffset(ephemeris, theory, iSat);
			
			target.setDateRange(jdstart, jdend);
			target.setMethod(SatelliteOffset.SIMPLIFIED);
			
			double[] coeffs = new double[N];
			
			target.setComponent(iXYZ);
			target.calculateChebyshevCoefficients(coeffs);
			
			for (int i = 0; i < N; i++)
				System.out.printf("%3d  %17.13f\n", i, coeffs[i]);

			double djd = (jdend - jdstart)/32.0;
			
			double[] T = new double[N];
			
			double jd = jdstart;
				
			for (int k = 0; k < 33; k++) {
				double x = -1.0 + 2.0 * (jd - jdstart)/(jdend - jdstart);
					
				double exact = target.evaluate(x);
					
				Chebyshev.calculateChebyshevPolynomials(x, T);
					
				double approx = T[0];
				
				for (int j = 1; j < N; j++)
					approx += coeffs[j] * T[j];
				
				System.out.printf("%13.5f %13.10f %13.4f %13.4f %13.4f\n", jd, x, exact, approx, exact-approx);
					
				jd += djd;
			}
		} catch (IOException | JPLEphemerisException e) {
			e.printStackTrace();
		}
	}
}
