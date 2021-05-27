package pansharpener.algorithms.helpers;

import java.awt.Taskbar;
import pansharpener.gui.GUI;

public class Action {
    private final String action;
    private final int progress;

    public Action(String action, int progress) {
        this.action = action;
        this.progress = progress;
    }

    public void updateProgress(GUI ui) {
        ui.setCurrentAction(action);
        ui.setProgress(progress);
        Taskbar taskbar = Taskbar.getTaskbar();
        taskbar.setWindowProgressValue(ui, progress);
    }
}
