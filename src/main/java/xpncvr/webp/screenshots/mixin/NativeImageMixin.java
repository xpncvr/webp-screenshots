package xpncvr.webp.screenshots.mixin;


import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static xpncvr.webp.screenshots.Main.decodeWebp;
import static xpncvr.webp.screenshots.Main.writeWebpHighQuality;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin {

    @Inject(
            method = "writeToFile(Ljava/nio/file/Path;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void writeWebPIfNeeded(Path path, CallbackInfo ci) throws IOException {
        String name = path.getFileName().toString().toLowerCase();

        if (!name.endsWith(".webp")) {
            return;
        }

        NativeImage self = (NativeImage)(Object)this;

        int width = self.getWidth();
        int height = self.getHeight();

        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffered.setRGB(x, y, self.getPixel(x, y));
            }
        }

        Files.createDirectories(path.getParent());

        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        rgb.getGraphics().drawImage(buffered, 0, 0, null);

        writeWebpHighQuality(rgb, path);

        ci.cancel();
    }

    @Inject(
            method = "read(Lcom/mojang/blaze3d/platform/NativeImage$Format;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void readWebp(
            NativeImage.Format format,
            ByteBuffer buffer,
            CallbackInfoReturnable<NativeImage> cir
    ) throws IOException {

        if (isWebp(buffer)) {
            NativeImage img = decodeWebp(buffer);
            cir.setReturnValue(img);
        }
    }

    private static boolean isWebp(ByteBuffer buf) {
        return buf.get(0) == 'R'
                && buf.get(1) == 'I'
                && buf.get(2) == 'F'
                && buf.get(3) == 'F'
                && buf.get(8) == 'W'
                && buf.get(9) == 'E'
                && buf.get(10) == 'B'
                && buf.get(11) == 'P';
    }

}