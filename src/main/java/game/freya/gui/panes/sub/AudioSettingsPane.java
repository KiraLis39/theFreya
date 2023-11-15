package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.gui.panes.MenuCanvas;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

@Slf4j
public class AudioSettingsPane extends JPanel {
    private transient BufferedImage snap;

    public AudioSettingsPane(MenuCanvas canvas) {
        setName("Audio settings pane");
        setVisible(false);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
        setDoubleBuffered(Constants.getUserConfig().isMultiBufferEnabled());

        add(new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 3, 3)) {{
            setOpaque(false);
            add(new JLabel("Звук") {{
                setForeground(Color.WHITE);
            }});
            add(new JSlider(0, 100, Constants.getUserConfig().getSoundVolumePercent()) {{
                setFocusable(false);
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
                setFont(Constants.GAME_FONT_01);

                setPaintTicks(true);
                setMinorTickSpacing(5);
                setMajorTickSpacing(25);
                setSnapToTicks(true);

                setPaintLabels(true);
                setPaintTrack(true);

//                        addMouseListener(MenuCanvas.this);
            }});
        }});

        add(new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 3, 3)) {{
            setOpaque(false);
            add(new JLabel("Музыка") {{
                setForeground(Color.WHITE);
            }});
            add(new JSlider(0, 100, Constants.getUserConfig().getMusicVolumePercent()) {{
                setFocusable(false);
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
                setFont(Constants.GAME_FONT_01);

                setPaintTicks(true);
                setMinorTickSpacing(5);
                setMajorTickSpacing(25);
                setSnapToTicks(true);

                setPaintLabels(true);
                setPaintTrack(true);

//                        addMouseListener(MenuCanvas.this);
            }});
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null || snap.getHeight() != getHeight()) {
            log.info("Reload audio snap...");
            BufferedImage back = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = back.getSubimage(
                    (int) (back.getWidth() * 0.335d), 0,
                    (int) (back.getWidth() - back.getWidth() * 0.3345d),
                    back.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }
}
