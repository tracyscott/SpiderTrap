package art.lookingup.linear;

import art.lookingup.colors.ColorPalette;
import art.lookingup.spidertrap.SpiderTrapModel;
import art.lookingup.util.EaseUtil;
import art.lookingup.util.GLUtil;
import heronarts.lx.color.LXColor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static art.lookingup.spidertrap.SpiderTrapModel.*;

public class Blob {
  private static final Logger logger = Logger.getLogger(Blob.class.getName());
  public static final int WAVEFORM_TRIANGLE = 0;
  public static final int WAVEFORM_SQUARE = 1;
  public static final int WAVEFORM_STEPDECAY = 2;

  public DirectionalLP dlp;
  public float pos = 0f;
  public float speed = 1f;
  public List<DirectionalLP> prevLPs = new ArrayList<DirectionalLP>();
  public List<DirectionalLP> nextLPs = new ArrayList<DirectionalLP>();
  public int color = LXColor.rgba(255, 255, 255, 255);
  public boolean enabled = true;
  public float intensity = 1.0f;
  public float blobWidth = -1.0f;
  public int pal = -1;
  public EaseUtil easeUtil;
  public float palTVal = -1f;

  // When rendering position parametrically from 0 to 1, we need a pre-computed set of LinearPoints
  // that we intend to render on.
  public List<DirectionalLP> pathLPs;

  // World space coordinates
  public float u;
  public float v;
  public Point3D worldSpace = new Point3D(0f, 0f, 0f);

  // For render shaders
  public GLUtil.SpiderGLContext spGLCtx;
  public float shaderSpeed = 1f;

  public void updateUV() {
    if (dlp != null) {
      dlp.lp.edge.interpolate(worldSpace, pos);
      float zOffset = 0.07f;
      zOffset = (1.0f - (zRange / xRange))/2f;
      u = (worldSpace.x - SpiderTrapModel.modelXMin)/largestRange;
      v = (worldSpace.z - SpiderTrapModel.modelZMin)/largestRange + zOffset;
    }
  }

  public void updateCurrentLP(int barSelector) {
    // First, lets transfer the current LinearPoints into our
    // previous LinearPoints list.  The list of older linearpoints will be trimmed in our draw loop.
    prevLPs.add(0, dlp);
    // Next the current LinearPoints should come from the beginning of the nextLPs
    // The nextLPs list will be filled out in the draw loop if necessary.  For example, if the slope of a
    // triangle wave is shallow enough the current position might be on one LinearPoints segment but the
    // leading edge of the triangle might be on the 'next' LinearPoints, so we need to look ahead and compute
    // them as we need them.

    //logger.info("current lp: " + dlp.lp.lpNum + " " + ((dlp.forward)?"forward":"reverse"));
    if (nextLPs.size() > 0) {
      dlp = nextLPs.remove(0);
    } else {
      dlp = dlp.chooseNextBar(barSelector);
    }

    //logger.info("new lp: " + dlp.lp.lpNum + " " + ((dlp.forward)?"forward":"reverse"));

    // Set our position based on the directionality of the current LinearPoints
    if (dlp.forward) {
      pos = 0.0f;
    } else {
      pos = 1.0f;
    }
    updateUV();
  }

  public void reset(int lightBarNum, float initialPos, float randomSpeed, boolean forward) {
    pos = initialPos;
    dlp = new DirectionalLP(lightBarNum, forward);
    speed = randomSpeed * (float)Math.random();
    nextLPs = new ArrayList<DirectionalLP>();
    prevLPs = new ArrayList<DirectionalLP>();
  }

  public void renderBlob(int[] colors, float baseSpeed, float defaultWidth, float slope,
                         float maxValue, int waveform, int whichJoint, boolean initialTail,
                         int whichEffect, float fxDepth, float cosineFreq) {
    renderBlob(colors, baseSpeed, defaultWidth, slope, maxValue, waveform, whichJoint, initialTail, LXColor.Blend.ADD,
        whichEffect, fxDepth, cosineFreq);
  }

  public void renderBlobShader(int[] colors, float baseSpeed, int whichJoint, LXColor.Blend blend, double deltaMs) {
    renderBlob(colors, baseSpeed, 1.0f, 1.0f, 1.0f, 0, whichJoint,
        false, blend, 0, 0.0f, 0.0f,  true, deltaMs);
  }

  public void renderBlob(int[] colors, float baseSpeed, float defaultWidth, float slope,
                         float maxValue, int waveform, int whichJoint, boolean initialTail, LXColor.Blend blend,
                         int whichEffect, float fxDepth, float cosineFreq) {
      renderBlob(colors, baseSpeed, defaultWidth, slope, maxValue, waveform, whichJoint, initialTail, blend,
          whichEffect, fxDepth, cosineFreq, false, 0.0);
  }

