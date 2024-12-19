package twitter.application;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

public class PreloadPageController {
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label label;

    public MFXProgressSpinner getProgressSpinner() {
        return progressSpinner;
    }

    public Label getLabel() {
        return label;
    }
}
