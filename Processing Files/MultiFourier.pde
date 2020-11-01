/** We're going to push the fourier transform to 2D.
 * APPROACH: I'm going to use is to just generalize Fourier in
 * the most natural way: Use the "ultimate" periodic function e^ix.
 * I am condifident that with complex integration BS I can write
 *   f(x) = sum re^itheta * e^ix for coefficients re^itheta
 *        = sum r * e^i(x+theta)
 * Note this is just a magnitude r multiplied by the rotation x
 * and a phase shift theta. This is all geometrically doable.
 */

// Standard stuff
// Paused
boolean notPaused = true;

// Global time variable
float t;
// Coefficients
float[][] coefs;
int terms = 11;  // 5 negative, 0, 5 positive
float maxMag;

int curveSteps = 200; // Number of steps in the curve-drawing

// Stores points as {time, x, y}.
ArrayList<float[]> userPoints;
// This is updated when all userPoints have been added
float[][] givenPoints;

float p = 1; // Period
float[] f (float t)
{
  // User hasn't drawn anything yet: Generic square
  if(givenPoints.length == 0)
  {
    while(t < 0) ++t; t = t % 1;
    // Bottom right to top right
    if(t < 0.25)
      return new float[] {0.3*height, -0.3*height + 2.4*height*t};
    // Top right to top left
    if(t < 0.50)
      return new float[] {0.3*height - 2.4*height*(t-0.25) , 0.3*height};
    // Top left to bottom left
    if(t < 0.75)
      return new float[] {-0.3*height, 0.3*height - 2.4*height*(t-0.50)};
    // Bottom left to bottom right
    return new float[] {-0.3*height + 2.4*height*(t-0.75), -0.3*height};
  }
  // They've drawn something! Now we figure out where to put the point
  t = t % p;
  // We find which i gives the next point after t
  int i = 0;
  for(i = 0; i < givenPoints.length && givenPoints[i][0] < t; ++i);
  // Now we approximate this with a line
  if(i != 0)
  {
    // Our two points are givenPoints[i] and givenPoints[i-1]
    float tDiff = (t - givenPoints[i-1][0]) / (givenPoints[i][0] - givenPoints[i-1][0]);
    return new float[] {(1 - tDiff)*givenPoints[i-1][1] + tDiff * givenPoints[i][1],
      (1 - tDiff) * givenPoints[i-1][2] + tDiff * givenPoints[i][2]};
  }
  // Else it is the case that t = 0 exactly; i = 0 gives t = 0, so 0 >= t.
  // So we just spit out the starting point
  return new float[] {givenPoints[0][1], givenPoints[0][2]};
}

// Calculates out a point given time and global Fourier coefficients
float[] fourier (float t)
{
  float[] r = new float[2];
  for(int i = 0; i < coefs.length; ++i)
  {
    // Remember elements of coef are organized {freq, mag, phase shift}
    r[0] += coefs[i][1] * cos(2*PI*coefs[i][0]/p*t + coefs[i][2]);
    r[1] += coefs[i][1] * sin(2*PI*coefs[i][0]/p*t + coefs[i][2]);
  }
  return r;
}

// Compute the coefficient in the Fourier series
// Returns an array with {frequency, magnitude, argument}
float[] getCoef (int n)
{
  float[] r = new float[3]; r[0] = n;
  // Suppose f(t) = sum am e^(2ipim/l t).
  // Consider the integral of f(t)e^(-2ipi n/l t) dt from 0 to L
  // Plugging in our series, we want sum am int e^(2ipim/l t)e^(2ipin/l t) from 0 to l
  // For m != n, then this integral becomes 1/(thing)e^(2ipi(m-n)/l t) from 0 to l
  // This is just 1/(thing) (1-1) = 0.
  // However, for m = n, then this is sum an int 1 from 0 to l, which is just an * L.
  // This way we can solve for an because the entire integral is just an * L.
  // In particular, we integrate by the inverse!
  // I compute the integral by the rectangle method because lazy.
  int steps = 200; // Probably good enough
  for(float x = 0; x < p; x += p/steps)
  {
    // For now we will make r[1], r[2] be in rectnagular form and convert at the end.
    // Note we are integrating f(t)e^(-2i pi n/l t) = f(t)(cos(2pi n/l t)-i sin(2pi n/l t)).
    float c = cos(-2*PI*n/p*x); float s = sin(-2*PI*n/p*x);
    r[1] += f(x)[0] * c - f(x)[1] * s;
    r[2] += f(x)[0] * s + f(x)[1] * c;
  }
  // Multiply by step size for integral; divide by period for coefficient
  r[1] /= steps; r[2] /= steps;
  // Convert to polar
  return new float[] {n, sqrt(r[1]*r[1] + r[2]*r[2]), atan2(r[2],r[1])};
}

