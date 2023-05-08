package com.obliquity.astronomy.tass17;

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
