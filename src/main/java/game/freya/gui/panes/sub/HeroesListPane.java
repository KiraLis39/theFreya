package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.JTexztArea;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
public class HeroesListPane extends JPanel {
    private final transient FoxCanvas canvas;
    private final transient GameController gameController;
    private transient BufferedImage snap;

    public HeroesListPane(FoxCanvas canvas, GameController controller) {
        this.canvas = canvas;
        this.gameController = controller;

        setName("Heroes list pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.32d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.03d), 64));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (isVisible()) {
            reloadHeroes(canvas);
        }
    }

    private void reloadHeroes(FoxCanvas canvas) {
        HeroesListPane.this.removeAll();

        int i = 0;
        for (HeroDTO hero : gameController.findAllHeroesByWorldUid(gameController.getCurrentWorldUid())) {
            i++;
            add(new SubPane("Hero 0" + i) {{
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

                add(new JTexztArea("Герой '%s'".formatted(hero.getHeroName()), 1, 30), BorderLayout.CENTER);

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
                                            "Вы хотите уничтожить своего героя\nбез возможности восстановления?",
                                            FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                                    ) {
                                        if (canvas instanceof MenuCanvas mCanvas) {
                                            mCanvas.deleteExistsPlayerHero(hero.getUid());
                                            reloadHeroes(mCanvas);
                                        }
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
                                if (canvas instanceof MenuCanvas mCanvas) {
                                    mCanvas.playWithThisHero(hero);
                                }
                            }
                        });
                    }});
                }}, BorderLayout.EAST);

                add(new ZLabel(hero.getCreateDate().format(Constants.DATE_FORMAT_3), hero.getIcon()), BorderLayout.SOUTH);
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
}
