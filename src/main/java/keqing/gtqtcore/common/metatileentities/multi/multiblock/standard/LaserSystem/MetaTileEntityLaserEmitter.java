package keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.LaserSystem;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import keqing.gtqtcore.client.textures.GTQTTextures;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import keqing.gtqtcore.common.block.blocks.GTQTMultiblockCasing;
import keqing.gtqtcore.common.metatileentities.multi.generators.Tide.MetaTileEntityTideControl;
import keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.MetaTileEntityBaseWithControl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.V;
import static gregtech.api.GTValues.VN;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.INPUT_ENERGY;
import static keqing.gtqtcore.api.metaileentity.multiblock.GTQTMultiblockAbility.LASER_OUTPUT;
import static keqing.gtqtcore.api.metaileentity.multiblock.GTQTMultiblockAbility.LASER_OUTPUT;

public class MetaTileEntityLaserEmitter extends MetaTileEntityBaseWithControl {

    public MetaTileEntityLaserEmitter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    public int Amperage;
    public int Voltage;
    long Laser;//当前的激光
    long MaxLaser;
    long SetLaser;// 设定 的 最大激光强度
    int tier;
    @Override
    protected void updateFormedValid() {
        Amperage=this.getAbilities(LASER_OUTPUT).get(0).Amperage();
        Voltage=this.getAbilities(LASER_OUTPUT).get(0).Voltage();
        Laser=this.getAbilities(LASER_OUTPUT).get(0).Laser();
        MaxLaser=this.getAbilities(LASER_OUTPUT).get(0).MaxLaser();
        SetLaser=this.getAbilities(LASER_OUTPUT).get(0).SetLaser();
        tier=this.getAbilities(LASER_OUTPUT).get(0).tier();

        if(isWorkingEnabled()) {

            if (this.energyContainer.getEnergyStored() >= 0) {
                this.getAbilities(LASER_OUTPUT).get(0).setLaser(Math.min(this.getAbilities(LASER_OUTPUT).get(0).SetLaser(), this.energyContainer.getEnergyStored()));
                this.energyContainer.removeEnergy(Math.min(this.getAbilities(LASER_OUTPUT).get(0).SetLaser(), this.energyContainer.getEnergyStored()));
            } else this.getAbilities(LASER_OUTPUT).get(0).setLaser(0);
        }
        else this.getAbilities(LASER_OUTPUT).get(0).setLaser(0);

    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }
    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 260, 240);

        builder.widget((new AdvancedTextWidget(95, 10, this::addDisplayText, 2302755)).setMaxWidthLimit(181));

        builder.widget(new ClickButtonWidget(7, 10, 80, 20, "V -1", this::decrementThresholdV));
        builder.widget(new ClickButtonWidget(7, 40, 80, 20, "V +1", this::incrementThresholdV));

        builder.widget(new ClickButtonWidget(7, 70, 80, 20, "A -1", this::decrementThresholdA));
        builder.widget(new ClickButtonWidget(7, 100, 80, 20, "A +1", this::incrementThresholdA));

        builder.widget(new ClickButtonWidget(7, 130, 80, 20, "I/O", data -> setWorkingEnabled(!isWorkingEnabled())));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 92,160);
        return builder;
    }
    private void incrementThresholdV(Widget.ClickData clickData) {
        this.getAbilities(LASER_OUTPUT).get(0).addVoltage(1);
    }

    private void decrementThresholdV(Widget.ClickData clickData) {
        this.getAbilities(LASER_OUTPUT).get(0).addVoltage(-1);
    }

    private void incrementThresholdA(Widget.ClickData clickData) {
        this.getAbilities(LASER_OUTPUT).get(0).addAmperage(1);
    }

    private void decrementThresholdA(Widget.ClickData clickData) {
        this.getAbilities(LASER_OUTPUT).get(0).addAmperage(-1);
    }
    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString( "激光传输器输出  端-等级: " + tier));
        textList.add(new TextComponentString( "开关 是否传输: " + isWorkingEnabled()));
        textList.add(new TextComponentString( "实际 传输电压: " + Voltage));
        textList.add(new TextComponentString( "实际 传输电流: " + Amperage));
        textList.add(new TextComponentString( "实际 传输激光: " + Laser+" "+VN[GTUtility.getTierByVoltage(Laser)]));
        textList.add(new TextComponentString( "额定 传输激光: " + SetLaser+" "+VN[GTUtility.getTierByVoltage(SetLaser)]));
        textList.add(new TextComponentString( "最大 传输激光: " + MaxLaser+" "+VN[GTUtility.getTierByVoltage(MaxLaser)]));
        textList.add(new TextComponentString( "能量 缓存容量： " + energyContainer.getEnergyStored()));
        textList.add(new TextComponentString( "能量 容量上限: " + energyContainer.getEnergyCapacity()));
    }
    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("MMM", "MOM", "MMM")
                .aisle("MMM", "MMM", "MMM")
                .aisle("MMM", "MMM", "MMM")
                .aisle("MMM", "MCM", "MMM")
                .where('C', selfPredicate())
                .where('M', states(getCasingState()).setMinGlobalLimited(30)
                        .or(abilities(INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)))

                .where('O', abilities(LASER_OUTPUT))
                .build();
    }

    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public boolean hasMufflerMechanics() {
        return false;
    }

    private static IBlockState getCasingState() {
        return GTQTMetaBlocks.MULTI_CASING.getState(GTQTMultiblockCasing.CasingType.NITINOL_MACHINE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GTQTTextures.NITINOL_CASING;
    }

    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.POWER_SUBSTATION_OVERLAY;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaserEmitter(metaTileEntityId);
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.emptyList();
    }
}
