package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.templates.WorldCreator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

@Slf4j
public class NetCreatingPane extends WorldCreator implements iSubPane {
    private static final Random r = new Random();

    private final transient GameController gameController;

    private transient BufferedImage snap;

    @Getter
    private String worldName;

    private JTextField ntf;

    @Getter
    private HardnessLevel hardnessLevel = HardnessLevel.EASY;

    @Getter
    private boolean isNetAvailable = true;

    @Getter
    private int netPasswordHash;

    public NetCreatingPane(FoxWindow canvas, GameController gameController) {
        this.gameController = gameController;

        setName("Net creating pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        recalculate(canvas);
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));

        add(new SubPane("Создание сетевого мира") {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(32));
            add(new SubPane("Название мира:") {{
                ntf = new JTextField(worldName, 20);
                ntf.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        worldName = ntf.getText();
                    }
                });
                add(ntf);
            }});

            add(Box.createVerticalStrut(18));

            add(new SubPane("Уровень сложности:") {{
                add(new JComboBox<>(Arrays.stream(HardnessLevel.values()).map(HardnessLevel::getDescription).toArray()) {{
                    setSelectedIndex(1);
                    addActionListener(e -> hardnessLevel = Arrays.stream(HardnessLevel.values())
                            .filter(hl -> hl.getDescription().equals(Objects.requireNonNull(getSelectedItem()).toString()))
                            .findFirst().orElseThrow());
                }});
            }});

            add(Box.createVerticalStrut(18));

            add(new SubPane("Сетевой пароль:") {{
                add(new JTextField() {{
                    addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            netPasswordHash = getText().isBlank() ? -1 : getText().hashCode();
                        }
                    });
                }});
            }});

            add(Box.createVerticalStrut(getHeight() / 2));
            add(new JSeparator(SwingConstants.HORIZONTAL));
            add(Box.createVerticalStrut(9));

            add(Box.createVerticalStrut(getHeight()));
        }});

        add(new SubPane("Открыть") {
            {
                add(new FButton("Открыть") {{
                    setBackground(Color.DARK_GRAY);
                    setForeground(Color.WHITE);
                    setPreferredSize(new Dimension(256, 32));
                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ArrayList<String> addresses = new ArrayList<>();
                            try {
                                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                                while (interfaces.hasMoreElements()) {
                                    NetworkInterface iface = interfaces.nextElement();
                                    // filters out 127.0.0.1 and inactive interfaces
                                    if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) { //  || iface.getDisplayName().contains("Radmin")
                                        continue;
                                    }

                                    Iterator<InetAddress> addrIterator = iface.getInetAddresses().asIterator();
                                    while (addrIterator.hasNext()) {
                                        if (addrIterator.next() instanceof Inet4Address naf) {
                                            addresses.add(naf.getHostAddress());
                                        }
                                    }
                                }

                                if (addresses.isEmpty()) {
                                    addresses.add(InetAddress.getLocalHost().toString().split("/")[1]);
                                }
                            } catch (SocketException | UnknownHostException ex) {
                                addresses.add("localhost");
                            }

                            log.warn("Найдено сетей для размещения Сервера: {}. Будет использован адрес {}", addresses.size(), addresses.get(0));
                            WorldDTO aNewWorld = WorldDTO.builder()
                                    .author(canvas.getGameController().getCurrentPlayerUid())
                                    .createDate(LocalDateTime.now())
                                    .title(getWorldName())
                                    .level(getHardnessLevel())
                                    .isLocalWorld(true)
                                    .isNetAvailable(true)
                                    .passwordHash(getNetPasswordHash())
                                    .networkAddress(addresses.get(0))
                                    .build();
                            gameController.serverUp(aNewWorld);
                        }
                    });
                }});
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Reload net creating snap...");
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

        this.worldName = "Net_World_%s".formatted(r.nextInt(1000));
        super.setVisible(isVisible);

        if (ntf != null) {
            ntf.setText(this.worldName);
            ntf.requestFocusInWindow();
            ntf.selectAll();
        }
    }

    @Override
    public void recalculate(FoxWindow canvas) {
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));
    }
}
