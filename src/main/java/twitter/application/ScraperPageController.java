package twitter.application;

import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import twitter.controller.JsonFileManager;
import twitter.entity.ProgressPrinter;
import io.github.palexdev.materialfx.beans.NumberRange;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.effects.Interpolators;
import io.github.palexdev.materialfx.utils.AnimationUtils.KeyFrames;
import io.github.palexdev.materialfx.utils.AnimationUtils.PauseBuilder;
import io.github.palexdev.materialfx.utils.AnimationUtils.TimelineBuilder;
import javafx.animation.Animation;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import twitter.entity.ProgressPrinter;
import twitter.entity.TaskVoid;
import twitter.entity.User;
import twitter.scraper.XScraper;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.geometry.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Getter
@Setter
public class ScraperPageController implements Initializable {
    private final String DATA_ROOT_DIR = "data/";
    private final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";

    @FXML
    private MFXProgressBar progressBar;
    @FXML
    private Label helloLabel;
    @FXML
    private Label progressMessageLabel;

    @FXML
    private MFXTableView<User> userTable;

    @FXML
    private MFXTextField searchQueryTextField;
    @FXML
    private MFXTextField minFollowersTextField;
    @FXML
    private MFXTextField numberOfUsersTextField;

    @FXML
    private MFXButton scrapeUserListButton;
    @FXML
    private MFXCheckbox userProfileCheckbox;
    @FXML
    private MFXCheckbox followingCheckbox;
    @FXML
    private MFXCheckbox followersCheckbox;
    @FXML
    private MFXCheckbox tweetsCheckbox;
    @FXML
    private MFXCheckbox commentsCheckbox;
    @FXML
    private MFXCheckbox reTweetsCheckbox;
    @FXML
    private MFXButton scrapeAllButton;

    @FXML
    private MFXButton userListUploadButton;
    @FXML
    private MFXButton userListDownloadButton;
    @FXML
    private MFXButton userProfileUploadButton;
    @FXML
    private MFXButton userProfileDownloadButton;
    @FXML
    private MFXButton followingUploadButton;
    @FXML
    private MFXButton followingDownloadButton;
    @FXML
    private MFXButton followersUploadButton;
    @FXML
    private MFXButton followersDownloadButton;
    @FXML
    private MFXButton tweetsUploadButton;
    @FXML
    private MFXButton tweetsDownloadButton;
    @FXML
    private MFXButton commentUploadButton;
    @FXML
    private MFXButton commentDownloadButton;
    @FXML
    private MFXButton reTweetUploadButton;
    @FXML
    private MFXButton reTweetDownloadButton;
    @FXML
    private MFXButton allDataDownloadButton;

    private XScraper xScraper;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.getRanges1().add(NumberRange.of(0.0, 0.30));
        progressBar.getRanges2().add(NumberRange.of(0.31, 0.60));
        progressBar.getRanges3().add(NumberRange.of(0.61, 1.0));

        MFXTableColumn<User> usernameColumn = new MFXTableColumn<>("Username", true, Comparator.comparing(User::getUsername));
        MFXTableColumn<User> userLinkColumn = new MFXTableColumn<>("Link", true, Comparator.comparing(User::getUserLink));
        MFXTableColumn<User> followersCountColumn = new MFXTableColumn<>("Followers", true, Comparator.comparing(User::getFollowersCount));
        MFXTableColumn<User> followingCountColumn = new MFXTableColumn<>("Following", true, Comparator.comparing(User::getFollowingCount));

        usernameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUsername) {});
        userLinkColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUserLink) {});
        followersCountColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFollowersCount) {{
            setAlignment(Pos.CENTER_RIGHT);
        }});
        followingCountColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFollowingCount) {{
            setAlignment(Pos.CENTER_RIGHT);
        }});

        Map<String, User> users = JsonFileManager.fromJson(USERS_SCRAPE_FILE, false, new TypeToken<Map<String, User>>(){}.getType());
        ObservableList<User> userList = FXCollections.observableList(users.values().stream().toList());

