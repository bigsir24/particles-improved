package bigsir.particlesmod.mixin;

import net.minecraft.client.render.LightmapHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LightmapHelper.class, remap = false)
public interface LightmapHelperAccessor {
	@Accessor
	int getLightmapTexture();
}
