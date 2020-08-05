package mandelbrot.java;

import java.util.ArrayList;

/**
 * Overarching neural network class
 * @author Nir Elber
 * @version 1.1
 */
public class Network {
	/**
	 * Stores the number of neurons in the lth layer.
	 */
	int[] numNeurons;
	/**
	 * Stores the weight matrix associated with the lth layer.
	 */
	ArrayList<Matrix> ws = new ArrayList<Matrix>();
	/**
	 * Stores the bias vector associated with the lth layer.
	 */
	ArrayList<Matrix> bs = new ArrayList<Matrix>();
	
	/**
	 * Current learning rate. Changes over time.
	 */
	float rate;
	
	/**
	 * Global variable for the network's loss.
	 */
	float loss = 0;
	
	/**
	 * Initiate a network with random weights and biases.
	 * @param numNeurons_ number of neurons in each layer
	 * @param rate_ base learning rate
	 */
	public Network(int[] numNeurons_, float rate_) {
		numNeurons = numNeurons_;
		for(int l = 1; l < numNeurons.length; l ++) { // Which layer we're in. Skip 0
			// I will use the notation w^l_jk where j is the output neuron in the lth layer
			// Thus, all layers but the input one have a weight matrix
			ws.add(new Matrix(numNeurons[l], numNeurons[l-1]));
			// Notice that ws.get(l).components[j][k] = w^l_jk
			ws.get(ws.size() - 1).setRandom(); // And set it with a Gaussian
			// Similarly, all layers but the input have a bias vector, one per neuron
			bs.add(new Matrix(numNeurons[l], 1));
			bs.get(bs.size() - 1).setRandom(); // Again, set with a Gaussian
		}
		
		rate = rate_;
	}
	
	/**
	 * Computes the forward propagation given inputs
	 * @param inputs vector containing inputs
	 * @return output vector after going through the network.
	 */
	public Matrix forwprop (Matrix inputs) {
		Matrix r = new Matrix(inputs.rows, inputs.cols); // Make corresponding 0 matrix
		// We run through the first layer to actually set r
		r = activate(inputs.mult(ws.get(0), inputs).add(bs.get(0)));
		for(int i = 1; i < ws.size(); i ++) { // Run through each weight matrix (and bias)
			// Multiply by weight matrix, add bias, and activate
			r = activate(r.mult(ws.get(i), r).add(bs.get(i)));
		}
		return r;
	}
	
	/**
	 * One long function for a single trial and update using backpropagation.
	 * @param input single input
	 * @param desire single desired output
	 * @return output after forward propagation
	 */
	public Matrix backprop (Matrix input, Matrix desire) {
		// Start with a forward pass where we store weighted outputs (z) and activations (a)
		ArrayList<Matrix> z = new ArrayList<Matrix>(); // Stores weighted outputs
		ArrayList<Matrix> a = new ArrayList<Matrix>(); // Stores activations
		// Begin with "initialization" (z = wa + b, and a = activate(z) = r)
		z.add(input);
		a.add(input); // Inputs don't get activated
		// Now run through the remaining weight matrices
		for(int i = 0; i < ws.size(); i ++) {
			// Recall z = wa + b and a = activate(z)
			z.add(input.mult(ws.get(i), a.get(i)).add(bs.get(i)));
			a.add(activate(z.get(i+1))); // z is shifted :/
		} // Notice "a" has the same number of terms as "z"
		
		// NOW, we do our backward pass, because we have the needed values stored
		// Store errors in an ArrayList
		ArrayList<Matrix> deltas = new ArrayList<Matrix>();
		// Compute error for last layer directly: delta^L = grad_a of C * act'(z)
		Matrix delta = loss_ddx(a.get(a.size() - 1), desire);
		delta = input.elementProd(delta, activate_ddx(z.get(z.size()-1)));
		deltas.add(delta);
		// Compute errors for each successive layer backwards (l is the layer number)
		for(int l = numNeurons.length - 2; l >= 1; l --) { // Notice l = 0 isn't there
			delta = input.multTrans(ws.get(l), delta);
			delta = input.elementProd(delta, activate_ddx(z.get(l)));
			deltas.add(0, delta);
		}
		// Because there is no nablaB/W for l=0, nablaB/W[0] corresponds with b/ws[0]
		
		// Adjust weights
		// The gradient is given by: dC/d(w^l_jk) = a^l_k * delta^(l+1)_j
		// This is where the weight goes from layer l neuron k to l+1 neuron j
		for(int l = 0; l < ws.size(); l ++) {
			for(int r = 0; r < ws.get(l).rows; r ++) {
				for(int c = 0; c < ws.get(l).cols; c ++) { // Choose a weight
					ws.get(l).components[r][c] -= rate * 
							a.get(l).components[c][0] * deltas.get(l).components[r][0];
					// deltas.get(l) is used because that corresponds with layer l+1
				}
			}
		}
		
		// Adjust biases
		// The gradient is given by: dC/d(b^l) = delta^l
		for(int l = 0; l < bs.size(); l ++) {
			for(int r = 0; r < bs.get(l).components.length; r ++) {
				bs.get(l).components[r][0] = bs.get(l).components[r][0] 
						- rate * deltas.get(l).components[r][0]; // Adjust
			}
		}
		
		loss =  loss(a.get(a.size() - 1), desire);
		
		return z.get(z.size() - 1);
	}
	
