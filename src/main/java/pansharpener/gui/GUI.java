package pansharpener.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import pansharpener.gui.blocks.DataBlock;

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
    private ButtonGroup radioButtonGroup;

    final FileChooser fileChooser = new FileChooser();

    private List<GenericAlgorithm> algorithms;
    private List<DataBlock> dataBlocks;

    public GUI() throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException {
        super("PanSharpener");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("GeoTIFF files", "tiff", "tif");
        fileChooser.setFileFilter(filter);

        algorithms = new ArrayList<>();
        algorithms.add(new AlgorithmCombine());
        algorithms.add(new AlgorithmMean());
        algorithms.add(new AlgorithmMax());
        algorithms.add(new AlgorithmBrovey());

        createDataBlocks();

        comboBoxAlgorithm.addActionListener(e -> updateDataBlocks());

        dataBlocks.forEach(o -> o.addListener(fileChooser));

        buttonMerge.addActionListener(e -> {
            if (!checkReadiness()) return;

            int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
            GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
            Boolean[] usedBands = currentAlgorithm.getUsedBands();
            int interpolationType = Integer.parseInt(radioButtonGroup.getSelection().getActionCommand());

            int returnVal = fileChooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try {
                    String pathResult = file.getPath();
                    List<String> inputs = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        if (usedBands[i] & dataBlocks.get(i).isValid()) {
                            inputs.add(dataBlocks.get(i).getFullPath());
                        }
                    }
                    inputs.add(pathResult);

                    buttonMergeSetEnabled(false);
                    currentAlgorithm.start(inputs, interpolationType, this);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
        updateDataBlocks();
        this.setMinimumSize(getPreferredSize());
        this.setSize(this.getMinimumSize());
        this.setVisible(true);



        panelMain.addComponentListener(new ResizeListener());
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

        // updateSize();
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

    private void updateSize() {
         pack();
         setMinimumSize(getPreferredSize());
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

    class ResizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            System.out.println(getSize());
            System.out.println();
            //System.out.println(getSize());
        }
    }
}