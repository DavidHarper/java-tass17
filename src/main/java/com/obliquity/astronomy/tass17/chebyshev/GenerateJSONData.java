package com.obliquity.astronomy.tass17.chebyshev;

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

import com.obliquity.astronomy.almanac.AstronomicalDate;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.tass17.TASSTheory;

public class GenerateJSONData {
	public static void main(String[] args) {
		int satID = -1;
		double jdStart = 0.0, jdFinish = 0.0, stepSize = 0.0;
		int nCoeffs = -1;
		
		for (int i = 0; i < args.length; i++) {
			String keyword = args[i].toLowerCase();
			
			switch (keyword) {
			case "-name":
			case "-moon":
			case "-body":
				satID = nameToID(args[++i]);
				break;
				
			case "-startdate":
			case "-start":
				jdStart = parseDate(args[++i]);
				break;
				
			case "-enddate":
			case "-end":
				jdFinish = parseDate(args[++i]);
				break;
				
			case "-step":
			case "-stepsize":
				stepSize = Double.parseDouble(args[++i]);
				break;
				
			case "-ncoeffs":
			case "-order":
				nCoeffs = Integer.parseInt(args[++i]);
				break;
				
			default:
				System.err.println("Unknown keyword: " + keyword);
				System.exit(1);
			}
		}
		
		if (satID < 0 || jdStart == 0.0 || jdFinish == 0.0 || stepSize <= 0.0 || nCoeffs < 1) {
			System.err.println("Invalid or missing options.");
			System.exit(2);
		}
		
		try {
			generateJSONData(satID, jdStart, jdFinish, stepSize, nCoeffs);
		} catch (IOException | JPLEphemerisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static double parseDate(String datestr) {
		String[] words = datestr.split("-");
		
		if (words.length < 3)
			return -1.0;
		
		int year = Integer.parseInt(words[0]);
		int month = Integer.parseInt(words[1]);
		int day = Integer.parseInt(words[2]);
		
		AstronomicalDate ad = new AstronomicalDate(year, month, day);
		
		return ad.getJulianDate();
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
	
	private static JPLEphemeris getEphemeris() throws IOException, JPLEphemerisException {
		String ephemerisHomeName = System.getProperty("ephemeris.home");
		
		if (ephemerisHomeName == null)
			throw new IllegalStateException("Set property ephemeris.home and re-run");
		
		File ephemerisHome = new File(ephemerisHomeName);
		
		if (!ephemerisHome.isDirectory())
			throw new IllegalStateException("Not a directory: " + ephemerisHomeName);

		
		File de430 = new File(ephemerisHome, "de430/lnxp1550p2650.430");
		
		if (!de430.exists()) 
			throw new IllegalStateException("File does not exist: " + de430.getAbsolutePath());

		return new JPLEphemeris(de430);
	}
	
	static final String[] idToName = {
			"mimas", "enceladus", "tethys", "dione", "rhea", "titan", "hyperion", "iapetus"
	};
	
	private static void generateJSONData(int satID, double jdStart, double jdFinish, double stepSize, int nCoeffs) throws IOException, JPLEphemerisException {
		JPLEphemeris ephemeris = getEphemeris();
		
		TASSTheory theory = new TASSTheory();			
		
		SatelliteOffset target = new SatelliteOffset(ephemeris, theory, satID);
		
		target.setMethod(SatelliteOffset.RIGOROUS);
		
		double[] coeffs = new double[nCoeffs];
		
		System.out.printf("{\n  \"name\":\"%s\",\n  \"jdstart\":%13.5f,\n  \"jdfinish\":%13.5f,\n  \"stepsize\":%13.5f,\n",
				idToName[satID], jdStart, jdFinish, stepSize);
		
		System.out.println("  \"data\": [");
			
		for (double jd0 = jdStart; jd0 < jdFinish; jd0 += stepSize) {
			double jd1 = jd0 + stepSize;
			
			target.setDateRange(jd0, jd1);
			
			System.out.printf("    [ \"jdstart\":%13.5f, \"jdfinish\":%13.5f,\n", jd0, jd1);
			
			for (int iXYZ = 0; iXYZ < 3; iXYZ++) {
				target.setComponent(iXYZ);
				target.calculateChebyshevCoefficients(coeffs);	
				
				System.out.print("      [");
				
				for (int i = 0; i < nCoeffs; i++) {
					if (i > 0)
						System.out.print(", ");
					
					System.out.printf("%8.3f", coeffs[i]);
				}
				
				System.out.println(" ],");
			}
			
			System.out.println("    ],");
		}
		
		System.out.println("  ]\n};");
	}

}
