package com.infoworks.fileprocessing.services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service("S3")
public class S3StorageService extends LocalStorageService {

    @Override
    protected boolean saveFile(InputStream file, String name) throws IOException {
        throw new IOException(name + " not found!");
    }

    @Override
    public boolean retrieve() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
