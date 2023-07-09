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

import java.io.IOException;

import com.obliquity.astronomy.almanac.ApparentPlace;
import com.obliquity.astronomy.almanac.EarthCentre;
import com.obliquity.astronomy.almanac.IAUEarthRotationModel;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.MovingPoint;
import com.obliquity.astronomy.almanac.PlanetCentre;
import com.obliquity.astronomy.almanac.chebyshev.Chebyshev;
import com.obliquity.astronomy.almanac.chebyshev.Evaluatable;
import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSMovingPoint;
import com.obliquity.astronomy.tass17.TASSTheory;

public class SatelliteOffset implements Evaluatable {
	public static final int X_OFFSET = 0, Y_OFFSET = 1, Z_OFFSET = 2;
	public static final int RIGOROUS = 0, SIMPLIFIED = 1;
	
	private int method = SIMPLIFIED;
	private int component = X_OFFSET;
	private double tMinimum = 0.0, tMaximum = 0.0;
	private double[] offsets = new double[3];
	
	private int iSat;

	private ApparentPlace apSaturn;
	private ApparentPlace apSatellite;
	private IAUEarthRotationModel erm = null;
	private TASSTheory theory;
	private final double cosObliquity, sinObliquity;
	
	public SatelliteOffset(JPLEphemeris ephemeris, TASSTheory theory, int iSat) throws IOException {
		this.theory = theory;
		this.iSat = iSat;
		
		this.erm = new IAUEarthRotationModel();
		
		double obliquity = erm.meanObliquity(2451545.0);
		
		sinObliquity = Math.sin(obliquity);
		cosObliquity = Math.cos(obliquity);
		
		EarthCentre earth = new EarthCentre(ephemeris);
		
		MovingPoint sun = new PlanetCentre(ephemeris, JPLEphemeris.SUN);
		
		MovingPoint saturn = new PlanetCentre(ephemeris, JPLEphemeris.SATURN);
		
		this.apSaturn = new ApparentPlace(earth, saturn, sun, erm);
		
		MovingPoint satellite = new TASSMovingPoint(ephemeris, iSat);
		apSatellite = new ApparentPlace(earth, satellite, sun, erm);
	}
	
	public void setDateRange(double tMinimum, double tMaximum) {
		this.tMinimum = tMinimum;
		this.tMaximum = tMaximum;
	}
	
	public double getEarliestDate() {
		return tMinimum;
	}
	
	public double getLatestdate() {
		return tMaximum;
	}
	
	public void setMethod(int method) {
		if (method != RIGOROUS && method != SIMPLIFIED)
			throw new IllegalArgumentException("Method should be RIGOROUS or SIMPLIFED");
		
		this.method = method;
	}
	
	public int getMethod() {
		return method;
	}
	
	public void setComponent(int component) {
		if (component < 0 || component > 2)
			throw new IllegalArgumentException("Component index is out of range");
		
		this.component = component;
	}
	
	public int getComponent() {
		return component;
	}

	public double evaluate(double x) {
		if (tMinimum == 0.0 || tMaximum == 0.0)
			throw new IllegalStateException("Date range has not been set");
		
		double t = tMinimum + (x + 1.0) * (tMaximum - tMinimum)/2.0;
		
		try {
			calculateOffsets(t);
		} catch (JPLEphemerisException e) {
			e.printStackTrace();
		}
		
		return offsets[component];
	}
	
	private void calculateOffsets(double jd) throws JPLEphemerisException {
    	switch (method) {
    	case RIGOROUS:
    		calculateRigorousOffsets(jd);
    		break;
    		
    	case SIMPLIFIED:
    		calculateSimplifiedOffsets(jd);
    		break;

    	default:
    		throw new IllegalStateException("Method was not one of RIGOROUS or SIMPLIFIED");
    	}
	}
	
	private void calculateRigorousOffsets(double jd) throws JPLEphemerisException {
		apSaturn.calculateApparentPlace(jd);
		
		double raSaturn = apSaturn.getRightAscensionJ2000();
		double decSaturn = apSaturn.getDeclinationJ2000();
  		double gdSaturn = apSaturn.getGeometricDistance();

   		apSatellite.calculateApparentPlace(jd);
   			
   		double raSatellite = apSatellite.getRightAscensionJ2000();
   		double decSatellite = apSatellite.getDeclinationJ2000();
   		double gdSatellite = apSatellite.getGeometricDistance();
   			
   		offsets[0] = (raSatellite - raSaturn) * Math.cos(decSaturn) * 3600.0 * 180.0/Math.PI;
   		offsets[1] = (decSatellite - decSaturn) * 3600.0 * 180.0/Math.PI;
   		offsets[2] = gdSatellite - gdSaturn;
	}
	
	private void calculateSimplifiedOffsets(double jd) throws JPLEphemerisException {
		apSaturn.calculateApparentPlace(jd);
		
		double raSaturn = apSaturn.getRightAscensionJ2000();
		double decSaturn = apSaturn.getDeclinationJ2000();
    	double jdSatellites = jd - apSaturn.getLightTime();
    	
		double ca = Math.cos(raSaturn);
		double sa = Math.sin(raSaturn);
		double cd = Math.cos(decSaturn);
		double sd = Math.sin(decSaturn);

    	TASSElements elements = new TASSElements();
    	theory.calculateElements(jdSatellites, iSat, elements);

    	double[] position = new double[3];

   		theory.calculatePosition(iSat, elements, position);
   			
   		double xe = position[0], ye = position[1], ze = position[2];
   			
   		double xa = xe;
   		double ya = ye * cosObliquity - ze * sinObliquity;
   		double za = ye * sinObliquity + ze * cosObliquity;
   			
   		double q = (3600.0 * 180.0 / Math.PI)/apSaturn.getGeometricDistance();
   			
   		double ux = ca * cd, uy = sa * cd, uz = sd;
   		double vx = -sa, vy = ca, vz = 0.0;
   		double wx = -ca * sd, wy = -sa * sd, wz = cd;
   			
   		offsets[0] = (vx * xa + vy * ya + vz * za) * q;
   		offsets[1] = (wx * xa + wy * ya + wz * za) * q;
   		offsets[2] = (ux * xa + uy * ya + uz * za) * q;
	}
	
	public void calculateChebyshevCoefficients(double[] coeffs) {
		Chebyshev.calculateChebyshevCoefficients(this, coeffs);
	}
	
	public double[] calculateChebyshevCoefficients(int N) {
		double[] coeffs = new double[N];
		calculateChebyshevCoefficients(coeffs);
		return coeffs;
	}
}
