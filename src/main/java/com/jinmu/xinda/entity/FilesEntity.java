package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

/**
 * ClassName:FilesEntity
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/16 10:53
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class FilesEntity {

    private String fileID;

    private ObjectId version;
}
