package pansharpener.gui.blocks;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import pansharpener.algorithms.helpers.AdditionalParameter;

public class ParameterBlock {
    private final JPanel panel;
    private final JLabel label;
    private final JSpinner spinner;
    private Boolean valid = false;

    public ParameterBlock(JPanel panel, JLabel label, JSpinner spinner) {
        this.panel = panel;
        this.label = label;
        this.spinner = spinner;
    }

    public void setVisible(boolean flag) {
        panel.setVisible(flag);
        valid = flag;
    }

    public void setVisible(boolean flag, AdditionalParameter additionalParameter) {
        setVisible(flag);
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

    public Boolean isValid() {
        return valid;
    }

    public double getValue() {
        return (double) spinner.getValue();
    }
}
