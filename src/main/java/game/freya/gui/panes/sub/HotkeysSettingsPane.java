package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.MenuCanvas;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class HotkeysSettingsPane extends JPanel {
    private transient BufferedImage snap;

    public HotkeysSettingsPane(MenuCanvas canvas) {
        setName("Hotkeys settings pane");
        setVisible(false);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));

        for (UserConfig.HotKeys key : UserConfig.HotKeys.values()) {
            add(new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 3, 3)) {{
                setOpaque(false);
                add(new JLabel(key.getDescription()) {{
                    setForeground(Color.WHITE);
                }});
                add(new JTextField((key.getMask() != 0
                        ? (InputEvent.getModifiersExText(key.getMask()) + " + ") : "")
                        + KeyEvent.getKeyText(key.getEvent()), 12) {{
                    setHorizontalAlignment(CENTER);
                    setFocusable(false);
                    setEditable(false);
                    setBackground(Color.DARK_GRAY);
                    setForeground(Color.WHITE);
                    setFont(Constants.DEBUG_FONT);

//                    addMouseListener(this);
                }});
            }});
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null || snap.getHeight() != getHeight()) {
            log.info("Reload hotkeys snap...");
            BufferedImage back = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = back.getSubimage(
                    (int) (back.getWidth() * 0.335d), 0,
                    (int) (back.getWidth() - back.getWidth() * 0.3345d),
                    back.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }
}
