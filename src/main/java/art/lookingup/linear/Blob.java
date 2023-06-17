package art.lookingup.linear;

import art.lookingup.colors.ColorPalette;
import art.lookingup.spidertrap.SpiderTrapModel;
import heronarts.lx.color.LXColor;

import java.util.ArrayList;
import java.util.List;

public class Blob {
  public static final int WAVEFORM_TRIANGLE = 0;
  public static final int WAVEFORM_SQUARE = 1;
  public static final int WAVEFORM_STEPDECAY = 2;

  public DirectionalLP dlb;
  public float pos = 0f;
  public float speed = 1f;
  public List<DirectionalLP> prevLPs = new ArrayList<DirectionalLP>();
  public List<DirectionalLP> nextLPs = new ArrayList<DirectionalLP>();
  public int color = LXColor.rgba(255, 255, 255, 255);
  public boolean enabled = true;
  public float intensity = 1.0f;
  public float blobWidth = -1.0f;
  public ColorPalette pal = null;
  public float palTVal = -1f;

  // When rendering position parametrically from 0 to 1, we need a pre-computed set of LinearPoints
  // that we intend to render on.  See TopBottomT for an example of setting this up.
  public List<DirectionalLP> pathLPs;

  public void updateCurrentLP(int barSelector) {
    // First, lets transfer the current LinearPoints into our
    // previous LinearPoints list.  The list will be trimmed in our draw loop.
    prevLPs.add(0, dlb);
    // Next the current LinearPoints should come from the beginning of the nextLPs
    // The nextLPs list will be filled out in the draw loop if necessary.
    //System.out.println("current lp: " + dlp.lp.lpNum + " " + ((dlp.forward)?"forward":"reverse"));
    if (nextLPs.size() > 0) {
      //System.out.println("from nextBars");
      dlb = nextLPs.remove(0);
    } else {
      //System.out.println("from dlb.chooseNextBar");
      dlb = dlb.chooseNextBar(barSelector);
    }

    // System.out.println("new bar: " + dlb.lb.barNum + " " + ((dlb.forward)?"forward":"reverse"));

    // Set or position based on the directionality of the current lightbar.
    if (dlb.forward) {
      pos = 0.0f;
    } else {
      pos = 1.0f;
    }
  }

