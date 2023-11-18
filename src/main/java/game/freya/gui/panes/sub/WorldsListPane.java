package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.sub.components.JTexztArea;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

@Slf4j
public class WorldsListPane extends JPanel {
    private final transient MenuCanvas canvas;
    private transient BufferedImage snap;

    public WorldsListPane(MenuCanvas canvas) {
        this.canvas = canvas;

        setName("Worlds list pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.32d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.03d), 64));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        reloadWorlds(canvas);
    }

    private void reloadWorlds(MenuCanvas canvas) {
        WorldsListPane.this.removeAll();

        int i = 0;
        for (WorldDTO world : canvas.getExistsWorlds()) {
            i++;
            add(new SubPane("World 0" + i) {{
                add(new JPanel() {
                    transient BufferedImage wImage = null;

                    @Override
                    protected void paintComponent(Graphics g) {
                        if (wImage == null) {
                            g.setColor(Color.DARK_GRAY);
                            g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                            g.setColor(Color.GRAY);
                            g.setFont(Constants.DEBUG_FONT);
                            g.drawString("NO IMAGE",
                                    (int) (getWidth() / 2d - Constants.FFB.getHalfWidthOfString(g, "NO IMAGE")),
                                    getHeight() / 2 + 3);
                        } else {
                            g.drawImage(wImage, 0, 0, getWidth(), getHeight(), this);
                        }
                        g.dispose();
                    }

                    {
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        setMinimumSize(new Dimension(96, 96));
                        setPreferredSize(new Dimension(96, 96));

                        try {
                            wImage = ImageIO.read(new File(Constants.getWorldsImagesDir() + world.getUid() + Constants.getImageExtension()));
                        } catch (IOException e) {
                            log.error("Ошибка при чтении миниатюры мира: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }
                }, BorderLayout.WEST);

                add(new SubPane("Мир:") {{
                    add(new JTexztArea("Название: '%s'\tСложность: %s".formatted(world.getTitle(),
                            world.getLevel().getDescription()), 1, 30), BorderLayout.NORTH);

                    Set<HeroDTO> hers = world.getHeroes();
                    if (hers.isEmpty()) {
                        add(new JTexztArea("Герои: (нет)", 1, 30), BorderLayout.CENTER);
                    } else {
                        JTextPane textPane = new JTextPane() {{
                            setBorder(new EmptyBorder(3, 3, 3, 3));
                            setFocusable(false);
                            setOpaque(false);
                            setBackground(new Color(0, 0, 0, 0));
                            setForeground(Color.WHITE);
                            setFont(Constants.DEBUG_FONT);
                            setIgnoreRepaint(true);
                        }};
                        StyledDocument doc = textPane.getStyledDocument();
                        Style styleRed = textPane.addStyle("red Style", null);
                        Style styleBlue = textPane.addStyle("blue Style", null);
                        Style styleGreen = textPane.addStyle("green Style", null);
                        Style styleYellow = textPane.addStyle("yellow Style", null);
                        Style styleMagenta = textPane.addStyle("magenta Style", null);
                        StyleConstants.setForeground(styleRed, Color.red);
                        StyleConstants.setForeground(styleBlue, Color.blue);
                        StyleConstants.setForeground(styleGreen, Color.green);
                        StyleConstants.setForeground(styleYellow, Color.yellow);
                        StyleConstants.setForeground(styleMagenta, Color.magenta);

                        try {
                            doc.insertString(doc.getLength(), "Герои: ", null);
                            boolean isFirst = true;
                            for (HeroDTO her : hers) {
                                if (!isFirst) {
                                    doc.insertString(doc.getLength(), " | ", null);
                                }
                                switch (her.getType()) {
                                    case SNIPER -> doc.insertString(doc.getLength(), her.getHeroName(), styleBlue);
                                    case TOWER -> doc.insertString(doc.getLength(), her.getHeroName(), styleRed);
                                    case FIXER -> doc.insertString(doc.getLength(), her.getHeroName(), styleYellow);
                                    case HUNTER -> doc.insertString(doc.getLength(), her.getHeroName(), styleGreen);
                                    case HACKER -> doc.insertString(doc.getLength(), her.getHeroName(), styleMagenta);
                                    default -> doc.insertString(doc.getLength(), her.getHeroName(), null);
                                }
                                isFirst = false;
                            }
                        } catch (BadLocationException ble) {
                            log.error("Ble: {}", ble.getMessage());
                        }

                        add(textPane, BorderLayout.CENTER);
                    }
                }});

                add(new JPanel() {{
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setFocusable(false);
                    setIgnoreRepaint(true);
                    add(new JButton() {
                        @Override
                        public void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2D = (Graphics2D) g;
                            g2D.setColor(getForeground());
                            g2D.setFont(Constants.GAME_FONT_01);
                            g2D.drawString("X",
                                    (int) (getWidth() / 2d - Constants.FFB.getStringBounds(g2D, "X").getWidth() / 2d),
                                    getHeight() / 2 + 4);
                            g2D.dispose();
                        }

                        {
                            setBackground(Color.RED.darker().darker());
                            setForeground(Color.PINK.brighter());
                            setFocusPainted(false);
                            setIgnoreRepaint(true);
                            setMinimumSize(new Dimension(24, 24));
                            setPreferredSize(new Dimension(24, 24));
                            setMaximumSize(new Dimension(24, 24));
                            setAlignmentY(TOP_ALIGNMENT);

                            addActionListener(new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                                            "Вы хотите уничтожить данный мир\nбез возможности восстановления?",
                                            FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                                    ) {
                                        canvas.deleteExistsWorldAndCloseThatPanel(world.getUid());
                                        reloadWorlds(canvas);
                                        WorldsListPane.this.revalidate();
                                    }
                                }
                            });
                        }
                    });

                    add(new JButton(" PLAY ") {{
                        setBackground(Color.BLUE.darker().darker().darker());
                        setForeground(Color.WHITE);
                        setFocusPainted(false);
                        setMinimumSize(new Dimension(96, 96));
                        setPreferredSize(new Dimension(96, 96));
                        setMaximumSize(new Dimension(96, 96));
                        setAlignmentY(TOP_ALIGNMENT);

                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                canvas.createNewHeroForExistsWorldAndCloseThatPanel(world.getUid());
                            }
                        });
                    }});
                }}, BorderLayout.EAST);

                add(new ZLabel(world.getCreateDate().format(Constants.DATE_FORMAT_3), world.getIcon()), BorderLayout.SOUTH);
            }});

            add(Box.createVerticalStrut(6));
        }

        add(Box.createVerticalStrut(canvas.getHeight()));
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Worlds list snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (super.isVisible() == isVisible) {
            return;
        }
        reloadWorlds(canvas);
        super.setVisible(isVisible);
    }
}
