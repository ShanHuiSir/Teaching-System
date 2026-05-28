package com.teachingeval.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teachingeval.dto.SubmissionRequest;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
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

    public WorkSubmission createSubmission(SubmissionRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        WorkSubmission submission = new WorkSubmission();
        submission.setStudentId(request.getStudentId());
        submission.setStudentName(student.getName());
        submission.setTitle(request.getTitle());
        submission.setFileName(request.getFileName());
        submission.setWorkType(request.getWorkType());
        submission.setRemark(request.getRemark());
        return submissionRepository.save(submission);
    }
}
