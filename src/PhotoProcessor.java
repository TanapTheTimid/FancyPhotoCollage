/**
 * Created by Poom on 8/12/2017.
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.Color;
import java.nio.Buffer;
import java.util.*;
import java.util.List;

public class PhotoProcessor {

    public static int blocksize = 20;
    static double compressionConstant = 1.3;

    public static ResourceBank generatePhotoResourceBank(ArrayList<BufferedImage> resourceList) {
        ResourceBank resourceBank = new ResourceBank();

        System.out.println("processing images...");

        int progress = 0;

        for (BufferedImage image : resourceList) {
            ++progress;
            if(progress % 10 == 0)
            {
                System.out.println(progress / (double)resourceList.size() * 100 + "%");
            }


            int width = image.getWidth();
            int height = image.getHeight();


            float luminanceSum = 0;
            int pixelCount = 0;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = image.getRGB(x, y);

                    //extract color component
                    int red = (color >>> 16) & 0xFF;
                    int green = (color >>> 8) & 0xFF;
                    int blue = (color >>> 0) & 0xFF;

                    //calculate luminance in range from 0.0 to 1.0 using srgb luminance constants
                    float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

                    luminanceSum += luminance;
                    ++pixelCount;
                }
            }

            float averageLuminance = luminanceSum / pixelCount;

            resourceBank.luminanceMap.put(averageLuminance, image);
        }

        resourceBank.generateDarknessThreshold();

        return resourceBank;
    }

    public static BufferedImage generateToOutline(BufferedImage outline, ResourceBank resourceBank) {
        BufferedImage finalImage = new BufferedImage(outline.getWidth(), outline.getHeight(), BufferedImage.TYPE_INT_ARGB);
        finalImage.createGraphics();
        Graphics2D g = (Graphics2D)finalImage.getGraphics();

        Set<Float> keyset = resourceBank.luminanceMap.keySet();

        int firstBrightIndex = 0;

        int count = 0;
        for(float luminance: keyset)
        {
            if(luminance > resourceBank.darknessThreshold)
            {
                firstBrightIndex = count;
                break;
            }
            ++count;
        }

        ArrayList<Float> keyList = new ArrayList<>();

        keyList.addAll(keyset);

        ArrayList<Float> brightList = new ArrayList<>();
        brightList.addAll(keyList.subList(firstBrightIndex, keyList.size()));

        ArrayList<Float> darkList = new ArrayList<>();
        darkList.addAll(keyList.subList(0, firstBrightIndex));

        Random random = new Random();

        for(int x = 0; x < finalImage.getWidth() - blocksize + 1; x+=blocksize)
        {
            for(int y = 0; y < finalImage.getHeight() - blocksize + 1; y+=blocksize)
            {
                boolean hasBlack = false;
                for(int innerx = x; innerx < x + blocksize; innerx++)
                {
                    for(int innery = y; innery < y + blocksize; innery++)
                    {
                        int color = outline.getRGB(innerx, innery);

                        //extract color component
                        int red = (color >>> 16) & 0xFF;
                        int green = (color >>> 8) & 0xFF;
                        int blue = (color >>> 0) & 0xFF;

                        //calculate luminance in range from 0.0 to 1.0 using srgb luminance constants
                        float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

                        hasBlack = hasBlack || (luminance < 0.5);
                    }
                }

                float luminance;
                if(!hasBlack)
                {
                    if(brightList.isEmpty())
                    {
                        brightList.addAll(keyList.subList(firstBrightIndex, keyList.size()));
                    }
                    luminance = brightList.remove(random.nextInt(brightList.size()));
                }
                else
                {
                    if(darkList.isEmpty())
                    {
                        darkList.addAll(keyList.subList(0, firstBrightIndex));
                    }
                    luminance = darkList.remove(random.nextInt(darkList.size()));
                }

                g.drawImage(resourceBank.luminanceMap.get(luminance), x , y , blocksize, blocksize, null);
            }
        }

        return finalImage;
    }

    public static class ResourceBank {
        TreeMap<Float, BufferedImage> luminanceMap = new TreeMap<>();
        float darknessThreshold;

        public void generateDarknessThreshold()
        {
            float sum = 0;
            int count = 0;
            for(float luminance: luminanceMap.keySet())
            {
                sum += Math.pow((luminance*10),1/(compressionConstant));
                ++count;
            }

            darknessThreshold = sum/10 / count;
        }
    }
}
