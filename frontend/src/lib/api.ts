// バックエンドの application.yaml と同じ「環境変数 or 既定値」方式。
// ローカルは既定値、AWS では NEXT_PUBLIC_API_BASE_URL で本番URLに差し替える
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

// Spring Boot 側が RFC 9457（ProblemDetail）形式で返すエラー本文
export type ProblemDetail = {
  status: number;
  title?: string;
  detail?: string;
  errors?: { field: string; message: string }[];
};

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly problem?: ProblemDetail,
  ) {
    super(problem?.detail ?? `API request failed with status ${status}`);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
    // 自分の資産・進捗を見るツールなので、古い値を表示しないよう毎回取得する
    cache: "no-store",
  });

  // fetch は 4xx/5xx でも例外を投げないため、この層で例外に統一する
  if (!response.ok) {
    // 502 などエラー本文が JSON でない場合もあるので、パース失敗は許容する
    const problem: ProblemDetail | undefined = await response.json().catch(() => undefined);
    throw new ApiError(response.status, problem);
  }

  // DELETE の 204 は本文が無く、json() を呼ぶとパースエラーになる
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
  delete: (path: string) => request<void>(path, { method: "DELETE" }),
};