// Sorts the coefficients
float[][] sortCoefs (float[][] coefs)
{
  // Coefficients are sorted by magnitude, with frequency 0 placed first.
  float[][] r = new float[coefs.length][3];
  // I use insertion sort because lazy.
  // Find 0
  for(int i = 0; i < coefs.length; ++i)
  {
    if(coefs[i][0] == 0)
    {
      // Set manually to avoid aliasing
      r[0][0] = coefs[i][0]; r[0][1] = coefs[i][1]; r[0][2] = coefs[i][2];
      // Set magnitude to 0 to avoid duplicating it later in the array
      coefs[i][1] = 0;
    }
  }

  // Now we use insertion sort on everyone else
  for(int ri = 1; ri < r.length; ++ri)
  {
    int maxIndex = 0;
    for(int ci = 0; ci < coefs.length; ++ci)
      if(abs(coefs[ci][1]) > abs(coefs[maxIndex][1]))
        maxIndex = ci;
    // Now that we've found the smallest . . .
    r[ri][0] = coefs[maxIndex][0];
    r[ri][1] = coefs[maxIndex][1];
    r[ri][2] = coefs[maxIndex][2];
    // Now set this point's mag to 0 so that it's never the maximum again
    coefs[maxIndex][1] = 0;
  }
  return r;
}

void circle (float x, float y, float r)
{
  ellipse(x, y, r, r);
}

void drawCurves (float tp)
{
  float[] pt0f; // Old Fourier point
  float[] pt1f; // New Fourier point
  // Set old point prematurely
  pt0f = fourier(tp);

  float[] pt0g; // Old given point
  float[] pt1g; // New given point
  // Set old point prematurely
  pt0g = f(tp);
  for(float t = tp; t >= tp - p; t -= p/curveSteps)
  {
    // GIVEN CURVE
    // Update new point and draw
    pt1g = f(t);
    // Squaring gives a pretty good drop-off in alpha
    strokeWeight(5);
    stroke(147, 112, 219, 255 * (1 - (t-tp)/p * (t-tp)/p));
    line(pt0g[0], pt0g[1], pt1g[0], pt1g[1]);
    // I just set manually because I don't want pt0 to alias to pt1.
    pt0g[0] = pt1g[0]; pt0g[1] = pt1g[1];

    // FOURIER CURVE
    // Update new point and draw
    pt1f = fourier(t);
    // Squaring gives a pretty good drop-off in alpha
    strokeWeight(3);
    stroke(0, 137, 189, 255 * (1 - (t-tp)/p * (t-tp)/p));
    line(pt0f[0], pt0f[1], pt1f[0], pt1f[1]);
    // I just set manually because I don't want pt0 to alias to pt1.
    pt0f[0] = pt1f[0]; pt0f[1] = pt1f[1];
  }
}

void drawCircles (float tp)
{
  noFill();
  // We'll update these points as we draw the circles
  // One longer than frequency because we start at the center
  float[] xs = new float[coefs.length + 1]; // Itialized to start at (0,0)
  float[] ys = new float[coefs.length + 1];
  stroke(200, 51, 36, 80);
  int i = 0;
  for(i = 1; i < coefs.length; ++i)
  {
    // Remember coefs[i] = {freq, mag, phase shift}.
    // Update position
    xs[i] = xs[i-1] + coefs[i-1][1] * cos(2*PI*coefs[i-1][0]/p*tp + coefs[i-1][2]);
    ys[i] = ys[i-1] + coefs[i-1][1] * sin(2*PI*coefs[i-1][0]/p*tp + coefs[i-1][2]);
    // Circle
    strokeWeight(2);
    circle(xs[i], ys[i], 2 * coefs[i][1]); // Ellipse takes diameters?
  }
  // Update last point for the lines
  xs[i] = xs[i-1] + coefs[i-1][1] * cos(2*PI*coefs[i-1][0]/p*tp + coefs[i-1][2]);
  ys[i] = ys[i-1] + coefs[i-1][1] * sin(2*PI*coefs[i-1][0]/p*tp + coefs[i-1][2]);

  // Draw lines
  strokeWeight(3);
  stroke(0, 137, 189);
  // Don't start at 0 because that's the boring constant
  for(i = 1; i < xs.length - 1; ++i)
    line(xs[i], ys[i], xs[i+1], ys[i+1]);
}

