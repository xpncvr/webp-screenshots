package xpncvr.webp.screenshots;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;


public class Main implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("webpscreenshots");


	@Override
	public void onInitialize() {
		LOGGER.info("WebP screenshots initialised");
    }


    public static void writeWebpHighQuality(BufferedImage image, Path path) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IOException("No WebP writer found (is sejda webp-imageio on classpath?)");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("Lossless"); // "Lossy"            //param.setCompressionQuality(0.9f);
        }

        Files.createDirectories(path.getParent());

        try (ImageOutputStream ios =
                     ImageIO.createImageOutputStream(Files.newOutputStream(path))) {

            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    public static NativeImage fromBufferedImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), nativeImage.getPixelsABGR(), 0, image.getWidth());
        return nativeImage;
    }

    public static NativeImage decodeWebp(ByteBuffer buffer) throws IOException {
        ByteBuffer dup = buffer.asReadOnlyBuffer();
        dup.rewind();

        byte[] bytes = new byte[dup.remaining()];
        dup.get(bytes);

        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Failed to decode WebP image");
            }

            int width = image.getWidth();
            int height = image.getHeight();


            NativeImage nativeImage = new NativeImage(width, height, true);
            image.getRGB(0, 0, width, height, nativeImage.getPixelsABGR(), 0, width);


            return nativeImage;
        }
    }


}