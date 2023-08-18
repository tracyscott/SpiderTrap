package art.lookingup.spidertrap.ui;

import art.lookingup.spidertrap.SpiderTrapApp;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UICollapsibleSection;
import heronarts.p4lx.ui.component.UIKnob;

public class UIPreviewComponents extends UICollapsibleSection {
  public UIPreviewComponents(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("Overlay");
    UI2dContainer viewToggleContainer = new UI2dContainer(0, 0, getContentWidth(), 20);
    viewToggleContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    viewToggleContainer.setPadding(10, 10, 10, 10);
    viewToggleContainer.addToContainer(this);

    UIButton showBodies = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        SpiderTrapApp.preview.showBodies = on;
      }
    }.setLabel("bodies").setActive(SpiderTrapApp.preview.showBodies);
    showBodies.setWidth(35).setHeight(16);
    showBodies.addToContainer(viewToggleContainer);

    UIButton showEdges = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        SpiderTrapApp.edgesPreview.showEdges = on;
      }
    }.setLabel("bodies").setActive(SpiderTrapApp.edgesPreview.showEdges);
    showBodies.setWidth(35).setHeight(16);
    showBodies.addToContainer(viewToggleContainer);

    UI2dContainer body1Knobs = new UI2dContainer(0, 0, getContentWidth(), 20);
    UIKnob body1X = new UIKnob(0, 0, 20, 20);

  }
}



