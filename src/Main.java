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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private ListView<String> songListView;
    private ObservableList<String> songs;
    private Label playlistTitle;

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

        sidebar
            .getChildren()
            .addAll(title, chooseFolderButton, clearPlaylistButton);
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

        songListView.setCellFactory(param ->
            new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: #191414;");
                    }
                }
            }
        );

        content.getChildren().addAll(playlistTitle, songListView);
        return content;
    }

    private void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select MP3 Folder");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            loadSongsFromDirectory(selectedDirectory);
            playlistTitle.setText(
                "Your Playlist: " + selectedDirectory.getName()
            );
        }
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
