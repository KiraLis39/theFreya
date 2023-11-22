package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class NetworkListPane extends JPanel {
    private static final int maxElementsDim = 96;
    private final transient FoxCanvas canvas;
    private final transient GameController gameController;
    private transient BufferedImage snap;
    private final SubPane centerList;

    public NetworkListPane(FoxCanvas canvas, GameController controller) {
        this.canvas = canvas;
        this.gameController = controller;

        setName("Network list pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.32d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
        setLayout(new BorderLayout(1, 1));
        setBorder(new EmptyBorder((int) (getHeight() * 0.035d), 0, (int) (getHeight() * 0.015d), 32));

        centerList = new SubPane("Сетевые миры:") {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }};
        add(centerList, BorderLayout.CENTER);

        add(new FButton("Новое подключение", null) {{
            setBackground(Color.MAGENTA.darker().darker());
            addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String result = (String) new FOptionPane()
                            .buildFOptionPane("Подключиться:", "Адрес сервера:",
                                    FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                    log.info("Подключение к серверу по адресу {} ещё не реализовано", result);
                }
            });
        }}, BorderLayout.SOUTH);

        if (isVisible()) {
            reloadNet(canvas);
        }
    }

    private void reloadNet(FoxCanvas canvas) {
        centerList.removeAll();
        centerList.add(Box.createVerticalStrut(9));

        int i = 0;
        for (WorldDTO world : gameController.findAllWorldsByNetworkAvailable(true)) {
            int k = ++i;
            centerList.add(new SubPane("Net world 0" + k) {{
                add(new JPanel() {{
                    setOpaque(false);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);

                    add(new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            if (world.getIcon() != null) {
                                g.drawImage(world.getIcon(), 0, 2, maxElementsDim, maxElementsDim, this);
                                g.dispose();
                            }
                        }

                        {
                            setOpaque(false);
                            setIgnoreRepaint(true);
                            setDoubleBuffered(false);
                            setMinimumSize(new Dimension(maxElementsDim, maxElementsDim));
                            setPreferredSize(new Dimension(maxElementsDim, maxElementsDim));
                            setMaximumSize(new Dimension(maxElementsDim, maxElementsDim));
                        }
                    }, BorderLayout.WEST);

                    add(new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1)) {{
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        setDoubleBuffered(false);

                        add(new ZLabel(("<html><pre>"
                                + "Название:<font color=#F8CF43><b>   %s</b></font>"
                                + "<br>Уровень:<font color=#43F8C9><b>    %s</b></font>"
                                + "<br>Сетевой:<font color=#239BEE><b>    %s</b></font>"
                                + "<br>Создано:<font color=#8805A8><b>    %s</b></font>"
                                + "</pre></html>")
                                .formatted(world.getTitle(), world.getLevel().getDescription(),
                                        world.isNetAvailable(), world.getCreateDate().format(Constants.DATE_FORMAT_3)),
                                null));
                    }}, BorderLayout.CENTER);
                }}, BorderLayout.WEST);

                add(new FButton(" CONN ") {{
                    setBackground(Color.GREEN);
                    setForeground(Color.WHITE);
                    setFocusPainted(false);
                    setMinimumSize(new Dimension(64, getHeight()));
                    setMaximumSize(new Dimension(96, getHeight()));
                    setAlignmentY(TOP_ALIGNMENT);

                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (canvas instanceof MenuCanvas mCanvas) {
                                mCanvas.getOrCreateHeroForSelectedWorldAndCloseThat(world.getUid());
                            }
                        }
                    });
                }}, BorderLayout.EAST);
            }});

            centerList.add(Box.createVerticalStrut(6));
        }

        centerList.add(Box.createVerticalStrut(canvas.getHeight()));

        NetworkListPane.this.revalidate();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Net list snap...");
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
            reloadNet(canvas);
        }
        super.setVisible(isVisible);
    }
}
