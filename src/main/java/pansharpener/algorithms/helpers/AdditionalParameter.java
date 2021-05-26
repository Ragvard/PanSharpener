package pansharpener.algorithms.helpers;

import pansharpener.gui.GUI;
import pansharpener.gui.blocks.ParameterBlock;

public class AdditionalParameter {
    private final String label;
    private final double minValue;
    private final double maxValue;
    private final double defValue;
    private final double stepSize;

    public AdditionalParameter(String label, double defValue, double minValue, double maxValue,  double stepSize) {
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defValue = defValue;
        this.stepSize = stepSize;
    }

    public String getLabel() {
        return label;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getDefValue() {
        return defValue;
    }

    public double getStepSize() {
        return stepSize;
    }
}
