package com.offcn.upload;

import com.offcn.entity.Result;
import com.offcn.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    @Value("${file_ip}")
    private String file_ip;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        try {
            String filename = file.getOriginalFilename();
            //图片的后缀
            String exName = filename.substring(filename.lastIndexOf(".")+1);

            FastDFSClient client = new FastDFSClient("classpath:fdfs_client.conf");

            String url = client.uploadFile(file.getBytes(), exName);

            url = file_ip+url;

            return new Result(true,url);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"图片上传失败");
        }
    }
}
