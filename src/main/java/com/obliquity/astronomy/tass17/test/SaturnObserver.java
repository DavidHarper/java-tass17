package com.obliquity.astronomy.tass17.test;

import java.io.BufferedReader;

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
import java.io.InputStreamReader;

import com.obliquity.astronomy.almanac.ApparentPlace;
import com.obliquity.astronomy.almanac.EarthCentre;
import com.obliquity.astronomy.almanac.IAUEarthRotationModel;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.MovingPoint;
import com.obliquity.astronomy.almanac.PlanetCentre;
import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSTheory;

public class SaturnObserver {
	private ApparentPlace apSaturn;
	private IAUEarthRotationModel erm = null;
	private TASSTheory theory;
	private final double cosObliquity, sinObliquity;

	public static void main(String[] args) {
		String ephemerisHomeName = System.getenv("saturnobserver.ephemerishome");
		
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
	
	public SaturnObserver(JPLEphemeris ephemeris, TASSTheory theory) {
		this.theory = theory;
		
		this.erm = new IAUEarthRotationModel();
		
		double obliquity = erm.meanObliquity(2451545.0);
		
		sinObliquity = Math.sin(obliquity);
		cosObliquity = Math.cos(obliquity);
		
		EarthCentre earth = new EarthCentre(ephemeris);
		
		MovingPoint sun = new PlanetCentre(ephemeris, JPLEphemeris.SUN);
		
		MovingPoint saturn = new PlanetCentre(ephemeris, JPLEphemeris.SATURN);
		
		this.apSaturn = new ApparentPlace(earth, saturn, sun, erm);
	}
	
	public void run() throws IOException, JPLEphemerisException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null)
    			System.exit(0);
    		
    		double jd = Double.parseDouble(line);
    		
    		calculateSatelliteOffsets(jd);
    	}
	}
	
	private void calculateSatelliteOffsets(double jd) throws JPLEphemerisException {
		apSaturn.calculateApparentPlace(jd);
		
		double raSaturn = apSaturn.getRightAscensionOfDate();
		double ca = Math.cos(raSaturn);
		double sa = Math.sin(raSaturn);
		
		double decSaturn = apSaturn.getDeclinationOfDate();
		double cd = Math.cos(decSaturn);
		double sd = Math.sin(decSaturn);
		
    	TASSElements[] elements = new TASSElements[8];
    	
    	double[] position = new double[3];

   		theory.calculateElements(jd, elements);
  		
   		for (int iSat = 0; iSat < 8; iSat++) {
   			theory.calculatePosition(iSat, elements[iSat], position);
   			
   			double xe = position[0], ye = position[1], ze = position[2];
   			
   			double xa = xe;
   			double ya = ye * cosObliquity - ze * sinObliquity;
   			double za = ye * sinObliquity + ze * cosObliquity;
   			
   			double q = (3600.0 * 180.0 / Math.PI)/apSaturn.getGeometricDistance();
   			
   			double ux = ca * cd, uy = sa * cd, uz = sd;
   			double vx = -sa * cd, vy = ca * cd, vz = 0.0;
   			double wx = -ca * sd, wy = -sa * sd, wz = cd;
   			
   			double dx = (vx * xa + vy * ya + vz * za) * q;
   			double dy = (wx * xa + wy * ya + wz * za) * q;
   			double dz = (ux * xa + uy * ya + uz * za) * q;
   			
   			System.out.printf(" %13.5f %1d  %8.3f  %8.3f  %8.3f ]\n", dx, dy, dz);
   		}
	}
}
