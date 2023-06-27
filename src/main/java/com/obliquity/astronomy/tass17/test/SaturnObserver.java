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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.obliquity.astronomy.almanac.ApparentPlace;
import com.obliquity.astronomy.almanac.AstronomicalDate;
import com.obliquity.astronomy.almanac.EarthCentre;
import com.obliquity.astronomy.almanac.IAUEarthRotationModel;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.MovingPoint;
import com.obliquity.astronomy.almanac.PlanetCentre;
import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSMovingPoint;
import com.obliquity.astronomy.tass17.TASSTheory;

public class SaturnObserver {
	private ApparentPlace apSaturn;
	private ApparentPlace[] apSatellites;
	private IAUEarthRotationModel erm = null;
	private TASSTheory theory;
	private final double cosObliquity, sinObliquity;
	
	private final String[] names = { "Mim", "Enc", "Tet", "Dio", "Rhe", "Ttn", "Hyp", "Iap" };
	
	private boolean timeIsUT = Boolean.getBoolean("saturnobserver.timeisut");
	private boolean usePositionOfDate = Boolean.getBoolean("saturnobserver.usepositionofdate");

	public static void main(String[] args) {
		String ephemerisHomeName = System.getProperty("saturnobserver.ephemerishome");
		
		if (ephemerisHomeName == null) {
			System.err.println("Set property saturnobserver.ephemerishome and re-run");
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
			
			SaturnObserver observer = new SaturnObserver(ephemeris, theory);
			
			observer.run();
		} catch (IOException | JPLEphemerisException e) {
			e.printStackTrace();
		}
	}
	
	public SaturnObserver(JPLEphemeris ephemeris, TASSTheory theory) throws IOException {
		this.theory = theory;
		
		this.erm = new IAUEarthRotationModel();
		
		double obliquity = erm.meanObliquity(2451545.0);
		
		sinObliquity = Math.sin(obliquity);
		cosObliquity = Math.cos(obliquity);
		
		EarthCentre earth = new EarthCentre(ephemeris);
		
		MovingPoint sun = new PlanetCentre(ephemeris, JPLEphemeris.SUN);
		
		MovingPoint saturn = new PlanetCentre(ephemeris, JPLEphemeris.SATURN);
		
		this.apSaturn = new ApparentPlace(earth, saturn, sun, erm);
		
		apSatellites = new ApparentPlace[8];
		
		for (int iSat = 0; iSat < 8; iSat++) {
			MovingPoint satellite = new TASSMovingPoint(ephemeris, iSat);
			apSatellites[iSat] = new ApparentPlace(earth, satellite, sun, erm);
		}
	}
	
