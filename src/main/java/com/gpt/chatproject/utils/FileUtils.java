package com.gpt.chatproject.utils;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@Component
public class FileUtils {
    /**
     * 根据BASE64获取文件
     *
     * @param b64
     * @param suffix
     * @return
     */
    public File getFileByBase64(String b64, String suffix) {
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), suffix);
            Files.write(tempFile.toPath(), bytes);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * jpg转png
     *
     * @param jpgImage
     * @return
     */
    public File jpgToPng(File jpgImage) {
        try {
            BufferedImage image = ImageIO.read(jpgImage);
            File pngImage = File.createTempFile("jpgToPng-",".png");
            ImageIO.write(image, "png", pngImage);
            Files.deleteIfExists(jpgImage.toPath());
            return pngImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将图片缩放为长宽相等的图片
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static File scaleImage(File file) throws IOException {
        try {
            //读取原图像
            BufferedImage originalImage = ImageIO.read(file);

            //计算目标宽度和高度
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int targetSize = Math.min(originalWidth, originalHeight);

            //创建缩放后的图像对象
            BufferedImage scaledImage = new BufferedImage(targetSize, targetSize, originalImage.getType());

            //绘制缩放后的图像
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(originalImage, 0, 0, targetSize, targetSize, null);
            graphics2D.dispose();

            //将缩放后的图像写入到临时文件中
            File scaledFile = File.createTempFile("scaled-", ".png");
            ImageIO.write(scaledImage, "png", scaledFile);
            return scaledFile;
        } finally {
            if (file != null) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }


}
