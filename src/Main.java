import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Poom on 8/12/2017.
 */
public class Main {
    public static void main(String args[])
    {
        String outline  = args[0];
        String resource = args[1];

        File resourceDirectory = new File(resource);

        ArrayList<BufferedImage> resourceList = new ArrayList<>();

        System.out.println("loading image...");

        long dirLen = resourceDirectory.listFiles().length;
        int progress = 0;

        for(File file: resourceDirectory.listFiles())
        {
            ++progress;
            if(progress  % 10 == 0)
            {
                System.out.println((double)progress / dirLen * 100 + "%");
            }
            if(file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png"))
            {
                try
                {
                    BufferedImage toLoad = ImageIO.read(file);
                    BufferedImage compressed = new BufferedImage(PhotoProcessor.blocksize, PhotoProcessor.blocksize, BufferedImage.TYPE_INT_ARGB);
                    compressed.createGraphics();
                    compressed.getGraphics().drawImage(toLoad,0,0,compressed.getWidth(), compressed.getHeight(),null);
                    resourceList.add(compressed);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        PhotoProcessor.ResourceBank resourceBank = PhotoProcessor.generatePhotoResourceBank(resourceList);

        File outlineFile = new File(outline);
        BufferedImage outlineImage = null;
        try
        {
            outlineImage = ImageIO.read(outlineFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        BufferedImage finalImage = PhotoProcessor.generateToOutline(outlineImage, resourceBank);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1900,1080);
        frame.setLayout(new FlowLayout());

        JPanel panel = new JPanel(){
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                g.drawImage(finalImage,0,0,finalImage.getWidth(), finalImage.getHeight(),null);
            }
        };


        panel.setPreferredSize(new Dimension(1880,1060));
        panel.setBackground(Color.BLACK);

        JButton button = new JButton("Save");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File outputfile = new File(args[2]);
                    ImageIO.write(finalImage, "png", outputfile);
                }
                catch(IOException ex)
                {ex.printStackTrace();}
            }
        });

        frame.add(button);
        frame.add(panel);
        frame.setVisible(true);
    }
}
