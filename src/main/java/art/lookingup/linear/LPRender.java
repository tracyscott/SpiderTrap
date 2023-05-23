package art.lookingup.linear;

import art.lookingup.colors.ColorPalette;
import art.lookingup.colors.Colors;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * LPRender implements a variety of 1D rendering functions that
 * are local to the specified LinearPoints object.  pointSets are used for double sided
 * LinearPoints.
 */
public class LPRender {
  private static final Logger logger = Logger.getLogger(LPRender.class.getName());

  static public void randomGray(int colors[], LinearPoints linearPoints, LXColor.Blend blend) {
    Random r = new Random();
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> pointSet : pointSets) {
      for (LPPoint pt : pointSet) {
        int randomValue = r.nextInt(256);
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(randomValue, randomValue, randomValue, 255), blend);
      }
    }
  }

  static public void randomGrayBaseDepth(int colors[], LinearPoints linearPoints, LXColor.Blend blend, int min, int depth) {
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> pointSet : pointSets) {
      for (LPPoint pt : pointSet) {
        if (depth < 0)
          depth = 0;
        int randomDepth = ThreadLocalRandom.current().nextInt(depth);
        int value = min + randomDepth;
        if (value > 255) {
          value = 255;
        }
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(value, value, value, 255), blend);
      }
    }
  }

  static public void sine(int colors[], LinearPoints linearPoints, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> pointSet : pointSets) {
      for (LPPoint pt : pointSet) {
        float ptX = pt.lpx / linearPoints.length;
        float value = ((float) Math.sin((double) freq * (head - ptX) + phase) + 1.0f) / 2.0f;
        value = min + depth * value;
        int color = (int) (value * 255f);
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(color, color, color, 255), blend);
      }
    }
  }

  static public void cosine(int colors[], LinearPoints linearPoints, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> pointSet : pointSets) {
      for (LPPoint pt : pointSet) {
        float ptX = pt.lpx / linearPoints.length;
        float value = ((float) Math.cos((double) freq * (head - ptX) + phase) + 1.0f) / 2.0f;
        value = min + depth * value;
        int color = (int) (value * 255f);
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(color, color, color, 255), blend);
      }
    }
  }

  /**
   * Render a triangle gradient in gray.  t is the 0 to 1 normalized x position.  Slope
   * is the slope of the gradient.
   * TODO(tracy): Slope normalization needs to account for led density? i.e. Max slope should include
   * only one led.  Minimum slope should include all leds.
   * @param colors LED colors array.
   * @param linearPoints The LinearPoints strip to render on.
   * @param t Normalized (0.0-1.0) x position.
   * @param slope The slope of the gradient.  Not normalized currently.
   * @param maxValue Maximum value of the step function (0.0 - 1.0)
   * @param blend Blend mode for writing into the colors array.
   * @return A float array containing the minimum x intercept and maximum x intercept in that order.
   */
  static public float[] renderTriangle(int colors[], LinearPoints linearPoints, float t, float slope, float maxValue, LXColor.Blend blend) {
    return renderTriangle(colors, linearPoints, t, slope, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
  }

  static public float[] renderTriangle(int colors[], LinearPoints linearPoints, float t, float slope, float maxValue, LXColor.Blend blend,
                                       int color) {
    return renderTriangle(colors, linearPoints, t, slope, maxValue, blend, color, null, -1);
  }

  static public float[] renderTriangle(int colors[], LinearPoints linearPoints, float t, float slope, float maxValue, LXColor.Blend blend,
                                       int color, ColorPalette pal, float palTVal) {
    float[] minMax = new float[2];
    minMax[0] = (float) zeroCrossingTriangleWave(t, slope);
    minMax[1] = (float) zeroCrossingTriangleWave(t, -slope);
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> nextPointSet : pointSets) {
      for (LPPoint pt : nextPointSet) {
        float val = (float) triangleWave(t, slope, pt.lpx / linearPoints.length) * maxValue;
        //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
        int theColor = color;
        if (pal != null) {
          if (palTVal == -1f)
            theColor = pal.getColor(val);
          else
            theColor = pal.getColor(palTVal);
        }
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int) (Colors.red(theColor) * val), (int) (Colors.green(theColor) * val), (int) (Colors.blue(theColor) * val), 255),
            blend);
      }
    }
    return minMax;
  }

  static public float[] renderSquare(int colors[], LinearPoints linearPoints, float t, float width, float maxValue, LXColor.Blend blend) {
    return renderSquare(colors, linearPoints, t, width, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
  }

  static public float[] renderSquare(int colors[], LinearPoints linearPoints, float t, float width, float maxValue, LXColor.Blend blend,
                                     int color) {
    return renderSquare(colors, linearPoints, t, width, maxValue, blend, color, null, -1f);
  }

  static public float[] renderSquare(int colors[], LinearPoints linearPoints, float t, float width, float maxValue, LXColor.Blend blend,
                                     int color, ColorPalette pal, float palTVal) {
    double barPos = t * linearPoints.length;
    float[] minMax = new float[2];
    minMax[0] = t - width/2.0f;
    minMax[1] = t + width/2.0f;
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> nextPointSet : pointSets) {
      for (LPPoint pt : nextPointSet) {
        //int gray = (int) ((((pt.lbx > minMax[0]*linearPoints.length) && (pt.lbx < minMax[1]*linearPoints.length))?maxValue:0f)*255.0f);
        float val = (((pt.lpx > minMax[0] * linearPoints.length) && (pt.lpx < minMax[1] * linearPoints.length)) ? maxValue : 0f);
        int theColor = color;
        if (pal != null) {
          if (palTVal == -1f)
            theColor = pal.getColor(val);
          else
            theColor = pal.getColor(palTVal);
        }
        int newColor = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int) (Colors.red(theColor) * val), (int) (Colors.green(theColor) * val), (int) (Colors.blue(theColor) * val), 255),
            blend);
        colors[pt.index] = newColor;
      }
    }
    return minMax;
  }

  /**
   * Render a step function at the given position with the given slope.
   * @param colors Points color array to write into.
   * @param linearPoints The linearpoints strip to render on.
   * @param t Normalized (0.0-1.0) x position of the step function on the linearpoints strip.
   * @param slope The slope of edge of the step function.
   * @param maxValue Maximum value of the step function (0.0 - 1.0)
   * @param forward Direction of the step function.
   * @param blend Blend mode for writing into the colors array.
   */
  static public float[] renderStepDecay(int colors[], LinearPoints linearPoints, float t, float width, float slope,
                                        float maxValue, boolean forward, LXColor.Blend blend) {
    return renderStepDecay(colors, linearPoints, t, width, slope, maxValue, forward, blend, LXColor.rgba(255, 255, 255, 255));
  }

  static public float[] renderStepDecay(int colors[], LinearPoints linearPoints, float t, float width, float slope,
                                        float maxValue, boolean forward, LXColor.Blend blend, int color) {
    return renderStepDecay(colors, linearPoints, t, width, slope, maxValue, forward, blend, color, null, -1f);
  }

  static public float[] renderStepDecay(int colors[], LinearPoints linearPoints, float t, float width, float slope,
                                        float maxValue, boolean forward, LXColor.Blend blend, int color,
                                        ColorPalette pal, float palTVal) {
    float[] minMax = stepDecayZeroCrossing(t, width, slope, forward);
    List<List<LPPoint>> pointSets = linearPoints.getPointSets();
    for (List<LPPoint> nextPointSet : pointSets) {
      for (LPPoint pt : nextPointSet) {
        //int gray = (int) (stepDecayWave(t, width, slope, pt.lbx/linearPoints.length, forward)*255.0*maxValue);
        float val = stepDecayWave(t, width, slope, pt.lpx / linearPoints.length, forward)*maxValue;
        //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
        int theColor = color;
        if (pal != null) {
          if (palTVal == -1f)
            theColor = pal.getColor(val);
          else
            theColor = pal.getColor(palTVal);
        }
        colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(
                (int)(Colors.red(theColor) * val), (int)(Colors.green(theColor) * val), (int)(Colors.blue(theColor) * val), 255),
            blend);
      }
    }

    return minMax;
  }

  static public float triWave(float t, float p)  {
    return 2.0f * (float)Math.abs(t / p - Math.floor(t / p + 0.5f));
  }

  static public float[] stepDecayZeroCrossing(float stepPos, float width, float slope, boolean forward) {
    float[] minMax = new float[2];
    float max = stepPos + width/2.0f;
    float min = stepPos - width/2.0f - 1.0f/slope;
    // If our orientation traveling along the bar is backwards, swap our min/max computations.

    float tail = 0f;
    if (forward) {
      tail  = - 1.0f/slope + stepPos - width/2.0f;
    } else {
      tail = 1.0f/slope + stepPos + width/2.0f;
    }

    float head = 0;
    if (forward) {
      head = stepPos + width/2.0f;
    } else {
      head = stepPos - width/2.0f;
    }

    if (forward) {
      minMax[0] = tail;
      minMax[1] = head;
    } else {
      minMax[1] = tail;
      minMax[0] = head;
    }
    /*
    if (forward) {
      minMax[0] = min;
      minMax[1] = max;
    } else {
      minMax[0] = max;
      minMax[1] = min;
    }
    */
    return minMax;
  }

  /**
   * Step wave with attack slope.
   * Returns value from 0.0f to 1.0f
   */
  static public float stepDecayWave(float stepPos, float width, float slope, float x, boolean forward) {
    float value;
    if ((x > stepPos - width/2.0f) && (x < stepPos + width/2.0f))
      return 1.0f;

    if ((x > stepPos + width/2.0f) && forward)
      return 0f;
    else if ((x < stepPos - width/2.0f && !forward))
      return 0f;

    if (forward) {
      value = 1.0f + slope * (x - (stepPos - width/2.0f));
      if (value < 0f) value = 0f;
    } else {
      value = 1.0f - slope * (x - (stepPos + width/2.0f));
      if (value < 0f) value = 0f;
    }
    return value;
  }

  static public double zeroCrossingTriangleWave(double peakX, double slope) {
    return peakX - 1.0/slope;
  }

  /**
   * Normalized triangle wave function.  Given position of triangle peak and the
   * slope, return value of function at evalAtX.  If less than 0, clip to zero.
   */
  static public double triangleWave(double peakX, double slope, double evalAtX)
  {
    // If we are to the right of the triangle, the slope is negative
    if (evalAtX > peakX) slope = -slope;
    double y = slope * (evalAtX - peakX) + 1.0f;
    if (y < 0f) y = 0f;
    return y;
  }

  static public void renderColor(int[] colors, LinearPoints lb, int red, int green, int blue, int alpha) {
    renderColor(colors, lb, LXColor.rgba(red, green, blue, alpha));
  }

  static public void renderColor(int[] colors, LinearPoints lb, int color) {
    renderColor(colors, lb, color, 1.0f);
  }

  static public void renderColor(int[] colors, LinearPoints lb, int color, float maxValue) {
    for (LXPoint point: lb.points) {
      colors[point.index] = LXColor.rgba(
          (int)(Colors.red(color) * maxValue), (int)(Colors.green(color) * maxValue), (int)(Colors.blue(color) * maxValue), 255);
    }
  }
}
