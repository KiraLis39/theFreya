package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.HardnessLevel;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

@Slf4j
public class NetCreatingPane extends WorldCreator {
    private static final Random r = new Random();
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

    public NetCreatingPane(FoxCanvas canvas) {
        setName("Net creating pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));

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
                    addActionListener(e -> hardnessLevel = Arrays.stream(HardnessLevel.values())
                            .filter(hl -> hl.getDescription().equals(getSelectedItem().toString())).findFirst().orElseThrow());
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
                            if (canvas instanceof MenuCanvas mCanvas) {
                                String curLocAddr;
                                try {
                                    curLocAddr = InetAddress.getLocalHost().toString().split("/")[1];

                                    WorldDTO aNewWorld = WorldDTO.builder()
                                            .author(canvas.getGameController().getCurrentPlayerUid())
                                            .createDate(LocalDateTime.now())
                                            .title(getWorldName())
                                            .level(getHardnessLevel())
                                            .isLocalWorld(true)
                                            .isNetAvailable(true)
                                            .passwordHash(getNetPasswordHash())
                                            .networkAddress(curLocAddr)
                                            .build();
                                    // mCanvas.saveNewWorldAndCreateHero(aNewWorld);
                                    mCanvas.serverUp(aNewWorld);
                                    setVisible(false);
                                } catch (UnknownHostException ex) {
                                    log.error("Не удалось создать мир на текущем локальном адресе");
                                }
                            }
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
        if (ntf != null) {
            ntf.setText(this.worldName);
        }
        super.setVisible(isVisible);
    }
}
