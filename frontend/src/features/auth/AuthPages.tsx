import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { z } from "zod";
import { api, TokenBundle } from "../../api/client";
import { fetchGoogleUrl, useAuth } from "../../components/AuthProvider";

const authSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});

type AuthForm = z.infer<typeof authSchema>;

function AuthLayout({ title, onSubmit, alternatePath, alternateLabel }: {
  title: string;
  onSubmit: (values: AuthForm) => Promise<void>;
  alternatePath: string;
  alternateLabel: string;
}) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<AuthForm>({
    resolver: zodResolver(authSchema)
  });

  const isLogin = title === "Anmelden";
  const subtitle = isLogin
    ? "Melde dich mit deinem Konto an."
    : "Erstelle ein Schuelerkonto, um mit den Lernbereichen zu starten.";

  return (
    <div className="min-h-screen bg-gradient-to-b from-brand-50 to-white px-4 py-10">
      <div className="mx-auto max-w-md rounded-3xl bg-white p-8 shadow-xl">
        <h1 className="text-3xl font-semibold text-brand-900">{title}</h1>
        <p className="mb-6 mt-2 text-sm text-slate-600">{subtitle}</p>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <input autoComplete="email" className="w-full rounded-xl border p-3" placeholder="E-Mail" {...register("email")} />
            {errors.email ? <p className="mt-1 text-sm text-red-600">{errors.email.message}</p> : null}
          </div>
          <div>
            <input autoComplete={isLogin ? "current-password" : "new-password"} className="w-full rounded-xl border p-3" type="password" placeholder="Passwort" {...register("password")} />
            {errors.password ? <p className="mt-1 text-sm text-red-600">{errors.password.message}</p> : null}
          </div>
          <button className="w-full rounded-xl bg-brand-700 px-4 py-3 text-white" disabled={isSubmitting} type="submit">
            {isSubmitting ? "Bitte warten..." : title}
          </button>
        </form>
        <div className="mt-4 flex items-center justify-between text-sm">
          <Link className="text-brand-700" to={alternatePath}>{alternateLabel}</Link>
          <GoogleLoginLink />
        </div>
      </div>
    </div>
  );
}

function GoogleLoginLink() {
  async function handleClick() {
    const response = await fetchGoogleUrl();
    window.location.href = response.url;
  }

  return <button className="text-brand-700" onClick={handleClick} type="button">Google Login</button>;
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(values: AuthForm) {
    const response = await api<TokenBundle>("/auth/login", {
      method: "POST",
      body: JSON.stringify(values)
    });
    login(response);
    navigate("/");
  }

  return <AuthLayout title="Anmelden" onSubmit={onSubmit} alternatePath="/register" alternateLabel="Konto erstellen" />;
}

export function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(values: AuthForm) {
    const response = await api<TokenBundle>("/auth/register", {
      method: "POST",
      body: JSON.stringify(values)
    });
    login(response);
    navigate("/");
  }

  return <AuthLayout title="Schuelerkonto erstellen" onSubmit={onSubmit} alternatePath="/login" alternateLabel="Bereits registriert?" />;
}
