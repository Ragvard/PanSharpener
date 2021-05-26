package pansharpener.algorithms;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.media.jai.Interpolation;
import javax.swing.SwingWorker;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.geometry.Envelope;
import pansharpener.algorithms.helpers.Action;
import pansharpener.algorithms.helpers.AdditionalParameter;
import pansharpener.gui.GUI;

public abstract class GenericAlgorithm extends SwingWorker<String, Action> {
    protected List<String> paths;
    protected int interpolationType;
    protected GUI ui;

    // TODO protected
    public static void WriteImage(String pathResult, Envelope env, RenderedImage imagePan, WritableRaster raster)
            throws IOException {
        ComponentColorModel cm = (ComponentColorModel) imagePan.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable<String, Object> properties = new Hashtable<>();
        String[] keys = imagePan.getPropertyNames();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                properties.put(keys[i], imagePan.getProperty(keys[i]));
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
        newCoverage.dispose(true);
    }

    // TODO protected
    public static GridCoverage2D rescale(GridCoverage2D baseCoverage, GridCoverage2D targetCoverage,
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

    abstract public String[] getBandNames();
    abstract public Boolean[] getUsedBands();
    abstract public Boolean[] getRequiredBands();
    abstract public AdditionalParameter[] getParameters();


    abstract public void start(List<String> paths, int interpolationType, GUI ui) throws IOException;

    @Override
    protected void process(List<Action> chunks) {
        chunks.get(chunks.size() - 1).updateProgress(ui);
    }

    @Override
    protected void done() {
        try {
            String statusMsg = get();
            ui.buttonMergeSetEnabled(true);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
