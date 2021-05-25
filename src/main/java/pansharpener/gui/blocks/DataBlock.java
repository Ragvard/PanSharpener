package pansharpener.gui.blocks;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pansharpener.algorithms.helpers.ReaderHelper;

public class DataBlock {
    private JPanel panel;
    private JButton button;
    private JLabel name;
    private JLabel path;
    private JLabel w;
    private JLabel h;

    private String fullPath;
    private Boolean valid = false;

    public DataBlock(JPanel panel, JButton button, JLabel name, JLabel path, JLabel w, JLabel h) {
        this.panel = panel;
        this.button = button;
        this.name = name;
        this.path = path;
        this.w = w;
        this.h = h;
    }

    public void setVisible(boolean flag) {
        setVisible(flag, "");
    }

    public void setVisible(boolean flag, String newName) {
        panel.setVisible(flag);
        name.setText(newName);
        clear();
    }

    public void addListener(JFileChooser fileChooser) {
        button.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try {
                    Map<String, String> info = ReaderHelper.getInfo(file);
                    path.setText(file.getName());
                    fullPath = file.getPath();
                    w.setText("W:" + info.get("Width"));
                    h.setText("H:" + info.get("Height"));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    clear();
                }
            }
        });
    }

    private void clear() {
        path.setText("No data selected");
        fullPath = "";
        w.setText("");
        h.setText("");
        valid = false;
    }

    public String getFullPath() {
        return fullPath;
    }

    public Boolean isValid() {
        return valid;
    }
}
