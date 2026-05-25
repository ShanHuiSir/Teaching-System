package com.teachingeval.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teachingeval.model.Student;
import com.teachingeval.model.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             StudentRepository studentRepository) {
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
    }

    public List<WorkSubmission> listSubmissions() {
        return submissionRepository.findAll();
    }

    public WorkSubmission createSubmission(WorkSubmission submission) {
        Student student = studentRepository.findById(submission.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        if (isBlank(submission.getTitle())) {
            throw new IllegalArgumentException("作品标题不能为空");
        }
        if (isBlank(submission.getFileName())) {
            throw new IllegalArgumentException("作品文件名不能为空");
        }
        if (isBlank(submission.getWorkType())) {
            throw new IllegalArgumentException("作品类型不能为空");
        }

        submission.setId(null);
        submission.setStudentName(student.getName());
        return submissionRepository.save(submission);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
