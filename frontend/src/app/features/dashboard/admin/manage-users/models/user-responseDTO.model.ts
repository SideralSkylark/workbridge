export interface UserResponseDTO {
  id: number;
  username: string;
  email: string;
  roles: string[];
  enabled: boolean;
}