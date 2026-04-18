package com.recycle.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 使用 ZXing 库生成 PNG 格式的二维码图片
 * 用于回收员称重完成后生成溯源二维码，供机构扫码接收
 */
public class QRCodeUtil {

    /** 二维码图片宽度（像素） */
    private static final int WIDTH = 300;

    /** 二维码图片高度（像素） */
    private static final int HEIGHT = 300;

    /**
     * 生成二维码图片并保存到指定目录
     *
     * @param content    二维码内容（通常为订单编号）
     * @param uploadPath 文件保存目录（如 ./uploads/）
     * @return 二维码图片的访问路径（如 /uploads/qr_RC20260413xxxx.png）
     */
    public static String generateQRCode(String content, String uploadPath) {
        try {
            // 设置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 字符编码
            hints.put(EncodeHintType.MARGIN, 1);              // 边距

            // 生成二维码矩阵
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

            // 确保上传目录存在
            Path dirPath = Paths.get(uploadPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 文件名：qr_ + 订单编号 + .png（订单编号唯一，文件名也唯一）
            String fileName = "qr_" + content + ".png";
            Path filePath = dirPath.resolve(fileName);

            // 写入 PNG 文件
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            // 返回可访问的 URL 路径
            return "/uploads/" + fileName;
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败：" + e.getMessage(), e);
        }
    }
}
