import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, AuthUser, TokenBundle } from "../api/client";

type AuthContextValue = {
  user: AuthUser | null;
  accessToken: string | null;
  login: (bundle: TokenBundle) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);
const STORAGE_KEY = "pflege-auth";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);

  useEffect(() => {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const parsed = JSON.parse(raw) as TokenBundle;
    setUser(parsed.user);
    setAccessToken(parsed.accessToken);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      accessToken,
      login: (bundle) => {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(bundle));
        setUser(bundle.user);
        setAccessToken(bundle.accessToken);
      },
      logout: () => {
        window.localStorage.removeItem(STORAGE_KEY);
        setUser(null);
        setAccessToken(null);
      }
    }),
    [accessToken, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("Auth context missing");
  }
  return context;
}

export async function fetchGoogleUrl() {
  return api<{ url: string }>("/auth/google-url");
}
