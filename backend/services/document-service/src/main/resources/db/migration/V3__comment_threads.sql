CREATE TABLE comment_threads (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    line_start INTEGER NOT NULL,
    line_end INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by_id UUID NOT NULL,
    created_by_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE comment_messages (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL REFERENCES comment_threads(id) ON DELETE CASCADE,
    author_id UUID NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_comment_threads_document ON comment_threads(document_id);
CREATE INDEX idx_comment_threads_status ON comment_threads(status);
