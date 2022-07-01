package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;
import sun.nio.cs.ISO_8859_7;

/**
 * ClassName:FsFiles
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/16 10:07
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class FsFiles {

    private String filename;
    private String length;
    private String chunkSize;
    private String uploadDate;

    private String folder;
    private String customMetaData;
    private String id;
    private String tags;

}
