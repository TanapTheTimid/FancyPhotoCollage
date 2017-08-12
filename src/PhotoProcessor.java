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

        return resourceBank;
    }

    public static BufferedImage generateToOutline(BufferedImage outline, ResourceBank resourceBank, int blocksize) {
        BufferedImage finalImage = new BufferedImage(outline.getWidth(), outline.getHeight(), BufferedImage.TYPE_INT_ARGB);
        finalImage.createGraphics();
        Graphics2D g = (Graphics2D)finalImage.getGraphics();

        for(int x = 0; x < finalImage.getWidth() - blocksize + 1; x+=blocksize)
        {
            for(int y = 0; y < finalImage.getHeight() - blocksize + 1; y+=blocksize)
            {
                float luminanceSum = 0;

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

                       luminanceSum += luminance;
                    }
                }

                float avgLumin = luminanceSum / (blocksize * blocksize);

                BufferedImage chosenImage = getClosestLuminanceMatch(avgLumin, resourceBank);

                g.drawImage(chosenImage, x , y , blocksize, blocksize, null);
            }
        }

        return finalImage;
    }

    public static BufferedImage getClosestLuminanceMatch(float outlineLumin, ResourceBank resourceBank)
    {

        float closestLumin = -999;

        for(float resLumin: resourceBank.luminanceMap.keySet())
        {
            if(Math.abs(resLumin - outlineLumin) < Math.abs(closestLumin - outlineLumin))
            {
                closestLumin = resLumin;
            }
        }
        return resourceBank.luminanceMap.get(closestLumin);
    }

    public static class ResourceBank {
        TreeMap<Float, BufferedImage> luminanceMap = new TreeMap<>();
    }
}
