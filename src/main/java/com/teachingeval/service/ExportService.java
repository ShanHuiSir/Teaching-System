package com.teachingeval.service;

import org.springframework.stereotype.Service;

import com.teachingeval.dto.ExportResponse;

@Service
public class ExportService {

    public ExportResponse exportExcel() {
        return new ExportResponse("/files/export/成绩汇总_示例.xlsx");
    }
}
