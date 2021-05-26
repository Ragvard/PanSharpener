package pansharpener.algorithms;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.helpers.Action;
import pansharpener.algorithms.helpers.AdditionalParameter;
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
    public Boolean[] getUsedBands() {
        return new Boolean[] {
                true,
                true,
                true,
                true,
                true
        };
    }

    @Override
    public Boolean[] getRequiredBands() {
        return new Boolean[] {
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
            if (parameters.size() != 3) {
                displayMessage("Invalid number of parameters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ui.buttonMergeSetEnabled(false);
            execute();
        } else if (numberOfInputs == 6) {
            if (parameters.size() != 4) {
                displayMessage("Invalid number of parameters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // TODO NIR вариант
            System.out.println("Заглушка для NIR");
        } else {
            displayMessage("Invalid number of inputs", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected String doInBackground() throws IOException {
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

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int r = bufferRed.getElem(x + y * w);
                int g = bufferGreen.getElem(x + y * w);
                int b = bufferBlue.getElem(x + y * w);
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

        return "Done";
    }

}
