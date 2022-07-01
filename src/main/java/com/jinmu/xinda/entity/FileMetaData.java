package com.jinmu.xinda.entity;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * ClassName:FileMetaData
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/15 10:09
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class FileMetaData {

    private String fileID;


    private String versionID;

    private String filename;

    private Date uploadDate;

    //private String uploadData;
//    文件校验
    private String md5;
//    用户自定义元数据
    private String customProperties;


}
