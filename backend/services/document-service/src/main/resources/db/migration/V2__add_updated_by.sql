ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS updated_by_id UUID,
    ADD COLUMN IF NOT EXISTS updated_by_email VARCHAR(255);

UPDATE documents
SET updated_by_id = owner_id,
    updated_by_email = NULL
WHERE updated_by_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_documents_updated_by ON documents(updated_by_id);
