package com.example.mandelbrotset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class Render {
    public final int MAX_ITERATION = 20000;
    public final int DISPLAY_WIDTH = 1920;
    public final int DISPLAY_HEIGHT = 1080;

    private Path path;
    private int width, height;
    private BufferedImage image;
    private int tCount;
    private AtomicInteger nextRenderSection = new AtomicInteger(tCount);
    private double centerX;
    private double centerY;
    private double zoomFactor;
    private JFrame jFrame;
    private JLabel jLabel;

    public Render() {
        Path cwdTest = Paths.get(System.getProperty("user.dir") + "/images");
        if (Files.notExists(cwdTest, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectories(cwdTest);
            } catch (IOException e) {
                System.out.println("Ошибка при создании директории");
                System.exit(1);
            }
        }
        jFrame = new JFrame("Мандельброт");
        jLabel = new JLabel();
        path = cwdTest;
    }

    private void displayFractal() {
        jLabel.setIcon(new ImageIcon(image.getScaledInstance((int) (DISPLAY_WIDTH * 0.79), (int) (DISPLAY_HEIGHT * 0.79), BufferedImage.SCALE_FAST)));
        jFrame.setVisible(true);
    }

    public boolean renderSetup(int width, int height, double cX, double cY, double zoom, int threadCount) {
        this.width = width;
        this.height = height;
        centerX = cX;
        centerY = cY;
        zoomFactor = zoom;
        tCount = threadCount;
        long startTime = System.currentTimeMillis();
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        RenderThread[] threads = new RenderThread[threadCount];
        int numSections = tCount * 2;

        ImageIcon imageIcon = new ImageIcon(image);

        jFrame.setLayout(new FlowLayout());
        jFrame.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);

        jLabel.setIcon(imageIcon);
        jFrame.getContentPane().add(jLabel);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (int thCount = 0; thCount < tCount; thCount++) {
            threads[thCount] = new RenderThread(path, "r-thread" + thCount, image, width, height, this);
            threads[thCount].setStartX(0);
            threads[thCount].setStartY((height / numSections) * thCount);
            threads[thCount].setEndX(width);
            threads[thCount].setEndY((height / numSections) * (thCount + 1));
            threads[thCount].start();
        }

        try {
            for (RenderThread t : threads) {
                t.t.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Поток интерраптед");
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy-HH_mm_ss");
        File f = new File(path.toString() + "/" + formatter.format(Calendar.getInstance().getTime()) + ".png");
        try {
            ImageIO.write(image, "PNG", f);
        } catch (IOException e) {
            System.out.println("Сохранить изображение не удалось");
            System.exit(1);
        }
        return true;
    }

    public void setPixel(int px, int py) {
        double xToYRatio = 1.875;
        double xScaleLower = centerX - xToYRatio / zoomFactor;
        double xScaleUpper = centerX + xToYRatio / zoomFactor;
        double yScaleLower = centerY - (Math.abs(xScaleUpper - xScaleLower) / 2 / xToYRatio);
        double yScaleUpper = centerY + (Math.abs(xScaleUpper - xScaleLower) / xToYRatio / 2);
        double x0 = scale(px, width, xScaleLower, xScaleUpper);
        double y0 = scale(py, height, yScaleLower, yScaleUpper);
        double x = 0.0, y = 0.0;
        int iteration = 0;
        int bailout = 4;
        while (x * x + y * y <= bailout && iteration < MAX_ITERATION) {
            double xtemp = x * x - y * y + x0;
            y = 2 * x * y + y0;
            x = xtemp;
            iteration++;
        }
        int r = (int) scale(Math.pow(iteration, 4), MAX_ITERATION, 0, 255);
        int g = (int) scale(Math.pow(iteration, 3), MAX_ITERATION, 0, 255);
        int b = (int) scale(Math.pow(iteration, 3), MAX_ITERATION, 0, 255);
        int rgb = (r << 16) | (g << 8) | b;

        image.setRGB(px, py, rgb);
    }

    public double scale(double n, int max, double minPrime, double maxPrime) {
        return (n / max) * (maxPrime - minPrime) + minPrime;
    }

    public boolean startNextSection(RenderThread renderThread) {
        displayFractal();
        if (nextRenderSection.get() < (tCount * 2)) {
            System.out.println("Переходим к следующей секции");
            renderThread.setStartY((height / (tCount * 2)) * nextRenderSection.get());
            renderThread.setEndY((height / (tCount * 2)) * nextRenderSection.getAndIncrement());
            return true;
        }
        return false;
    }
}