	/**
	 * One longer function for a single epoch and update using backpropagation.
	 * @param inputs matrix of given inputs
	 * @param desires matrix of desired outputs
	 * @param iterations number of iterations in the epoch
	 */
	public void backprop (ArrayList<Matrix> inputs, ArrayList<Matrix> desires, int iterations) {
		// Declare an ArrayList for each of weights and biases
		ArrayList<Matrix> deltaW = new ArrayList<Matrix>();
		ArrayList<Matrix> deltaB = new ArrayList<Matrix>();
		
		for(int l = 0; l < ws.size(); l ++) {
			// We set each entry of deltaW to the structure of ws.get(l), but entirely 0s
			deltaW.add(ws.get(l).sub(ws.get(l)));
		}
		for(int l = 0; l < bs.size(); l ++) {
			// Similar
			deltaB.add(bs.get(l).sub(bs.get(l)));
		}
		
		// And now we actually run the iterations
		for(int k = 0; k < iterations; k ++) { // k is a useless dummy
			// Choose a random input from the given inputs
			int entry = (int)Math.floor(inputs.size()*Math.random());
			Matrix input = inputs.get(entry);
			// Start with a forward pass where we store weighted outputs (z) and activations (a)
			ArrayList<Matrix> z = new ArrayList<Matrix>(); // Stores weighted outputs
			ArrayList<Matrix> a = new ArrayList<Matrix>(); // Stores activations
			// Begin with "initialization" (z = wa + b, and a = activate(z) = r)
			z.add(input);
			a.add(input); // Inputs don't get activated
			// Now run through the remaining weight matrices
			for(int i = 0; i < ws.size(); i ++) {
				// Recall z = wa + b and a = activate(z)
				z.add(input.mult(ws.get(i), a.get(i)).add(bs.get(i)));
				a.add(activate(z.get(i+1))); // z is shifted :/
			} // Notice "a" has the same number of terms as "z"
			
			// NOW, we do our backward pass, because we have the needed values stored
			// Store errors in an ArrayList
			ArrayList<Matrix> deltas = new ArrayList<Matrix>();
			// Compute error for last layer directly: delta^L = grad_a of C * act'(z)
			Matrix delta = loss_ddx(a.get(a.size() - 1), desires.get(entry));
			delta = input.elementProd(delta, activate_ddx(z.get(z.size()-1)));
			// We set the last layer
			// We add in the current delta with out overall delta
			deltas.add(delta);
			// Compute errors for each successive layer backwards (l is the layer number)
			for(int l = numNeurons.length - 2; l >= 1; l --) { // Notice l = 0 isn't there
				delta = input.multTrans(ws.get(l), delta);
				delta = input.elementProd(delta, activate_ddx(z.get(l)));
				// Add this new layer at the beginning
				deltas.add(0, delta);
			}
			
			for(int l = 0; l < deltas.size(); l ++) {
				for(int r = 0; r < ws.get(l).rows; r ++) {
					// The bias gradient is given by: dC/d(b^l) = delta^l
					// So biases are easy
					deltaB.get(l).components[r][0] += deltas.get(l).components[r][0];
					for(int c = 0; c < ws.get(l).cols; c ++) { // Choose a weight
						// The weight gradient is given by: dC/d(w^l_jk) = a^l_k * delta^(l+1)_j
						deltaW.get(l).components[r][c] += 
								a.get(l).components[c][0] * deltas.get(l).components[r][0];
						// deltas.get(l) is used because that corresponds with weights/biases of l
					}
				}
			}
			
			// Compute loss at the end
			this.loss += loss(a.get(a.size() - 1), desires.get(entry));
		}
		// Because there is no deltaB/W for l=0, deltaB/W[0] corresponds with b/ws[0]
		
		// Adjust weights
		// The gradient is given by: dC/d(w^l_jk) = a^l_k * delta^(l+1)_j
		// This is where the weight goes from layer l neuron k to l+1 neuron j
		for(int l = 0; l < ws.size(); l ++) {
			for(int r = 0; r < ws.get(l).rows; r ++) {
				for(int c = 0; c < ws.get(l).cols; c ++) { // Choose a weight
					ws.get(l).components[r][c] -=
							rate * deltaW.get(l).components[r][c] / iterations;
					// deltas.get(l) is used because that corresponds with layer l+1
				}
			}
		}
		
		// Adjust biases
		// The gradient is given by: dC/d(b^l) = delta^l
		for(int l = 0; l < bs.size(); l ++) {
			for(int r = 0; r < bs.get(l).components.length; r ++) {
				bs.get(l).components[r][0] -= 
						rate * deltaB.get(l).components[r][0] / iterations;
			}
		}
		
		// Adjust loss to be an average
		this.loss /= iterations;
	}
	
