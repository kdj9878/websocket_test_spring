package com.example.demo.util.data.impl;

import com.example.demo.util.data.DataAbstractClass;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
public class ImgUtil extends DataAbstractClass {

    private static final String ROOT_FOLDER = "/img/";
    public ImgUtil(String profile) {
        super(profile);
    }

    /**
     * base64로 인코딩 된 이미지 파일을 디코딩
     * @param base64Img
     * @return Object[0] -> 이미지파일, Object[1] -> 이미지 타입
     */
    private Object[] decodingBase64(String base64Img){
        BufferedImage img = null;
        String type = null;
        try{
            String[] splitArr = base64Img.split(",");
            String base64Image = splitArr[1];
            type = splitArr[0].split("/")[1].split(";")[0];
            byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Image);
            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        }catch (IOException e){
            log.error("An error occurred while base64 to image \n\r, {}", base64Img);
            e.printStackTrace();
        }
        return new Object[]{img, type};
    }

    @Override
    protected void saveWithoutPath(String base64Img, String fileName, String category) {
        String adaptUrl = ROOT_FOLDER + category;
        try{
            Object[] decodingArray = decodingBase64(base64Img);
            Assert.notNull(decodingArray[0]);
            saveImage((BufferedImage) decodingArray[0], fileName, (String) decodingArray[1], adaptUrl);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveWithPath(String base64Img, String fileName, String category, String path) {
        String adaptUrl = ROOT_FOLDER + category + "/" + path;
        if(!checkFileDirectory(adaptUrl)){
            log.info("디렉토리가 존재하지 않아 생성합니다. " + adaptUrl);
            createFolder(adaptUrl);
        }
        try{
            Object[] decodingArray = decodingBase64(base64Img);
            Assert.notNull(decodingArray[0]);
            saveImage((BufferedImage) decodingArray[0], fileName, (String) decodingArray[1], adaptUrl);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveImage(BufferedImage image, String fileName, String type, String adaptUrl) throws IOException {
        Assert.state(!fileName.contains(".."), "File name cannot contains ..");
        String fileNameWithType = fileName + "." + type;
        Path targetFileNamePath = Paths.get(getWholeFileDirectory(adaptUrl)).resolve(fileNameWithType).normalize();
        Assert.state(!Files.exists(targetFileNamePath), fileNameWithType + "file already exist");
        ImageIO.write(image, type, new File(targetFileNamePath.toUri()));
    }

    private void deleteFiles(List<String> filePaths){
        filePaths.forEach(filePath -> deleteImage(filePath));
    }

    private void deleteImage(String filePath){
        try{
            Path path = Paths.get(filePath);
            Files.delete(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteDirectory(String deleteTargetDirectory) throws IOException{
        Path deleteTargetPath = Paths.get(deleteTargetDirectory);
        Files.delete(deleteTargetPath);
    }

    @Override
    public void delete(String category, String path) {
        String adaptUrl = ROOT_FOLDER + category + "/" + path;
        String deleteTargetDirectory = getWholeFileDirectory(adaptUrl);
        try{
            deleteDirectory(deleteTargetDirectory);
        }catch (DirectoryNotEmptyException e){
            log.info("디렉토리 안에 삭제하지 않은 이미지 파일이 존재하여 삭제합니다.");
            log.info("deleteTargetDirectory : {}", deleteTargetDirectory);
            List<String> filePaths = Arrays.stream(new File(deleteTargetDirectory).listFiles())
                    .map( file -> file.getPath())
                    .collect(Collectors.toList());
            deleteFiles(filePaths);
            delete(category, path);
        }catch (NoSuchFileException e){
            log.error("존재하지 않는 디렉토리, " + deleteTargetDirectory);
        }catch (IOException e){
            e.printStackTrace();
        }
        log.info("디렉토리가 정상적으로 삭제되었습니다. ", deleteTargetDirectory);
    }
}
