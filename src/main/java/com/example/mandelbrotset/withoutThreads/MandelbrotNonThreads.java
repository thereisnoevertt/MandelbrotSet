package com.example.mandelbrotset.withoutThreads;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MandelbrotNonThreads extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 2000;

    private double zoom = 200;
    private double offsetX = -2.0;
    private double offsetY = -1.5;

    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        drawMandelbrot();

        canvas.setOnMouseClicked(this::handleZoom);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Множество Мандельброта");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawMandelbrot() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double cx = x / zoom + offsetX;
                double cy = y / zoom + offsetY;

                double zx = 0;
                double zy = 0;
                int iter = 0;

                while (zx * zx + zy * zy < 4 && iter < MAX_ITER) {
                    double temp = zx * zx - zy * zy + cx;
                    zy = 2 * zx * zy + cy;
                    zx = temp;
                    iter++;
                }

                int color = iter == MAX_ITER ? 0 : 255 - (iter * 255 / MAX_ITER);
                gc.getPixelWriter().setColor(x, y, Color.rgb(color, color, color));
            }
        }
    }

    private void handleZoom(MouseEvent event) {
        // Координаты клика в окне
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Преобразуем их в координаты на комплексной плоскости
        double clickedCX = mouseX / zoom + offsetX;
        double clickedCY = mouseY / zoom + offsetY;

        // Увеличиваем масштаб и сдвигаем центр
        zoom *= 2;

        // Новый сдвиг так, чтобы клик стал центром
        offsetX = clickedCX - (WIDTH / 2.0) / zoom;
        offsetY = clickedCY - (HEIGHT / 2.0) / zoom;

        drawMandelbrot();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