	/**
	 * Unfinished function to update the network after an epoch.
	 * @param nablaB bias vector gradient
	 * @param nablaW weight matrix gradient
	 */
	public void update(ArrayList<Matrix> nablaB, ArrayList<Matrix> nablaW) {
		
	}
	
	/**
	 * Computes quadratic loss.
	 * @param actual what the network thinks it is
	 * @param desired what the answer should be
	 * @return quadratic loss
	 */
	public float loss (Matrix actual, Matrix desired) {
		// I'm using quadratic loss: 1/2 * |desired - actual|^2
		return desired.sub(actual).sq_mag()/2;
	}
	
	/**
	 * Computes the derivative of the loss, with respect to a given row.
	 * @param actual what the network thinks it is
	 * @param desired what the answer should be
	 * @return the derivative of quadratic loss
	 */
	private Matrix loss_ddx (Matrix actual, Matrix desired) {
		return actual.sub(desired);
	}
	
	/**
	 * Activation function.
	 * @param V vector to be activated
	 * @return reLU
	 */
	public Matrix activate (Matrix V) {
		Matrix r = new Matrix(V.rows, 1); // We don't want to change V
		// I assume we are given a vector; ie, 1 column
		for(int rr = 0; rr < r.rows; rr ++) {
			//r.components[rr][0] = (float) Math.log(1 + Math.exp((double) V.components[rr][0]));
			if(V.components[rr][0] > 0) {
				r.components[rr][0] = V.components[rr][0];
			} else {
				r.components[rr][0] = (float) 0.01 * V.components[rr][0];
			}
		}
		return r;
	}
	
	/**
	 * Derivative of the activation function.
	 * @param V
	 * @return The derivative of the activation function.
	 */
	public Matrix activate_ddx (Matrix V) {
		Matrix r = new Matrix(V.rows, 1);
		// Again I assume we are given a vector
		for(int rr = 0; rr < r.rows; rr ++) {
			/*float sig = (float) Math.exp((double) -V.components[rr][0]);
			r.components[rr][0] = 1/(1+sig);*/
			if(V.components[rr][0] > 0) {
				r.components[rr][0] = 1;
			} else {
				r.components[rr][0] = (float) 0.01 ;
			}
		}
		return r;
	}
}