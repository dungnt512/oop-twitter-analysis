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
import twitter.entity.TaskVoid;
import twitter.scraper.XScraper;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private XScraper scraper;
    @Override
    public void start(Stage stage) throws IOException {
        CSSFX.start();

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-page.fxml"));
        Scene scene = new Scene(loader.load());
        LoginPageController controller = loader.getController();
        stage.setTitle("X Scraper");
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("logo.png"))));
        }
        catch (Exception _) {}
        controller.setScraper(scraper);
        controller.setStage(stage);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
