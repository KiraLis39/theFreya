package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.HeroCorpusType;
import game.freya.enums.HeroPeriferiaType;
import game.freya.gui.panes.MenuCanvas;
import game.freya.gui.panes.handlers.FoxCanvas;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class HeroCreatingPane extends JPanel {
    private static final Random r = new Random();
    private final float hnrci = 0.18f;
    private final Color hnrc = new Color(hnrci, hnrci, hnrci, 0.75f);
    private transient BufferedImage snap;
    @Getter
    private UUID worldUid;
    @Getter
    private String heroName;
    private JTextField ntf;

    public HeroCreatingPane(FoxCanvas canvas, GameController gameController) {
        setName("Hero creating pane");
        setVisible(false);
        setDoubleBuffered(false);
        setIgnoreRepaint(true);

        setLocation((int) (canvas.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (canvas.getWidth() * 0.66d), canvas.getHeight() - 4));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(Box.createHorizontalStrut(canvas.getWidth() / 3));
        add(new JSeparator(SwingConstants.VERTICAL));
//        add(Box.createHorizontalStrut(9));
        add(new SubPane("Test hero panel") {{
            setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(new SubPane("Разные штуки героя") {{
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                add(Box.createVerticalStrut(6));
                add(new SubPane("Имя героя") {{
                    ntf = new JTextField(heroName, 20);
                    ntf.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            heroName = ntf.getText();
                        }
                    });
                    add(ntf);
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Тип корпуса") {{
                    add(new JComboBox<>(HeroCorpusType.values()));
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Тип периферии") {{
                    add(new JComboBox<>(HeroPeriferiaType.values()));
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Размер периферии") {{
                    add(new JZlider("periferiaSizer") {{
                        setMinimum(0);
                        setMaximum(100);
                        setMinorTickSpacing(5);
                        setMajorTickSpacing(25);
                        setValue(50);

                        setBackground(Color.DARK_GRAY);
                        setForeground(Color.WHITE);
                        setFont(Constants.GAME_FONT_01);

                        setPaintTicks(false);
                    }});
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Основной цвет") {{
                    add(new FButton("choose"));
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Дополнительный цвет") {{
                    add(new FButton("choose"));
                }});
            }});

            add(Box.createVerticalStrut(canvas.getHeight() / 3));
            add(new JSeparator(SwingConstants.HORIZONTAL));
            add(Box.createVerticalStrut(6));

            add(new JPanel() {{
                setLayout(new BorderLayout(3, 3));
                setIgnoreRepaint(true);
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));
                setDoubleBuffered(false);

                add(new FButton("Готово") {{
                    setBackground(Color.GRAY);
                    setFocusPainted(false);
                    setDoubleBuffered(false);
                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            worldUid = gameController.getCurrentWorldUid();
                            HeroDTO existsHero = gameController.findHeroByNameAndWorld(getHeroName(), worldUid);
                            if (existsHero != null) {
                                new FOptionPane().buildFOptionPane("Провал:", "Герой с таким именем уже есть в этом мире");
                            } else {
                                if (canvas instanceof MenuCanvas mCanvas) {
                                    mCanvas.createNewHeroForNewWorldAndCloseThatPanel(HeroCreatingPane.this);
                                }
                            }
                        }
                    });
                }});
            }});

            add(Box.createVerticalStrut(3));
        }});
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (snap == null) {
            log.info("Reload hero creating snap...");
            BufferedImage bim = ((BufferedImage) Constants.CACHE.get("backMenuImageShadowed"));
            snap = bim.getSubimage((int) (bim.getWidth() * 0.335d), 0,
                    (int) (bim.getWidth() - bim.getWidth() * 0.3345d), bim.getHeight());
        }
        // фоновый рисунок:
        g.drawImage(snap, 0, 0, getWidth(), getHeight(), this);

        // для верного расчета границ шрифта далее:
        g.setFont(Constants.PROPAGANDA_FONT);

        // рамка вокруг героя:
        g.setColor(Color.DARK_GRAY);
        g.drawRoundRect(
                0, (int) (getHeight() * 0.1d),
                getWidth() / 2 - 32, (int) (getHeight() * 0.8d),
                32, 32);

        g.setColor(Color.DARK_GRAY);
        g.drawString("ЗДЕСЬ БУДЕТ",
                (int) ((getWidth() / 2d - 32d) / 2d - Constants.FFB.getStringBounds(g, "ЗДЕСЬ БУДЕТ").getWidth() / 2d),
                (int) (getHeight() * 0.4d));
        g.drawString("ВНЕШНИЙ ВИД ГЕРОЯ",
                (int) ((getWidth() / 2d - 32d) / 2d - Constants.FFB.getStringBounds(g, "ВНЕШНИЙ ВИД ГЕРОЯ").getWidth() / 2d),
                (int) (getHeight() * 0.445d));

        // прямоугольник под именем героя:
        g.setColor(hnrc);
        if (heroName != null) {
            g.fillRect((int) (getWidth() * 0.025d), (int) (getHeight() * 0.075d),
                    (int) (Constants.FFB.getStringBounds(g, heroName).getWidth() + 3), (int) (getHeight() * 0.05d));
        }

        // имя героя:
        if (heroName != null) {
            g.setColor(Color.GRAY);
            g.drawString(heroName, (int) (getWidth() * 0.028d), (int) (getHeight() * 0.109d));
        }
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (super.isVisible() == isVisible) {
            return;
        }

        this.heroName = "Hero_%s".formatted(r.nextInt(1000));
        if (ntf != null) {
            ntf.setText(this.heroName);
        }

        super.setVisible(isVisible);
    }
}
