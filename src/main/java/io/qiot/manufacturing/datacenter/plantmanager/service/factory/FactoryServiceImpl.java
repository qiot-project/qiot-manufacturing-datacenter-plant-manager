package io.qiot.manufacturing.datacenter.plantmanager.service.factory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.landscape.FactoryDTO;
import io.qiot.manufacturing.all.commons.domain.landscape.SubscriptionResponse;
import io.qiot.manufacturing.datacenter.commons.domain.subscription.FactorySubscriptionRequest;
import io.qiot.manufacturing.datacenter.plantmanager.domain.pojo.FactoryBean;
import io.qiot.manufacturing.datacenter.plantmanager.persistence.FactoryRepository;
import io.qiot.manufacturing.datacenter.plantmanager.util.converter.FactoryConverter;
import io.qiot.ubi.all.registration.client.RegistrationServiceClient;
import io.qiot.ubi.all.registration.domain.CertificateRequest;
import io.qiot.ubi.all.registration.domain.CertificateResponse;

@ApplicationScoped
class FactoryServiceImpl implements FactoryService {

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper MAPPER;

    @Inject
    FactoryRepository factoryRepository;

    @Inject
    FactoryConverter converter;

    @Inject
    @RestClient
    RegistrationServiceClient registrationServiceClient;

    @Override
    public SubscriptionResponse subscribe(FactorySubscriptionRequest request) throws Exception {
        /*
         * persist & flush the new entity (and get the generated ID)
         */
        FactoryBean factoryBean = new FactoryBean();
        factoryBean.serial = request.serial;
        factoryBean.name = request.name;

        factoryRepository.persist(factoryBean);
        factoryBean.registeredOn=Instant.now();

        LOGGER.info("Factory entity persisted, factory ID assigned: {}",
                factoryBean.id);

        /*
         * Get certificates
         */
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.id = factoryBean.id;
        certificateRequest.domain = "";
        certificateRequest.serial = request.serial;
        certificateRequest.name = request.name;
        certificateRequest.keyStorePassword = request.keyStorePassword;
        certificateRequest.ca = true;

        CertificateResponse certificateResponse = null;

        // while (certificateResponse == null) {
        // TODO: put sleep time in application.properties
        long sleepTime = 2000;
        try {
            LOGGER.info(
                    "Attempting to provision a new CA cert with the following data: \n{}",
                    MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(certificateRequest));

            certificateResponse = registrationServiceClient
                    .provisionCertificate(certificateRequest);

            LOGGER.info("CA cert obtained: {}",
                    MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(certificateResponse));

            /*
             * Return generated content
             */
            SubscriptionResponse response = new SubscriptionResponse();
            response.id = factoryBean.id;
            response.keystore = certificateResponse.keystore;
            response.truststore = certificateResponse.truststore;
            response.tlsCert = certificateResponse.tlsCert;
            response.tlsKey = certificateResponse.tlsKey;
            response.subscribedOn = factoryBean.registeredOn;
            LOGGER.info("Factory subscription process completed successfully: \n{}",
                    MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(response));
            return response;
        } catch (Exception e) {
            // TODO: improve exception handling
            LOGGER.info(
                    "An error occurred registering the factory. "
                            + "Retrying in {} millis.\n Error message: {}",
                    sleepTime, e.getMessage());

            factoryRepository.delete(factoryBean);

            throw e;
            // try {
            // Thread.sleep(sleepTime);
            // } catch (InterruptedException ie) {
            // Thread.currentThread().interrupt();
            // }
            // }
        }
    }

    @Override
    public FactoryDTO getById(UUID id) {
        FactoryBean factoryBean = factoryRepository.findById(id);
        return converter.sourceToDest(factoryBean);
    }

    @Override
    public List<FactoryDTO> getAllStations() {
        List<FactoryDTO> factoryDTOs = null;
        List<FactoryBean> factoryBeans = factoryRepository.findAll().list();
        factoryDTOs = converter.allSourceToDest(factoryBeans);
        return factoryDTOs;
    }
}
