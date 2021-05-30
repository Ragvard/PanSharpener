package pansharpener.algorithms;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.helpers.Action;
import pansharpener.algorithms.helpers.AdditionalParameter;
import pansharpener.algorithms.helpers.AlgorithmWorker;
import pansharpener.gui.GUI;

public class AlgorithmBrovey extends GenericAlgorithm{

    @Override
    public String[] getBandNames() {
        return new String[] {
                "Panchromatic Band",
                "Red Band",
                "Green Band",
                "Blue Band",
                "Near-Infrared Band"
        };
    }

    @Override
    public boolean[] getUsedBands() {
        return new boolean[] {
                true,
                true,
                true,
                true,
                true
        };
    }

    @Override
    public boolean[] getRequiredBands() {
        return new boolean[] {
                true,
                true,
                true,
                true,
                false
        };
    }

    @Override
    public AdditionalParameter[] getParameters() {
        return new AdditionalParameter[] {
                new AdditionalParameter(
                        "Red Band Weight",
                        1d, 0d, 2d, 0.01
                ),
                new AdditionalParameter(
                        "Green Band Weight",
                        1d, 0d, 2d, 0.01
                ),
                new AdditionalParameter(
                        "Blue Band Weight",
                        1d, 0d, 2d, 0.01
                ),
                new AdditionalParameter(
                        "Near-Infrared Band Weight",
                        0.1, 0d, 1d, 0.01
                )
        };
    }

    @Override
    public void start(List<String> paths, int interpolationType, List<Double> parameters, GUI ui) {
        this.paths = paths;
        this.interpolationType = interpolationType;
        this.ui = ui;
        int numberOfInputs = paths.size();

        if (numberOfInputs == 5) {
            ui.buttonMergeSetEnabled(false);
            mergeNoNir(paths, interpolationType, parameters);
        } else if (numberOfInputs == 6) {
            ui.buttonMergeSetEnabled(false);
            mergeNir(paths, interpolationType, parameters);
        } else {
            displayMessage("Invalid number of inputs", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mergeNoNir(List<String> paths, int interpolationType, List<Double> parameters) {
        worker = new AlgorithmWorker(ui, this) {
            @Override
            protected Void doInBackground() throws Exception {
                publish(new Action("Preprocessing: Reading Files...", 0));
                File filePan = new File(paths.get(0));
                File fileRed = new File(paths.get(1));
                File fileGreen = new File(paths.get(2));
                File fileBlue = new File(paths.get(3));

                publish(new Action("Preprocessing: Reading Geodata...", 0));
                GeoTiffReader reader = new GeoTiffReader(filePan);
                GridCoverage2D coveragePan = reader.read(null);
                Envelope env = coveragePan.getEnvelope();
                RenderedImage imagePan = coveragePan.getRenderedImage();
                Raster rasterPan = imagePan.getData();

                DataBuffer bufferPan = rasterPan.getDataBuffer();

                publish(new Action("Preprocessing: Resampling Red Band...", 0));
                reader.dispose();
                reader = new GeoTiffReader(fileRed);
                DataBuffer bufferRed = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Resampling Green Band...", 25));
                reader.dispose();
                reader = new GeoTiffReader(fileGreen);
                DataBuffer bufferGreen = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Resampling Blue Band...", 50));
                reader.dispose();
                reader = new GeoTiffReader(fileBlue);
                DataBuffer bufferBlue = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                int w = rasterPan.getWidth();
                int h = rasterPan.getHeight();

                publish(new Action("Preprocessing: Creating Raster...", 75));
                WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, w, h, 3, null);

                double rw = parameters.get(0);
                double gw = parameters.get(1);
                double bw = parameters.get(2);

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int r = (int) (bufferRed.getElem(x + y * w) * rw);
                        int g = (int) (bufferGreen.getElem(x + y * w) * gw);
                        int b = (int) (bufferBlue.getElem(x + y * w) * bw);
                        int p = bufferPan.getElem(x + y * w);

                        float div = (r + g + b) / 3f;
                        float change = 0;
                        if (div != 0) change = p / div;

                        int[] arr = new int[3];
                        arr[0] = (int) (r * change);
                        arr[1] = (int) (g * change);
                        arr[2] = (int) (b * change);
                        raster.setPixel(x, y, arr);
                    }
                    publish(new Action("Pansharpening...", (int) (x * 100d / w)));
                }

                publish(new Action("Saving Results...", 100));

                WriteImage(paths.get(4), env, imagePan, raster);
                reader.dispose();
                coveragePan.dispose(true);

                publish(new Action("Pansharpening Complete!", 100));

                return null;
            }
        };
        worker.execute();
    }

