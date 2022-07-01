package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName:metadata
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/20 10:58
 * author:ck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class Metadata {

    private Map<String,Object> customData;
    private String department;
    private String folder;
    private ArrayList<String> tags;
    private String fileID;
    private int delete;

}