//        userTable.getTableColumns().addAll(usernameColumn, userLinkColumn, followersCountColumn, followingCountColumn);
        userTable.getTableColumns().addAll(usernameColumn, followersCountColumn, followingCountColumn);
        userTable.getFilters().addAll(
                new StringFilter<>("Username", User::getUsername),
//                new StringFilter<>("Link", User::getUserLink),
                new IntegerFilter<>("Followers", User::getFollowersCount),
                new IntegerFilter<>("Following", User::getFollowingCount)
        );
        userTable.setItems(userList);
    }

    private final String DEFAULT_SEARCH_QUERY = "blockchain, crypto, ethereum, #blockchain, #crypto";
    private final int DEFAULT_MIN_FOLLOWERS = 200;
    private final int DEFAULT_NUMBER_OF_USERS = 3200;
    private DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
    private StringProperty messageProperty = new SimpleStringProperty("Processing...");

    public void setProgressMessageProperty() {
//        System.err.println("+ " + progressProperty + " " + messageProperty);
        progressProperty = new SimpleDoubleProperty(0.0);
        messageProperty = new SimpleStringProperty("Processing...");
        xScraper.getTwitterScraper().getTwitterUserScraper().setProgress(progressProperty);
        xScraper.getTwitterScraper().getTwitterUserScraper().setMessage(messageProperty);
        xScraper.getNitterScraper().getNitterUserScraper().setProgress(progressProperty);
        xScraper.getNitterScraper().getNitterUserScraper().setMessage(messageProperty);
        xScraper.getNitterScraper().getNitterTweetScraper().setProgress(progressProperty);
        xScraper.getNitterScraper().getNitterTweetScraper().setMessage(messageProperty);
    }

    private void runUserListScraper(TaskVoid task) throws InterruptedException {
        String queries = searchQueryTextField.getText();
        if (queries == null || queries.isEmpty()) {
            queries = DEFAULT_SEARCH_QUERY;
            searchQueryTextField.setText(queries);
        }
        String[] splits = queries.split(",");

        int minFollowers;
        try {
            minFollowers = Integer.parseInt(minFollowersTextField.getText());
        }
        catch (Exception e) {
            minFollowers = DEFAULT_MIN_FOLLOWERS;
            minFollowersTextField.setText(String.valueOf(minFollowers));
            System.err.println("Invalid minimum followers! Using default value: " + minFollowers);
        }
        int numberOfUsers;
        try {
            numberOfUsers = Integer.parseInt(numberOfUsersTextField.getText());
        }
        catch (Exception e) {
            numberOfUsers = DEFAULT_NUMBER_OF_USERS;
            numberOfUsersTextField.setText(String.valueOf(numberOfUsers));
            System.err.println("Invalid number of users! Using default value: " + numberOfUsers);
        }
//        System.err.println(xScraper.getProgress() + " " + xScraper.getMessage());
        xScraper.getTwitterScraper().getTwitterUserScraper().getUserSearches(numberOfUsers, splits);
        xScraper.getNitterScraper().getNitterUserScraper().setMINIMUM_FOLLOWERS_COUNT(minFollowers);
    }
    private void runTweetScraper(TaskVoid task) throws Exception {
//        setProgressMessageProperty();
        xScraper.getNitterScraper().getNitterTweetScraper().getTweetsOfUsers(0, 0);
    }

    private void runFollowersScraper(TaskVoid task) throws Exception {
//        setProgressMessageProperty();
        xScraper.getTwitterScraper().getTwitterUserScraper().getUsersFollowers(0);
    }
    private void runFollowingScraper(TaskVoid task) throws Exception {
//        setProgressMessageProperty();
        xScraper.getTwitterScraper().getTwitterUserScraper().getUsersFollowing(0);
    }
    private void runUserProfileScraper(TaskVoid task) throws Exception {
//        setProgressMessageProperty();
        xScraper.getNitterScraper().getNitterUserScraper().getInfoOfUsers(0);
    }

    class userListScraperTask extends TaskVoid {
        @Override
        protected Void call() throws Exception {
//            System.err.println(progressProperty + " " + messageProperty);
//            System.err.println(xScraper.getProgress() + " " + xScraper.getMessage());
            disableAllButtons();
            setProgressMessageProperty();
            new Thread(()-> {
                try {
                    runUserListScraper(this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            do {
                updateProgress(progressProperty.get(), 1.0);
                updateMessage(messageProperty.get());
                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } while (progressProperty.get() < 1.0);
            updateProgress(1.0, 1.0);
            updateMessage("Scraping finished!");
//            System.err.println(getProgress() + " " + getMessage());
            return null;
        }
        protected void succeeded() {
            super.succeeded();
//            System.err.println("Done!");
//            System.out.println(getProgress() + " " + getMessage());
            System.out.println(progressProperty().getValue() + " " + messageProperty().getValue());
            enableAllButtons();
        }
        protected void failed() {
            super.failed();
//            System.err.println(exceptionProperty().get().getMessage());
//            System.err.println("Failed!");
            enableAllButtons();
        }
    }
    class tweetScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runTweetScraper(this);
            return null;
        }
    }

    class followersScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runFollowersScraper(this);
            return null;
        }
    }

    class followingScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runFollowingScraper(this);
            return null;
        }
    }

    class userProfileScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runUserProfileScraper(this);
            return null;
        }
    }

    private void enableAllButtons() {
        scrapeUserListButton.setDisable(false);
        scrapeAllButton.setDisable(false);
    }
    private void disableAllButtons() {
        scrapeUserListButton.setDisable(true);
        scrapeAllButton.setDisable(true);
    }

    private void setProgress(TaskVoid task) {
        progressBar.progressProperty().bind(task.progressProperty());
        progressMessageLabel.textProperty().bind(task.messageProperty());
    }

    int allDataScraperCounter = 0;
    class allDataScraperTask extends TaskVoid {
        @Override
        protected Void call() throws Exception {
//            setProgressMessageProperty();
            disableAllButtons();
            setProgressMessageProperty();
            new Thread(()-> {
                try {
                    if (userProfileCheckbox.isSelected()) {
                        allDataScraperCounter++;
                        runUserProfileScraper(this);
                    }
                    if (followersCheckbox.isSelected()) {
                        allDataScraperCounter++;
                        runFollowersScraper(this);
                    }
                    if (followingCheckbox.isSelected()) {
                        allDataScraperCounter++;
                        runFollowingScraper(this);
                    }
                    if (tweetsCheckbox.isSelected()) {
                        allDataScraperCounter++;
                        runTweetScraper(this);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (allDataScraperCounter-- > 0) {
                do {
                    updateProgress(progressProperty.get(), 1.0);
                    updateMessage(messageProperty.get());
                    try {
                        //noinspection BusyWait
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } while (progressProperty.get() < 1.0);
            }
            updateProgress(1.0, 1.0);
            updateMessage("Scraping finished!");
            return null;
        }

        protected void succeeded() {
            super.succeeded();

            enableAllButtons();
        }
        protected void failed() {
            super.failed();
            disableAllButtons();
        }
    }

    @FXML
    public void userListScraper() throws InterruptedException {
        TaskVoid task = new userListScraperTask();
        setProgress(task);
        new Thread(task).start();
    }

    private void tweetScraper() throws InterruptedException {
        TaskVoid task = new tweetScraper();
        setProgress(task);
        new Thread(task).start();
    }

    private void followersScraper() throws InterruptedException {
        TaskVoid task = new followersScraper();
        setProgress(task);
        new Thread(task).start();
    }

    private void followingScraper() throws InterruptedException {
        TaskVoid task = new followingScraper();
        setProgress(task);
        new Thread(task).start();
    }

    private void userProfileScraper() throws InterruptedException {
        TaskVoid task = new userProfileScraper();
        setProgress(task);
        new Thread(task).start();
    }

    @FXML
    public void allDataScraper() throws InterruptedException {
        TaskVoid task = new allDataScraperTask();
        setProgress(task);
        new Thread(task).start();
    }

    private void uploadJsonFile(Stage stage, Type type) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JSON File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
//                jsonDisplayArea.setText(content.toString());
            } catch (IOException ex) {
//                jsonDisplayArea.setText("Error loading file: " + ex.getMessage());
            }
        }
    }

    private void downloadJsonFile(Stage stage, Type type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//                writer.write(jsonDisplayArea.getText());
            } catch (IOException ex) {
//                jsonDisplayArea.setText("Error saving file: " + ex.getMessage());
            }
        }
    }


}
