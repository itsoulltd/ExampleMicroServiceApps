package com.infoworks.fileprocessing;

import com.it.soul.lab.data.simple.SimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class Config {

    @Bean("inMemFileStore")
    public SimpleDataSource<String, MultipartFile> getFileStore(){
        return new SimpleDataSource();
    }

}
