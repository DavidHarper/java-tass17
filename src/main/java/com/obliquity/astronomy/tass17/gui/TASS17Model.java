package com.obliquity.astronomy.tass17.gui;

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
import java.io.PrintStream;

import com.obliquity.astronomy.almanac.ApparentPlace;
import com.obliquity.astronomy.almanac.AstronomicalDate;
import com.obliquity.astronomy.almanac.EarthCentre;
import com.obliquity.astronomy.almanac.IAUEarthRotationModel;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.MovingPoint;
import com.obliquity.astronomy.almanac.PlanetCentre;
import com.obliquity.astronomy.almanac.SaturnRingAngles;
import com.obliquity.astronomy.almanac.AlmanacData;
import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSTheory;

public class TASS17Model {
	private ApparentPlace apSaturn, apSun;
	private IAUEarthRotationModel erm = null;
	private TASSTheory theory;
	private final double cosObliquity, sinObliquity;
	private double jd = Double.NaN;
	private double[][] satelliteOffsets = new double[8][3];
	private boolean validData = false;
	private AlmanacData saturnData = new AlmanacData();
	private String dateAsText;

	public TASS17Model(JPLEphemeris ephemeris) throws IOException {
		this.theory = new TASSTheory();
		
		this.erm = new IAUEarthRotationModel();
		
		double obliquity = erm.meanObliquity(2451545.0);
		
		sinObliquity = Math.sin(obliquity);
		cosObliquity = Math.cos(obliquity);
		
		EarthCentre earth = new EarthCentre(ephemeris);
		
		MovingPoint sun = new PlanetCentre(ephemeris, JPLEphemeris.SUN);
		
		MovingPoint saturn = new PlanetCentre(ephemeris, JPLEphemeris.SATURN);
		
		this.apSaturn = new ApparentPlace(earth, saturn, sun, erm);

		this.apSun = new ApparentPlace(earth, sun, sun, erm);
	}
	
	public void setTime(double jd) throws JPLEphemerisException {
		this.jd = jd;
		calculateData();
	}
	
	public void incrementTime(double djd) throws JPLEphemerisException {
		this.jd += djd;
		calculateData();
	}
	
	public boolean haveValidData() {
		return validData;
	}
	
	public void getSatelliteOffsets(int iSat, double[] offsets) {
		for (int i = 0; i < 3; i++) {
			offsets[i] = satelliteOffsets[iSat][i];
		}
	}
	
	public double[] getSatelliteOffsets(int iSat) {
		double[] offsets = new double[3];
		getSatelliteOffsets(iSat, offsets);
		return offsets;
	}
	
	public SaturnRingAngles getSaturnRingAnglesForEarth() {
		return saturnData.saturnRingAnglesForEarth;
	}
	
	public SaturnRingAngles getSaturnRingAnglesForSun() {
		return saturnData.saturnRingAnglesForSun;
	}
	
	public double getSaturnMagnitude() {
		return saturnData.magnitude;
	}
	
	public double getSaturnSemiDiameter() {
		return saturnData.semiDiameter;
	}
	
	public String getDateAsText() {
		return dateAsText;
	}
	
	private void calculateData() throws JPLEphemerisException {
		saturnData = AlmanacData.calculateAlmanacData(apSaturn, apSun, jd, AlmanacData.J2000, saturnData);
		
		apSaturn.calculateApparentPlace(jd);

		double raSaturn = apSaturn.getRightAscensionJ2000();
		double ca = Math.cos(raSaturn);
		double sa = Math.sin(raSaturn);
		
		double decSaturn = apSaturn.getDeclinationJ2000();
		double cd = Math.cos(decSaturn);
		double sd = Math.sin(decSaturn);
		
    	TASSElements[] elements = new TASSElements[8];
    	
    	double[] position = new double[3];
			
		double q = (3600.0 * 180.0 / Math.PI)/apSaturn.getGeometricDistance();
		
		double lightTime = apSaturn.getLightTime();
		
		double jdSatellites = jd - lightTime;

   		theory.calculateElementsForAllSatellites(jdSatellites, elements);
  		
   		for (int iSat = 0; iSat < 8; iSat++) {
   			theory.calculatePosition(iSat, elements[iSat], position);
   			
   			double xe = position[0], ye = position[1], ze = position[2];
   			
   			double xa = xe;
   			double ya = ye * cosObliquity - ze * sinObliquity;
   			double za = ye * sinObliquity + ze * cosObliquity;
   			
   			double ux = ca * cd, uy = sa * cd, uz = sd;
   			double vx = -sa * cd, vy = ca * cd, vz = 0.0;
   			double wx = -ca * sd, wy = -sa * sd, wz = cd;
   			
   			satelliteOffsets[iSat][0] = (vx * xa + vy * ya + vz * za) * q;
   			satelliteOffsets[iSat][1] = (wx * xa + wy * ya + wz * za) * q;
   			satelliteOffsets[iSat][2] = (ux * xa + uy * ya + uz * za) * q;
   		}
   		
   		AstronomicalDate ad = new AstronomicalDate(jd);
   		
   		ad.roundToNearestMinute();
   		
   		dateAsText = String.format("%4d-%02d-%02d %02d:%02d", ad.getYear(), ad.getMonth(), ad.getDay(), ad.getHour(), ad.getMinute());
   		
   		validData = true;
	}

	private static final String SEPARATOR = "================================================================================";
	
	public void show(PrintStream ps) {
		ps.println(SEPARATOR);
		ps.printf("JD = %13.5f = %s\n", jd, dateAsText);
		ps.printf("Earth\n    SD = %6.2f\n     B = %6.2f\n     P = %6.2f\nSun\n     B = %6.2f\n",
				getSaturnSemiDiameter(),
				saturnData.saturnRingAnglesForEarth.B, saturnData.saturnRingAnglesForEarth.P,
				saturnData.saturnRingAnglesForSun.B);
		ps.println("Moons");
		for (int iSat = 0; iSat < 8; iSat++)
			ps.printf("    %1d    %8.3f  %8.3f  %8.3f\n", iSat, satelliteOffsets[iSat][0], satelliteOffsets[iSat][1], satelliteOffsets[iSat][2]);
	}
}
