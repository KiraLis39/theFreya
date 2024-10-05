package game.freya.gui.panes2d.sub;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.gui.panes2d.RunnableCanvasPanel;
import game.freya.gui.panes2d.sub.components.FButton;
import game.freya.gui.panes2d.sub.components.SubPane;
import game.freya.gui.panes2d.sub.components.ZLabel;
import game.freya.net.data.NetConnectTemplate;
import game.freya.services.GameControllerService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
public class HeroesListPane extends JPanel implements iSubPane {
    private final transient RunnableCanvasPanel canvas;

    private final transient GameControllerService gameControllerService;

    private transient BufferedImage snap;

    public HeroesListPane(RunnableCanvasPanel canvas, GameControllerService gameControllerService) {
        this.canvas = canvas;
        this.gameControllerService = gameControllerService;

        setName("Heroes list pane");
        setVisible(false);
        setDoubleBuffered(false);
//        setIgnoreRepaint(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (isVisible()) {
            reloadHeroes(canvas);
        }
    }

    private void reloadHeroes(RunnableCanvasPanel canvas) {
        HeroesListPane.this.removeAll();

        List<PlayCharacterDto> worldHeroes = gameControllerService.getCharacterService().findAllByWorldUidAndOwnerUid(
                gameControllerService.getWorldService().getCurrentWorld().getUid(),
                gameControllerService.getPlayerService().getCurrentPlayer().getUid());

        for (PlayCharacterDto hero : worldHeroes) {
            add(new SubPane("Герой: ".concat(hero.getName()), hero.getType().getColor()) {{
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
                        .formatted(hero.getType().getDescription(), hero.getCorpusType(), hero.getPeripheralType(), hero.getPeripheralSize()),
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
                                            gameControllerService.getCharacterService().deleteByUuid(hero.getUid());
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

//                                addActionListener(new AbstractAction() {
//                                    @Override
//                                    public void actionPerformed(ActionEvent e) {
//                                        ((MenuCanvasRunnable) canvas).openCreatingNewHeroPane(hero);
//                                    }
//                                });
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
                                if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()
                                        && !Constants.getLocalSocketConnection().isOpen()
                                ) {
                                    canvas.connectToServer(NetConnectTemplate.builder()
                                            .address(gameControllerService.getWorldService().getCurrentWorld().getAddress())
                                            .worldUid(gameControllerService.getWorldService().getCurrentWorld().getUid())
                                            .password(gameControllerService.getWorldService().getCurrentWorld().getPassword())
                                            .build());
                                } else {
                                    canvas.playWithThisHero(hero);
                                }
                            }
                        });
                    }});
                }}, BorderLayout.EAST);

                // нижняя надпись Создано:
                add(new ZLabel("Создан: ".concat(hero.getCreatedDate().format(Constants.DATE_FORMAT_3)), hero.getIcon()) {{
                    setFont(Constants.INFO_FONT);
                    setForeground(Color.GRAY);
                }}, BorderLayout.SOUTH);
            }});

            add(Box.createVerticalStrut(6));
        }

//        add(Box.createVerticalStrut(canvas.getHeight()));

        HeroesListPane.this.revalidate();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Heroes list snap...");
            BufferedImage bim = Constants.CACHE.getBufferedImage("menu_shadowed");
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
            recalculate();
        }
        super.setVisible(isVisible);
    }

    @Override
    public void recalculate() {
//        setLocation((int) (canvas.getWidth() * 0.32d), 2);
//        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
//        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.03d), 64));
    }
}
