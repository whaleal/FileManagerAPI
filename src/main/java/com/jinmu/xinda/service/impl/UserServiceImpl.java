package com.jinmu.xinda.service.impl;

import com.jinmu.xinda.entity.*;
import com.jinmu.xinda.service.UserService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * ClassName:UserServiceImpl
 * Package:com.jinmu.xinda.service.impl
 * Description:
 * Date:2022/6/14 11:49
 * author:ck
 */

@Service
public class UserServiceImpl implements UserService {


    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private GridFsTemplate gridFsTemplate;


    @Override
    public UserEntity queryByName(String name) {

        UserEntity user = mongoTemplate.findOne(new Query(where("username").is(name)),UserEntity.class);

        return user;
    }

    @Override
    public UserEntity userLogin(String username, String password) {

        return mongoTemplate.findOne(new Query(where("username").is(username).and(password).is(password)),UserEntity.class);
    }

    @Override
    public void updateFileFieldById(String fileId, String folder, String customMetaData, String tags, String id) {
        Update update = new Update();
        if(folder != null){
            update.set("folder",folder);
        }
        if(customMetaData != null){
            update.set("customMetaData",customMetaData);
        }
        if(tags != null){
            update.set("tags",tags);
        }
        if(id != null){
            update.set("id",id);
        }
        mongoTemplate.updateFirst(new Query(where("_id").is(fileId)),update,"fs.files");

    }

    @Override
    public List<FileMetaData> queryBySelective(String folder, String tag, String range) {
        Query query = new Query();

        int start = 0;
        int end = 9;
        if(tag != null){
            query.addCriteria(where("tag").regex(".*?"+tag+".*"));
        }
        if(folder != null){
            query.addCriteria(where("folder").regex(".*?"+folder+".*"));
        }
        if(range != null){
            start = Integer.parseInt(range.substring(0,range.lastIndexOf("-")))-1;
            end = Integer.parseInt(range.substring(range.lastIndexOf("-")+1));
        }
        //MetaList metaList = new MetaList();
        query.skip(start).limit(end-start);
        List<FileMetaData> fsFiles = mongoTemplate.find(query, FileMetaData.class,"fs.files");
        return fsFiles;
    }

    @Override
    public void updateFilesById(String ObjectId, String id) {
        Update update = new Update();

        Query query = new Query(where("_id").is(ObjectId));
        // 查询一个文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(query);


        updateDelete(gridFSFile.getObjectId());

    }

    @Override
    public void updateDelete(ObjectId objectId) {

        Update update = new Update();

        update.set("metadata.delete",1);

        mongoTemplate.updateFirst(new Query(where("_id").is(objectId)),update,"fs.files");
        //mongoTemplate.remove(Query.query(where("files.0.version").is()));

    }

    @Override
    public void updateArchives(FilesEntity filesEntity) {

        //String versionId = filesEntity.getVersion().toString();
        String fileID = filesEntity.getFileID();

        Query query = new Query(where("files.0.fileID").is(fileID));
        Update update = new Update();
        update.set("files.0.version",filesEntity.getVersion());

        mongoTemplate.updateFirst(query,update,"archives");

    }

    @Override
    public Archives findIdByFileId(String id) {

        Query q = new Query(where("files.fileID").is(id));



        return mongoTemplate.findOne(q,Archives.class,"archives");
    }

    @Override
    public List<Archives> queryAllVersionById(String id) {
        return mongoTemplate.find(new Query(where("files.0.fileID").is(id)),Archives.class,"archives");
    }

}
