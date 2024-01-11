package game.freya.gui.panes;

import fox.components.FOptionPane;
import game.freya.GameController;
import game.freya.config.Constants;
import game.freya.entities.dto.HeroDTO;
import game.freya.entities.dto.WorldDTO;
import game.freya.enums.other.ScreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.WindowManager;
import game.freya.gui.panes.handlers.FoxWindow;
import game.freya.gui.panes.sub.HeroCreatingPane;
import game.freya.gui.panes.sub.NetworkListPane;
import game.freya.net.data.NetConnectTemplate;
import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3d;

@Slf4j
public class MenuWindow extends FoxWindow {
    private final GameController gameController;

    private final Font f = new Font("Serif", Font.BOLD, 48); // Times New Roman | Serif | Courier New

    private double widthMemory = -1;

    private volatile float leftShift, upShift, downShift, verticalSpace = -1, btnHeight, btnWidth;

    public MenuWindow(WindowManager windowManager, GameController gameController) {
        super(ScreenType.MENU_SCREEN, "MenuCanvas", windowManager, gameController);
        this.gameController = gameController;

//        if (gameController.isServerIsOpen()) {
//            gameController.closeServer();
//            log.error("Мы в меню, но Сервер ещё запущен! Закрытие Сервера...");
//        }
//        if (gameController.isSocketIsOpen()) {
//            gameController.closeSocket();
//            log.error("Мы в меню, но соединение с Сервером ещё запущено! Закрытие подключения...");
//        }

        init();
    }

    @Override
    public void init() {
        // перевод курсора в режим меню:
        Constants.setAltControlMode(true, getWindow());

        createWindowContext(ScreenType.MENU_SCREEN);

        // load textures:
        if (Constants.getGameConfig().isUseTextures()) {
            gameController.loadMenuTextures();
        }
    }

//    private STBTruetype tt = STBTruetype.;
//    private final UnicodeFont ttf01 = new UnicodeFont(f, 24, true, false);
//    private final UnicodeFont ttf02 = new UnicodeFont(f, 48, false, false);
//    private final UnicodeFont ttf03 = new UnicodeFont(f.deriveFont(Font.PLAIN, 32));

    @Override
    public void render() {
        configureThis();
        glFrontFace(GL_CW);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glPushMatrix();

        drawBackground();
        drawGreyCorner();
        drawMenu();
        drawGameInfo();
        drawDebug(getWidth(), getHeight(), null);
        super.render();

        glPopMatrix();

//        ttf01.drawString(0.25f, -0.25f, "something else 12345", Color.cyan);
//        ttf01.drawString(0.25f, 0.25f, "something else 12345", Color.blue);
//        ttf02.drawString(0.0f, 0.1f, "something else 12345", Color.yellow);
//        ttf02.drawString(16f, 48f, "something else 12345", Color.magenta);
//        ttf03.drawString(0, 0, "something else 12345", Color.green);
//        ttf03.drawString(0.25f, -0.25f, "something else 12345", Color.green);
//        ttf03.drawString(0.25f, 0.25f, "something else 12345", Color.green);
    }

    private void drawBackground() {
        if (gameController.isTextureExist("menu")) {
            gameController.bindTexture("menu");
        }

        glBegin(GL_QUADS);

        glColor3f(0.75f, 0.75f, 0.75f);
        glNormal3f(0, 0, -1);

        glTexCoord2f(0, 0);
        glVertex2d(0, 0);

        glTexCoord2f(1, 0);
        glVertex2d(getWidth(), 0);

        glTexCoord2f(1, 1);
        glVertex2d(getWidth(), getHeight());

        glTexCoord2f(0, 1);
        glVertex2d(0, getHeight());

        glEnd();

        gameController.unbindTexture("menu");
    }

    private void drawGreyCorner() {
        // glEnable(GL_BLEND);

        glBegin(GL_QUADS);

        glColor4f(0.0f, 0.0f, 0.0f, 0.8f);

        glVertex2d(0.0f, 0.0f);
        glVertex2d(getWidth() / 3.5f, 0);
        glVertex2d(getWidth() / 4.0f, getHeight());
        glVertex2d(0, getHeight());

        glEnd();

        // glDisable(GL_BLEND);
    }

