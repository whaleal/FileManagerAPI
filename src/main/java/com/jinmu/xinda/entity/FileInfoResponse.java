package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

/**
 * ClassName:FileInfoResponse
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/15 10:04
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class FileInfoResponse {

    private String message;
    private String fileID;
    private String versionID;

}
