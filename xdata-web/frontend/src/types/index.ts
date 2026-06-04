export interface User {
  id: string;
  username: string;
  loginId: string;
  email?: string;
  role: string;
  courseIds?: string[];
  courseId?: string;
  enabled?: boolean;
}

export interface Course {
  id: number;
  courseName: string;
  instructorCourseId: string;
  year?: number;
  semester?: string;
  description?: string;
}

export interface Assignment {
  id: number;
  assignmentId?: number;
  name: string;
  deadline: string;
  courseId: string;
  defaultSchemaId?: number;
  penaltyPercentage?: number;
  publishedDate?: string;
  lateSubmissionAllowed?: boolean;
  totalQuestions?: number;
  totalMarks?: number;
  achievedMarks?: number;
  percentage?: number;
}

export interface Question {
  id: number;
  name: string;
  instructorQuery: string;
  marks: number;
  assignmentId: number;
  assignment?: Assignment;
  partialMarkInfo?: string;
  partialMarkParameters?: PartialMarkParameters;
}

export interface Submission {
  submissionId: number;
  studentId: string;
  questionId: number;
  query: string;
  marks: number;
  details: string;
  submissionTime: string;
  evaluated: boolean;
  markInfoJson?: string;
  instructorFeedback?: string;
}

export interface Announcement {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  course: Course;
  createdBy: User;
}

export interface SystemStatus {
  memoryUsed: number;
  memoryMax: number;
  osName: string;
  availableProcessors: number;
  systemLoad: number;
  diskTotal: number;
  diskFree: number;
  z3Available: boolean;
  uptime: number;
}

export interface PartialMarkParameters {
  predicate: number;
  projection: number;
  joins: number;
  groupBy: number;
  havingClause: number;
  orderBy: number;
  aggregates: number;
  relation?: number;
  whereSubQueries?: number;
  fromSubQueries?: number;
  outerQuery?: number;
  subQConnective?: number;
  setOperators?: number;
  distinct?: number;
  maxPartialMarks?: number;
}

export interface QueryInfo {
  level: number;
  studentRelations: string[];
  instructorRelations: string[];
  studentRelationMarks: number[];
  
  studentProjections: string[];
  instructorProjections: string[];
  studentProjectionMarks: number[];
  
  studentPredicates: string[];
  instructorPredicates: string[];
  studentPredicateMarks: number[];
  
  studentJoins: string[];
  instructorJoins: string[];
  studentJoinMarks: number[];
  
  studentGroupBy: string[];
  instructorGroupBy: string[];
  studentGroupByMarks: number[];
  
  studentOrderBy: string[];
  instructorOrderBy: string[];
  studentOrderByMarks: number[];
  
  studentHaving?: string;
  instructorHaving?: string;
  studentHavingMark: number;
  
  studentDistinct: boolean;
  instructorDistinct: boolean;
  studentDistinctMark: number;
}

export interface MarkInfo {
  marks: number;
  maxMarks: number;
  percentage: number;
  subqueryData: QueryInfo[];
}
