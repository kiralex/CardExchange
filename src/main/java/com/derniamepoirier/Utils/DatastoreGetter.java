package com.derniamepoirier.Utils;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class DatastoreGetter {

    private DatastoreGetter(){};

    public static class DataStoreNotAvailableException extends Exception{
        public DataStoreNotAvailableException(String str){
            super(str);
        }
    }

    public static boolean isAvailable(){
        CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();
        CapabilityStatus statut = service.getStatus(Capability.DATASTORE).getStatus();
        return statut == CapabilityStatus.ENABLED || statut == CapabilityStatus.SCHEDULED_MAINTENANCE; // the service is active when SCHEDULED_MAINTENANCE
    }

    public static DatastoreService getDatastore() throws DataStoreNotAvailableException {
        if(!isAvailable())
            throw new DataStoreNotAvailableException("Service datastore non disponible");
        return DatastoreServiceFactory.getDatastoreService();
    }
}
