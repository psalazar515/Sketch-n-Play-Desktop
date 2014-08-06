package ksketch;

import java.util.ArrayList;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

//import javax.swing.*;
import java.awt.*;

import javax.swing.Spring;


@SuppressWarnings("serial")
public class Stroke extends Object  implements java.io.Serializable{
	/* A list of the model's views. */
	private ArrayList<IView> views = new ArrayList<IView>();
	
	//The points that the stroke consists of
	public ArrayList<Point> points;
	//Whether it has been selected or not
	public boolean selected;
	//Color of the stroke
	private Color color;
	//The bounding box of the stroke
	public Rectangle boundBox;
	//Time stamp array list
	public ArrayList<double[]> timeStamp;
	//When the stroke was created and deleted
	public double createtime;
	public double deletetime;
	//Stroke's offset on a given time
	public double xoffset, yoffset;
	
	//Constructor
	public Stroke(double time){	
		this.points = new ArrayList<Point>();
		this.timeStamp = new ArrayList<double[]>(); 
		this.selected = false;
		this.color = Color.BLACK;
		this.createtime = time;
		this.deletetime = -1;
	}
	
	/************** DRAWING STROKE *************/
		public void drawStroke(Graphics2D g, double time){
			if (deletetime != -1 && time >= deletetime || time < createtime) return;
			if (this.points.size() > 1){
				
				setOffset(time);
				
				//Connect and Draw the points
				for (int j = 0; j < this.points.size() - 2; j++){
					Point p1 =this.points.get(j);
					Point p2 = this.points.get(j + 1);
					g.setPaint(this.color);
					g.drawLine(p1.x + (int)this.xoffset , p1.y + (int)this.yoffset, p2.x + (int)this.xoffset, p2.y + (int)this.yoffset);
				}
			}
		}

		/*********** ETC METHODS *********/
		public void addStamp(double [] s){
			//check if this is a new stamp
			double latestAnimation = 0;
			
			if (this.timeStamp.size() > 0) latestAnimation = this.timeStamp.get(this.timeStamp.size() - 1)[0];

			//if no animation needs to be overwritten
			if (s[0] >= latestAnimation) this.timeStamp.add(s);
			
			//animation needs to be overwritten
			else{
				ArrayList<double []> temp = new ArrayList<double[]>(); 
				int i = 0;
				while (this.timeStamp.get(i)[0] <= s[0]){
					temp.add(this.timeStamp.get(i));//add points to the temporary array
					i++;
				}
				this.timeStamp.clear();
				this.timeStamp = temp;
			}
		}
		
		public void calculateOffset(double time, Point2D p1, Point2D p2){
			//If stroke has not been animated before, simply add the time stamp
			if (this.timeStamp.size() == 0){			
				double [] s = {time, p1.getX() - p2.getX(), 	p1.getY() - p2.getY() };
				this.addStamp(s);
			}
			else{//stroke has been animated before, calculate offset relative to last stamp, and add it
				double lastx, lasty;
				lastx = this.timeStamp.get(this.timeStamp.size() - 1)[1];
				lasty = this.timeStamp.get(this.timeStamp.size() - 1)[2];
				
				
				//offsets between the two points (xo = xoffset, yo = yoffset)
				double xo = p1.getX() - p2.getX();
				double yo = p1.getY() - p2.getY();
				lastx += xo;
				lasty += yo;
				
				
				//create a new time stamp
				//System.out.print(xo + " " + yo + "\n");
				double [] s = {time, lastx, lasty};
				this.addStamp(s);
			}
		}
		
		//Draws the stroke's bounding box
		public void drawBox(Graphics2D g, double time){
			setOffset(time);
			Point2D topLeft;
			double tempX = this.points.get(0).x, tempY = this.points.get(0).y;
			//get the top most left coordinates
			for (int i = 1; i < this.points.size(); i ++){
				if (this.points.get(i).x < tempX) tempX = this.points.get(i).x; 
				if (this.points.get(i).y < tempY) tempY = this.points.get(i).y; 
			}
			topLeft = new Point2D.Double(tempX + this.xoffset, tempY + this.yoffset);
			//get the bottom most right coordinates
			for (int i = 0; i < this.points.size(); i ++){
				if (this.points.get(i).x > tempX) tempX = this.points.get(i).x; 
				if (this.points.get(i).y > tempY) tempY = this.points.get(i).y; 
			}
			//draws the bounding rectangle
			boundBox = new Rectangle((int)topLeft.getX(), (int)topLeft.getY(), 
																	(int)(tempX - topLeft.getX() + this.xoffset), (int)(tempY - topLeft.getY() + this.yoffset));
			g.draw(boundBox);
		}
	
		//Returns true if the point lies anywhere on the stroke
		public boolean hit(Point2D point, double time){
			if (time >= this.deletetime && this.deletetime != -1) return false;//stroke not drawn at this point

			setOffset(time);

			if (this.points.size() > 1){
				for (int j = 0; j < this.points.size() - 2; j++){
					//check if point is anywhere near the stroke
					Point2D p1 = new Point2D.Double(this.points.get(j).getX() + this.xoffset, this.points.get(j).getY() + this.yoffset);
					Point2D p2 = new Point2D.Double(this.points.get(j + 1).getX() + this.xoffset, this.points.get(j + 1).getY() + this.yoffset);
					Line2D line = new Line2D.Double(p1, p2);
					if (line.ptSegDist(point) <= 5.0) return true;
				}
			}
			return false;
		}
		
		//Returns true if the stroke is inside select polygon
		public void checkStrokeSelected(Polygon p, double time){
			
				setOffset(time);
			
				for (int j = 0; j < this.points.size(); j++){
					//if one of the points is not contained by the polygon or if stroke has been deleted at that point in time, check next stroke
					if (!(p.contains((int)this.points.get(j).getX() + this.xoffset,  (int)this.points.get(j).getY() + this.yoffset)) || 
							(time >= this.deletetime && this.deletetime != -1)){
						this.setSelected(false);
						break;
					}
					//all points are contained by the polygon, set selected field to true
					else this.setSelected(true);
				}
		}
		//set the stroke's offset
		private void setOffset(double time){
			for(double[] el : this.timeStamp){
				if (el[0] == time){//found timeStamp to be used
					this.xoffset = el[1];
					this.yoffset = el[2];
					break;
				}
			}
		}
		//clear strokes fields
		public void deleteStroke(){
			this.points.clear();
			this.timeStamp.clear();
		}
		
		/************* SET FIELD METHODS ************/
		//set the stroke's colour
		public void setColor(Color c){
			this.color = c;
		}
		//set the stoke's selected field
		public void setSelected(boolean s){
			this.selected = s;
		}
		//set the stroke's delete time
		public void setDeleteTime(double t){
			this.deletetime = t;
		}
		//see the stroke's colour
		public String getColour(){
			Color temp = this.color;
			if (temp == Color.BLACK) return "black";
			else if (temp == Color.RED) return "red";
			else if (temp == Color.BLUE) return "blue";
			else if (temp == Color.GREEN) return "green";
			else if (temp == Color.ORANGE) return "orange";
			else if (temp == Color.YELLOW) return "yellow";
			else return "dafuq";
		}
		
		//View Methods.
		/** Add a new view. */
		public void addView(IView view) {
			this.views.add(view);
			view.updateView();
		}
		/** Remove a view */
		public void removeView(IView view) {
			this.views.remove(view);
		}
		/** Update all the views that are viewing this */
		@SuppressWarnings("unused")
		private void updateAllViews() {
			for (IView view : this.views) {
				view.updateView();
			}
		}

}