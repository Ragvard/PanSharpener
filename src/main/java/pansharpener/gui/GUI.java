package pansharpener.gui;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import pansharpener.algorithms.AlgorithmBrovey;
import pansharpener.algorithms.AlgorithmCombine;
import pansharpener.algorithms.AlgorithmMax;
import pansharpener.algorithms.GenericAlgorithm;
import pansharpener.algorithms.AlgorithmMean;
import pansharpener.algorithms.helpers.AdditionalParameter;
import pansharpener.gui.blocks.DataBlock;
import pansharpener.gui.blocks.ParameterBlock;

public class GUI extends JFrame {
    private class FileChooser extends JFileChooser {
        public FileChooser() throws ClassNotFoundException, UnsupportedLookAndFeelException,
                InstantiationException, IllegalAccessException {
            this(null);
        }

        public FileChooser(String path) throws ClassNotFoundException, UnsupportedLookAndFeelException,
                InstantiationException, IllegalAccessException {
            super(path);
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        }
    }

    private JPanel panelMain;
    private JPanel panelAlgorithm;
    private JComboBox comboBoxAlgorithm;
    private JButton buttonMerge;
    private JPanel panelSettings;
    private JPanel panelDataGeneral;
    private JPanel panelDataPan;
    private JButton buttonDataPan;
    private JLabel labelDataPathPan;
    private JLabel labelDataWPan;
    private JLabel labelDataHPan;
    private JLabel labelDataNamePan;
    private JPanel panelData2;
    private JButton buttonData2;
    private JLabel labelDataName2;
    private JLabel labelDataPath2;
    private JLabel labelDataW2;
    private JLabel labelDataH2;
    private JPanel panelData3;
    private JButton buttonData3;
    private JLabel labelDataName3;
    private JLabel labelDataPath3;
    private JLabel labelDataW3;
    private JLabel labelDataH3;
    private JPanel panelData4;
    private JButton buttonData4;
    private JLabel labelDataName4;
    private JLabel labelDataPath4;
    private JLabel labelDataW4;
    private JLabel labelDataH4;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JRadioButton radioButton3;
    private JRadioButton radioButton4;
    private JButton buttonClearPan;
    private JButton buttonClear2;
    private JButton buttonClear3;
    private JButton buttonClear4;
    private JPanel panelInterpolationType;
    private JProgressBar progressBar;
    private JPanel panelData5;
    private JButton buttonData5;
    private JButton buttonClear5;
    private JLabel labelDataPath5;
    private JLabel labelDataW5;
    private JLabel labelDataH5;
    private JLabel labelDataName5;
    private JLabel labelAction;
    private JSpinner spinner1;
    private JLabel labelSpinner1;
    private JPanel panelParameters;
    private JSpinner spinner2;
    private JSpinner spinner3;
    private JSpinner spinner5;
    private JSpinner spinner4;
    private JSpinner spinner6;
    private JLabel labelSpinner2;
    private JLabel labelSpinner3;
    private JLabel labelSpinner5;
    private JLabel labelSpinner4;
    private JLabel labelSpinner6;
    private JPanel panelSpinner1;
    private JPanel panelSpinner2;
    private JPanel panelSpinner3;
    private JPanel panelSpinner4;
    private JPanel panelSpinner5;
    private JPanel panelSpinner6;
    private ButtonGroup radioButtonGroup;

    final FileChooser fileChooser = new FileChooser();

    private List<GenericAlgorithm> algorithms;
    private List<DataBlock> dataBlocks;
    private List<ParameterBlock> parameterBlocks;


    public GUI() throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException {
        super("PanSharpener");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("GeoTIFF files", "tiff", "tif");
        fileChooser.setFileFilter(filter);

        createAlgorithms();
        createDataBlocks();
        createParameterBlocks();

        createListeners();

        setLookAndFeel();
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
        updateDataBlocks();
        updateParameterBlocks();
        this.setMinimumSize(getPreferredSize());
        this.setSize(this.getMinimumSize());
        this.setVisible(true);
    }


    public void setProgress(int progress) {
        this.progressBar.setValue(progress);
    }

    public void setCurrentAction(String action) {
        labelAction.setText(action);
    }

    public void buttonMergeSetEnabled(boolean flag) {
        buttonMerge.setEnabled(flag);
    }


