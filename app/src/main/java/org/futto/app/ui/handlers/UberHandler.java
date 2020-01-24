package org.futto.app.ui.handlers;

import com.google.android.gms.maps.model.LatLng;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

//TODO: Low priority: Eli. Redoc.

/**HTTPAsync is a... special AsyncTask for handling network (HTTP) requests using our PostRequest class.
 * HTTPAsync handles the asynchronous requirement for UI threads, and automatically handles user
 * notification for the well defined HTTP errors.
 *
 * HTTPAsync objects start executing on instantiation. While working it pops up an android UI spinner.
 * If the spinner UI element ("progressBar"?) is not declared in the activity's manifest it will instead run "silently"
 *
 * Inside your overridden doInBackground function you must assign the HTTP return value to responseCode (as an int).
 *
 * @author Dev */
public class UberHandler {
	private static final String CLIENT_ID = "b79PP-CdlzbQGAgDxWC78G19Gjif5Z89";
//	private static final String TOKEN = "6L76yWHXVP1XZ_8wjQJ3EYKuTtP-vYHEmOs81WmE";
	private static final String TOKEN = "GEej4_2O2nyzX-FHSaXAXCSrj-7S0QaVvOAMOg3F";

	public static void showUber(RideRequestButton rideRequestButton, LatLng pickup, LatLng dropOff) {
		RideParameters rideParams = new RideParameters.Builder()
				.setPickupLocation(pickup.latitude, pickup.longitude, "", "")
				.setDropoffLocation(dropOff.latitude, dropOff.longitude, "", "") // Price estimate will only be provided if this is provided.
				.build();

		SessionConfiguration config = new SessionConfiguration.Builder()
				.setClientId(CLIENT_ID)
				.setServerToken(TOKEN)
				.build();
		ServerTokenSession session = new ServerTokenSession(config);

		RideRequestButtonCallback callback = new RideRequestButtonCallback() {

			@Override
			public void onRideInformationLoaded() {

			}

			@Override
			public void onError(ApiError apiError) {

			}

			@Override
			public void onError(Throwable throwable) {

			}
		};
		rideRequestButton.setRideParameters(rideParams);
		rideRequestButton.setSession(session);
		rideRequestButton.setCallback(callback);
		rideRequestButton.loadRideInformation();
	}
}
