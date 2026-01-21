package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.zerocontact.ZeroContact;
import net.zerocontact.entity.ArmedRaider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ArmedRaiderModel extends GeoModel<ArmedRaider> {
    @Override
    public ResourceLocation getModelResource(ArmedRaider armedRaider) {
        return new ResourceLocation(ZeroContact.MOD_ID, "geo/armed_raider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ArmedRaider armedRaider) {
        return new ResourceLocation(ZeroContact.MOD_ID, "textures/models/armed_raider.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ArmedRaider armedRaider) {
        return new ResourceLocation(ZeroContact.MOD_ID,"animations/raider.animation.json");
    }

    @Override
    public void setCustomAnimations(ArmedRaider animatable, long instanceId, AnimationState<ArmedRaider> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        EntityModelData entityModelData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        setHeadRot(entityModelData);
        setArmsTrack(entityModelData);
    }

    private void setHeadRot(EntityModelData entityModelData) {
        CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
        head.setRotX(entityModelData.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(entityModelData.netHeadYaw() * Mth.DEG_TO_RAD);
    }

    private void setArmsTrack(EntityModelData entityModelData){
        CoreGeoBone leftArm = this.getAnimationProcessor().getBone("LeftArm");
        CoreGeoBone rightArm = this.getAnimationProcessor().getBone("RightArm");
        leftArm.setRotX(entityModelData.headPitch()*Mth.DEG_TO_RAD + leftArm.getRotX());
        rightArm.setRotX(entityModelData.headPitch()*Mth.DEG_TO_RAD + rightArm.getRotX());
        if(leftArm.getRotY()!=0){
            leftArm.setRotY(entityModelData.netHeadYaw()*Mth.DEG_TO_RAD + leftArm.getRotY());
            rightArm.setRotY(entityModelData.netHeadYaw()*Mth.DEG_TO_RAD + rightArm.getRotY());
        }
    }
}
