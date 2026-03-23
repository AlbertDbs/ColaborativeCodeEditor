const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function handle(res: Response) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.json();
}

export type Workspace = {
  id: string;
  name: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
};

export async function fetchWorkspaces(token: string): Promise<Workspace[]> {
  const res = await fetch(`${API_BASE}/workspaces`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}

export async function fetchWorkspace(token: string | null, id: string): Promise<Workspace> {
  const headers: Record<string, string> = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const res = await fetch(`${API_BASE}/workspaces/${id}`, {
    headers
  });
  return handle(res);
}

export async function createWorkspace(token: string, name: string): Promise<Workspace> {
  const res = await fetch(`${API_BASE}/workspaces`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ name })
  });
  return handle(res);
}
