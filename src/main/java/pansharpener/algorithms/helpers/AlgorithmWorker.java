package pansharpener.algorithms.helpers;

import java.awt.Taskbar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import pansharpener.algorithms.GenericAlgorithm;
import pansharpener.gui.GUI;

public class AlgorithmWorker extends SwingWorker<Void, Action> {
    private final GUI ui;
    private final GenericAlgorithm parent;

    public AlgorithmWorker(GUI ui, GenericAlgorithm parent) {
        this.ui = ui;
        this.parent = parent;
    }

    @Override
    protected void process(List<Action> chunks) {
        chunks.get(chunks.size() - 1).updateProgress(ui);
    }

    @Override
    protected void done() {
        try {
            get();
            ui.buttonMergeSetEnabled(true);
            parent.clearWorker();
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setWindowProgressValue(ui, 0);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        return null;
    }
}
