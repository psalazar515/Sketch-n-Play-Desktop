package ksketch;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;*/

@SuppressWarnings("serial")
public class GraphicalView extends JPanel{
	
	public GraphicalView() {
        super(true);//set Double buffering for JPanel
    }
	private static Stroke model;
	
	/********************* GLOBAL VARIABLES ********************/
	//List of all the strokes made
	private static ArrayList<Stroke> strokes = new ArrayList<Stroke>();
	//Current mode: draw, select, erase
	private static String mode = "draw";
	//top toolbar buttons
	private JButton drawButton = new JButton("Draw");
	private JButton eraseButton = new JButton("Erase");
	private JButton selectButton = new JButton("Select");
	private JButton saveButton = new JButton("Save");
	private JButton loadButton = new JButton("Load");
	private JButton saveXML = new JButton("Save XML");
	
	//Colour toolbar buttons
	private JButton blackButton = new JButton("    ");
	private JButton redButton = new JButton("    ");
	private JButton greenButton = new JButton("    ");
	private JButton yellowButton = new JButton("    ");
	private JButton orangeButton = new JButton("    ");
	private JButton blueButton = new JButton("    ");
	//Bottom toolbar stuff
	private JButton playButton = new JButton("Play");
	private JButton stopButton = new JButton("Stop");
	private JButton tostartButton = new JButton("I<");
	private JButton rewindButton = new JButton("<<");
	private JButton forwardButton = new JButton(">>");
	private JButton toendButton = new JButton(">I");
	private JSlider timeSlider = new JSlider(0, 100, 0);
	private JButton addTime = new JButton("+");
	private JButton restartButton = new JButton("RESTART");
	
	//Select / animating Mode stuff
	private Stroke selectStroke;//The stroke that selects strokes to be animated
	private Polygon selectPolygon;//polygon created with selectStroke's points
	private boolean animating = false;//if a stroke is being animated
//	private boolean playing = false;
	private double laststamp = 0;
	private Point2D referencePoint = new Point2D.Double();

