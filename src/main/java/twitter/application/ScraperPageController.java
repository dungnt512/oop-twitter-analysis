package twitter.application;

import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.enums.SortState;
import io.github.palexdev.materialfx.filter.DoubleFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.stage.DirectoryChooser;
import twitter.algorithms.PageRank;
import twitter.controller.JsonFileManager;
import io.github.palexdev.materialfx.beans.NumberRange;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import twitter.entity.GraphData;
import twitter.entity.GraphNode;
import twitter.entity.TaskVoid;
import twitter.entity.User;
import twitter.navigators.TwitterQuery;
import twitter.scraper.XScraper;
import javafx.scene.control.Label;
import javafx.geometry.*;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.reflect.TypeToken;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Comparator;
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
    private final String USERS_DATA_FILE = DATA_ROOT_DIR + "user-data.json";
    private final String PAGE_RANK_DATA_FILE = DATA_ROOT_DIR + "page-rank.json";

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
    private MFXTableView<GraphNode> pageRankResultTable;

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
    private MFXButton refreshUserListButton;
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
    private MFXButton pageRankResultDownload;
    @FXML
    private MFXButton runPageRankButton;

    @FXML
    private MFXButton exitButton;

    private boolean isLogin = true;
    private XScraper xScraper;
    private PageRank pageRank;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.getRanges1().add(NumberRange.of(0.0, 0.30));
        progressBar.getRanges2().add(NumberRange.of(0.31, 0.60));
        progressBar.getRanges3().add(NumberRange.of(0.61, 1.0));

        createUserTableList(userTable, false);
        createPageRankResultTable(true);
//        userTable.autosizeColumnsOnInitialization();
        pageRank = new PageRank();
    }

    private long lastExitTime = 0;
    private final int exitMaxDuration = 500;
    @FXML
    private void exit() {
        if (System.currentTimeMillis() - lastExitTime < exitMaxDuration) {
            if (xScraper != null) {
                xScraper.quitDriver();
            }
            stage.close();
            Platform.exit();
        }
        else {
            lastExitTime = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("unchecked")
    private void createUserTableList(MFXTableView<User> userTable, boolean refresh) {
        if (!refresh) {
            MFXTableColumn<User> usernameColumn = new MFXTableColumn<>("Username", true, Comparator.comparing(User::getUsername));
            MFXTableColumn<User> userLinkColumn = new MFXTableColumn<>("Link", true, Comparator.comparing(User::getUserLink));
            MFXTableColumn<User> followersCountColumn = new MFXTableColumn<>("Followers", true, Comparator.comparing(User::getFollowersCount));
            MFXTableColumn<User> followingCountColumn = new MFXTableColumn<>("Following", true, Comparator.comparing(User::getFollowingCount));

            followersCountColumn.setSortState(SortState.DESCENDING);

            usernameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUsername) {
            });
            userLinkColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUserLink) {{
                Hyperlink link = new Hyperlink(user.getUserLink());
                link.setOnAction(event -> {
                    try {
                        Desktop.getDesktop().browse(new URI(user.getUserLink()));
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
                setGraphic(link);
            }});
            followersCountColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFollowersCount) {{
                setAlignment(Pos.CENTER);
            }});
            followingCountColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFollowingCount) {{
                setAlignment(Pos.CENTER);
            }});
//        userTable.getTableColumns().addAll(usernameColumn, userLinkColumn, followersCountColumn, followingCountColumn);

            userTable.getTableColumns().addAll(usernameColumn, userLinkColumn, followersCountColumn, followingCountColumn);
            userTable.getFilters().addAll(
                    new StringFilter<>("Username", User::getUsername),
                    new StringFilter<>("Link", User::getUserLink),
                    new IntegerFilter<>("Followers", User::getFollowersCount),
                    new IntegerFilter<>("Following", User::getFollowingCount)
            );
        }

        Map<String, User> users = JsonFileManager.fromJson(USERS_SCRAPE_FILE, false, new TypeToken<Map<String, User>>(){}.getType());
        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (user.getUserLink() == null || user.getUserLink().isEmpty()) {
                user.setUserLink(TwitterQuery.TWITTER_HOME_PAGE + user.getUsername());
            }
        }
        JsonFileManager.toJson(USERS_SCRAPE_FILE, users, true);
        ObservableList<User> userList = FXCollections.observableList(users.values().stream().toList());
        userTable.setItems(userList);
    }

    @FXML
    private void handleRefreshUserList() {
        TaskVoid task = new TaskVoid() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Loading KOLs List...");
                createUserTableList(userTable, true);
                updateProgress(1, 2);
//                Map<String, User> users = JsonFileManager.fromJson(USERS_SCRAPE_FILE, false, new TypeToken<Map<String, User>>(){}.getType());

//                for (Map.Entry<String, User> entry : users.entrySet()) {
//                    User user = entry.getValue();
//                    if (user.getUserLink() == null || user.getUserLink().isEmpty()) {
//                        user.setUserLink(X_HOME_PAGE + user.getUsername());
//                    }
//                }
//                JsonFileManager.toJson(USERS_SCRAPE_FILE, users, true);
//                updateProgress(2, 3);
//                ObservableList<User> userList = FXCollections.observableList(users.values().stream().toList());
//                userTable.setItems(userList);
//                updateProgress(3, 3);
                return null;
            }

            @Override
            protected void succeeded() {
                updateProgress(1, 1);
                updateMessage("Refresh KOLs List completed!");
            }
        };
        setProgress(task);
        new Thread(task).start();
