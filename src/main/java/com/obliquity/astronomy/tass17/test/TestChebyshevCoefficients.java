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
		int iSat = -1;
		int iXYZ = -1;
		int nCoeffs = -1;
		int method = SatelliteOffset.RIGOROUS;
		int refsys = SatelliteOffset.J2000;
		double jdstart = 0.0;
		double jdend = 0.0;
		double range = 0.0;
		
		for (int i = 0; i < args.length; i++) {
			String keyword = args[i].toLowerCase();
			
			switch (keyword) {
			case "-name":
			case "-moon":
			case "-body":
				iSat = nameToID(args[++i]);
				break;
				
			case "-startdate":
			case "-start":
				jdstart = Double.parseDouble(args[++i]);
				break;
				
			case "-enddate":
			case "-end":
				jdend = Double.parseDouble(args[++i]);
				break;
				
			case "-range":
				range = Double.parseDouble(args[++i]);
				break;
				
			case "-ncoeffs":
			case "-order":
				nCoeffs = Integer.parseInt(args[++i]);
				break;
				
			case "-simplified":
				method = SatelliteOffset.SIMPLIFIED;
				break;
				
			case "-rigorous":
			case "-exact":
				method = SatelliteOffset.RIGOROUS;
				
			case "-x":
				iXYZ = SatelliteOffset.X_OFFSET;
				break;
				
			case "-y":
				iXYZ = SatelliteOffset.Y_OFFSET;
				break;
				
			case "-z":
				iXYZ = SatelliteOffset.Z_OFFSET;
				break;
				
			case "-j2000":
				refsys = SatelliteOffset.J2000;
				break;
				
			case "-mean":
				refsys = SatelliteOffset.MEAN;
				break;
				
			case "-of-date":
				refsys = SatelliteOffset.OF_DATE;
				break;
				
			default:
				System.err.println("Unknown keyword: " + keyword);
				System.exit(1);
			
			}
		}
		
		if (jdstart > 0.0 && range > 0.0)
			jdend = jdstart + range;
		
		if (iXYZ < 0 || iSat < 0 || nCoeffs < 0 || jdstart == 0.0 || jdend == 0.0) {
			System.err.println("One or more mandatory parameters were not defined");
			System.exit(1);
		}
		
		String ephemerisHomeName = System.getProperty("ephemeris.home");
		
		if (ephemerisHomeName == null) {
			System.err.println("Set property ephemeris.home and re-run");
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
			target.setMethod(method);
			target.setReferenceSystem(refsys);
			
			double[] coeffs = new double[nCoeffs];
			
			target.setComponent(iXYZ);
			target.calculateChebyshevCoefficients(coeffs);
			
			for (int i = 0; i < nCoeffs; i++)
				System.out.printf("%3d  %17.13f\n", i, coeffs[i]);

			double djd = (jdend - jdstart)/32.0;
			
			double[] T = new double[nCoeffs];
			
			double jd = jdstart;
				
			for (int k = 0; k < 33; k++) {
				double x = -1.0 + 2.0 * (jd - jdstart)/(jdend - jdstart);
					
				double exact = target.evaluate(x);
					
				Chebyshev.calculateChebyshevPolynomials(x, T);
					
				double approx = coeffs[0];
				
				for (int j = 1; j < nCoeffs; j++)
					approx += coeffs[j] * T[j];
				
				System.out.printf("%13.5f %13.10f %13.4f %13.4f %13.4f\n", jd, x, exact, approx, exact-approx);
					
				jd += djd;
			}
		} catch (IOException | JPLEphemerisException e) {
			e.printStackTrace();
		}
	}
	
	private static int nameToID(String name) {
		String namelc = name.toLowerCase();
		
		switch (namelc) {
			case "mimas": return 0;
			
			case "enceladus": return 1;
			
			case "tethys": return 2;
			
			case "dione": return 3;
			
			case "rhea": return 4;
			
			case "titan": return 5;
			
			case "hyperion": return 6;
			
			case "iapetus": return 7;
			
			default: return -1;
		}
	}

}
