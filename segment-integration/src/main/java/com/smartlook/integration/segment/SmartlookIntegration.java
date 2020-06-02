package com.smartlook.integration.segment;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.smartlook.sdk.smartlook.Smartlook;
import com.smartlook.sdk.smartlook.analytics.video.model.annotation.ViewState;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SmartlookIntegration extends Integration<Void> {

    private static final String SMARTLOOK_KEY = "Smartlook";

    // Integration settings
    private static final String SMARTLOOK_API_KEY = "api_key";
    private static final String SMARTLOOK_FPS = "fps";
    private static final int SMARTLOOK_DEFAULT_FPS = 2;

    public static final Factory FACTORY = new Factory() {

        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {

            Smartlook.SetupOptionsBuilder smartlookBuilder = parseOutIntegrationSettings(settings);

            if (smartlookBuilder != null) {
                return new SmartlookIntegration(smartlookBuilder);
            }

            return null;
        }

        @NotNull
        @Override
        public String key() {
            return SMARTLOOK_KEY;
        }
    };

    private static Smartlook.SetupOptionsBuilder parseOutIntegrationSettings(ValueMap settings) {

        Smartlook.SetupOptionsBuilder builder;

        if (settings.containsKey(SMARTLOOK_API_KEY)) {
            builder = new Smartlook.SetupOptionsBuilder(settings.getString(SMARTLOOK_API_KEY));
        } else {
            return null;
        }

        if (settings.containsKey(SMARTLOOK_FPS)) {
            builder.setFps(settings.getInt(SMARTLOOK_FPS, SMARTLOOK_DEFAULT_FPS));
        }

        //todo we can parse out other params too

        return builder;
    }

    private SmartlookIntegration(Smartlook.SetupOptionsBuilder smartlookBuilder) {
        Smartlook.setupAndStartRecording(smartlookBuilder.build());
    }

    @Override
    public void identify(IdentifyPayload identify) {
        String userId = identify.userId();
        JSONObject properties = identify.traits().toJsonObject();

        if (!isValidUserId(userId) && properties == null) {
            return;
        }

        if (isValidUserId(userId) && properties == null) {
            Smartlook.setUserIdentifier(userId);
        }

        if (!isValidUserId(userId) && properties != null) {
            Smartlook.setUserIdentifier("undefined", properties);
        }
    }


    @Override
    public void track(TrackPayload track) {
        String eventName = track.event();
        Properties properties = track.properties();

        if (properties.isEmpty()) {
            Smartlook.trackCustomEvent(eventName);
        } else {
            Smartlook.trackCustomEvent(eventName, properties.toJsonObject());
        }
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

    private boolean isValidUserId(String userId) {
        return !(userId == null || userId.isEmpty());
    }
}
