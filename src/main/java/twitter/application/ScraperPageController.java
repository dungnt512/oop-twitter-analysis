package twitter.application;

import io.github.palexdev.materialfx.beans.NumberRange;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.effects.Interpolators;
import io.github.palexdev.materialfx.utils.AnimationUtils.KeyFrames;
import io.github.palexdev.materialfx.utils.AnimationUtils.PauseBuilder;
import io.github.palexdev.materialfx.utils.AnimationUtils.TimelineBuilder;
import javafx.animation.Animation;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import twitter.scraper.XScraper;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

@Getter
@Setter
public class ScraperPageController implements Initializable {

    @FXML
    private MFXProgressBar progressBar;
    @FXML
    private Label helloLabel;

    private XScraper xScraper;

    class userListScraperTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            int numUser = 2000;
            String queries = "blockchain";
            String[] splits = queries.split(",");
            xScraper.getTwitterScraper().getTwitterUserScraper().getUserSearches(200, splits);
            return null;
        }
    }

    class tweetScraper extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            xScraper.getNitterScraper().getNitterTweetScraper().getTweetsOfUsers(0, 0);
            return null;
        }
    }

    class followerScraper extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            xScraper.getTwitterScraper().getTwitterUserScraper().getUsersFollowers(0);
            return null;
        }
    }

    class userProfileScraper extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            xScraper.getNitterScraper().getNitterUserScraper().getInfoOfUsers(0);
            return null;
        }
    }
    @FXML
    public void userListScraper() throws InterruptedException {
        new Thread(new userListScraperTask()).start();
    }

    @FXML
    public void tweetScraper() throws InterruptedException {
        new Thread(new tweetScraper()).start();
    }

    @FXML
    public void followerScraper() throws InterruptedException {
        new Thread(new followerScraper()).start();
    }

    @FXML
    void userProfileScraper() throws InterruptedException {
        new Thread(new userProfileScraper()).start();
    }

    @FXML
    public void followingScraper() throws InterruptedException {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.getRanges1().add(NumberRange.of(0.0, 0.30));
        progressBar.getRanges2().add(NumberRange.of(0.31, 0.60));
        progressBar.getRanges3().add(NumberRange.of(0.61, 1.0));
    }
}
