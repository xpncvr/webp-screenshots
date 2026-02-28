package xpncvr.webp.screenshots.mixin;


import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static xpncvr.webp.screenshots.Main.writeWebpHighQuality;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin {

    @Inject(
            method = "writeTo(Ljava/nio/file/Path;)V",
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
                buffered.setRGB(x, y, self.getColorArgb(x, y));
            }
        }

        Files.createDirectories(path.getParent());

        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        rgb.getGraphics().drawImage(buffered, 0, 0, null);

        writeWebpHighQuality(rgb, path);

        ci.cancel();
    }

}