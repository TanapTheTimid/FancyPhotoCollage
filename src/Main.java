import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;

/**
 * Created by Poom on 8/12/2017.
 */
public class Main {
    static BufferedImage finalImage;
    static String args[];

    static int blocksize = 30;

    static JTextField outlineLocation;
    static JTextField saveFileName;
    static JPanel panel;
    static JProgressBar resourceLoadBar;

    static PhotoProcessor.ResourceBank resourceBank;
    static JFrame firstFrame;

    static ArrayList<BufferedImage> resourceList;

    public static void main(String args[])
    {
        firstFrame  = new JFrame();
        firstFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        firstFrame.setSize(500,500);
        firstFrame.setLayout(new FlowLayout());

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

        resourceLoadBar = new JProgressBar();
        resourceLoadBar.setMaximum(102);
        resourceLoadBar.setMinimum(0);
        resourceLoadBar.setPreferredSize(new Dimension(400,20));
        resourceLoadBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JProgressBar progressBar = (JProgressBar) e.getSource();
                if(progressBar.getValue() == 102)
                {
                    resourceBank = PhotoProcessor.generatePhotoResourceBank(resourceList);
                    JOptionPane.showMessageDialog(firstFrame, "Done Loading Resources!");
                    genUI();
                }
            }
        });

        JLabel lable = new JLabel("Picture resource folder location:");
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(400,20));
        JButton button = new JButton("Load Resources");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                genResourceBank(field.getText());
            }
        });

        firstFrame.add(lable);
        firstFrame.add(field);
        firstFrame.add(button);
        firstFrame.add(resourceLoadBar);
        firstFrame.setVisible(true);
    }

    public static void genResourceBank(String resourceLocation)
    {
        FutureTask<Void> task = new FutureTask<Void>(new Runnable() {
            @Override
            public void run() {

                File resourceDirectory = new File(resourceLocation);

                resourceList = new ArrayList<>();

                System.out.println("loading image...");

                long dirLen = resourceDirectory.listFiles().length;
                int progress = 0;

                for(File file: resourceDirectory.listFiles())
                {
                    ++progress;
                    if(progress  % 10 == 0)
                    {
                        resourceLoadBar.setValue((int)((double)progress / dirLen * 100));
                        resourceLoadBar.revalidate();
                        resourceLoadBar.repaint();
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
                resourceLoadBar.setValue(102);
                //
            }
        },null);

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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

        outlineFile = null;

        finalImage = PhotoProcessor.generateToOutline(outlineImage, resourceBank, blocksize);

        panel.repaint();
    }

    static boolean isUILoaded = false;

    public static void genUI()
    {
        if(!isUILoaded) {
            firstFrame.setVisible(false);
            firstFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            firstFrame.setSize(1800, 900);

            panel.setPreferredSize(new Dimension(1780, 880));
            panel.setBackground(Color.BLACK);

            JButton button = new JButton("Save");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (finalImage != null) {
                        try {
                            File outputfile = new File(saveFileName.getText() + ".png");
                            ImageIO.write(finalImage, "png", outputfile);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            JLabel jLable = new JLabel("Location of Outline");

            outlineLocation = new JTextField();
            outlineLocation.setPreferredSize(new Dimension(400, 20));

            saveFileName = new JTextField();
            saveFileName.setPreferredSize(new Dimension(400, 20));

            JButton create = new JButton("Create");
            create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createMosaic();
                }
            });


            firstFrame.add(jLable);
            firstFrame.add(outlineLocation);
            firstFrame.add(create);
            firstFrame.add(saveFileName);
            firstFrame.add(button);
            firstFrame.add(panel);


            firstFrame.validate();
            firstFrame.repaint();

            firstFrame.setVisible(true);
        }
        isUILoaded = true;
    }
}
