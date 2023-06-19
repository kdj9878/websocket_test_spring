package com.example.demo.util.data;

import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 파일 저장 및 삭제를 비롯한 데이터 관리를 위한 추상 클래스
 */
public abstract class DataAbstractClass {

    private static String baseUrl;

    public DataAbstractClass(String profile) {
        Assert.notNull(profile);
        if(profile.equals("local")){
            //프로젝트 폴더 경로
            baseUrl = System.getProperty("user.dir");
        }
        else if(profile.equals("dev")){
            baseUrl = "/home/side1_back";
        }
    }

    protected String getWholeFileDirectory(String adaptUrl){
        return baseUrl + adaptUrl;
    }

    protected boolean checkFileDirectory(String adaptUrl){
        return Files.exists(Path.of(baseUrl + adaptUrl));
    }

    protected void createFolder(String adaptUrl){
        File file = new File(baseUrl + adaptUrl);
        try{
            if(!file.exists()){
                file.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected abstract void saveWithoutPath(String file, String fileName, String category);

    protected abstract void saveWithPath(String file, String fileName, String category, String path);
    protected abstract void delete(String category, String fileName) throws IOException;
}
