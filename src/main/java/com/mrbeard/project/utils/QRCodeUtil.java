package com.mrbeard.project.utils;

import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * 二维码生成读取工具
 *
 * @author: hubin
 * @date: 2020/11/17 16:49
 */
public class QRCodeUtil {

    /**
     * 字符编码
     */
    private static final String CHARSET = "UTF-8";
    /**
     * 图片格式
     */
    private static final String FORMAT_NAME = "PNG";
    /**
     * LOGO宽度
     */
    private static int WIDTH = 60;
    /**
     * LOGO高度
     */
    private static int HEIGHT = 60;

    /**
     * 创建二维码图片
     *
     * @param content      二维码内容
     * @param logoImgPath  Logo
     * @param needCompress 是否压缩Logo
     * @param qrcodeWidth  二维码宽度
     * @param qrcodeHeight 二维码高度
     * @return 返回二维码图片
     * @throws WriterException
     * @throws IOException     BufferedImage
     */
    private static BufferedImage createImage(String content, String logoImgPath, boolean needCompress, int qrcodeWidth, int qrcodeHeight) throws WriterException, IOException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, qrcodeWidth, qrcodeHeight, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (logoImgPath == null || "".equals(logoImgPath)) {
            return image;
        }

        // 插入图片
        QRCodeUtil.insertImage(image, logoImgPath, needCompress, qrcodeWidth, qrcodeHeight);
        return image;
    }

    /**
     * 添加Logo
     *
     * @param source       二维码图片
     * @param logoImgPath  Logo
     * @param needCompress 是否压缩Logo
     * @throws IOException void
     */
    private static void insertImage(BufferedImage source, String logoImgPath, boolean needCompress, int qrcodeWidth, int qrcodeHeight) throws IOException {
        File file = new File(logoImgPath);
        if (!file.exists()) {
            return;
        }

        Image src = ImageIO.read(new File(logoImgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        // 压缩LOGO
        if (needCompress) {
            if (width > WIDTH) {
                width = WIDTH;
            }

            if (height > HEIGHT) {
                height = HEIGHT;
            }

            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            // 绘制缩小后的图
            g.drawImage(image, 0, 0, null);
            g.dispose();
            src = image;
        }

        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (qrcodeWidth - width) / 2;
        int y = (qrcodeHeight - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 生成带Logo的二维码
     *
     * @param content      二维码内容
     * @param logoImgPath  Logo
     * @param destPath     二维码输出路径
     * @param needCompress 是否压缩Logo
     * @throws Exception void
     */
    public static void encode(String content, String logoImgPath, String destPath, boolean needCompress, int qrcodeWidth, int qrcodeHeight) throws Exception {
        QRCodeUtil.WIDTH = qrcodeWidth / 4;
        QRCodeUtil.HEIGHT = qrcodeHeight / 4;
        BufferedImage image = QRCodeUtil.createImage(content, logoImgPath, needCompress, qrcodeWidth, qrcodeHeight);
        mkdirs(destPath);
        ImageIO.write(image, FORMAT_NAME, new File(destPath));
    }

    /**
     * 生成不带Logo的二维码
     *
     * @param content  二维码内容
     * @param destPath 二维码输出路径
     */
    public static void encode(String content, String destPath, int qrcodeWidth, int qrcodeHeight) throws Exception {
        QRCodeUtil.encode(content, null, destPath, false, qrcodeWidth, qrcodeHeight);
    }

    /**
     * 生成带Logo的二维码，并输出到指定的输出流
     *
     * @param content      二维码内容
     * @param logoImgPath  Logo
     * @param output       输出流
     * @param needCompress 是否压缩Logo
     */
    public static void encode(String content, String logoImgPath, OutputStream output, boolean needCompress, int qrcodeWidth, int qrcodeHeight) throws Exception {
        QRCodeUtil.WIDTH = qrcodeWidth / 4;
        QRCodeUtil.HEIGHT = qrcodeHeight / 4;
        BufferedImage image = QRCodeUtil.createImage(content, logoImgPath, needCompress, qrcodeWidth, qrcodeHeight);
        ImageIO.write(image, FORMAT_NAME, output);
    }

    /**
     * 生成不带Logo的二维码，并输出到指定的输出流
     *
     * @param content 二维码内容
     * @param output  输出流
     * @throws Exception void
     */
    public static void encode(String content, OutputStream output, int qrcodeWidth, int qrcodeHeight) throws Exception {
        QRCodeUtil.encode(content, null, output, false, qrcodeWidth, qrcodeHeight);
    }

    /**
     * 二维码解析
     *
     * @param file 二维码
     * @return 返回解析得到的二维码内容
     * @throws Exception String
     */
    public static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        String resultStr = result.getText();
        return resultStr;
    }

    /**
     * 二维码解析
     *
     * @param path 二维码存储位置
     * @return 返回解析得到的二维码内容
     * @throws Exception String
     */
    public static String decode(String path) throws Exception {
        return QRCodeUtil.decode(new File(path));
    }

    /**
     * 判断路径是否存在，如果不存在则创建
     *
     * @param dir 目录
     */
    public static void mkdirs(String dir) {
        if (dir != null && !"".equals(dir)) {
            File file = new File(dir);
            if (!file.isDirectory()) {
                file.mkdirs();
            }
        }
    }
}
