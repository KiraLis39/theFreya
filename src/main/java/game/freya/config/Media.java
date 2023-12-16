package game.freya.config;

import fox.player.playerUtils.VolumeConverter;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public final class Media {
    private static final VolumeConverter converter = new VolumeConverter();

    private static Music currentPlayed;


    private Media() {
    }

    public static void add(String name, File file) {
        if (!TinySound.isInitialized()) {
            TinySound.init();
        }

        Sound loaded = TinySound.loadSound(file);
        Constants.CACHE.addIfAbsent(name, loaded);
    }

    public static void playSound(String soundName) {
        playSound(soundName, converter.volumePercentToGain(Constants.getUserConfig().getMusicVolumePercent()));
    }

    public static void playSound(String soundName, double vol) {
        if (!Constants.getUserConfig().isSoundEnabled()) {
            return;
        }

        if (Constants.CACHE.hasKey(soundName)) {
            ((Sound) Constants.CACHE.get(soundName)).play(vol);
            return;
        }
        log.error("Media: Not found the sound '" + soundName + "'.");
    }

    public static void playMusic(String musicName) {
        if (!Constants.getUserConfig().isMusicEnabled()) {
            return;
        }

        log.info("Try to play the music: " + musicName);
        currentPlayed = (Music) Constants.CACHE.get(musicName); // TinySound.loadMusic(new File(audioPath));
        currentPlayed.play(true, converter.volumePercentToGain(Constants.getUserConfig().getMusicVolumePercent()));
    }
}