    private void drawMenu() {
        if (getWidth() != widthMemory) {
            verticalSpace = 8.5f;
            btnHeight = getHeight() * 0.08f;
            btnWidth = getWidth() * 0.18f;
            leftShift = getWidth() * 0.0225f;
            upShift = getHeight() * 0.15f;
            downShift = 25.0f;
            widthMemory = getWidth();
        }

        if (gameController.isTextureExist("metallicBtnOFF")) {
            gameController.bindTexture("metallicBtnOFF");
        }

        glBegin(GL_QUADS);

        glColor4f(0.2f, 0.4f, 0.75f, 0.5f);
        glNormal3f(0, 0, -1);

        drawButton("start", new Rectangle2D.Float(leftShift, upShift, btnWidth, btnHeight));
        drawButton("net game", new Rectangle2D.Float(leftShift, upShift + (verticalSpace + btnHeight),
                btnWidth, btnHeight));
        drawButton("options", new Rectangle2D.Float(leftShift, upShift + (verticalSpace + btnHeight) * 2,
                btnWidth, btnHeight));
        drawButton("something else", new Rectangle2D.Float(leftShift, upShift + (verticalSpace + btnHeight) * 3,
                btnWidth, btnHeight));
        drawButton("something else", new Rectangle2D.Float(leftShift, upShift + (verticalSpace + btnHeight) * 4,
                btnWidth, btnHeight));

        glNormal3f(0, 0, 1);
        drawButton("exit", new Rectangle2D.Float(leftShift, getHeight() - downShift - btnHeight,
                btnWidth, btnHeight));

        glEnd();

        gameController.unbindTexture("metallicBtnOFF");
    }

    private void drawButton(String text, Rectangle2D btnRect) {
        glTexCoord2f(0, 0);
        glVertex3d(btnRect.getX(), btnRect.getY(), 0.2f);

        glTexCoord2f(1, 0);
        glVertex3d(btnRect.getX() + btnRect.getWidth(), btnRect.getY(), 0.2f);

        glTexCoord2f(1, 1);
        glVertex3d(btnRect.getX() + btnRect.getWidth(), btnRect.getY() + btnRect.getHeight(), 0.2f);

        glTexCoord2f(0, 1);
        glVertex3d(btnRect.getX(), btnRect.getY() + btnRect.getHeight(), 0.2f);
    }

    private void drawGameInfo() {

    }

    public void deleteExistsWorldAndCloseThatPanel(UUID worldUid) {
        log.info("Удаление мира {}...", worldUid);
        gameController.deleteWorld(worldUid);
    }

    public void deleteExistsPlayerHero(UUID heroUid) {
        gameController.deleteHero(heroUid);
    }

    public void openCreatingNewHeroPane(HeroDTO template) {
        getHeroesListPane().setVisible(false);
        getHeroCreatingPane().setVisible(true);
        if (template != null) {
            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
        }
    }

    public void mouseReleased() {
        if (isFirstButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getAudiosPane().isVisible()) {
                    getAudiosPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getWorldsListPane().isVisible()) {
                getWorldsListPane().setVisible(false);
                getWorldCreatingPane().setVisible(true);
            } else if (getHeroesListPane().isVisible()) {
                openCreatingNewHeroPane(null);
            } else if (getNetworkListPane().isVisible()) {
                getNetworkListPane().setVisible(false);
                getNetworkCreatingPane().setVisible(true);
            } else {
                if (gameController.findAllWorldsByNetworkAvailable(false).isEmpty()) {
                    getWorldCreatingPane().setVisible(true);
                } else {
                    getWorldsListPane().setVisible(true);
                }
            }
        }

