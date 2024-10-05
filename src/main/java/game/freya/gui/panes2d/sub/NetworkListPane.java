package game.freya.gui.panes2d.sub;

import fox.FoxRender;
import fox.components.FOptionPane;
import fox.components.layouts.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.dto.roots.WorldDto;
import game.freya.gui.panes2d.RunnableCanvasPanel;
import game.freya.gui.panes2d.sub.components.FButton;
import game.freya.gui.panes2d.sub.components.SubPane;
import game.freya.gui.panes2d.sub.components.ZLabel;
import game.freya.net.data.NetConnectTemplate;
import game.freya.services.GameControllerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static game.freya.config.Constants.FFB;

@Slf4j
public class NetworkListPane extends JPanel implements iSubPane {
    private static final String connectionString = "- CONNECTION -";

    private static final String pingString = "- PING -";

    private static final int maxElementsDim = 96;

    private final transient RunnableCanvasPanel canvas;

    private final transient GameControllerService gameControllerService;

    private final SubPane centerList;

    private final String[] dot = new String[]{".", "..", "..."};

    private transient BufferedImage snap;

    @Getter
    private String address;

    @Getter
    private String password = "";

    private int dots = 0;

    private long was = System.currentTimeMillis();

    private transient Thread pingActionThread;

    public NetworkListPane(RunnableCanvasPanel canvas, GameControllerService gameControllerService) {
        this.canvas = canvas;
        this.gameControllerService = gameControllerService;

        setName("Network list pane");
        setVisible(false);
        setDoubleBuffered(false);
//        setIgnoreRepaint(true);

        setLayout(new BorderLayout(1, 1));

        centerList = new SubPane(null) {{
//            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 1, 1));
//            setOpaque(true);
            setBackground(Color.PINK);
        }};

        JScrollPane centerScroll = new JScrollPane(centerList) {
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

        add(new SubPane("Сетевые миры:", Color.BLACK) {{
            add(centerScroll);
        }}, BorderLayout.CENTER);

        add(new FButton("Новое подключение", null) {{
            setBackground(Color.MAGENTA.darker().darker());
            addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    address = (String) new FOptionPane()
                            .buildFOptionPane("Подключиться по IP:", "Адрес сервера:",
                                    FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                    if (address.isBlank()) {
                        return;
                    }
                    address = address.replace(",", ".");
//                    if (canvas instanceof MenuCanvasRunnable mCanvas) {
//                        password = (String) new FOptionPane()
//                                .buildFOptionPane("Подключиться по IP:", "Пароль сервера:",
//                                        FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
//                        Constants.setConnectionAwait(true);
//                        new Thread(() -> mCanvas.connectToServer(NetConnectTemplate.builder()
//                                .address(address)
//                                .worldUid(null)
//                                .password(gameControllerService.getBcryptUtil().encode(password))
//                                .build())).start();
//                    }
                }
            });
        }}, BorderLayout.SOUTH);

