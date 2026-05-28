package com.teachingeval.service;

import org.springframework.stereotype.Service;

import com.teachingeval.dto.ExportResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExportService {

    public ExportResponse exportExcel() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "成绩汇总_" + timestamp + ".xlsx";
        String fileUrl = "/files/export/" + fileName;
        return new ExportResponse(fileUrl);
    }
}
