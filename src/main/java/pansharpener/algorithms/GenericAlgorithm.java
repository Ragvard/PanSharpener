package pansharpener.algorithms;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.media.jai.Interpolation;
import javax.swing.JOptionPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.helpers.AdditionalParameter;
import pansharpener.algorithms.helpers.AlgorithmWorker;
import pansharpener.gui.GUI;

public abstract class GenericAlgorithm {
    protected List<String> paths;
    protected int interpolationType;
    protected GUI ui;
    protected AlgorithmWorker worker;
    abstract public String[] getBandNames();
    abstract public boolean[] getUsedBands();
    abstract public boolean[] getRequiredBands();
    abstract public AdditionalParameter[] getParameters();


    abstract public void start(List<String> paths, int interpolationType, List<Double> parameters, GUI ui) throws IOException;

    protected void displayMessage(String text, String title, int type) {
        JOptionPane.showMessageDialog(ui, text, title, type);
    }

    protected static void WriteImage(String pathResult, Envelope env, RenderedImage imagePan, WritableRaster raster)
            throws IOException {
        ComponentColorModel cm = (ComponentColorModel) imagePan.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable<String, Object> properties = new Hashtable<>();
        String[] keys = imagePan.getPropertyNames();
        if (keys != null) {
            for (String key : keys) {
                properties.put(key, imagePan.getProperty(key));
            }
        }

        ColorSpace instance = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

        ComponentColorModel componentColorModel = new ComponentColorModel(
                instance,
                cm.hasAlpha(),
                cm.isAlphaPremultiplied(),
                cm.getTransparency(),
                cm.getTransferType());


        BufferedImage bufferedImage = new BufferedImage(componentColorModel, raster, isAlphaPremultiplied, properties);

        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D newCoverage = factory.create("GridCoverage", bufferedImage, env);
        final File writeFile = new File(pathResult);
        final GeoTiffWriter writer = new GeoTiffWriter(writeFile);

        writer.write(newCoverage, null);
        writer.dispose();
        newCoverage.dispose(true);
    }

    protected static GridCoverage2D rescale(GridCoverage2D baseCoverage, GridCoverage2D targetCoverage,
                                         int interpolationType) {
        RenderedImage baseImage = targetCoverage.getRenderedImage();
        double baseScale = (targetCoverage.getEnvelope2D().getMaxY() - targetCoverage.getEnvelope2D().getMinY())
                / baseImage.getHeight();

        RenderedImage targetImage = baseCoverage.getRenderedImage();
        double targetScale = (baseCoverage.getEnvelope2D().getMaxY() - baseCoverage.getEnvelope2D().getMinY())
                / targetImage.getHeight();

        double scalingFactor = targetScale / baseScale;

        if (scalingFactor != 1d) {
            GridCoverage2D resampledCoverage = (GridCoverage2D) Operations.DEFAULT.resample(baseCoverage, baseCoverage.getCoordinateReferenceSystem(),
                    targetCoverage.getGridGeometry(), Interpolation.getInstance(interpolationType));
            baseCoverage.dispose(true);
            return resampledCoverage;
        }

        return baseCoverage;
    }

    public void clearWorker() {
        worker = null;
        System.gc();
    }

    public static Map<String, String> getInfo(File file) throws IOException {
        Map<String, String> result = new HashMap<>();

        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D coveragePan = reader.read(null);
        coveragePan.getGridGeometry().getGridRange2D();
        result.put("Height", String.valueOf(coveragePan.getGridGeometry().getGridRange2D().height));
        result.put("Width", String.valueOf(coveragePan.getGridGeometry().getGridRange2D().width));

        return result;
    }
}
