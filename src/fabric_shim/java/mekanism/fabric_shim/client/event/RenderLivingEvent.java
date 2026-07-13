package mekanism.fabric_shim.client.event;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code RenderLivingEvent} (game bus): Pre/Post wrap a living entity's
 * render so Mekanism can toggle model-part visibility while a MekaSuit is worn. Posted from a
 * LivingEntityRenderer#render mixin (Pre cancellable, at head; Post at tail) at step 3b. Only the
 * slice Mekanism reads (getEntity + getRenderer). Fresh implementation.
 */
public abstract class RenderLivingEvent<T extends LivingEntity, M extends EntityModel<T>> extends Event {

    private final T entity;
    private final LivingEntityRenderer<T, M> renderer;

    protected RenderLivingEvent(T entity, LivingEntityRenderer<T, M> renderer) {
        this.entity = entity;
        this.renderer = renderer;
    }

    public T getEntity() {
        return entity;
    }

    public LivingEntityRenderer<T, M> getRenderer() {
        return renderer;
    }

    public static class Pre<T extends LivingEntity, M extends EntityModel<T>> extends RenderLivingEvent<T, M> implements ICancellableEvent {

        public Pre(T entity, LivingEntityRenderer<T, M> renderer) {
            super(entity, renderer);
        }
    }

    public static class Post<T extends LivingEntity, M extends EntityModel<T>> extends RenderLivingEvent<T, M> {

        public Post(T entity, LivingEntityRenderer<T, M> renderer) {
            super(entity, renderer);
        }
    }
}
