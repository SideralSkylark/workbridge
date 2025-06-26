package com.workbridge.workbridge_app.service.projection;

public interface ServiceFeedProjection {
    Long getServiceId();
    String getTitle();
    String getDescription();
    Double getPrice();
    Long getProviderId();
    String getProviderUsername();
    String getProviderEmail();
    Double getProviderRating();
}
