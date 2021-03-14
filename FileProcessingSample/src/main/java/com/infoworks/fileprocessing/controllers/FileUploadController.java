package com.infoworks.fileprocessing.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.fileprocessing.services.ExcelParsingService;
import com.infoworks.fileprocessing.services.LocalStorageService;
import com.infoworks.lab.rest.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    public ResponseEntity<Map<String, Object>> handleReadContent(
            @RequestParam("contentName") String contentName
            , @RequestParam(value = "sheetAt", required = false) int sheetAt
            , @RequestParam(value = "rowStartIdx", required = false) int rowStartIdx
            , @RequestParam(value = "rowEndIdx", required = false) int rowEndIdx) {
        //
        Response response = new Response().setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()).setMessage("");
        if (sheetAt < 0) sheetAt = 0;
        if (rowStartIdx < 0) rowStartIdx = 0;
        if (rowEndIdx <= 0) rowEndIdx = Integer.MAX_VALUE;
        //
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //
        Map<String, Object> result = new HashMap<>();
        InputStream inputStream = storageService.read(contentName);
        ObjectMapper mapper = new ObjectMapper();
        if(inputStream != null) {
            try {
                InputStream fileInputStream = inputStream;
                Map<Integer, List<String>> data = contentService.read(fileInputStream, sheetAt, rowStartIdx, rowEndIdx);
                List<String> rows = new ArrayList<>();
                data.forEach((key, value) -> {
                    try {
                        rows.add(mapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {}
                });
                result.put("rows", rows);
                response.setStatus(HttpStatus.OK.value());
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                response.setError(e.getMessage());
            }
            //removing file once consume:
            storageService.remove(contentName);
        }
        //
        stopWatch.stop();
        //Log method execution time
        long executionTime = stopWatch.getTotalTimeMillis();
        result.put("executionTimeInMillis", executionTime);
        LOG.info("Execution time of " + ":: " + executionTime + " ms");
        //
        if (response.getStatus() == HttpStatus.OK.value()){
            return ResponseEntity.ok(result);
        }else{
            result.put("error", contentName + " not found!");
            return ResponseEntity.unprocessableEntity().body(result);
        }
    }

}
