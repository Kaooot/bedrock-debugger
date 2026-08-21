package dev.kaooot.debugger.player;

import lombok.Data;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
public class ClientAuthoritativeSettings {

    private boolean bypassInvalidCreativeDestroyAction;
    private boolean forceMineAbilityEnabled;
    private boolean nukerEnabled;
    private int nukerWidth = 1;
    private int nukerHeight = 0;
    private float actorInteractionRange = 4.0f;
    private float actorAttackRange = 6.0f;
    private boolean cpsOverrideEnabled;
    private int clicksPerSecond = 20;
}