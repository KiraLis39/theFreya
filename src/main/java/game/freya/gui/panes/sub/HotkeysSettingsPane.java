package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

@Slf4j
public class HotkeysSettingsPane extends JPanel implements MouseListener, iSubPane {
    private transient BufferedImage snap;

    public HotkeysSettingsPane(FoxWindow canvas) {
        setName("Hotkeys settings pane");
        setVisible(false);
        setDoubleBuffered(false);
//        setIgnoreRepaint(true);

        recalculate(canvas);
        setLayout(new BorderLayout(3, 3));

        add(new SubPane(null) {{
            setBorder(new EmptyBorder(0, 0, 0, 32));
            add(new SubPane("Горячие клавиши:") {{
                setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 9, 9));

                for (UserConfig.DefaultHotKeys key : UserConfig.DefaultHotKeys.values()) {
                    add(new SubPane(key.getDescription()) {{
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        add(new JTextField((key.getMask() != 0
                                ? (InputEvent.getModifiersExText(key.getMask()) + " + ") : "")
                                + KeyEvent.getKeyText(key.getEvent()), canvas.getWidth() / 3 / 24 - 6) {{
                            setHorizontalAlignment(CENTER);
                            setDoubleBuffered(false);
                            setFocusable(false);
                            setEditable(false);
                            setBackground(Color.DARK_GRAY);
                            setForeground(Color.WHITE);
                            setFont(Constants.DEBUG_FONT);
                            setBorder(BorderFactory.createRaisedSoftBevelBorder());
                            addMouseListener(HotkeysSettingsPane.this);
                        }});
                    }});
                }
            }});
        }}, BorderLayout.CENTER);

        add(new SubPane(null) {{
            setBorder(new EmptyBorder(3, 0, 8, 32));
            add(new FButton("По умолчанию") {{
                setPreferredSize(new Dimension(128, 32));

                addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Constants.getUserConfig().resetControlKeys();
                    }
                });
            }});
        }}, BorderLayout.SOUTH);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Reload hotkeys snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
            setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));

        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void recalculate(FoxWindow canvas) {
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
    }
}
