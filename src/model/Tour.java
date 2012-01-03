package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

public class Tour {

	private final double nwLat[]; //Northwest latitude point(s) in degrees
	private final double nwLong[]; //Northwest longitude point(s) in degrees
	private final double seLat[]; //Southeast latitude point(s) in degrees
	private final double seLong[]; //Southeast longitude point(s) in degrees
	private final double alt[]; //Eye-level altitude in km
//	private final double elev[]; //Average elevation of tour.
	private final double waitTime; //Interval [sec] for Google Earth to wait to cache each view
	private final double overlapFraction = 0.1; //Make tiled views of GE overlap by this fraction to ensure good coverage. 
	
	private static final double a=6378.137; //Major radius of the earth.
	private static final double b=6356.752; //Minor radius of the earth in km.
	
	private final String filename;
	/**
	 * Tour is conducted from northwest
	 * @param nw Northwest point of tour - a vector specifying [latitude,longitude]
	 * @param sw
	 * @param alt
	 */
	public Tour(double nwLat[], double nwLong[], double seLat[], double seLong[], double alt[], double waitTime, String filename)  throws IOException {
		//Validation
		System.out.println("nwLatLength="+nwLat.length+" nwLongLength="+nwLong.length+" seLatLength="+seLat.length+" seLongLength="+seLong.length+" altLength="+alt.length);
		if( nwLat.length!=nwLong.length || nwLat.length!=seLat.length || nwLat.length!=seLong.length || nwLat.length!=alt.length )
			throw new IOException();
		
		for(int i=0; i<nwLat.length; i++){
			if(nwLat[i]<-90.0 || nwLat[i]>90.0 || seLat[i]<-90.0 || seLat[i]>90.0)
				throw new IOException();
		}
		
		//Initialize private fields specifying tour northwest
		//and southeast corners, as well as eye altitude.
		this.nwLat=nwLat;
		this.nwLong=nwLong;
		this.seLat=seLat;
		this.seLong=seLong;
		
		for(int i=0; i<this.nwLong.length; i++){
			//First, make all longitudinal angles between 0 and 360 degrees.
			wrap(this.nwLong[i]);
			wrap(this.seLong[i]);
			
			//Now, "unwrap" longitudinal angle once:
			if( this.nwLong[i] > this.seLong[i] )
				this.seLong[i]+=360.0;
		}
			
		
		this.alt=alt;
		this.waitTime=waitTime;
		this.filename=filename+".kml"; //Append .kml extension to end of filename.
		
	}
	
//	public Voyage(double nwLat[], double nwLong[], double seLat[], double seLong[], double alt, double waitTime, String filename) throws IOException {
//		//Make alt be a single-element array.
//		double[] temp = new double[1];
//		//temp[0]=alt;
//		
//		//Kluge to make this work for now.
//		for(int i=0; i<nwLong.length; i++)
//			temp[i]=alt;
//		
//		new Voyage( nwLat, nwLong, seLat, seLong, temp, waitTime, filename );
//				
//	}
	
