package mandelbrot.java;

// NOTE: THIS CLASS DOES NO ERROR CHECKING
// EG, IF 2 MATRICES HAVE DIFFERENT SIZES AND YOU ADD THEM, THE COMPUTER
// WILL STILL ADD TO THE BEST OF IT'S ABILITY
/**
 * A quick and dirty linear algebra library. Does no error checking.
 * @author Nir Elber
 * @version 1.0
 */
public class Matrix {
	/**
	 * All components of the matrix, stored in a 2D array.
	 */
	float[][] components;
	/**
	 * Number of rows for easy accessing.
	 */
	int rows;
	/**
	 * Number of columns for easy accessing.
	 */
	int cols;
	
	/**
	 * Initialize a non-descriptive matrix with a given number of rows and columns.
	 * @param rows_ number of rows
	 * @param cols_ number of columns
	 */
	public Matrix (int rows_, int cols_) {
		rows = rows_;
		cols = cols_;
		components = new float[rows][cols];
	}
	
	/**
	 * Initialize a matrix with all the components already given in a 2D array.
	 * @param components_
	 */
	public Matrix (float[][] components_) {
		rows = components_.length;
		cols = components_[0].length;
		components = components_;
	}
	
	/**
	 * Sets all components of a matrix to a random value between -1 and 1.
	 */
	public void setRandom () {
		for(int i = 0; i < rows; i ++) {
			for(int j = 0; j < cols; j ++) {
				// Choose from a normal Gaussian Distribution
				components[i][j] = (float) (2 * Math.random() - 1);
			}
		}
	}
	
	/**
	 * Adds two matrices
	 * @param A addend matrix
	 * @return this + A
	 */
	public Matrix add(Matrix A) {
		if(this.rows != A.rows || this.cols != A.cols) {
			throw new java.lang.RuntimeException("Matrices have different sizes.");
		}
		Matrix r = new Matrix(this.rows, this.cols); // We have a starting point
		for(int ar = 0; ar < A.rows; ar ++) { // Rows first
			for(int ac = 0; ac < A.cols; ac ++) { // Then columns
				// Run through each element and add
				r.components[ar][ac] = this.components[ar][ac] + A.components[ar][ac];
			}
		}
		return r;
	}
	
	/**
	 * Subtracts two matrices
	 * @param A subtrahend matrix
	 * @return this - A
	 */
	public Matrix sub(Matrix A) {
		if(this.rows != A.rows || this.cols != A.cols) {
			throw new java.lang.RuntimeException("Matrices have different sizes.");
		}
		Matrix r = new Matrix(this.rows, this.cols); // We have a starting point
		for(int ar = 0; ar < A.rows; ar ++) { // Rows first
			for(int ac = 0; ac < A.cols; ac ++) { // Then columns
				// Run through each element and subtract
				r.components[ar][ac] = this.components[ar][ac] - A.components[ar][ac];
			}
		}
		return r;
	}
	
	/**
	 * Multiples two matrices, with no error checking regarding sizes.
	 * @param A first factor
	 * @param B second factor
	 * @return AB
	 */
	public Matrix mult(Matrix A, Matrix B) {
		Matrix r = new Matrix(A.rows, B.cols); // Ex: a 3x2 times a 2x3 gives a 3x3
		for(int br = 0; br < B.rows; br ++) { // Rows selected first
			for(int bc = 0; bc < B.cols; bc ++) { // Then columns
				for(int ar = 0; ar < A.rows; ar ++) {
					r.components[ar][bc] += // We take the column of B and the row of A
							B.components[br][bc] * 
							A.components[ar][br]; // B's rows correspond to A's columns
				}
			}
		}
		return r;
	}
	
	/**
	 * Multiplies the transpose of a matrix with another matrix.
	 * @param AT first factor, to be transposed
	 * @param B second factor
	 * @return A^T B
	 */
	public Matrix multTrans(Matrix AT, Matrix B) {
		Matrix r = new Matrix(AT.cols, B.cols); // Ex: a 3x2 times a 2x3 gives a 3x3
		for(int br = 0; br < B.rows; br ++) { // Rows selected first
			for(int bc = 0; bc < B.cols; bc ++) { // Then columns
				for(int ac = 0; ac < AT.cols; ac ++) {
					r.components[ac][bc] += // We take the column of B and the row of A
							B.components[br][bc] * 
							AT.components[br][ac]; // B's rows correspond to A's columns
				}
			}
		}
		return r;
	}
	
	/**
	 * Performs element-by-element multiplication of two vectors (single-column matrices).
	 * @param V1 first vector
	 * @param V2 second vector
	 * @return a vector with V_i = V1_i * V2_i
	 */
	public Matrix elementProd(Matrix V1, Matrix V2) {
		Matrix r = new Matrix(V1.rows, 1);
		for(int vr = 0; vr < V1.rows; vr ++) {
			r.components[vr][0] = V1.components[vr][0] * V2.components[vr][0];
		}
		return r;
	}
	
	/**
	 * Computes the square of the magnitude of a vector (single-column matrix).
	 * @return mag(this)^2
	 */
	public float sq_mag() {
		float r = 0;
		// I assume V has only 1 column; ie, it's a vector
		for(int vr = 0; vr < this.rows; vr ++) {
			// Add the squares down the column
			r += this.components[vr][0] * this.components[vr][0];
		}
		return r;
	}
	
	/**
	 * Prints "this" matrix, element by element, with spaces between elements in the same row.
	 */
	public void printM() {
		for(int mr = 0; mr < this.rows; mr ++) {
			String out = "";
			for(int mc = 0; mc < this.cols; mc ++) {
				out += Float.toString(this.components[mr][mc]) + " ";
			}
			System.out.println(out);
		}
	}
	
	/**
	 * Self-explanatory.
	 */
	public String toString() {
		String r = "";
		for(int mr = 0; mr < this.rows; mr ++) {
			String out = "";
			for(int mc = 0; mc < this.cols; mc ++) {
				out += Float.toString(this.components[mr][mc]) + " ";
			}
			r += out + "\n";
		}
		return r;
	}
}
