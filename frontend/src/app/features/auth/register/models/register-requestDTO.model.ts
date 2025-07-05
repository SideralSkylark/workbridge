// user.model.ts
export interface RegisterRequestDTO {
    username: string;
    email: string;
    password: string;
    roles: string[];
    status?: string;
  }
  