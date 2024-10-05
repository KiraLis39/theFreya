package game.freya.gui;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.StreamAssetInfo;
import com.jme3.awt.AWTSettingsDialog;
import com.jme3.cursors.plugins.CursorLoader;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import fox.utils.FoxVideoMonitorUtil;
import game.freya.config.ApplicationProperties;
import game.freya.config.Constants;
import game.freya.config.Controls;
import game.freya.enums.gui.FullscreenType;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.gui.states.MainMenuState;
import game.freya.gui.states.substates.global.DebugInfoState;
import game.freya.gui.states.substates.global.ExitHandlerState;
import game.freya.gui.states.substates.global.NiftyTestState;
import game.freya.gui.states.substates.global.OptionsState;
import game.freya.gui.states.substates.menu.MenuBackgState;
import game.freya.services.GameControllerService;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
@Setter
@Getter
public class JMEApp extends SimpleApplication {
    private ApplicationProperties props;
    private GameControllerService gameControllerService;
    private OptionsState optionsState;
    private volatile boolean isReady;

    public JMEApp(GameControllerService gameControllerService, AppSettings settings, ApplicationProperties props) {
        this.gameControllerService = gameControllerService;
        this.props = props;

        // show game settings dialog:
        if (Constants.getUserConfig().isShowSettingsOnLaunch()) {
            if (!AWTSettingsDialog.showDialog(settings, false)) {
                throw new GlobalServiceException(ErrorMessages.WRONG_STATE, "Settings change was canceled");
            }
            // update settings by User wish:
            Constants.getUserConfig().setUseVSync(settings.isVSync());
            Constants.getUserConfig().setFpsLimit(settings.getFrequency());
            Constants.getUserConfig().setMultiSamplingLevel(settings.getSamples());
            Constants.getUserConfig().setWindowWidth(settings.getWindowWidth());
            Constants.getUserConfig().setWindowHeight(settings.getHeight()); // todo: а делить на aspect?
        }
        if (!settings.isGammaCorrection()) {
            log.error("Внимание! Гамма-коррекция отключена настройками приложения! Это не штатный режим работы!");
            settings.setGammaCorrection(true);
        }
        setSettings(settings);

        // игра может быть сетевой, так что сами контролируем когда пауза:
        setPauseOnLostFocus(false);

        // снижаем FPS при потере фокуса окном игры (вплоть до 20 fps):
        // (хотя всё это уже кастомизировано в this.loseFocus()\this.gainFocus()...)
        setLostFocusBehavior(LostFocusBehavior.ThrottleOnLostFocus);
    }

    /* Initialize the game scene here */
    @Override
    public void simpleInitApp() {
        // здесь регистрируем все шрифты, статические картинки, звуки и т.п...:
        registerAppResources(Constants.getGameCanvas().getAssetManager());

        // прикручиваем общие стейты для всего процесса игры, от меню до геймплея:
        attachBaseAppStates();

        // запуск игрового окна:
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        setReady(true);
    }

