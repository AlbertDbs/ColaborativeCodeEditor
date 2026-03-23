const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

type LoginResponse = { accessToken: string };

async function handleResponse(res: Response) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  if (res.status === 204) return null;
  return res.json();
}

export async function registerRequest(email: string, password: string): Promise<void> {
  await handleResponse(
    await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
  );
}

export async function loginRequest(email: string, password: string): Promise<string> {
  const data = (await handleResponse(
    await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
  )) as LoginResponse;
  return data.accessToken;
}

export async function googleLoginRequest(idToken: string): Promise<string> {
  const data = (await handleResponse(
    await fetch(`${API_BASE}/auth/login/google`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken })
    })
  )) as LoginResponse;
  return data.accessToken;
}
