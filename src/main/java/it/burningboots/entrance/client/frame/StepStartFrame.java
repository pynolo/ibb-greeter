package it.burningboots.entrance.client.frame;

import it.burningboots.entrance.client.ClientConstants;
import it.burningboots.entrance.client.CookieSingleton;
import it.burningboots.entrance.client.LocaleConstants;
import it.burningboots.entrance.client.UiSingleton;
import it.burningboots.entrance.client.UriBuilder;
import it.burningboots.entrance.client.UriDispatcher;
import it.burningboots.entrance.client.WaitSingleton;
import it.burningboots.entrance.client.service.DataService;
import it.burningboots.entrance.client.service.DataServiceAsync;
import it.burningboots.entrance.client.widgets.HeartbeatWidget;
import it.burningboots.entrance.shared.AppConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StepStartFrame extends FramePanel {
	
	private final DataServiceAsync dataService = GWT.create(DataService.class);
	private LocaleConstants constants = GWT.create(LocaleConstants.class);
	
	private Timer timer = null;
	private String idWebSession = null;
	private int queuePosition = 1000;
	private int confirmed = 1000;
	
	private HeartbeatWidget heartbeat = null;
	private InlineHTML countLabel = new InlineHTML();
	
	public StepStartFrame(UriBuilder params) {
		super();
		assignWebSessionCookie();
	}
	
	private void startQueueCheckTimer() {
		reload();
		timer = new Timer() {
			public void run() {
				reload();
			}
		};
		// Schedule the timer to run once in 1 minute.
		timer.scheduleRepeating(AppConstants.QUEUE_RELOAD_TIME);
	}

	public void cancelQueueCheckTimer() {
		if (timer != null) {
			if (timer.isRunning()) {
				timer.cancel();
				if (timer.isRunning())
					UiSingleton.get().addWarning("I couldn't cancel Queue Check timer");
			}
		}
	}
	
	private void draw() {
		VerticalPanel cp = new VerticalPanel(); //Content panel
		
		//TITLE
		setTitle(constants.queueTitle());
				
		this.add(cp);
		cp.add(new HTML("<p>"+constants.queueVerify()+"</p><p>&nbsp;</p>"));
		HorizontalPanel waitPanel = new HorizontalPanel();
		cp.add(waitPanel);
		
		waitPanel.add(new HTML(constants.queueCurrentlyOnline1()));
		waitPanel.add(countLabel);
		waitPanel.add(new HTML(constants.queueCurrentlyOnline2()+"</p>"));
		cp.add(new HTML("<p>&nbsp;</p>"));
		cp.add(new HTML(constants.queuePleaseWait()));
		cp.add(new HTML("<p>&nbsp;</p>"));
		cp.add(new HTML("<p align='center'>"+ClientConstants.ICON_LOADING_BIG+"</p>"));
		heartbeat = new HeartbeatWidget();
		cp.add(heartbeat);
	}
	
	private void controller() {
		if (this.queuePosition < AppConstants.QUEUE_MAX_LENGTH) {
			//Forward
			if (confirmed >= AppConstants.DONATION_MAX) {
				cancelQueueCheckTimer();
				heartbeat.cancelHeartbeatTimer();
				UriBuilder param = new UriBuilder();
				param.triggerUri(UriDispatcher.ERROR_CLOSED);
			} else {
				cancelQueueCheckTimer();
				heartbeat.cancelHeartbeatTimer();
				UriBuilder param = new UriBuilder();
				param.triggerUri(UriDispatcher.STEP_PERSONAL);
			}
		} else {
			countLabel.setHTML("<b>"+this.queuePosition+"</b>");
		}
	}
	
	private void reload() {
		//Window.Location.reload();
		loadConfirmedParticipants();
	}

	
	//Async methods
	
	
	private void assignWebSessionCookie() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				UiSingleton.get().addError(caught);
				WaitSingleton.get().stop();
				
				UriBuilder param = new UriBuilder();
				param.triggerUri(UriDispatcher.ERROR_SYSTEM);
			}
			@Override
			public void onSuccess(String result) {
				setIdWebSession(result);
				CookieSingleton.get().removeCookie(ClientConstants.WEBSESSION_COOKIE_NAME);
				CookieSingleton.get().setCookie(ClientConstants.WEBSESSION_COOKIE_NAME, result);
				WaitSingleton.get().stop();
				
				draw();
				startQueueCheckTimer();
			}
		};
		dataService.createWebSession(Window.getClientHeight()+" "+Window.getClientWidth(), callback);
	}
	
	private void loadConfirmedParticipants() {
		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				UiSingleton.get().addError(caught);
				//WaitSingleton.get().stop();
			}
			@Override
			public void onSuccess(Integer result) {
				setConfirmedParticipants(result);
				//WaitSingleton.get().stop();
				
				loadQueuePosition();
			}
		};
		//WaitSingleton.get().start();
		dataService.countConfirmed(callback);
	}
	
	private void loadQueuePosition() {
		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				UiSingleton.get().addError(caught);
				//WaitSingleton.get().stop();
			}
			@Override
			public void onSuccess(Integer result) {
				setQueuePosition(result);
				//WaitSingleton.get().stop();
				
				controller();
			}
		};
		//WaitSingleton.get().start();
		dataService.getQueuePosition(idWebSession, callback);
	}

	public void setIdWebSession(String idWebSession) {
		this.idWebSession = idWebSession;
	}
	public void setQueuePosition(int queuePosition) {
		this.queuePosition = queuePosition;
	}
	public void setConfirmedParticipants(int confirmed) {
		this.confirmed = confirmed;
	}
	
}
