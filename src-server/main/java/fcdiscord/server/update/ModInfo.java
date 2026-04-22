package fcdiscord.server.update;

import fcdiscord.server.utils.FabricModException;
import fcdiscord.server.utils.Pair;
import org.jspecify.annotations.Nullable;
import org.tomlj.Toml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public record ModInfo(
        Path path,
        String modId,
        String version
) {
    public static List<ModInfo> parse(Path jarFilePath) throws IOException, IllegalStateException, FabricModException {
        List<ModInfo> modInfos = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarFilePath.toFile())) {
            var manifest = jarFile.getManifest();
            var neoForgeEntry = jarFile.getEntry("META-INF/neoforge.mods.toml");
            if (neoForgeEntry == null) {
                // If it's not a neoforge one, is it fabric? Because if it is, Bad!
                var fabricEntry =  jarFile.getEntry("fabric.mod.json");
                if (fabricEntry != null) {
                    throw new FabricModException("Fabric mod detected, skipping: " + jarFile);
                }

                if (manifest != null) {
                    // If there is no neoforge manifest, we can attempt to parse the manifest
                    var parsedManifest = parseManifest(manifest);
                    if (parsedManifest != null) {
                        modInfos.add(new ModInfo(jarFilePath, parsedManifest.left(), parsedManifest.right()));
                        return modInfos;
                    } else {
                        throw new IllegalStateException("No neoforge manifest and unable to parse jar manifest in " + jarFile);
                    }
                }

                throw new IllegalStateException("Not a NeoForge mod: " + jarFile);
            }

            var parsedData = Toml.parse(jarFile.getInputStream(neoForgeEntry));
            // Get the [[mods]] section
            var mods = parsedData.getArray("mods");
            if (mods == null) {
                throw new IllegalStateException("No [[mods]] section in " + jarFile);
            }

            for (int i = 0; i < mods.size(); i++) {
                var mod = mods.getTable(i);
                var modId =  mod.getString("modId");
                if (modId == null) {
                    throw new IllegalStateException("Missing mods[].modId section in " + jarFile);
                }

                var version = mod.getString("version");
                if (version == null) {
                    throw new IllegalStateException("Missing mods[].version section in " + jarFile);
                }

                if (version.equals("${file.jarVersion}")) {
                    if (manifest == null) {
                        throw new IllegalStateException("Mod version for [%s] is set to ${file.jarVersion} but no manifest found in %s".formatted(modId, jarFile));
                    }

                    version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                    if (version == null) {
                        throw new IllegalStateException("Missing mods[].version and unable to resolve jarVersion section in " + jarFile);
                    }
                }

                modInfos.add(new ModInfo(jarFilePath, modId, version));
            }
        }

        return modInfos;
    }

    @Nullable
    private static Pair<String, String> parseManifest(Manifest manifest) {
        String name = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        String version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);

        if (name == null || version == null) {
            return null;
        }

        return Pair.of(name, version);
    }
}
