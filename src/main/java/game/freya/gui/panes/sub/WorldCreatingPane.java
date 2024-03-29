package game.freya.gui.panes.sub;

import fox.components.tools.VerticalFlowLayout;
import game.freya.config.Constants;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.other.HardnessLevel;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.templates.WorldCreator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class WorldCreatingPane extends WorldCreator implements iSubPane {
    private static final Random r = new Random();

    private transient BufferedImage snap;

    @Getter
    private String worldName;

    private JTextField ntf;

    @Getter
    private HardnessLevel hardnessLevel = HardnessLevel.EASY;

    @Getter
    private boolean isNetAvailable = false;

    @Getter
    private int netPasswordHash;

    private SubPane netPassPane;

    public WorldCreatingPane(WindowManager windowManager) {
        setName("World creating pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        recalculate(windowManager.getWindow());
        setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 12, 12));

        add(new SubPane("Создание игрового мира") {{
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
                            .filter(hl -> hl.getDescription()
                                    .equals(Objects.requireNonNull(getSelectedItem()).toString())).findFirst()
                            .orElseThrow());
                }});
            }});

            add(Box.createVerticalStrut(18));
            CheckBokz nac = new CheckBokz("netAvailableCheck") {{
                setAction(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        isNetAvailable = isSelected();
                        if (netPassPane != null) {
                            netPassPane.setVisible(isSelected());
                        }
                    }
                });
            }};
            netPassPane = new SubPane("Сетевой пароль:") {{
                setVisible(nac.isSelected());
                add(new JPasswordField(20) {{
                    addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            netPasswordHash = Arrays.hashCode(getPassword());
                        }
                    });
                }});
            }};

            add(new SubPane("Доступен для сети:") {{
                add(nac);
            }});

            add(Box.createVerticalStrut(9));

            add(netPassPane);

            add(Box.createVerticalStrut(9));
            add(new JSeparator(SwingConstants.HORIZONTAL));
            add(Box.createVerticalStrut(9));

            add(Box.createVerticalStrut(getHeight()));
        }});

        add(new SubPane("Готово") {
            {
                add(new FButton("Готово", null) {{
                    setBackground(Color.GRAY);
                    setPreferredSize(new Dimension(256, 32));
                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (windowManager.isMenuScreen()) {
                                WorldDTO aNewWorld = WorldDTO.builder()
                                        .uid(UUID.randomUUID())
                                        .author(windowManager.getWindow().getGameController().getCurrentPlayerUid())
                                        .createDate(LocalDateTime.now())
                                        .title(getWorldName())
                                        .level(getHardnessLevel())
                                        .isNetAvailable(false)
                                        .isLocalWorld(true)
                                        .build();
                                windowManager.getWindow().getGameController().saveNewLocalWorldAndCreateHero(aNewWorld);
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
            log.info("Reload world creating snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (super.isVisible() == isVisible) {
            repaint();
            return;
        }

        this.worldName = "World_%s".formatted(r.nextInt(1000));
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