  public void reset(int lightBarNum, float initialPos, float randomSpeed, boolean forward) {
    pos = initialPos;
    dlb = new DirectionalLP(lightBarNum, forward);
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

  /**
   * Renders a 'blob'.  Could we a number of different 'waveforms' centered at the current
   * position.  Position will be incremented by baseSpeed + the blobs random speed component.
   * This method will handle making sure there are enough DirectionalLightBars so that the
   * waveform can be rendered across multiple lightbars.
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
                         int whichEffect, float fxDepth, float cosineFreq) {
    if (!enabled) return;
    boolean needsCurrentBarUpdate = false;
    float resolvedWidth = defaultWidth;
    if (blobWidth >= 0f)
      resolvedWidth = blobWidth;
    for (Edge edge : SpiderTrapModel.allEdges) {
      // NOTE(tracy): This was used for iterating both lightbars in a double sided lightbar.
      for (int elbNum = 0; elbNum < 1; elbNum++) {
        LinearPoints lb = edge.linearPoints;
        if (dlb.lp.lpNum == lb.lpNum) {
          // -- Render on our target light bar --
          float minMax[] = renderWaveform(colors, dlb, pos, resolvedWidth, slope, intensity * maxValue, waveform, blend);

          if (whichEffect == 1) {
            LPRender.randomGrayBaseDepth(colors, dlb.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                (int) (255 * fxDepth));
          } else if (whichEffect == 2) {
            LPRender.cosine(colors, dlb.lp, pos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
          }
          // -- Fix up the set of lightbars that we are rendering over.
          int numPrevBars = -1 * (int) Math.floor(minMax[0]);
          int numNextBars = (int) Math.ceil(minMax[1] - 1.0f);
          if (!dlb.forward) {
            int oldNumNextBars = numNextBars;
            numNextBars = numPrevBars;
            numPrevBars = oldNumNextBars;
          }
          // We need to handle the initial case, so we might need to add multiple next bars to our list.
          while (nextLPs.size() < numNextBars) {
            DirectionalLP nextDlb;
            if (nextLPs.size() == 0)
              nextDlb = dlb.chooseNextBar(whichJoint);
            else
              nextDlb = nextLPs.get(nextLPs.size() - 1).chooseNextBar(whichJoint);
            nextLPs.add(nextDlb);
          }

          // Pre-populate the previous lightbars if we want an initial tail.  Otherwise
          // these will be populated as we update the current bar to the next bar.
          while (initialTail && (prevLPs.size() < numPrevBars)) {
            DirectionalLP prevDlb;
            if (prevLPs.size() == 0)
              prevDlb = dlb.choosePrevBar(whichJoint);
            else
              prevDlb = prevLPs.get(prevLPs.size() - 1).choosePrevBar(whichJoint);
            prevLPs.add(prevDlb);
          }

          // Garbage collect any old bars.
          // TODO(tracy): We should trim both nextBars and prevBars each time so for example if our slope changes
          // dynamically, we might want to reduce our prevBars and nextBars list.  It is only an optimization since
          // we will just render black in ADD mode which should have no effect but is just inefficient.
          if (prevLPs.size() > numPrevBars && prevLPs.size() > 0) {
            prevLPs.remove(prevLPs.size() - 1);
          }

          // For the number of previous bars, render on each bar
          for (int j = 0; j < numPrevBars && j < prevLPs.size(); j++) {
            DirectionalLP prevBar = prevLPs.get(j);
            // We need to compute the next bar pos but we need to account for any intermediate bars.
            float prevBarPos = dlb.computePrevBarPos(pos, prevBar);
            // LightBar lengths are normalized to 1.0, so we need to shift our compute distance based on
            // whether there are any intermediate lightbars.
            if (prevBar.forward) prevBarPos += j;
            else prevBarPos -= j; //
            renderWaveform(colors, prevBar, prevBarPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
            if (whichEffect == 1) {
              LPRender.randomGrayBaseDepth(colors, prevBar.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                  (int) (255 * fxDepth));
            } else if (whichEffect == 2) {
              LPRender.cosine(colors, prevBar.lp, prevBarPos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
            }
          }

          for (int j = 0; j < numNextBars; j++) {
            DirectionalLP nextBar = nextLPs.get(j);
            float nextBarPos = dlb.computeNextBarPos(pos, nextBar);
            if (nextBar.forward)
              nextBarPos -= j; // shift the position to the left by the number of bars away it is actually at.
            else
              nextBarPos += j;
            renderWaveform(colors, nextBar, nextBarPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
            if (whichEffect == 1) {
              LPRender.randomGrayBaseDepth(colors, nextBar.lp, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                  (int) (255 * fxDepth));
            } else if (whichEffect == 2) {
              LPRender.cosine(colors, nextBar.lp, nextBarPos, cosineFreq, 0f, 1f - fxDepth, fxDepth,
                  LXColor.Blend.MULTIPLY);
            }
          }

          if (dlb.forward) {
            pos += (baseSpeed + speed) / 100f;
          } else {
            pos -= (baseSpeed + speed) / 100f;
          }

          // System.out.println("lightbar: " + dlb.lb.barNum + " pos: " + pos);

          if (pos <= 0.0 || pos >= 1.0f) {
            needsCurrentBarUpdate = true;
          }
        }
      }
    }

    if (needsCurrentBarUpdate) {
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
      return LPRender.renderTriangle(colors, targetDlb.lp, position, slope, maxValue, blend, color, pal, palTVal);
    else if (waveform == WAVEFORM_SQUARE)
      return LPRender.renderSquare(colors, targetDlb.lp, position, width, maxValue, blend, color, pal, palTVal);
    else
      return LPRender.renderStepDecay(colors, targetDlb.lp, position, width, slope,
          maxValue, targetDlb.forward, LXColor.Blend.ADD, color, pal, palTVal);
  }
}
