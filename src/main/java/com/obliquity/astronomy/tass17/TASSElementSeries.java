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

public class TASSElementSeries {
	private double constantTerm = 0.0, secularRate = 0.0;
	private TASSPeriodicTerm[] periodicTerms = null;
	private int nCriticalTerms = 0;
	
	public void setConstantTerm(double constantTerm) {
		this.constantTerm = constantTerm;
	}
	
	public double getConstantTerm() {
		return constantTerm;
	}
	
	public void setSecularRate(double secularRate) {
		this.secularRate = secularRate;
	}
	
	public double getSecularRate() {
		return secularRate;
	}
	
	public void setPeriodicTerms(TASSPeriodicTerm[] periodicTerms) {
		this.periodicTerms = periodicTerms;
	}
	
	public TASSPeriodicTerm[] getPeriodicTerms() {
		return periodicTerms;
	}
	
	public void setNumberOfCriticalTerms(int nCriticalTerms) {
		this.nCriticalTerms = nCriticalTerms;
	}
	
	public int getNumberOfCriticalTerms() {
		return nCriticalTerms;
	}
	
	public double calculateLinearTerm(double t) {
		return constantTerm + t * secularRate;
	}
	
	public double calculateCriticalTermsInSine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = 0; i < nCriticalTerms; i++)
			value += periodicTerms[i].getAmplitude() * Math.sin(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}
	
	public double calculateCriticalTermsInCosine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = 0; i < nCriticalTerms; i++)
			value += periodicTerms[i].getAmplitude() * Math.cos(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}
	
	public double calculateShortPeriodTermsInSine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = nCriticalTerms; i < periodicTerms.length; i++)
			value += periodicTerms[i].getAmplitude() * Math.sin(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}
	
	public double calculateShortPeriodTermsInCosine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = nCriticalTerms; i < periodicTerms.length; i++)
			value += periodicTerms[i].getAmplitude() * Math.cos(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}
	
	public double calculateAllTermsInSine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = 0; i < periodicTerms.length; i++)
			value += periodicTerms[i].getAmplitude() * Math.sin(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}
	
	public double calculateAllTermsInCosine(double t, double[] longPeriodLongitudes) {
		double value = 0.0;
		
		for (int i = 0; i < periodicTerms.length; i++)
			value += periodicTerms[i].getAmplitude() * Math.cos(periodicTerms[i].getArgument(t, longPeriodLongitudes));
		
		return value;
	}

	public String toString() {
		return getClass().getName() + "[ constantTerm = " + constantTerm
				+ ", secularRate = " + secularRate
				+ ", array of " + periodicTerms.length + " periodic terms (" + nCriticalTerms + " critical) ]";
	}
}
