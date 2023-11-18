package game.freya.gui.panes.sub;

import game.freya.config.Constants;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class VideoSettingsPane extends JPanel {
    private transient BufferedImage snap;

    private JZlider zlider;
    private CheckBokz cBox;

    public VideoSettingsPane(MenuCanvas canvas) {
        setName("Video settings pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, (int) (getHeight() * 0.035d), 64));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // left panel:
        add(new JPanel() {{
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(new SubPane("Ограничить частоту кадров") {{
                cBox = new CheckBokz("fpsLimitedCheck") {{
                    setSelected(Constants.isFrameLimited());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (isSelected()) {
                                Constants.getUserConfig().setScreenDiscreteLimit(zlider.getValue());
                                log.debug("Включено ограничение частоты кадров на {}",
                                        Constants.getUserConfig().getScreenDiscreteLimit());
                            } else {
                                log.debug("Снято ограничение на частоту кадров");
                                Constants.getUserConfig().setScreenDiscreteLimit(0);
                            }
                        }
                    });
                }};
                zlider = new JZlider("fpsLimiterSlider") {{
                    setMinimum(30);
                    setMaximum(Constants.MON.getRefreshRate());

                    setMinorTickSpacing(5);
                    setMajorTickSpacing(15);

                    setValue((int) Constants.getUserConfig().getScreenDiscreteLimit());
                    addChangeListener(e -> {
                        if (cBox.isSelected()) {
                            Constants.getUserConfig().setScreenDiscreteLimit(getValue());
                        }
                    });
                }};

                add(new SubPane("◑") {{
                    add(cBox);
                }}, BorderLayout.WEST);
                add(zlider, BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(12));
            add(new SubPane(null) {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 57));

                add(new ZLabel("Использовать мультибуфер", null) {{
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
            }});

            add(Box.createVerticalStrut(6));

            add(new SubPane(null) {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 57));

                add(new ZLabel("Размер мультибуфера", null) {{
                    setVerticalAlignment(CENTER);
                }});
                add(new JSpinner(new SpinnerNumberModel(2, 2, 3, 1)) {{
                    setValue(Constants.getUserConfig().getBufferedDeep());
                    setBorder(null);
                    setFocusable(false);
                    addChangeListener(e -> Constants.getUserConfig().setBufferedDeep(Integer.parseInt(getValue().toString())));
                }}, BorderLayout.EAST);
            }});

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
