package io.qiot.manufacturing.datacenter.plantmanager.persistence;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.qiot.manufacturing.datacenter.plantmanager.domain.pojo.MachineryBean;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class MachineryRepository implements PanacheRepositoryBase<MachineryBean, UUID> {

    @Inject
    Logger LOGGER;

}