        if (isSecondButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                // нажато Настройки графики:
                if (!getVideosPane().isVisible()) {
                    getVideosPane().setVisible(true);
                    getAudiosPane().setVisible(false);
                    getHotkeysPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (getNetworkListPane().isVisible()) {
                ((NetworkListPane) getNetworkListPane()).reloadNet(this);
            } else {
                getNetworkListPane().setVisible(true);
            }
        }

        if (isThirdButtonOver()) {
            if (!isOptionsMenuSetVisible() && !getHeroCreatingPane().isVisible() && !getWorldsListPane().isVisible()) {
                setOptionsMenuSetVisible(true);
                getAudiosPane().setVisible(true);
            } else if (getHeroCreatingPane().isVisible()) {
                Constants.showNFP();
            } else if (isOptionsMenuSetVisible()) {
                if (!getHotkeysPane().isVisible()) {
                    getHotkeysPane().setVisible(true);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                    getGameplayPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isFourthButtonOver()) {
            if (isOptionsMenuSetVisible()) {
                if (!getGameplayPane().isVisible()) {
                    getGameplayPane().setVisible(true);
                    getHotkeysPane().setVisible(false);
                    getVideosPane().setVisible(false);
                    getAudiosPane().setVisible(false);
                }
            } else {
                Constants.showNFP();
            }
        }

        if (isExitButtonOver()) {
            onExitBack();
        }
    }

    /**
     * Когда создаём локальный, несетевой мир - идём сюда, для его сохранения и указания как текущий мир в контроллере.
     *
     * @param newWorld модель нового мира для сохранения.
     */
    public void saveNewLocalWorldAndCreateHero(WorldDTO newWorld) {
        gameController.setCurrentWorld(gameController.saveNewWorld(newWorld));
        chooseOrCreateHeroForWorld(gameController.getCurrentWorldUid());
    }

    // NETWORK game methods:
    public void serverUp(WorldDTO aNetworkWorld) {
        getNetworkListPane().repaint(); // костыль для отображения анимации

        // Если игра по сети, но Сервер - мы, и ещё не запускался:
        gameController.setCurrentWorld(gameController.saveNewWorld(aNetworkWorld));

        // Открываем локальный Сервер:
        if (gameController.isCurrentWorldIsLocal() && gameController.isCurrentWorldIsNetwork() && !gameController.isServerIsOpen()) {
            if (gameController.openServer()) {
                log.info("Сервер сетевой игры успешно активирован на {}", gameController.getServerAddress());
            } else {
                log.warn("Что-то пошло не так при активации Сервера.");
                new FOptionPane().buildFOptionPane("Server error:", "Что-то пошло не так при активации Сервера.", 60, true);
                return;
            }
        }

        if (gameController.isSocketIsOpen()) {
            log.error("Socket should was closed here! Closing...");
            gameController.closeSocket();
        }

        // Подключаемся к локальному Серверу как новый Клиент:
        connectToServer(NetConnectTemplate.builder()
                .address(aNetworkWorld.getNetworkAddress())
                .passwordHash(aNetworkWorld.getPasswordHash())
                .worldUid(aNetworkWorld.getUid())
                .build());
    }

    public void connectToServer(NetConnectTemplate connectionTemplate) {
        getHeroesListPane().setVisible(false);

        setConnectionAwait(true);
        getNetworkListPane().repaint(); // костыль для отображения анимации

        if (connectionTemplate.address().isBlank()) {
            new FOptionPane().buildFOptionPane("Ошибка адреса:", "Адрес сервера не может быть пустым.", 10, true);
        }

        // 1) приходим сюда с host:port для подключения
        String address = connectionTemplate.address().trim();
        String h = address.contains(":") ? address.split(":")[0].trim() : address;
        Integer p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;
        getNetworkListPane().repaint(); // костыль для отображения анимации
        try {
            // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
            if (gameController.connectToServer(h.trim(), p, connectionTemplate.passwordHash())) {
                // 3) проверка героя в этом мире:
                chooseOrCreateHeroForWorld(gameController.getCurrentWorldUid());
            } else {
                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, gameController.getLocalSocketConnection().getLastExplanation());
            }
        } catch (GlobalServiceException gse) {
            log.warn("GSE here: {}", gse.getMessage());
            if (gse.getErrorCode().equals("ER07")) {
                new FOptionPane().buildFOptionPane("Не доступно:", gse.getMessage(), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            }
        } catch (IllegalThreadStateException tse) {
            log.error("Connection Thread state exception: {}", ExceptionUtils.getFullExceptionMessage(tse));
        } catch (Exception e) {
            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка подключения '%s'.\n"
                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10/13:13958)")
                    .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
        } finally {
            //gameController.closeSocket();
            setConnectionAwait(false);
        }
    }


    /**
     * Приходим сюда для создания нового героя для мира.
     *
     * @param newHeroTemplate модель нового героя для игры в новом мире.
     */
    public void saveNewHeroAndPlay(HeroCreatingPane newHeroTemplate) {
        // сохраняем нового героя и проставляем как текущего:
        HeroDTO aNewToSave = new HeroDTO();

        aNewToSave.setBaseColor(newHeroTemplate.getBaseColor());
        aNewToSave.setSecondColor(newHeroTemplate.getSecondColor());

        aNewToSave.setCorpusType(newHeroTemplate.getChosenCorpusType());
        aNewToSave.setPeriferiaType(newHeroTemplate.getChosenPeriferiaType());
        aNewToSave.setPeriferiaSize(newHeroTemplate.getPeriferiaSize());

        aNewToSave.setWorldUid(newHeroTemplate.getWorldUid());
        aNewToSave.setCharacterUid(UUID.randomUUID());
        aNewToSave.setCharacterName(newHeroTemplate.getHeroName());
        aNewToSave.setOwnerUid(gameController.getCurrentPlayerUid());
        aNewToSave.setCreateDate(LocalDateTime.now());

        gameController.saveNewHero(aNewToSave, true);

        // если подключение к Серверу уже закрылось пока мы собирались:
        if (gameController.isCurrentWorldIsNetwork() && !gameController.isServerIsOpen()) {
            log.warn("Сервер уже закрыт. Требуется повторное подключение.");
            getHeroCreatingPane().setVisible(false);
            getHeroesListPane().setVisible(false);
            getNetworkListPane().setVisible(true);
            return;
        }

        playWithThisHero(gameController.getCurrentHero());
    }
}
