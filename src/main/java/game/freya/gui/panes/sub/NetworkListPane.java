package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import game.freya.gui.panes.sub.templates.WorldCreator;
import game.freya.net.NetConnectTemplate;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
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
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static game.freya.config.Constants.FFB;

@Slf4j
public class NetworkListPane extends WorldCreator {
    private static final int maxElementsDim = 96;
    private final transient FoxCanvas canvas;
    private final transient GameController gameController;
    private final SubPane centerList;
    private final String[] dot = new String[]{".", "..", "..."};
    private transient BufferedImage snap;
    @Getter
    private String address;
    @Getter
    private String password = "";
    private int dots = 0;
    private long was = System.currentTimeMillis();

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
                    address = (String) new FOptionPane()
                            .buildFOptionPane("Подключиться:", "Адрес сервера:",
                                    FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                    if (address.isBlank()) {
                        return;
                    }
                    if (canvas instanceof MenuCanvas mCanvas) {
                        password = (String) new FOptionPane()
                                .buildFOptionPane("Подключиться:", "Пароль сервера:",
                                        FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                        mCanvas.setConnectionAwait(true);
                        new Thread(() -> mCanvas.connectToNetworkWorld(NetConnectTemplate.builder()
                                .address(address)
                                .worldUid(null)
                                .passwordHash(password.hashCode())
                                .build())).start();
                    }
                }
            });
        }}, BorderLayout.SOUTH);

        if (isVisible()) {
            reloadNet(canvas);
            pingServers();
        }
    }

    private void reloadNet(FoxCanvas canvas) {
        centerList.removeAll();
        centerList.add(Box.createVerticalStrut(9));

        for (WorldDTO world : gameController.findAllWorldsByNetworkAvailable(true)) {
            centerList.add(new SubPane(world.getTitle()) {{
                setWorld(world);

                setHeaderLabel(new ZLabel(("<html><pre>"
                        + "Уровень:<font color=#43F8C9><b>    %s</b></font>"
                        + "<br>Создано:<font color=#8805A8><b>    %s</b></font>"
                        + "</pre></html>")
                        .formatted(getWorld().getLevel().getDescription(), getWorld().getCreateDate().format(Constants.DATE_FORMAT_3)),
                        null));
                add(new JPanel() {{
                    setOpaque(false);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);

                    add(new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            if (getWorld().getIcon() != null) {
                                g.drawImage(getWorld().getIcon(), 0, 2, maxElementsDim, maxElementsDim, this);
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

                        add(getHeaderLabel());
                    }}, BorderLayout.CENTER);
                }}, BorderLayout.WEST);

                add(new JPanel(new BorderLayout(1, 1)) {{
                    setOpaque(false);
                    setFocusable(false);
                    setDoubleBuffered(false);
                    setIgnoreRepaint(true);

                    add(new JPanel(new BorderLayout(1, 1)) {
                        {
                            setOpaque(false);
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
                                            if ((int) new FOptionPane().buildFOptionPane("Подтвердить:",
                                                    "Вы хотите уничтожить данный мир\nбез возможности восстановления?",
                                                    FOptionPane.TYPE.YES_NO_TYPE, Constants.getDefaultCursor()).get() == 0
                                                    && canvas instanceof MenuCanvas mCanvas
                                            ) {
                                                mCanvas.deleteExistsWorldAndCloseThatPanel(getWorld().getUid());
                                                reloadNet(canvas);
                                                pingServers();
                                                NetworkListPane.this.revalidate();
                                            }
                                        }
                                    });
                                }
                            }, BorderLayout.NORTH);
                        }
                    }, BorderLayout.CENTER);
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
//                                    address = getWorld().getNetworkAddress(); // todo: по идее должно работать так, просто надо сначала включать сервер!
                                    if (!getWorld().isLocalWorld()) {
                                        address = getWorld().getNetworkAddress();
                                        password = (String) new FOptionPane()
                                                .buildFOptionPane("Подключиться:", "Пароль сервера:",
                                                        FOptionPane.TYPE.INPUT, null, Constants.getDefaultCursor(), 0, true).get();
                                    } else {
                                        try {
                                            address = InetAddress.getLocalHost().toString().split("/")[1]; // 127.0.0.1 | 0.0.0.0
                                        } catch (UnknownHostException ex) {
                                            log.error("Не удается получить локальный хост: {}", ExceptionUtils.getFullExceptionMessage(ex));
                                        }
                                    }
                                    mCanvas.connectToNetworkWorld(NetConnectTemplate.builder()
                                            .address(address)
                                            .worldUid(world.getUid())
                                            .passwordHash(password.hashCode())
                                            .build());
                                }
                            }
                        });
                    }}, BorderLayout.EAST);
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
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);

        if (canvas.isConnectionAwait()) {
            g.setFont(Constants.PROPAGANDA_BIG_FONT);

            g.setColor(Color.BLACK);
            g.drawString("- CONNECTION -",
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, "- CONNECTION -")) - 3, getHeight() / 2);
            g.drawString(dot[dots],
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, dot[dots])) - 3, getHeight() / 2 + 16);

            g.setColor(Color.WHITE);
            g.drawString("- CONNECTION -",
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, "- CONNECTION -")) - 3, getHeight() / 2);
            g.drawString(dot[dots],
                    (int) (getWidth() / 2d - FFB.getHalfWidthOfString(g, dot[dots])) - 3, getHeight() / 2 + 16);

            if (System.currentTimeMillis() - was > 1000) {
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
            pingServers();
        }
        super.setVisible(isVisible);
    }

    private void pingServers() {
        // пинг нелокальных миров...
        Arrays.stream(centerList.getComponents()).filter(SubPane.class::isInstance).iterator().forEachRemaining(spn -> {
            SubPane sp = (SubPane) spn;
            ZLabel spHeader = sp.getHeaderLabel();
            new Thread(() -> {
                String add = "<br>Доступен:<font color=#239BEE><b>   %s</b></font></pre>";

                if (sp.getWorld().isLocalWorld()) {
                    add = add.formatted("(локальный)");
                } else if (sp.getWorld().isNetAvailable()) {
                    String nad = sp.getWorld().getNetworkAddress();
                    String host = nad.contains(":") ? nad.split(":")[0] : nad;
                    Integer port = nad.contains(":") ? Integer.parseInt(nad.split(":")[1]) : null;
                    add = add.formatted(canvas.ping(host, port) ? "(доступен)" : "(не доступен)");
                } else {
                    add = add.formatted("(не известно)");
                }

                spHeader.setText(spHeader.getText().replace("</pre>", add));
            }).start();
        });
    }

    @Override
    public String getWorldName() {
        return null;
    }

    @Override
    public HardnessLevel getHardnessLevel() {
        return null;
    }

    @Override
    public boolean isNetAvailable() {
        return false;
    }

    @Override
    public int getNetPasswordHash() {
        return 0;
    }
}
