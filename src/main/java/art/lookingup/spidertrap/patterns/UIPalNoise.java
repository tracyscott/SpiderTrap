package art.lookingup.spidertrap.patterns;

import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIDoubleBox;
import heronarts.p4lx.ui.component.UIIntegerBox;
import heronarts.p4lx.ui.component.UISlider;
import heronarts.lx.pattern.texture.NoisePattern;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;

/**
 * TODO(tracy): This is a separate UI implementation of the controls for PalNoise.  It is currently
 * unused because it was not possible to late bind this class to the lxpattern due to some private/protected
 * issues in LXStudio.  It should be fixed with the latest 'dev' build of LXStudio so it should be possible
 * to use this now.  Currently the UI stuff is included in PalNoise directly, but that is less than ideal
 * because it has implications for headless operations.
 */
public class UIPalNoise implements UIDeviceControls<art.lookingup.spidertrap.patterns.PalNoise> {

  private UIIntegerBox seed;
  private UIIntegerBox octaves;
  private UIDoubleBox lacunarity;
  private UIDoubleBox gain;
  private UIDoubleBox ridgeOffset;


  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice, PalNoise noise) {
    uiDevice.setLayout(UI2dContainer.Layout.HORIZONTAL);
    uiDevice.setChildSpacing(4);

    addColumn(uiDevice,
        "Zoom",
        newKnob(noise.scale),
        newDoubleBox(noise.minScale),
        controlLabel(ui, "Min"),
        newDoubleBox(noise.maxScale),
        controlLabel(ui, "Max")
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        "Dynamics",
        newKnob(noise.level),
        newKnob(noise.contrast)
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        sectionLabel("X-Pos").setBottomMargin(-6),
        newButton(noise.xMode).setBottomMargin(-6),
        newHorizontalSlider(noise.xOffset).setShowLabel(false),

        sectionLabel("Y-Pos").setMargin(-6, 0),
        newButton(noise.yMode).setBottomMargin(-6),
        newHorizontalSlider(noise.yOffset).setShowLabel(false),

        sectionLabel("Z-Pos").setMargin(-6, 0),
        newButton(noise.zMode).setBottomMargin(-6),
        newHorizontalSlider(noise.zOffset).setShowLabel(false)
    );

    addVerticalBreak(ui, uiDevice);

    addColumn(uiDevice,
        "Animate",
        new UISlider(UISlider.Direction.VERTICAL, COL_WIDTH, 116, noise.motionSpeed)
    );

    addColumn(uiDevice,
        newButton(noise.motion),
        newDoubleBox(noise.motionSpeedRange),
        newHorizontalSlider(noise.xMotion),
        newHorizontalSlider(noise.yMotion),
        newHorizontalSlider(noise.zMotion)
    ).setLeftMargin(4);

    addVerticalBreak(ui, uiDevice);

    /*
    addColumn(uiDevice,
        "Algorithm",
        newDropMenu(noise.algorithm),
        this.seed = newIntegerBox(noise.seed),
        this.octaves = newIntegerBox(noise.octaves),
        this.gain = newDoubleBox(noise.gain),
        this.lacunarity = newDoubleBox(noise.lacunarity),
        this.ridgeOffset = newDoubleBox(noise.ridgeOffset),
        newKnob(noise.pal),
        newKnob(noise.ease),
        newButton(noise.bright)
    );

    noise.algorithm.addListener((p) -> {
      updateAlgorithmControls(noise);
    });
    updateAlgorithmControls(noise);

     */
  }

  private void updateAlgorithmControls(NoisePattern noise) {
    NoisePattern.Algorithm algorithm = noise.algorithm.getEnum();
    this.seed.setVisible(algorithm == NoisePattern.Algorithm.PERLIN);
    this.octaves.setVisible(algorithm.isPerlinFeedback());
    this.gain.setVisible(algorithm.isPerlinFeedback());
    this.lacunarity.setVisible(algorithm.isPerlinFeedback());
    this.ridgeOffset.setVisible(algorithm == NoisePattern.Algorithm.RIDGE);
  }

}
