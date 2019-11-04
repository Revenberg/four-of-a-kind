package info.revenberg.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class Test {

    public void copyFile(String source, String dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);

            // the size of the buffer doesn't have to be exactly 1024 bytes, try playing
            // around with this number and see what effect it will have on the performance
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public void readFile(String destDir, String filename) throws IOException {
        destDir = destDir + "/cards";
        File directory = new File(String.valueOf(destDir));
        if (!directory.exists()) {
            File file = new File(destDir);
            file.mkdirs();
        }
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int r = 0;
        try {
            String line = br.readLine();
            while (line != null) {
                r++;
                if (r > 1) {
                    String[] s = line.split(";");
                    for (int i = 0; i < s.length; i++) {
                        File tempFile = new File(s[i]);
                        String k="";
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

                        if (tempFile.exists()) {
                            String ext = s[i].substring(s[i].lastIndexOf('.') + 1);
                            filename = destDir + "/" + Integer.toString(r) + " of " + k + "." + ext;                            
                            copyFile(tempFile.getAbsolutePath(), filename);
                        } else {
                            filename = destDir + "/" + Integer.toString(r) + " of " + k + ".png";
                        
                            createImage(s[i], filename);
                        }                        
                    }
                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }

    public void createImage(String Text, String filename) {
        int width = 145;
        int height = 100;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // create a string with yellow
        g2d.setColor(Color.black);
        g2d.drawString(Text, 10, 50);

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

    public static void main(String[] args) throws InterruptedException, IOException {
        Test test = new Test();
        test.readFile("c:/temp/game", "C:/temp/game.csv");
    }
}
