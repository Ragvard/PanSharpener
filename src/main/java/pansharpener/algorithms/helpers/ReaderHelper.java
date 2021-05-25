package pansharpener.algorithms.helpers;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public class ReaderHelper {
    public static Map<String, String> getInfo(File file) throws IOException {
        Map<String, String> result = new HashMap<>();

        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D coveragePan = reader.read(null);
        RenderedImage renderedImage = coveragePan.getRenderedImage();

        result.put("Height", String.valueOf(renderedImage.getHeight()));
        result.put("Width", String.valueOf(renderedImage.getWidth()));

        return result;
    }
}
