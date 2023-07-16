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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.obliquity.astronomy.almanac.AstronomicalDate;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.tass17.TASSTheory;

public class GenerateJSONData {
	public static void main(String[] args) {
		int satID = -1;
		double jdStart = 0.0, jdFinish = 0.0, stepSize = 0.0;
		int nCoeffs = -1;
		boolean rigorous = true;
		int refsys = SatelliteOffset.J2000;
		
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
				
			case "-simplified":
				rigorous = false;
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
		
		if (satID < 0 || jdStart == 0.0 || jdFinish == 0.0 || stepSize <= 0.0 || nCoeffs < 1) {
			System.err.println("Invalid or missing options.");
			System.exit(2);
		}
		
		try {
			generateJSONData(satID, jdStart, jdFinish, stepSize, nCoeffs, rigorous, refsys);
		} catch (IOException | JPLEphemerisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final Pattern jdPattern = Pattern.compile("(\\d+(\\.)?(\\d+)?)");
	private static final Pattern dateTimePattern = Pattern.compile("(\\d{4})-(\\d{2})\\-(\\d{2})(\\s+(\\d{2}):(\\d{2}))?");

	private static double parseDate(String datestr) {
		Matcher matcher = jdPattern.matcher(datestr);

		if (matcher.matches())
			return Double.parseDouble(datestr);

		matcher = dateTimePattern.matcher(datestr);

		if (!matcher.matches())
			throw new IllegalArgumentException("String \"" + datestr + "\" cannot be parsed as a date/time or a Julian Day Number");

		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));

		String strHour = matcher.group(5);
		String strMinute = matcher.group(6);

		int hour = strHour != null ? Integer.parseInt(strHour) : 0;
		int minute = strMinute != null ? Integer.parseInt(strMinute) : 0;
		double seconds = 0.0;

		AstronomicalDate ad = new AstronomicalDate(year, month, day, hour, minute, seconds);

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
	
	private static void generateJSONData(int satID, double jdStart, double jdFinish, double stepSize, int nCoeffs, boolean rigorous, int refsys) throws IOException, JPLEphemerisException {
		JPLEphemeris ephemeris = getEphemeris();
		
		TASSTheory theory = new TASSTheory();			
		
		SatelliteOffset target = new SatelliteOffset(ephemeris, theory, satID);
		
		target.setMethod(rigorous ? SatelliteOffset.RIGOROUS : SatelliteOffset.SIMPLIFIED);
		target.setReferenceSystem(refsys);
		
		double[] coeffs = new double[nCoeffs];
		
		System.out.printf("{\n  \"name\":\"%s\",\n  \"jdstart\":\"%13.5f\",\n  \"stepsize\":\"%.5f\",\n",
				idToName[satID], jdStart, stepSize);
		
		String strRefsys = null;

		switch (refsys) {
		case SatelliteOffset.J2000:
			strRefsys = "J2000";
			break;

		case SatelliteOffset.MEAN:
			strRefsys = "mean";
			break;

		case SatelliteOffset.OF_DATE:
			strRefsys = "of-date";
			break;
		}

		System.out.printf("  \"refsys\":\"%s\",\n  \"data\": [", strRefsys);

		boolean first = true;
		double jdMaximum = 0.0;
			
		for (double jd0 = jdStart; jd0 < jdFinish; jd0 += stepSize) {
			double jd1 = jd0 + stepSize;
			
			target.setDateRange(jd0, jd1);
			
			if (!first)
				System.out.println(",");
			
			first = false;
			
			System.out.printf("    [ \"%13.5f\", \"%13.5f\",\n", jd0, jd1);
			
			for (int iXYZ = 0; iXYZ < 3; iXYZ++) {
				target.setComponent(iXYZ);
				target.calculateChebyshevCoefficients(coeffs);	
				
				System.out.print("      [");
				
				for (int i = 0; i < nCoeffs; i++) {
					if (i > 0)
						System.out.print(", ");
					
					System.out.printf("\"%.3f\"", coeffs[i]);
				}
				
				System.out.println(iXYZ < 2 ? " ]," : " ]");
			}
			
			System.out.print("    ]");

			jdMaximum = jd1;
		}

		System.out.println("\n  ],");

		System.out.printf("  \"jdfinish\":\"%13.5f\"\n}\n", jdMaximum);
	}
}