	//Current color for drawing
	private Color color = Color.BLACK;//default
	//frames per second
	private double fps = 40;
	//Change listener for the slider
	private class timeController implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			time = timeSlider.getValue();
			repaint();
		}
	}
	//Timer stuff
	private Timer timer;
	private Timer playtimer;
	private double endTime = 0;
	private double time = 0;//current time
	

	/********************* GRAPHICAL VIEW ******************/
	public GraphicalView(Stroke kSketch) {
		super(new BorderLayout());
		
		//Create top toolbar
		JToolBar topTB = new JToolBar("topTB");
		topTB.add(drawButton);
		topTB.add(eraseButton);
		topTB.add(selectButton);

		topTB.add(saveButton);
		topTB.add(loadButton);
		topTB.add(saveXML);
		topTB.setBackground(Color.BLACK);
		topTB.setFloatable(false);
		
		topTB.add(Box.createHorizontalGlue());
		topTB.add(blackButton);
		topTB.add(redButton);
		topTB.add(yellowButton);
		topTB.add(blueButton);
		topTB.add(greenButton);
		topTB.add(orangeButton);
		
		//Create bottom toolbar
		JToolBar bottomTB = new JToolBar("bottomTB");
		bottomTB.add(playButton);
		bottomTB.add(stopButton);
		bottomTB.add(tostartButton);
		//bottomTB.add(rewindButton);
		//bottomTB.add(forwardButton);
		bottomTB.add(toendButton);
		bottomTB.add(addTime);
		bottomTB.add(timeSlider);
		bottomTB.add(restartButton);
		bottomTB.setFloatable(false);
		

		//Button Properties
		drawButton.setFocusPainted(false);
		eraseButton.setFocusPainted(false);
		selectButton.setFocusPainted(false);
		
		blackButton.setBackground(Color.BLACK);
		blackButton.setFocusPainted(false);
		redButton.setBackground(Color.RED);
		redButton.setFocusPainted(false);
		yellowButton.setBackground(Color.YELLOW);
		yellowButton.setFocusPainted(false);
		blueButton.setBackground(Color.BLUE);
		blueButton.setFocusPainted(false);
		greenButton.setBackground(Color.GREEN);
		greenButton.setFocusPainted(false);
		orangeButton.setBackground(Color.ORANGE);
		orangeButton.setFocusPainted(false);
		
		//JSlider properties
		timeSlider.setSnapToTicks(false);
		
		//Layout
		GraphicalView.model = kSketch;
		this.setPreferredSize(new Dimension(800, 480));
		this.setBackground(Color.WHITE);
		add(topTB, BorderLayout.PAGE_START);
		add(bottomTB, BorderLayout.SOUTH);
		
		//Timer action listeners
		  ActionListener recordingAnimation = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		    	  time = time + 1/fps;//40 fps
		    	  if (time > endTime) endTime = time;
		   //       repaint();
		      }
		  };
		  ActionListener playingAnimation = new ActionListener(){
			  public void actionPerformed(ActionEvent evt){
				  if (time < endTime){
					  time = time + 1/fps;
					  repaint();
				  }
				  else{
				//	  playing = false;
					  if (playtimer.isRunning()) playtimer.stop();
				  }
			  }
		  };
		  this.timer = new Timer((int) (1000/fps), recordingAnimation);
		  this.playtimer = new Timer((int) (1000/fps), playingAnimation);

		this.registerControllers();
		GraphicalView.model.addView(new IView() {//Model changed. Repaint sketch
			public void updateView() {
				repaint();
			}
		});
	}
	//Mouse Listeners
	private void registerControllers() {
		MouseInputListener mil = new MController();
		this.addMouseListener(mil);
		this.addMouseMotionListener(mil);
		
		//Register Buttons
		this.drawButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mode = "draw";
				repaint();
			}
		});
		this.eraseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mode = "erase";
				repaint();
			}
		});
		//Load and save Button
		this.selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mode = "select";
				repaint();
			}
		});
		this.saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try{
	                createSerialisable();
	            } catch (IOException exc){
	            	System.out.println("Something wrong with saving");
	            }
				JOptionPane.showMessageDialog(null, "Your animation has been saved!");
				repaint();
			}
		});
		this.loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try{
					Deserialize();
	            } catch (IOException exc){
	            	System.out.println("Something wrong with saving");
	            }
				JOptionPane.showMessageDialog(null, "Load complete!");
				repaint();
			}
		});
		this.saveXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
					saveFile();
					repaint();
			}
		});
		//Handle mouse clicks on color buttons
		this.blackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.BLACK;
				repaint();
			}
		});
		this.redButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.RED;
				repaint();
			}
		});
		this.yellowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.YELLOW;
				repaint();
			}
		});
		this.blueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.BLUE;
				repaint();
			}
		});
		this.greenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.GREEN;
				repaint();
			}
		});
		this.orangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				color = Color.ORANGE;
				repaint();
			}
		});
		this.playButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				playAnimation();
			}
		});
		this.stopButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				if (playtimer.isRunning()) playtimer.stop();
				repaint();
			}
		});
		this.tostartButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				time = 0;
				repaint();
			}
		});
		this.rewindButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				if (time - 1 >= 0) time --;
				repaint();
			}
		});
		this.forwardButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				if (time + 1 <= endTime)  time++;
				repaint();
			}
		});
		this.toendButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				time = endTime;
				repaint();
			}
		});
		this.addTime.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				time += 1;
				if (endTime < time) endTime = time;
				repaint();
			}
		});
		this.restartButton.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent evt){
				//clear all the stroke's fields.
				for (int i = 0; i < strokes.size(); i++){
					strokes.get(i).deleteStroke();
				}
				strokes.clear();
				time = 0;
				endTime = 0;
				animating = false;//if a stroke is being animated
				laststamp = 0;
				mode = "draw";
				repaint();
			}
		});
		//Slider listener
		this.timeSlider.addChangeListener(new timeController());
	}
	
	//Changes button color depending on what is selected
	private void updateButtons(){
			if(mode == "draw"){
				drawButton.setBackground(Color.DARK_GRAY);
				drawButton.setForeground(Color.WHITE);
			} else{
				drawButton.setBackground(Color.LIGHT_GRAY);
				drawButton.setForeground(Color.BLACK);
			}
			if(mode == "erase"){
				eraseButton.setBackground(Color.DARK_GRAY);
				eraseButton.setForeground(Color.WHITE);
			} else{
				eraseButton.setBackground(Color.LIGHT_GRAY);
				eraseButton.setForeground(Color.BLACK);
			}
			if(mode == "select"){
				selectButton.setBackground(Color.DARK_GRAY);
				selectButton.setForeground(Color.WHITE);
			}
			else{
				selectButton.setBackground(Color.LIGHT_GRAY);
				selectButton.setForeground(Color.BLACK);
			}
	}
	
	
	/*********************** PAINTING METHODS ********************/
	//Paint the component
	public void paintComponent(Graphics g) {

		super.paintComponent(g); 
	    Graphics2D g2 = (Graphics2D) g;

	    //Set  anti-alias
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (timeSlider.getMaximum() <= time) timeSlider.setMaximum(timeSlider.getMaximum() + 1);//increase size automatically
		timeSlider.setValue((int)time);

		
		//Paint all the strokes
		g2.setStroke(new BasicStroke(5));
		paintStrokes(g2);
		
		updateButtons();
		//Draw the select stroke
		if (mode == "select" && selectStroke != null){
			float dash[] = { 10.0f };
			g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
		    selectStroke.drawStroke(g2, time);
		    
		    if (selectPolygon != null) {//draw bounding boxes for selected strokes
		    	g2.setColor(Color.PINK);
		    	drawSelected(g2);
		    }
		    g2.setStroke(new BasicStroke(5));
		}
	}
	//Paint all strokes
	public void paintStrokes(Graphics2D g){
		//make sure there are strokes to draw
		if (strokes.size() > 0){
			//draw every stroke in the list
			for (int i = 0; i < strokes.size(); i++){
				strokes.get(i).drawStroke(g, time);
			}
		}
	}
	/**************************** ETC METHODS ****************************/
	//Creates the selectPolygon out of the selectStroke's points
	public void createSelectPolygon(){
		if (selectStroke == null) return;//don't do anything 
		int size = selectStroke.points.size();
		int [] xpoints = new int [size], ypoints = new int [size];
		for (int i = 0; i < size; i++){
			xpoints[i] = selectStroke.points.get(i).x;
			ypoints[i] = selectStroke.points.get(i).y;
		}
		selectPolygon = new Polygon(xpoints, ypoints, size);
	}
	
	//Sets all the strokes' selected fields if they have been selected by the selectPolygon
	public void checkSelected(){
		if (selectPolygon != null){
			for (int i = 0; i < strokes.size(); i++){
				strokes.get(i).checkStrokeSelected(selectPolygon, time);
			}
		}
	}
	//Draws all the selected strokes' bounding boxes
	public void drawSelected(Graphics2D g){
		for (int i = 0; i < strokes.size(); i ++){
			if (strokes.get(i) != null && strokes.get(i).selected) strokes.get(i).drawBox(g, time);
		}
	}
	
	//Plays animation
	public void playAnimation(){
		playtimer.start();
	}
	/**************************** Serealize Methods *************************/
	//Saves strokes to a file via serializing
	public void createSerialisable() throws IOException{
		//create a file to save the sketch
        FileOutputStream fileOut =  new FileOutputStream("savedSketch.txt");
        ObjectOutputStream out1 =  new ObjectOutputStream(fileOut);
        out1.writeObject(strokes);
        out1.close();
        
        //create a file to save the endTime
        FileWriter fileOut2 = new FileWriter("endTime.txt");
        BufferedWriter out = new BufferedWriter(fileOut2);
        out.write(Double.toString(endTime));
        out.close();
    }
	@SuppressWarnings("unchecked")
	public void Deserialize()  throws IOException{  
		
		try {  
			//Clear the current strokes and load them
           FileInputStream fin = new FileInputStream("savedSketch.txt");  
           ObjectInputStream ois = new ObjectInputStream(fin);  
           
           //clear everything
           for (int i = 0; i < strokes.size(); i++){
				strokes.get(i).deleteStroke();
			}
			strokes.clear();
			time = 0;
			endTime = 0;
			animating = false;
			laststamp = 0;
			mode = "draw";
			
			//load the new strokes
           strokes = (ArrayList<Stroke>) ois.readObject(); 
           ois.close();  

           //Load the endTime for the animation
          BufferedReader in = new BufferedReader(new FileReader("endTime.txt"));
          String temp = in.readLine();
          in.close();
          endTime = Double.parseDouble(temp);
      

       }catch(Exception ex){  
           ex.printStackTrace();  
       }   
    } 
	
	/***************************** XML METHODS *****************************/
	public void toXML(File file){
		 try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element animation_x = doc.createElement("Animation");
			animation_x.setAttribute("endTime", (Integer.toString((int) endTime)));
			doc.appendChild(animation_x);
			
			Element strokes_x = doc.createElement("Strokes");
			animation_x.appendChild(strokes_x);
			
			/***STROKES***/
			for (int i = 0; i < strokes.size(); i++){
				//Stroke's attributes
				Element stroke_x = doc.createElement("Stroke");
				strokes_x.appendChild(stroke_x);
				
				//Colour
				Attr colour_x = doc.createAttribute("Colour");
				colour_x.setValue(strokes.get(i).getColour());
				stroke_x.setAttributeNode(colour_x);
				//Create Time
				Attr create_x = doc.createAttribute("CreateTime");
				create_x.setValue(Double.toString(strokes.get(i).createtime));
				stroke_x.setAttributeNode(create_x);
				//Delete Time
				Attr delete_x = doc.createAttribute("DeleteTime");
				delete_x.setValue(Double.toString(strokes.get(i).deletetime));
				stroke_x.setAttributeNode(delete_x);
				
				/***POINTS***/
				Element points_x = doc.createElement("Points");
				stroke_x.appendChild(points_x);
				for (int j = 0; j < strokes.get(i).points.size(); j++){
					Element point_x = doc.createElement("Point");
					points_x.appendChild(point_x);
					//x point
					Attr x_x = doc.createAttribute("x");
					x_x.setValue(Double.toString(strokes.get(i).points.get(j).getX()));
					point_x.setAttributeNode(x_x);
					//y point
					Attr y_x = doc.createAttribute("y");
					y_x.setValue(Double.toString(strokes.get(i).points.get(j).getY()));
					point_x.setAttributeNode(y_x);
				}
				/***TIMESTAMPS***/
				Element stamps_x = doc.createElement("TimeStamps");
				stroke_x.appendChild(stamps_x);
				for (int j = 0; j < strokes.get(i).timeStamp.size(); j++){
					Element stamp_x = doc.createElement("Stamp");
					stamps_x.appendChild(stamp_x);
					//STAMP - TIME
					Attr s_time_x = doc.createAttribute("time");
					s_time_x.setValue(Double.toString(strokes.get(i).timeStamp.get(j)[0]));
					stamp_x.setAttributeNode(s_time_x);
					//STAMP - X
					Attr s_x_x = doc.createAttribute("x");
					s_x_x.setValue(Double.toString(strokes.get(i).timeStamp.get(j)[1]));
					stamp_x.setAttributeNode(s_x_x);
					//STAMP - Y
					Attr s_y_x = doc.createAttribute("y");
					s_y_x.setValue(Double.toString(strokes.get(i).timeStamp.get(j)[2]));
					stamp_x.setAttributeNode(s_y_x);
				}
				
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//StreamResult result = new StreamResult(new File("file.xml"));
			StreamResult result = new StreamResult(file);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
			 
			System.out.println("File saved!");
		 }catch (ParserConfigurationException pce) {
				pce.printStackTrace();
		 } catch (TransformerException tfe) {
			tfe.printStackTrace();
		 }
	}
	
	public void saveFile(){
		//TODO
		final JFileChooser fc = new JFileChooser();
		
		fc.setCurrentDirectory(new File("animations"));
		int returnVal = fc.showSaveDialog(this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //Set the extension
            String filePath = file.getPath();
            if(!filePath.toLowerCase().endsWith(".xml"))
            {
                file = new File(filePath + ".xml");
            }
            System.out.println("File: " + file.getName() + ".");   
            toXML(file);
			JOptionPane.showMessageDialog(null, "Saved to XML!");
        } else {
            System.out.println("Open command cancelled by user.");
        } 
	}
	
	/**************************** Mouse Handling ***************************/
	//MController
		private class MController extends MouseInputAdapter {

			//Handles Mouse Press
			public void mousePressed(MouseEvent e) {
				
				//Draw Mode
				if (mode == "draw"){
					//Create a new stroke object and adds it to the list
					Stroke s = new Stroke(time);
					strokes.add(s);
					//Adds current point to the stroke
					strokes.get(strokes.size() - 1).points.add(e.getPoint());
					//Set the color of the stroke
					strokes.get(strokes.size() - 1).setColor(color);
				}
				//Erase Mode
				else if (mode == "erase"){
					for (int i = 0; i < strokes.size(); i++){
						if (strokes.get(i).hit(e.getPoint(), time)){
							strokes.get(i).setDeleteTime(time);
						}
					}
				}
				//Select Mode
				else if (mode == "select"){
					//check if it clicked in a selected stroke
					for (int i = 0; i < strokes.size(); i++){
						if (strokes.get(i).boundBox != null && strokes.get(i).boundBox.contains(e.getPoint()) 	&& strokes.get(i).selected){
							referencePoint.setLocation(e.getPoint());
							animating = true;
							break;
						}
						animating = false;
					}
					
					if (animating){//a selected stroke has been clicked
						for (int i = 0; i < strokes.size(); i++){//record first clicked point in Stamps vector
							if (strokes.get(i).selected){
								strokes.get(i).calculateOffset(time, e.getPoint(), referencePoint);
							}
						}
					}
					else{//no selected stroke has been clicked
						selectStroke = new Stroke(0);
						selectStroke.setColor(Color.CYAN);
					}
				}
				repaint();
			}

			//Handles Mouse Drag
			public void mouseDragged(MouseEvent e) {
				//add points to the stroke as the mouse is dragged
				if (mode == "draw" && strokes.get(strokes.size() -1) != null){
					strokes.get(strokes.size() - 1).points.add(e.getPoint());	
				}
				//Handle erase mode
				else if (mode == "erase"){
					for (int i = 0; i < strokes.size(); i++){
						if (strokes.get(i).hit(e.getPoint(), time)){
							strokes.get(i).setDeleteTime(time);
						}
					}
				}
				//Select Mode
				else if (mode == "select"){
					if (animating && System.currentTimeMillis() - laststamp >= 0.025){
						if (!timer.isRunning()) timer.start();
						for (int i = 0; i < strokes.size(); i++){//record timestamps in selected strokes
							if (strokes.get(i).selected){
								strokes.get(i).calculateOffset(time, e.getPoint(), referencePoint);
							}
						}
						referencePoint.setLocation(e.getPoint());
						laststamp = System.currentTimeMillis();
					}
					else if (!animating) selectStroke.points.add(e.getPoint());
				}
				repaint(); 
			}
			
			//Handles Mouse Release
			public void mouseReleased(MouseEvent e){
				if (mode == "select" && selectStroke != null){
					createSelectPolygon();
					checkSelected();
					selectStroke.points.clear();
					if (timer.isRunning()) timer.stop();
				}
				if (animating) animating = false;
				repaint();
			}
		}
		/****************** END OF MOUSE HANDLING ******************/
}
