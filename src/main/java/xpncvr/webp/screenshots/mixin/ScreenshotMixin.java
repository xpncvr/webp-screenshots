package xpncvr.webp.screenshots.mixin;

import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;


@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {


    @Redirect(method = "getFile", at = @At(value = "NEW", target = "(Ljava/io/File;Ljava/lang/String;)Ljava/io/File;"))
    private static File changeExtension(File directory, String child) {
        if (child.endsWith(".png")) {
            child = child.substring(0, child.length() - 4) + ".webp";
        }
        return new File(directory, child);
    }
}
