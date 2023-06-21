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

    /**
     * 디렉토리가 존재할 경우 true, 존재하지 않을 경우 false 반환
     * @param adaptUrl
     * @return
     */
    protected boolean checkFileDirectory(String adaptUrl){
        return Files.exists(Path.of(baseUrl + adaptUrl));
    }

    protected void createDirectory(String adaptUrl){
        try{
            Files.createDirectory(Path.of(baseUrl + adaptUrl));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
