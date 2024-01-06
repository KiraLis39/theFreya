package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import fox.components.tools.VerticalFlowLayout;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.MenuWindow;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class WorldsListPane extends JPanel implements iSubPane {
    private static final int maxElementsDim = 96;

    private static final int maxImageDim = 88;

    private final transient FoxWindow canvas;

    private final transient GameController gameController;

    private final SubPane centerList;

    private final JScrollPane centerScroll;

    private transient BufferedImage snap;

    private transient ZLabel zlabel;

    public WorldsListPane(FoxWindow canvas, GameController controller) {
        this.canvas = canvas;
        this.gameController = controller;

        setName("Worlds list pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        recalculate(canvas);
        setLayout(new BorderLayout(1, 1));

        centerList = new SubPane(null) {{
            setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 1, 1));
        }};

        centerScroll = new JScrollPane(centerList) {
            {
                setBackground(Color.ORANGE);
                setBorder(null);

                getVerticalScrollBar().setUnitIncrement(16);
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

                JViewport vp = getViewport();
                vp.setOpaque(false);
                vp.setBackground(Color.BLUE);
                vp.setBorder(null);

                vp.setFocusable(false);
                vp.setAutoscrolls(true);
            }

            @Override
            public void paint(Graphics g) {
                super.paintChildren(g);
            }
        };

        add(new SubPane("Локальные миры:", Color.BLACK) {{
            add(centerScroll);
        }}, BorderLayout.CENTER);

        if (isVisible()) {
            reloadWorlds(canvas);
        }
    }

    private void reloadWorlds(FoxWindow canvas) {
        centerList.removeAll();
        centerList.add(Box.createVerticalStrut(6));

        List<WorldDTO> worlds = gameController.findAllWorldsByNetworkAvailable(false);
        for (WorldDTO world : worlds) {
            centerList.add(new SubPane("Мир: " + world.getTitle()) {{
                setWorld(world);
                setPreferredSize(new Dimension(
                        Constants.getUserConfig().isFullscreen()
                                ? (worlds.size() > 5 ? centerScroll.getWidth() - 8 : centerScroll.getWidth() - 4)
                                : (worlds.size() > 5 ? centerScroll.getWidth() - 24 : centerScroll.getWidth() - 8),
                        128));

                add(new SubPane(null) {{
                    setOpaque(false);
                    setBackground(Color.YELLOW);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);
                    setBorder(new EmptyBorder(-3, -3, -3, -3));

                    add(new JPanel() {
                        private transient BufferedImage wImage = null;

                        @Override
                        protected void paintComponent(Graphics g) {
                            if (wImage == null) {
                                g.setColor(Color.DARK_GRAY);
                                g.fillRoundRect(0, 0, getWidth(), getHeight(), maxImageDim / 2, maxImageDim / 2);
                                g.setColor(Color.GRAY);
                                g.setFont(Constants.DEBUG_FONT);
                                g.drawString("NO IMAGE",
                                        (int) (getWidth() / 2d - Constants.FFB.getHalfWidthOfString(g, "NO IMAGE")) + 1,
                                        getHeight() / 2 + 7);
                            } else {
                                g.drawImage(wImage, 0, 0, getWidth(), getHeight(), this);
                            }
                            g.dispose();
                        }

                        {
                            setOpaque(false);
                            setIgnoreRepaint(true);
                            setMinimumSize(new Dimension(maxImageDim, maxImageDim));
                            setPreferredSize(new Dimension(maxImageDim, maxImageDim));
                            setMaximumSize(new Dimension(maxImageDim, maxImageDim));

                            try {
                                wImage = ImageIO.read(new File(Constants.getWorldsImagesDir() + world.getUid() + Constants.getImageExtension()));
                            } catch (IOException e) {
                                log.error("Ошибка при чтении миниатюры мира: {}", ExceptionUtils.getFullExceptionMessage(e));
                            }
                        }
                    }, BorderLayout.WEST);

                    add(new SubPane("Мир:") {{
                        setBorder(new EmptyBorder(-6, 3, 3, 3));
                        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 1, 1));

                        zlabel = new ZLabel(("<html><pre>"
                                + "Уровень:<font color=#43F8C9><b>  %s</b></font>"
                                + "<br>Сетевой:<font color=#239BEE><b>  %s</b></font>"
                                + "</pre></html>")
                                .formatted(world.getLevel().getDescription(), world.isNetAvailable()),
                                null);

                        add(zlabel, BorderLayout.WEST);

                        List<HeroDTO> hers = gameController.findAllHeroesByWorldUid(world.getUid());
                        if (hers.isEmpty()) {
                            zlabel.setText(zlabel.getText().replace("</pre>",
                                    "<br>Герои:<font color=#99c7b5><b>    (нет)</b></font></pre>"));
                        } else {
                            String lineEnd = "</b></font>";
                            StringBuilder heroes = new StringBuilder("<br>Герои:    ");
                            boolean isFirst = true;
                            for (HeroDTO her : hers) {
                                if (!isFirst) {
                                    heroes.append("<font color=#000000> | </font>");
                                }
                                switch (her.getHeroType()) {
                                    case SNIPER ->
                                            heroes.append("<font color=#f0ec22><b>").append(her.getCharacterName()).append(lineEnd);
                                    case TOWER ->
                                            heroes.append("<font color=#0f294d><b>").append(her.getCharacterName()).append(lineEnd);
                                    case FIXER ->
                                            heroes.append("<font color=#bcd918><b>").append(her.getCharacterName()).append(lineEnd);
                                    case HUNTER ->
                                            heroes.append("<font color=#d95818><b>").append(her.getCharacterName()).append(lineEnd);
                                    case HACKER ->
                                            heroes.append("<font color=#223df0><b>").append(her.getCharacterName()).append(lineEnd);
                                    default ->
                                            heroes.append("<font color=#545454><b>").append(her.getCharacterName()).append(lineEnd);
                                }
                                isFirst = false;
                            }
                            zlabel.setText(zlabel.getText().replace("</pre>", "%s</pre>".formatted(heroes)));
                        }
                    }}, BorderLayout.CENTER);

                    add(new ZLabel("Создан: " + world.getCreateDate().format(Constants.DATE_FORMAT_3), null) {{
                        setFont(Constants.INFO_FONT);
                        setForeground(Color.GRAY);
                    }}, BorderLayout.SOUTH);
                }}, BorderLayout.WEST);

                add(new JPanel(new BorderLayout(1, 1)) {{
                    setOpaque(false);
                    setBackground(Color.CYAN.darker());
                    setIgnoreRepaint(true);
                    setDoubleBuffered(false);
                    setMinimumSize(new Dimension(maxElementsDim, maxElementsDim));
                    setPreferredSize(new Dimension(maxElementsDim, maxElementsDim));
                    setMaximumSize(new Dimension(maxElementsDim, maxElementsDim));
                    add(new FButton() {
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
                                            && canvas instanceof MenuWindow mCanvas
                                    ) {
                                        mCanvas.deleteExistsWorldAndCloseThatPanel(world.getUid());
                                        reloadWorlds(canvas);
                                        WorldsListPane.this.revalidate();
                                    }
                                }
                            });
                        }
                    }, BorderLayout.WEST);

                    add(new FButton(" PLAY ") {{
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
                                canvas.chooseOrCreateHeroForWorld(world.getUid());
                            }
                        });
                    }}, BorderLayout.CENTER);
                }}, BorderLayout.EAST);
            }});
        }

        WorldsListPane.this.revalidate();
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
        super.setVisible(isVisible);
        if (isVisible()) {
            reloadWorlds(canvas);
        }
    }

    @Override
    public void recalculate(FoxWindow canvas) {
        setLocation((int) (canvas.getWidth() * 0.32d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.03d), 64));
    }
}