        if (isVisible()) {
            reloadNet(canvas);
        }
    }

    public void reloadNet(RunnableCanvasPanel canvas) {
        centerList.removeAll();
        centerList.add(Box.createVerticalStrut(6));

        List<WorldDto> worlds = gameControllerService.getWorldService().findAllByNetAvailable(true);
        for (WorldDto world : worlds) {
            centerList.add(new SubPane(world.getName()) {{
                setWorld(world);
                setPreferredSize(new Dimension(
                        Constants.getUserConfig().isFullscreen()
                                ? (worlds.size() > 5 ? centerList.getWidth() - 8 : centerList.getWidth() - 4)
                                : (worlds.size() > 5 ? centerList.getWidth() - 24 : centerList.getWidth() - 8),
                        128));

                add(new JPanel() {{
                    setOpaque(false);
                    setBackground(Color.YELLOW);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);
                    setBorder(new EmptyBorder(-6, -3, -6, -3));

                    add(new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
//                            if (getWorld().getIcon() != null) {
//                                g.drawImage(getWorld().getIcon(), 0, 0, maxElementsDim, maxElementsDim, this);
//                                g.dispose();
//                            }
                        }

                        {
                            setOpaque(false);
                            setBackground(Color.CYAN.darker());
                            setIgnoreRepaint(true);
                            setDoubleBuffered(false);
                            setMinimumSize(new Dimension(maxElementsDim, maxElementsDim));
                            setPreferredSize(new Dimension(maxElementsDim, maxElementsDim));
                            setMaximumSize(new Dimension(maxElementsDim, maxElementsDim));
                        }
                    }, BorderLayout.WEST);

                    add(new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 1, 1)) {{
                        setOpaque(false);
                        setIgnoreRepaint(true);
                        setDoubleBuffered(false);

                        setAlignmentX(LEFT_ALIGNMENT);
                        setHeaderLabel(new ZLabel(("<html><pre>"
                                + "Уровень:<font color=#43F8C9><b>    %s</b></font>"
                                + "<br>Адрес:<font color=#fcba03><b>      %s</b></font>"
                                + "<br>Создано:<font color=#8805A8><b>    %s</b></font>"
                                + "<br> </pre></html>")
                                .formatted(getWorld().getHardnessLevel().getDescription(), getWorld().getAddress(),
                                        getWorld().getCreatedDate().format(Constants.DATE_FORMAT_3)),
                                null) {{
                            setBackground(Color.CYAN);
                        }});

                        add(getHeaderLabel());
                    }}, BorderLayout.CENTER);
                }}, BorderLayout.WEST);

                add(new JPanel(new BorderLayout(1, 1)) {{
                    setOpaque(false);
                    setBackground(Color.MAGENTA);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);
                    setBorder(null);

                    add(new JPanel(new BorderLayout(1, 1)) {
                        {
                            setOpaque(false);
                            setBackground(Color.BLUE);
                            setFocusable(false);
                            setDoubleBuffered(false);
                            setIgnoreRepaint(true);
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
//                                            if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
//                                                    "Вы хотите уничтожить данный мир\nбез возможности восстановления?",
//                                                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
//                                                    && canvas instanceof MenuCanvasRunnable mCanvas
//                                            ) {
//                                                deleteExistsWorldAndCloseThatPanel(getWorld().getUid());
//                                                reloadNet(canvas);
//                                                NetworkListPane.this.revalidate();
//                                            }
                                        }
                                    });
                                }
                            }, BorderLayout.NORTH);
                        }
                    }, BorderLayout.CENTER);

                    setConnButton(new FButton(" CONN ") {{
                        setBackground(Color.YELLOW);
                        setForeground(Color.WHITE);
                        setFocusPainted(false);
                        setMinimumSize(new Dimension(96, 96));
                        setPreferredSize(new Dimension(96, 96));
                        setMaximumSize(new Dimension(96, 96));
                        setAlignmentY(TOP_ALIGNMENT);

                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                pingActionThread.interrupt();
                                gameControllerService.getWorldService().setCurrentWorld(world.getUid());

                                Constants.setConnectionAwait(true);
                                if (getWorld().isLocal()) {
                                    canvas.serverUp(world);
                                } else {
                                    address = getWorld().getAddress();
                                    password = (String) new FOptionPane()
                                            .buildFOptionPane("Подключиться:", "Пароль сервера:",
                                                    FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                                    canvas.connectToServer(NetConnectTemplate.builder()
                                            .address(address)
                                            .worldUid(world.getUid())
                                            .password(gameControllerService.getBcryptUtil().encode(password))
                                            .build());
                                }
                            }
                        });
                    }});
                    add(getConnButton(), BorderLayout.EAST);
                }}, BorderLayout.EAST);
            }});
        }

        pingServers();
        NetworkListPane.this.revalidate();
    }

    private void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameControllerService.getWorldService().deleteByUid(worldUid);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (snap == null) {
            log.info("Net list snap...");
            BufferedImage bim = Constants.CACHE.getBufferedImage("menu_shadowed");
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);

        if (Constants.isConnectionAwait() || Constants.isPingAwait()) {
            g.setFont(Constants.GAME_FONT_02);
            Constants.RENDER.setRender((Graphics2D) g, FoxRender.RENDER.LOW);

            g.setColor(Color.BLACK);
            g.drawString(Constants.isPingAwait() ? pingString : connectionString,
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, Constants.isPingAwait() ? pingString : connectionString)) - 34,
                    getHeight() / 2 + 2);
            g.drawString(dot[dots],
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, dot[dots])) - 34, getHeight() / 2 + 18);

            g.setColor(Color.WHITE);
            g.drawString(Constants.isPingAwait() ? pingString : connectionString,
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, Constants.isPingAwait() ? pingString : connectionString)) - 32,
                    getHeight() / 2);
            g.drawString(dot[dots],
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, dot[dots])) - 32, getHeight() / 2 + 16);

            if (System.currentTimeMillis() - was > 500) {
                was = System.currentTimeMillis();
                dots = dots >= dot.length - 1 ? 0 : dots + 1;
            }
        }
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (super.isVisible() == isVisible) {
            return;
        }
        if (isVisible) {
            reloadNet(canvas);
            recalculate();
        }
        super.setVisible(isVisible);
    }

    private void pingServers() {
        Constants.setPingAwait(true);

        pingActionThread = Thread.startVirtualThread(() -> {
            // пинг нелокальных миров...
            Arrays.stream(centerList.getComponents()).filter(SubPane.class::isInstance).iterator().forEachRemaining(spn -> {
                if (pingActionThread.isInterrupted()) {
                    gameControllerService.getPingService().breakPing();
                    Constants.setPingAwait(false);
                    return;
                }

                String add = "<br>Доступен:%s</b></font></pre>";

                SubPane sp = (SubPane) spn;
                FButton connButton = sp.getConnButton();
                if (sp.getWorld().isLocal()) {
                    add = add.formatted("<font color=#394b5e><b>   (локальный)");
                    connButton.setBackground(Color.BLUE);
                } else {
                    String nad = sp.getWorld().getAddress();
                    String host = nad.contains(":") ? nad.split(":")[0] : nad;
                    Integer port = nad.contains(":") ? Integer.parseInt(nad.split(":")[1]) : null;
                    boolean isPingOk = pingServer(host, port, sp.getWorld().getUid());
                    add = add.formatted(isPingOk ? "<font color=#07ad3c><b>   (доступен)" : "<font color=#a31c25><b>   (не доступен)");
                    connButton.setBackground(isPingOk ? Color.GREEN : Color.GRAY);
                    connButton.setEnabled(isPingOk);
                }

                ZLabel spHeader = sp.getHeaderLabel();
                spHeader.setText(spHeader.getText().replace("<br> </pre>", add));
            });
            Constants.setPingAwait(false);
        });
    }

    /**
     * Проверка доступности удалённого сервера:
     *
     * @param host     адрес, куда стучимся для получения pong.
     * @param port     адрес, куда стучимся для получения pong.
     * @param worldUid uid мира, который пропинговывается.
     * @return успешность получения pong от удалённого Сервера.
     */
    private boolean pingServer(String host, Integer port, UUID worldUid) {
        return gameControllerService.getPingService().pingServer(host, port, worldUid);
    }

    @Override
    public void recalculate() {
//        setLocation((int) (canvas.getWidth() * 0.32d), 2);
//        setSize(new Dimension((int) (canvas.getWidth() * 0.68d), canvas.getHeight() - 4));
//        setBorder(new EmptyBorder((int) (getHeight() * 0.035d), 0, (int) (getHeight() * 0.015d), 32));
    }
}