	public void writeTour(){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			//Write preamble
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.newLine();
			writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\"");
			writer.newLine();
			writer.write(" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
			
			writer.newLine();
			writer.newLine();
			
			writer.write("<Document>");
			writer.newLine();
			writer.write("<name>TourEgg presents: "+filename+"</name>");
			writer.newLine();
			writer.write("<open>1</open>");
			
			writer.newLine();
			writer.newLine();
			
			//Now, start putting in view cells.
			//"Fly to" focuses desired point at center of cell.  Make views overlap slightly.
			for(int i=0; i<nwLat.length; i++){ //Loop over all given tour parameters.
				writer.write(" <gx:Tour>"); //Opening tour tag.
				writer.newLine();
				writer.write(" <name>Tour #"+(i+1)+" - Double click me to play!</name>");
				writer.newLine();

				writer.write("  <gx:Playlist>");
				writer.newLine();
//				double viewWidth = 1.15*(alt[i]-elev[i]);  //This is a width of the GE view on-screen.
				double viewWidth = 1.15*alt[i];  //This is a width of the GE view on-screen.  See comment on relativeToGround in printTourItem.
				double viewHeight = (6.0*viewWidth)/7.0; //This is a height of the GE on-screen view.

				//Length traversed north-south in tour.
				double tourHeight = Math.abs(integrateHeight(nwLat[i]*Math.PI/180.0, seLat[i]*Math.PI/180.0));
				//Increment for latitude, with some overlap.
				double dLat = (nwLat[i]-seLat[i]) / (tourHeight/(viewHeight*(1.0-overlapFraction)));

				//Go from North to South, so latitude is decreasing.  Latitude is in degrees.
				for( double lat=nwLat[i]; lat>(seLat[i]-0.5*dLat); lat-=dLat){
					//Length traversed east-west in this latitude of the tour.
					double tourWidth = Math.abs( rad(lat) *Math.sin( (90.0-lat)*Math.PI/180.0 )*(seLong[i]-nwLong[i])*Math.PI/180.0 );
					//Increment for longitude, with some overlap.
					double dLong = (seLong[i]-nwLong[i]) / (tourWidth/(viewWidth*(1.0-overlapFraction)));
					
					for( double longitude = nwLong[i]; longitude<(seLong[i]+0.5*dLong); longitude+=dLong){
						printTourItem(writer, lat, longitude, alt[i], waitTime);
					}
						
				}
				writer.write("  </gx:Playlist>");
				writer.newLine();
				writer.write(" </gx:Tour>"); //Closing tour tag.
				writer.newLine();
			}
			
			writer.write("</Document>"); //Closing document tag.
			writer.newLine();
			writer.write("</kml>"); //Closing kml tag.
			writer.newLine();
			writer.close(); //Close the writer when done.
			System.out.println("Wrote to "+filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Approximate the earth as an oblate spheroid with
	//major axis a=6378.137 km and b=6356.752 km, providing an aspect ratio
	//of 0.99664717, a flattening of 0.003352859934, and inverse 
	//flattening of 298.2572
	//See http://en.wikipedia.org/wiki/Oblate_spheroid
	//At a given latitude, phi_1, the distance between two lines of
	//longitude,theta_1 and theta_2, should be
	//(theta_2 - theta_1)*sin(phi)*(a-(a-b)*|phi|/(pi/2)),
	//where all angles are in RADIANS.
	//Likewise, the distance between two lines of latitude, phi_1
	//and phi_2, at any longitude is
	//int( r(phi) dphi, phi=phi_1,phi_2 )
	//=int( a-(a-b)*|phi|/(pi/2), phi=phi_1,phi_2 )
	//=a*(phi_2-phi_1) - (a-b)*(phi_2^2 - phi_1^2)/2)).
	//with all angles in RADIANS.
	//The error from EGM96, revised in 2004, which used a spherical
	//harmonic basis with 300 terms, is between -105 m and +85 m, which
	//should be acceptable, and may be accounted for by padding.
	//This is according to http://en.wikipedia.org/wiki/World_Geodetic_System
	
	//In Google Earth, based on taking samples of eye altitude, map
	//scale bar values, length and height on the map measured with GE,
	//as a rule of thumb, the map view is
	//length=5*0.23*alt=1.15*alt
	//height = 6*length/7

	/**
	 * @param ang angle (in degrees) to wrap to be between 0 and 360 degrees.  Subtract or add a multiple of 360 degrees to accomplish this. 
	 */
	public static void wrap(double ang){
		if( ang<0 ) { 
			ang+= Math.floor(-ang/360.0)*360.0;
		} else if(ang>360.0) {
			ang-= Math.floor(ang/360.0)*360.0;
		}
	}

	/**
	 * Heaviside step function.
	 * @param x argument to Heaviside step function.
	 * @return Returns 1 if x>0, 0 if x<0, and 0.5 if x is equal to 0.
	 */
	public static double heaviside(double x){
		if(x>0.0){
			return 1.0;
		} else if(x<0.0) {
			return 0.0;
		} else return 0.5;
	}
	
	/**
	 * 
	 * @param phi1 First latitude angle (radians)
	 * @param phi2 Second latitude angle (radians)
	 * @return height on oblate spheroid of earth integrated between two latitude angles.
	 */
	private double integrateHeight(double phi1, double phi2){
		return -a*(phi2-phi1  + (b/a-1.0)/Math.PI * ( Math.pow(phi2, 2)*(2.0*(heaviside(phi2)-1)) - Math.pow(phi1, 2)*(2.0*(heaviside(phi1)-1)) ) );
	}
	
	/**
	 * 
	 * @param phi latitude angle IN DEGREES
	 * @return radius of the earth in km at given latitude.
	 */
	private double rad(double phi){
		return a + (b-a)*(Math.abs(phi)/90.0);
	}
	
	public static void printTourItem( BufferedWriter writer, double phi, double theta, double alt, double waitTime ) throws IOException{
		//This allows formatted strings to be printed to out file, appending with inline calls to writer.write.
		Formatter myFormatter = new Formatter( writer, Locale.US );
		
		writer.write("   <gx:FlyTo>");
	      writer.newLine();
	      writer.write("    <gx:duration>1.0</gx:duration>");
	      writer.newLine();
	      writer.write("    <Camera>");
		  	writer.newLine();
		  	  //Append longitude information to output file.
		      myFormatter.format("     <longitude>%3.5f</longitude>\n", theta);
		      //Append latitude information to output file
	          myFormatter.format("     <latitude>%3.5f</latitude>\n", phi);
	          myFormatter.format("     <altitude>%5.5f</altitude>\n", alt*1000.0); //Print altitude in METERs, but alt specified in km.
	          //Assume heading and tilt are default.
	          myFormatter.format("     <heading>0.0</heading>\n");
	          myFormatter.format("     <tilt>0</tilt>\n");
	          //The view size is proportional to the eye altitude above ground.  Unless the "relative to ground" setting
	          //is used (i.e. if absolute mode is used), when the elevation is greater than (1+overlapFrac) times the altitude,
	          //the calculated view would be larger than the actual view, and the step sizes in the tour would skip over area.
	          myFormatter.format("     <altitudeMode>relativeToGround</altitudeMode>\n");
	      writer.write("    </Camera>");
	      writer.newLine();
      writer.write("   </gx:FlyTo>");
      writer.newLine();
      writer.newLine();
      
      writer.write("   <gx:Wait>");
      writer.newLine();
      	myFormatter.format("    <gx:duration>%3.1f</gx:duration>\n", waitTime);
      writer.write("   </gx:Wait>");
      writer.newLine();
      writer.newLine();
	}

}
