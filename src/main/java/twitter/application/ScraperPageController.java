package twitter.application;

import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import twitter.controller.JsonFileManager;
import io.github.palexdev.materialfx.beans.NumberRange;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import twitter.entity.TaskVoid;
import twitter.entity.User;
import twitter.scraper.XScraper;
import javafx.scene.control.Label;
import javafx.geometry.*;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.util.ResourceBundle;

@Getter
@Setter
public class ScraperPageController implements Initializable {
    private final String DATA_ROOT_DIR = "data/";
    private final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_IDS_SCRAPE_FILE = DATA_ROOT_DIR + "userIds.json";
    private final String USER_FOLLOWERS_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowers.json";
    private final String USER_FOLLOWING_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowing.json";
    private final String USER_TWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userTweets.json";
    private final String USER_RETWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userRetweets.json";
    private final String USER_COMMENTS_SCRAPE_FILE = DATA_ROOT_DIR + "userComments.json";

    private Stage stage;

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

    @FXML
    private MFXButton exitButton;

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

    private long lastExitTime = 0;
    private final int exitMaxDuration = 500;
    @FXML
    private void exit() {
        if (System.currentTimeMillis() - lastExitTime < exitMaxDuration) {
            xScraper.quitDriver();
            stage.close();
            Platform.exit();
        }
        else {
            lastExitTime = System.currentTimeMillis();
        }
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

    class allDataScraperTask extends TaskVoid {
        @Override
        protected Void call() throws Exception {
//            setProgressMessageProperty();
            disableAllButtons();
            setProgressMessageProperty();
            int allDataScraperCounter = 0;
            if (userProfileCheckbox.isSelected()) {
                allDataScraperCounter++;
            }
            if (followersCheckbox.isSelected()) {
                allDataScraperCounter++;
            }
            if (followingCheckbox.isSelected()) {
                allDataScraperCounter++;
            }
            if (tweetsCheckbox.isSelected()) {
                allDataScraperCounter++;
            }
            new Thread(()-> {
                try {
                    if (userProfileCheckbox.isSelected()) {
                        runUserProfileScraper(this);
                    }
                    if (followersCheckbox.isSelected()) {
                        runFollowersScraper(this);
                    }
                    if (followingCheckbox.isSelected()) {
                        runFollowingScraper(this);
                    }
                    if (tweetsCheckbox.isSelected()) {
                        runTweetScraper(this);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (allDataScraperCounter-- > 0) {
                System.err.println(progressProperty.get() + " " + messageProperty.get());
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

    @FXML
    private void handleUploadUserList() throws IOException {
        uploadJsonFile(USER_IDS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserList() throws IOException {
        downloadJsonFile(USER_IDS_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserProfile() throws IOException {
        uploadJsonFile(USERS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserProfile() throws IOException {
        downloadJsonFile(USERS_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserFollowers() throws IOException {
        uploadJsonFile(USER_FOLLOWERS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserFollowers() throws IOException {
        downloadJsonFile(USER_FOLLOWERS_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserFollowing() throws IOException {
        uploadJsonFile(USER_FOLLOWING_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserFollowing() throws IOException {
        downloadJsonFile(USER_FOLLOWING_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserTweets() throws IOException {
        uploadJsonFile(USER_TWEETS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserTweets() throws IOException {
        downloadJsonFile(USER_TWEETS_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserRetweets() throws IOException {
        uploadJsonFile(USER_RETWEETS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserRetweets() throws IOException {
        downloadJsonFile(USER_RETWEETS_SCRAPE_FILE);
    }

    @FXML
    private void handleUploadUserComments() throws IOException {
        uploadJsonFile(USER_COMMENTS_SCRAPE_FILE);
    }
    @FXML
    private void handleDownloadUserComments() throws IOException {
        downloadJsonFile(USER_COMMENTS_SCRAPE_FILE);
    }


    private void uploadJsonFile(String fileName) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload JSON " + fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File uploadFile = new File(fileName);

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Path uploadFilePath = uploadFile.getParentFile().toPath();
            Path filePath = file.toPath();
            TaskVoid task = TaskVoid.testTask();
            setProgress(task);
            try {
                Files.copy(filePath, uploadFilePath.resolve(uploadFile.getName()));
            }
            catch (IOException e) {
                progressMessageLabel.setText("Upload failed!");
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    private void downloadJsonFile(String filePath) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Download JSON " + filePath);

        File downloadFile = new File(filePath);

        File folder = directoryChooser.showDialog(stage);
        if (folder != null) {
//            System.err.println(downloadFile.toPath() + " " + folder.toPath());
            Path downloadFilePath = downloadFile.toPath();
            Path folderPath = folder.toPath();
            TaskVoid task = TaskVoid.testTask();
            setProgress(task);
            try {
                Files.copy(downloadFilePath, folderPath.resolve(downloadFilePath.getFileName()));
            }
            catch (IOException ex) {
                progressMessageLabel.setText("Download failed!");
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }
        }
    }


}
