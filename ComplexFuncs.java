// Alright! I think I like it! Donezo!
/*** INSTRUCTIONS ***/
/*1. Click on the function to graph it in the complex plane. Inputs drag to outputs
  2. Click the graph once to pause, double-click to reset
  3. To compose functions, drag mouse over a few of them in succession.
    Eg, to graph (1/sin)^2, drag mouse over 1. sin, 2. invert, 3. x^2
  4. Click on the composition line/function to watch it reset
  5. Click and drag on the graph to zoom. Mouse wheel also works.
  6. (Hit enter/return to go back to original zoom.)
  7. Try it! (I recommend playing with the Pythagorean identities.)  */
  
// NEGATE BUTTON

float W = 1200; // Actual window width and height
float H = 1200;
float w = TAU; // The length of the window we're looking at
float h = TAU; // In reality, the domain and range is (-TAU/2, TAU/2)
float f = 15*W/600; // Occur with frequency every f pixels
float zoom = 1; // Set zoom
float mult = 1; // Multiplier for the zoom; preview for dragging
int frames = 0; // This counts frames since mouse clicked
int paused = 0; // Are we paused
int lastPause = 0; // Stores frame of last pause. (last click . . . )
int lastClick = 0; // Sotres frame of last click in the bottom box
String func = "x";
ArrayList<String> newFunc = new ArrayList<String>(); // Stores functions to compose
ArrayList<PVector> dragPts = new ArrayList<PVector>(); // Stores points while dragging
int invt = 0; // Are we inverting?
int negt = 0; // Are we negating?
float t = 5; // Amount of time to reach endpoint
PVector pos1 = new PVector(W/w,0); // We track 1 + 0i to aid in visualization. (We scale!)
PVector end1; PVector dir1;
PVector[][] pos = new PVector[int(W/f) + 1][int(H/f) + 1]; // Stores position
PVector[][] end = new PVector[int(W/f) + 1][int(H/f) + 1]; // Stores endpoint
PVector[][] dir = new PVector[int(W/f) + 1][int(H/f) + 1]; // Stores direction
float e = 2.7182818284590452353602874713526624977572470936999595749669676277240766303535;
PVector ex(float x, float y) {
  //We take e^(x+yi)=e^x * e^(yi)=e^x(cos(y) + i*sin(y))*/
  PVector r = new PVector(pow(e,x)*cos(y),pow(e,x)*sin(y));
  return r;
}
PVector function(float x, float y, String which, int invert, int negate) { // Apply a function
  PVector r = new PVector(x,y); // Initiate
  /* FUNCTION : e^z*/
  if(which == "ex") {
    r = ex(x,y);
  }
  /* FUNCTION : z^2
  We take (x+yi)^2=x^2+(yi)^2+2xyi=x^2-y^2+2xyi */
  if(which == "x2") {
    r = new PVector(pow(x,2) - pow(y,2),2*x*y);
  }
  /* FUNCTION : z^3
  We take (x+yi)^3=x^3-3xy^2+3x^2yi-y^3i */
  if(which == "x3") {
    r = new PVector(pow(x,3)-3*x*pow(y,2),3*pow(x,2)*y-pow(y,3));
  }
  // ***TRIG FUNCTIONS***
  /* FUNCTION : cos(z)
  We take cos(x+yi)=(e^[i(x+yi)]+e^[-i(x+yi)])/2.
  After math: [e^(-y+ix)+e^(y-ix)]/2*/
  if(which == "cos") {
    r = PVector.add(ex(-y,x),ex(y,-x));
    r.x /= 2;
    r.y /= 2;
  }
  /* FUNCTION : sin(z)
  We take sin(x+yi)=(e^[i(x+yi)]-e^[-i(x+yi)])/2i
  Becomes (e^(-y+ix)-e^(y-ix))/2i*/
  if(which == "sin") {
    r = PVector.sub(ex(-y,x),ex(y,-x));//Note that r = a+bi=>r/2i=b/2-ai/2
    float a = r.x;
    r.x = r.y/2;
    r.y = -a/2;
  }
  /* FUNCTION : tan(z)
  I'm getting lazy, so let's use cos and sin and then divide.
  We want (s.x+is.y)/(c.x+ic.y) * (c.x-ic.y)/(c.x-ic.y).
  This gives (s.x*c.x+s.yc.y+i(s.yc.x-s.xc.y))/(mag(c)^2)*/
  if(which == "tan") {
    PVector c = PVector.add(ex(-y,x),ex(y,-x));
    c.x /= 2;
    c.y /= 2;
    PVector s = PVector.sub(ex(-y,x),ex(y,-x));
    float a = s.x;
    s.x = s.y/2;
    s.y = -a/2;
    float magc = pow(c.x,2) + pow(c.y,2);
    r = new PVector((s.x*c.x+s.y*c.y)/magc,(s.y*c.x-s.x*c.y)/magc);
  }
  // ***HYPERBOLIC TRIG***
  /** FUNCTION : cosh(z)
  We take cosh(x)=[e^x+e^(-x)]/2*/
  if(which == "cosh") {
    r = PVector.add(ex(x,y),ex(-x,-y));
    r.x /= 2;
    r.y /= 2;
  }
  /** FUNCTION : sinh(z)
  We take sinh(x)=[e^x+e^(-x)]/2*/
  if(which == "sinh") {
    r = PVector.sub(ex(x,y),ex(-x,-y));
    r.x /= 2;
    r.y /= 2;
  }
  /** FUNCTION : tanh(z)
  We take tanh(z)=sinh(z)/cosh(z). Note (s.x+is.y)/(c.x+ic.y)*(c.x-ic.y)/(c.x-ic.y)=
  =[s.xc.x+s.yc.y+i(s.yc.x-s.xc.y)]/(c.x^2+c.y)^2+*/
  if(which == "tanh") {
    PVector sh = PVector.sub(ex(x,y),ex(-x,-y));
    sh.x /= 2;
    sh.y /= 2;
    PVector ch = PVector.add(ex(x,y),ex(-x,-y));
    ch.x /= 2;
    ch.y /= 2;
    float magc = pow(ch.x,2) + pow(ch.y,2);
    r = new PVector((sh.x*ch.x+sh.y*ch.y)/magc,(sh.y*ch.x-sh.x*ch.y)/magc);
  }
  // LINEAR TRANSFORMATIONS
  /**[a c] [x] = [ax+cy]
     [b d] [y] = [bx+dy]*/
  if(which == "line") {
    float a = 1; float b = 2; float c = -1; float d = 4;
    r = new PVector(a*x+c*y,b*x+d*y);
  }
  // INVERSION : f(x) = 1/x
  /* We take 1/(a+bi)=(a-bi)/(a^2+b^2) */
  if(which == "invert") {
    r = new PVector(x,y);
    float buf = r.x;
    r.x = r.x/(pow(r.x,2)+pow(r.y,2));
    r.y = -r.y/(pow(buf,2)+pow(r.y,2));
  }
  if(invert == 1) {
    float buf = r.x;
    r.x = r.x/(pow(r.x,2)+pow(r.y,2));
    r.y = -r.y/(pow(buf,2)+pow(r.y,2));
  }
  // NEGATION : f(z) = -z
  if(which == "negate") {
    r = new PVector(-x,-y);
  }
  if(negate == 1) {
    r.x *= -1;
    r.y *= -1;
  }
  return r;
}
void initCoords(int trans) { // Resets pos, which is something we want sometimes
  for(int i = 0; i < dir.length; i ++) {
    for(int j = 0; j < dir[i].length; j ++) {
      if(trans == 0) { // Not in "transition," don't reset
        pos[i][j] = new PVector(f*i-W/2,f*j-H/2); // This is where we start
      }
      end[i][j] = function((f*i-W/2)*w/W, (f*j-H/2)*h/H, func, invt, negt);
      // This is a pure direction vector from where we are to where we want to be
      dir[i][j] = new PVector(W/w*end[i][j].x-pos[i][j].x, W/w*end[i][j].y-pos[i][j].y);
      // This rescales so that we are going 1/(30*t) of the way
      dir[i][j] = new PVector(dir[i][j].x/(30*t),dir[i][j].y/(30*t));
    }
  }
  if(trans == 0) {
    pos1 = new PVector(W/w, 0);
  }
  end1 = function(1, 0, func, invt, negt);
  dir1 = new PVector((W/w*end1.x-pos1.x)/(30*t),(W/w*end1.y-pos1.y)/(30*t));
}
void applyFunc(String nextFunc) { // Doesn't reset pos, just does composition
  for(int i = 0; i < dir.length; i ++) {
    for(int j = 0; j < dir[i].length; j ++) {
      // Filter the old endpoint back into the new function.
      end[i][j] = function(end[i][j].x, end[i][j].y, nextFunc, 0, 0);
      // This is a pure direction vector from where we are to where we want to be
      dir[i][j] = new PVector(W/w*end[i][j].x-pos[i][j].x, W/w*end[i][j].y-pos[i][j].y);
      // This rescales so that we are going 1/(30*t) of the way
      dir[i][j] = new PVector(dir[i][j].x/(30*t),dir[i][j].y/(30*t));
    }
  }
  end1 = function(end1.x, end1.y, nextFunc, 0, 0);
  dir1 = new PVector((W/w*end1.x-pos1.x)/(30*t),(W/w*end1.y-pos1.y)/(30*t));
}
void setup() {
  frameRate(30); // This means we need 30 * t frames to reach the endpoint
  size(1200,1800);
  textAlign(CENTER,CENTER);
  initCoords(0);
}
void funcButton(String funcB, String funcE, float x, float y, int circ) { // Display a button
  pushMatrix();
  translate(x,y); // I do this translation thing . . . because it makes me happy?
  textSize(40*2/3*width/600);
  noStroke();
  int alpha = 150;
  if(circ == 1) { // These are always exponents. How convenient.
    if(func.equals(funcB + funcE)) { // If the function applied is this one, light up button
      alpha = 255;
    }
    fill(255,0,0,alpha); // So we make the button as a circle
    ellipse(0,0,35*width/600,35*width/600);
    fill(255);
    text(funcB,-4*width/600,-13/3*width/600); // Function base
    textSize(20*2/3*width/600); // This next bit is the exponent
    text(funcE,(15*2/3-4)*width/600,(-10*2/3-13/3)*width/600);
  } else {
    if(func.equals(funcB)) { // Light up button if this is the one
      alpha = 255;
    }
    if (funcE == "i") { // INVERT needs a larger ellipse and different text color
      if ((invt==1&&funcB.equals("Invert"))||(negt==1&&funcB.equals("Negate"))) { // Light it up
        fill(255,255);
      } else {
        fill(255,150);
      }
      ellipse(0,0,120*width/600,35*width/600);
      fill(0); // Text color is actually black for invert. I thought it was funny
    } else if (funcE == "") { // Trig functions
      fill(250,150,0,alpha);
      ellipse(0,0,60*width/600,35*width/600);
      fill(255);
    } else if (funcE == "h") { // Hyperbolic Trig functions
      fill(255,255,0,150+(alpha-150)*150/255);
      ellipse(0,0,75*width/600,35*width/600);
      fill(255);
    }
    textSize(40*2/3*width/600);
    if(funcB == "cos" && funcE == "") { // cos looks a bit weird under the other one
      text(funcB,-width/600,(-6)*width/600);
    } else {
      text(funcB,-width/600,(-5)*width/600); // No exponent, so only funcB
    }
  }
  popMatrix();
}
void draw() {
  pushMatrix(); // Mainly for good practice
  translate(width/2, width/2); // The graph is a square of side length width
  scale(zoom *  mult); // Use of mult is stated earlier and later. This is for zooming
  // I also have to remember to - all the y coords to restore Q1 as (+,+)
  background(0);
  for(int i = 0; i < dir.length; i ++) {
    for(int j = 0; j < dir[i].length; j ++) {
      // ANIMATE
      if(frames < 30 * t && paused == 0) { // Are we animating?
        pos[i][j].x += dir[i][j].x; // Move along the direction vector
        pos[i][j].y += dir[i][j].y;
      } else if ((pos[i][j].x!=W/w*end[i][j].x||pos[i][j].y!=W/w*end[i][j].y)&&frames>=30*t) {
        // Done animating, but not at destination
        pos[i][j].x = W/w*end[i][j].x; // Set to destination
        pos[i][j].y = W/w*end[i][j].y;
      }
      // DISPLAY (Gridlines)
      // Horizontal
      if(i > 0) { // Check for out of bounds
        if(f * j - H/2 == 0) { // On the x-axis (y=0), make it red. Otherwise no.
          stroke(0,0,255,220);
          strokeWeight(4);
        } else {
          stroke(150,150,255,180);
          strokeWeight(2);
        }
        // Shows pt and next pt up; To avoid using unupdated pt: chose a previous pt
        line(pos[i][j].x,-pos[i][j].y,pos[i-1][j].x,-pos[i-1][j].y);
      }
      // Vertical
      if(j > 0) { // Check for out of bounds
        if(f * i - W/2 == 0) { // On the y-axis (x=0), make it red. Otherwise no.
          stroke(0,255,0,220);
          strokeWeight(4);
        } else {
          stroke(150,255,150,180);
          strokeWeight(2);
        }
        // Shows pt and next pt up; To avoid using unupdated pt: chose a previous pt
        line(pos[i][j].x,-pos[i][j].y,pos[i][j-1].x,-pos[i][j-1].y);
      }
    }
  }
  // To help out with visualizatoin, I track 1+0i. Remember to rescale . . .
  strokeWeight(6*width/600);
  stroke(255,0,0);
  if(frames < 30 * t && paused == 0) { // Are we animating?
    pos1.x += dir1.x; // Move along the direction vector
    pos1.y += dir1.y;
  } else if ((pos1.x!=W/w*end1.x||pos1.y!=W/w*end1.y)&&frames>=30*t) {
    // Done animating, but not at destination
    pos1.x = W/w*end1.x; // Set to destination
    pos1.y = W/w*end1.y;
  }
  point(pos1.x,pos1.y);
  popMatrix();
  if(frames < 30 * t && paused == 0) { // Only increment if we're really animating
    frames++;
  } else if (paused == 1) { // Make a pause button!
    stroke(255);
    strokeWeight(30*width/600);
    line(3*width/8, width/3, 3*width/8, 2*width/3);
    line(5*width/8, width/3, 5*width/8, 2*width/3);
  }
  
  // BOTTOM PARTS
  stroke(255); strokeWeight(3); fill(0);
  rect(-1.5,width,width+3,width); // Forms the entire black box on the bottom
  // Wooo . . . buttons
  funcButton("e","x",50*width/600,width+(height-width)/2,1); // e^x
  funcButton("x","2",2*50*width/600,width+(height-width)/4,1); // x^2
  funcButton("x","3",2*50*width/600,width+3*(height-width)/4,1); // x^3
  funcButton("cos","",4*50*width/600,width+(height-width)/4,0); // cos
  funcButton("sin","",4*50*width/600,width+3*(height-width)/4,0); // sin
  funcButton("tan","",5*50*width/600,width+(height-width)/2,0); // tan
  funcButton("cosh","h",7*50*width/600,width+(height-width)/4,0); // cosh
  funcButton("sinh","h",7*50*width/600,width+3*(height-width)/4,0); // sinh
  funcButton("tanh","h",8.3*50*width/600,width+(height-width)/2,0); // tanh
  funcButton("Invert","i",10.5*50*width/600,width+(height-width)/3,0); // Invert
  funcButton("Negate","i",10.5*50*width/600,width+2*(height-width)/3,0); // Invert
  // Displays the composition line
  stroke(255);
  for(int i = 1; i < dragPts.size(); i ++) { // Similar thing to the gridlines
    strokeWeight(7*width/1200);
    point(dragPts.get(i).x,dragPts.get(i).y);
    strokeWeight(3*width/1200);
    line(dragPts.get(i).x, dragPts.get(i).y, dragPts.get(i-1).x, dragPts.get(i-1).y);
  }
  if(dragPts.size() > 0) { // Make sure it has an element 0
    strokeWeight(21*width/1200); // Make the first point huge
    point(dragPts.get(0).x, dragPts.get(0).y);
  }
  println(frameRate); // For debugging purposes. Meh
  if(frameCount % 5 == 0)
  {
    save("img_"+frameCount+".png");
  }
}
int button = 1; // Have we pressed a button? Assume so.
int lastButton = 1;  // Stores last button state
String testButtons() { // Outputs which button was pressed
  String function = ""; // Filler to output if nothing was pressed
  if(dist(mouseX,mouseY,50*width/600,width+(height-width)/2) < 35*width/600/2) {
    function = "ex";
  } else if (dist(mouseX,mouseY,2*50*width/600,width+(height-width)/4) < 35*width/600/2) {
    function = "x2";
  } else if (dist(mouseX,mouseY,2*50*width/600,width+3*(height-width)/4) < 35*width/600/2) {
    function = "x3";
  } else if(pow((4*50*width/600-mouseX)/(60*width/600/2),2)+ /* Test inside ellipse */
      pow((width+(height-width)/4-mouseY)/(35*width/600/2),2) < 1){
    function = "cos";
  } else if(pow((4*50*width/600-mouseX)/(60*width/600/2),2)+ /* Test inside ellipse */
      pow((width+3*(height-width)/4-mouseY)/(35*width/600/2),2)<1){
    function = "sin";
  } else if(pow((5*50*width/600-mouseX)/(60*width/600/2),2)+ /* Test inside ellipse */
      pow((width+(height-width)/2-mouseY)/(35*width/600/2),2) < 1){
    function = "tan";
  } else if(pow((7*50*width/600-mouseX)/(75*width/600/2),2)+ /* Test inside ellipse */
      pow((width+(height-width)/4-mouseY)/(35*width/600/2),2) < 1){
    function = "cosh";
  } else if(pow((7*50*width/600-mouseX)/(75*width/600/2),2)+ /* Test inside ellipse */
      pow((width+3*(height-width)/4-mouseY)/(35*width/600/2),2) < 1){
    function = "sinh";
  } else if(pow((8.3*50*width/600-mouseX)/(75*width/600/2),2)+ /* Test inside ellipse */
      pow((width+(height-width)/2-mouseY)/(35*width/600/2),2) < 1){
    function = "tanh";
  } else if(pow((10.5*50*width/600-mouseX)/(120*width/600/2),2)+ /* Test inside ellipse */
      pow((width+(height-width)/3-mouseY)/(35*width/600/2),2) < 1){
    function = "invert";
  } else if(pow((10.5*50*width/600-mouseX)/(120*width/600/2),2)+ /* Test inside ellipse */
      pow((width+2*(height-width)/3-mouseY)/(35*width/600/2),2) < 1){
    function = "negate";
  } else { // This last else is impossible when put inside the button displayer
    button = 0; // Without this else, button = 1 has to be in every other one
  }
  return function; // We output the actual button, not just whether one was hit
}
PVector whichButton(String function) {
  PVector r = new PVector(0,0);
  if(function.equals("ex")) {
    r = new PVector(50*width/600,width+(height-width)/2);
  } else if (function.equals("x2")) {
    r = new PVector(2*50*width/600,width+(height-width)/4);
  } else if (function.equals("x3")){
    r = new PVector(2*50*width/600,width+3*(height-width)/4);
  } else if(function.equals("cos")) {
    r = new PVector(4*50*width/600,width+(height-width)/4);
  } else if(function.equals("sin")) {
    r = new PVector(4*50*width/600,width+3*(height-width)/4);
  } else if(function.equals("tan")) {
    r = new PVector(5*50*width/600,width+(height-width)/2);
  } else if(function.equals("cosh")) {
    r = new PVector(7*50*width/600,width+(height-width)/4);
  } else if(function.equals("sinh")) {
    r = new PVector(7*50*width/600,width+3*(height-width)/4);
  } else if(function.equals("tanh")) {
    r = new PVector(8.3*50*width/600,width+(height-width)/2);
  } else if(function.equals("invert")) {
    r = new PVector(10.5*50*width/600,width+(height-width)/3);
  } else if(function.equals("negate")) {
    r = new PVector(10.5*50*width/600,width+2*(height-width)/3);
  }
  return r;
}
void mousePressed() { // Reset animation if mouse is pressed
  int trans = 1; // For function(stuff). Do we transition?
  String testChangeF = func;
  int testChangeI = invt; // These test if there was change when mouse was pressed
  int testChangeN = negt;
  button = 1; // Assume something was pressed
  if(mouseY > width) { // Bottom box
    if(testButtons() != "") { // A button was hit!
      dragPts = new ArrayList<PVector>(); // Redo the line every time
      func = testButtons();
      if(func.equals("invert")) { // testButtons() only outputs for that button
        func = testChangeF; // So we change the func back
        invt = (invt + 1) % 2; // And change invrt manually
      } else if (func.equals("negate")) {
        func = testChangeF;
        negt = (negt + 1) % 2;
      }
      if((invt == 1 || negt == 1) && whichButton(func).x != 0) { // These are inherent compositions
        dragPts.add(whichButton(func)); // So make the line
        if(invt == 1) { // Basically these check whether to make that part of the line
          dragPts.add(whichButton("invert"));
        }
        if(negt == 1) {
          dragPts.add(whichButton("negate"));
        }
      }
      frames = 0; // Re-animate
      initCoords(trans);
    }
  } else { // Clicked on the graph
    paused = (paused + 1) % 2; // Change pause state
    if(frameCount - lastPause < 15) { // Half second is "double-click" => reset
      zoom = 1; // Zoom gets reset
      func = "asdfghjkl"; // Reset everything
      trans = 0; // Reset immediately
      paused = 0; // Unpause after reset
      invt = 0;
      negt = 0;
      initCoords(trans);
      dragPts = new ArrayList<PVector>(); // The composition line gets renewed whenever we overhaul
    }
    lastPause = frameCount; // Update last pause/click on graph
    button = 0; // Button was not hit
  }
  if (!testChangeF.equals(func) || testChangeI != invt || testChangeN != negt) {
    // This is just a filter to make sure the other 2 elses don't get triggered!
  } else if (button == 1) { // Button was hit but nothing changed => hit same button => reset
    frames = 0;
    func = "x";
    initCoords(trans); // But we traaaaaansfer
    dragPts = new ArrayList<PVector>(); // The composition line gets renewed whenever we overhaul
  } else if (dragPts.size() > 0 && dist(mouseX,mouseY,dragPts.get(0).x,dragPts.get(0).y) < 25/2) {
    frames = 0; // Double-click on the beginning of the composition line => reset it
    func = "x"; // Return to original grid
    invt = 0; // Eh, no invert
    negt = 0;
    initCoords(trans); // But we traaaaaansfer
    dragPts = new ArrayList<PVector>(); // The composition line gets renewed whenever we overhaul
  }
}
void keyReleased() {
  if(key == ' ') { // Space bar to pause
    if(paused == 0) { // Switch between 0 and 1
      paused = 1;
    } else {
      paused = 0;
    }
  } else if (keyCode == ENTER || keyCode == RETURN) {
    zoom = 1;
  }
}

