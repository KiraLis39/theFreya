package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class AudioSettingsPane extends JPanel implements ChangeListener {
    private transient BufferedImage snap;

    public AudioSettingsPane(MenuCanvas canvas) {
        setName("Audio settings pane");
        setVisible(false);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        add(new SubPane("Звук") {{
            add(new SubPane("Вкл") {{
                add(new CheckBokz(canvas.getWidth(), "soundCheck") {{
                    setSelected(Constants.getUserConfig().isSoundEnabled());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setSoundEnabled(isSelected());
                        }
                    });
                }});
            }});
            add(new JZlider(canvas.getWidth(), "soundSlider") {{
                setValue(Constants.getUserConfig().getSoundVolumePercent());
                addChangeListener(AudioSettingsPane.this);
            }});
        }});

        add(new SubPane("Музыка") {{
            add(new SubPane("Вкл") {{
                add(new CheckBokz(canvas.getWidth(), "musicCheck") {{
                    setSelected(Constants.getUserConfig().isMusicEnabled());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setMusicEnabled(isSelected());
                        }
                    });
                }});
            }});
            add(new JZlider(canvas.getWidth(), "musicSlider") {{
                setValue(Constants.getUserConfig().getMusicVolumePercent());
                addChangeListener(AudioSettingsPane.this);
            }});
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Reload audio snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String cName = ((JComponent) e.getSource()).getName();
        switch (cName) {
            case "soundSlider" -> {
                log.debug("Sound bar: {}", ((JSlider) e.getSource()).getValue());
                Constants.getUserConfig().setSoundVolumePercent(((JSlider) e.getSource()).getValue());
            }
            case "musicSlider" -> {
                log.debug("Music bar: {}", ((JSlider) e.getSource()).getValue());
                Constants.getUserConfig().setMusicVolumePercent(((JSlider) e.getSource()).getValue());
            }
            default -> log.warn("Неопознанное событие слушателя на объекте {}", cName);
        }
    }
}
