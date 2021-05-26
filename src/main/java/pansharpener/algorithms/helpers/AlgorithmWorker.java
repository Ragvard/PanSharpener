package pansharpener.algorithms.helpers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import pansharpener.gui.GUI;

public abstract class AlgorithmWorker extends SwingWorker<Void, Action> {
    protected GUI ui;

    @Override
    protected void process(List<Action> chunks) {
        chunks.get(chunks.size() - 1).updateProgress(ui);
    }

    @Override
    protected void done() {
        try {
            get();
            ui.buttonMergeSetEnabled(true);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
