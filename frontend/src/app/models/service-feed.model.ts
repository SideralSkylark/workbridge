import { ServiceDTO } from "../services/service.service";
  
export interface ServiceFeedDTO {
    service: ServiceDTO;
    providerRating: number;
    providerUsername: string;
    providerEmail: string;
}
  