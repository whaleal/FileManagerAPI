package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:ArchiveResponse
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/15 10:12
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class ArchiveResponse {

    private String archiveId;
//    文件信息包括文件ID和版本号
    private List<FilesEntity> files;

}
