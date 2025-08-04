package bigsir.particlesmod.mixin;

import bigsir.particlesmod.utils.AccessorClient;
import net.minecraft.client.render.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Frustum.class, remap = false)
public abstract class FrustumMixin {
	@Shadow
	@Final
	private static Frustum FRUSTUM;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void saveFrustum(CallbackInfo ci) {
		AccessorClient.frustum = FRUSTUM;
	}
}
