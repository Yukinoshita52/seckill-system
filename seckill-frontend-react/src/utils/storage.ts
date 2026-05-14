const TOKEN_KEY = 'seckill_token';
const USERNAME_KEY = 'seckill_username';

export const storage = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  },

  getUsername(): string | null {
    return localStorage.getItem(USERNAME_KEY);
  },

  setUsername(username: string): void {
    localStorage.setItem(USERNAME_KEY, username);
  },

  removeUsername(): void {
    localStorage.removeItem(USERNAME_KEY);
  },

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
  },
};
