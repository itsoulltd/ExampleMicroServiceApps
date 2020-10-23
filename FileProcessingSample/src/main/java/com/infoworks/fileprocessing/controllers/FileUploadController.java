package com.infoworks.fileprocessing.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.fileprocessing.services.ExcelParsingService;
import com.infoworks.fileprocessing.services.LocalStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static Logger LOG = LoggerFactory.getLogger("FileUploadController");
    private LocalStorageService storageService;
    private ExcelParsingService contentService;

    @Autowired
    public FileUploadController(@Qualifier("local") LocalStorageService storageService
                            , ExcelParsingService contentService) {
        this.storageService = storageService;
        this.contentService = contentService;
    }

    @GetMapping("/rowCount")
    public ResponseEntity<String> getCounts(){
        return ResponseEntity.ok(String.format("{\"count\":\"%s\"}", storageService.size()));
    }

    @GetMapping
    public ResponseEntity<List<String>> getFileNames(){
        List<String> names = Arrays.asList(storageService.readKeys());
        return ResponseEntity.ok(names);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleMultipartContent(
            @RequestParam("content") MultipartFile content,
            RedirectAttributes redirectAttributes) throws IOException {
        //
        storageService.put(content.getOriginalFilename(), content.getInputStream());
        //storageService.save(false);
        return ResponseEntity.ok("Content Received: " + content.getOriginalFilename());
    }

    @PostMapping("/read")
    public ResponseEntity<List<String>> handleReadContent(
            @RequestParam("contentName") String contentName) {
        //
        List<String> result = new ArrayList<>();
        InputStream inputStream = storageService.read(contentName);
        ObjectMapper mapper = new ObjectMapper();
        if(inputStream != null) {
            try {
                InputStream fileInputStream = inputStream;//storageService.copy(file);
                Map<Integer, List<String>> data = contentService.read(fileInputStream, 0);
                data.forEach((key, value) -> {
                    try {
                        result.add(mapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {}
                });
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                return ResponseEntity.badRequest().body(Arrays.asList(e.getMessage()));
            }
            //removing file once consume:
            storageService.remove(contentName);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.unprocessableEntity().body(Arrays.asList(contentName + " not found!"));
    }

}