void initGraphs (float p_)
{
  // Initialize coefficients
  coefs = new float[terms][3];
  for(int i = 0; i < coefs.length; ++i)
    coefs[i] = getCoef(i - terms/2);
  coefs = sortCoefs(coefs);
  maxMag = coefs[1][1];

  // I'm setting time a bit high because of negative modulus issues.
  t = p_;
  p = p_;
}

void run ()
{
  // Standard function calls
  drawCircles(t);
  drawCurves(t);

  // Increment time; set to 1 period every 6 seconds
  if(notPaused)
    t += p / 60 / 6;
}

void setup ()
{
  size(1200, 1200);
  textAlign(CENTER);
  textSize(height/25);

  // Itialize user arrays
  userPoints = new ArrayList<float[]>();
  givenPoints = new float[0][0];

  // Initialize coefficients
  initGraphs(p);
}

void draw ()
{
  background(224, 201, 175);

  // Text portions
  fill(0);
  if(givenPoints.length == 0)
    // Some encouragement
    text("Click and drag!", width/2, 3*height/32);
  text("Terms (next): " + (terms < 10 ? "0" : "") + terms, 7 * height/40, height - 20);

  // I apply these transformations to make my life easier.
  // Eg, Q1 is where we expect it to be: Top left.
  translate(width/2, height/2);
  scale(1, -1);

  // I put this one first because branch predictor, but whatever
  if(!mousePressed)
    run();
  else
  {
    // Add to the userPoints (for safekeeping)
    // mouseX and mouseY are not subject to the transformations, so we apply manually
    userPoints.add(new float[] {frameCount, mouseX - width/2, height/2 - mouseY});
    // We display them so that the user can see what's going on
    strokeWeight(5);
    stroke(147, 112, 219);
    for(int i = 1; i < userPoints.size(); ++i)
      line(userPoints.get(i-1)[1], userPoints.get(i-1)[2],
        userPoints.get(i)[1], userPoints.get(i)[2]);
  }
  // For my convenience
  println(frameRate);
}

// Update after drawing
void mouseReleased ()
{
  // We test to make sure that the user actually drew *something*
  if(userPoints.size() > 1)
  {
    // We transfer the data to a 2D array and make userPoints empty again
    givenPoints = new float[userPoints.size()][3];
    for(int i = 0; i < givenPoints.length; ++i)
    {
      // We shift time to start at t = 0
      givenPoints[i][0] = userPoints.get(i)[0] - userPoints.get(0)[0];
      givenPoints[i][1] = userPoints.get(i)[1];
      givenPoints[i][2] = userPoints.get(i)[2];
      // We've also avoided aliasing, but this isn't an issue
    }
    // We make this empty again
    userPoints = new ArrayList<float[]>();

    // Now we alter p and t accordingly
    p = givenPoints[givenPoints.length - 1][0];
    t = p;

    // Finally, we re-initialize the coefficients
    coefs = new float[terms][3];
    for(int i = 0; i < coefs.length; ++i)
      coefs[i] = getCoef(i - terms/2);
    coefs = sortCoefs(coefs);
    maxMag = coefs[1][1];
  }
  // Change nothing
  else
    userPoints = new ArrayList<float[]>();
}

// Things I don't care happen number of times per key press
void keyPressed ()
{
  // Exit for my convenience
  if (keyCode == ESC) exit();
}

// Events I only want to happen once per key press
void keyReleased ()
{
  // Pause
  if(key == ' ') notPaused = !notPaused;
  // Increment terms
  if(keyCode == UP && terms < 99) terms += 2;
  if(keyCode == DOWN && terms > 3) terms -= 2;
}
