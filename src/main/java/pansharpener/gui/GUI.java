package pansharpener.gui;

import java.awt.HeadlessException;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.AlgorithmBrovey;
import pansharpener.algorithms.AlgorithmCombine;
import pansharpener.algorithms.AlgorithmMax;
import pansharpener.algorithms.GenericAlgorithm;
import pansharpener.algorithms.AlgorithmMean;
import pansharpener.algorithms.helpers.ReaderHelper;
import pansharpener.gui.blocks.DataBlock;
import static pansharpener.algorithms.GenericAlgorithm.WriteImage;
import static pansharpener.algorithms.GenericAlgorithm.rescale;

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
    private JRadioButton radioButton4;
    private JButton buttonClear1;
    private JButton buttonClear2;
    private JButton buttonClear3;
    private JButton buttonClear4;
    private JPanel panelInterpolationType;
    private ButtonGroup radioButtonGroup;

    final FileChooser fileChooser = new FileChooser();

    private List<GenericAlgorithm> algorithms;
    private List<DataBlock> dataBlocks;

    public GUI() throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException {

        super("PanSharpener");

        algorithms = new ArrayList<>();
        algorithms.add(new AlgorithmCombine());
        algorithms.add(new AlgorithmMean());
        algorithms.add(new AlgorithmMax());
        algorithms.add(new AlgorithmBrovey());

        createDataBlocks();
        updateDataBlocks();

        comboBoxAlgorithm.addActionListener(e -> updateDataBlocks());

        dataBlocks.forEach(o -> o.addListener(fileChooser));

        buttonMerge.addActionListener(e -> {
            if (!checkReadiness()) return;

            int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
            GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);

            int interpolationType = Integer.parseInt(radioButtonGroup.getSelection().getActionCommand());

            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                startThread(currentAlgorithm, file, dataBlocks, interpolationType);
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
                buttonClear1,
                labelDataName1,
                labelDataPath1,
                labelDataW1,
                labelDataH1
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

    private Boolean checkReadiness() {
        int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
        GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
        int numberOfBands = currentAlgorithm.getNumberOfBands();

        for (int i = 0; i < numberOfBands; i++) {
            if (!dataBlocks.get(i).isValid()) {
                return false;
            }
        }

        return true;
    }

    private static void startThread(GenericAlgorithm currentAlgorithm, File file, List<DataBlock> dataBlocks, int interpolationType)
    {

        SwingWorker<String, Integer> worker = new SwingWorker<>()
        {

            @Override
            protected String doInBackground() throws Exception
            {
                    try {
                        String pathResult = file.getPath();
                        List<String> inputs = new ArrayList<>();
                        for (int i = 0; i < currentAlgorithm.getNumberOfBands(); i++) {
                            inputs.add(dataBlocks.get(i).getFullPath());
                        }
                        inputs.add(pathResult);

                        publish(-3);

                        File filePan = new File(inputs.get(0));
                        File fileRed = new File(inputs.get(1));
                        File fileGreen = new File(inputs.get(2));
                        File fileBlue = new File(inputs.get(3));

                        GeoTiffReader reader = new GeoTiffReader(filePan);
                        GridCoverage2D coveragePan = reader.read(null);
                        Envelope env = coveragePan.getEnvelope();
                        RenderedImage imagePan = coveragePan.getRenderedImage();
                        Raster rasterPan = imagePan.getData();

                        DataBuffer bufferPan = rasterPan.getDataBuffer();

                        publish(-2);


                        reader = new GeoTiffReader(fileRed);
                        DataBuffer bufferRed = rescale(reader.read(null), coveragePan, interpolationType)
                                .getRenderedImage().getData().getDataBuffer();

                        reader = new GeoTiffReader(fileGreen);
                        DataBuffer bufferGreen = rescale(reader.read(null), coveragePan, interpolationType)
                                .getRenderedImage().getData().getDataBuffer();

                        reader = new GeoTiffReader(fileBlue);
                        DataBuffer bufferBlue = rescale(reader.read(null), coveragePan, interpolationType)
                                .getRenderedImage().getData().getDataBuffer();

                        int w = rasterPan.getWidth();
                        int h = rasterPan.getHeight();

                        publish(-1);

                        // TODO OutOfMemoryError
                        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, w, h, 3, null);

                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                int r = bufferRed.getElem(x + y * w);
                                int g = bufferGreen.getElem(x + y * w);
                                int b = bufferBlue.getElem(x + y * w);
                                int p = bufferPan.getElem(x + y * w);

                                int[] arr = new int[3];
                                arr[0] = (r + p) / 2;
                                arr[1] = (g + p) / 2;
                                arr[2] = (b + p) / 2;

                                raster.setPixel(x, y, arr);
                            }
                            publish((int) (x * 100d / w));
                        }

                        WriteImage(inputs.get(4), env, imagePan, raster);
                        System.gc();
                        System.out.println("Finished");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    return "Oof";
            }



            @Override
            protected void process(List<Integer> chunks)
            {
                // define what the event dispatch thread
                // will do with the intermediate results received
                // while the thread is executing
                int val = (int) chunks.get(chunks.size() - 1);
                if (val == -3) System.out.println("Чтение файлов");
                else if (val == -2) System.out.println("Ресамплинг");
                else if (val == -1) System.out.println("Создание растра");
                else {
                    System.out.println(val);
                }
            }

            @Override
            protected void done()
            {
                // this method is called when the background
                // thread finishes execution
                try
                {
                    String statusMsg = get();
                    System.out.println("Inside done function");

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

}


//        buttonMerge.addActionListener(e -> {
//            if (!checkReadiness()) return;
//
//            int selectedIndex = comboBoxAlgorithm.getSelectedIndex();
//            GenericAlgorithm currentAlgorithm = algorithms.get(selectedIndex);
//
//            int interpolationType = Integer.parseInt(radioButtonGroup.getSelection().getActionCommand());
//
//            int returnVal = fileChooser.showOpenDialog(null);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                File file = fileChooser.getSelectedFile();
//
//                try {
//                    String pathResult = file.getPath();
//                    List<String> inputs = new ArrayList<>();
//                    for (int i = 0; i < currentAlgorithm.getNumberOfBands(); i++) {
//                        inputs.add(dataBlocks.get(i).getFullPath());
//                    }
//                    inputs.add(pathResult);
//
//                    currentAlgorithm.start(inputs, interpolationType);
//                    System.gc();
//                    System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
//                    System.out.println("Finished");
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            }
//        });