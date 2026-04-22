package fcdiscord.server.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModIndex.class);

    private static ModIndex INSTANCE;

    public static ModIndex instance() {
        if (INSTANCE == null) {
            INSTANCE = new ModIndex();
        }

        return INSTANCE;
    }

    private final Map<Long, List<ModInfo>> index = new HashMap<>();

    public void indexPath(Long channelId, Path path) throws IOException {
        try (var filesStream = Files.walk(path.resolve("servermods"))) {
            var jarFiles = filesStream
                    .filter(Files::isRegularFile)
                    .filter(e -> e.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .toList();

            List<ModInfo> modInfo = jarFiles
                    .stream().map(file -> {
                        try {
                            return ModInfo.parse(file);
                        } catch (IOException | IllegalStateException e) {
                            System.out.println("Error processing file " + file + ": " + e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();

            index.put(channelId, modInfo);
            LOGGER.info("Indexed {} mods for channel {}", modInfo.size(), channelId);
        }
    }

    public List<ModInfo> getModsByChannelAndId(Long channelId) {
        return index.getOrDefault(channelId, List.of());
    }
}
