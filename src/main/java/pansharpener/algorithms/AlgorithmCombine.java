package pansharpener.algorithms;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.helpers.Action;
import pansharpener.algorithms.helpers.AdditionalParameter;
import pansharpener.algorithms.helpers.AlgorithmWorker;
import pansharpener.gui.GUI;

public class AlgorithmCombine extends GenericAlgorithm{

    @Override
    public String[] getBandNames() {
        return new String[] {
                "",
                "Red Band",
                "Green Band",
                "Blue Band",
                ""
        };
    }

    @Override
    public boolean[] getUsedBands() {
        return new boolean[] {
                false,
                true,
                true,
                true,
                false
        };
    }

    @Override
    public boolean[] getRequiredBands() {
        return new boolean[] {
                false,
                true,
                true,
                true,
                false
        };
    }

    @Override
    public AdditionalParameter[] getParameters() {
        return new AdditionalParameter[0];
    }

    @Override
    public void start(List<String> paths, int interpolationType, List<Double> parameters, GUI ui){
        this.paths = paths;
        this.interpolationType = interpolationType;
        this.ui = ui;
        int numberOfInputs = paths.size();

        if (numberOfInputs == 4) {
            merge(paths);
        } else {
            throw new IllegalArgumentException("Invalid number of paths: expected 4, received  " + numberOfInputs);
        }
    }

    private void merge(List<String> paths) {
        worker = new AlgorithmWorker(ui, this) {
            @Override
            protected Void doInBackground() throws Exception {
                publish(new Action("Preprocessing: Reading Files...", 0));
                File fileRed = new File(paths.get(0));
                File fileGreen = new File(paths.get(1));
                File fileBlue = new File(paths.get(2));

                publish(new Action("Preprocessing: Reading Geodata...", 0));
                GeoTiffReader reader = new GeoTiffReader(fileRed);
                GridCoverage2D coveragePan = reader.read(null);
                Envelope env = coveragePan.getEnvelope();
                RenderedImage imagePan = coveragePan.getRenderedImage();
                Raster rasterPan = imagePan.getData();

                publish(new Action("Preprocessing: Preparing Red Band...", 0));
                reader = new GeoTiffReader(fileRed);
                DataBuffer bufferRed = reader.read(null).getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Preparing Green Band...", 25));
                reader = new GeoTiffReader(fileGreen);
                DataBuffer bufferGreen = reader.read(null).getRenderedImage().getData().getDataBuffer();

                publish(new Action("Preprocessing: Preparing Blue Band...", 50));
                reader = new GeoTiffReader(fileBlue);
                DataBuffer bufferBlue = reader.read(null).getRenderedImage().getData().getDataBuffer();

                int w = rasterPan.getWidth();
                int h = rasterPan.getHeight();

                publish(new Action("Preprocessing: Creating Raster...", 75));
                WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, w, h, 3, null);

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int r = bufferRed.getElem(x + y * w);
                        int g = bufferGreen.getElem(x + y * w);
                        int b = bufferBlue.getElem(x + y * w);

                        int[] arr = new int[3];
                        arr[0] = r;
                        arr[1] = g;
                        arr[2] = b;

                        raster.setPixel(x, y, arr);
                    }
                    publish(new Action("Pansharpening...", (int) (x * 100d / w)));
                }

                publish(new Action("Saving Results...", 100));

                WriteImage(paths.get(3), env, imagePan, raster);

                publish(new Action("Pansharpening Complete!", 100));

                return null;
            }
        };
        worker.execute();
    }
}
