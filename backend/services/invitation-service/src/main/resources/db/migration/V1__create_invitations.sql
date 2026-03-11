CREATE TABLE IF NOT EXISTS invitations (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    workspace_owner_id UUID NOT NULL,
    inviter_id UUID NOT NULL,
    invitee_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    accepted_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_invitations_invitee_email ON invitations(invitee_email);
CREATE INDEX IF NOT EXISTS idx_invitations_inviter ON invitations(inviter_id);