//        createUserTableList(userTable, true);
    }

    private final String DEFAULT_SEARCH_QUERY = "blockchain, crypto, ethereum, #blockchain, #crypto";
    private final int DEFAULT_MIN_FOLLOWERS = 200;
    private final int DEFAULT_NUMBER_OF_USERS = 3200;
    private DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
    private StringProperty messageProperty = new SimpleStringProperty("Processing...");

    private void setProgressMessageProperty() {
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

    private void setProgressMessagePageRank() {
        progressProperty = new SimpleDoubleProperty(0.0);
        messageProperty = new SimpleStringProperty("Processing...");
        pageRank.setProgress(progressProperty);
        pageRank.setMessage(messageProperty);
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
    private void runRetweetsScraper(TaskVoid task) throws Exception {
//        setProgressMessageProperty();
        xScraper.getTwitterScraper().getTwitterUserScraper().getUsersRetweets(0);
    }
    private void runCommentsScraper(TaskVoid task) throws Exception {
        xScraper.getNitterScraper().getNitterUserScraper().getUserComments(0);
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
    class retweetsScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runRetweetsScraper(this);
            return null;
        }
    }
    class commentsScraper extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runCommentsScraper(this);
            return null;
        }
    }

    public void enableAllScrapeButton() {
        scrapeUserListButton.setDisable(false);
        scrapeAllButton.setDisable(false);
    }
    public void enableAllButtons() {
        if (isLogin) {
            enableAllScrapeButton();
        }
        runPageRankButton.setDisable(false);
    }
    public void disableAllScrapeButton() {
        scrapeUserListButton.setDisable(true);
        scrapeAllButton.setDisable(true);
    }
    public void disableAllButtons() {
        disableAllScrapeButton();
        runPageRankButton.setDisable(true);
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
            if (reTweetsCheckbox.isSelected()) {
                allDataScraperCounter++;
            }
            if (commentsCheckbox.isSelected()) {
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
                    if (reTweetsCheckbox.isSelected()) {
                        runRetweetsScraper(this);
                    }
                    if (commentsCheckbox.isSelected()) {
                        runCommentsScraper(this);
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

    @SuppressWarnings("unchecked")
    private void createPageRankResultTable(boolean isFirst) {
        if (isFirst) {
            MFXTableColumn<GraphNode> usernameColumn = new MFXTableColumn<>("Username", true, Comparator.comparing(GraphNode::getId));
            MFXTableColumn<GraphNode> userLinkColumn = new MFXTableColumn<>("Link", true, Comparator.comparing(GraphNode::getId));
            MFXTableColumn<GraphNode> followersCountColumn = new MFXTableColumn<>("Followers", true, Comparator.comparing(GraphNode::getFollowersCount));
            MFXTableColumn<GraphNode> weightColumn = new MFXTableColumn<>("Weight", true, Comparator.comparing(GraphNode::getWeight));


            usernameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(GraphNode::getId) {
            });
            userLinkColumn.setRowCellFactory(user -> new MFXTableRowCell<>(GraphNode::getType) {{
                Hyperlink link = new Hyperlink(user.getType());
                link.setOnAction(event -> {
                    try {
                        Desktop.getDesktop().browse(new URI(user.getType()));
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
                setGraphic(link);
            }});

            followersCountColumn.setRowCellFactory(user -> new MFXTableRowCell<>(GraphNode::getFollowersCount) {{
                setAlignment(Pos.CENTER);
            }});
            weightColumn.setSortState(SortState.DESCENDING);
            weightColumn.setRowCellFactory(user -> new MFXTableRowCell<>(GraphNode::getWeight) {{
                setAlignment(Pos.CENTER);
            }});
//        userTable.getTableColumns().addAll(usernameColumn, userLinkColumn, followersCountColumn, followingCountColumn);

            pageRankResultTable.getTableColumns().addAll(usernameColumn, userLinkColumn, followersCountColumn, weightColumn);
            pageRankResultTable.getFilters().addAll(
                    new StringFilter<>("Username", GraphNode::getId),
                    new StringFilter<>("Link", GraphNode::getId),
                    new IntegerFilter<>("Followers", GraphNode::getFollowersCount),
                    new DoubleFilter<>("Weight", GraphNode::getWeight)
            );
        }

        GraphData pageRankData = JsonFileManager.fromJson(PAGE_RANK_DATA_FILE, true, GraphData.class);
        if (pageRankData != null) {
            ObservableList<GraphNode> pageRankUserList = FXCollections.observableList(pageRankData.getNodes());
            pageRankResultTable.setItems(pageRankUserList);
        }
    }


    class runPageRankTask extends TaskVoid {
        @Override
        protected Void call() throws Exception {
//            System.err.println(progressProperty + " " + messageProperty);
//            System.err.println(xScraper.getProgress() + " " + xScraper.getMessage());
            disableAllButtons();
            setProgressMessagePageRank();
            new Thread(()-> {
                pageRank.runPageRank();
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
//            updateMessage("PageRank completed!");
//            System.err.println(getProgress() + " " + getMessage());
            return null;
        }
        protected void succeeded() {
            super.succeeded();
            enableAllButtons();
            createPageRankResultTable(false);
            System.out.println(progressProperty().getValue() + " " + messageProperty().getValue());
        }
        protected void failed() {
            super.failed();
//            System.err.println(exceptionProperty().get().getMessage());
            System.err.println("Failed!");
            enableAllButtons();
        }
    }
    @FXML
    private void handleDownloadPageRankResult() throws IOException {
        downloadJsonFile(PAGE_RANK_DATA_FILE);
    }

    @FXML
    private void handleRunPageRank() {
        TaskVoid task = new runPageRankTask();
        setProgress(task);
        new Thread(task).start();
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
            TaskVoid task = TaskVoid.testTask("Upload complete!");
            setProgress(task);
            new Thread(task).start();
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
            TaskVoid task = TaskVoid.testTask("Download complete!");
            setProgress(task);
            new Thread(task).start();
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
