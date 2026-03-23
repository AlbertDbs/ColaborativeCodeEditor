import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { loginRequest, registerRequest, googleLoginRequest } from '../api/auth';

type AuthContextValue = {
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: (idToken: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = 'collab_jwt';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));

  useEffect(() => {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  }, [token]);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      login: async (email, password) => {
        const t = await loginRequest(email, password);
        setToken(t);
      },
      loginWithGoogle: async (idToken: string) => {
        const t = await googleLoginRequest(idToken);
        setToken(t);
      },
      register: async (email, password) => {
        await registerRequest(email, password);
        const t = await loginRequest(email, password); // auto-login după register
        setToken(t);
      },
      logout: () => setToken(null)
    }),
    [token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
