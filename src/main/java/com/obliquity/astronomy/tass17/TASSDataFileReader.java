package com.obliquity.astronomy.tass17;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TASSDataFileReader {
	public TASSElementSeries readTerms(String file) throws IOException {
		InputStream is = getClass().getResourceAsStream(file);
		
		TASSElementSeries result= readTerms(is);
		
		is.close();
		
		return result;
	}

	private TASSElementSeries readTerms(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		String line = br.readLine();
		
		String[] words = line.split("\s+");
		
		int[] headerData = new int[words.length];
		
		for (int i = 0; i < words.length; i++)
			headerData[i] = Integer.parseInt(words[i]);
		
		return (headerData[0] == TASSConstants.HYPERION) ? readHyperionTerms(br, headerData) : readTerms(br, headerData);
	}

	private TASSElementSeries readTerms(BufferedReader br, int[] headerData) throws IOException {
		TASSElementSeries result = new TASSElementSeries();
		
		int nTerms = headerData[2];
		
		result.setNumberOfCriticalTerms(headerData[3]);
		
		if (headerData[1] == 2) {
			String line = br.readLine().strip();
			String[] words = line.split("\s+");
			result.setConstantTerm(Double.parseDouble(words[0]));
			result.setSecularRate(Double.parseDouble(words[1]));
			nTerms--;
		}
		
		TASSPeriodicTerm[] terms = new TASSPeriodicTerm[nTerms];
		
		for (int i = 0; i < nTerms; i++) {
			String line = br.readLine().strip();
			
			String[] words = line.split("\s+");
			
			double amplitude = Double.parseDouble(words[1]);
			double phase = Double.parseDouble(words[2]);
			double frequency = Double.parseDouble(words[3]);
			
			int[] lpc = new int[8];
			
			for (int j = 0; j < 8; j++)
				lpc[j] = Integer.parseInt(words[j+4]);
			
			terms[i] = new TASSPeriodicTerm(amplitude, phase, frequency, lpc);
		}
		
		result.setPeriodicTerms(terms);

		return result;
	}

	private TASSElementSeries readHyperionTerms(BufferedReader br, int[] headerData) throws IOException {
		TASSElementSeries result = new TASSElementSeries();
		
		int nTerms = headerData[2];
		
		if (headerData[1] < 3) {
			String line = br.readLine().strip();
			result.setConstantTerm(Double.parseDouble(line));
			nTerms--;
		}
		
		TASSPeriodicTerm[] terms = new TASSPeriodicTerm[nTerms];
		
		for (int i = 0; i < nTerms; i++) {
			String line = br.readLine().strip();
			
			String[] words = line.split("\s+");
			
			double amplitude = Double.parseDouble(words[0]);
			double phase = Double.parseDouble(words[1]);
			double frequency = Double.parseDouble(words[2]);
			
			terms[i] = new TASSPeriodicTerm(amplitude, phase, frequency);
		}
		
		result.setPeriodicTerms(terms);
		
		return result;
	}
}