    private void createDataBlocks() {
        dataBlocks = new ArrayList<>();
        dataBlocks.add(new DataBlock(
                panelDataPan,
                buttonDataPan,
                buttonClearPan,
                labelDataNamePan,
                labelDataPathPan,
                labelDataWPan,
                labelDataHPan
        ));
        dataBlocks.add(new DataBlock(
                panelData2,
                buttonData2,
                buttonClear2,
                labelDataName2,
                labelDataPath2,
                labelDataW2,
                labelDataH2
        ));
        dataBlocks.add(new DataBlock(
                panelData3,
                buttonData3,
                buttonClear3,
                labelDataName3,
                labelDataPath3,
                labelDataW3,
                labelDataH3
        ));
        dataBlocks.add(new DataBlock(
                panelData4,
                buttonData4,
                buttonClear4,
                labelDataName4,
                labelDataPath4,
                labelDataW4,
                labelDataH4
        ));
        dataBlocks.add(new DataBlock(
                panelData5,
                buttonData5,
                buttonClear5,
                labelDataName5,
                labelDataPath5,
                labelDataW5,
                labelDataH5
        ));
    }

    private void createParameterBlocks() {
        parameterBlocks = new ArrayList<>();
        parameterBlocks.add(new ParameterBlock(
                panelSpinner1,
                labelSpinner1,
                spinner1
        ));
        parameterBlocks.add(new ParameterBlock(
                panelSpinner2,
                labelSpinner2,
                spinner2
        ));
        parameterBlocks.add(new ParameterBlock(
                panelSpinner3,
                labelSpinner3,
                spinner3
        ));
        parameterBlocks.add(new ParameterBlock(
                panelSpinner4,
                labelSpinner4,
                spinner4
        ));
        parameterBlocks.add(new ParameterBlock(
                panelSpinner5,
                labelSpinner5,
                spinner5
        ));
        parameterBlocks.add(new ParameterBlock(
                panelSpinner6,
                labelSpinner6,
                spinner6
        ));
    }

    private void createAlgorithms() {
        algorithms = new ArrayList<>();
        algorithms.add(new AlgorithmCombine());
        algorithms.add(new AlgorithmMean());
        algorithms.add(new AlgorithmMax());
        algorithms.add(new AlgorithmBrovey());
    }

    private void createListeners() {
        comboBoxAlgorithm.addActionListener(e -> {
            updateDataBlocks();
            updateParameterBlocks();
        });

        dataBlocks.forEach(o -> o.addListener(fileChooser));

        buttonMerge.addActionListener(e -> mergeImages());
    }

    private void updateDataBlocks() {
        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        if (selectedIndex == -1) {
            dataBlocks.forEach(o -> o.setVisible(false));
            return;
        }

        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
        String[] bandNames = currentAlgorithm.getBandNames();
        Boolean[] usedBands = currentAlgorithm.getUsedBands();

        for (int i = 0; i < 5; i++) {
            dataBlocks.get(i).setVisible(usedBands[i], bandNames[i]);
        }
    }

    private void updateParameterBlocks() {
        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        if (selectedIndex == -1) {
            parameterBlocks.forEach(o -> o.setVisible(false));
            return;
        }

        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
        AdditionalParameter[] parameters = currentAlgorithm.getParameters();

        int i = 0;
        for (; i < parameters.length; i++) {
            parameterBlocks.get(i).setVisible(true, parameters[i]);
        }
        for(;i < 6; i++) {
            parameterBlocks.get(i).setVisible(false);
        }
    }


    private void mergeImages() {
        if (!checkReadiness()) {
            JOptionPane.showMessageDialog(this,
                    "You need to select all required input data to perform pansharpening.",
                    "Tip",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
        Boolean[] usedBands = currentAlgorithm.getUsedBands();
        int interpolationType = Integer.parseInt(radioButtonGroup.getSelection().getActionCommand());

        int returnVal = fileChooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                List<String> inputs = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    if (usedBands[i] & dataBlocks.get(i).isValid()) {
                        inputs.add(dataBlocks.get(i).getFullPath());
                    }
                }
                String pathResult = file.getPath();
                inputs.add(pathResult);

                List<Double> parameters = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    if (parameterBlocks.get(i).isValid()) {
                        parameters.add(parameterBlocks.get(i).getValue());
                    }
                }

//                buttonMergeSetEnabled(false);
                createAlgorithms();
                currentAlgorithm.start(inputs, interpolationType, parameters, this);
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(this,
                        "Unable to save to specified file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Boolean checkReadiness() {
        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
        Boolean[] requiredBands = currentAlgorithm.getRequiredBands();


        for (int i = 0; i < 5; i++) {
            if (requiredBands[i] & !dataBlocks.get(i).isValid()) {
                return false;
            }
        }

        return true;
    }

    private void setLookAndFeel() throws ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (IllegalAccessException | InstantiationException |
                UnsupportedLookAndFeelException | ClassNotFoundException e) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
    }
}