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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class TASS17View extends JPanel {
	private final int preferredWidth = 1000;
	private final int preferredHeight = 800;

	private double xc = 0.0, yc = 0.0;
	private double scale = 10.0;
	
	private boolean autoCentre = true;
	
	private Color SATURN_COLOUR = new Color(0xFF, 0xEE, 0xBB);
	private Color A_RING_COLOUR = new Color(0xFF, 0xFF, 0xF0);
	private Color B_RING_COLOUR = new Color(0xFF, 0xFF, 0xF0);
	private Color C_RING_COLOUR = new Color(0x66, 0x66, 0x57, 0x7f);
	
	private Color DARK_RING_COLOUR = Color.DARK_GRAY;
	private Color UNLIT_RING_COLOUR = Color.DARK_GRAY.darker();
	
	private Color GRID_COLOUR = new Color(0x40, 0x40, 0x40);
	
	private Color SATELLITE_COLOUR = Color.RED;
	
	private Color DATE_COLOUR = Color.YELLOW;
	
	private final String[] SATELLITE_LABELS = {
			"Mi", "En", "Te", "Di", "Rh", "Ti", "Hy", "Ia"
	};

	private final double FLATTENING = 0.90;
	
	private TASS17Model model;
	
	public TASS17View() {
		super();
		setBackground(Color.BLACK);
	}
	
	public TASS17View(TASS17Model model) {
		this();
		setModel(model);
	}
	
	public void setModel(TASS17Model model) {
		this.model = model;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(preferredWidth, preferredHeight);
	}
	
	public void setCentre(double xc, double yc) {
		this.xc = xc;
		this.yc = yc;
	}
	
	public double getXCentre() {
		return xc;
	}
	
	public double getYCentre() {
		return yc;
	}
	
	public void setAutoCentre(boolean autoCentre) {
		this.autoCentre = autoCentre;
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	
	public double getSaturnRadius() {
		return scale * model.getSaturnSemiDiameter();
	}
	
	public double getEarthLatitude() {
		return model.getSaturnRingAnglesForEarth().B * Math.PI/180.0;
	}
	
	public double getSunLatitude() {
		return model.getSaturnRingAnglesForSun().B * Math.PI/180.0;
	}
	
	public double getSineEarthLatitude() {
		return Math.sin(getEarthLatitude());
	}

	public double getPositionAngle() {
		return model.getSaturnRingAnglesForEarth().P * Math.PI/180.0;
	}
	
	public void paintComponent(Graphics gOriginal) {
		Graphics2D g = (Graphics2D)gOriginal;
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
		Rectangle r = getBounds();
		
		g.setColor(Color.BLACK);

		g.fillRect(r.x, r.y, r.width, r.height);
		
		if (autoCentre) {
			xc = (double)r.x + 0.5 * (double)r.width;
			yc = (double)r.y + 0.5 * (double)r.height;
		}
		
		g.setColor(GRID_COLOUR);
		
		double x = 0.0;
		double dx = 10.0;
		
		while (true) {
			double xl = xc + x * scale;
			
			if ((int)xl > r.x+r.width)
				break;
			
			g.drawLine((int)xl, r.y, (int)xl, r.y+r.height);
			
			x += dx;
		}
		
		x = -dx;
		
		while (true) {
			double xl = xc + x * scale;
			
			if ((int)xl < r.x)
				break;
			
			g.drawLine((int)xl, r.y, (int)xl, r.y+r.height);
			
			x -= dx;
		}
		
		double y = 0.0;
		double dy = 10.0;
		
		while (true) {
			double yl = yc + y * scale;
			
			if ((int)yl > r.y+r.height)
				break;
			
			g.drawLine(r.x, (int)yl, r.x+r.width, (int)yl);
			
			y += dy;
		}
		
		y = -dy;
		
		while (true) {
			double yl = yc + y * scale;
			
			if ((int)yl < r.y)
				break;
			
			g.drawLine(r.x, (int)yl, r.x+r.width, (int)yl);
			
			y -= dy;
		}

		g.setColor(SATURN_COLOUR);
		
		AffineTransform xform = new AffineTransform();
		xform.rotate(-getPositionAngle(), xc, yc);
		
		Path2D.Double saturnGlobe = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		
		double saturnRadius = getSaturnRadius();
		
		saturnGlobe.append(new Arc2D.Double(xc-saturnRadius, yc-saturnRadius*FLATTENING, 2.0*saturnRadius, 2.0*saturnRadius*FLATTENING, 0.0, 360.0, Arc2D.PIE), false);
		
		g.fill(xform.createTransformedShape(saturnGlobe));
		
		Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		
		double boxSize = saturnRadius * 2.50;
		
		path.append(new Rectangle2D.Double(xc-boxSize, yc-boxSize, 2.0*boxSize, 2.0*boxSize), false);
		
		path.append(new Arc2D.Double(xc-saturnRadius, yc-saturnRadius*FLATTENING, 2.0*saturnRadius, 2.0*saturnRadius*FLATTENING, 0.0f, getSineEarthLatitude() > 0.0 ? 180.0 : -180.0, Arc2D.PIE), false);
		
		Shape savedClip = g.getClip();
		
		g.setClip(xform.createTransformedShape(path));
		
		Path2D.Double ringA = getRingPath(saturnRadius, 2.03, 2.27);
		Path2D.Double ringB = getRingPath(saturnRadius, 1.53, 1.95);
		Path2D.Double ringC = getRingPath(saturnRadius, 1.24, 1.53);
		
		double earthB = getEarthLatitude();
		double sunB = getSunLatitude();
		
		boolean ringsUnlit = (earthB < 0.0 && sunB > 0.0 ) || (earthB > 0.0 && sunB < 0.0);
		boolean lowSunAngle = Math.abs(sunB) < 0.05;
		
		Color ringColour = ringsUnlit ? UNLIT_RING_COLOUR : (lowSunAngle ? DARK_RING_COLOUR : C_RING_COLOUR);
		
		g.setColor(ringColour);
		g.fill(xform.createTransformedShape(ringC));
		
		ringColour = ringsUnlit ? UNLIT_RING_COLOUR : (lowSunAngle ? DARK_RING_COLOUR : B_RING_COLOUR);
		g.setColor(ringColour);
		g.fill(xform.createTransformedShape(ringB));
		
		ringColour = ringsUnlit ? UNLIT_RING_COLOUR : (lowSunAngle ? DARK_RING_COLOUR : A_RING_COLOUR);
		g.setColor(ringColour);
		g.fill(xform.createTransformedShape(ringA));
		
		g.setClip(savedClip);
		
		double[] offsets = new double[3];
		
		g.setColor(SATELLITE_COLOUR);
		g.setFont(new Font("SansSerif", Font.BOLD, 10));
		
		for (int iSat = 0; iSat < 8; iSat++) {
			model.getSatelliteOffsets(iSat, offsets);
			
			dx = offsets[0];
			dy = offsets[1];
			double dz = offsets[2];
			
			double saturnA = model.getSaturnSemiDiameter();
			double saturnB = FLATTENING * saturnA;
			
			double rSquared = Math.pow(dx/saturnA, 2.0) + Math.pow(dy/saturnB, 2.0);
			
			boolean visible = rSquared > 1.0 || dz < 0.0;

			if (visible) {
				double xSat = xc - scale * dx;
				double ySat = yc - scale * dy;

				Shape moon = new Ellipse2D.Double(xSat - 2.0, ySat - 2.0, 4.0, 4.0);

				g.fill(moon);

				g.drawString(SATELLITE_LABELS[iSat], (float)xSat + 3.0f, (float)ySat + 3.0f);
			}
		}
		
		g.setColor(DATE_COLOUR);
		g.setFont(new Font("SansSerif", Font.PLAIN, 20));
		
		g.drawString(model.getDateAsText(), 10.0f, 25.0f);
	}
	
	private Path2D.Double getRingPath(double saturnRadius, double innerRadius, double outerRadius) {
		double sinEarthLatitude = getSineEarthLatitude();
		
		double innerWidth = saturnRadius * innerRadius;
		double innerHeight = innerWidth * Math.abs(sinEarthLatitude);
		
		double outerWidth = saturnRadius * outerRadius;
		double outerHeight = outerWidth * Math.abs(sinEarthLatitude);
		
		Path2D.Double ring = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		
		ring.append(new Arc2D.Double(xc-innerWidth, yc-innerHeight, 2.0f*innerWidth, 2.0f*innerHeight, 0.0f, 360.0f, Arc2D.PIE), false);
		ring.append(new Arc2D.Double(xc-outerWidth, yc-outerHeight, 2.0f*outerWidth, 2.0f*outerHeight, 0.0f, 360.0f, Arc2D.PIE), false);
		
		return ring;
	}

}