// ZOOM STUFF
int drag = 0;
float d1, d2;
void mouseDragged() {
  paused = 1; // Pause while dragging
  int currentButton;
  if(testButtons() == "") { // Test whether currently on a button
    currentButton = 0;
  } else {
    currentButton = 1;
  }
  if(mouseY < width) { // Test if in the box
    if(mult == 1) {
      d1 = dist(mouseX,mouseY,width/2,width/2)+0.01; // Avoid /0 errors
    }
    d2 = dist(mouseX,mouseY,width/2,width/2);
    mult = d2/d1; // While dragging, we get a preview of the zoom before setting
  } else {
    if(currentButton == 1 && lastButton == 0) { // If we are now pressing, but previously were not
      newFunc.add(testButtons()); // Add the func we're on
    }
    dragPts.add(new PVector(mouseX, mouseY)); // Add point to the composition line
  }
  lastButton = currentButton;
  drag = 1; // We are now dragging!
}
void mouseReleased() {
  if(mult != 1) {
    paused = 0; // Unpause if after zooming
  }
  if(drag == 1) {
    paused = 0;
    for(int i = 0; i < newFunc.size(); i ++) {
      if(i != 0 || !func.equals(newFunc.get(0))) { // Don't want to end up doing 1st function twice
        applyFunc(newFunc.get(i));
        frames = 0; // Only reset if we're really changing something
      }
    }
    newFunc = new ArrayList<String>();
  }
  zoom *= mult; // When we stop dragging, multiply to set zoom officially
  mult = 1; // And then set the multiplier to 1. :/
  drag = 0; // We are not dragging
  lastButton = 0; // We are not on a button
}
void mouseWheel(MouseEvent event) {
  float test = zoom/(1 + float(event.getCount())/25); // See if zoom would be in range
  if(test <= 500 && test >= 0.001) { // Zoom if in range
    zoom = test; // (Zooming as multiplication feels better)
  }
}
