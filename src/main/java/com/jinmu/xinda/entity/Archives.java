package com.jinmu.xinda.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName:Archives
 * Package:com.jinmu.xinda.entity
 * Description:
 * Date:2022/6/20 9:45
 * author:ck
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class Archives {

    private String _id;

    private List<FilesEntity> files;


}
