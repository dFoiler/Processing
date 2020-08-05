package penrose;

public class LSys {
	Penrose app; // Create an instance . . . 
	
	String start; // Starting position stored for re-initialization
	
	float sx, sy; // External files tell where we start
	
	float angle; // How much we rotate
	
	String instructions = ""; // List of instructions
	// Rule sets for each character
	String rM, rN, rO, rP, rF;
	
	public LSys (float sx_, float sy_, Penrose app_) {
		sx = sx_;
		sy = sy_;
		
		// Angles are negated because y-coords are
		angle = 36 * -(float)3.141/(float)180;
		
		// Axiom
		start = "[N]++[N]++[N]++[N]++[N]";
		instructions += start;
		
		// RULE SET:
		rM = "OF++PF----NF[-OF----MF]++";
		rN = "+OF--PF[---MF--NF]+";
		rO = "-MF++NF[+++OF++PF]-";
		rP = "--OF++++MF[+PF++++NF]--NF";
		rF = "";
		
		app = app_; // Pass in the instance
	}
	
	// Iteration function
	public String iterate(String oldI) {
		// Create a new one because I'm lazy
		String newI = "";
		for(int i = 0; i < oldI.length(); i ++) {
			char c = oldI.charAt(i); // Go through each character
			if(c == 'M') { // Apply M's rules
				newI += rM;
			} else if (c == 'N') { // Apply N's rules
				newI += rN;
			} else if (c == 'O') { // And so on
				newI += rO;
			} else if (c == 'P') {
				newI += rP;
			} else if (c == 'F') {
				newI += rF;
			} else if (c == '+' || c == '-' || c == '[' || c == ']') {
				newI += c; // Essentially "do nothing." Just carry.
			}
		}
		return newI;
	}
	
	// Function to display some part of the full triangle
	// (If we string the partial ones fast enough, it looks like it's animating!)
	public void animate(float len, int steps) {
		if(steps > instructions.length() - 1) { // Give the complete diagram
			steps = instructions.length() - 1; // (Avoids null-pointer)
		}
		int pushes = 0;
		app.translate(sx, sy); // We move x and y to 0
		for(int i = 0; i < steps; i ++) {
			char c = instructions.charAt(i); // Run through each character . . .
			if(c == 'F') { // Basic application of each character
				app.line(0, 0, len, 0);
				app.translate(len,0);
			} else if (c == '+') {
				app.rotate(angle);
			} else if (c == '-') {
				app.rotate(-angle);
			} else if (c == '[') { // "Push" and "pop" the state
				app.pushMatrix();
				pushes ++;
			} else if (c == ']'){
				app.popMatrix();
				pushes --;
			}
		}
		// While doing some of the string, we may have more [s than ]s.
		while(pushes > 0) { // So we as much as we need to necessary.
			app.popMatrix();
			pushes --;
		}
	} // End animate
} // End class