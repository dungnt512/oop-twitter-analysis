package twitter.application;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import twitter.*;
import javafx.scene.control.*;
import twitter.controller.JsonFileManager;
import twitter.entity.LoginAccount;

public class LoginPageController {
    @FXML
    private MFXTextField usernameField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXPasswordField passwordField;

    private final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    private final String X_LOGIN_ACCOUNT_FILE = X_LOGIN_DATA_ROOT_DIR + "userAccount.json";

    @FXML
    private void handleLogin() {
        String email = this.emailField.getText();
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
//        LoginAccount temp = JsonFileManager.fromJson(X_LOGIN_ACCOUNT_FILE, true, LoginAccount.class);
//        System.out.println(temp.getMail() + " " + temp.getUsername() + " " + temp.getPassword());
//        System.out.println(username + " " + email + " " + password);
        LoginAccount loginAccount = new LoginAccount(email, username, password);
        JsonFileManager.toJson(X_LOGIN_ACCOUNT_FILE, loginAccount, true);
    }

}
