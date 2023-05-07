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
}
