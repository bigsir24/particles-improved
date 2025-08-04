package bigsir.particlesmod;

import bigsir.particlesmod.mixin.ShaderAccessor;
import bigsir.particlesmod.shader.ShaderProviderJar;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.item.Items;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;
import turniplabs.halplibe.util.OptionsInitEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ParticlesMod implements ModInitializer, RecipeEntrypoint, ClientStartEntrypoint, OptionsInitEntrypoint {
    public static final String MOD_ID = "particlesmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Shader particleShader;
	public static KeyBinding recompile;

    @Override
    public void onInitialize() {
        LOGGER.info("Particles Mod initialized.");
    }

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}

	@Override
	public void beforeClientStart() {
	}

	@Override
	public void afterClientStart() {
		compileShaders();

		OptionsPage page = OptionsPages.register(new OptionsPage(key("options.page"), Items.PAPER.getDefaultStack()));

		page.withComponent(new OptionsCategory(key("options.category.keys"))
			.withComponent(new KeyBindingComponent(recompile))
		);

		try {
			TextureRegistry.initializeAllFiles(MOD_ID, TextureRegistry.particleAtlas, false);
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
    }

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static void devBuildResources() {
		ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).get();
		Path path = mod.findPath("assets").get();
		if ("jar".equals(path.toUri().getScheme())) return; // Return if jar

		Path shadersBuildFolder = path.resolve(MOD_ID + "/shaders");
		Path projectResources = path.getRoot().resolve(path.subpath(0, path.getNameCount() - 4)).resolve("src/main/resources/assets/" + MOD_ID + "/shaders");

		try (Stream<Path> pathStream = Files.list(projectResources)){
			for (Path shaderFile : pathStream.collect(Collectors.toList())) {
				Files.copy(shaderFile, shadersBuildFolder.resolve(shaderFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ignore) {}
    }

	public static void compileShaders() {
		if (particleShader != null) GL20.glDeleteProgram(((ShaderAccessor)particleShader).getProgram());
		particleShader = new Shader().compile(new ShaderProviderJar(MOD_ID), "particle");
	}

	@Override
	public void initOptions(GameSettings settings) {
		recompile = new KeyBinding(key("options.compile")).setDefault(InputDevice.keyboard, Keyboard.KEY_O);
	}

	public static String key(String string) {
		return MOD_ID + "." + string;
	}


}
