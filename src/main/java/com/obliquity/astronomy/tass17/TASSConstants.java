package com.obliquity.astronomy.tass17;

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

public class TASSConstants {
	public static final int MIMAS = 0, ENCELADUS = 1, TETHYS = 2, DIONE = 3, RHEA = 4, TITAN = 5, HYPERION = 6, IAPETUS = 7;
	
	public static final double K_SQUARED = 0.01720209895E0;
	public static final double MASS_OF_SATURN = 1.0/3498.790;
	public static final double GK1 = Math.pow(K_SQUARED * 365.25, 2.0) * MASS_OF_SATURN;
	
	public static final double INCL_SATURN_EQUATOR = 28.0512 * Math.PI/180.0, NODE_SATURN_EQUATOR = 169.5291 * Math.PI/180.0;
	
	public static final double MASSES[] = {	    
	    1.0/0.1577287066246e+08,
	    1.0/0.6666666666667E+07,
	    1.0/0.9433962264151E+06,
	    1.0/0.5094243504840E+06,
	    1.0/0.2314814814815E+06,
	    1.0/0.4225863977890E+04,
	    1.0/0.3333333333333E+08,
	    1.0/0.3225806451613E+06,
	    1.0/0.2858130953844E-03,
	};
	
	public static final double MEAN_MOTIONS[] = {
	   0.6667061728782E+01,
	   0.4585536751534E+01,
	   0.3328306445055E+01,
	   0.2295717646433E+01,
	   0.1390853715957E+01,
	   0.3940425676910E+00,
	   0.2953088138695E+00,
	   0.7920197763193E-01,
	   0.5839811452566E-03
	};
	
	public static final double EPOCH = 2444240.0;
	
	public static final double EPOCH_HYPERION = 2451545.0;
}