    private void registerAppResources(AssetManager assetManager) {
        Constants.setFontDefault(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        Constants.setFontConsole(assetManager.loadFont("Interface/Fonts/Console.fnt"));
    }

    /**
     * Здесь прикрепляем к приложению стейты, которые должны быть активны не зависимо от того,
     * где мы находимся - в меню игры или в самом процессе геймплея. Например выход из приложения или Нифти.
     */
    private void attachBaseAppStates() {
        // контроллер выхода из игры:
        stateManager.attach(new ExitHandlerState(gameControllerService));

        // удаляем дефолтный debug info state и ставим свой state:
        stateManager.detach(stateManager.getState(StatsAppState.class, false));
        stateManager.attach(new DebugInfoState());

        // the options state:
        optionsState = new OptionsState(gameControllerService, props);
        stateManager.attach(optionsState);

        // подключаем меню настроек, опций игры для меню и геймпея:
        stateManager.attach(new NiftyTestState());
    }

    // tpf большой на медленных ПК и маленький на быстрых ПК.
    @Override
    public void simpleUpdate(float tpf) {
        // todo: ...
    }

    @Override
    public void simpleRender(RenderManager rm) {
        // todo: add render code
    }

    @Override
    public void requestClose(boolean esc) {
        stateManager.getState(ExitHandlerState.class).onExit();
    }

    public void setScene(BaseAppState state) {
        log.info("Loading the scene '{}'...", state.getId());
        stateManager.attach(state);

        enqueue(() -> {
            // вывод в debug info имени текущего state:
            stateManager.getState(DebugInfoState.class).currentStateId(state.getId());
            stateManager.getState(DebugInfoState.class).rebuildFullText();
        });
    }

    public void toggleFullscreen() {
        AppSettings settings = Constants.getGameCanvas().getContext().getSettings();
        DisplayMode vMode = FoxVideoMonitorUtil.getDisplayMode();
        Dimension dDim = FoxVideoMonitorUtil.getConfiguration().getBounds().getSize();

        if (Constants.getUserConfig().isUseVSync()) {
            settings.setFrequency(vMode.getRefreshRate()); // use VSync
        } else if (Constants.getUserConfig().getFpsLimit() > 0) {
            settings.setFrequency(Constants.getUserConfig().getFpsLimit()); // use fps limit
        } else {
            settings.setFrequency(-1); // unlimited
        }

        if (Constants.getUserConfig().isFullscreen()) {
            restoreToWindow(settings);
            Constants.getUserConfig().setFullscreen(false);
        } else if (FoxVideoMonitorUtil.isFullScreenSupported()) {
            switch (Constants.getUserConfig().getFullscreenType()) {
                case EXCLUSIVE -> doExclusive(settings, vMode, dDim);
                case MAXIMIZE_WINDOW -> doMaximize(settings, vMode, dDim);
                case null, default ->
                        log.error("Некорректное указание режима окна '{}'", Constants.getUserConfig().getFullscreenType());
            }
            Constants.getUserConfig().setFullscreen(true);
        }

        settings.setFullscreen(Constants.getUserConfig().isFullscreen());
        log.info("Fullscreen mode now: {} ({})", Constants.getUserConfig().isFullscreen(), settings.isFullscreen());

        Constants.getGameCanvas().restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек
        Constants.getGameFrame().reloadCanvasDim();
    }

    private void doExclusive(AppSettings settings, DisplayMode vMode, Dimension dDim) {
        log.info("Do exclusive window fullscreen...");
        // frame:
        FoxVideoMonitorUtil.setFullscreen(Constants.getGameFrame());

        // canvas:
        settings.setResolution(dDim.width, dDim.height);
        settings.setBitsPerPixel(vMode.getBitDepth());
        settings.setFullscreen(true);
    }

    private void doMaximize(AppSettings settings, DisplayMode vMode, Dimension dDim) {
        log.info("Do pseudo maximize window fullscreen...");
        // frame:
        Constants.getGameFrame().dispose();
        Constants.getGameFrame().setUndecorated(true);
        // +1 нужен, иначе будет переходить в блокирующий полный режим:
        Constants.getGameFrame().setSize(dDim.width + 1, dDim.height + 1);
        Constants.getGameFrame().setState(Frame.MAXIMIZED_BOTH);
        Constants.getGameFrame().setLocationRelativeTo(null);
        Constants.getGameFrame().setVisible(true);

        // canvas:
        settings.setResolution(vMode.getWidth(), vMode.getHeight());
        settings.setFullscreen(true);
    }

    private void restoreToWindow(AppSettings settings) {
        log.info("Restore from fullscreen mode...");
        // frame:
        FoxVideoMonitorUtil.setFullscreen(null);
        Constants.getGameFrame().setState(Frame.NORMAL);
        Constants.getGameFrame().setLocationRelativeTo(null);
        Constants.getGameFrame().setPreferredSize(new Dimension(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight()));
        Constants.getGameFrame().setSize(new Dimension(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight()));
        Constants.getGameFrame().setSize(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight());
        Constants.getGameFrame().setLocationRelativeTo(null);

        if (Constants.getUserConfig().getFullscreenType().equals(FullscreenType.MAXIMIZE_WINDOW)) {
            Constants.getGameFrame().dispose();
            Constants.getGameFrame().setUndecorated(false);
            Constants.getGameFrame().setVisible(true);
        }

        // canvas:
        settings.setFullscreen(false);
        settings.setBitsPerPixel(settings.getDepthBits());
        settings.setResolution(Constants.getUserConfig().getWindowWidth(), Constants.getUserConfig().getWindowHeight());
    }

    public void setAltControlMode(boolean altMode) {
        if (altMode) {
            try {
                JmeCursor defCur = new CursorLoader().load(new StreamAssetInfo(assetManager,
                        new AssetKey<>("cur.cur"), Constants.CACHE.getResourceStream("cur")));
                defCur.setxHotSpot(10);
                defCur.setyHotSpot(25);
//                Mouse.setNativeCursor(defCur);
                mouseInput.setNativeCursor(defCur);
            } catch (Exception e) {
                log.error("Не удалось прочитать курсор: {}", ExceptionUtils.getFullExceptionMessage(e));
            }

            flyCam.setEnabled(false);
            flyCam.setDragToRotate(false);
            stateManager.detach(getStateManager().getState(FlyCamAppState.class));
            mouseInput.setCursorVisible(true);
        } else {
            stateManager.attach(new FlyCamAppState());
            mouseInput.setCursorVisible(false);
            flyCam.setDragToRotate(true);
        }
    }

    /**
     * В этот момент уже всё готово, инициализировано, отображено
     * и cam имеет актуальные размеры после смены фуллскрин режима:
     * самое время для перестройки GUI.
     */
    @Override
    public void gainFocus() {
        log.info("Focus gained...");
        context.setAutoFlushFrames(true);
        if (inputManager != null) {
            inputManager.reset();
        }

        if (paused && Constants.getUserConfig().isPauseOnHidden()) {
            paused = false;
            log.debug("Game resumed...");
        }

        if (Constants.getUserConfig().isMuteOnLostFocus()
                && getStateManager().hasState(getStateManager().getState(MenuBackgState.class, false))
        ) {
            getStateManager().getState(MenuBackgState.class).resume();
        }

        // сброс расположения debug full info and etc UI elements:
        enqueue(() -> {
            log.info("Reloading context and UI elements...");
            restart(); // Это не перезапускает и не переинициализирует всю игру, перезапускает контекст и применяет обновленный объект настроек

            // пересборка расположений и размеров UI:
            getStateManager().getState(DebugInfoState.class).rebuildFullText();
            OptionsState optState = getStateManager().getState(OptionsState.class, false);
            if (getStateManager().hasState(optState) && optState.isInitialized()) {
                optState.rebuild();
            }
            MainMenuState menuState = getStateManager().getState(MainMenuState.class, false);
            if (getStateManager().hasState(menuState) && menuState.isInitialized()) {
                menuState.setupMenuCamera();
            }

            // коррекция гаммы:
            renderer.setMainFrameBufferSrgb(true);
            renderer.setLinearizeSrgbImages(true);
        });
    }

    @Override
    public void loseFocus() {
        log.info("Focus lost...");
        context.setAutoFlushFrames(false); // снижаем fps

        if (Constants.getUserConfig().isMuteOnLostFocus() && getStateManager().hasState(getStateManager().getState(MenuBackgState.class, false))) {
            getStateManager().getState(MenuBackgState.class).pause();
        }

        if (Controls.isGameActive() && gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
            return;
        }

        if (!paused && Constants.getUserConfig().isPauseOnHidden()) {
            paused = true;
            log.debug("Game paused...");
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean b) {
        paused = b;
    }
}
