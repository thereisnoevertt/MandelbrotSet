package com.example.mandelbrotset;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class RenderThread implements Runnable {
    public Thread t;
    public final Path cwd;
    public final String tName;
    public final int width, height;
    private int startX, startY, endX, endY;
    private Render controller;


    public RenderThread(Path current, String threadName, BufferedImage i, int width, int height, Render render) {
        System.out.println("Поток создан");
        cwd = current;
        tName = threadName;
        this.width = width;
        this.height = height;
        controller = render;
    }

    @Override
    public void run() {
        System.out.println(tName + " запущен!");
        threadRender();
    }

    private void threadRender() {
        do {
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    controller.setPixel(x, y);
                }
            }
            System.out.println("Поток " + t.getName() + " закончил рендер");
        } while (controller.startNextSection(this));
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, tName);
            t.start();
        } else {
            run();
        }
    }

    public void setStartX(int x) {
        startX = x;
    }

    public void setEndX(int x) {
        endX = x;
    }

    public void setStartY(int y) {
        startY = y;
    }

    public void setEndY(int y) {
        endY = y;
    }

}
