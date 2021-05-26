package pansharpener.gui.blocks;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import pansharpener.algorithms.helpers.AdditionalParameter;

public class ParameterBlock {
    private JPanel panel;
    private JLabel label;
    private JSpinner spinner;

    public ParameterBlock(JPanel panel, JLabel label, JSpinner spinner) {
        this.panel = panel;
        this.label = label;
        this.spinner = spinner;
    }

    public void setVisible(boolean flag) {
        panel.setVisible(flag);
    }

    public void setVisible(boolean flag, AdditionalParameter additionalParameter) {
        panel.setVisible(flag);
        if (flag) update(additionalParameter);
    }

    public void update(AdditionalParameter additionalParameter) {
        label.setText(additionalParameter.getLabel());
        spinner.setModel(new SpinnerNumberModel(
                additionalParameter.getDefValue(),
                additionalParameter.getMinValue(),
                additionalParameter.getMaxValue(),
                additionalParameter.getStepSize()
        ));
    }

    public JSpinner getSpinner() {
        return spinner;
    }
}
