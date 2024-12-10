package twitter.application;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginPageController {
    @FXML
    private JFXTextField username;

    @FXML
    private JFXTextField email;

    @FXML
    private JFXPasswordField password;

    private void handleLogin() {
        String username = this.username.getText();
        String email = this.email.getText();
        String password = this.password.getText();
        System.out.println(username + " " + email + " " + password);
    }
}
