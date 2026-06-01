export interface Student {
  id: number;
  studentNo: string;
  name: string;
  className: string;
}

export interface StudentPageResponse {
  content: Student[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasPrevious: boolean;
  hasNext: boolean;
}

export interface WorkSubmission {
  id: number;
  studentId: number;
  studentName: string;
  title: string;
  fileName: string;
  workType: string;
  remark?: string;
  submittedAt: string;
}

export interface EvaluationResult {
  id: number;
  submissionId: number;
  aiScore?: number;
  aiIssues?: string;
  aiComment?: string;
  teacherScore?: number;
  teacherComment?: string;
  status: number;
}

export type EvalStatus = 0 | 1 | 2;

export interface EvalStatusInfo {
  text: string;
  icon: string;
  color: string;
  bg: string;
}
