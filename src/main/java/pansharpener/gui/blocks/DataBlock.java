package pansharpener.gui.blocks;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pansharpener.algorithms.GenericAlgorithm;

public class DataBlock {
    private final JPanel panel;
    private final JButton buttonSelect;
    private final JButton buttonClear;
    private final JLabel name;
    private final JLabel path;
    private final JLabel w;
    private final JLabel h;

    private String fullPath;
    private Boolean valid = false;

    public DataBlock(JPanel panel, JButton buttonSelect, JButton buttonClear, JLabel name, JLabel path, JLabel w, JLabel h) {
        this.panel = panel;
        this.buttonSelect = buttonSelect;
        this.buttonClear = buttonClear;
        this.name = name;
        this.path = path;
        this.w = w;
        this.h = h;
    }

    public void setVisible(boolean flag) {
        setVisible(flag, "", false);
    }

    public void setVisible(boolean flag, String newName, boolean isRequired) {
        panel.setVisible(flag);
        name.setText(newName);
        if (isRequired) {
            name.setForeground(Color.BLACK);
            panel.setToolTipText("Required input");
        }
        else {
            name.setForeground(Color.DARK_GRAY);
            panel.setToolTipText("Optional input");
        }
        if(!flag) clear();
    }

    public void addListener(JFileChooser fileChooser) {
        buttonSelect.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    Map<String, String> info = GenericAlgorithm.getInfo(file);
                    path.setText(file.getName());
                    fullPath = file.getPath();
                    w.setText("W:" + info.get("Width"));
                    h.setText("H:" + info.get("Height"));
                    valid = true;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    clear();
                }
            }
        });

        buttonClear.addActionListener(e -> clear());
    }

    private void clear() {
        path.setText("No data selected");
        fullPath = "";
        w.setText("W:");
        h.setText("H:");
        valid = false;
    }

    public String getFullPath() {
        return fullPath;
    }

    public Boolean isValid() {
        return valid;
    }
}
