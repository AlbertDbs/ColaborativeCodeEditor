import { authFetch } from './utils';

export type CommentMessage = {
  id: string;
  authorId: string;
  authorEmail: string;
  body: string;
  createdAt: string;
};

export type CommentThread = {
  id: string;
  documentId: string;
  workspaceId: string;
  lineStart: number;
  lineEnd: number;
  status: 'ACTIVE' | 'RESOLVED';
  createdById: string;
  createdByEmail: string;
  createdAt: string;
  updatedAt: string;
  messages: CommentMessage[];
};

export async function listThreads(token: string, documentId: string, status: 'ACTIVE' | 'RESOLVED' | 'ALL' = 'ACTIVE'): Promise<CommentThread[]> {
  const url = `/documents/${documentId}/comments/threads?status=${status}`;
  return authFetch<CommentThread[]>(token, url);
}

export async function createThread(token: string, documentId: string, lineStart: number, lineEnd: number, body: string): Promise<CommentThread> {
  return authFetch<CommentThread>(token, `/documents/${documentId}/comments/threads`, {
    method: 'POST',
    body: JSON.stringify({ lineStart, lineEnd, body })
  });
}

export async function addReply(token: string, threadId: string, body: string): Promise<CommentMessage> {
  return authFetch<CommentMessage>(token, `/comments/threads/${threadId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ body })
  });
}

export async function resolveThread(token: string, threadId: string): Promise<void> {
  await authFetch<void>(token, `/comments/threads/${threadId}/resolve`, { method: 'POST' });
}

export async function reopenThread(token: string, threadId: string): Promise<void> {
  await authFetch<void>(token, `/comments/threads/${threadId}/reopen`, { method: 'POST' });
}
