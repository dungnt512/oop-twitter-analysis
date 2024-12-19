package twitter.application;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import javafx.scene.control.*;
import twitter.controller.JsonFileManager;
import twitter.entity.LoginAccount;
import twitter.entity.TaskVoid;
import twitter.scraper.XScraper;

import java.io.IOException;

@Setter
public class LoginPageController {
    @FXML
    private MFXTextField usernameField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXPasswordField passwordField;

    private final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    private final String X_LOGIN_ACCOUNT_FILE = X_LOGIN_DATA_ROOT_DIR + "userAccount.json";

    private XScraper scraper;
    private Stage stage;
    private LoginAccount loginAccount;

    public void initialize() throws IOException {
        loginAccount = JsonFileManager.fromJson(X_LOGIN_ACCOUNT_FILE, true, LoginAccount.class);
        usernameField.setText(loginAccount.getUsername());
        emailField.setText(loginAccount.getMail());
        passwordField.setText(loginAccount.getPassword());
//        scene.setFill(Color.TRANSPARENT);
//        stage.initStyle(StageStyle.TRANSPARENT);


    }

    private void login(boolean loginWithCookies) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("preload-page.fxml"));
        Scene scene = new Scene(loader.load());
        PreloadPageController controller = loader.getController();
        Label label = controller.getLabel();
        label.setText("Prepare to login to X...");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        Task<Void> task = loginWithCookiesTask(loginWithCookies);
        controller.getProgressSpinner().progressProperty().bind(task.progressProperty());
        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    private void handleLoginWithCookies() throws IOException {
        login(true);
    }

    @NotNull
    private Task<Void> loginWithCookiesTask(boolean loginWithCookies) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, 4);
                scraper = new XScraper("", false, loginWithCookies);
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
            TaskVoid task = TaskVoid.testTask("");
            controller.getProgressBar().progressProperty().bind(task.progressProperty());
            controller.setXScraper(scraper);
            controller.setStage(stage);
//            controller.setProgressMessageProperty();
            Label label = controller.getHelloLabel();
            label.setText("Hello " + loginAccount.getName() + " (" + loginAccount.getUsername() + ")! Welcome to X Scraper!");

            stage.setScene(scene);
            stage.centerOnScreen();
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = this.emailField.getText();
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
//        LoginAccount temp = JsonFileManager.fromJson(X_LOGIN_ACCOUNT_FILE, true, LoginAccount.class);
//        System.out.println(temp.getMail() + " " + temp.getUsername() + " " + temp.getPassword());
//        System.out.println(username + " " + email + " " + password);
        LoginAccount loginAccount = new LoginAccount(email, username, password);
        JsonFileManager.toJson(X_LOGIN_ACCOUNT_FILE, loginAccount, true);
        login(false);
    }

}
