package com.obliquity.astronomy.tass17.test;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.obliquity.astronomy.tass17.TASSElements;
import com.obliquity.astronomy.tass17.TASSTheory;

public class TheoryTester {
    public static void main(String[] args) {
    	TheoryTester tester = new TheoryTester();
    	try {
			tester.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void run() throws IOException {
    	TASSTheory theory = new TASSTheory();
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	TASSElements[] elements = new TASSElements[8];
    	
    	double[] position = new double[3];
    	double[] velocity = new double[3];
    	
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null)
    			System.exit(0);
    		
    		double jd = Double.parseDouble(line);
    		
    		theory.calculateElementsForAllSatellites(jd, elements);
  		
    		for (int iSat = 0; iSat < 8; iSat++) {   			
    			System.out.printf(" %13.5f %d  %13.8f %13.8f %13.8f  %13.8f %13.8f %13.8f\n", jd, iSat+1,
    					elements[iSat].meanMotionAdjustment,
    					elements[iSat].lambda,
    					elements[iSat].k,
    					elements[iSat].h,
    					elements[iSat].q,
    					elements[iSat].p);
    			
    			theory.calculatePositionAndVelocity(iSat, elements[iSat], position, velocity);
    			
    			double speed = Math.sqrt(velocity[0]*velocity[0] + velocity[1]*velocity[1] + velocity[2]*velocity[2]);
    			
    			System.out.printf(" %13.5f %d  %13.8f %13.8f %13.8f  %13.8f %13.8f %13.8f  (%13.8f)\n",jd,  iSat+1,
    					position[0], position[1], position[2],
    					velocity[0], velocity[1], velocity[2],
    					speed);
    		}
    	}
    }
}
