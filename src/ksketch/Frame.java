package ksketch;

/*import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.JComponent;*/
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Frame extends JFrame {
		public static void main(String[] args) {
			Stroke model = new Stroke(0);
			GraphicalView vGraphical = new GraphicalView(model);

			JFrame frame = new JFrame("Sketch n Play!");
			frame.getContentPane().add(vGraphical);

			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
 }
	 