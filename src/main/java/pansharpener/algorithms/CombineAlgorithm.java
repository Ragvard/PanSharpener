package pansharpener.algorithms;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;

public class CombineAlgorithm extends GenericAlgorithm{
    @Override
    public int getNumberOfBands() {
        return 3;
    }

    @Override
    public List<String> getBandNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("Red");
        names.add("Green");
        names.add("Blue");
        return names;
    }

    @Override
    public void start(List<String> paths) throws IOException {
        File fileRed = new File(paths.get(0));
        File fileGreen = new File(paths.get(1));
        File fileBlue = new File(paths.get(2));

        GeoTiffReader reader = new GeoTiffReader(fileRed);
        GridCoverage2D coveragePan = reader.read(null);
        Envelope env = coveragePan.getEnvelope();
        RenderedImage imagePan = coveragePan.getRenderedImage();
        Raster rasterPan = imagePan.getData();

        reader = new GeoTiffReader(fileRed);
        DataBuffer bufferRed = reader.read(null).getRenderedImage().getData().getDataBuffer();

        reader = new GeoTiffReader(fileGreen);
        DataBuffer bufferGreen = reader.read(null).getRenderedImage().getData().getDataBuffer();

        reader = new GeoTiffReader(fileBlue);
        DataBuffer bufferBlue = reader.read(null).getRenderedImage().getData().getDataBuffer();


        int w = rasterPan.getWidth();
        int h = rasterPan.getHeight();


        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, w, h, 3, null);

        int a = 1;
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
            if (x > w * a * 0.1) System.out.print(a++);
        }

        WriteImage(paths.get(3), env, imagePan, raster);
    }
}
