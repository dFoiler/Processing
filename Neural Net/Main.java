package mandelbrot.java;

import processing.core.PApplet;
import java.util.ArrayList;

/**
 * Runs the main program.
 * @author Nir Elber
 * @version 1.0
 */
public class Main extends PApplet {
	/**
	 * The entire network.
	 */
	Network net;
	
	// Declare these as global
	/**
	 * Complex number to be tested.
	 */
	float[] num = new float[] {(float) random(-2,1), (float) random(-1,1)};
	/**
	 * Amount to change the complex number in next trial.
	 */
	float numDelta = random((float)-.4,(float).4);
	/**
	 * Output matrix.
	 */
	Matrix out;
	/**
	 * Array storing a set number of previous losses for the average.
	 */
	ArrayList<Float> losses = new ArrayList<Float>();
	/**
	 * Current average loss, taking into account a set number of previous losses.
	 */
	float avgloss = 0;
	/**
	 * Current size of the trials/batch.
	 */
	int batchSize = 100;
	/**
	 * I use Processing for graphics, so I've imported everything already.
	 * Don't judge me.
	 * @param args
	 */
	public static void main (String[] args) {
		PApplet.main(new String [] {"--present", Main.class.getName()} );
	}
	
	public void settings () {
		size(2400,1600);
	}
	
	public void setup () {
		// Initialize network
		net = new Network(new int[] {2,3,4,3,1}, (float) 0.005);
		
		textSize(100);
		
		frameRate(6000);
	}
	
	/**
	 *  Tests whether or not the complex number is in the mandelbrot set
	 *  @param real real part
	 *  @param imaginary imaginary part
	 */
	public int mTest(float real, float imaginary) { // The input is z = a+bi
		// Initialize cnum as z
		float[] cnum = new float[] {real, imaginary};
		for(int i = 0; i < 100; i ++) {
			// Our new cnum is cnums^2 + z
			cnum = new float[] {sq(cnum[0])-sq(cnum[1]) + real, 2*cnum[0]*cnum[1] + imaginary};
			// If the magnitude is larger than 2, the number will explode.
			if(sq(cnum[0]) + sq(cnum[1]) > 4) {
				return 0;
			}
		}
		// The number has not exploded, it is probably in the set.
		return 1;
	}
	
	// Below will draw out the set based on the above.
	/**public void draw () {
		for(float i = 0; i < width; i ++) {
			for(float j = 0; j < height; j ++) {
				// We are dealing with the complex number (3*i/width-2) - (2*j/height-2) i
				stroke(255 * mTest(3*i/width - 2, -(2*j/height - 1)));
				point(i, j);
			}
		}
		saveFrame("For reference");
	}/**/
	
	/**
	 * Long function to display the entire network.
	 * @param net_ to be displayed
	 * @param x is x displacement
	 * @param y is y displacement
	 */
	public void display(Network net_, float x, float y) {
		pushMatrix();
		translate(x, y);
		for(int l = 0; l < net_.ws.size(); l ++) { // Draw in the lines for weights and biases
			// Draw in the weights
			for(int j = 0; j < net_.ws.get(l).components.length; j++) {
				for(int k = 0; k < net_.ws.get(l).components[j].length; k ++) {
					// Recall that j is the output neuron, and k is the input neuron
					// Positive weights are green!
					if(net_.ws.get(l).components[j][k] > 0) {
						stroke(0,255,0);
						// Scale the strokeWeight depending on how large it is
						strokeWeight(10 * net_.ws.get(l).components[j][k]);
					}
					// Negative ones are red!
					else {
						stroke(255,0,0);
						strokeWeight(-10 * net_.ws.get(l).components[j][k]);
					}
					// Draw in the line for the weight
					line(l*300, k*300, (l+1)*300, j*300);
				}
			}
			// Draw in the biases
			for(int i = 0; i < net_.bs.get(l).components.length; i ++) {
				// Positive biases are green!
				if(net_.bs.get(l).components[i][0] > 0) {
					stroke(0,255,0);
					// Scale the strokeWeight depending on how large it is
					strokeWeight(10 * net_.bs.get(l).components[i][0]);
				}
				// Negative ones are red!
				else {
					stroke(255,0,0);
					strokeWeight(-10 * net_.bs.get(l).components[i][0]);
				}
				line(l*300, net_.numNeurons[l] * 300, (l+1)*300, i*300);
			}
			
			noStroke();
			// This will cover all neurons but the last layer ones
			for(int i = 0; i < net_.numNeurons[l] + 1; i ++) { // +1 for bias neuron
				if(l == 0 && i != net.numNeurons[l]) { // Bias should be white
					// First layer inputs are colored as they should be
					fill(255);
				} else {
					fill(255);
				}
				ellipse(l*300, i*300, 100, 100);
			}
		}
		
		// Last layer neurons
		for(int i = 0; i < net_.numNeurons[net_.numNeurons.length - 1]; i ++) {
			// Color as what the network thinks it should be
			fill(255);
			ellipse(net_.ws.size()*300, i*300, 100, 100);
		}
		popMatrix();
	}
	
