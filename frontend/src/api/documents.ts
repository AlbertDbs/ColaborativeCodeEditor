export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function handle(res: Response) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.json();
}

export type Document = {
  id: string;
  workspaceId: string;
  ownerId: string;
  title: string;
  content: string;
  version: number;
  updatedById?: string;
  updatedByEmail?: string;
  createdAt: string;
  updatedAt: string;
};

export async function listDocuments(token: string, workspaceId: string): Promise<Document[]> {
  const res = await fetch(`${API_BASE}/documents?workspaceId=${workspaceId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}

export async function createDocument(token: string, workspaceId: string, title: string, content: string): Promise<Document> {
  const res = await fetch(`${API_BASE}/documents`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ workspaceId, title, content })
  });
  return handle(res);
}

export async function deleteDocument(token: string, id: string): Promise<void> {
  const res = await fetch(`${API_BASE}/documents/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
}

export async function updateDocument(token: string, id: string, title: string, content: string): Promise<Document> {
  const res = await fetch(`${API_BASE}/documents/${id}`, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ title, content })
  });
  return handle(res);
}

export async function getDocument(token: string, id: string): Promise<Document> {
  const res = await fetch(`${API_BASE}/documents/${id}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}
