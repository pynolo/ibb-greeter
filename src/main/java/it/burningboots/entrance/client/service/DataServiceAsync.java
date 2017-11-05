package it.burningboots.entrance.client.service;

import it.burningboots.entrance.shared.Amount;
import it.burningboots.entrance.shared.ConfigBean;
import it.burningboots.entrance.shared.entity.Config;
import it.burningboots.entrance.shared.entity.Participant;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see it.burningboots.entrance.client.service.DataService
     */
    
	void getConfigBean(AsyncCallback<ConfigBean> callback );
	void findConfigByKey(String key, AsyncCallback<Config> callback );
    //void saveOrUpdateConfig(Config config, AsyncCallback<Void> callback);
	void getDonationMin(AsyncCallback<Double> callback );
	
    void findParticipantById( java.lang.Integer id, AsyncCallback<Participant> callback );
    void findParticipantByItemNumber( java.lang.String itemNumber, int delayMillis, AsyncCallback<Participant> callback );
    void findParticipants( boolean confirmed, String orderBy, AsyncCallback<java.util.List<Participant>> callback );
    void saveOrUpdateParticipant(Participant prt, AsyncCallback<Participant> callback );
	void countConfirmed(AsyncCallback<Integer> callback);

	//Payment
	void payWithStripe(String itemNumber, Amount amount, String number,
			String expMonth, String expYear, AsyncCallback<String> callback);
    
    //WebSession
	void createWebSession(String seed, AsyncCallback<String> callback);
	void verifyWebSession(String idWebSession, AsyncCallback<Boolean> callback);
	void getQueuePosition(String idWebSession, AsyncCallback<Integer> callback);
	void updateHeartbeat(String idWebSession, AsyncCallback<Date> callback);
	void deleteWebSession(String idWebSession, AsyncCallback<Boolean> callback);
	
    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util { 
        private static DataServiceAsync instance;

        public static final DataServiceAsync getInstance() {
            if ( instance == null ) {
                instance = (DataServiceAsync) GWT.create( DataService.class );
            }
            return instance;
        }

        private Util() {
            // Utility class should not be instantiated
        }
    }

}