  /**
   * Renders a 'blob'.  Could be a number of different 'waveforms' centered at the current
   * position.  Position will be incremented by baseSpeed + the blobs random speed component.
   * This method will handle making sure there are enough DirectionalLinearPoints so that the
   * waveform can be rendered across multiple LinearPoints.  Accounting for both trailing LinearPoints and
   * leading LinearPoints (for example with a triangle wave that can fall-off forward).
   *
   * @param colors
   * @param baseSpeed
   * @param defaultWidth
   * @param slope
   * @param maxValue
   * @param waveform
   * @param whichJoint
   */
  public void renderBlob(int[] colors, float baseSpeed, float defaultWidth, float slope,
                         float maxValue, int waveform, int whichJoint, boolean initialTail, LXColor.Blend blend,
                         int whichEffect, float fxDepth, float cosineFreq, boolean useShader, double deltaMs) {
    if (!enabled) return;
    boolean needsCurrentLPUpdate = false;
    float resolvedWidth = defaultWidth;
    if (blobWidth >= 0f)
      resolvedWidth = blobWidth;
    for (Edge edge : SpiderTrapModel.allEdges) {
      // NOTE(tracy): This was used for iterating both LinearPoints in a double-sided LinearPoints.
      for (int elbNum = 0; elbNum < 1; elbNum++) {
        LinearPoints lp = edge.linearPoints;
        if (dlp.lp.lpNum == lp.lpNum) {
          // -- Render on our target linearpoints --

          float minMax[] = {0.5f, 0.5f};

          if (!useShader)
            minMax = renderWaveform(colors, dlp, pos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
          else {
            // TODO(tracy): Should params be set per blob?
            spGLCtx.scriptParams.put("x1", u - 0.5f);
            spGLCtx.scriptParams.put("y1", v - 0.5f);
            GLUtil.glRun(spGLCtx, deltaMs, shaderSpeed, false);
            GLUtil.copyTFBufferToPoints(colors, spGLCtx, LXColor.Blend.ADD);
          }

          if (whichEffect == 1) {
            LPRender.randomGrayBaseDepth(colors, dlp.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                (int) (255 * fxDepth));
          } else if (whichEffect == 2) {
            LPRender.cosine(colors, dlp.lp, pos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
          }
          // -- Fix up the set of multiple linearpoints that we are rendering over.  minMax values are always 0 to 1
          // normalized.  This helps us compute how many trailing or leading linearpoints we need easily without accounting
          // for their length.
          int numPrevLPs = -1 * (int) Math.floor(minMax[0]);
          int numNextLPs = (int) Math.ceil(minMax[1] - 1.0f);
          if (!dlp.forward) {
            int oldNumNextBars = numNextLPs;
            numNextLPs = numPrevLPs;
            numPrevLPs = oldNumNextBars;
          }
          // We need to handle the initial case, so we might need to add multiple next linearpoints to our list.
          while (nextLPs.size() < numNextLPs) {
            DirectionalLP nextDlp;
            if (nextLPs.size() == 0)
              nextDlp = dlp.chooseNextBar(whichJoint);
            else
              nextDlp = nextLPs.get(nextLPs.size() - 1).chooseNextBar(whichJoint);
            nextLPs.add(nextDlp);
          }

          // Pre-populate the previous linearpoints if we want an initial tail.  Otherwise
          // these will be populated as we update the current linearpoints to the next linearpoints.
          while (initialTail && (prevLPs.size() < numPrevLPs)) {
            DirectionalLP prevDlp;
            if (prevLPs.size() == 0)
              prevDlp = dlp.choosePrevBar(whichJoint);
            else
              prevDlp = prevLPs.get(prevLPs.size() - 1).choosePrevBar(whichJoint);
            prevLPs.add(prevDlp);
          }

          // Garbage collect any old linearpoints.
          // TODO(tracy): We should trim both nextLPs and prevLPs each time so for example if our slope changes
          // dynamically, we might want to reduce our prevLPs and nextLPs list.  It is only an optimization since
          // we will just render black in ADD mode which should have no effect but is just inefficient.
          if (prevLPs.size() > numPrevLPs && prevLPs.size() > 0) {
            prevLPs.remove(prevLPs.size() - 1);
          }

          // For the number of previous linearpoints, render on each linearpoints
          for (int j = 0; j < numPrevLPs && j < prevLPs.size(); j++) {
            DirectionalLP prevLP = prevLPs.get(j);
            // We need to compute the next linearpoints pos but we need to account for any intermediate bars.
            float prevLPPos = dlp.computePrevBarPos(pos, prevLP);
            // LinearPoints lengths are normalized to 1.0, so we need to shift our compute distance based on
            // whether there are any intermediate LinearPoints.
            if (prevLP.forward) prevLPPos += j;
            else prevLPPos -= j; //
            renderWaveform(colors, prevLP, prevLPPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
            if (whichEffect == 1) {
              LPRender.randomGrayBaseDepth(colors, prevLP.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                  (int) (255 * fxDepth));
            } else if (whichEffect == 2) {
              LPRender.cosine(colors, prevLP.lp, prevLPPos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
            }
          }

          for (int j = 0; j < numNextLPs; j++) {
            DirectionalLP nextLP = nextLPs.get(j);
            float nextLPPos = dlp.computeNextBarPos(pos, nextLP);
            if (nextLP.forward)
              nextLPPos -= j; // shift the position to the left by the number of linearpoints away it is actually at.
            else
              nextLPPos += j;
            renderWaveform(colors, nextLP, nextLPPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
            if (whichEffect == 1) {
              LPRender.randomGrayBaseDepth(colors, nextLP.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                  (int) (255 * fxDepth));
            } else if (whichEffect == 2) {
              LPRender.cosine(colors, nextLP.lp, nextLPPos, cosineFreq, 0f, 1f - fxDepth, fxDepth,
                  LXColor.Blend.MULTIPLY);
            }
          }

          // NOTE(tracy): The position is normalized to the length of the linear points.  The effect from this is that
          // the blob would travel faster on longer LinearPoints.  We need to scale the update by the length of the LinearPoints.
          if (dlp.forward) {
            pos += ((baseSpeed + speed) / 100f)/(dlp.lp.length);
          } else {
            pos -= ((baseSpeed + speed) / 100f)/(dlp.lp.length);
          }

          updateUV();

          //logger.info("linear points: " + dlp.lp.lpNum + " pos: " + pos);

          if (pos <= 0.0 || pos >= 1.0f) {
            needsCurrentLPUpdate = true;
          }
        }
      }
    }

    if (needsCurrentLPUpdate) {
      //logger.info("Need bar update");
      updateCurrentLP(whichJoint);
    }
  }
  public void renderBlobAtT(int[] colors, float paramT, float defaultWidth, float slope,
                            float maxValue, int waveform, float maxGlobalPos) {
    renderBlobAtT(colors, paramT, defaultWidth, slope, maxValue, waveform, 0f, maxGlobalPos);
  }

  /**
   * Renders a waveform on a pre-computed list of lightbars stored in pathBars.  Position is
   * defined parametrically from 0 to 1 where 1 is at the end of the last lightbar.  Automatically
   * adjusts to number of lightbars.
   * @param colors
   * @param paramT
   * @param defaultWidth
   * @param slope
   * @param maxValue
   * @param waveform
   */
  public void renderBlobAtT(int[] colors, float paramT, float defaultWidth, float slope,
                            float maxValue, int waveform, float startMargin, float maxGlobalPos) {
    if (!enabled) return;
    float resolvedWidth = defaultWidth;
    if (blobWidth >= 0f) resolvedWidth = blobWidth;
    for (Edge edge: SpiderTrapModel.allEdges) {
      LinearPoints lb = edge.linearPoints;
      int dlbNum = 0;
      for (DirectionalLP currentDlb : pathLPs) {
        if (currentDlb.lp.lpNum == lb.lpNum && !currentDlb.disableRender) {
          // -- Render on our target light bar and adjust pos based on bar num.
          float localDlbPos = paramT * (maxGlobalPos + startMargin) - startMargin;
          localDlbPos -= dlbNum;
          if (!currentDlb.forward)
            localDlbPos = 1.0f - localDlbPos;

          renderWaveform(colors, currentDlb, localDlbPos, resolvedWidth, slope, intensity * maxValue, waveform, LXColor.Blend.ADD);
        }
        dlbNum++;
      }
    }
  }

  /**
   * Render the specified waveform at the specified position.  maxValue already includes the blob intensity override multiplied
   * into it by this point.
   * @param colors
   * @param targetDlb
   * @param position
   * @param width
   * @param slope
   * @param maxValue
   * @param waveform
   * @param blend
   * @return
   */
  public float[] renderWaveform(int[] colors, DirectionalLP targetDlb, float position, float width, float slope,
                                float maxValue, int waveform, LXColor.Blend blend) {
    if (waveform == WAVEFORM_TRIANGLE)
      return LPRender.renderTriangle(colors, targetDlb.lp, position, slope, maxValue, blend, color, pal, easeUtil, palTVal);
    else if (waveform == WAVEFORM_SQUARE)
      return LPRender.renderSquare(colors, targetDlb.lp, position, width, maxValue, blend, color, pal, easeUtil, palTVal);
    else
      return LPRender.renderStepDecay(colors, targetDlb.lp, position, width, slope,
          maxValue, targetDlb.forward, LXColor.Blend.ADD, color, pal, easeUtil, palTVal);
  }
}
