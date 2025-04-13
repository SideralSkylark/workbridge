import { ServiceDTO } from "../services/service.service";
  
export interface ServiceFeedDTO {
    service: ServiceDTO;
    providerRating: number;
    providerName: string;
    providerEmail: string;
}
  