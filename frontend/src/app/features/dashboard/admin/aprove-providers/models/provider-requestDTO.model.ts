export interface ProviderRequest {
  id: number;
  username: string;
  email: string;
  approved: boolean;
  requestedOn: string;
  approvedOn?: string;
}