import { useQuery } from "@tanstack/react-query";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, Domain, ExamAttempt, PracticeQuestion, PracticeSession } from "../../api/client";
import { useAuth } from "../../components/AuthProvider";
import { useState, type ReactNode } from "react";

function Shell({ title, children }: { title: string; children: ReactNode }) {
  const { user, logout } = useAuth();
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <div>
            <h1 className="text-xl font-semibold text-brand-900">{title}</h1>
            <p className="text-sm text-slate-500">{user?.email}</p>
          </div>
          <div className="flex gap-3 text-sm">
            <Link to="/">Dashboard</Link>
            <Link to="/history">Historie</Link>
            {user?.role === "ADMIN" ? <Link to="/admin">Admin</Link> : null}
            <button onClick={logout} type="button">Logout</button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">{children}</main>
    </div>
  );
}

export function DashboardPage() {
  const { accessToken } = useAuth();
  const navigate = useNavigate();
  const domains = useQuery({
    queryKey: ["domains"],
    queryFn: () => api<Domain[]>("/domains", {}, accessToken ?? undefined)
  });
  const unlock = useQuery({
    queryKey: ["unlock"],
    queryFn: () => api<{ unlocked: boolean; masteredQuestions: number; totalQuestions: number }>("/exams/unlock-status", {}, accessToken ?? undefined)
  });

  async function startExam() {
    const attempt = await api<ExamAttempt>("/exams", { method: "POST" }, accessToken ?? undefined);
    navigate(`/exam/${attempt.attemptId}`);
  }

  return (
    <Shell title="Pflege Lernen">
      <section className="mb-6 rounded-3xl bg-brand-900 p-6 text-white">
        <p>Beherrschte Fragen: {unlock.data?.masteredQuestions ?? 0} / {unlock.data?.totalQuestions ?? 0}</p>
        <button className="mt-4 rounded-xl bg-white px-4 py-2 text-brand-900 disabled:opacity-50" disabled={!unlock.data?.unlocked} onClick={startExam} type="button">
          Prüfung starten
        </button>
      </section>
      <section className="grid gap-4 md:grid-cols-2">
        {domains.data?.map((domain) => (
          <Link className="rounded-2xl bg-white p-5 shadow-sm" key={domain.id} to={`/practice/${domain.id}`}>
            <h2 className="text-lg font-semibold">{domain.name}</h2>
            <p className="mt-2 text-sm text-slate-500">{domain.masteredQuestions} / {domain.totalQuestions} beherrscht</p>
            <div className="mt-4 h-2 rounded-full bg-slate-200">
              <div className="h-2 rounded-full bg-brand-500" style={{ width: `${domain.progressPercent}%` }} />
            </div>
          </Link>
        ))}
      </section>
    </Shell>
  );
}

export function PracticePage() {
  const { domainId } = useParams();
  const { accessToken } = useAuth();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selected, setSelected] = useState<number[]>([]);
  const [feedback, setFeedback] = useState<{ correct: boolean; correctOptionIds: number[] } | null>(null);
  const session = useQuery({
    queryKey: ["practice", domainId],
    queryFn: () => api<PracticeSession>(`/practice/domains/${domainId}/session`, {}, accessToken ?? undefined)
  });
  const current = session.data?.questions[currentIndex];

  async function submit() {
    if (!current) return;
    const result = await api<{ correct: boolean; correctOptionIds: number[]; mastered: boolean }>(
      `/practice/questions/${current.id}/answer`,
      { method: "POST", body: JSON.stringify({ selectedOptionIds: selected }) },
      accessToken ?? undefined
    );
    setFeedback(result);
  }

  function nextQuestion() {
    setSelected([]);
    setFeedback(null);
    setCurrentIndex((index) => index + 1);
  }

  return (
    <Shell title={session.data?.domainName ?? "Praxis"}>
      {!current ? <p>Keine offenen Fragen in diesem Bereich.</p> : (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <p className="mb-2 text-sm text-slate-500">Frage {currentIndex + 1} / {session.data?.questions.length}</p>
          <h2 className="mb-6 text-xl font-semibold">{current.text}</h2>
          <div className="space-y-3">
            {current.options.map((option) => (
              <label className="flex items-center gap-3 rounded-xl border p-3" key={option.id}>
                <input
                  checked={selected.includes(option.id)}
                  onChange={(event) =>
                    setSelected((prev) => event.target.checked ? [...prev, option.id] : prev.filter((id) => id !== option.id))
                  }
                  type="checkbox"
                />
                <span>{option.text}</span>
              </label>
            ))}
          </div>
          {feedback ? (
            <div className="mt-6">
              <p className={feedback.correct ? "text-green-700" : "text-red-700"}>
                {feedback.correct ? "Richtig" : `Falsch. Richtige Antworten: ${current.options.filter((option) => feedback.correctOptionIds.includes(option.id)).map((option) => option.text).join(", ")}`}
              </p>
              <button className="mt-4 rounded-xl bg-brand-700 px-4 py-2 text-white" onClick={nextQuestion} type="button">Weiter</button>
            </div>
          ) : (
            <button className="mt-6 rounded-xl bg-brand-700 px-4 py-2 text-white" onClick={submit} type="button">Antwort prüfen</button>
          )}
        </div>
      )}
    </Shell>
  );
}

