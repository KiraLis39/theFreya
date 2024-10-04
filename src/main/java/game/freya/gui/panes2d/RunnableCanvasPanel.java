package game.freya.gui.panes2d;

import game.freya.config.Constants;
import game.freya.dto.PlayCharacterDto;
import game.freya.dto.roots.WorldDto;
import game.freya.net.data.NetConnectTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class RunnableCanvasPanel {

    public void serverUp(WorldDto aNetworkWorld) {
//        // Если игра по сети, но Сервер - мы, и ещё не запускался:
//        UUID curWorldUid = gameControllerService.getWorldService().saveOrUpdate(aNetworkWorld).getUid();
//        gameControllerService.getWorldService().setCurrentWorld(curWorldUid);
//
//        // Открываем локальный Сервер:
//        if (gameControllerService.getWorldService().getCurrentWorld().isLocal()
//                && gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()
//                && (Constants.getServer() == null || Constants.getServer().isClosed())
//        ) {
//            if (openServer()) {
//                log.info("Сервер сетевой игры успешно активирован на {}", Constants.getServer().getAddress());
//            } else {
//                log.warn("Что-то пошло не так при активации Сервера.");
//                new FOptionPane().buildFOptionPane("Server error:", "Что-то пошло не так при активации Сервера.", 60, true);
//                return;
//            }
//        }
//
//        if (Constants.getLocalSocketConnection() != null && Constants.getLocalSocketConnection().isOpen()) {
//            log.error("Socket should was closed here! Closing...");
//            Constants.getLocalSocketConnection().close();
//        }
//
//        // Подключаемся к локальному Серверу как новый Клиент:
//        connectToServer(NetConnectTemplate.builder()
//                .address(aNetworkWorld.getAddress())
//                .password(aNetworkWorld.getPassword())
//                .worldUid(aNetworkWorld.getUid())
//                .build());
    }

    /**
     * Создание и открытие Сервера.
     * Создаётся экземпляр Сервера, ждём его запуска и возвращаем успешность процесса.
     *
     * @return успешность открытия Сервера.
     */
    private boolean openServer() {
//        Constants.setServer(Server.getInstance(gameControllerService));
//        Constants.getServer().start();
//        Constants.getServer().untilOpen(Constants.getGameConfig().getServerOpenTimeAwait());
        return Constants.getServer().isOpen();
    }

    public void connectToServer(NetConnectTemplate connectionTemplate) {
//        getHeroesListPane().setVisible(false);
//
//        Constants.setConnectionAwait(true);
//        getNetworkListPane().repaint(); // костыль для отображения анимации
//
//        if (connectionTemplate.address().isBlank()) {
//            new FOptionPane().buildFOptionPane("Ошибка адреса:", "Адрес сервера не может быть пустым.", 10, true);
//        }
//
//        // 1) приходим сюда с host:port для подключения
//        String address = connectionTemplate.address().trim();
//        String h = address.contains(":") ? address.split(":")[0].trim() : address;
//        Integer p = address.contains(":") ? Integer.parseInt(address.split(":")[1].trim()) : null;
//        getNetworkListPane().repaint(); // костыль для отображения анимации
//        try {
//            // 2) подключаемся к серверу, авторизуемся там и получаем мир для сохранения локально
//            if (connectToServer(h.trim(), p, connectionTemplate.password())) {
//                // 3) проверка героя в этом мире:
//                chooseOrCreateHeroForWorld(gameControllerService.getWorldService().getCurrentWorld().getUid());
//            } else {
//                new FOptionPane().buildFOptionPane("Отказ:", "Сервер отклонил подключение!", 5, true);
//                throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED, Constants.getLocalSocketConnection().getLastExplanation());
//            }
//        } catch (GlobalServiceException gse) {
//            log.warn("GSE here: {}", gse.getMessage());
//            if (gse.getCode().equals("ER07")) {
//                new FOptionPane().buildFOptionPane("Не доступно:", gse.getMessage(), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
//            }
//        } catch (IllegalThreadStateException tse) {
//            log.error("Connection Thread state exception: {}", ExceptionUtils.getFullExceptionMessage(tse));
//        } catch (Exception e) {
//            new FOptionPane().buildFOptionPane("Ошибка данных:", ("Ошибка подключения '%s'.\n"
//                    + "Верно: <host_ip> или <host_ip>:<port> (192.168.0.10/13:13958)")
//                    .formatted(ExceptionUtils.getFullExceptionMessage(e)), FOptionPane.TYPE.INFO, Constants.getDefaultCursor());
//            log.error("Server aim address to connect error: {}", ExceptionUtils.getFullExceptionMessage(e));
//        } finally {
////            gameControllerService.getLocalSocketConnection().close();
//            Constants.setConnectionAwait(false);
//        }
    }

    private boolean connectToServer(String host, Integer port, String password) {
//        // создание нового подключения к Серверу (сокета):
//        Constants.setLocalSocketConnection(new SocketConnection());
//
//        // подключаемся к серверу:
//        if (Constants.getLocalSocketConnection().isOpen() && Constants.getLocalSocketConnection().getHost().equals(host)) {
//            // верно ли подобное поведение?
//            log.warn("Сокетное подключение уже открыто, пробуем использовать {}", Constants.getLocalSocketConnection().getHost());
//        } else {
//            Constants.getLocalSocketConnection().openSocket(host, port, gameControllerService, false);
//            Constants.getLocalSocketConnection().untilOpen(Constants.getGameConfig().getSocketConnectionTimeout());
//        }
//
//        if (!Constants.getLocalSocketConnection().isOpen()) {
//            throw new GlobalServiceException(ErrorMessages.NO_CONNECTION_REACHED,
//                    "No reached socket connection to " + host + (port == null ? "" : ":" + port));
//        } else if (!host.equals(Constants.getLocalSocketConnection().getHost())) {
//            throw new GlobalServiceException(ErrorMessages.WRONG_DATA, "current socket host address");
//        }
//
//        // передаём свои данные для авторизации:
//        Constants.getLocalSocketConnection().authRequest(password);
//
//        Thread authThread = Thread.startVirtualThread(() -> {
//            while (!Constants.getLocalSocketConnection().isAuthorized() && !Thread.currentThread().isInterrupted()) {
//                Thread.yield();
//            }
//        });
//
//        try {
//            // ждём окончания авторизации Сервером:
//            authThread.join(Constants.getGameConfig().getSocketAuthTimeout());
//
//            // когда таймаут уже кончился:
//            if (authThread.isAlive()) {
//                log.error("Так и не получили успешной авторизации от Сервера за отведённое время.");
//                authThread.interrupt();
//                Constants.getLocalSocketConnection().close();
//                return false;
//            } else {
//                log.info("Успешная авторизация Сервером.");
//                return true;
//            }
//        } catch (InterruptedException e) {
//            log.error("Ошибка авторизации Сервером: {}", e.getMessage(), e);
//            authThread.interrupt();
//            Constants.getLocalSocketConnection().close();
        return false;
//        }
    }

    /**
     * После выбора мира - приходим сюда для создания нового героя или
     * выбора существующего, для игры в данном мире.
     *
     * @param worldUid uid выбранного для игры мира.
     */
    public void chooseOrCreateHeroForWorld(UUID worldUid) {
//        getWorldsListPane().setVisible(false);
//        getWorldCreatingPane().setVisible(false);
//        getNetworkListPane().setVisible(false);
//        getNetworkCreatingPane().setVisible(false);
//
//        gameControllerService.getWorldService().setCurrentWorld(worldUid);
//        List<PlayCharacterDto> heroes = gameControllerService.getCharacterService().findAllByWorldUidAndOwnerUid(
//                gameControllerService.getWorldService().getCurrentWorld().getUid(),
//                gameControllerService.getPlayerService().getCurrentPlayer().getUid());
//        if (heroes.isEmpty()) {
//            getHeroCreatingPane().setVisible(true);
//        } else {
//            getHeroesListPane().setVisible(true);
//        }
    }

    public void openCreatingNewHeroPane(PlayCharacterDto template) {
//        getHeroesListPane().setVisible(false);
//        getHeroCreatingPane().setVisible(true);
//        if (template != null) {
//            ((HeroCreatingPane) getHeroCreatingPane()).load(template);
//        }
    }

    /**
     * После выбора или создания мира (и указания его как текущего в контроллере) и выбора или создания героя, которым
     * будем играть в выбранном мире - попадаем сюда для последних приготовлений и
     * загрузки холста мира (собственно, начала игры).
     *
     * @param hero выбранный герой для игры в выбранном ранее мире.
     */
    public void playWithThisHero(PlayCharacterDto hero) {
//        gameControllerService.getPlayerService().setCurrentPlayerLastPlayedWorldUid(hero.getWorldUid());
//        gameControllerService.getCharacterService().setCurrentHero(hero);
//
//        // если этот мир по сети:
//        if (gameControllerService.getWorldService().getCurrentWorld().isNetAvailable()) {
//            // шлем на Сервер своего выбранного Героя:
//            if (Constants.getLocalSocketConnection().registerOnServer()) {
////                gameControllerService.getPlayedHeroes().addHero(characterService.getCurrentHero());
//                startGame();
//            } else {
//                log.error("Сервер не принял нашего Героя: {}", Constants.getLocalSocketConnection().getLastExplanation());
//                characterService.getCurrentHero().setOnline(false);
//                characterService.saveCurrent();
//                getHeroCreatingPane().repaint();
//                getHeroesListPane().repaint();
//            }
//        } else {
//            // иначе просто запускаем мир и играем локально:
//            startGame();
//        }
    }
}
