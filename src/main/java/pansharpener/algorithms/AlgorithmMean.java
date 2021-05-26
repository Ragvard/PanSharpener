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
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.geometry.Envelope;

public class AlgorithmMean extends GenericAlgorithm{
    @Override
    public int getNumberOfBands() {
        return 4;
    }

    @Override
    public List<String> getBandNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("Panchromatic");
        names.add("Red");
        names.add("Green");
        names.add("Blue");
        return names;
    }

    @Override
    public void start(List<String> paths, int interpolationType) throws IOException {
        File filePan = new File(paths.get(0));
        File fileRed = new File(paths.get(1));
        File fileGreen = new File(paths.get(2));
        File fileBlue = new File(paths.get(3));

        GeoTiffReader reader = new GeoTiffReader(filePan);
        GridCoverage2D coveragePan = reader.read(null);
        Envelope env = coveragePan.getEnvelope();
        RenderedImage imagePan = coveragePan.getRenderedImage();
        Raster rasterPan = imagePan.getData();

        DataBuffer bufferPan = rasterPan.getDataBuffer();

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
        }

        WriteImage(paths.get(4), env, imagePan, raster);
    }

}
