package twitter.application;

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
import twitter.scraper.XScraper;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
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
import java.util.ResourceBundle;

@Getter
@Setter
public class ScraperPageController implements Initializable {

    @FXML
    private MFXProgressBar progressBar;
    @FXML
    private Label helloLabel;
    @FXML
    private Label progressMessageLabel;

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
    private int minFollowers = 200;

    private void runUserListScraper(TaskVoid task) throws InterruptedException {
        xScraper.setTask(task);
        String queries = searchQueryTextField.getText();
        String[] splits = queries.split(",");

        try {
            minFollowers = Integer.parseInt(minFollowersTextField.getText());
        }
        catch (Exception _) {}
        int numberOfUsers = 3200;
        try {
            numberOfUsers = Integer.parseInt(numberOfUsersTextField.getText());
        }
        catch (Exception _) {}
        xScraper.getTwitterScraper().getTwitterUserScraper().getUserSearches(numberOfUsers, splits);
        xScraper.getNitterScraper().getNitterUserScraper().setMINIMUM_FOLLOWERS_COUNT(minFollowers);
    }
    private void runTweetScraper(TaskVoid task) throws Exception {
        xScraper.setTask(task);
        xScraper.getNitterScraper().getNitterTweetScraper().getTweetsOfUsers(0, 0);
    }

    private void runFollowersScraper(TaskVoid task) throws Exception {
        xScraper.setTask(task);
        xScraper.getTwitterScraper().getTwitterUserScraper().getUsersFollowers(0);
    }
    private void runFollowingScraper(TaskVoid task) throws Exception {
        xScraper.setTask(task);
        xScraper.getTwitterScraper().getTwitterUserScraper().getUsersFollowing(0);
    }
    private void runUserProfileScraper(TaskVoid task) throws Exception {
        xScraper.setTask(task);
        xScraper.getNitterScraper().getNitterUserScraper().getInfoOfUsers(0);
    }

    class userListScraperTask extends TaskVoid {
        @Override
        protected Void call() throws Exception {
            runUserListScraper(this);
            return null;
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
            disableAllButtons();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.getRanges1().add(NumberRange.of(0.0, 0.30));
        progressBar.getRanges2().add(NumberRange.of(0.31, 0.60));
        progressBar.getRanges3().add(NumberRange.of(0.61, 1.0));
    }
}
