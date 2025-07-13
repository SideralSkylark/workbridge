export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
  updatedAt?: string;
}
