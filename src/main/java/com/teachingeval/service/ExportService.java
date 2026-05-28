package com.teachingeval.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.teachingeval.dto.ExportRow;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

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
