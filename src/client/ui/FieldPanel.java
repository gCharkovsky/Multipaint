package client.ui;

import client.net.ConnectionManager;
import graphics.Line;
import graphics.SoftLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class FieldPanel extends JPanel {

    private final int BACKGROUND_CELL_SIZE = 5;
    private boolean leftPressed, rightPressed;
    private boolean soft;
    private BufferedImage mainImage, tempImage, backgroundImage;
    private Graphics mainGraphics, tempGraphics, backgroundGraphics;
    private ConnectionManager connectionManager;
    private Color pencilColor;
    private int[] lastPosition;
    private int[] nowPosition;
    private int pencilSize;
    private int dx = 0, dy = 0;

    FieldPanel(int fieldWidth, int fieldHeight) throws IOException {
        nowPosition = new int[]{-1, -1};
        lastPosition = new int[]{-1, -1};
        pencilSize = 20;
        soft = false;
        setSize(fieldWidth, fieldHeight);
        setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        setMaximumSize(new Dimension(fieldWidth, fieldHeight));
        leftPressed = false;
        rightPressed = false;
        setOpaque(true);
        setVisible(true);
        pencilColor = Color.black;
        setBackground(Color.white);


        mainImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        tempImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        backgroundImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);


        mainGraphics = mainImage.createGraphics();
        backgroundGraphics = backgroundImage.createGraphics();
        backgroundGraphics.setColor(Color.white);
        backgroundGraphics.fillRect(0, 0, getWidth(), getHeight());
        backgroundGraphics.setColor(Color.lightGray);
        for (int i = 0; BACKGROUND_CELL_SIZE * i < getWidth(); i++) {
            for (int j = 0; BACKGROUND_CELL_SIZE * j < getHeight(); j++) {
                if (i % 2 != j % 2) {
                    backgroundGraphics.fillRect(BACKGROUND_CELL_SIZE * i, BACKGROUND_CELL_SIZE * j, BACKGROUND_CELL_SIZE, BACKGROUND_CELL_SIZE);
                }
            }
        }


        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                pencilSize -= e.getWheelRotation() * e.getWheelRotation() * e.getWheelRotation() / Math.abs(e.getWheelRotation());
                if (pencilSize > 1000)
                    pencilSize = 1000;
                if (pencilSize < 1)
                    pencilSize = 1;
                repaint();
                getParent().repaint();
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {


            }

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case 1:
                        leftPressed = true;
                        lastPosition = new int[]{e.getX(), e.getY()};
                        break;
                    case 3:
                        rightPressed = true;
                        lastPosition = new int[]{e.getX(), e.getY()};
                        break;
                }
                if (leftPressed) {

                    tempImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    tempGraphics = tempImage.getGraphics();
                    ((Graphics2D) tempGraphics).setComposite(soft ? AlphaComposite.SrcOver : AlphaComposite.Src);
                    drawLine(e.getX(), e.getY());
                    if (soft) {
                        (new SoftLine(lastPosition, new int[]{e.getX(), e.getY()}, pencilSize, pencilColor)).draw(tempGraphics);
                    }
                    lastPosition = new int[]{e.getX(), e.getY()};
                }
                if (rightPressed) {
                    clearLine(e.getX(), e.getY());
                    lastPosition = new int[]{e.getX(), e.getY()};
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                    case 1:
                        leftPressed = false;
                        mainGraphics.drawImage(tempImage, 0, 0, null);
                        tempImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                        break;
                    case 3:
                        rightPressed = false;
                        break;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nowPosition = new int[]{e.getX(), e.getY()};
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nowPosition = new int[]{-1, -1};
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (leftPressed) {
                    drawLine(e.getX(), e.getY());
                    lastPosition = new int[]{e.getX(), e.getY()};
                }
                if (rightPressed) {
                    clearLine(e.getX(), e.getY());
                    lastPosition = new int[]{e.getX(), e.getY()};
                }
                nowPosition = new int[]{e.getX(), e.getY()};
                repaint();
            }


            @Override
            public void mouseMoved(MouseEvent e) {

                nowPosition = new int[]{e.getX(), e.getY()};
                repaint();
            }
        });


    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    BufferedImage getMainImage() {
        return mainImage;
    }

    void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private void drawLine(int x, int y) {
        if (soft) {
            ((Graphics2D) tempGraphics).setComposite(AlphaComposite.SrcOver);
            dx += (Math.abs(lastPosition[0] - x));
            dy += (Math.abs(lastPosition[1] - y));
            if (dx + dy > Math.min(pencilSize / 2, 15)) {
                (new SoftLine(lastPosition, new int[]{x, y}, pencilSize, pencilColor)).draw(tempGraphics);
                dx = 0;
                dy = 0;
            }

        } else {
            (new Line(lastPosition, new int[]{x, y}, pencilSize, pencilColor)).draw(tempGraphics);
        }
        if (connectionManager.isConnected())
            connectionManager.sendCommand("line" + " " + lastPosition[0] + " " + lastPosition[1] + " " + x + " " + y +
                    " " + pencilSize + " "
                    + pencilColor.getRed() + " " + pencilColor.getGreen() + " " + pencilColor.getBlue());
    }

    private void clearLine(int x, int y) {
        (new Line(lastPosition, new int[]{x, y}, pencilSize, Color.white)).clear(mainGraphics);
        if (connectionManager.isConnected())
            connectionManager.sendCommand("line" + " " + lastPosition[0] + " " + lastPosition[1] + " " + x + " " + y +
                    " " + pencilSize +
                    " 255 255 255");
    }

    public void paintComponent(Graphics g) { //отрисовка поля
        super.paintComponent(g); //отрисовка как JPanel
        g.drawImage(backgroundImage, 0, 0, null);
        g.drawImage(mainImage, 0, 0, null);
        g.drawImage(tempImage, 0, 0, null);
        g.setColor(Color.black);
        if (nowPosition[0] + nowPosition[1] > 0) {
            g.drawOval(nowPosition[0] - pencilSize, nowPosition[1] - pencilSize, 2 * pencilSize, 2 * pencilSize);
            g.setColor(Color.white);
            g.drawOval(nowPosition[0] - pencilSize - 1, nowPosition[1] - pencilSize - 1, 2 * pencilSize + 2, 2 * pencilSize + 2);
        }

    }

    public String toString() {

        return "";
    }

    Color getPencilColor() {
        return pencilColor;
    }

    void setPencilColor(Color pencilColor) {
        this.pencilColor = pencilColor;
    }

    int getPencilSize() {
        return pencilSize;
    }

    void setPencilSize(int pencilSize) {
        this.pencilSize = pencilSize;
    }

}

