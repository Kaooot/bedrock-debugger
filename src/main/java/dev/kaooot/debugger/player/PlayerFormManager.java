package dev.kaooot.debugger.player;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.forms.BaseForm;
import dev.kaooot.debugger.api.forms.CustomForm;
import dev.kaooot.debugger.api.forms.FormListener;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundCloseFormPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsResponsePacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class PlayerFormManager {

    private final BedrockDebuggerProxy proxy;

    private final Int2ObjectMap<BaseForm> forms = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<FormListener> formListeners = new Int2ObjectOpenHashMap<>();
    private int serverSettingsFormId = -1;
    @Getter
    private boolean formOpened;

    public boolean isValidFormId(int formId) {
        return this.forms.containsKey(formId);
    }

    public <R> FormListener<R> sendServerSettings(CustomForm form) {
        final int formId = 1337 + this.forms.size();
        this.forms.put(formId, form);

        final ServerSettingsResponsePacket packet = new ServerSettingsResponsePacket();
        packet.setFormID(formId);
        packet.setFormData(((BaseForm<?>) form).toJson().toString());
        this.proxy.getServer().sendPacket(packet);

        this.serverSettingsFormId = formId;
        final FormListener listener = new FormListener();
        this.formListeners.put(formId, listener);
        return listener;
    }

    public <R> FormListener<R> showForm(BaseForm form) {
        if (this.formOpened) {
            return new FormListener<>();
        }
        this.formOpened = true;
        final int formId = 1337 + this.forms.size();
        this.forms.put(formId, form);

        final ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.setFormID(formId);
        packet.setFormData(((BaseForm<?>) form).toJson().toString());
        this.proxy.getServer().sendPacket(packet);
        final FormListener listener = new FormListener();
        this.formListeners.put(formId, listener);
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        if (settingsConfig.isPrintDebugInfo()) {
            this.proxy.getLogger().debug("Opened internal form: " + form.getTitle());
        }
        return listener;
    }

    public void parseFormResponse(ModalFormResponsePacket packet) {
        final int formId = packet.getFormID();
        final String formData = packet.getJsonResponse();
        final BaseForm form = this.forms.get(formId);
        if (form != null) {
            final FormListener listener = this.formListeners.get(formId);
            if (formId != this.serverSettingsFormId) {
                this.forms.remove(formId);
                this.formListeners.remove(formId);
            }
            this.formOpened = false;
            if (formData == null || formData.trim().equalsIgnoreCase("null")) {
                listener.getCloseConsumer().accept(null);
            } else {
                final Object response = form.parseResponse(formData);
                if (response == null) {
                    listener.getCloseConsumer().accept(null);
                } else {
                    listener.getResponseConsumer().accept(response);
                }
            }
        }
    }

    public void closeForm() {
        this.proxy.getServer().sendPacket(new ClientboundCloseFormPacket());
        this.forms.clear();
        this.formListeners.clear();
        this.formOpened = false;
    }
}