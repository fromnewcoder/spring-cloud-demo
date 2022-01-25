package com.gonwan.springcloud.license.service;

import com.gonwan.springcloud.license.client.OrganizationWebClient;
import com.gonwan.springcloud.license.model.License;
import com.gonwan.springcloud.license.model.LicenseRepository;
import com.gonwan.springcloud.license.model.Organization;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private OrganizationWebClient organizationClient;

    private Random rand = new Random();

    @RateLimiter(name = "lsGetLicense")
    public License getLicense(String organizationId, String licenseId) {
        License license = licenseRepository.findByIdAndOrganizationId(licenseId, organizationId);
        if (license == null) {
            logger.debug("Cannot find license with id: {}", licenseId);
            return null;
        }
        Organization org = organizationClient.getOrganization(organizationId);
        if (org == null) {
            logger.debug("Cannot find organization with id: {}", organizationId);
        } else {
            license.setOrganizationName(org.getName());
            license.setContactName(org.getContactName());
            license.setContactEmail(org.getContactEmail());
            license.setContactPhone(org.getContactPhone());
        }
        return license;
    }

    private void randomRun() {
        int r = rand.nextInt(4);
        switch (r) {
            case 0:
                break;
            case 1:
                throw new IllegalArgumentException("Random thrown exception");
            case 2:
            case 3:
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
        }
    }

    @CircuitBreaker(name = "lsGetLicenses", fallbackMethod = "buildFallbackLicenseList")
    public List<License> getLicenses(String organizationId) {
        randomRun();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    /*
     * CallNotPermittedException is not handled and will be returned to client.
     * io.github.resilience4j.circuitbreaker: DEBUG
     */
    @SuppressWarnings("unused")
    private List<License> buildFallbackLicenseList(String organizationId, IllegalArgumentException e) {
        logger.warn("Got exception in fallback: {}", e.getMessage());
        List<License> fallbackList = new ArrayList<>();
        License license = new License();
        license.setId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no license information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    public void saveLicense(License license) {
        license.setId(UUID.randomUUID().toString());
        licenseRepository.save(license);
    }

    public void updateLicense(String licenseId, License license) {
        license.setId(licenseId);
        licenseRepository.save(license);
    }

    public void deleteLicense(String licenseId) {
        licenseRepository.deleteById(licenseId);
    }

}
