# java-tass17

Java classes to calculate positions of Saturn's major satellites
using the TASS 1.7 orbit model developed by Alain Vienne and Luc Duriez.
For full details of the development and use of the TASS 1.7 model,
see the following papers by Vienne and Duriez which were published in the journal
*Astronomy & Astrophysics* between 1991 and 1997:

- [A general theory of motion for the eight major satellites of Saturn. I - Equations and method of resolution](https://ui.adsabs.harvard.edu/abs/1991A%26A...243..263D/abstract)
- [A general theory of motion for the eight major satellites of Saturn. II - Short-period perturbations](https://ui.adsabs.harvard.edu/abs/1991A%26A...246..619V/abstract)
- [A general theory of motion for the eight major satellites of Saturn. III - Long-period perturbations](https://ui.adsabs.harvard.edu/abs/1992A%26A...257..331V/abstract)
- [TASS1.6: Ephemerides of the major Saturnian satellites](https://ui.adsabs.harvard.edu/abs/1995A%26A...297..588V/abstract)
- [Theory of motion and ephemerides of Hyperion](https://ui.adsabs.harvard.edu/abs/1997A%26A...324..366D/abstract)

## Building the library

This project uses Gradle as its build tool.  You will need a recent
version of Gradle and a Java compiler capable of generating Java 16
code.  The source code is in the standard location for a Gradle
project, namely

`src/main/java`

The simplest option is to generate a JAR file containing the library
and all of the test classes.

`gradle jar`

The JAR file can be found in **build/libs**

To generate a Maven artefact, run

`gradle publish`

The generated files can be found in **build/repo**

## Documentation

The Javadoc documentation for the classes in this library is sparse
to non-existent.  If you wish to use this library, you should look
at the examples in the *com.obliquity.astronomy.tass17.test* and
*com.obliquity.astronomy.tass17.gui* packages to understand how the
library works.

## Requirements

The data files containing the TASS17 periodic terms are bundled with
this project in the *src/main/resources* directory.  These are
sufficient to calculate the Saturnicentric rectangular coordinates of
the eight major satellites.

If the user wishes to calculate the positions as seen from Earth,
they will need to provide a suitable Solar System ephemeris.  The
demonstration classes described below use the NASA JPL DE430
ephemeris and the *astrojava* library.  Gradle will import the
latter, but the user must obtain a copy of the former from the JPL
SSD web site.

## Example applications

### A Simple Almanac Program

The class *com.obliquity.astronomy.tass17.test.SaturnObserver*
calculates the positions of the eight major satellites as seen
from the Earth, as offsets in arc-seconds from the centre of
Saturn.

The Java run-time property *saturnobserver.ephemerishome*
**must** be defined, and it must point to the directory
containing the JPL DE430 ephemeris.  The ephemeris file is assumed
to be named *de430/lnxp1550p2650.430* **relative** to the directory
defined by this property.

### A Graphical Almanac Program

The class *com.obliquity.astronomy.tass17.gui.TASS17SimpleController*
displays a graphical representation of Saturn and its satellites as
seen from Earth.  The program reads commands from standard input.
The most useful commands are as follows:

- **<RETURN>** increments the time by the current step size and re-draws the view.
- **NNNNNNN.N** sets the time to the specified Julian Date and re-draws the view.
- **YYYY MM DD HH MM** sets the date and time and re-draws the view.
- **scale N** changes the scale to *N* pixels per arc-second. The default is 10.
- **stepsize N** changes the step size to *N* days. The default is 1.
- **animate STEPS STEPSIZE PAUSE** runs a basic animation, incrementing the time by the
specified stepsize, re-drawing the view, and then pausing for the specified number of
milliseconds (except after the final step). Note that this does **NOT** change the
stepsize that is set using the **stepsize** command.
- **show** displays the positions of the satellites at the time that is currently
being displayed.  It is mainly for diagnostic use.
- **quit** or **exit** exits the program. **Ctrl+D** or end-of-file will also exit
the program.

See the source code for a full list of commands.

The Java run-time property *saturnobserver.ephemerishome*
**must** be defined, and it must point to the directory
containing the JPL DE430 ephemeris.  The ephemeris file is assumed
to be named *de430/lnxp1550p2650.430* **relative** to the directory
defined by this property.

## DISCLAIMER

THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY
APPLICABLE LAW.  EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT
HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY
OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM
IS WITH YOU.  SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF
ALL NECESSARY SERVICING, REPAIR OR CORRECTION.