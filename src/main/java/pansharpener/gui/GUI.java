package pansharpener.gui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import pansharpener.algorithms.CombineAlgorithm;
import pansharpener.algorithms.GenericAlgorithm;
import pansharpener.algorithms.MeanAlgorithm;
import pansharpener.algorithms.helpers.ReaderHelper;
import pansharpener.gui.blocks.DataBlock;

public class GUI extends JFrame {
    private JPanel panelMain;
    private JPanel panelAlgorithm;
    private JComboBox comboBoxAlgorithm;
    private JButton buttonMerge;
    private JPanel panelSettings;
    private JPanel panelDataGeneral;
    private JPanel panelData1;
    private JButton buttonData1;
    private JLabel labelDataPath1;
    private JLabel labelDataW1;
    private JLabel labelDataH1;
    private JLabel labelDataName1;
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

    final FileChooser fileChooser = new FileChooser();


    private List<GenericAlgorithm> algorithms;
    private List<DataBlock> dataBlocks;


    public GUI() throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException {

        super("PanSharpener");

        algorithms = new ArrayList<>();
        algorithms.add(new CombineAlgorithm());
        algorithms.add(new MeanAlgorithm());

        createDataBlocks();
        updateDataBlocks();

        comboBoxAlgorithm.addActionListener(e -> updateDataBlocks());

        dataBlocks.forEach(o -> o.addListener(fileChooser));

        buttonMerge.addActionListener(e -> {
            int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
            GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);

            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try {
                    String pathResult = file.getPath();
                    List<String> inputs = new ArrayList<>();
                    for (int i = 0; i < currentAlgorithm.getNumberOfBands(); i++) {
                        inputs.add(dataBlocks.get(i).getFullPath());
                    }
                    inputs.add(pathResult);

                    currentAlgorithm.start(inputs);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        FileNameExtensionFilter filter = new FileNameExtensionFilter("GeoTIFF files", "tiff", "tif");
        fileChooser.setFileFilter(filter);

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
        this.setVisible(true);
    }

    private void createDataBlocks() {
        dataBlocks = new ArrayList<>();
        dataBlocks.add(new DataBlock(
                panelData1,
                buttonData1,
                labelDataName1,
                labelDataPath1,
                labelDataW1,
                labelDataH1
        ));
        dataBlocks.add(new DataBlock(
                panelData2,
                buttonData2,
                labelDataName2,
                labelDataPath2,
                labelDataW2,
                labelDataH2
        ));
        dataBlocks.add(new DataBlock(
                panelData3,
                buttonData3,
                labelDataName3,
                labelDataPath3,
                labelDataW3,
                labelDataH3
        ));
        dataBlocks.add(new DataBlock(
                panelData4,
                buttonData4,
                labelDataName4,
                labelDataPath4,
                labelDataW4,
                labelDataH4
        ));
    }

    private void updateDataBlocks() {
        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);

        int numberOfBands = currentAlgorithm.getNumberOfBands();
        List<String> bandNames = currentAlgorithm.getBandNames();

        for (int i = 0; i < 4; i++) {
            if (i < numberOfBands) {
                dataBlocks.get(i).setVisible(true, bandNames.get(i));
            }
            else {
                dataBlocks.get(i).setVisible(false);
            }
        }
    }


}
