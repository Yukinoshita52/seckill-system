export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nickname?: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
}

export interface UserInfo {
  username: string;
}
