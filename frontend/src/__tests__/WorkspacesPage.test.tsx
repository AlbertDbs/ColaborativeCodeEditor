import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../state/AuthProvider', () => ({
  useAuth: () => ({
    token: 'test-token',
    logout: vi.fn()
  })
}));

const mockFetch = vi.fn();
const mockCreate = vi.fn();

vi.mock('../api/workspaces', () => ({
  fetchWorkspaces: (...args: any[]) => mockFetch(...args),
  createWorkspace: (...args: any[]) => mockCreate(...args)
}));

import WorkspacesPage from '../pages/WorkspacesPage';

describe('WorkspacesPage', () => {
  it('loads and displays workspaces', async () => {
    mockFetch.mockResolvedValueOnce([{ id: '1', name: 'WS1', ownerId: 'u1', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }]);
    render(
      <MemoryRouter>
        <WorkspacesPage />
      </MemoryRouter>
    );
    await waitFor(() => expect(screen.getByText('WS1')).toBeInTheDocument());
  });

  it('creates workspace on submit', async () => {
    mockFetch.mockResolvedValueOnce([]);
    mockCreate.mockResolvedValueOnce({ id: '2', name: 'New WS', ownerId: 'u1', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() });
    render(
      <MemoryRouter>
        <WorkspacesPage />
      </MemoryRouter>
    );
    fireEvent.change(screen.getByLabelText(/workspace name/i), { target: { value: 'New WS' } });
    fireEvent.click(screen.getByRole('button', { name: /Create/i }));
    await waitFor(() => expect(screen.getByText('New WS')).toBeInTheDocument());
  });
});
