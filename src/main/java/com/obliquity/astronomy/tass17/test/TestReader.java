package com.obliquity.astronomy.tass17.test;

import java.io.IOException;

import com.obliquity.astronomy.tass17.TASSDataFileReader;
import com.obliquity.astronomy.tass17.TASSElementSeries;

public class TestReader {
    public static void main(String[] args) {
    	TestReader reader = new TestReader();
    	try {
			reader.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void run() throws IOException {
    	TASSDataFileReader tdfr = new TASSDataFileReader();
    	
    	for (int iSat = 1; iSat < 9; iSat++) {
    		for (int iElem = 1; iElem < 5; iElem++) {
    			String filename = String.format("/tass17/S%02d_%02d.dat", iSat, iElem);
    			
    			System.out.println("Reading resource " + filename);
    			
    			TASSElementSeries result = tdfr.readTerms(filename);
    			
    			System.out.println(result);
    		}
    	}
    }
}
