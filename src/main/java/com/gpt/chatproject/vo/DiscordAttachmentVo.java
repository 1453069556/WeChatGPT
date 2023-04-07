package com.gpt.chatproject.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Discord 附件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordAttachmentVo implements Serializable {
    /**
     * 附件 ID。
     */
    private String id;

    /**
     * 附件文件名。
     */
    private String filename;

    /**
     * 附件文件大小（字节）。
     */
    private int size;

    /**
     * 附件 URL。
     */
    private String url;

    /**
     * 附件代理 URL。
     */
    private String proxyUrl;

    /**
     * 附件宽度（仅针对图片附件）。
     */
    private int width;

    /**
     * 附件高度（仅针对图片附件）。
     */
    private int height;

    /**
     * 附件的 MIME 类型。
     */
    private String contentType;
}