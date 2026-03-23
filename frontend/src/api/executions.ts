import { authFetch } from './utils';

export type ExecutionResult = {
  status: string;
  stdout: string;
  stderr: string;
  compileOutput: string;
  executionTime: string;
  memory: string;
  language: string;
};

export async function runCode(token: string, language: string, sourceCode: string, stdin?: string): Promise<ExecutionResult> {
  return authFetch<ExecutionResult>(token, '/executions', {
    method: 'POST',
    body: JSON.stringify({ language, sourceCode, stdin })
  });
}
