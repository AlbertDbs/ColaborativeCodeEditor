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
import { runCode, ExecutionResult } from '../api/executions';
import { fetchInvitations } from '../api/invitations';
import { fetchWorkspace, fetchWorkspaces, Workspace } from '../api/workspaces';
import AppHeader from '../components/AppHeader';
import { useAuth } from '../state/AuthProvider';
import type { editor } from 'monaco-editor';

const LANGUAGE_OPTIONS = [
  { value: 'c', label: 'C', extension: '.c', editorLanguage: 'c' },
  { value: 'cpp', label: 'C++', extension: '.cpp', editorLanguage: 'cpp' },
  { value: 'java', label: 'Java', extension: '.java', editorLanguage: 'java' },
  { value: 'python', label: 'Python 3', extension: '.py', editorLanguage: 'python' },
  { value: 'kotlin', label: 'Kotlin', extension: '.kt', editorLanguage: 'kotlin' },
  { value: 'php', label: 'PHP', extension: '.php', editorLanguage: 'php' },
  { value: 'csharp-mono', label: 'C# (mono)', extension: '.cs', editorLanguage: 'csharp' },
  { value: 'csharp-dotnet', label: 'C# (dotnet)', extension: '.cs', editorLanguage: 'csharp' },
  { value: 'ocaml', label: 'OCaml', extension: '.ml', editorLanguage: 'ocaml' },
  { value: 'vb', label: 'VB', extension: '.vb', editorLanguage: 'vb' },
  { value: 'ruby', label: 'Ruby', extension: '.rb', editorLanguage: 'ruby' },
  { value: 'perl', label: 'Perl', extension: '.pl', editorLanguage: 'perl' },
  { value: 'cobol', label: 'Cobol', extension: '.cob', editorLanguage: 'cobol' },
  { value: 'r', label: 'R', extension: '.r', editorLanguage: 'r' },
  { value: 'fortran', label: 'Fortran', extension: '.f90', editorLanguage: 'plaintext' },
  { value: 'haskell', label: 'Haskell', extension: '.hs', editorLanguage: 'haskell' },
  { value: 'assembly-gcc', label: 'Assembly (GCC)', extension: '.s', editorLanguage: 'plaintext' },
  { value: 'assembly-nasm', label: 'Assembly (NASM)', extension: '.asm', editorLanguage: 'plaintext' },
  { value: 'objective-c', label: 'Objective C', extension: '.m', editorLanguage: 'plaintext' },
  { value: 'sqlite', label: 'SQLite', extension: '.sql', editorLanguage: 'sql' },
  { value: 'dart', label: 'Dart', extension: '.dart', editorLanguage: 'dart' },
  { value: 'groovy', label: 'Groovy', extension: '.groovy', editorLanguage: 'groovy' },
  { value: 'typescript', label: 'TypeScript', extension: '.ts', editorLanguage: 'typescript' },
  { value: 'javascript', label: 'Javascript', extension: '.js', editorLanguage: 'javascript' },
  { value: 'prolog', label: 'Prolog', extension: '.pro', editorLanguage: 'plaintext' },
  { value: 'swift', label: 'Swift', extension: '.swift', editorLanguage: 'swift' },
  { value: 'rust', label: 'Rust', extension: '.rs', editorLanguage: 'rust' },
  { value: 'go', label: 'Go', extension: '.go', editorLanguage: 'go' },
  { value: 'bash', label: 'Bash', extension: '.sh', editorLanguage: 'shell' }
] as const;

