const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";

export type AuthUser = {
  id: number;
  email: string;
  role: "USER" | "ADMIN";
};

export type TokenBundle = {
  accessToken: string;
  refreshToken: string;
  user: AuthUser;
};

export type Domain = {
  id: number;
  name: string;
  slug: string;
  totalQuestions: number;
  masteredQuestions: number;
  progressPercent: number;
};

export type PracticeQuestion = {
  id: number;
  text: string;
  difficulty: string;
  options: { id: number; displayOrder: number; text: string; correct: boolean }[];
};

export type PracticeSession = {
  domainId: number;
  domainName: string;
  totalQuestions: number;
  questions: PracticeQuestion[];
};

export type ExamQuestion = {
  examQuestionId: number;
  questionId: number;
  text: string;
  difficulty: string;
  options: { id: number; displayOrder: number; text: string; correct: boolean }[];
  selectedOptionIds: number[];
};

export type ExamAttempt = {
  attemptId: number;
  submitted: boolean;
  score: number;
  totalQuestions: number;
  passed: boolean;
  startedAt: string;
  completedAt?: string;
  questions: ExamQuestion[];
};

export async function api<T>(path: string, init: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers ?? {})
    }
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "Request failed" }));
    throw new Error(error.message ?? "Request failed");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}
