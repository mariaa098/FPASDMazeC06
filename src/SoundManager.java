import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundManager {

    public static void playSound(String soundFileName) {
        new Thread(() -> {
            try {
                InputStream audioSrc = SoundManager.class.getResourceAsStream("/sound/" + soundFileName);

                if (audioSrc == null) {
                    System.err.println("Sound file not found: /sound/" + soundFileName);
                    return;
                }

                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }).start();
    }

    public static Clip playSoundLoop(String soundFileName) {
        try {
            InputStream audioSrc = SoundManager.class.getResourceAsStream("/sound/" + soundFileName);

            if (audioSrc == null) {
                System.err.println("Sound file not found: /sound/" + soundFileName);
                return null;
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            return clip;

        } catch (Exception e) {
            System.err.println("Error playing loop sound: " + e.getMessage());
            return null;
        }
    }

    public static void stopSound(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
