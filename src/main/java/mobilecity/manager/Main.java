package mobilecity.manager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public final class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            new MainFrame(stage);
            stage.show();
        } catch (IOException | RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi khởi động ứng dụng");
            alert.setHeaderText("Không thể tải MobileCity Test Manager");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
