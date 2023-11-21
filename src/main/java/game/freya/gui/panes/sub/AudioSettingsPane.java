package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class AudioSettingsPane extends JPanel implements ChangeListener {
    private transient BufferedImage snap;

    public AudioSettingsPane(FoxCanvas canvas) {
        setName("Audio settings pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));

        add(new SubPane("Звук") {{
            add(new SubPane("◑") {{
                add(new CheckBokz("soundCheck") {{
                    setSelected(Constants.getUserConfig().isSoundEnabled());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setSoundEnabled(isSelected());
                        }
                    });
                }});
            }}, BorderLayout.WEST);
            add(new JZlider("soundSlider") {{
                setMinorTickSpacing(5);
                setMajorTickSpacing(20);
                setValue(Constants.getUserConfig().getSoundVolumePercent());
                addChangeListener(AudioSettingsPane.this);
            }}, BorderLayout.CENTER);
        }});

        add(new SubPane("Музыка") {{
            add(new SubPane("◑") {{
                add(new CheckBokz("musicCheck") {{
                    setSelected(Constants.getUserConfig().isMusicEnabled());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setMusicEnabled(isSelected());
                        }
                    });
                }});
            }}, BorderLayout.WEST);
            add(new JZlider("musicSlider") {{
                setMinorTickSpacing(5);
                setMajorTickSpacing(20);
                setValue(Constants.getUserConfig().getMusicVolumePercent());
                addChangeListener(AudioSettingsPane.this);
            }}, BorderLayout.CENTER);
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
