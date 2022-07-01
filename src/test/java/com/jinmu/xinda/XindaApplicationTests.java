package com.jinmu.xinda;

import com.jinmu.xinda.entity.*;

import com.jinmu.xinda.service.UserService;
import com.jinmu.xinda.util.JwtUtil;
import com.jinmu.xinda.util.RCode;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.jsonwebtoken.Claims;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootTest
class XindaApplicationTests {


    @Resource
    private GridFsTemplate gridFsTemplate;
    @Resource
    private MongoTemplate mongoTemplate;



    @Resource
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    public void test1(){
        System.out.println(1);
    }

    @Test
    void test2(){

        UserEntity u1 = new UserEntity();
        u1.setUsername("zss");
        u1.setPassword("zs");
        u1.setIndividualUserID("123");

        mongoTemplate.save(u1);

        UserEntity u2 = new UserEntity();
        u2.setUsername("lss");
        u2.setPassword("ls");

        u2.setIndividualUserID("123");
        mongoTemplate.save(u2);
    }

    @Test
    public void test3(){

        Map<String,Object> data = new HashMap<>();
        data.put("11","22");
        data.put("qq","aa");
        System.out.println(jwtUtil.creatJwt(data, 12));

    }

    @Test
    public void test31(){

        FilesEntity file = new FilesEntity();
        List<FilesEntity> list = new ArrayList<>();
        file.setFileID("111");
        ObjectId objectId = new ObjectId("62afdf580683a2106542556d");
        file.setVersion(objectId);
        list.add(file);
        Archives archives = new Archives();
        archives.setFiles(list);

        mongoTemplate.save(archives);

    }

    @Test
    public void test4() throws Exception {

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTUyNjIwMTUsImlhdCI6MTY1NTI2MTI5NSwianRpIjoiODVBRTlFQzlFNDA0NEM3M0JDQjlDMTJCRUFEN0JGOEEiLCIxMSI6IjIyIiwicXEiOiJhYSJ9.24vGpP225UhpImt2uj09JoMfLvWIcB9F1SPCxvaMX_4";

        Claims claims = jwtUtil.readJwt(jwt);

        System.out.println(claims);
        System.out.println(claims.get("11"));
        System.out.println(claims.get("qq"));


    }


    @Test
    public void test5(){

//        userService.updateFileFieldById("62a9aa6559b6d42f1357249d","111","{1 2 3}","chen kang","123");
//        Query query = new Query(where("_id").is("62a99df767f2a634d1fde160"));
        Update update1 = new Update();

//        GridFSFile gridFSFile = gridFsTemplate.findOne(query);
//        ObjectId objectId = gridFSFile.getObjectId();
//        System.out.println(objectId);
       // update1.set("files_id",objectId);
        update1.set("delete",1);

        mongoTemplate.updateFirst(new Query(where("_id").is("62afe8e419114d1186028802")),update1,"fs.files");
        //gridFsTemplate.delete(new Query(where("fileId").is("")));
    }

    @Test
    public void test6() {

        //Query query = new Query();
        String tag = "lss";
        String folder = "1";
        String range = "3";

        Query query = new Query();
        // 查询所有文件
        GridFSFindIterable gridFSFiles = gridFsTemplate.find(query);
        for (GridFSFile gridFSFile : gridFSFiles) {

            //query.addCriteria(where(gridFsTemplate.findOne(new Query(where("filename").is("正则表达式"))).getMetadata().getString("folder")).regex(".*?"+"a"+".*"));

            System.out.println(gridFSFile.getMetadata());
            System.out.println(gridFSFile.getMetadata().getString("folder"));
            if (gridFSFile.getMetadata().getString("folder") != null &&
                    gridFSFile.getMetadata().getString("folder").contains("use")) {
                System.out.println(11111);

            }

        }

        if(range == null){
            range = "9";
        }
        // 查询一个文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is("62afe87c19114d1186028800")));
        System.out.println(gridFSFile.getMetadata());
        System.out.println(gridFSFile.getMetadata().getString("delete"));
        System.out.println("======================="
                +gridFSFile.getMetadata().toJson());
        System.out.println(gridFSFile);
    }


    @Test
    public void test7() {
        // 查询一个文件
        //Map<String,Object> map = new HashMap<>();

        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is("62b128704694fd54bab4e94c")));

        System.out.println(gridFSFile.getMetadata().get("customData").toString());
        System.out.println(gridFSFile.getFilename());
        //System.out.println(gridFSFile.getMetadata().getString("delete"));
        //map.put("metadata",gridFSFile.getMetadata());

       // gridFSFile.getMetadata().getString()
        //System.out.println(map);

        //userService.updateDelete(gridFSFile.getObjectId());

//        System.out.println("======================="
//                +gridFSFile.getMetadata().toJson().concat("delete"));
//        System.out.println("======================="
//                +gridFSFile.getMetadata().toJson());
//        System.out.println("======================="
//                +gridFSFile.getMetadata().toJson().concat("delete").intern());
//        System.out.println(gridFSFile);
    }

    @Test
    public void test8(){
        ObjectId objectId = new ObjectId();

        FilesEntity filesEntity = new FilesEntity();
        filesEntity.setFileID("62b1252accb3c249ed676814");
        filesEntity.setVersion(objectId);
        System.out.println(objectId);

        userService.updateArchives(filesEntity);

    }


    @Test
    public void test9(){


       //List<Archives> archives = mongoTemplate.find(new Query(), Archives.class, "archives");

       String q = "2-4";
       //folder.substring(folder.lastIndexOf("/")+1

        int start = Integer.parseInt(q.substring(0,q.lastIndexOf("-")))-1;
        int end = Integer.parseInt(q.substring(q.lastIndexOf("-")+1));
        System.out.println(q.substring(0,q.lastIndexOf("-")));
        System.out.println(q.substring(q.lastIndexOf("-")+1));
        GridFSFindIterable gridFSFiles = gridFsTemplate.find(new Query().skip(start).limit(end-start));
        for(GridFSFile file : gridFSFiles){
            System.out.println(file);
        }


    }


}
