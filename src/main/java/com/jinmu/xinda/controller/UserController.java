package com.jinmu.xinda.controller;

import com.jinmu.xinda.entity.*;
import com.jinmu.xinda.service.UserService;
import com.jinmu.xinda.util.JwtUtil;
import com.jinmu.xinda.util.RCode;
import com.jinmu.xinda.util.RespResult;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;
import io.jsonwebtoken.Claims;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * ClassName:UserController
 * Package:com.jinmu.xinda.controller
 * Description:
 * Date:2022/6/14 10:12
 * author:ck
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {


    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Resource
    private UserService userService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private MongoTemplate mongoTemplate;

    private Claims claims;


    @PostMapping("/login")
    public RespResult login(@RequestBody UserEntity u) {

        RespResult result = RespResult.fail();
        result.setRCode(RCode.FAIL_LOGIN);
        System.out.println(u.getExpiredIn());

        UserEntity user = userService.queryByName(u.getUsername());
        if (user != null) {
            if (u.getPassword().equals(user.getPassword())) {
                Map<String, Object> data = new HashMap<>();
                data.put("username", u.getUsername());

                //登陆成功，生成token
                String jwtToken = jwtUtil.creatJwt(data, u.getExpiredIn());
                System.out.println(jwtToken);
                result.setAccessToken(jwtToken);
                result.setRCode(RCode.SUCC_LONGIN);
            }
        }

        return result;
    }


    //    按条件查询文件
    @GetMapping("/files")
    public RespResult queryFiles(HttpServletRequest request, String folder, String tag, String range) {
        RespResult result = RespResult.fail();

        int start = 0;
        int end = 9;

        Query query = new Query();

        if (tag != null) {
            query.addCriteria(where("metadata.tags").is(tag));
        }
        if (folder != null) {
            query.addCriteria(where("metadata.folder").is(folder));
        }

        if(range != null){
            start = Integer.parseInt(range.substring(0,range.lastIndexOf("-")))-1;
            end = Integer.parseInt(range.substring(range.lastIndexOf("-")+1));
        }
        query.skip(start).limit(end-start);

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            //e.printStackTrace();
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));

        if (user != null) {

            MetaList metaList = new MetaList();
            List<FileMetaData> list = new ArrayList<>();

            // 查询所有文件
            GridFSFindIterable gridFSFiles = gridFsTemplate.find(query);

            for (GridFSFile gridFSFile : gridFSFiles) {


                System.out.println(gridFSFile.getMetadata().get("tags"));
                FileMetaData fileMetaData = new FileMetaData();
                String q = gridFSFile.getMetadata().getString("fileID");
                Archives archives = mongoTemplate.findOne(new Query(where("files.0.fileID").is(q)), Archives.class, "archives");

                if (archives == null) {
                    continue;
                }


                fileMetaData.setFileID(archives.getFiles().get(0).getFileID());
                fileMetaData.setVersionID(archives.getFiles().get(0).getVersion().toString());
                fileMetaData.setFilename(gridFSFile.getFilename());
                fileMetaData.setUploadDate(gridFSFile.getUploadDate());
                fileMetaData.setCustomProperties(gridFSFile.getMetadata().getString("department"));
                //fileMetaData.getMd5();


                list.add(fileMetaData);


            }
            metaList.setMetaList(list);
            metaList.setCount(list.size());
            result.setObj(metaList);
            result.setRCode(RCode.SUCC_QUERY);

        }
        return result;
    }


    //    增加文件
    @PostMapping("files")
    public RespResult saveFiles(HttpServletRequest request, MultipartFile file, String folder, @RequestParam(required = false) Map<String, Object> CustomMetaData1,
                                @RequestParam(required = false) ArrayList<String> tags, String id) throws FileNotFoundException {
        RespResult result = RespResult.fail();
        FilesEntity filesEntity = new FilesEntity();

        Archives archives = new Archives();
        List<FilesEntity> filesEntityList = new ArrayList<>();

        FileInfoResponse fileInfoResponse = new FileInfoResponse();
        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {

            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));

        if (user != null && file != null) {

            if (id == null || id == "") {
                id = new ObjectId().toString();
            }
            if(file.isEmpty()){
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }
            String fileName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));

            String prefix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

            Map<String,Object> q = new HashMap<>();
            q.put("customMetaData",CustomMetaData1.get("customMetaData"));

            Metadata metadata = new Metadata();
            metadata.setFolder(folder);
            metadata.setCustomData(q);

            metadata.setFileID(id);
            metadata.setDepartment(folder.substring(folder.lastIndexOf("/")+1));
            metadata.setDelete(0);
            metadata.setTags(tags);

            try {

                ObjectId objectId = gridFsTemplate.store(file.getInputStream(), fileName, prefix, metadata);


                filesEntity.setVersion(objectId);
                filesEntity.setFileID(id);
                filesEntityList.add(filesEntity);
                archives.setFiles(filesEntityList);
                mongoTemplate.insert(archives);

                fileInfoResponse.setMessage("上传成功！");
                fileInfoResponse.setFileID(id);
                fileInfoResponse.setVersionID(objectId.toString());
                result.setObj(fileInfoResponse);

            } catch (IOException e) {
                //e.printStackTrace();
                result.setRCode(RCode.FAIL_UPLOAD);
            }

            result.setRCode(RCode.SUCC);
        }

        return result;
    }

    //    获取文件
    @GetMapping("files/{id}")
    public RespResult queryFilesById(HttpServletRequest request, @PathVariable("id") String id, HttpServletResponse response) throws IOException {
        RespResult result = RespResult.fail();

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {


            Archives archives = mongoTemplate.findOne(new Query(where("files.fileID").is(id)), Archives.class, "archives");
            if (archives == null) {
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }
            ObjectId version = archives.getFiles().get(0).getVersion();

            InputStream inputStream = null;

            OutputStream outputStream = null;
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("metadata.fileID").is(id).and("metadata.delete")
                    .is(0)).with(Sort.by(Sort.Direction.DESC,"uploadDate")));
            //GridFSFile gridFSFiles1 = gridFsTemplate.findOne(Query.query(where("metadata.delete").is(0)).with(Sort.by(Sort.Direction.DESC,"uploadDate")));

            if (gridFSFile == null) {

                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;

            }

            //打开下载流对象
            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFSResource,用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //获取流中的数据

            try {
                inputStream = gridFsResource.getInputStream();
                outputStream = new BufferedOutputStream(response.getOutputStream());
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    response.getOutputStream().write(bytes, 0, len);
                }

                result.setRCode(RCode.SUCC_DOWNLOAD);
            } catch (Exception e) {
                e.printStackTrace();
                result.setRCode(RCode.FAIL_DOWNLOAD);
            } finally {
                outputStream.flush();
                outputStream.close();
                inputStream.close();

            }
        }
        return result;
    }


    //    删除文件
    @DeleteMapping("/files/{id}")
    public RespResult deleteFilesById(@PathVariable("id") String id, HttpServletRequest request) {
        RespResult result = RespResult.fail();

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {


            GridFSFindIterable gridFSFile = gridFsTemplate.find(Query.query(where("metadata.fileID").is(id).and("metadata.delete").is(0)));
            if(gridFSFile == null){
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }


            for (GridFSFile file : gridFSFile){
                userService.updateDelete(file.getObjectId());
            }


            result.setRCode(RCode.SUCC_DELETE);
            System.out.println(id);

        }
        return result;
    }

    //    替换现有文件
    @PutMapping("/files/{id}")
    public RespResult replaceFiles(@PathVariable("id") String id, HttpServletRequest request, MultipartFile newFile,
                                   @RequestParam(required = false) ArrayList<String> tags, @RequestParam(required = false) Map<String, Object> customMetaData) {

        RespResult result = RespResult.fail();
        FilesEntity filesEntity = new FilesEntity();

        Archives archives = new Archives();
        List<FilesEntity> filesEntityList = new ArrayList<>();
        FileInfoResponse fileInfoResponse = new FileInfoResponse();

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));


        if (user != null) {

            if(newFile.isEmpty()){
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }
            String fileName = newFile.getOriginalFilename().substring(0, newFile.getOriginalFilename().lastIndexOf("."));
            Archives archives1 = mongoTemplate.findOne(new Query(where("files.0.fileID").is(id)), Archives.class, "archives");
            String prefix = newFile.getOriginalFilename().substring(newFile.getOriginalFilename().lastIndexOf(".") + 1);
            if (archives1 == null) {

                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }
            ObjectId version = archives1.getFiles().get(0).getVersion();

            try {

                Query query = new Query(where("_id").is(version).and("metadata.delete").is(0));
                // 查询一个文件
                GridFSFile gridFSFile = gridFsTemplate.findOne(query);
                if (gridFSFile == null) {
                    result.setRCode(RCode.FILE_NOT_FOUNT);
                    return result;
                }


                //userService.updateArchives();

                Metadata metadata = new Metadata();

                metadata.setFolder(gridFSFile.getMetadata().getString("folder"));
                metadata.setDepartment(gridFSFile.getMetadata().getString("department"));
                if (tags != null) {
                    metadata.setTags(tags);
                }else{
                    //qq.put("tag",gridFSFile.getMetadata().get("tags"));
                    metadata.setTags((ArrayList<String>) gridFSFile.getMetadata().get("tags"));
                }
                Map<String,Object> q = new HashMap<>();

                q.put("customMetaData",customMetaData.get("customMetaData"));
                metadata.setDelete(0);
                if(q.get("customMetaData")=="" || q.get("customMetaData")==null){
                    Object customData = gridFSFile.getMetadata().get("customData");
                    q.put("customMetaData",customData);
                    System.out.println(q);
                    System.out.println(customData);
                    metadata.setCustomData((Map<String, Object>) customData);
                }else {
                    q.put("customMetaData",customMetaData.get("customMetaData"));
                    metadata.setCustomData(q);
                }

                metadata.setFileID(id);


                ObjectId objectId = gridFsTemplate.store(newFile.getInputStream(), fileName, prefix, metadata);
                filesEntity.setVersion(objectId);
                filesEntity.setFileID(id);
                filesEntityList.add(filesEntity);
                archives.setFiles(filesEntityList);
                //mongoTemplate.save(archives);
                //userService.updateArchives(filesEntity);
                mongoTemplate.insert(archives);

                result.setRCode(RCode.SUCC);
                fileInfoResponse.setMessage("上传成功！");
                fileInfoResponse.setFileID(id);
                fileInfoResponse.setVersionID(objectId.toString());
                result.setObj(fileInfoResponse);

                //gridFsTemplate.s
            } catch (IOException e) {
                //e.printStackTrace();
                result.setRCode(RCode.FAIL_UPLOAD);
            }
        }
        return result;
    }


    //    获取文件元数据信息
    @GetMapping("/files/metadata/{id}")
    public RespResult getFilesInfo(@PathVariable("id") String id, HttpServletRequest request) {
        RespResult result = RespResult.fail();
        FileMetaData fileMetaData = new FileMetaData();

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {

            //Archives archives = mongoTemplate.findOne(new Query(where("files.0.fileID").is(id)), Archives.class, "archives");

//            if (archives == null) {
//                result.setRCode(RCode.FILE_NOT_FOUNT);
//                return result;
//            }
            //ObjectId version = archives.getFiles().get(0).getVersion();

            //fileMetaData.setFileID(id);

            //GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(version).and("metadata.delete").is(0)));
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(where("metadata.fileID").is(id).
                    and("metadata.delete").is(0)).with(Sort.by(Sort.Direction.DESC,"uploadDate")));
            fileMetaData.setFilename(gridFSFile.getFilename());
            fileMetaData.setUploadDate(gridFSFile.getUploadDate());
            //Archives archives = userService.findIdByFileId(id);
            fileMetaData.setVersionID(gridFSFile.getObjectId().toString());
            fileMetaData.setFileID(id);
            //fileMetaData.setMd5();
            fileMetaData.setCustomProperties(gridFSFile.getMetadata().getString("folder"));
            result.setRCode(RCode.SUCC);
            result.setObj(fileMetaData);

        }


        return result;
    }

    //    获取文件版本列表
    @GetMapping("/files/versions/{id}")
    public RespResult getFilesVersions(@PathVariable("id") String id, HttpServletRequest request) {
        RespResult result = RespResult.fail();


        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {
            MetaList metaList = new MetaList();
            List<FileMetaData> gridFSFileList = new ArrayList<>();

            List<Archives> list = userService.queryAllVersionById(id);
            for (Archives l : list) {

                ObjectId version = l.getFiles().get(0).getVersion();
                GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(version).and("metadata.delete").is(0)));
                FileMetaData fileMetaData = new FileMetaData();
                fileMetaData.setFileID(id);
                fileMetaData.setVersionID(l.getFiles().get(0).getVersion().toString());
                fileMetaData.setFilename(gridFSFile.getFilename());
                fileMetaData.setUploadDate(gridFSFile.getUploadDate());
                //fileMetaData.setCustomProperties(gridFSFile.getMetadata().getString());

                gridFSFileList.add(fileMetaData);

            }
            metaList.setMetaList(gridFSFileList);
            metaList.setCount(gridFSFileList.size());
            result.setObj(metaList);
            result.setRCode(RCode.SUCC);

        }


        return result;
    }


    //    按版本号获取文件
    @GetMapping("/files/version/{versionId}")
    public RespResult getFilesByVersionId(@PathVariable("versionId") String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RespResult result = RespResult.fail();


        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {

            InputStream inputStream = null;
            OutputStream outputStream = null;

            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(id).and("metadata.delete").is(0)));

            if (gridFSFile == null) {
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }

            //打开下载流对象
            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFSResource,用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //获取流中的数据
            inputStream = gridFsResource.getInputStream();

            try {
                outputStream = new BufferedOutputStream(response.getOutputStream());
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    response.getOutputStream().write(bytes, 0, len);
                }

                //result.setObj(1);
                //f1.get

                result.setRCode(RCode.SUCC_DOWNLOAD);
            } catch (Exception e) {
                e.printStackTrace();
                result.setRCode(RCode.FAIL_DOWNLOAD);
            } finally {
                outputStream.flush();
                outputStream.close();
                inputStream.close();

            }
