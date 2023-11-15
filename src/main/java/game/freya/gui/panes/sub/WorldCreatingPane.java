package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.gui.panes.MenuCanvas;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

@Slf4j
public class WorldCreatingPane extends JPanel {
    private transient BufferedImage snap;

    public WorldCreatingPane(MenuCanvas canvas) {
        setName("World creating pane");
        setVisible(false);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        add(new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 3, 3)) {{
            setOpaque(false);
            setDoubleBuffered(false);
            add(new JLabel("Some world`s option") {{
                setForeground(Color.WHITE);
                setDoubleBuffered(false);
            }});
            add(new JTextField("some_value", 12) {{
                setHorizontalAlignment(CENTER);
                setFocusable(false);
                setEditable(false);
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
                setFont(Constants.DEBUG_FONT);
                setDoubleBuffered(false);
//                        addMouseListener(MenuCanvas.this);
            }});
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null || snap.getHeight() != getHeight()) {
            log.info("Reload world creating snap...");
            BufferedImage back = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = back.getSubimage(
                    (int) (back.getWidth() * 0.335d), 0,
                    (int) (back.getWidth() - back.getWidth() * 0.3345d),
                    back.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }
}
