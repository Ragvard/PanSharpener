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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.geometry.Envelope;

public abstract class GenericAlgorithm {
    abstract public int getNumberOfBands();
    abstract public List<String> getBandNames();

    abstract public void start(List<String> paths) throws IOException;

    protected static void WriteImage(String pathResult, Envelope env, RenderedImage imagePan, WritableRaster raster) throws IOException {
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
    }
}
