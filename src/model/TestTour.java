package model;

import java.io.IOException;

public class TestTour {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Create a new voyage
		Tour tour;
		try {
//			tour = new Tour(new double[] {9.37}, new double[]{42.795}, new double[]{9.36}, new double[]{42.805}, new double[]{2.5}, new double[]{1.6}, 5, "TestTour2");
			tour = new Tour(new double[] {9.37}, new double[]{42.795}, new double[]{9.36}, new double[]{42.805}, new double[]{2.5}, 5, "TestTour2");
			tour.writeTour();
			System.out.println("Done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