    private void mergeNir(List<String> paths, int interpolationType, List<Double> parameters) {
        worker = new AlgorithmWorker(ui, this) {
            @Override
            protected Void doInBackground() throws Exception {
                publish(new Action("Preprocessing: Reading Files...", 0));
                File filePan = new File(paths.get(0));
                File fileRed = new File(paths.get(1));
                File fileGreen = new File(paths.get(2));
                File fileBlue = new File(paths.get(3));
                File fileNir = new File(paths.get(4));

                publish(new Action("Preprocessing: Reading Geodata...", 0));
                GeoTiffReader reader = new GeoTiffReader(filePan);
                GridCoverage2D coveragePan = reader.read(null);
                Envelope env = coveragePan.getEnvelope();
                RenderedImage imagePan = coveragePan.getRenderedImage();
                Raster rasterPan = imagePan.getData();

                DataBuffer bufferPan = rasterPan.getDataBuffer();

                publish(new Action("Preprocessing: Resampling Red Band...", 0));
                reader.dispose();
                reader = new GeoTiffReader(fileRed);
                DataBuffer bufferRed = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Resampling Green Band...", 20));
                reader.dispose();
                reader = new GeoTiffReader(fileGreen);
                DataBuffer bufferGreen = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Resampling Blue Band...", 40));
                reader.dispose();
                reader = new GeoTiffReader(fileBlue);
                DataBuffer bufferBlue = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Resampling Near-Infrared Band...", 60));
                reader.dispose();
                reader = new GeoTiffReader(fileNir);
                DataBuffer bufferNir = rescale(reader.read(null), coveragePan, interpolationType)
                        .getRenderedImage().getData().getDataBuffer();

                int w = rasterPan.getWidth();
                int h = rasterPan.getHeight();

                publish(new Action("Preprocessing: Creating Raster...", 80));
                WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, w, h, 3, null);

                double rw = parameters.get(0);
                double gw = parameters.get(1);
                double bw = parameters.get(2);
                double irw = parameters.get(3);

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int r = (int) (bufferRed.getElem(x + y * w) * rw);
                        int g = (int) (bufferGreen.getElem(x + y * w) * gw);
                        int b = (int) (bufferBlue.getElem(x + y * w) * bw);
                        int ir = (int) (bufferNir.getElem(x + y * w) * irw);
                        int p = bufferPan.getElem(x + y * w);

                        float div = (r + g + b) / 3f;
                        float change = 0;
                        if (div != 0) change = (p - ir) / div;

                        int[] arr = new int[3];
                        arr[0] = (int) (r * change);
                        arr[1] = (int) (g * change);
                        arr[2] = (int) (b * change);
                        raster.setPixel(x, y, arr);
                    }
                    publish(new Action("Pansharpening...", (int) (x * 100d / w)));
                }

                publish(new Action("Saving Results...", 100));

                WriteImage(paths.get(5), env, imagePan, raster);
                reader.dispose();
                coveragePan.dispose(true);

                publish(new Action("Pansharpening Complete!", 100));

                return null;
            }
        };
        worker.execute();
    }
}
