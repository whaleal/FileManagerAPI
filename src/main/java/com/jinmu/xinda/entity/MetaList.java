package com.jinmu.xinda.entity;


import com.mongodb.client.gridfs.model.GridFSFile;
import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName:MetaList
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/15 10:05
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class MetaList {

    private List<FileMetaData> metaList;
    private Integer count;


}
