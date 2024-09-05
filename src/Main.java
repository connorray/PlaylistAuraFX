import java.io.File;
import java.util.Arrays;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private ListView<String> songListView;
    private ObservableList<String> songs;
    private Label playlistTitle;
    private MediaPlayer mediaPlayer;
    private Button playButton;
    private Button stopButton;
    private ProgressBar timeProgressBar;
    private Label currentTimeLabel;
    private File currentDirectory;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Playlist Aura Check");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #191414;");

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        VBox content = createContent();
        mainLayout.setCenter(content);

        Scene scene = new Scene(mainLayout, 900, 600);
        scene
            .getStylesheets()
            .add(
                "https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap"
            );
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #000000;");
        sidebar.setPrefWidth(200);

        Label title = new Label("Playlist Aura");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button chooseFolderButton = new Button("Choose MP3 Folder");
        chooseFolderButton.setStyle(
            "-fx-background-color: #1DB954; -fx-text-fill: white;"
        );
        chooseFolderButton.setOnAction(e -> chooseFolder());

        Button clearPlaylistButton = new Button("Clear Playlist");
        clearPlaylistButton.setStyle(
            "-fx-background-color: #E91429; -fx-text-fill: white;"
        );
        clearPlaylistButton.setOnAction(e -> clearPlaylist());

        // add playing capabilities
        playButton = new Button("Play");
        playButton.setStyle("-fx-background-color: #1DB954; -fx-text-fill: white;");
        playButton.setOnAction(e -> playSong());
        playButton.setDisable(true);

        stopButton = new Button("Stop");
        stopButton.setStyle("-fx-background-color: #E91429; -fx-text-fill: white;");
        stopButton.setOnAction(e -> stopSong());
        stopButton.setDisable(true);

        sidebar.getChildren().addAll(title, chooseFolderButton, clearPlaylistButton, playButton, stopButton);
        return sidebar;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        playlistTitle = new Label("Your Playlist");
        playlistTitle.setFont(Font.font("Montserrat", FontWeight.BOLD, 24));
        playlistTitle.setTextFill(Color.WHITE);

        songs = FXCollections.observableArrayList();
        songListView = new ListView<>(songs);
        songListView.setPrefHeight(400);
        songListView.setStyle(
            "-fx-background-color: #191414; -fx-control-inner-background: #191414;"
        );
        songListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #191414;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #1DB954;");
                    } else {
                        setStyle("-fx-background-color: #191414;");
                    }
                }
            }
        });

        // Add a listener to update cell styles when selection changes
        songListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            songListView.refresh();
        });

        timeProgressBar = new ProgressBar(0);
        timeProgressBar.setPrefWidth(300);
        timeProgressBar.setStyle("-fx-accent: #1DB954;");

        currentTimeLabel = new Label("0:00 / 0:00");
        currentTimeLabel.setTextFill(Color.WHITE);

        content.getChildren().addAll(playlistTitle, songListView, timeProgressBar, currentTimeLabel);
        return content;
    }

    private void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select MP3 Folder");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            currentDirectory = selectedDirectory;
            loadSongsFromDirectory(selectedDirectory);
            playlistTitle.setText("Your Playlist: " + selectedDirectory.getName());
            playButton.setDisable(false);
        }
    }
    
    private void playSong() {
        String selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        File file = new File(currentDirectory, selectedSong);
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            timeProgressBar.setProgress(newValue.toSeconds() / media.getDuration().toSeconds());
            updateTimeLabel();
        });

        mediaPlayer.setOnReady(() -> {
            updateTimeLabel();
        });

        mediaPlayer.play();
        stopButton.setDisable(false);
        songListView.refresh(); // Refresh to update the visual selection
    }

    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            timeProgressBar.setProgress(0);
            updateTimeLabel();
        }
        stopButton.setDisable(true);
        songListView.refresh(); // Refresh to update the visual selection
    }

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();
            currentTimeLabel.setText(formatTime(current) + " / " + formatTime(total));
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) Math.floor(duration.toMinutes());
        int seconds = (int) Math.floor(duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private void loadSongsFromDirectory(File directory) {
        songs.clear();
        File[] files = directory.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".mp3")
        );
        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                songs.add(file.getName());
            }
        }
    }

   private void clearPlaylist() {
        songs.clear();
        playlistTitle.setText("Your Playlist");
        stopSong();
        playButton.setDisable(true);
        songListView.refresh(); // Refresh to clear any visual selection
    }

    public static void main(String[] args) {
        launch(args);
    }
}
