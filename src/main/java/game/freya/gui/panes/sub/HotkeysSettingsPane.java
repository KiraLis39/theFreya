package game.freya.gui.panes.sub;

import fox.components.layouts.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

@Slf4j
public class HotkeysSettingsPane extends JPanel implements MouseListener, iSubPane {
    private transient BufferedImage snap;

    public HotkeysSettingsPane(RunnableCanvasPanel canvas) {
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

                for (UserConfig.HotKeys key : UserConfig.HotKeys.values()) {
                    add(new SubPane(key.description()) {{
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        add(new JTextField((key.mask() != 0
                                ? (InputEvent.getModifiersExText(key.mask()) + " + ") : "")
                                + KeyEvent.getKeyText(key.event()), canvas.getWidth() / 3 / 24 - 6) {{
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
            BufferedImage bim = Constants.CACHE.getBufferedImage("menu_shadowed");
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
    public void recalculate(RunnableCanvasPanel canvas) {
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
    }
}