export function ExamPage() {
  const { attemptId } = useParams();
  const { accessToken } = useAuth();
  const [currentIndex, setCurrentIndex] = useState(0);
  const exam = useQuery({
    queryKey: ["exam", attemptId],
    queryFn: () => api<ExamAttempt>(`/exams/${attemptId}`, {}, accessToken ?? undefined)
  });
  const current = exam.data?.questions[currentIndex];

  async function toggle(optionId: number, checked: boolean) {
    if (!current || !exam.data) return;
    const updated = checked
      ? [...current.selectedOptionIds, optionId]
      : current.selectedOptionIds.filter((id) => id !== optionId);
    await api<ExamAttempt>(
      `/exams/${attemptId}/questions/${current.questionId}/answer`,
      { method: "POST", body: JSON.stringify({ selectedOptionIds: updated }) },
      accessToken ?? undefined
    );
    await exam.refetch();
  }

  async function submitExam() {
    await api<ExamAttempt>(`/exams/${attemptId}/submit`, { method: "POST" }, accessToken ?? undefined);
    await exam.refetch();
  }

  return (
    <Shell title="Prüfung">
      {exam.data?.submitted ? (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-2xl font-semibold">{exam.data.passed ? "Bestanden" : "Nicht bestanden"}</h2>
          <p className="mt-2">Punkte: {exam.data.score} / {exam.data.totalQuestions}</p>
          <p className="mt-2">Falsche Fragen kehren in den Lernmodus zurück.</p>
        </div>
      ) : current ? (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <p className="mb-2 text-sm text-slate-500">Frage {currentIndex + 1} / {exam.data?.questions.length}</p>
          <h2 className="mb-6 text-xl font-semibold">{current.text}</h2>
          <div className="space-y-3">
            {current.options.map((option) => (
              <label className="flex items-center gap-3 rounded-xl border p-3" key={option.id}>
                <input checked={current.selectedOptionIds.includes(option.id)} onChange={(e) => void toggle(option.id, e.target.checked)} type="checkbox" />
                <span>{option.text}</span>
              </label>
            ))}
          </div>
          <div className="mt-6 flex gap-3">
            <button className="rounded-xl border px-4 py-2" disabled={currentIndex === 0} onClick={() => setCurrentIndex((x) => x - 1)} type="button">Zurück</button>
            <button className="rounded-xl bg-brand-700 px-4 py-2 text-white" disabled={currentIndex === (exam.data?.questions.length ?? 1) - 1} onClick={() => setCurrentIndex((x) => x + 1)} type="button">Weiter</button>
            <button className="rounded-xl bg-slate-900 px-4 py-2 text-white" onClick={submitExam} type="button">Prüfung abgeben</button>
          </div>
        </div>
      ) : <p>Lade Prüfung...</p>}
    </Shell>
  );
}

export function HistoryPage() {
  const { accessToken } = useAuth();
  const history = useQuery({
    queryKey: ["history"],
    queryFn: () => api<Array<{ attemptId: number; score: number; totalQuestions: number; passed: boolean; startedAt: string }>>("/exams/history", {}, accessToken ?? undefined)
  });

  return (
    <Shell title="Prüfungshistorie">
      <div className="space-y-3">
        {history.data?.map((item) => (
          <div className="rounded-2xl bg-white p-4 shadow-sm" key={item.attemptId}>
            <p className="font-medium">Versuch #{item.attemptId}</p>
            <p>{item.score} / {item.totalQuestions} · {item.passed ? "Bestanden" : "Nicht bestanden"}</p>
          </div>
        ))}
      </div>
    </Shell>
  );
}
