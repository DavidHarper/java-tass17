package com.obliquity.astronomy.tass17.gui;

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

import com.obliquity.astronomy.almanac.JPLEphemerisException;
import com.obliquity.astronomy.almanac.AstronomicalDate;
import com.obliquity.astronomy.almanac.JPLEphemeris;

import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class TASS17SimpleController {
	private TASS17View view;
	private TASS17Model model;
	
	public static void main(String[] args) {		
		try {
			final TASS17View view = new TASS17View();
			JPLEphemeris ephemeris = getEphemeris();
			final TASS17Model model =  new TASS17Model(ephemeris);
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						createAndShowGUI(model, view);
					} catch (IOException | JPLEphemerisException e) {
						e.printStackTrace();
					}
				}
			});
			
			TASS17SimpleController controller = new TASS17SimpleController(model, view);
			
			controller.setTimeToNow();
			
			controller.run();
		} catch (IOException | JPLEphemerisException e) {
			e.printStackTrace();
		}		
	}
	
	private static JPLEphemeris getEphemeris() throws IOException, JPLEphemerisException {
		String ephemerisHomeName = System.getProperty("ephemeris.home");
		
		if (ephemerisHomeName == null) {
			System.err.println("Set property ephemeris.home and re-run");
			System.exit(1);
		}
		
		File ephemerisHome = new File(ephemerisHomeName);
		
		if (!ephemerisHome.isDirectory()) {
			System.err.println("Not a directory: " + ephemerisHomeName);
			System.exit(2);
		}
		
		File de430 = new File(ephemerisHome, "de430/lnxp1550p2650.430");
		
		if (!de430.exists()) {
			System.err.println("File does not exist: " + de430.getAbsolutePath());
			System.exit(2);
		}

		return new JPLEphemeris(de430);
	}
	
	private static void createAndShowGUI(TASS17Model model, TASS17View view) throws IOException, JPLEphemerisException {	
		view.setModel(model);
		
		JFrame frame = new JFrame(view.getClass().getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JComponent contentPane = (JComponent) frame.getContentPane();

		contentPane.setOpaque(true);

		contentPane.setLayout(new BorderLayout());	

		contentPane.add(BorderLayout.CENTER, view);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public TASS17SimpleController(TASS17Model model, TASS17View view) {
		this.model = model;
		this.view = view;
		
		view.setModel(model);
	}
	
	public void setTime(double jd) throws JPLEphemerisException {
		model.setTime(jd);
		view.repaint();
	}
	
	public void setTimeToNow() throws JPLEphemerisException {
		long millis = System.currentTimeMillis();
		double jdNow = 2440587.5 + ((double)millis)/86400000.0;
		setTime(jdNow);
	}
	
	public void setScale(double scale) {
		view.setScale(scale);
		view.repaint();
	}
	
	public void setCentre(double xc, double yc) {
		view.setAutoCentre(false);
		view.setCentre(xc, yc);
		view.repaint();
	}
	
	public void setAutoCentre() {
		view.setAutoCentre(true);
		view.repaint();
	}
	
	public void animate(int nSteps, double stepSize, long sleep) throws JPLEphemerisException, InterruptedException {
		for (int i = 0; i < nSteps; i++) {
			model.incrementTime(stepSize);
			view.repaint();
			
			if ((i % 10) == 0)
				System.out.print(" " + i);
			
			if (i < nSteps - 1)
				Thread.sleep(sleep);
		}
		
		System.out.println();
	}
	
	private static String[] helpText = {
			"<JD> : display at the specified Julian Date",
			"<Y M D h m> : display at the specified date and time",
			"step (or <ENTER>) : advance by one step",
			"stepsize S : set stepsize to S",
			"now : display at current date and time",
			"scale S : set scale to S",
			"centre X Y : centre display at offset (X,Y)",
			"auto : auto-centre display",
			"animate N S P : animate for N steps with stepsize S and pause P milliseconds between steps",
			"show : show the current model parameters on standard output",
			"quit or exit : exit the program"
	};

	public void run() throws IOException, JPLEphemerisException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		double step = 1.0;
	
    	while (true) {
    		System.out.print("JAVA-TASS17 > ");
    		String line = br.readLine();
    		double jd = 0.0;
    		
    		if (line == null)
    			System.exit(0);
    		
    		line = line.trim();

    		String command = "step";
    		String[] words = null;
    		
    		if (line.length() > 0) {
    			words = line.split("\s+");
    			command = words[0].toLowerCase();
    		}
    		
    		switch (command) {
    		case "help":
    			for (String text : helpText)
    				System.out.println(text);
    			break;
    			
    		case "scale":
    			double scale = Double.parseDouble(words[1]);
    			setScale(scale);
    			break;
    			
    		case "centre":
    			double xc = Double.parseDouble(words[1]);
    			double yc = Double.parseDouble(words[2]);
    			setCentre(xc, yc);
    			break;
    			
    		case "auto":
    			setAutoCentre();
    			break;
    			
    		case "now":
    			setTimeToNow();
    			break;
    			
    		case "animate":
    			int nSteps = Integer.parseInt(words[1]);
    			double stepSize = Double.parseDouble(words[2]);
    			long sleep = Long.parseLong(words[3]);
    			try {
					animate(nSteps, stepSize, sleep);
				} catch (JPLEphemerisException | InterruptedException e) {
					e.printStackTrace();
				}
    			break;
    			
    		case "show":
    			model.show(System.out);
    			break;
    			
    		case "stepsize":
    			step = Double.parseDouble(words[1]);
    			break;
    			
    		case "step":
    			model.incrementTime(step);
    			view.repaint();
    			break;
    			
    		case "quit":
    		case "exit":
    			System.exit(0);
    			
    		default:
    			if (words.length == 1) {
    				try {
    					jd = Double.parseDouble(line);
    					setTime(jd);
    				}
    				catch (NumberFormatException e) {
    					e.printStackTrace();
    				}
    			} else if (words.length == 5) {
    				try {
    					int year = Integer.parseInt(words[0]);
    					int month = Integer.parseInt(words[1]);
    					int day = Integer.parseInt(words[2]);
    					int hour = Integer.parseInt(words[3]);
    					int minute = Integer.parseInt(words[4]);

    					AstronomicalDate ad = new AstronomicalDate(year, month, day, hour, minute, 0.0);

    					jd = ad.getJulianDate();
    					setTime(jd);
    				}
    				catch (NumberFormatException e) {
    					e.printStackTrace();
    				}
    			} else {
    				System.err.println("Invalid input");
    			}
    		}

    	}
	}
}
