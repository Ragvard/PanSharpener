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

public class AlgorithmIHS extends GenericAlgorithm {

    @Override
    public String[] getBandNames() {
        return new String[]{
                "Panchromatic Band",
                "Red Band",
                "Green Band",
                "Blue Band",
                "Near-Infrared Band"
        };
    }

    @Override
    public boolean[] getUsedBands() {
        return new boolean[]{
                true,
                true,
                true,
                true,
                true
        };
    }

    @Override
    public boolean[] getRequiredBands() {
        return new boolean[]{
                true,
                true,
                true,
                true,
                false
        };
    }

    @Override
    public AdditionalParameter[] getParameters() {
        return new AdditionalParameter[]{
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
            mergenonir(paths, interpolationType, parameters);
        } else if (numberOfInputs == 6) {
            ui.buttonMergeSetEnabled(false);
            mergenir(paths, interpolationType, parameters);
        } else {
            displayMessage("Invalid number of inputs", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mergenir(List<String> paths, int interpolationType, List<Double> parameters) {
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

                int width = rasterPan.getWidth();
                int height = rasterPan.getHeight();

                publish(new Action("Preprocessing: Creating Raster...", 80));
                WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, width, height, 3, null);

                double rw = parameters.get(0);
                double gw = parameters.get(1);
                double bw = parameters.get(2);
                double irw = parameters.get(3);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        double r = (bufferRed.getElem(x + y * width) * rw) / 65536d;
                        double g = (bufferGreen.getElem(x + y * width) * gw) / 65536d;
                        double b = (bufferBlue.getElem(x + y * width) * bw) / 65536d;
                        double ir = (bufferNir.getElem(x + y * width) * irw) / 65536d;
                        double p = bufferPan.getElem(x + y * width) / 65536d;

                        double ma = Math.max(Math.max(r, g), b);
                        double mi = Math.min(Math.min(r, g), b);
                        double delta = ma - mi;
                        double h;
                        double s;
                        double v;

                        if (delta > 0) {
                            if (ma == r) {
                                h = 60 * (((g - b) / delta) % 6d);
                            } else if (ma == g) {
                                h = 60 * (((b - r) / delta) + 2);
                            } else { // if (ma == b)
                                h = 60 * (((r - g) / delta) + 4);
                            }

                            if (ma > 0) {
                                s = delta / ma;
                            } else {
                                s = 0;
                            }
                            v = ma;
                        } else {
                            h = 0;
                            s = 0;
                            v = ma;
                        }

                        if (h < 0) {
                            h = 360 + h;
                        }

                        //////////////////////////

                        v = p - ir;

                        ////////////////////////
                        double c = v * s; // Chroma
                        double fHPrime = (h / 60.0) % 6;
                        double X = c * (1 - Math.abs((fHPrime % 2) - 1));
                        double M = v - c;

                        if (0 <= fHPrime && fHPrime < 1) {
                            r = c;
                            g = X;
                            b = 0;
                        } else if (1 <= fHPrime && fHPrime < 2) {
                            r = X;
                            g = c;
                            b = 0;
                        } else if (2 <= fHPrime && fHPrime < 3) {
                            r = 0;
                            g = c;
                            b = X;
                        } else if (3 <= fHPrime && fHPrime < 4) {
                            r = 0;
                            g = X;
                            b = c;
                        } else if (4 <= fHPrime && fHPrime < 5) {
                            r = X;
                            g = 0;
                            b = c;
                        } else if (5 <= fHPrime && fHPrime < 6) {
                            r = c;
                            g = 0;
                            b = X;
                        } else {
                            r = 0;
                            g = 0;
                            b = 0;
                        }

                        r += M;
                        g += M;
                        b += M;


                        r = r * 65536;
                        b = b * 65536;
                        g = g * 65536;

                        int[] arr = new int[3];
                        arr[0] = (int) r;
                        arr[1] = (int) g;
                        arr[2] = (int) b;
                        raster.setPixel(x, y, arr);
                    }
                    publish(new Action("Pansharpening...", (int) (x * 100d / width)));
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

    private void mergenonir(List<String> paths, int interpolationType, List<Double> parameters) {
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

                int width = rasterPan.getWidth();
                int height = rasterPan.getHeight();

                publish(new Action("Preprocessing: Creating Raster...", 75));
                WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, width, height, 3, null);

                double rw = parameters.get(0);
                double gw = parameters.get(1);
                double bw = parameters.get(2);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        double r = (bufferRed.getElem(x + y * width) * rw) / 65536d;
                        double g = (bufferGreen.getElem(x + y * width) * gw) / 65536d;
                        double b = (bufferBlue.getElem(x + y * width) * bw) / 65536d;
                        double p = bufferPan.getElem(x + y * width) / 65536d;

                        double max = Math.max(Math.max(r, g), b);
                        double min = Math.min(Math.min(r, g), b);
                        double delta = max - min;
                        double h;
                        double s;
                        double v;

                        if (delta > 0) {
                            if (max == r) {
                                h = 60 * (((g - b) / delta) % 6d);
                            } else if (max == g) {
                                h = 60 * (((b - r) / delta) + 2);
                            } else { // if (max == b)
                                h = 60 * (((r - g) / delta) + 4);
                            }
                        } else {
                            h = 0;
                        }

                        if (max == 0) {
                            s = 0;
                        } else {
                            s = delta / max;
                        }

                        if (h < 0) {
                            h = 360 + h;
                        }

                        //////////////////////////

                        v = p;

                        ////////////////////////
                        double c = v * s;
                        int fHPrime = (int) (h / 60);
                        double X = c * (1 - Math.abs(((h / 60.0) % 2d) - 1));
                        double M = v - c;

                        switch (fHPrime) {
                            case 0:
                                r = c;
                                g = X;
                                b = 0;
                                break;
                            case 1:
                                r = X;
                                g = c;
                                b = 0;
                                break;
                            case 2:
                                r = 0;
                                g = c;
                                b = X;
                                break;
                            case 3:
                                r = 0;
                                g = X;
                                b = c;
                                break;
                            case 4:
                                r = X;
                                g = 0;
                                b = c;
                                break;
                            case 5:
                                r = c;
                                g = 0;
                                b = X;
                                break;
                            default:
                                r = 0;
                                g = 0;
                                b = 0;
                                break;
                        }

                        r += M;
                        g += M;
                        b += M;

                        r = r * 65536d;
                        b = b * 65536d;
                        g = g * 65536d;

                        int[] arr = new int[3];
                        arr[0] = (int) r;
                        arr[1] = (int) g;
                        arr[2] = (int) b;
                        raster.setPixel(x, y, arr);
                    }
                    publish(new Action("Pansharpening...", (int) (x * 100d / width)));
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
}
