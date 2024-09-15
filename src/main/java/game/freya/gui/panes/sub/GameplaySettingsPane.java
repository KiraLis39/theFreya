package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.Constants;
import game.freya.config.UserConfig;
import game.freya.gui.panes.handlers.RunnableCanvasPanel;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.CheckBokz;
import game.freya.gui.panes.sub.components.JTexztArea;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import game.freya.gui.panes.sub.components.ZLabel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class GameplaySettingsPane extends JPanel implements iSubPane {
    private transient BufferedImage snap;

    public GameplaySettingsPane(RunnableCanvasPanel canvas) {
        setName("Gameplay settings pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        recalculate(canvas);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // left panel:
        add(new JPanel() {{
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(new SubPane(null) {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 35));

                add(new CheckBokz("moveScreenCheck") {{
                    setSelected(Constants.getUserConfig().isDragGameFieldOnFrameEdgeReached());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setDragGameFieldOnFrameEdgeReached(isSelected());
                        }
                    });
                }}, BorderLayout.WEST);

                add(new ZLabel("Перемещать экран мышью", null), BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(6));
            add(new SubPane(null) {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 57));

                add(new CheckBokz("fullscreenTypeCheck") {{
                    setSelected(Constants.getUserConfig().getFullscreenType().equals(UserConfig.FullscreenType.EXCLUSIVE));
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (isSelected()) {
                                int accept = 0;
                                if (!FoxVideoMonitorUtil.isFullScreenSupported()) {
                                    accept = (int) new FOptionPane().buildFOptionPane("Внимание!",
                                            "Кажется, ваш монитор не поддерживает данный режим. Уверены?",
                                            FOptionPane.TYPE.YES_NO_TYPE, null, 10, true).get();
                                }

                                if (accept == 0) {
                                    Constants.getUserConfig().setFullscreenType(UserConfig.FullscreenType.EXCLUSIVE);
                                } else {
                                    setSelected(false);
                                }
                            } else {
                                Constants.getUserConfig().setFullscreenType(UserConfig.FullscreenType.MAXIMIZE_WINDOW);
                            }
                        }
                    });
                }}, BorderLayout.WEST);

                add(new JTextArea("Использовать эксклюзивный полный экран (не стабильно)", 3, 21) {{
                    setWrapStyleWord(true);
                    setLineWrap(true);
                    setBorder(null);
                    setFocusable(false);
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setForeground(Color.RED);
                    setFont(Constants.DEBUG_FONT);
                }}, BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(6));
            add(new SubPane("Видимость миникарты") {{
                setPreferredSize(new Dimension(canvas.getWidth() / 4, 64));
                add(new JZlider("minimapOpacitySlider") {{
                    setMinimum(25);
                    setMaximum(100);
                    setValue((int) (Constants.getUserConfig().getMiniMapOpacity() * 100));

                    setMinorTickSpacing(5);
                    setMajorTickSpacing(10);

                    addChangeListener(_ -> Constants.getUserConfig().setMiniMapOpacity(getValue() / 100f));
                }});
            }});

            add(Box.createVerticalStrut(canvas.getHeight()));
        }});

        add(Box.createHorizontalStrut(16));

        // right panel:
        add(new JPanel() {{
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new SubPane(null) {{
                add(new CheckBokz("pauseOnHideCheck") {{
                    setSelected(Constants.getUserConfig().isPauseOnHidden());
                    setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Constants.getUserConfig().setPauseOnHidden(isSelected());
                        }
                    });
                }}, BorderLayout.WEST);

                add(new JTexztArea("Авто-пауза при сворачивании (не для сетевой игры)", 4, 21), BorderLayout.CENTER);
            }});

            add(Box.createVerticalStrut(canvas.getHeight()));
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        if (snap == null) {
            log.info("Reload gameplay snap...");
            BufferedImage bim = Constants.CACHE.getBufferedImage("menu_shadowed");
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void recalculate(RunnableCanvasPanel canvas) {
        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setBorder(new EmptyBorder((int) (getHeight() * 0.05d), (int) (getWidth() * 0.025d), (int) (getHeight() * 0.025d), 0));
    }
}
