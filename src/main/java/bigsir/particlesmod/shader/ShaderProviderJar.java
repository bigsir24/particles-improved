package bigsir.particlesmod.shader;

import bigsir.particlesmod.ParticlesMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.shader.ShaderProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShaderProviderJar implements ShaderProvider {
	public final String MOD_ID;

	public ShaderProviderJar(String modId) {
		this.MOD_ID = modId;
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Override
	public String getShaderSource(String shaderFileName) {
		String shaderRoot = "assets/" + MOD_ID + "/shaders/" + shaderFileName;

		Optional<Path> path = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath(shaderRoot);
		if (!path.isPresent() || !Files.exists(path.get())) {
			logGenericError(shaderFileName);
			return null;
		}

		try (Stream<String> stream = Files.lines(path.get(), StandardCharsets.UTF_8)){
            return stream.collect(Collectors.joining("\n"));
		} catch (IOException ignored) {
			logGenericError(shaderFileName);
		}

		return null;
	}

	public void logGenericError(String file) {
		ParticlesMod.LOGGER.error("Failed to load {}", file);
	}
}
