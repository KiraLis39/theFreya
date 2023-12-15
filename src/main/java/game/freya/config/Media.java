package game.freya.config;

import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class Media {
    private static final Map<String, Sound> soundMap = new HashMap<>();


    public static void add(String name, File file) {
        if (!TinySound.isInitialized()) {
            TinySound.init();
        }

        soundMap.put(name, TinySound.loadSound(file));
    }

    public static void playSound(String soundName) {
        playSound(soundName, 6d);
    }

    public static void playSound(String soundName, double vol) {
        for (String name : soundMap.keySet()) {
            if (name.equals(soundName)) {
                soundMap.get(soundName).play(vol);
                return;
            }
        }
        log.error("Media: Not found the sound '" + soundName + "'.");
    }

    private Media() {
    }
}
