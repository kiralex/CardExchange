package com.derniamepoirier.Utils;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class DatastoreGetter {

    private DatastoreGetter(){};

    /**
     * Exception throwed if {@link DatastoreService} is not available
     */
    public static class DataStoreNotAvailableException extends Exception{
        public DataStoreNotAvailableException(String str){
            super(str);
        }
    }

    /**
     * Check if {@link DatastoreService} is available
     * @return
     *      <ul>
     *          <li>true if {@link DatastoreService} is available</li>
     *          <li>false if not</li>
     *      </ul>
     */
    public static boolean isAvailable(){
        CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();
        CapabilityStatus statut = service.getStatus(Capability.DATASTORE).getStatus();
        return statut == CapabilityStatus.ENABLED || statut == CapabilityStatus.SCHEDULED_MAINTENANCE; // the service is active when SCHEDULED_MAINTENANCE
    }

    /**
     *
     * @return {@link DatastoreService} instance
     * @throws DataStoreNotAvailableException Exception throwed if {@link DatastoreService} is not available
     */
    public static DatastoreService getDatastore() throws DataStoreNotAvailableException {
        if(!isAvailable())
            throw new DataStoreNotAvailableException("Service datastore non disponible");
        return DatastoreServiceFactory.getDatastoreService();
    }
}
