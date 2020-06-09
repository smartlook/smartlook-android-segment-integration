package com.smartlook.integration.segment;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.AliasPayload;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.smartlook.sdk.smartlook.Smartlook;
import com.smartlook.sdk.smartlook.analytics.video.model.annotation.ViewState;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartlookIntegration extends Integration<Void> {

    private static final String SMARTLOOK_KEY = "Smartlook";

    // Integration settings
    private static final String SMARTLOOK_API_KEY = "api_key";

    public static final Factory FACTORY = new Factory() {

        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {

            if (Smartlook.isRecording()) {
                return new SmartlookIntegration();
            } else if (settings.containsKey(SMARTLOOK_API_KEY)) {
                Smartlook.setup(settings.getString(SMARTLOOK_API_KEY));
                return new SmartlookIntegration();
            }

            return null;
        }

        @NotNull
        @Override
        public String key() {
            return SMARTLOOK_KEY;
        }
    };

    @Override
    public void identify(IdentifyPayload identify) {
        String userId = identify.userId();
        String anonymousId = identify.anonymousId();
        JSONObject properties = identify.traits().toJsonObject();

        smartlookIdentify(userId, anonymousId, properties);
    }

    @Override
    public void alias(AliasPayload alias) {
        String userId = alias.userId();
        String anonymousId = alias.anonymousId();

        smartlookIdentify(userId, anonymousId, null);
    }


    @Override
    public void track(TrackPayload track) {
        String eventName = track.event();
        Properties properties = track.properties();

        properties.put("sl-origin", "segment");
        Smartlook.trackCustomEvent(eventName, properties.toJsonObject());
    }

    @Override
    public void screen(ScreenPayload screen) {
        String eventName = screen.event();
        Smartlook.trackNavigationEvent(eventName, ViewState.START);
    }

    // Smartlook only uses static calls thus we don't have any underlying instance
    @Override
    public Void getUnderlyingInstance() {
        return null;
    }

    private void smartlookIdentify(String userId, String anonymousId, JSONObject properties) {
        String identifier = isValidId(userId) ? userId : isValidId(anonymousId) ? anonymousId : null;

        if (isValidId(userId) && isValidId(anonymousId)) {
            if (properties == null) {
                properties = new JSONObject();
            }

            try {
                properties.put("anonymous_id", anonymousId);
            } catch (JSONException ignored) {
            }
        }

        if (identifier == null && properties != null) {
            Smartlook.setUserIdentifier("unknown", properties);
        }

        if (identifier != null) {
            if (properties == null) {
                Smartlook.setUserIdentifier(identifier);
            } else {
                Smartlook.setUserIdentifier(identifier, properties);
            }
        }
    }

    private boolean isValidId(String id) {
        return !(id == null || id.isEmpty());
    }
}
