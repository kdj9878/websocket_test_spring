package com.example.demo.util.data.impl;

import com.example.demo.util.data.DataAbstractClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ImgUtil extends DataAbstractClass {

    private static final String ROOT_FOLDER = "/img/chatroom/";
    public ImgUtil(String profile) {
        super(profile);
    }

    /**
     * 디렉터리가 존재할 경우 디렉토리만 반환, 디렉터리가 존재하지 않을 경우 디렉토리를 생성하고 반환
     * @param roomId
     * @return 파일 디렉토리를 반환
     */
    public void checkAndCreateDirectory(String roomId) {
        String adaptUrl = ROOT_FOLDER + roomId;
        if(!checkFileDirectory(adaptUrl)){
            createDirectory(adaptUrl);
        }
    }

    public void saveChunkFile(String roomId, String fileName, byte[] data, Long chunkNumber){
        try {
            String tempFileName = fileName + ".part." + chunkNumber;
            Path filePath = Paths.get(getWholeFileDirectory(ROOT_FOLDER + roomId), tempFileName);
            Files.write(filePath, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFinalChunkFile(String roomId, String fileName, String fileType, Long finalFileSeq) {
        try{
            String outputFileName = UUID.randomUUID() + fileName + "." + fileType;
            Path outputFile = Paths.get(getWholeFileDirectory(ROOT_FOLDER + roomId), outputFileName);
            Files.createFile(outputFile);

            for(int i = 0; i < finalFileSeq; i++){
                Path chunkFile = Paths.get(getWholeFileDirectory(ROOT_FOLDER + roomId), fileName + ".part." + i);
                Files.write(outputFile, Files.readAllBytes(chunkFile), StandardOpenOption.APPEND);
                Files.delete(chunkFile);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
