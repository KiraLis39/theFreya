package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.config.Constants;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VideoSettingsPane extends JPanel {
    private final List<DisplayMode> modes = new ArrayList<>(List.of(Constants.MON.getDevice().getDisplayModes()));

    private transient BufferedImage snap;

    private JZlider zlider;

    private CheckBokz cBox;

    private JComboBox<DisplayMode> displayModeBox;

    public VideoSettingsPane(FoxCanvas canvas) {
        setName("Video settings pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), (int) (getWidth() * 0.025d), (int) (getHeight() * 0.025d), 0));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // left panel:
        add(new JPanel() {{
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(new SubPane("Ограничить частоту кадров") {{
                cBox = new CheckBokz("fpsLimitedCheck") {{
                    setSelected(Constants.isFpsLimited());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (isSelected()) {
                                Constants.getUserConfig().setFpsLimit(zlider.getValue());
                                log.debug("Включено ограничение частоты кадров на {}", Constants.getUserConfig().getFpsLimit());
                            } else {
                                log.debug("Снято ограничение на частоту кадров");
                                Constants.getUserConfig().setFpsLimit(0);
                            }
                        }
                    });
                }};
                zlider = new JZlider("fpsLimiterSlider") {{
                    setMinimum(30);
                    setMaximum(Constants.MON.getRefreshRate());

                    setMinorTickSpacing(2);
                    setMajorTickSpacing(5);

                    setValue(Constants.getUserConfig().getFpsLimit());
                    addChangeListener(e -> {
                        if (cBox.isSelected()) {
                            Constants.getUserConfig().setFpsLimit(getValue());
                        }
                    });
                }};

                add(new SubPane("◑") {{
                    add(cBox);
                }}, BorderLayout.WEST);
                add(zlider, BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(12));
            add(new SubPane("Мультибуффер") {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 57));

                add(new SubPane(null) {{
                    add(new ZLabel("Использовать", null) {{
                        setVerticalAlignment(CENTER);
                    }});

                    add(new CheckBokz("useMultiBufferCheck") {{
                        setSelected(Constants.getUserConfig().isMultiBufferEnabled());
                        setAction(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Constants.getUserConfig().setMultiBufferEnabled(isSelected());
                            }
                        });
                    }}, BorderLayout.EAST);
                }}, BorderLayout.NORTH);

                add(new SubPane(null) {{
                    add(new ZLabel("Количество буферов", null) {{
                        setVerticalAlignment(CENTER);
                    }});
                    add(new JSpinner(new SpinnerNumberModel(Constants.getUserConfig().getBufferedDeep(), 1,
                            Constants.getUserConfig().getMaxBufferedDeep(), 1) {{
                        setBackground(Color.DARK_GRAY);
                        setForeground(Color.WHITE);
                    }}
                    ) {{
                        setBorder(null);
                        setFocusable(false);
                        setBackground(Color.DARK_GRAY);
                        setForeground(Color.WHITE);

                        addChangeListener(e -> Constants.getUserConfig().setBufferedDeep(Integer.parseInt(getValue().toString())));
                    }}, BorderLayout.EAST);
                }}, BorderLayout.SOUTH);
            }});

            modes.removeIf(nextMode -> nextMode.getRefreshRate() < 60 || nextMode.getWidth() < 1024);
            displayModeBox = new JComboBox<>(modes.toArray(new DisplayMode[0])) {{
                setBorder(null);
                setSelectedItem(Constants.MON.getDevice().getDisplayMode());
            }};

            add(new SubPane("Экран") {{
                add(displayModeBox);
                add(new FButton("Применить") {{
                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (!Constants.getUserConfig().isFullscreen()) {
                                new FOptionPane().buildFOptionPane("Ошибка", "Требуется полноэкранный режим", 10, false);
                            } else {
                                if (Constants.getDefaultDisplayMode() == null) {
                                    Constants.setDefaultDisplayMode(Constants.MON.getDevice().getDisplayMode());
                                }
                                try {
                                    DisplayMode chosenMode = (DisplayMode) displayModeBox.getSelectedItem();
                                    Constants.getUserConfig().setFpsLimit(chosenMode.getRefreshRate());
                                    Constants.MON.getDevice().setDisplayMode(chosenMode);
                                } catch (Exception e1) {
                                    log.error("Не удалось изменить разрешение монитора: {}", ExceptionUtils.getFullExceptionMessage(e1));
                                }
                            }
                        }
                    });
                }}, BorderLayout.SOUTH);
            }});

            add(Box.createVerticalStrut(6));

            add(Box.createVerticalStrut(canvas.getHeight()));
        }});

        add(Box.createHorizontalStrut(16));

        // right panel:
        add(new JPanel() {{
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(9));
            add(new SubPane(null) {{
                add(new CheckBokz("useSmoothingCheck") {{
                    setSelected(Constants.getUserConfig().isUseSmoothing());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setUseSmoothing(isSelected());
                        }
                    });
                }}, BorderLayout.WEST);

                add(new ZLabel("Использовать сглаживание", null), BorderLayout.CENTER);
            }});
            add(Box.createVerticalStrut(6));
            add(new SubPane(null) {{
                add(new CheckBokz("useBicubicCheck") {{
                    setSelected(Constants.getUserConfig().isUseBicubic());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setUseBicubic(isSelected());
                        }
                    });
                }}, BorderLayout.WEST);

                add(new ZLabel("Использовать бикубическое сглаживание", null), BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(canvas.getHeight()));
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Reload gameplay snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }
}
