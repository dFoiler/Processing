package penrose;

import processing.core.PApplet;

public class Penrose extends PApplet {
	LSys system; // Declare an object of the system
	
	int iterations = 4; // Number of iterations (current)
	
	int speed = 12; // How many steps we do per frame
	
	boolean up = false; // Describes the state of the mouse last frame
	
	public static void main (String[] args) {
		PApplet.main(new String[] {"--present", Penrose.class.getName() });
	}
	
	public void settings () {
		size(1800,1800);
	}
	
	public void setup () {
		stroke(255, 45);
		system = new LSys(width/2, height/2, this);
		for(int i = 0; i < iterations; i ++) {
			system.instructions = system.iterate(system.instructions);
		}
		frameRate(pow(2,16));
	}
	
	public void draw () {
		background(0);
		system.animate(100, speed * frameCount);
	}
}