const KNOWN_EXTENSIONS = [
  '.c', '.cpp', '.java', '.py', '.kt', '.php', '.cs', '.ml', '.vb', '.rb', '.pl', '.cob',
  '.r', '.f90', '.f', '.for', '.hs', '.s', '.asm', '.m', '.sql', '.dart', '.groovy',
  '.ts', '.tsx', '.js', '.jsx', '.pro', '.swift', '.rs', '.go', '.sh'
];

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
    case 'kt':
      return 'kotlin';
    case 'java':
      return 'java';
    case 'cpp':
      return 'cpp';
    case 'c':
      return 'c';
    case 'php':
      return 'php';
    case 'cs':
      return 'csharp-dotnet';
    case 'ml':
      return 'ocaml';
    case 'vb':
      return 'vb';
    case 'rb':
      return 'ruby';
    case 'pl':
      return 'perl';
    case 'cob':
      return 'cobol';
    case 'r':
      return 'r';
    case 'f':
    case 'for':
    case 'f90':
      return 'fortran';
    case 'hs':
      return 'haskell';
    case 's':
      return 'assembly-gcc';
    case 'asm':
      return 'assembly-nasm';
    case 'm':
      return 'objective-c';
    case 'sql':
      return 'sqlite';
    case 'dart':
      return 'dart';
    case 'groovy':
    case 'gvy':
      return 'groovy';
    case 'go':
      return 'go';
    case 'rs':
      return 'rust';
    case 'pro':
      return 'prolog';
    case 'swift':
      return 'swift';
    case 'sh':
    case 'bash':
      return 'bash';
    default:
      return 'javascript';
  }
};

const languageLabel = (lang: string): string => {
  return LANGUAGE_OPTIONS.find((option) => option.value === lang)?.label || 'Javascript';
};

const editorLanguageForSelection = (lang: string): string => {
  return LANGUAGE_OPTIONS.find((option) => option.value === lang)?.editorLanguage || 'javascript';
};

const extensionForLanguage = (lang: string): string => {
  return LANGUAGE_OPTIONS.find((option) => option.value === lang)?.extension || '.js';
};

const hasKnownExtension = (name: string): boolean => {
  const lowered = name.trim().toLowerCase();
  return KNOWN_EXTENSIONS.some((ext) => lowered.endsWith(ext));
};

const buildFilenameForLanguage = (name: string, lang: string): string => {
  const trimmed = name.trim();
  if (!trimmed) return '';
  return hasKnownExtension(trimmed) ? trimmed : `${trimmed}${extensionForLanguage(lang)}`;
};

const syncFilenameToLanguage = (name: string, lang: string): string => {
  const trimmed = name.trim();
  if (!trimmed) return '';
  const lowered = trimmed.toLowerCase();
  const matchedExtension = KNOWN_EXTENSIONS.find((ext) => lowered.endsWith(ext));
  if (!matchedExtension) return `${trimmed}${extensionForLanguage(lang)}`;
  return `${trimmed.slice(0, trimmed.length - matchedExtension.length)}${extensionForLanguage(lang)}`;
};

type SaveStatus = 'idle' | 'unsaved' | 'saving' | 'saved' | 'error';

