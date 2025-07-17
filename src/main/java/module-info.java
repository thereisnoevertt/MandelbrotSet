module com.example.mandelbrotset {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.mandelbrotset to javafx.fxml;
    exports com.example.mandelbrotset;
    exports com.example.mandelbrotset.withoutThreads;
    opens com.example.mandelbrotset.withoutThreads to javafx.fxml;
}