//
//            String type = gridFSFile.getMetadata().getString("_contentType");
//
//
//            File f1 = new File("C:\\Users\\ck，\\Desktop\\test\\"+gridFSFile.getFilename()+"."+type);
//            if (!f1.exists()) {
//                f1.getParentFile().mkdirs();//递归创建父目录
//            }
//            FileOutputStream fileOutputStream = new FileOutputStream(f1);
//            byte[] bytes = new byte[1025];
//            int len = 0;
//            while ((len = inputStream.read(bytes))!=-1) {
//                fileOutputStream.write(bytes,0,len);
//            }
            inputStream.close();


        }

        return result;
    }

    //    按版本号获取文件元数据信息
    @GetMapping("/files/version/metadata/{versionId}")
    public RespResult getFilesInfoByVersionId(@PathVariable("versionId") String id, HttpServletRequest request) {
        RespResult result = RespResult.fail();

        FileMetaData fileMetaData = new FileMetaData();
        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {

            ObjectId objectId = new ObjectId(id);
            Archives archives = mongoTemplate.findOne(new Query(where("files.0.version").is(objectId)), Archives.class, "archives");
            if (archives == null) {
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }

            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(id).and("metadata.delete").is(0)));
            fileMetaData.setFileID(archives.getFiles().get(0).getFileID());
            //fileMetaData.setMd5();
            fileMetaData.setFilename(gridFSFile.getFilename());
            fileMetaData.setVersionID(id);
            fileMetaData.setUploadDate(gridFSFile.getUploadDate());
            //fileMetaData.setCustomProperties(gridFSFile.getMetadata().getString("folder"));

            result.setObj(fileMetaData);
            result.setRCode(RCode.SUCC);

        }

        return result;
    }


    //    增加归档
    @PostMapping("/archives")
    public RespResult addArchives(HttpServletRequest request, String fileIds) {
        RespResult result = RespResult.fail();
        ArchiveResponse archiveResponse = new ArchiveResponse();
        List<FilesEntity> list = new ArrayList<>();
        Archives archivesRes = new Archives();
        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {

            String[] res = fileIds.split(",");

            for (String r : res) {

                //ObjectId objectId = new ObjectId(r);
                Archives archives = mongoTemplate.findOne(new Query(where("files.0.fileID").is(r)), Archives.class, "archives");

                if (archives == null) {
                    result.setRCode(RCode.FILE_NOT_FOUNT);
                    return result;
                }
                list.add(archives.getFiles().get(0));


                //archiveResponse.setArchiveId();
            }

            archivesRes.setFiles(list);
            Archives save = mongoTemplate.save(archivesRes, "archives");


            archiveResponse.setFiles(list);

            archiveResponse.setArchiveId(save.get_id());

            result.setObj(archiveResponse);
            result.setRCode(RCode.SUCC);

        }

        return result;
    }


    //    获取归档中文件信息
    @GetMapping("/archives/{id}")
    public RespResult getArchivesInfo(@PathVariable("id") String id, HttpServletRequest request) {
        RespResult result = RespResult.fail();

        ArchiveResponse a = new ArchiveResponse();
        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {

            Archives one = mongoTemplate.findOne(new Query(where("_id").is(id)), Archives.class, "archives");
            if (one == null) {
                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;
            }

            a.setArchiveId(one.get_id());
            a.setFiles(one.getFiles());
            result.setObj(a);
            result.setRCode(RCode.SUCC);

        }

        return result;
    }

    //    更新文件元数据信息
    @PutMapping("/files/metadata/{id}")
    public RespResult updateFilesInfo(@PathVariable("id") String id, HttpServletRequest request, String folder,
                                      @RequestParam(required = false) ArrayList<String> tag, @RequestParam(required = false) Map<String, Object> customMetadata) {
        RespResult result = RespResult.fail();

        FileInfoResponse fileInfoResponse = new FileInfoResponse();

        try {
            claims = jwtUtil.readJwt(request.getHeader("token"));
        } catch (Exception e) {
            result.setRCode(RCode.TOKEN_INVALID);
            return result;
        }
        UserEntity user = userService.userLogin((String) claims.get("username"), (String) claims.get("password"));
        if (user != null) {


            Archives archives = mongoTemplate.findOne(new Query(where("files.fileID").is(id)), Archives.class, "archives");
            if (archives == null) {

                result.setRCode(RCode.FILE_NOT_FOUNT);
                return result;

            }
            //GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(where("_id").is(archives.getFiles().get(0).getVersion()).and("metadata.delete").is(0)));

            Map<String,Object> q = new HashMap<>();
            Update update = new Update();

            if (folder != null) {
                update.set("metadata.folder", folder);
            }
            if (tag != null) {
                update.set("metadata.tags", tag);
            }

            if(!customMetadata.isEmpty()){
                q.put("customMetaData",customMetadata.get("customMetaData"));

                if(q.get("customMetaData") !="" || q.get("customMetaData")!=null){
                    q.put("customMetaData",customMetadata.get("customMetaData"));
                    update.set("metadata.customData.customMetaData", q.get("customMetaData"));
                }
            }


            //GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("metadata.fileID").is(id).and("metadata.delete")
                    //.is(0)).with(Sort.by(Sort.Direction.DESC,"uploadDate")));
            GridFSFile uploadDate = gridFsTemplate.findOne(new Query(where("metadata.fileID").is(id).and("metadata.delete")
                    .is(0)).with(Sort.by(Sort.Direction.DESC, "uploadDate")));


            Query query1 = new Query();
            query1.addCriteria(where("_id").is(uploadDate.getObjectId()));
            mongoTemplate.updateFirst(query1, update, "fs.files");



//            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(archives.getFiles().get(0).getVersion()).and("metadata.delete").is(0)));
//            if (gridFSFile == null) {
//                result.setRCode(RCode.FILE_NOT_FOUNT);
//                return result;
//            }

            fileInfoResponse.setFileID(archives.getFiles().get(0).getFileID());
            fileInfoResponse.setVersionID(archives.getFiles().get(0).getVersion().toString());


            //fileInfoResponse.setMessage();

            result.setObj(fileInfoResponse);
            result.setRCode(RCode.SUCC);

        }

        return result;
    }


}