type EditorState = {
  id: string;
  name: string;
  version: number;
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
  const [currentLanguage, setCurrentLanguage] = useState<string>('python');
  const [threads, setThreads] = useState<CommentThread[]>([]);
  const [showResolved, setShowResolved] = useState(false);
  const [replyDrafts, setReplyDrafts] = useState<Record<string, string>>({});
  const [runStdin, setRunStdin] = useState('');
  const [runResult, setRunResult] = useState<ExecutionResult | null>(null);
  const [runLoading, setRunLoading] = useState(false);
  const liveSource = useRef<EventSource | null>(null);
  const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);
  const outputPanelRef = useRef<HTMLDivElement | null>(null);

  const allWorkspaces = useMemo(() => {
    const map = new Map<string, Workspace>();
    [...workspaces, ...invitedWorkspaces].forEach((w) => map.set(w.id, w));
    return Array.from(map.values());
  }, [workspaces, invitedWorkspaces]);

  const currentWorkspaceName = allWorkspaces.find((w) => w.id === workspaceId)?.name || '–';

  const selectedDoc = useMemo(() => files.find((f) => f.id === selected?.id), [files, selected]);
  const lastEditor = selectedDoc?.updatedByEmail || selectedDoc?.updatedById || '—';
  const newFilePreview = useMemo(() => buildFilenameForLanguage(newFileName, currentLanguage), [newFileName, currentLanguage]);

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
          } catch (e) {
            // ignore missing
          }
        }
        setInvitedWorkspaces(acceptedWs);

        const requested = searchParams.get('workspaceId');
        const first = requested || ws[0]?.id || acceptedWs[0]?.id || '';
        if (first) {
          setWorkspaceId(first);
        }
    } catch (e: any) {
        setError(e.message || 'Could not load workspaces');
      }
    };
    loadWorkspaces();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    const loadFiles = async () => {
      if (!token || !workspaceId) return;
      try {
        const list = await listDocuments(token, workspaceId);
        setFiles(list);
        if (list.length > 0) {
          const first = list[0];
          selectFile(first);
        } else {
          setSelected(null);
          setContent('');
        }
        if (list.length > 0) {
          await loadComments(list[0].id);
        } else {
          setThreads([]);
        }
      } catch (e: any) {
        setError(e.message || 'Could not load files');
      }
    };
    loadFiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [workspaceId, token]);

  useEffect(() => {
    if (selected?.id) {
      loadComments(selected.id);
    } else {
      setThreads([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.id, showResolved]);

  useEffect(() => {
    if (!runLoading && runResult) {
      outputPanelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [runLoading, runResult]);

  const selectFile = (doc: Document) => {
    const inferredLang = languageFromFilename(doc.title);
    setSelected({
      id: doc.id,
      name: doc.title,
      version: doc.version
    });
    setContent(doc.content);
    setCurrentLanguage(inferredLang);
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

    if (typeof EventSource === 'undefined') {
      // environment (tests) without EventSource support
      return;
    }

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
        setSelected((s) =>
          s && s.id === fresh.id
            ? {
                ...s,
                name: fresh.title,
                version: fresh.version
              }
            : s
        );
        setSaveStatus('saved');
      } catch (e) {
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, selected?.id]);

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
        version: updated.version
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
      const finalName = buildFilenameForLanguage(newFileName, currentLanguage);
      const created = await createDocument(token, workspaceId, finalName, '');
      setFiles((prev) => [created, ...prev]);
      selectFile(created);
      setNewFileName('');
      setNewFileOpen(false);
    } catch (e: any) {
      setError(e.message || 'Could not create file');
    }
  };

  const handleLanguageChange = (nextLanguage: string) => {
    setCurrentLanguage(nextLanguage);

    if (!selected) return;

    const nextName = syncFilenameToLanguage(selected.name, nextLanguage);
    if (nextName === selected.name) return;

    setFiles((prev) =>
      prev.map((file) => (file.id === selected.id ? { ...file, title: nextName } : file))
    );
    setSelected((prev) =>
      prev && prev.id === selected.id
        ? {
            ...prev,
            name: nextName
          }
        : prev
    );
    setSaveStatus('unsaved');
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
          version: updated.version
        });
        setCurrentLanguage(languageFromFilename(updated.title));
      }
    } catch (e: any) {
      setError(e.message || 'Could not rename file');
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
        prev.map((t) =>
          t.id === threadId ? { ...t, messages: [...t.messages, msg] } : t
        )
      );
      setReplyDrafts((d) => ({ ...d, [threadId]: '' }));
    } catch (e: any) {
      setError(e.message || 'Could not add reply');
    }
  };

  const handleResolve = async (threadId: string, reopen = false) => {
    if (!token) return;
    try {
      if (reopen) {
        await reopenThread(token, threadId);
      } else {
        await resolveThread(token, threadId);
      }
      await loadComments(selected!.id);
    } catch (e: any) {
      setError(e.message || 'Could not change thread status');
    }
  };

  const handleRun = async () => {
    if (!token || !selected?.id || !content.trim() || !currentLanguage) return;
    outputPanelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    setRunLoading(true);
    setRunResult(null);
    try {
      const result = await runCode(token, currentLanguage, content, runStdin);
      setRunResult(result);
    } catch (e: any) {
      setRunResult({
        status: 'INTERNAL_ERROR',
        stdout: '',
        stderr: e.message || 'Execution failed',
        compileOutput: '',
        executionTime: '',
        memory: '',
        language: currentLanguage
      });
    } finally {
      setRunLoading(false);
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
        if (next) selectFile(next);
        else {
          setSelected(null);
          setContent('');
          setSaveStatus('idle');
        }
      }
    } catch (e: any) {
      setError(e.message || 'Could not delete file');
    }
  };

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
            <button className="btn-accent" onClick={() => setNewFileOpen((v) => !v)} disabled={!workspaceId}>
              {newFileOpen ? 'Close' : '+ New file'}
            </button>
          </div>

          {newFileOpen && (
            <form className="new-file-box" onSubmit={handleNewFile}>
              <div className="new-file-row">
                <input
                  aria-label="file name"
                  placeholder="File name"
                  value={newFileName}
                  onChange={(e) => setNewFileName(e.target.value)}
                />
                <button className="btn" type="submit" disabled={!newFileName.trim()}>Create</button>
              </div>
              <div className="new-file-helper">
                <span>Extension is added automatically from the selected language.</span>
                <span className="new-file-extension">{extensionForLanguage(currentLanguage)}</span>
              </div>
              {newFilePreview && (
                <div className="new-file-preview">
                  Will create <strong>{newFilePreview}</strong>
                </div>
              )}
            </form>
          )}

          <div className="file-list">
            <div className="file-list-header">
              <div className="file-list-title">Files</div>
              <span className="file-count">{files.length}</span>
            </div>
            {files.length === 0 ? (
              <p className="muted">No files yet.</p>
            ) : (
              <ul>
                {files.map((f) => (
                  <li
                    key={f.id}
                    className={`file-item ${selected?.id === f.id ? 'selected' : ''}`}
                  >
                    <button type="button" className="file-item-main" onClick={() => selectFile(f)}>
                      <span className="dot" />
                      <span className="file-name">{f.title}</span>
                    </button>
                    <div className="file-actions">
                      <button type="button" className="ghost-btn file-action-btn" onClick={() => handleRename(f)}>Rename</button>
                      <button type="button" className="ghost-btn file-action-btn" onClick={() => handleDelete(f.id)}>Delete</button>
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
              <div className="editor-title">
                <span className="file-name">{selected?.name || 'No file selected'}</span>
                <span className="muted tiny">
                  {selected ? `Current language: ${languageLabel(currentLanguage)}` : 'Pick a language, then create or open a file.'}
                </span>
              </div>
              {selected && <span className="pill">Last: {lastEditor}</span>}
              {selected && <span className={statusClass}>{statusLabel}</span>}
            </div>
            <div className="editor-actions">
              <label className="toolbar-language">
                <span className="muted tiny">Language</span>
                <select
                  aria-label="language"
                  className="select"
                  value={currentLanguage}
                  onChange={(e) => handleLanguageChange(e.target.value)}
                >
                  {LANGUAGE_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
              </label>
              <button
                className="btn"
                onClick={handleRun}
                disabled={!selected || runLoading || !content.trim() || !currentLanguage}
              >
                {runLoading ? 'Running…' : 'Run'}
              </button>
              <button className="btn" onClick={handleSave} disabled={!selected || saving || saveStatus === 'saved'}>
                {saving ? 'Saving…' : 'Save'}
              </button>
              {selected && (
                <button className="ghost-btn" onClick={handleAddComment} disabled={!currentSelectionRange()}>
                  Comment
                </button>
              )}
              <Link className="button-link" to="/workspaces">Back</Link>
            </div>
          </div>

          <div className="editor-main">
            <div className="editor-body">
              {selected ? (
                <Editor
                  height="70vh"
                  theme="vs-dark"
                  language={editorLanguageForSelection(currentLanguage)}
                  value={content}
                  onChange={(v) => {
                    setContent(v ?? '');
                    setSaveStatus('unsaved');
                  }}
                  onMount={(editor) => {
                    editorRef.current = editor;
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
                    .filter((t) => showResolved || t.status === 'ACTIVE')
                    .map((t) => (
                      <div key={t.id} className={`comment-thread ${t.status === 'RESOLVED' ? 'resolved' : ''}`}>
                        <div className="comment-thread-meta">
                          <span className="badge">Lines {t.lineStart}-{t.lineEnd}</span>
                          <span className="muted">by {t.createdByEmail}</span>
                          <span className={`status ${t.status}`}>{t.status}</span>
                        </div>
                        <div className="comment-messages">
                          {t.messages.map((m) => (
                            <div key={m.id} className="comment-message">
                              <div className="muted">{m.authorEmail}</div>
                              <div>{m.body}</div>
                              <div className="muted tiny">{new Date(m.createdAt).toLocaleString()}</div>
                            </div>
                          ))}
                        </div>
                        <div className="comment-actions">
                          {t.status === 'ACTIVE' ? (
                            <button className="ghost-btn" onClick={() => handleResolve(t.id)}>Resolve</button>
                          ) : (
                            <button className="ghost-btn" onClick={() => handleResolve(t.id, true)}>Reopen</button>
                          )}
                        </div>
                        <div className="comment-reply">
                          <input
                            placeholder="Reply..."
                            value={replyDrafts[t.id] || ''}
                            onChange={(e) => setReplyDrafts((d) => ({ ...d, [t.id]: e.target.value }))}
                          />
                          <button className="btn" onClick={() => handleReply(t.id)}>Send</button>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </div>
          </div>

          <div className="output-panel" ref={outputPanelRef}>
            <div className="panel-header">
              <div>
                <h4>Output</h4>
                <p className="muted">Execution result and program input</p>
              </div>
              {runResult && <span className={`status ${runResult.status === 'SUCCESS' ? 'ACTIVE' : 'RESOLVED'}`}>{runResult.status}</span>}
            </div>
            <div className="output-section">
              <div className="output-label">Program input (stdin)</div>
              <textarea
                className="stdin-input"
                placeholder={'Example:\n5 7\nhello'}
                value={runStdin}
                onChange={(e) => setRunStdin(e.target.value)}
                rows={4}
              />
            </div>
            {runLoading && <p className="muted">Running...</p>}
            {!runLoading && !runResult && <p className="muted">Click Run to execute the current file.</p>}
            {!runLoading && runResult && (
              <div className={`output-body ${runResult.status === 'SUCCESS' ? 'ok' : 'error'}`}>
                {runResult.executionTime && (
                  <div className="muted tiny">Time: {runResult.executionTime}s | Memory: {runResult.memory}</div>
                )}
                {runResult.stdout && (
                  <div className="output-section">
                    <div className="output-label">Program output</div>
                    <pre className="output-ok">{runResult.stdout}</pre>
                  </div>
                )}
                {runResult.compileOutput && (
                  <div className="output-section">
                    <div className="output-label">Compile output</div>
                    <pre className="output-error">{runResult.compileOutput}</pre>
                  </div>
                )}
                {runResult.stderr && (
                  <div className="output-section">
                    <div className="output-label">Errors</div>
                    <pre className="output-error">{runResult.stderr}</pre>
                  </div>
                )}
                {!runResult.stdout && !runResult.stderr && !runResult.compileOutput && (
                  <div className="output-section">
                    <div className="output-label">Program output</div>
                    <pre className="output-ok">Program finished with no output.</pre>
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="status-bar">
            <span>{currentWorkspaceName}</span>
            <span>{languageLabel(currentLanguage)}</span>
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
