package com.jinmu.xinda.service;

import com.jinmu.xinda.entity.*;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * ClassName:UserService
 * Package:com.jinmu.xinda.service
 * Description:
 * Date:2022/6/14 11:49
 * author:ck
 */

public interface UserService {

    UserEntity queryByName(String name);

    UserEntity userLogin(String username,String password);

    void updateFileFieldById(String fileId, String folder, String customMetaData, String tags, String id);

    List<FileMetaData> queryBySelective(String folder, String tag, String range);

    void updateFilesById(String ObjectId, String id);

    void updateDelete(ObjectId objectId);

    void updateArchives(FilesEntity filesEntity);

    Archives findIdByFileId(String id);

    List<Archives> queryAllVersionById(String id);
}
