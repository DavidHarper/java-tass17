package com.obliquity.astronomy.tass17.test;

import java.io.IOException;

import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSTheory;

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

public class TheorySpeedTester {
    public static void main(String[] args) {
    	if (args.length < 3) {
    		System.err.println("One or more mandatory arguments missing: startdate stepsize steps");
    		System.exit(1);
    	}
    	
    	double jd = Double.parseDouble(args[0]);
    	double stepsize = Double.parseDouble(args[1]);
    	int steps = Integer.parseInt(args[2]);
    	
    	TheorySpeedTester tester = new TheorySpeedTester();
    	
    	try {
			tester.run(jd, stepsize, steps);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void run(double jd, double stepsize, int steps) throws IOException {
    	boolean verbose = Boolean.getBoolean("verbose");
    	
    	TASSTheory theory = new TASSTheory();
    	    	
    	TASSElements[] elements = new TASSElements[8];
    	
    	double[] position = new double[3];
    	
    	long tick0 = System.currentTimeMillis();
    	
    	for (int i = 0; i < steps; i++) {
    		theory.calculateElementsForAllSatellites(jd, elements);
  		
    		for (int iSat = 0; iSat < 8; iSat++) {   			    			
    			theory.calculatePosition(iSat, elements[iSat], position);
    			
    			if (verbose)
    				System.out.printf(" %13.5f %d %13.8f %13.8f %13.8f\n",jd,  iSat+1, position[0], position[1], position[2]);
    		}
    		
    		jd += stepsize;
    	}
    	
    	long ticks = System.currentTimeMillis() - tick0;
    	
    	System.out.println("Time for " + steps + " evaluations: " + ticks + " ms");
    }
}
