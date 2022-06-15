package it.unical.sadstudents.mediaplayeruid.controller;

import it.unical.sadstudents.mediaplayeruid.keyCombo;
import it.unical.sadstudents.mediaplayeruid.thread.ThreadManager;
import it.unical.sadstudents.mediaplayeruid.model.PlayQueue;
import it.unical.sadstudents.mediaplayeruid.model.Player;
import it.unical.sadstudents.mediaplayeruid.view.SceneHandler;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {


    //ELEMENTS ON LAYOUT

    @FXML
    private VBox leftItems;
    @FXML
    private StackPane containerView;
    @FXML
    private MediaView mediaView;

    @FXML
    private FontIcon iconPlayPause;
    @FXML
    private Button btnVideoView;
    @FXML
    private BorderPane myBorderPane;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider mediaSlider;
    @FXML
    private Label currentMediaTimeLabel, mediaNameLabel, endMediaTimeLabel, artistNameLabel;
    @FXML
    private ToolBar toolbarMenu;
    @FXML
    private Button plsEquilizer, plsNext, plsPlayPause, plsPrevious, plsProperties,
            plsScreenMode, plsSkipBack, plsSpeedPlay, plsSkipForward, lightMode, darkMode, about;
    @FXML
    private ToggleButton plsShuffle;
    @FXML
    private ToggleButton plsRepeat;
    @FXML
    private MenuButton volumeButton;
    @FXML
    private FontIcon volumeIcon;
    @FXML
    private StackPane stackPane;
    @FXML
    private FontIcon repeatIcon;
    @FXML
    private FontIcon shuffleIcon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startToolTip();

        //INITIALIZE AN INVISIBLE MEDIAVIEW
        Player.getInstance().setMediaView(mediaView);

        //AFTER THE LOAD CONTROL IN SCENE HANDLER, THE FUNCTION SET THE CORRECT PANE IN MIDDLE OF BORDER PANE
        switchMidPane();

        setKeyEvent();


        //START LISTENER VARI
        mediaSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                //System.out.println(mediaSlider.getValue());
                Player.getInstance().changePosition(mediaSlider.getValue());

            }
        });


        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (volumeSlider.getValue() == 0) volumeIcon.setIconLiteral("fa-volume-off");
                else if (volumeSlider.getValue() > 50) volumeIcon.setIconLiteral("fa-volume-up");
                else volumeIcon.setIconLiteral("fa-volume-down");
                Player.getInstance().setVolume(volumeSlider.getValue() * 0.01);

            }
        });
        // TODO: 07/06/2022 fixare tooltip plsPlayPause 
        SceneHandler.getInstance().currentMidPaneProperty().addListener(observable -> switchMidPane());
        Player.getInstance().mediaLoadedProperty().addListener(observable -> changeButtonEnabledStatus());
        Player.getInstance().isRunningProperty().addListener(observable -> {
            switchPlayPauseIcon();
            plsPlayPause.setTooltip(new Tooltip("Pause"));
        });
        Player.getInstance().currentMediaTimeProperty().addListener(observable -> {
            setMediaSlider();
            currentMediaTimeLabel.setText(formatTime(Player.getInstance().getCurrentMediaTime()));
            if (Player.getInstance().getCurrentMediaTime() == Player.getInstance().getEndMediaTime()) {
                PlayQueue.getInstance().changeMedia(1);
            }
        });

        mediaSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                double curr = Player.getInstance().getCurrentMediaTime();
                if (Math.abs(curr - newValue.doubleValue()) > 1) {
                    Player.getInstance().changePosition(newValue.doubleValue());
                    ThreadManager.getInstance().cancelTimer();
                    currentMediaTimeLabel.setText(formatTime(newValue.doubleValue()));
                    ThreadManager.getInstance().beginTimer();
                }
                double percentage = 100.0 * (newValue.doubleValue() - mediaSlider.getMin()) / (mediaSlider.getMax() - mediaSlider.getMin());
                mediaSlider.setStyle("-track-color: linear-gradient(to right, tertiarySelectionColor " + percentage + "%, white " + percentage + ("%);"));
            }
        });

        Player.getInstance().endMediaTimeProperty().addListener(observable -> {
            setMediaSliderEnd();
            endMediaTimeLabel.setText(formatTime(Player.getInstance().getEndMediaTime()));
        });


        Player.getInstance().mediaNameProperty().addListener(observable -> mediaNameLabel.setText(Player.getInstance().getMediaName()));
        PlayQueue.getInstance().isAVideoProperty().addListener(observable -> activeVideoView());
        // TODO: 15/06/2022 why??
        Player.getInstance().artistNameProperty().addListener(observable -> artistNameLabel.setText(Player.getInstance().getArtistName()));
        Player.getInstance().isRunningProperty().addListener(observable -> formatTime(Player.getInstance().getCurrentMediaTime()));
        //END LISTENER VARI


    }

    public void startToolTip() {
        // TODO: 07/06/2022 
        plsShuffle.setTooltip(new Tooltip("Shuffle mode"));
        plsScreenMode.setTooltip(new Tooltip("Screen mode"));
        plsSpeedPlay.setTooltip(new Tooltip("Speed play"));
        plsSkipForward.setTooltip(new Tooltip("Skip forward 10s"));
        plsSkipBack.setTooltip(new Tooltip("Skip back 10s"));
        volumeButton.setTooltip(new Tooltip("Volume"));
        plsRepeat.setTooltip(new Tooltip("Repeat"));
        plsEquilizer.setTooltip(new Tooltip("Equilizer"));
        plsProperties.setTooltip(new Tooltip("Info"));
        plsNext.setTooltip(new Tooltip("Next"));
        plsPrevious.setTooltip(new Tooltip("Previous"));
    }


    //ACTION EVENT MENU

    @FXML
    void onHome(ActionEvent event) {
        mediaView.setVisible(false);
        myBorderPane.getCenter().setVisible(true);
        SceneHandler.getInstance().setCurrentMidPane("home-view.fxml");
    }

    @FXML
    void onMusicLibrary(ActionEvent event) {
        mediaView.setVisible(false);
        myBorderPane.getCenter().setVisible(true);
        SceneHandler.getInstance().setCurrentMidPane("music-library-view.fxml");
    }

    @FXML
    void onVideoLibrary(ActionEvent event) {
        mediaView.setVisible(false);
        myBorderPane.getCenter().setVisible(true);
        SceneHandler.getInstance().setCurrentMidPane("video-library-view.fxml");
    }

    @FXML
    void onPlayQueue(ActionEvent event) {
        mediaView.setVisible(false);
        myBorderPane.getCenter().setVisible(true);
        SceneHandler.getInstance().setCurrentMidPane("play-queue-view.fxml");
    }

    @FXML
    void onPlayLists(ActionEvent event) {
        mediaView.setVisible(false);
        myBorderPane.getCenter().setVisible(true);
        SceneHandler.getInstance().setCurrentMidPane("playlist-view.fxml");
    }

    @FXML
    void onVideoView(ActionEvent event) {
        btnVideoView.setVisible(true);
        mediaView.setVisible(true);
        myBorderPane.getCenter().setVisible(false);
    }

    @FXML
    void onLightMode(ActionEvent event) {
        SceneHandler.getInstance().changeTheme("unical");
    }

    @FXML
    void onDarkMode(ActionEvent event) {
        SceneHandler.getInstance().changeTheme("dark");
    }

    @FXML
    void onAbout(ActionEvent event) {
        // TODO: 15/06/2022 Fare quello che ci si aspetta in about
        System.out.println("About");
    }

    //ACTION EVENT MEDIA CONTROLS BAR
    @FXML
    void onVolume(ActionEvent event) {


    }

    @FXML
    void onShuffle(ActionEvent event) {
        // TODO: 07/06/2022  se stai facendo girare un video in full screen e attivi onShuffle e skippi si bagga tutto X/
        if (PlayQueue.getInstance().isShuffleActive()) {
            PlayQueue.getInstance().setShuffleActive(false);
        } else {
            PlayQueue.getInstance().setShuffleActive(true);
        }

        if (!PlayQueue.getInstance().isShuffleQueueIndexesGenerated()) {
            PlayQueue.getInstance().generateShuffleList();
            System.out.println("Lista generata!");
        }
    }

    @FXML
    void onPrevious(ActionEvent event) {
        previous();
    }

    private void previous() {
        if (PlayQueue.getInstance().getQueue().size() > 1)
            PlayQueue.getInstance().changeMedia(-1);
    }

    @FXML
    void onSkipBack(ActionEvent event) {
        skipBack();
    }

    private void skipBack() {
        Player.getInstance().tenSecondBack();
    }

    @FXML
    void onSkipForward(ActionEvent event) {
        skipForward();
    }

    private void skipForward() {
        Player.getInstance().tenSecondForward();
    }

    @FXML
    void onNext(ActionEvent event) {
        next();
    }

    private void next() {
        if (PlayQueue.getInstance().getQueue().size() > 1)
            PlayQueue.getInstance().changeMedia(1);
        myBorderPane.requestFocus();
    }

    @FXML
    void onProperties(ActionEvent event) {

    }

    @FXML
    void onRepeat(ActionEvent event) {
        Player.getInstance().repeat();
    }

    @FXML
    void onScreenMode(ActionEvent event) {
        screenModeHandler();
    }

    private void screenModeHandler() {
        if (!SceneHandler.getInstance().getStage().isFullScreen()) {
            leftItems.setVisible(false);
            AnchorPane.setLeftAnchor(containerView, 0.0);
            AnchorPane.setBottomAnchor(containerView, 0.0);
            SceneHandler.getInstance().getStage().setFullScreen(true);
            adjustVideoSize();
            SceneHandler.getInstance().getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    KeyCode key = keyEvent.getCode();
                    if (key == KeyCode.ESCAPE) {
                        leftItems.setVisible(true);
                        AnchorPane.setLeftAnchor(containerView, 270.0);
                        AnchorPane.setBottomAnchor(containerView, 96.00);

                        adjustVideoSize();
                    }
                }
            });

        } else {
            leftItems.setVisible(true);
            AnchorPane.setLeftAnchor(containerView, 270.0);
            AnchorPane.setBottomAnchor(containerView, 96.00);
            SceneHandler.getInstance().getStage().setFullScreen(false);
            adjustVideoSize();
        }
    }

    @FXML
    void onSpeedPlay(ActionEvent event) {

    }

    @FXML
    void onEquilizer(ActionEvent event) {

    }


    @FXML
    void onPlayPause(ActionEvent event) {
        playPauseHandler();
    }

    private void playPauseHandler() {
        if (Player.getInstance().getIsRunning()) {
            iconPlayPause.setIconLiteral("fa-play");
            Player.getInstance().pauseMedia();
        } else {
            iconPlayPause.setIconLiteral("fa-pause");
            Player.getInstance().playMedia();
        }
    }

    // TODO: 15/06/2022 RIMUOVERE TUTTO FINO A PROSSIMO COMMENTO
    @FXML
    void dragDetected(MouseEvent event) {
        System.out.println("dragDetected");
    }

    @FXML
    void dragDropped(DragEvent event) {
        System.out.println("dragDropped");
    }

    @FXML
    void dragOver(DragEvent event) {
        System.out.println("dragOver");
    }

    @FXML
    void mouseDragEntered(MouseDragEvent event) {
        System.out.println("mouseDragEntered");
    }

    @FXML
    void mouseDragExited(MouseDragEvent event) {
        System.out.println("mouseDragExited");
    }

    @FXML
    void mouseDragOver(MouseDragEvent event) {
        System.out.println("mouseDragOver");
    }

    @FXML
    void mouseDragReleased(MouseDragEvent event) {
        System.out.println("mouseDragReleased");
    }
    // TODO: 15/06/2022 fino a qui (ASSICURARSI DI TOGLIERLI ANCHE DA SCENE BUILDER)

    //END ACTION EVENT MEDIA CONTROLS BAR


    // TODO: 06/06/2022 RENDERE INVISIBILE VIDEO VIEW DOPO CANCELLAZIONE DELLA PLAY QUEUE 
    //FUNCTION CALLED AFTER A LISTENER OR OTHER EVENT
    public void activeVideoView() {
        if (PlayQueue.getInstance().getIsAVideo()) {
            mediaView.setVisible(true);
            btnVideoView.setVisible(true);
            myBorderPane.getCenter().setVisible(false);
            plsScreenMode.setDisable(false);
            adjustVideoSize();
            SceneHandler.getInstance().getStage().heightProperty().addListener(observable -> adjustVideoSize());
            SceneHandler.getInstance().getStage().widthProperty().addListener(observable -> adjustVideoSize());
            Player.getInstance().isRunningProperty().addListener(observable -> adjustVideoSize());

        } else {
            mediaView.setVisible(false);
            btnVideoView.setVisible(false);
            myBorderPane.getCenter().setVisible(true);
            plsScreenMode.setDisable(true);
        }
    }

    public void adjustVideoSize() {
        if (Player.getInstance().getIsRunning()) {
            double currentWidth;
            double currentHeight;
            double controllBar = 96.0;
            double menuSize = 270.0;
            if (SceneHandler.getInstance().getStage().isFullScreen()) {
                currentWidth = SceneHandler.getInstance().getStage().getWidth();
                currentHeight = SceneHandler.getInstance().getStage().getHeight();
            } else {
                currentWidth = SceneHandler.getInstance().getStage().getWidth() - menuSize;
                currentHeight = SceneHandler.getInstance().getStage().getHeight() - controllBar;
            }
            mediaView.setFitWidth(currentWidth);
            mediaView.setFitHeight(currentHeight);
            // TODO: 04/06/2022 AGGIUNGERE SPOSTASMENTO SU ASSE Y DEL VIDEO SECONDO SORGENTE
            mediaView.setPreserveRatio(true);
        }
    }

    private String formatTime(double timeDouble) {
        if (timeDouble > 0) {
            int hh = (int) (timeDouble / 3600);
            int mm = (int) ((timeDouble % 3600) / 60);
            int ss = (int) ((timeDouble % 3600) % 60);

            return String.format("%02d:%02d:%02d", hh, mm, ss);
        }

        return "00:00:00";
    }

    private void setMediaSliderEnd() {
        mediaSlider.setMax(Player.getInstance().getEndMediaTime());
        //durationMediaPlayed.setText(CalcolaTempo(Player.getInstance().getEnd()));
        //System.out.println(CalcolaTempo(Player.getInstance().getEnd()));
    }

    private void switchMidPane() {
        if (SceneHandler.getInstance().getCurrentMidPane() == "video-view.fxml") {
            btnVideoView.setDisable(false);
        }
        Pane subScenePane = SceneHandler.getInstance().switchPane();
        myBorderPane.setCenter(subScenePane);
        subScenePane.autosize();
    }

    private void changeButtonEnabledStatus() {
        boolean status;
        if (Player.getInstance().isMediaLoaded()) {
            status = false;
        } else
            status = true;
        plsPrevious.setDisable(status);
        plsEquilizer.setDisable(status);
        plsSkipBack.setDisable(status);
        plsSkipForward.setDisable(status);
        plsNext.setDisable(status);
        plsPlayPause.setDisable(status);
        plsProperties.setDisable(status);
        plsRepeat.setDisable(status);
        plsShuffle.setDisable(status);
        plsSpeedPlay.setDisable(status);
        volumeButton.setDisable(status);
        mediaSlider.setDisable(status);
        volumeSlider.setDisable(status);
    }

    private void setMediaSlider() {
        mediaSlider.setValue(Player.getInstance().getCurrentMediaTime());
    }

    public void switchPlayPauseIcon() {
        if (Player.getInstance().getIsRunning())
            iconPlayPause.setIconLiteral("fa-pause");
        else {
            iconPlayPause.setIconLiteral("fa-play");
        }

    }

    private void muteUnmuteHandler() {
        // TODO: 15/06/2022 Tenersi da parte una variabile con il volume corrente quando si preme il muto, così da ripristinarla in seguito
        if (Player.getInstance().getMediaPlayer().isMute()) {
            Player.getInstance().getMediaPlayer().setMute(false);
            volumeSlider.setValue(100);
        } else {
            Player.getInstance().getMediaPlayer().setMute(true);
            volumeSlider.setValue(0);
        }
    }

    private void volumeChange(int offset) {
        if (volumeSlider.getValue() + offset < 0)
            volumeSlider.setValue(0);
        else if (volumeSlider.getValue() + offset > 100)
            volumeSlider.setValue(100);
        else
            volumeSlider.setValue(volumeSlider.getValue() + offset);
    }

    private void quit() {
        // TODO: 15/06/2022 Controlli prima di uscire dall'applicazione
        Platform.exit();
    }

    public void setKeyEvent() {

        myBorderPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                KeyCode key = keyEvent.getCode();


                if (key == KeyCode.SPACE && Player.getInstance().isMediaLoaded()) { //Space	Play/pause
                    plsPlayPause.requestFocus();
                    playPauseHandler();
                } else if (key == KeyCode.S) {//S	Stop
                    Player.getInstance().stop();
                } else if (key == KeyCode.N) {//N	Next track
                    next();
                } else if (key == KeyCode.P) {//P	Previous track
                    previous();
                } else if (key == KeyCode.L) { //L	Normal/loop/repeat
                    plsRepeat.fire();
                } else if (key == KeyCode.T) {//T	Shuffle
                    plsShuffle.fire();
                } else if (key == KeyCode.M) {//M	Mute
                    muteUnmuteHandler();
                } else if (keyCombo.skipBack.match(keyEvent)) {//ALT + LEFT  Skip back 10s
                    skipBack();
                } else if (keyCombo.skipForward.match(keyEvent)) {//ALT + RIGHT  Skip forward 10s
                    skipForward();
                } else if (keyCombo.volumeUp.match(keyEvent)) {//CTRL + UP  Volume up 10%
                    volumeChange(10);
                } else if (keyCombo.volumeDown.match(keyEvent)) {//CTRL + DOWN  Volume down 10%
                    volumeChange(-10);
                } else if (keyCombo.quit.match(keyEvent)) {//CTRL + Q  Quit application
                    quit();
                }

                myBorderPane.requestFocus();


            }
        });
        //END FUNCTION CALLED AFTER A LISTENER OR OTHER EVENT
    }
}
