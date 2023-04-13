package com.gpt.chatproject.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class FileUtils {

    @Value("${openai.use_proxy}")
    private static Integer USE_PROXY;
    @Value("${aliyun.access_key_id}")
    private String ACCESS_KEY_ID;
    @Value("${aliyun.access_key_secret}")
    private String ACCESS_KEY_SECRET;
    @Value("${aliyun.midjourney.end_point}")
    private String END_POINT;
    @Value("${aliyun.midjourney.bucket_name}")
    private String BUCKET_NAME;

    /**
     * 上传文件并获取url
     *
     * @param file 文件
     * @return
     */
    public String uploadAndGetUrl(File file) {
        String accessKeyId = ACCESS_KEY_ID;
        String accessKeySecret = ACCESS_KEY_SECRET;
        String endpoint = END_POINT;
        String bucketName = BUCKET_NAME;
        String objectName = file.getName();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 将文件上传到指定的存储桶中
            ossClient.putObject(bucketName, objectName, file);
            // 生成上传文件的公开链接
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            // 返回清理后的URL
            return url.toString().replaceAll("\\?.*", "");
        } catch (OSSException | ClientException e) {
            e.printStackTrace();
            return null;
        } finally {
            // 关闭OSS客户端
            ossClient.shutdown();
        }
    }

    /**
     * 下载图片文件并存入ConcurrentHashMap
     */
    public CompletableFuture<File> downloadImageAsync(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client;
            if (USE_PROXY == 0) {
                client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();
            } else {
                client = new OkHttpClient.Builder()
                        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10810)))
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();
            }
            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();
            File outFile = null;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                assert response.body() != null;
                InputStream inputStream = response.body().byteStream();
                BufferedImage image = ImageIO.read(inputStream);
                outFile = File.createTempFile("MidjourneyTempPic-", ".jpg");
                ImageIO.write(image, "jpg", outFile);
                return outFile;
            } catch (Exception e) {
                // 下载失败，删除已经下载但下载失败的图片，并抛出异常
                if (outFile != null) {
                    try {
                        Files.deleteIfExists(outFile.toPath());
                    } catch (IOException ex) {
                        // 忽略删除文件失败的异常
                    }
                }
                throw new RuntimeException("Failed to download image: " + imageUrl, e);
            }
        });
    }

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
            File pngImage = File.createTempFile("jpgToPng-", ".png");
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
