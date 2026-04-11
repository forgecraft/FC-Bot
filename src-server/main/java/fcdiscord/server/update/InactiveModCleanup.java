package fcdiscord.server.update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fcdiscord.server.update.Mod.ModEntry;
import fcdiscord.server.update.Mod.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InactiveModCleanup {
	private static final Logger LOGGER = LoggerFactory.getLogger(InactiveModCleanup.class);
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			LOGGER.error("usage: <mods dir>");
			System.exit(1);
		}

		Path modsDir = Paths.get(args[0]).toAbsolutePath().normalize();
		Path inactiveModsDir = modsDir.resolveSibling(modsDir.getFileName().toString()+"-archive");
		Files.createDirectories(inactiveModsDir);

		ModList modList = Mod.computeModList(modsDir);

		for (ModEntry mod : modList.inactive()) {
			Path out = inactiveModsDir.resolve(modsDir.relativize(mod.path()));

			if (Files.exists(out)) {
				LOGGER.info("deleting {} (already archived)", mod.path().getFileName());
				Files.delete(mod.path());
			} else {
				LOGGER.info("moving {}", mod.path().getFileName());
				Files.move(mod.path(), out);
			}
		}

		LOGGER.info("removed {} mods", modList.inactive().size());
	}
}
