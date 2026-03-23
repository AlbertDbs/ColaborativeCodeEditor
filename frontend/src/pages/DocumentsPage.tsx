import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import {
  API_BASE,
  createDocument,
  deleteDocument,
  Document,
  getDocument,
  listDocuments,
  updateDocument
} from '../api/documents';
import {
  addReply,
  CommentThread,
  createThread,
  listThreads,
  reopenThread,
  resolveThread
} from '../api/comments';
import { fetchInvitations } from '../api/invitations';
import { fetchWorkspace, fetchWorkspaces, Workspace } from '../api/workspaces';
import AppHeader from '../components/AppHeader';
import { useAuth } from '../state/AuthProvider';
import type { editor } from 'monaco-editor';

const LANGUAGE_OPTIONS = [
  { value: 'plaintext', label: 'Text' },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'php', label: 'PHP' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
  { value: 'html', label: 'HTML' },
  { value: 'css', label: 'CSS' },
  { value: 'json', label: 'JSON' }
] as const;

const languageFromFilename = (name: string): string => {
  const ext = name.split('.').pop()?.toLowerCase();
  switch (ext) {
    case 'txt':
      return 'plaintext';
    case 'ts':
    case 'tsx':
      return 'typescript';
    case 'js':
    case 'jsx':
      return 'javascript';
    case 'py':
      return 'python';
    case 'java':
      return 'java';
    case 'cpp':
      return 'cpp';
    case 'c':
      return 'c';
    case 'php':
      return 'php';
    case 'go':
      return 'go';
    case 'rs':
      return 'rust';
    case 'json':
      return 'json';
    case 'css':
      return 'css';
    case 'html':
      return 'html';
    default:
      return 'plaintext';
  }
};

const extensionForLanguage = (lang: string): string => {
  switch (lang) {
    case 'javascript':
      return '.js';
    case 'typescript':
      return '.ts';
    case 'python':
      return '.py';
    case 'java':
      return '.java';
    case 'cpp':
      return '.cpp';
    case 'c':
      return '.c';
    case 'php':
      return '.php';
    case 'go':
      return '.go';
    case 'rust':
      return '.rs';
    case 'json':
      return '.json';
    case 'css':
      return '.css';
    case 'html':
      return '.html';
    default:
      return '.txt';
  }
};

type SaveStatus = 'idle' | 'unsaved' | 'saving' | 'saved' | 'error';

type EditorState = {
  id: string;
  name: string;
  version: number;
  language: string;
};

