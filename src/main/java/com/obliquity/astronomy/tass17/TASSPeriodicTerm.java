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

public class TASSPeriodicTerm {
	protected double amplitude, phase, frequency;
	protected int[] longPeriodCoefficients;
	
	public TASSPeriodicTerm(double amplitude, double phase, double frequency, int[] longPeriodCoefficients) {
		this.amplitude = amplitude;
		this.phase = phase;
		this.frequency = frequency;
		this.longPeriodCoefficients = longPeriodCoefficients;
	}
	
	public TASSPeriodicTerm(double amplitude, double phase, double frequency) {
		this(amplitude, phase, frequency, null);
	}
	
	public double getArgument(double t, double[] longPeriodLongitudes) {
		double argument = phase;
		
		if (longPeriodCoefficients != null && longPeriodLongitudes != null) {
			int nCoeffs = longPeriodCoefficients.length;
			int nLongitudes = longPeriodLongitudes.length;
			
			int nTerms = nCoeffs < nLongitudes ? nCoeffs : nLongitudes;
			
			for (int i = 0; i < nTerms; i++)
				if (longPeriodCoefficients[i] != 0)
					argument += longPeriodLongitudes[i] * (double)longPeriodCoefficients[i];
		}
		
		argument += frequency * t;
		
		return argument;
	}
	
	public double getArgument(double t) {
		return getArgument(t, null);
	}
	
	public double getAmplitude() {
		return amplitude;
	}
}
