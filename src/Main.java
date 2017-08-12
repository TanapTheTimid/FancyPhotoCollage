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
    static BufferedImage finalImage;
    static String args[];

    static int blocksize = 30;

    static JTextField outlineLocation;
    static JPanel panel;

    static PhotoProcessor.ResourceBank resourceBank;

    public static void main(String args[])
    {
        Main.args = args;

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
                    BufferedImage compressed = new BufferedImage(blocksize, blocksize, BufferedImage.TYPE_INT_ARGB);
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

        resourceBank = PhotoProcessor.generatePhotoResourceBank(resourceList);

        genUI();
    }

    public static void createMosaic()
    {
        File outlineFile = new File(outlineLocation.getText());
        BufferedImage outlineImage = null;
        try
        {
            outlineImage = ImageIO.read(outlineFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        finalImage = PhotoProcessor.generateToOutline(outlineImage, resourceBank, blocksize);

        panel.repaint();
    }

    public static void genUI()
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1800,900);
        frame.setLayout(new FlowLayout());

        panel = new JPanel(){
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                if(finalImage != null) {
                    double scale = 1;
                    if (finalImage.getWidth() > 880.0) {
                        scale = 880.0 / finalImage.getWidth();
                    } else if (finalImage.getHeight() > 1780) {
                        scale = 1780.0 / finalImage.getHeight();
                    }
                    g.drawImage(finalImage, 0, 0, (int) (finalImage.getWidth() * scale), (int) (finalImage.getHeight() * scale), null);
                }
            }
        };


        panel.setPreferredSize(new Dimension(1780,880));
        panel.setBackground(Color.BLACK);

        JButton button = new JButton("Save");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(finalImage != null) {
                    try {
                        File outputfile = new File(args[2]);
                        ImageIO.write(finalImage, "png", outputfile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JLabel jLable = new JLabel("Location of Outline");

        outlineLocation = new JTextField(args[0]);

        JButton create = new JButton("Create");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createMosaic();
            }
        });


        frame.add(jLable);
        frame.add(outlineLocation);
        frame.add(create);
        frame.add(button);
        frame.add(panel);
        frame.setVisible(true);
    }
}
