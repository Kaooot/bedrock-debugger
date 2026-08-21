package dev.kaooot.debugger.core.service;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.common.util.Preconditions;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.core.http.HttpRequest;
import dev.kaooot.debugger.core.http.HttpRequestMethod;
import dev.kaooot.debugger.core.model.auth.AuthServiceInfoResponse;
import dev.kaooot.debugger.core.model.gatherings.JoinExperienceRequest;
import dev.kaooot.debugger.core.model.gatherings.JoinExperienceResponse;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class GatheringServiceConnection {

    private final BedrockDebuggerProxy proxy;
    private final String host = "gatherings-secondary.franchise.minecraft-services.net";

    public JoinExperienceResponse joinExperience(String experienceId) {
        final AuthServiceInfoResponse authResult =
            this.proxy.getAuthServiceConnection().getResult();

        Preconditions.checkNotNull(
            authResult,
            "Tried to join an experience without authentication"
        );

        final JoinExperienceRequest request = new JoinExperienceRequest(experienceId);
        final JoinExperienceResponse response = HttpRequest.<JoinExperienceResponse>builder()
            .proxy(this.proxy)
            .method(HttpRequestMethod.POST)
            .url("https://" + this.host + "/api/v2.0/join/experience")
            .body(request)
            .auth(authResult.getResult().getAuthorizationHeader())
            .build()
            .send(JoinExperienceResponse.class);

        Preconditions.checkNotNull(response.getResult(), "Received invalid join experience result");
        return response;
    }
}