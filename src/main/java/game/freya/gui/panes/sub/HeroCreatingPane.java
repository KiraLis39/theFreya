package game.freya.gui.panes.sub;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.enums.other.HeroCorpusType;
import game.freya.enums.other.HeroPeriferiaType;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.interfaces.iSubPane;
import game.freya.gui.panes.sub.components.FButton;
import game.freya.gui.panes.sub.components.JZlider;
import game.freya.gui.panes.sub.components.SubPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class HeroCreatingPane extends JPanel implements iSubPane {
    private static final Random r = new Random();

    private static final float rgb = 0.18f;

    private final Color hnrc = new Color(rgb, rgb, rgb, 0.75f);

    private transient BufferedImage snap;

    private transient Rectangle heroRamka;

    private JTextField ntf;

    private boolean isEditMode = false;

    @Getter
    private transient Image heroViewImage;

    @Getter
    private UUID worldUid;

    @Getter
    private String heroName;

    @Getter
    private HeroPeriferiaType chosenPeriferiaType = HeroPeriferiaType.COMPACT;

    @Getter
    private HeroCorpusType chosenCorpusType = HeroCorpusType.COMPACT;

    @Getter
    private short periferiaSize = 50;

    private JComboBox<HeroPeriferiaType> perChooser;

    private JComboBox<HeroCorpusType> corpChooser;

    private JZlider perSlider;

    private FButton bsb, sbc;

    @Getter
    private Color baseColor = Color.GREEN, secondColor = Color.DARK_GRAY;

    private transient HeroDTO editableHero;

    public HeroCreatingPane(FoxWindow window, GameController gameController) {
        setName("Hero creating pane");
        setVisible(false);
        setDoubleBuffered(false);
//        setIgnoreRepaint(true);

        recalculate(window);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(Box.createHorizontalStrut(window.getWidth() / 3));
        add(new JSeparator(SwingConstants.VERTICAL));
//        add(Box.createHorizontalStrut(9));
        add(new SubPane("Test hero panel") {{
            setBorder(new EmptyBorder((int) (getHeight() * 0.05d), 0, 0, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(new SubPane("Разные штуки героя") {{
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                add(Box.createVerticalStrut(6));
                add(new SubPane("Имя героя") {{
                    ntf = new JTextField(heroName, 20) {{
                        setEditable(!isEditMode);
                    }};
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
                    corpChooser = new JComboBox<>(HeroCorpusType.values()) {{
                        setSelectedItem(chosenCorpusType);
                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                chosenCorpusType = (HeroCorpusType) corpChooser.getSelectedItem();
                            }
                        });
                    }};
                    add(corpChooser);
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Тип периферии") {{
                    perChooser = new JComboBox<>(HeroPeriferiaType.values()) {{
                        setSelectedItem(chosenPeriferiaType);
                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                chosenPeriferiaType = (HeroPeriferiaType) perChooser.getSelectedItem();
                            }
                        });
                    }};
                    add(perChooser);
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Размер периферии") {{
                    perSlider = new JZlider("periferiaSizer") {{
                        setMinorTickSpacing(5);
                        setMajorTickSpacing(25);
                        setValue(periferiaSize);
                        setPaintTicks(false);

                        setBackground(Color.DARK_GRAY);
                        setForeground(Color.WHITE);
                        setFont(Constants.GAME_FONT_01);

                        addChangeListener(e -> periferiaSize = (short) getValue());
                    }};
                    add(perSlider);
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Основной цвет") {{
                    bsb = new FButton("choose") {{
                        setBackground(baseColor);
                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JColorChooser bcc = new JColorChooser(baseColor);
                                JOptionPane.showMessageDialog(null, Arrays.stream(bcc.getChooserPanels())
                                                .filter(p -> p.getDisplayName().equalsIgnoreCase("rgb")).findFirst()
                                                .orElse(bcc.getChooserPanels()[0]), "Выбор основного цвета героя:",
                                        JOptionPane.QUESTION_MESSAGE);

                                baseColor = bcc.getColor();
                                setBackground(baseColor);
                                recolorHeroView();
                            }
                        });
                    }};
                    add(bsb);
                }});
                add(Box.createVerticalStrut(8));
                add(new SubPane("Дополнительный цвет") {{
                    sbc = new FButton("choose") {{
                        setBackground(secondColor);
                        addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JColorChooser bcc = new JColorChooser(secondColor);
                                JOptionPane.showMessageDialog(null, Arrays.stream(bcc.getChooserPanels())
                                                .filter(p -> p.getDisplayName().equalsIgnoreCase("rgb")).findFirst()
                                                .orElse(bcc.getChooserPanels()[0]), "Выбор дополнительного цвета героя:",
                                        JOptionPane.QUESTION_MESSAGE);

                                secondColor = bcc.getColor();
                                setBackground(secondColor);
                                recolorHeroView();
                            }
                        });
                    }};
                    add(sbc);
                }});
            }});

            add(Box.createVerticalStrut(window.getHeight() / 3));
            add(new JSeparator(SwingConstants.HORIZONTAL));
            add(Box.createVerticalStrut(6));

            add(new JPanel() {{
                setLayout(new BorderLayout(3, 3));
//                setIgnoreRepaint(true);
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));
                setDoubleBuffered(false);

                add(new FButton("Готово") {{
                    setBackground(Color.GRAY);
                    addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (!isEditMode) {
                                worldUid = gameController.getCurrentWorldUid();
                                HeroDTO existsHero = gameController.findHeroByNameAndWorld(getHeroName(), worldUid);
                                if (existsHero != null) {
                                    new FOptionPane().buildFOptionPane("Провал:", "Герой с таким именем уже есть в этом мире");
                                } else {
                                    gameController.saveNewHeroAndPlay(HeroCreatingPane.this);
                                }
                            } else {
                                editableHero.setBaseColor(baseColor);
                                editableHero.setSecondColor(secondColor);
                                editableHero.setCorpusType(chosenCorpusType);
                                editableHero.setPeriferiaType(chosenPeriferiaType);
                                editableHero.setPeriferiaSize(periferiaSize);

                                gameController.justSaveAnyHero(editableHero);

                                HeroCreatingPane.this.setVisible(false);
                                window.getHeroesListPane().setVisible(true);
                            }
                        }
                    });
                }});
            }});

            add(Box.createVerticalStrut(3));
        }});
    }

    private void recolorHeroView() {
        int hexColor = (int) Long.parseLong("%02x%02x%02x%02x".formatted(223, // 191
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()), 16);
        heroViewImage = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(((Image) Constants.CACHE.get("player")).getSource(), new RGBImageFilter() {
                    @Override
                    public int filterRGB(final int x, final int y, final int rgb) {
                        return rgb & hexColor;
                    }
                }));
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
        if (heroRamka == null) {
            heroRamka = new Rectangle(0, (int) (getHeight() * 0.1d), getWidth() / 2 - 32, (int) (getHeight() * 0.8d));
        }
        g.setColor(Color.DARK_GRAY);
        g.drawRoundRect(heroRamka.x, heroRamka.y, heroRamka.width, heroRamka.height, 32, 32);

        // герой:
        if (heroViewImage == null) {
            heroViewImage = (BufferedImage) Constants.CACHE.get("player");
        }
        g.drawImage(heroViewImage,
                (heroRamka.x + heroRamka.width) / 2 - heroViewImage.getWidth(this) / 2, heroRamka.y + 32,
                heroViewImage.getWidth(this), heroViewImage.getHeight(this),
                this);

        // текст Скоро тут будет герой:
        g.setColor(Color.BLACK);
        g.drawString("ЗДЕСЬ БУДЕТ",
                (int) ((heroRamka.x + heroRamka.width) / 2d - Constants.FFB.getStringBounds(g, "ЗДЕСЬ БУДЕТ").getWidth() / 2d),
                (int) (getHeight() * 0.4d));
        g.drawString("ВНЕШНИЙ ВИД ГЕРОЯ",
                (int) ((heroRamka.x + heroRamka.width) / 2d - Constants.FFB.getStringBounds(g, "ВНЕШНИЙ ВИД ГЕРОЯ").getWidth() / 2d),
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
            repaint();
            return;
        }

        this.heroName = "Hero_%s".formatted(r.nextInt(1000));
        super.setVisible(isVisible);

        if (ntf != null) {
            ntf.setText(this.heroName);
            ntf.requestFocusInWindow();
            ntf.selectAll();
        }

        recolorHeroView();
    }

    public void load(HeroDTO template) {
        this.isEditMode = true;
        this.editableHero = template;
        this.ntf.setEditable(false);
        this.ntf.setEnabled(false);

        this.baseColor = template.getBaseColor();
        this.sbc.setBackground(baseColor);

        this.secondColor = template.getSecondColor();
        this.sbc.setBackground(secondColor);

        this.chosenCorpusType = template.getCorpusType();
        this.corpChooser.setSelectedItem(chosenCorpusType);

        this.chosenPeriferiaType = template.getPeriferiaType();
        this.perChooser.setSelectedItem(chosenPeriferiaType);

        this.periferiaSize = template.getPeriferiaSize();
        this.perSlider.setValue(periferiaSize);
    }

    @Override
    public void recalculate(FoxWindow window) {
        setLocation((int) (window.getWidth() * 0.34d), 2);
        setSize(new Dimension((int) (window.getWidth() * 0.66d), window.getHeight() - 4));
    }
}
