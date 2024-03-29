package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import game.freya.net.data.NetConnectTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class HeroesListPane extends JPanel implements iSubPane {
    private final transient FoxWindow canvas;

    private final transient GameController gameController;

    private transient BufferedImage snap;

    public HeroesListPane(FoxWindow canvas, GameController controller) {
        this.canvas = canvas;
        this.gameController = controller;

        setName("Heroes list pane");
        setVisible(false);
        setDoubleBuffered(false);
//        setIgnoreRepaint(true);

        recalculate(canvas);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (isVisible()) {
            reloadHeroes(canvas);
        }
    }

    private void reloadHeroes(FoxWindow canvas) {
        HeroesListPane.this.removeAll();

        for (HeroDTO hero : gameController.getMyCurrentWorldHeroes()) {
            add(new SubPane("Герой: " + hero.getCharacterName(), hero.getHeroType().getColor()) {{
                setAlignmentY(TOP_ALIGNMENT);
                add(new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(Color.MAGENTA);
                        g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g.dispose();
                    }

                    {
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        setMinimumSize(new Dimension(96, 96));
                        setPreferredSize(new Dimension(96, 96));
                    }
                }, BorderLayout.WEST);

                add(new ZLabel(("<html><pre>"
                        + "Класс:<font color=#43F8C9><b>     %s</b></font>"
                        + "<br>Корпус:<font color=#239BEE><b>    %s</b></font>"
                        + "<br>Периферия:<font color=#239BEE><b> %s (%d)</b></font>"
                        + "</pre></html>")
                        .formatted(hero.getHeroType().getDescription(), hero.getCorpusType(), hero.getPeriferiaType(), hero.getPeriferiaSize()),
                        null) {{
                    setVerticalAlignment(TOP);
                    setAlignmentY(TOP_ALIGNMENT);
                }}, BorderLayout.CENTER);

                add(new JPanel() {{
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setFocusable(false);
                    setIgnoreRepaint(true);
                    setAlignmentY(CENTER_ALIGNMENT);

                    // кнопки Удалить и Изменить героя:
                    add(new SubPane(null) {{
                        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
//                            setIgnoreRepaint(true);
                                setMinimumSize(new Dimension(24, 24));
                                setPreferredSize(new Dimension(24, 24));
                                setMaximumSize(new Dimension(24, 24));
                                setAlignmentY(TOP_ALIGNMENT);

                                addActionListener(new AbstractAction() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                                                "Вы хотите уничтожить своего героя\nбез возможности восстановления?",
                                                FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                                        ) {
                                            gameController.deleteExistsPlayerHero(hero.getCharacterUid());
                                            reloadHeroes(canvas);
                                        }
                                    }
                                });
                            }
                        });

                        add(Box.createVerticalStrut(2));
                        add(new FButton() {
                            @Override
                            public void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2D = (Graphics2D) g;
                                g2D.setColor(getForeground());
                                g2D.setFont(Constants.GAME_FONT_01);
                                g2D.drawString("O",
                                        (int) (getWidth() / 2d - Constants.FFB.getStringBounds(g2D, "X").getWidth() / 2d),
                                        getHeight() / 2 + 4);
                                g2D.dispose();
                            }

                            {
                                setBackground(Color.YELLOW.darker().darker());
                                setForeground(Color.ORANGE.brighter());
                                setFocusPainted(false);
//                            setIgnoreRepaint(true);
                                setMinimumSize(new Dimension(24, 24));
                                setPreferredSize(new Dimension(24, 24));
                                setMaximumSize(new Dimension(24, 24));
                                setAlignmentY(TOP_ALIGNMENT);

                                addActionListener(new AbstractAction() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        gameController.openCreatingNewHeroPane(hero);
                                    }
                                });
                            }
                        });

                        add(Box.createVerticalGlue());
                    }});

                    add(new FButton(" PLAY ") {{
                        setBackground(Color.BLUE.darker().darker().darker());
                        setForeground(Color.WHITE);
                        setMinimumSize(new Dimension(96, 96));
                        setPreferredSize(new Dimension(96, 96));
                        setMaximumSize(new Dimension(96, 96));
                        setVerticalAlignment(CENTER);

                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (gameController.isCurrentWorldIsNetwork() && !gameController.isSocketIsOpen()) {
                                    gameController.connectToServer(NetConnectTemplate.builder()
                                            .address(gameController.getCurrentWorldAddress())
                                            .worldUid(gameController.getCurrentWorldUid())
                                            .passwordHash(gameController.getCurrentWorldPassword())
                                            .build());
                                } else {
                                    gameController.playWithThisHero(hero);
                                }
                            }
                        });
                    }});
                }}, BorderLayout.EAST);

                // нижняя надпись Создано:
                add(new ZLabel("Создан: " + hero.getCreateDate().format(Constants.DATE_FORMAT_3), hero.getIcon()) {{
                    setFont(Constants.INFO_FONT);
                    setForeground(Color.GRAY);
                }}, BorderLayout.SOUTH);
            }});

            add(Box.createVerticalStrut(6));
        }

        add(Box.createVerticalStrut(canvas.getHeight()));

        HeroesListPane.this.revalidate();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Heroes list snap...");
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
        if (isVisible) {
            reloadHeroes(canvas);
        }
        super.setVisible(isVisible);
    }

    @Override
    public void recalculate(FoxWindow canvas) {
        setLocation((int) (canvas.getWidth() * 0.32d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.03d), 64));
    }
}
