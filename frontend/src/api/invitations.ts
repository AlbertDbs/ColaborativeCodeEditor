const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function handle(res: Response) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.json();
}

export type Invitation = {
  id: string;
  workspaceId: string;
  inviterId: string;
  inviteeEmail: string;
  status: 'PENDING' | 'ACCEPTED' | 'REFUSED';
  createdAt: string;
  updatedAt: string;
};

export async function fetchInvitations(token: string, scope: 'sent' | 'received' = 'sent'): Promise<Invitation[]> {
  const res = await fetch(`${API_BASE}/invitations?scope=${scope}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}

export async function createInvitation(token: string, workspaceId: string, inviteeEmail: string): Promise<Invitation> {
  const res = await fetch(`${API_BASE}/invitations`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ workspaceId, inviteeEmail })
  });
  return handle(res);
}

export async function acceptInvitation(token: string, id: string): Promise<Invitation> {
  const res = await fetch(`${API_BASE}/invitations/${id}/accept`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}

export async function refuseInvitation(token: string, id: string): Promise<Invitation> {
  const res = await fetch(`${API_BASE}/invitations/${id}/refuse`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` }
  });
  return handle(res);
}