const DocumentsPage = () => {
  const { token } = useAuth();
  const [searchParams] = useSearchParams();

  const [workspaceId, setWorkspaceId] = useState('');
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [invitedWorkspaces, setInvitedWorkspaces] = useState<Workspace[]>([]);
  const [files, setFiles] = useState<Document[]>([]);
  const [selected, setSelected] = useState<EditorState | null>(null);
  const [content, setContent] = useState('');
  const [saveStatus, setSaveStatus] = useState<SaveStatus>('idle');
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [newFileOpen, setNewFileOpen] = useState(false);
  const [newFileName, setNewFileName] = useState('');
  const [newFileLanguage, setNewFileLanguage] = useState<string>('javascript');
  const [languageOverride, setLanguageOverride] = useState<string | null>(null);
  const [threads, setThreads] = useState<CommentThread[]>([]);
  const [showResolved, setShowResolved] = useState(false);
  const [replyDrafts, setReplyDrafts] = useState<Record<string, string>>({});
  const liveSource = useRef<EventSource | null>(null);
  const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

  const allWorkspaces = useMemo(() => {
    const map = new Map<string, Workspace>();
    [...workspaces, ...invitedWorkspaces].forEach((w) => map.set(w.id, w));
    return Array.from(map.values());
  }, [workspaces, invitedWorkspaces]);

  const currentWorkspaceName = allWorkspaces.find((w) => w.id === workspaceId)?.name || '–';
  const selectedDoc = useMemo(() => files.find((f) => f.id === selected?.id), [files, selected]);
  const lastEditor = selectedDoc?.updatedByEmail || selectedDoc?.updatedById || '—';

  useEffect(() => {
    const loadWorkspaces = async () => {
      if (!token) return;
      try {
        const [ws, inv] = await Promise.all([
          fetchWorkspaces(token),
          fetchInvitations(token, 'received')
        ]);

        setWorkspaces(ws);

        const acceptedIds = Array.from(new Set(inv.filter((i) => i.status === 'ACCEPTED').map((i) => i.workspaceId)));
        const acceptedWs: Workspace[] = [];
        for (const id of acceptedIds) {
          try {
            const w = await fetchWorkspace(token, id);
            acceptedWs.push(w);
          } catch {
            // ignore missing workspaces
          }
        }
        setInvitedWorkspaces(acceptedWs);

        const requested = searchParams.get('workspaceId');
        const first = requested || ws[0]?.id || acceptedWs[0]?.id || '';
        if (first) setWorkspaceId(first);
      } catch (e: any) {
        setError(e.message || 'Could not load workspaces');
      }
    };

    loadWorkspaces();
  }, [searchParams, token]);

  useEffect(() => {
    const loadFiles = async () => {
      if (!token || !workspaceId) return;
      try {
        const list = await listDocuments(token, workspaceId);
        setFiles(list);
        if (list.length > 0) {
          selectFile(list[0]);
          await loadComments(list[0].id);
        } else {
          setSelected(null);
          setContent('');
          setThreads([]);
        }
      } catch (e: any) {
        setError(e.message || 'Could not load files');
      }
    };

    loadFiles();
  }, [workspaceId, token]);

  useEffect(() => {
    if (selected?.id) {
      loadComments(selected.id);
    } else {
      setThreads([]);
    }
  }, [selected?.id, showResolved]);

  const selectFile = (doc: Document) => {
    setSelected({
      id: doc.id,
      name: doc.title,
      version: doc.version,
      language: languageFromFilename(doc.title)
    });
    setContent(doc.content);
    setLanguageOverride(null);
    setSaveStatus('saved');
  };

  useEffect(() => {
    if (!token || !selected?.id) {
      if (liveSource.current) {
        liveSource.current.close();
        liveSource.current = null;
      }
      return;
    }

    if (typeof EventSource === 'undefined') return;

    if (liveSource.current) {
      liveSource.current.close();
    }

    const es = new EventSource(`${API_BASE}/documents/${selected.id}/stream?token=${token}`);
    liveSource.current = es;

    const refresh = async () => {
      try {
        const fresh = await getDocument(token, selected.id);
        setFiles((prev) => {
          const exists = prev.some((d) => d.id === fresh.id);
          const mapped = prev.map((d) => (d.id === fresh.id ? fresh : d));
          return exists ? mapped : [fresh, ...prev];
        });
        setContent(fresh.content);
        setSelected((state) =>
          state && state.id === fresh.id
            ? {
                ...state,
                name: fresh.title,
                version: fresh.version,
                language: languageOverride || languageFromFilename(fresh.title)
              }
            : state
        );
        setSaveStatus('saved');
      } catch {
        // ignore transient refresh errors
      }
    };

    es.addEventListener('changed', refresh);
    es.addEventListener('deleted', (event) => {
      let docId: string | null = null;
      try {
        docId = JSON.parse((event as MessageEvent).data);
      } catch {
        docId = (event as MessageEvent).data as string;
      }
      if (!docId) return;

      setFiles((prev) => {
        const filtered = prev.filter((d) => d.id !== docId);
        if (selected?.id === docId) {
          const next = filtered[0];
          if (next) {
            selectFile(next);
          } else {
            setSelected(null);
            setContent('');
            setSaveStatus('idle');
          }
        }
        return filtered;
      });
    });
    es.addEventListener('comments', async () => {
      await loadComments(selected.id);
    });
    es.onerror = () => {
      es.close();
      liveSource.current = null;
    };

    return () => {
      es.close();
      liveSource.current = null;
    };
  }, [languageOverride, selected?.id, token]);

  const handleSave = async () => {
    if (!token || !selectedDoc || !selected) return;
    try {
      setSaving(true);
      setSaveStatus('saving');
      const updated = await updateDocument(token, selectedDoc.id, selectedDoc.title, content);
      setFiles((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
      setSelected({
        id: updated.id,
        name: updated.title,
        version: updated.version,
        language: languageOverride || languageFromFilename(updated.title)
      });
      setSaveStatus('saved');
    } catch (e: any) {
      setError(e.message || 'Save failed');
      setSaveStatus('error');
    } finally {
      setSaving(false);
    }
  };

  const handleNewFile = async (e?: React.FormEvent<HTMLFormElement>) => {
    e?.preventDefault();
    if (!token || !workspaceId || !newFileName.trim()) return;
    try {
      const base = newFileName.trim();
      const finalName = base.includes('.') ? base : `${base}${extensionForLanguage(newFileLanguage)}`;
      const created = await createDocument(token, workspaceId, finalName, '');
      setFiles((prev) => [created, ...prev]);
      selectFile(created);
      setLanguageOverride(newFileLanguage);
      setNewFileName('');
      setNewFileLanguage('javascript');
      setNewFileOpen(false);
    } catch (e: any) {
      setError(e.message || 'Could not create file');
    }
  };

  const handleRename = async (file: Document) => {
    if (!token) return;
    const newName = window.prompt('File name', file.title)?.trim();
    if (!newName || newName === file.title) return;
    try {
      const updated = await updateDocument(token, file.id, newName, file.content);
      setFiles((prev) => prev.map((f) => (f.id === file.id ? updated : f)));
      if (selected?.id === file.id) {
        setSelected({
          id: updated.id,
          name: updated.title,
          version: updated.version,
          language: languageOverride || languageFromFilename(updated.title)
        });
      }
    } catch (e: any) {
      setError(e.message || 'Could not rename file');
    }
  };

  const handleDelete = async (id: string) => {
    if (!token) return;
    const confirmed = window.confirm('Delete this file?');
    if (!confirmed) return;
    try {
      await deleteDocument(token, id);
      setFiles((prev) => prev.filter((f) => f.id !== id));
      if (selected?.id === id) {
        const next = files.find((f) => f.id !== id);
        if (next) {
          selectFile(next);
        } else {
          setSelected(null);
          setContent('');
          setSaveStatus('idle');
        }
      }
    } catch (e: any) {
      setError(e.message || 'Could not delete file');
    }
  };

  const loadComments = async (documentId: string, resolvedFlag?: boolean) => {
    if (!token) return;
    try {
      const includeResolved = resolvedFlag ?? showResolved;
      const status = includeResolved ? 'ALL' : 'ACTIVE';
      const data = await listThreads(token, documentId, status as any);
      setThreads(data);
    } catch (e: any) {
      setError(e.message || 'Could not load comments');
    }
  };

  const currentSelectionRange = () => {
    const sel = editorRef.current?.getSelection();
    if (!sel) return null;
    return {
      lineStart: Math.min(sel.startLineNumber, sel.endLineNumber),
      lineEnd: Math.max(sel.startLineNumber, sel.endLineNumber)
    };
  };

  const handleAddComment = async () => {
    if (!token || !selected?.id) return;
    const range = currentSelectionRange();
    if (!range) {
      setError('Select at least one line to comment.');
      return;
    }
    const body = window.prompt(`Comment for lines ${range.lineStart}-${range.lineEnd}`, '');
    if (!body) return;
    try {
      const newThread = await createThread(token, selected.id, range.lineStart, range.lineEnd, body);
      setThreads((prev) => [newThread, ...prev]);
    } catch (e: any) {
      setError(e.message || 'Could not create comment');
    }
  };

  const handleReply = async (threadId: string) => {
    if (!token) return;
    const body = replyDrafts[threadId];
    if (!body?.trim()) return;
    try {
      const msg = await addReply(token, threadId, body.trim());
      setThreads((prev) =>
        prev.map((thread) =>
          thread.id === threadId ? { ...thread, messages: [...thread.messages, msg] } : thread
        )
      );
      setReplyDrafts((drafts) => ({ ...drafts, [threadId]: '' }));
    } catch (e: any) {
      setError(e.message || 'Could not add reply');
    }
  };

  const handleResolve = async (threadId: string, reopen = false) => {
    if (!token || !selected?.id) return;
    try {
      if (reopen) {
        await reopenThread(token, threadId);
      } else {
        await resolveThread(token, threadId);
      }
      await loadComments(selected.id);
    } catch (e: any) {
      setError(e.message || 'Could not change thread status');
    }
  };

  const statusLabel = {
    idle: 'Saved',
    saved: 'Saved',
    unsaved: 'Unsaved changes',
    saving: 'Saving...',
    error: 'Save failed'
  }[saveStatus];

  const statusClass =
    saveStatus === 'saved' || saveStatus === 'idle'
      ? 'status-pill saved'
      : saveStatus === 'unsaved'
        ? 'status-pill unsaved'
        : saveStatus === 'error'
          ? 'status-pill error'
          : 'status-pill';

  return (
    <>
      <AppHeader />
      <div className="editor-shell">
        <aside className="sidebar">
          <div className="sidebar-header">
            <div className="sidebar-workspace">
              <div className="muted tiny">Workspace</div>
              <select
                aria-label="workspace"
                className="select block"
                value={workspaceId}
                onChange={(e) => setWorkspaceId(e.target.value)}
              >
                <option value="" disabled>Select workspace</option>
                {allWorkspaces.map((ws) => (
                  <option key={ws.id} value={ws.id}>{ws.name}</option>
                ))}
              </select>
            </div>
            <button className="btn-accent" onClick={() => setNewFileOpen((value) => !value)} disabled={!workspaceId}>
              + New file
            </button>
          </div>

          {newFileOpen && (
            <form className="new-file-box" onSubmit={handleNewFile}>
              <input
                aria-label="file name"
                placeholder="ex: main.ts"
                value={newFileName}
                onChange={(e) => setNewFileName(e.target.value)}
              />
              <select
                aria-label="file language"
                className="select"
                value={newFileLanguage}
                onChange={(e) => setNewFileLanguage(e.target.value)}
              >
                {LANGUAGE_OPTIONS.map((lang) => (
                  <option key={lang.value} value={lang.value}>{lang.label}</option>
                ))}
              </select>
              <button className="btn" type="submit" disabled={!newFileName.trim()}>Create</button>
            </form>
          )}

          <div className="file-list">
            <div className="file-list-title">Files</div>
            {files.length === 0 ? (
              <p className="muted">No files yet.</p>
            ) : (
              <ul>
                {files.map((file) => (
                  <li key={file.id} className={`file-item ${selected?.id === file.id ? 'selected' : ''}`}>
                    <button type="button" className="file-item-main" onClick={() => selectFile(file)}>
                      <span className="dot" />
                      <span className="file-name">{file.title}</span>
                    </button>
                    <div className="file-actions">
                      <button type="button" className="ghost-btn file-action-btn" onClick={() => handleRename(file)}>Rename</button>
                      <button type="button" className="danger-btn file-action-btn" onClick={() => handleDelete(file.id)}>Delete</button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </aside>

        <main className="editor-panel">
          <div className="editor-topbar">
            <div className="editor-meta">
              <span className="file-name">{selected?.name || 'No file selected'}</span>
              {selected && (
                <select
                  aria-label="language"
                  className="select"
                  value={languageOverride || selected.language}
                  onChange={(e) => setLanguageOverride(e.target.value)}
                >
                  {LANGUAGE_OPTIONS.map((lang) => (
                    <option key={lang.value} value={lang.value}>{lang.label}</option>
                  ))}
                </select>
              )}
              {selected && <span className="pill">v{selected.version}</span>}
              {selected && <span className="pill">Last: {lastEditor}</span>}
              {selected && <span className={statusClass}>{statusLabel}</span>}
            </div>
            <div className="editor-actions">
              {selected && (
                <button className="ghost-btn" onClick={handleAddComment} disabled={!currentSelectionRange()}>
                  Add comment for selection
                </button>
              )}
              <Link className="button-link" to="/workspaces">Back</Link>
              <button className="btn" onClick={handleSave} disabled={!selected || saving || saveStatus === 'saved'}>
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </div>

          <div className="editor-main">
            <div className="editor-body">
              {selected ? (
                <Editor
                  height="70vh"
                  theme="vs-dark"
                  language={languageOverride || selected.language}
                  value={content}
                  onChange={(value) => {
                    setContent(value ?? '');
                    setSaveStatus('unsaved');
                  }}
                  onMount={(instance) => {
                    editorRef.current = instance;
                  }}
                  options={{ minimap: { enabled: false }, fontSize: 14 }}
                />
              ) : (
                <div className="empty-editor">Select or create a file to start editing.</div>
              )}
            </div>

            <div className="comments-panel">
              <div className="comments-header">
                <div>
                  <h4>Comments</h4>
                  <p className="muted">Line-specific threads in this file.</p>
                </div>
                <div className="comments-actions">
                  <label className="muted" style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    <input
                      type="checkbox"
                      checked={showResolved}
                      onChange={async (e) => {
                        const flag = e.target.checked;
                        setShowResolved(flag);
                        if (selected?.id) await loadComments(selected.id, flag);
                      }}
                    />
                    Show resolved
                  </label>
                </div>
              </div>

              {threads.length === 0 ? (
                <p className="muted">No comments {showResolved ? '' : 'yet'}.</p>
              ) : (
                <div className="comment-thread-list">
                  {threads
                    .filter((thread) => showResolved || thread.status === 'ACTIVE')
                    .map((thread) => (
                      <div key={thread.id} className={`comment-thread ${thread.status === 'RESOLVED' ? 'resolved' : ''}`}>
                        <div className="comment-thread-meta">
                          <span className="badge">Lines {thread.lineStart}-{thread.lineEnd}</span>
                          <span className="muted">by {thread.createdByEmail}</span>
                          <span className={`status ${thread.status}`}>{thread.status}</span>
                        </div>
                        <div className="comment-messages">
                          {thread.messages.map((message) => (
                            <div key={message.id} className="comment-message">
                              <div className="muted">{message.authorEmail}</div>
                              <div>{message.body}</div>
                              <div className="muted tiny">{new Date(message.createdAt).toLocaleString()}</div>
                            </div>
                          ))}
                        </div>
                        <div className="comment-actions">
                          {thread.status === 'ACTIVE' ? (
                            <button className="ghost-btn" onClick={() => handleResolve(thread.id)}>Resolve</button>
                          ) : (
                            <button className="ghost-btn" onClick={() => handleResolve(thread.id, true)}>Reopen</button>
                          )}
                        </div>
                        <div className="comment-reply">
                          <input
                            placeholder="Reply..."
                            value={replyDrafts[thread.id] || ''}
                            onChange={(e) => setReplyDrafts((drafts) => ({ ...drafts, [thread.id]: e.target.value }))}
                          />
                          <button className="btn" onClick={() => handleReply(thread.id)}>Send</button>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </div>
          </div>

          <div className="status-bar">
            <span>{currentWorkspaceName}</span>
            <span>{languageOverride || selected?.language || '–'}</span>
            <span>{selected ? `v${selected.version}` : '–'}</span>
            <span className={statusClass}>{statusLabel}</span>
            <span>Last: {lastEditor}</span>
          </div>

          {error && <p className="error" style={{ marginTop: '8px' }}>{error}</p>}
        </main>
      </div>
    </>
  );
};

export default DocumentsPage;