	public void run() throws IOException, JPLEphemerisException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null)
    			System.exit(0);
    		
    		String[] words = line.trim().split("\s+");
    		
    		double jd;
    		int year = 0, month = 0, day = 0, hour = 0, minute = 0;
    		
    		switch (words.length){
    		case 1:
    			jd = Double.parseDouble(line);
    			break;
    			
    		case 5:
    			hour = Integer.parseInt(words[3]);
    			minute = Integer.parseInt(words[4]);
    			// Fall through ...
    			
    		case 3:
    			year = Integer.parseInt(words[0]);
    			month = Integer.parseInt(words[1]);
    			day = Integer.parseInt(words[2]);
    			
    			AstronomicalDate ad = new AstronomicalDate(year, month, day, hour, minute, 0.0);
    			
    			jd = ad.getJulianDate();
    			break;
    			
    		default:
    			System.err.println("Invalid input");
    			jd = Double.NaN;
    		}
    		
    		if (!Double.isNaN(jd))
    			calculateSatelliteOffsets(jd);
    	}
	}
	
	private void calculateSatelliteOffsets(double jd) throws JPLEphemerisException {
		if (timeIsUT)
			jd += erm.deltaT(jd);
		
		apSaturn.calculateApparentPlace(jd);
		
		double raSaturn = usePositionOfDate ? apSaturn.getRightAscensionOfDate() : apSaturn.getRightAscensionJ2000();
		double ca = Math.cos(raSaturn);
		double sa = Math.sin(raSaturn);
		
		double decSaturn = usePositionOfDate ? apSaturn.getDeclinationOfDate() : apSaturn.getDeclinationJ2000();
		double cd = Math.cos(decSaturn);
		double sd = Math.sin(decSaturn);
		
    	TASSElements[] elements = new TASSElements[8];
    	
    	double[] position = new double[3];
    	
    	double jdSatellites = jd - apSaturn.getLightTime();

    	theory.calculateElementsForAllSatellites(jdSatellites, elements);

   		printPosition(System.out, "SAT", jd, raSaturn, decSaturn);
   		
   		double gdSaturn = apSaturn.getGeometricDistance();

   		for (int iSat = 0; iSat < 8; iSat++) {
   			theory.calculatePosition(iSat, elements[iSat], position);
   			
   			double xe = position[0], ye = position[1], ze = position[2];
   			
   			double xa = xe;
   			double ya = ye * cosObliquity - ze * sinObliquity;
   			double za = ye * sinObliquity + ze * cosObliquity;
   			
   			double q = (3600.0 * 180.0 / Math.PI)/apSaturn.getGeometricDistance();
   			
   			double ux = ca * cd, uy = sa * cd, uz = sd;
   			double vx = -sa, vy = ca, vz = 0.0;
   			double wx = -ca * sd, wy = -sa * sd, wz = cd;
   			
   			double dx = (vx * xa + vy * ya + vz * za) * q;
   			double dy = (wx * xa + wy * ya + wz * za) * q;
   			double dz = (ux * xa + uy * ya + uz * za) * q;
   			
   			apSatellites[iSat].calculateApparentPlace(jd);
   			double raSatellite = usePositionOfDate ? apSatellites[iSat].getRightAscensionOfDate() : apSatellites[iSat].getRightAscensionJ2000();
   			double decSatellite = usePositionOfDate ? apSatellites[iSat].getDeclinationOfDate() : apSatellites[iSat].getDeclinationJ2000();
   			double dx2 = (raSatellite - raSaturn) * Math.cos(decSaturn) * 3600.0 * 180.0/Math.PI;
   			double dy2 = (decSatellite - decSaturn) * 3600.0 * 180.0/Math.PI;
   			
   			double gdSatellite = apSatellites[iSat].getGeometricDistance();
   			
   			System.out.println();
   			printPosition(System.out, names[iSat], jd, raSatellite, decSatellite);
   			System.out.printf(" %13.5f REL %3s  %8.3f  %8.3f %9.6f\n", jd, names[iSat], dx2, dy2, gdSatellite - gdSaturn);  			
   			System.out.printf(" %13.5f DXY %3s  %8.3f  %8.3f  %8.3f\n", jd, names[iSat], dx, dy, dz);
   			System.out.printf(" %13.5f DIF %3s  %8.3f  %8.3f\n", jd, names[iSat], dx2-dx, dy2-dy);
   		}
	}
	
	private void printPosition(PrintStream ps, String name, double jd, double ra, double dec) {
		ra *= 12.0/Math.PI;
		
		if (ra < 0.0)
			ra += 24.0;
		
		int rah = (int)ra;
		
		ra = 60.0 * (ra - rah);
		
		int ram = (int)ra;
		
		ra = 60.0 * (ra - ram);
		
		String decSign = dec < 0.0 ? "-" : "+";
		
		if (dec < 0.0)
			dec = -dec;
		
		dec *= 180.0/Math.PI;
		
		int decd = (int)dec;
		
		dec = 60.0 * (dec - decd);
		
		int decm = (int)dec;
		
		dec = 60.0 * (dec - decm);
		
		ps.printf(" %13.5f ABS %3s %2d %02d %07.4f   %1s %2d %02d %7.3f\n", jd, name, rah, ram, ra, decSign, decd, decm, dec);
	}
}
