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
			model.setTimeToNow();
			
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
			
			controller.run();
		} catch (IOException | JPLEphemerisException e) {
			e.printStackTrace();
		}		
	}
	
	private static JPLEphemeris getEphemeris() throws IOException, JPLEphemerisException {
		String ephemerisHomeName = System.getProperty("tass17simplecontroller.ephemerishome");
		
		if (ephemerisHomeName == null) {
			System.err.println("Set property saturnobserver.ephemerishome and re-run");
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
		view.setAutoCentre(true);
		
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
	
	public void run() throws IOException, JPLEphemerisException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
    	while (true) {
    		String line = br.readLine();
    		
    		if (line == null || line.trim().length() == 0)
    			System.exit(0);
    		
    		double jd = Double.parseDouble(line);
    		
    		setTime(jd);
    	}
	}
}
