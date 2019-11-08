package info.revenberg.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.FontMetrics;

import javax.imageio.ImageIO;

public class ImportImages {
    public String[] headers;

    public BufferedImage resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight, Boolean border)
            throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();

        if (border) {
            g2d.setColor(Color.orange);
            g2d.fillRect(0, 0, Card.width, Card.height);    
            g2d.drawImage(inputImage, 10, 10, scaledWidth-20, scaledHeight-20, null);
        } else {
            g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        }
        g2d.dispose();

        return outputImage;
    }

    public void copyImage(String source, String dest, boolean resizeTheImage, boolean border) throws IOException {
        BufferedImage image = null;
        File file = null;
        if (resizeTheImage) {
            image = resize(source, dest, Card.width, Card.height, border);
        } else {
            file = new File(source);
            image = ImageIO.read(file);
        }
        file = new File(dest);
        
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CopyFile(String filename, String dest, boolean resizeTheImage, boolean border) throws IOException, URISyntaxException {
        File tempFile = new File(filename);

        if (tempFile.exists()) {
            copyImage(tempFile.getAbsolutePath(), dest, resizeTheImage, border);
        } else {
            System.out.println("Not found: " + filename);
        }
    }

    private void makeCardDir(String destDir) {
        File directory = new File(String.valueOf(destDir));
        if (!directory.exists()) {
            File file = new File(destDir);
            file.mkdirs();
        }
    }

    public void readFile(String dest, String filename) throws IOException, URISyntaxException {
        String destDir = "";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int r = 0;
        try {
            String line = br.readLine();
            String[] s = line.split(";");
            destDir = dest + "/" + s[0];
            makeCardDir(destDir + "/cards");

            CopyFile(s[1], destDir + "/background.png", false, false);
            CopyFile(s[2], destDir + "/back.png", true, false);
            CopyFile(s[3], destDir + "/front.png", true, false);

            destDir = destDir + "/cards";

            line = br.readLine();
            headers = line.split(";");

            line = br.readLine();
            while (line != null) {
                r++;
                s = line.split(";");
                for (int i = 0; i < s.length; i++) {
                    String k = "";
                    switch (i) {
                    case 0:
                        k = "A";
                        break;
                    case 1:
                        k = "B";
                        break;
                    case 2:
                        k = "C";
                        break;
                    case 3:
                        k = "D";
                        break;
                    }

                    File tempFile = new File(s[i]);
                    if (tempFile.exists()) {
                        // String ext = s[i].substring(s[i].lastIndexOf('.') + 1);
                        filename = destDir + "/" + Integer.toString(r) + " of " + k + ".png";
                        copyImage(tempFile.getAbsolutePath(), filename, true, true);
                    } else {
                        filename = destDir + "/" + Integer.toString(r) + " of " + k + ".png";
                        createImage(s[i], filename);
                    }
                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }

    public void createImage(String text, String filename) throws IOException {
        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(Card.width, Card.height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.orange);
        g2d.fillRect(0, 0, Card.width, Card.height);

        g2d.setColor(Color.white);
        g2d.fillRect(10, 10, Card.width-20, Card.height-20);

        g2d.setColor(Color.blue);

        FontMetrics metrics = g2d.getFontMetrics();
        int x = (Card.width - metrics.stringWidth(text)) / 2;
        int y = (Card.height / 2);
        
        g2d.drawString(text, x, y);

        // Disposes of this graphics context and releases any system resources that it
        // is using.
        g2d.dispose();

        // Save as PNG
        File file = new File(filename);
        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        ImportImages test = new ImportImages();
        File file = new File("src/main/resources/images");
        System.out.println( file.getAbsolutePath());
        test.readFile(file.getAbsolutePath(), "C:/temp/Dreus.csv");
    }
}
