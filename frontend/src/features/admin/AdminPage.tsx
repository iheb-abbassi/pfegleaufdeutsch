import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { api, Domain } from "../../api/client";
import { useAuth } from "../../components/AuthProvider";

const domainSchema = z.object({
  name: z.string().min(1),
  slug: z.string().min(1)
});

const questionSchema = z.object({
  domainId: z.coerce.number().int().positive("Bitte einen Bereich waehlen"),
  text: z.string().min(1, "Bitte eine Frage eingeben"),
  difficulty: z.enum(["EASY", "MEDIUM", "HARD"]),
  option1: z.string().min(1, "Option 1 darf nicht leer sein"),
  option2: z.string().min(1, "Option 2 darf nicht leer sein"),
  option3: z.string().min(1, "Option 3 darf nicht leer sein"),
  correct1: z.boolean(),
  correct2: z.boolean(),
  correct3: z.boolean()
}).superRefine((values, context) => {
  const correctCount = [values.correct1, values.correct2, values.correct3].filter(Boolean).length;
  if (correctCount < 1) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      message: "Mindestens eine richtige Antwort auswaehlen",
      path: ["correct1"]
    });
  }
});

type DomainForm = z.infer<typeof domainSchema>;
type QuestionForm = z.infer<typeof questionSchema>;

export function AdminPage() {
  const { accessToken } = useAuth();
  const queryClient = useQueryClient();
  const domains = useQuery({
    queryKey: ["admin-domains"],
    queryFn: () => api<Domain[]>("/admin/domains", {}, accessToken ?? undefined)
  });
  const questions = useQuery({
    queryKey: ["admin-questions"],
    queryFn: () => api<Array<{ id: number; text: string; difficulty: string; domainName: string }>>("/admin/questions", {}, accessToken ?? undefined)
  });

  const domainForm = useForm<DomainForm>({ resolver: zodResolver(domainSchema) });
  const questionForm = useForm<QuestionForm>({
    resolver: zodResolver(questionSchema),
    defaultValues: { difficulty: "EASY", correct1: false, correct2: false, correct3: false }
  });

  async function submitDomain(values: DomainForm) {
    await api("/admin/domains", { method: "POST", body: JSON.stringify(values) }, accessToken ?? undefined);
    domainForm.reset();
    await domains.refetch();
  }

  async function submitQuestion(values: QuestionForm) {
    try {
      await api("/admin/questions", {
        method: "POST",
        body: JSON.stringify({
          domainId: values.domainId,
          text: values.text,
          difficulty: values.difficulty,
          options: [
            { text: values.option1, correct: values.correct1 },
            { text: values.option2, correct: values.correct2 },
            { text: values.option3, correct: values.correct3 }
          ]
        })
      }, accessToken ?? undefined);
      questionForm.reset({ difficulty: "EASY", correct1: false, correct2: false, correct3: false });
      await questions.refetch();
      await queryClient.invalidateQueries({ queryKey: ["domains"] });
      await queryClient.invalidateQueries({ queryKey: ["unlock"] });
      await queryClient.invalidateQueries({ queryKey: ["practice"] });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Frage konnte nicht gespeichert werden";
      questionForm.setError("root.server", { type: "server", message });
    }
  }

  const optionFields = [
    { option: "option1" as const, correct: "correct1" as const, label: "Option 1" },
    { option: "option2" as const, correct: "correct2" as const, label: "Option 2" },
    { option: "option3" as const, correct: "correct3" as const, label: "Option 3" }
  ];

  const { errors: questionErrors, isSubmitting: isQuestionSubmitting } = questionForm.formState;

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-8">
      <div className="mx-auto max-w-6xl space-y-6">
        <h1 className="text-3xl font-semibold text-brand-900">Admin</h1>
        <div className="grid gap-6 lg:grid-cols-2">
          <form className="rounded-3xl bg-white p-6 shadow-sm" onSubmit={domainForm.handleSubmit(submitDomain)}>
            <h2 className="mb-4 text-xl font-semibold">Bereich anlegen</h2>
            <input className="mb-3 w-full rounded-xl border p-3" placeholder="Name" {...domainForm.register("name")} />
            <input className="mb-3 w-full rounded-xl border p-3" placeholder="Slug" {...domainForm.register("slug")} />
            <button className="rounded-xl bg-brand-700 px-4 py-2 text-white" type="submit">Speichern</button>
          </form>
          <form className="rounded-3xl bg-white p-6 shadow-sm" onSubmit={questionForm.handleSubmit(submitQuestion)}>
            <h2 className="mb-4 text-xl font-semibold">Frage anlegen</h2>
            <select className="mb-3 w-full rounded-xl border p-3" {...questionForm.register("domainId")}>
              <option value="">Bereich waehlen</option>
              {domains.data?.map((domain) => <option key={domain.id} value={domain.id}>{domain.name}</option>)}
            </select>
            {questionErrors.domainId ? <p className="mb-3 text-sm text-red-600">{questionErrors.domainId.message}</p> : null}
            <textarea className="mb-3 w-full rounded-xl border p-3" placeholder="Frage" {...questionForm.register("text")} />
            {questionErrors.text ? <p className="mb-3 text-sm text-red-600">{questionErrors.text.message}</p> : null}
            <select className="mb-3 w-full rounded-xl border p-3" {...questionForm.register("difficulty")}>
              <option value="EASY">1 Star Easy</option>
              <option value="MEDIUM">2 Star Medium</option>
              <option value="HARD">3 Star Hard</option>
            </select>
            {optionFields.map((field) => (
              <label className="mb-2 flex items-center gap-3" key={field.option}>
                <input type="checkbox" {...questionForm.register(field.correct)} />
                <input className="w-full rounded-xl border p-3" placeholder={field.label} {...questionForm.register(field.option)} />
              </label>
            ))}
            {questionErrors.option1 ? <p className="mb-2 text-sm text-red-600">{questionErrors.option1.message}</p> : null}
            {questionErrors.option2 ? <p className="mb-2 text-sm text-red-600">{questionErrors.option2.message}</p> : null}
            {questionErrors.option3 ? <p className="mb-2 text-sm text-red-600">{questionErrors.option3.message}</p> : null}
            {questionErrors.correct1 ? <p className="mb-2 text-sm text-red-600">{questionErrors.correct1.message}</p> : null}
            {questionErrors.root?.server ? <p className="mb-2 text-sm text-red-600">{questionErrors.root.server.message}</p> : null}
            <button className="mt-3 rounded-xl bg-brand-700 px-4 py-2 text-white" disabled={isQuestionSubmitting} type="submit">
              {isQuestionSubmitting ? "Bitte warten..." : "Frage speichern"}
            </button>
          </form>
        </div>
        <section className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-xl font-semibold">Fragen</h2>
          <div className="space-y-3">
            {questions.data?.map((question) => (
              <div className="rounded-2xl border p-4" key={question.id}>
                <p className="font-medium">{question.text}</p>
                <p className="text-sm text-slate-500">{question.domainName} - {question.difficulty}</p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
