package com.obliquity.astronomy.tass17;

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
