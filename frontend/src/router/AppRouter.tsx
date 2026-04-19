import type { ReactElement } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "../components/AuthProvider";
import { LoginPage, RegisterPage } from "../features/auth/AuthPages";
import { DashboardPage, PracticePage, ExamPage, HistoryPage } from "../features/learner/LearnerPages";
import { AdminPage } from "../features/admin/AdminPage";

function Protected({ children }: { children: ReactElement }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

function AdminOnly({ children }: { children: ReactElement }) {
  const { user } = useAuth();
  return user?.role === "ADMIN" ? children : <Navigate to="/" replace />;
}

export function AppRouter() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<Protected><DashboardPage /></Protected>} />
        <Route path="/practice/:domainId" element={<Protected><PracticePage /></Protected>} />
        <Route path="/exam/:attemptId" element={<Protected><ExamPage /></Protected>} />
        <Route path="/history" element={<Protected><HistoryPage /></Protected>} />
        <Route path="/admin" element={<Protected><AdminOnly><AdminPage /></AdminOnly></Protected>} />
      </Routes>
    </AuthProvider>
  );
}
