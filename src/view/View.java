package view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Tour;

public class View extends JPanel implements ActionListener {

	//Text fields for tour data.
	private JTextField nwLatField;
	private JTextField nwLongField;
	private JTextField seLatField;
	private JTextField seLongField;
	
	private JTextField altField;
	
	private JTextField waitField;
	
	
	//Labels for text fields.
	private JLabel nwLatLabel;
	private JLabel nwLongLabel;
	private JLabel seLatLabel;
	private JLabel seLongLabel;
	
	private JLabel altLabel;
	
	private JLabel waitLabel;
	
	//Directions on three lines.
	private JLabel actionLabel1;
	private JLabel actionLabel2;
	private JLabel actionLabel3;
	
	private JButton saveButton;
	
    private JFileChooser fc; //For user to get a dialog when saving .kml file. 
	
	//Constructor
	public View(){
		//Set layout first.
		setLayout(new BorderLayout());
		
        //Create a file chooser
        fc = new JFileChooser();
        //Make both files and directories visible.
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		//Make text fields for input and labels to specify
		//which inputs go where.
		nwLatField = new JTextField("0.0", 9);
		nwLongField = new JTextField("0.0", 9);
		seLatField = new JTextField("0.0", 9);
		seLongField = new JTextField("0.0", 9);
		
		altField = new JTextField("0.0", 5);
		
		waitField = new JTextField("0.0", 5);
		
		//Make labels for inputs.
		nwLatLabel = new JLabel("NW lat. [deg. decimal]: ");
		nwLatLabel.setLabelFor(nwLatField);
		nwLongLabel = new JLabel("NW long. [deg. decimal]: ");
		nwLongLabel.setLabelFor(nwLongField);
		seLatLabel = new JLabel("SE lat. [deg. decimal]: ");
		seLatLabel.setLabelFor(seLatField);
		seLongLabel = new JLabel("SE long. [deg. decimal]: ");
		seLongLabel.setLabelFor(seLongField);
		
		altLabel = new JLabel("View Altitude [km]: ");
		
		waitLabel = new JLabel("Wait time [s]: ");
		
		//Lay out the text controls and the labels.
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        textControlsPane.setLayout(gridbag);
		
		//Store labels and fields in vectors and add together with convenience method.
		JLabel[] labels = {nwLatLabel, nwLongLabel, seLatLabel, seLongLabel, altLabel, waitLabel};
		JTextField[] fields = {nwLatField, nwLongField, seLatField, seLongField, altField, waitField};
		addLabelTextRows(labels, fields, gridbag, textControlsPane);
        
        //Create a label to put messages during an action event.
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb, Locale.US); //This is to get the newline in the string; having trouble finding literal.
		f.format("\n");
		actionLabel1 = new JLabel("Enter the northwest and southeast points");
		actionLabel1.setBorder(BorderFactory.createEmptyBorder(5,0,0,0)); //Put some space between fields and label.
		actionLabel2 = new JLabel("of the tour as latitudes and longitudes;");
		actionLabel3 = new JLabel("use decimal degrees.");
		actionLabel3.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		
        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        textControlsPane.add(actionLabel1, c);
        textControlsPane.add(actionLabel2, c);
        textControlsPane.add(actionLabel3, c);
        textControlsPane.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Tour inputs"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        add(textControlsPane, BorderLayout.LINE_START);
        
        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Hatch a TourEgg...", createImageIcon("images/Save16.gif"));
        saveButton.addActionListener(this);
        
        add(saveButton, BorderLayout.PAGE_END);

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(View.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would save the file.
//                System.out.println("Got file");
                
                //Parse input.
//                double[] nwLat = parseInput(nwLatField);
//                double[] nwLong = parseInput(nwLongField);
//                double[] seLat = parseInput(seLatField);
//                double[] seLong = parseInput(seLongField);
//                double[] alt= parseInput(altField);
//                double[] waitTime = parseInput(waitField);
                
                //Here's where we create the Voyage object.
                try {
                	//Parse input and use to create Voyage object.
                	Tour myTour = new Tour(parseInput(nwLatField), parseInput(nwLongField), parseInput(seLatField), parseInput(seLongField), parseInput(altField), parseSingleInput(waitField), file.getPath());
//                	Tour myTour = new Tour(nwLat, nwLong, seLat, seLong, alt, waitTime[0], file.getPath());
//					Tour myTour = new Tour(new double[] {9.37}, new double[]{42.795}, new double[]{9.36}, new double[]{42.805}, new double[]{2.5}, new double[]{1.6}, 5, file.getPath());
//                	System.out.println(file.getPath());
                	myTour.writeTour();
//                	System.out.println("Tour written.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("TourEgg could not parse the input.  Please take a look...");
				}
            } else {
                System.out.println("Save command cancelled by user.");
            }

        }
	}
	
	private double[] parseInput(JTextField field){
		String s=field.getText();
		//Break up string on one or more occurrences of the ";" or "," or " " tokens
		String[] substrings=s.split("(;+\\s*,*)|(;*\\s+,*)|(;*\\s*,+)");

		double[] allNums = new double[substrings.length];
		
		//Bad kluge because I forgot how to have split use
		//a regex to skip also multiple delimiting tokens appearing one after the other.
//		ArrayList<Double> allNums = new ArrayList<Double>();
		
		for(int i=0; i<substrings.length; i++) {
//			System.out.println(substrings[i]);
			allNums[i]=Double.parseDouble(substrings[i]);
		}
		
		return allNums;
//			if(substrings[i].length()!=0)
//				allNums.add(Double.parseDouble(substrings[i]));
//		}
//		
//		double[] allNumsDouble=new double[allNums.size()];
//		
//		for(int i=0; i<allNums.size(); i++){
//			allNumsDouble[i]=(double)(allNums.get(i));
//		}
		

	}
	
	private double parseSingleInput(JTextField field){
		//There is only a single number in the input.
		return Double.parseDouble(field.getText());
	}
	
	private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, GridBagLayout gridbag, Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int numLabels = labels.length;
		
		for (int i = 0; i < numLabels; i++) {
			c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
			c.fill = GridBagConstraints.NONE;      //reset to default
			c.weightx = 0.0;                       //reset to default
			container.add(labels[i], c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;     //end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(textFields[i], c);
		}
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TourEgg");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new View());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = View.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
