package twitter.application;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import twitter.entity.LoginAccount;
import twitter.scraper.XScraper;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private Stage primaryStage;
    private XScraper scraper;
    private LoginAccount loginAccount;
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        CSSFX.start();

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        int counter = 0;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("preload-page.fxml"));
        Scene scene = new Scene(loader.load());
        PreloadPageController controller = loader.getController();
        Label label = controller.getLabel();
        label.setText("Prepare to login to X...");
//        scene.setFill(Color.TRANSPARENT);
//        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("X Scraper");
        stage.setScene(scene);
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("logo.png"))));
        }
        catch (Exception _) {}
        stage.show();


        Task<Void> task = getVoidTask();
        Thread thread = new Thread(task);
        thread.start();
        controller.getProgressSpinner().progressProperty().bind(task.progressProperty());
    }

    @NotNull
    private Task<Void> getVoidTask() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, 4);
                scraper = new XScraper("", false, true);
                updateProgress(2, 4);
                scraper.getTwitterScraper().getSiteQuery().goToHome();
                Thread.sleep(2000);
                updateProgress(3, 4);
                loginAccount = scraper.getTwitterScraper().getSiteQuery().getUserProfile();
                System.err.println(loginAccount.getUsername() + " " + loginAccount.getName());
                updateProgress(4, 4);

                return null;
            }
        };
        task.setOnSucceeded(e -> switchToScraperPage());
        return task;
    }

    private void switchToScraperPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("scraper-page.fxml"));
            Scene scene = new Scene(loader.load());

            ScraperPageController controller = loader.getController();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    int step = 100;
                    for (int i = 0; i <= step; i++) {
                        updateProgress(i, step);
                        Thread.sleep(25);
                    }
                    return null;
                }
            };
            controller.getProgressBar().progressProperty().bind(task.progressProperty());
            controller.setXScraper(scraper);
//            controller.setProgressMessageProperty();
            Label label = controller.getHelloLabel();
            label.setText("Hello " + loginAccount.getName() + " (" + loginAccount.getUsername() + ")! Welcome to X Scraper!");

            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