	/**
	 * Ugliness incarnate.
	 * Does a lot of things. Anything on the screen is done here.
	 */
	public void draw () {
		background(50);
		/** /// Adjust input num slightly. Easier on the eyes.
		for(int i = 0; i < num.length; i ++) {
			// If the current loss is more than average
			if(losses.size() > 1 && losses.get(losses.size() - 1) > avgloss) {
				// Notice: We don't randomize movement much. We only make is smaller!
				// If the loss is higher than usual, we want to train at that point/direction!
				numDelta += random((float)-.05, (float).05);
				num[i] += numDelta/10; // Change a bit less than usual
			} else { // less than average
				// Randomize movement. It's pretty large
				numDelta = random((float)-.2, (float).2) - numDelta;
				num[i] += numDelta;
			}
			// Constrain each element between -2 and 2
			if(num[i] < -2) {
				num[i] = -2;
			} else if (num[i] > 2) {
				num[i] = 2;
			}
		}/**/
		ArrayList<Matrix> inputs = new ArrayList<Matrix>();
		ArrayList<Matrix> desires = new ArrayList<Matrix>();
		for(int i = 0; i < batchSize; i ++) {
			// Use a random complex number
			num = new float[] {random(-2,2), random(-2,2)};
			inputs.add(new Matrix(new float[][] {{num[0]},{num[1]}}));
			desires.add(new Matrix(new float[][] {{(float)mTest(num[0], num[1])}}));
		}
		
		net.backprop(inputs, desires, batchSize);
		
		// Append the current loss
		losses.add(net.loss);
		// For any extra current loss
		while(losses.size() > 2000) {
			// Delete from the beginning
			losses.remove(0);
		}
		// Calculate average
		for(int i = 0; i < losses.size(); i ++) {
			avgloss += losses.get(i);
		}
		avgloss /= losses.size();
		
		// Display
		display(net, width/2 - 300 * net.ws.size()/2, height/5);
		fill(255);
		text("Avg: " + str(((float)(int)(1000*avgloss))/1000), 30, 200);
		text("Loss: " + str(((float)(int)(1000*net.loss))/1000), 30, 100);
		
		// Save the frame
		if(frameCount % (60 * 60 * 60) == 0) { // Every hour or so
			saveFrame("NN-######.png");
		}
		
		// Mouse is pressed --> Display the network's set
		if(mousePressed) {
			for(float i = 0; i < width; i ++) {
				for(float j = 0; j < height; j ++) {
					// We are dealing with the complex number z = (3*i/width-2) - (2*j/height-2) i
					// I don't round to make the  gradient clearer
					stroke(255 * net.forwprop(new Matrix(new float[][] {{3*i/width - 2},{-(2*j/height - 1)}})).components[0][0]);
					point(i, j);
				}
			}
			saveFrame("OUT-######.png");
		}
	}
}
