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

public class TASS17SimpleController {
	private TASS17Model model;
	private TASS17View view;
	
	public TASS17SimpleController(TASS17Model model, TASS17View view) {
		view.setModel(model);
	}
	
	public void setTime(double jd) throws JPLEphemerisException {
		model.setTime(jd);
		view.repaint();
	}
}
