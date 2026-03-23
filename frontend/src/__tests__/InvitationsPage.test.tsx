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
const mockAccept = vi.fn();
const mockRefuse = vi.fn();
const mockFetchWs = vi.fn();

vi.mock('../api/invitations', () => ({
  fetchInvitations: (...args: any[]) => mockFetch(...args),
  createInvitation: (...args: any[]) => mockCreate(...args),
  acceptInvitation: (...args: any[]) => mockAccept(...args),
  refuseInvitation: (...args: any[]) => mockRefuse(...args)
}));
vi.mock('../api/workspaces', () => ({
  fetchWorkspaces: (...args: any[]) => mockFetchWs(...args)
}));

import InvitationsPage from '../pages/InvitationsPage';

describe('InvitationsPage', () => {
  it('lists invitations', async () => {
    mockFetchWs.mockResolvedValueOnce([{ id: 'ws1', name: 'WS1', ownerId: 'u', createdAt: '', updatedAt: '' }]);
    mockFetch.mockResolvedValueOnce([
      { id: '1', workspaceId: 'ws1', inviterId: 'u', inviteeEmail: 'a@b.com', status: 'PENDING', createdAt: '', updatedAt: '' }
    ]); // sent
    mockFetch.mockResolvedValueOnce([
      { id: '2', workspaceId: 'ws2', inviterId: 'x', inviteeEmail: 'me@host.com', status: 'PENDING', createdAt: '', updatedAt: '' }
    ]); // received
    render(
      <MemoryRouter>
        <InvitationsPage />
      </MemoryRouter>
    );
    await waitFor(() => expect(screen.getByText('a@b.com')).toBeInTheDocument());
    expect(screen.getByText('me@host.com')).toBeInTheDocument();
  });

  it('creates invitation', async () => {
    mockFetchWs.mockResolvedValueOnce([{ id: 'ws2', name: 'WS2', ownerId: 'u', createdAt: '', updatedAt: '' }]);
    mockFetch.mockResolvedValueOnce([]); // sent list
    mockFetch.mockResolvedValueOnce([]); // received list
    mockCreate.mockResolvedValueOnce({ id: '2', workspaceId: 'ws2', inviterId: 'u', inviteeEmail: 'x@y.com', status: 'PENDING', createdAt: '', updatedAt: '' });
    render(
      <MemoryRouter>
        <InvitationsPage />
      </MemoryRouter>
    );
    await waitFor(() => expect(screen.getByLabelText(/workspace/i)).toHaveValue('ws2'));
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'x@y.com' } });
    fireEvent.click(screen.getByText(/Send invitation/i));
    await waitFor(() => expect(mockCreate).toHaveBeenCalled());
  });
});
