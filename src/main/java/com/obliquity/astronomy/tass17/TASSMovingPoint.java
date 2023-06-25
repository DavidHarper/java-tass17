package com.obliquity.astronomy.tass17;

import java.io.IOException;

import com.obliquity.astronomy.almanac.IAUEarthRotationModel;
import com.obliquity.astronomy.almanac.JPLEphemeris;
import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.Matrix;

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

import com.obliquity.astronomy.almanac.MovingPoint;
import com.obliquity.astronomy.almanac.PlanetCentre;
import com.obliquity.astronomy.almanac.StateVector;
import com.obliquity.astronomy.almanac.Vector;

public class TASSMovingPoint implements MovingPoint {
	private IAUEarthRotationModel erm;
	private TASSTheory theory;
	private MovingPoint saturn;
	private final int satelliteID;
	private Matrix rotateTASS17ToJ2000;
	
	public TASSMovingPoint(JPLEphemeris ephemeris, int satelliteID) throws IOException {
		this.theory = new TASSTheory();

		this.erm = new IAUEarthRotationModel();
		
		double obliquity = erm.meanObliquity(2451545.0);
		
		double sinObliquity = Math.sin(obliquity);
		double cosObliquity = Math.cos(obliquity);
		
		double[][] m = {
				{ 1.0, 0.0, 0.0 },
				{ 0.0, cosObliquity, -sinObliquity}, 
				{ 0.0, sinObliquity, cosObliquity }
		};
		
		rotateTASS17ToJ2000 = new Matrix();
		rotateTASS17ToJ2000.setComponents(m);

		this.saturn = new PlanetCentre(ephemeris, JPLEphemeris.SATURN);
		this.satelliteID = satelliteID;
	}

	public int getBodyCode() {
		return satelliteID;
	}

	public JPLEphemeris getEphemeris() {
		return saturn.getEphemeris();
	}

	public double getEpoch() {
		return saturn.getEpoch();
	}

	public double getEarliestDate() {
		return saturn.getEarliestDate();
	}

	public double getLatestDate() {
		return saturn.getLatestDate();
	}

	public boolean isValidDate(double t) {
		return saturn.isValidDate(t);
	}

	public Vector getPosition(double jd) throws JPLEphemerisException {
		Vector pos = new Vector();
		
		getPosition(jd, pos);
		
		return pos;
	}

	public void getPosition(double jd, Vector pos) throws JPLEphemerisException {
		Vector saturnPosition = saturn.getPosition(jd);
		
		TASSElements elements = theory.calculateElements(jd, satelliteID);
		
		double[] position = new double[3];
		
		theory.calculatePosition(satelliteID, elements, position);
		
		Vector v = new Vector(position);
		
		v.multiplyBy(rotateTASS17ToJ2000);
		
		v.add(saturnPosition);
		
		pos.copy(v);
	}

	public StateVector getStateVector(double jd) throws JPLEphemerisException {
		StateVector sv = new StateVector(new Vector(), new Vector());
		
		getStateVector(jd, sv);
		
		return sv;
	}

	public void getStateVector(double jd, StateVector sv) throws JPLEphemerisException {
		StateVector saturnState = saturn.getStateVector(jd);
		
		TASSElements elements = theory.calculateElements(jd, satelliteID);
		
		double[] position = new double[3];
		double[] velocity = new double[3];
		
		theory.calculatePositionAndVelocity(satelliteID, elements, position, velocity);
		
		Vector vPos = new Vector(position);
		Vector vVel = new Vector(velocity);
		
		vPos.multiplyBy(rotateTASS17ToJ2000);
		vVel.multiplyBy(rotateTASS17ToJ2000);
		
		saturnState.getPosition().add(vPos);
		saturnState.getVelocity().add(vVel);;
		
		sv.setPosition(saturnState.getPosition());
		sv.setVelocity(saturnState.getVelocity());
	}
}
