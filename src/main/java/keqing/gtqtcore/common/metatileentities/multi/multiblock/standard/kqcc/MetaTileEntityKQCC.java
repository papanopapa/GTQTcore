package keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.kqcc;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static gregtech.api.unification.material.Materials.PCBCoolant;
import static gregtech.api.unification.material.Materials.Water;
import static keqing.gtqtcore.api.unification.GTQTMaterials.LiquidNitrogen;
import static keqing.gtqtcore.common.block.blocks.GTQTKQCC.CasingType.*;

public class MetaTileEntityKQCC extends MultiblockWithDisplayBase implements IOpticalComputationProvider {
    private boolean isWorkingEnabled;

    private IFluidHandler coolantHandler;

    float HOT;
    private int CPU;
    private int GPU;
    private int RAM;

    int thresholdPercentage=0;
    private IEnergyContainer energyContainer;

    public MetaTileEntityKQCC(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }
    FluidStack COLD_STACK = Water.getFluid(10);
    FluidStack COLD_STACKA = PCBCoolant.getFluid(5);
    FluidStack COLD_STACKB = LiquidNitrogen.getFluid(1);

    FluidStack COLD_STACK1 = Water.getFluid(40);
    FluidStack COLD_STACKA1 = PCBCoolant.getFluid(20);
    FluidStack COLD_STACKB1 = LiquidNitrogen.getFluid(4);
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setFloat("HOT", HOT);
        data.setInteger("thresholdPercentage", thresholdPercentage);
        return super.writeToNBT(data);
    }
   
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        HOT = data.getFloat("HOT");
        thresholdPercentage = data.getInteger("thresholdPercentage");
    }

    private boolean hasNotEnoughEnergy;
    private void consumeEnergy() {
        int energyToConsume = CWTT()*15*thresholdPercentage;

        if (this.hasNotEnoughEnergy && energyContainer.getEnergyStored() >energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    isWorkingEnabled= HOT < 50000;
                    if(HOT < 50000) {
                        if (thresholdPercentage == 2) HOT = HOT + HEAT() * 4;
                        if (thresholdPercentage == 1) HOT = HOT + HEAT();
                    }
                } else {
                    this.hasNotEnoughEnergy = true;
                    isWorkingEnabled=false;
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            isWorkingEnabled=false;
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("NN","NN","NN","NN")
                .aisle("AN","AN","AN","NN")
                .aisle("AN","AN","AN","NN")
                .aisle("AN","AN","AN","NN")
                .aisle("AN","AN","AN","NN")
                .aisle("XN","SN","NN","NN")
                .where('S', selfPredicate())
                .where('X', abilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION))
                .where('N', states(getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1)))
                .where('A', any())

                .build();
    }
    public boolean hasMufflerMechanics() {
        return false;
    }
    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
    @SideOnly(Side.CLIENT)
    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.COMPUTATION;
    }
    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }
    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtqtcore.multiblock.kqcc.description")};
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 260, 240);


        builder.image(3, 4, 90, 48, GuiTextures.DISPLAY);
        builder.dynamicLabel(7, 8, () -> "散热模块", 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(7, 20, this::addHeatManager, 16777215)).setMaxWidthLimit(90).setClickHandler(this::handleDisplayClick));


        builder.image(92, 4, 162, 48, GuiTextures.DISPLAY);
        builder.dynamicLabel(95, 8, () -> "计算模块", 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(95, 20, this::addCwtManager, 16777215)).setMaxWidthLimit(162).setClickHandler(this::handleDisplayClick));

        //UI
        builder.image(92, 52, 162, 80, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(95, 56, this::ventInfo, 16777215)).setMaxWidthLimit(162).setClickHandler(this::handleDisplayClick));

        int j=0;
        //1号
        builder.image(3, 52, 90, 40, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 58, this::addInfo1, 16777215)).setMaxWidthLimit(90).setClickHandler(this::handleDisplayClick));
        j++;
        //2号
        builder.image(3, 52+j*40, 90, 40, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 58+j*40, this::addInfo2, 16777215)).setMaxWidthLimit(90).setClickHandler(this::handleDisplayClick));
        j++;
        //3号
        builder.image(3, 52+j*40, 90, 40, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 58+j*40, this::addInfo3, 16777215)).setMaxWidthLimit(90).setClickHandler(this::handleDisplayClick));

        j++;
        builder.image(3, 52+j*40, 90, 66, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 58+j*40, this::addInfo4, 16777215)).setMaxWidthLimit(90).setClickHandler(this::handleDisplayClick));

        //op
        builder.widget(new ClickButtonWidget(100, 138, 48, 18, "关机", data -> thresholdPercentage = 0));
        builder.widget(new ClickButtonWidget(150, 138, 48, 18, "开机", data -> thresholdPercentage = 1));
        builder.widget(new ClickButtonWidget(200, 138, 48, 18, "超频", data -> thresholdPercentage = 2));

        builder.widget((new ProgressWidget(() -> (double) HOT /50000, 5, 51, 85, 3, GuiTextures.PROGRESS_BAR_FUSION_HEAT, ProgressWidget.MoveType.HORIZONTAL)).setHoverTextConsumer((list) -> addBarHoverText(list, (long) HOT,50000,"热量: %s")));

        builder.widget((new ProgressWidget(() ->  (double) returncwt() / (CWTT()*2), 93, 51, 157, 3, GuiTextures.PROGRESS_BAR_HPCA_COMPUTATION, ProgressWidget.MoveType.HORIZONTAL)).setHoverTextConsumer((list) -> addBarHoverText(list, returncwt(),CWTT()* 2L,"CWUt: %s")));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 92,160);
        return builder;
    }
    public void addBarHoverText(List<ITextComponent> hoverList,long a,long b,String string) {
        ITextComponent cwutInfo = TextComponentUtil.stringWithColor(
                TextFormatting.AQUA,
                a+ " / " + b + " H");
        hoverList.add(TextComponentUtil.translationWithColor(
                TextFormatting.GRAY,
                string,
                cwutInfo));
    }
    protected void addInfo1(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(tl -> {
                    if (coolantHandler != null) {
                        FluidStack STACKA = coolantHandler.drain(Water.getFluid(Integer.MAX_VALUE), false);
                        int liquidWaterAmount = STACKA == null ? 0 : STACKA.amount;
                        textList.add(new TextComponentTranslation("冷却液种类-水："));
                        textList.add(new TextComponentTranslation("流体缓存：%s mb", TextFormattingUtil.formatNumbers((liquidWaterAmount))));
                        textList.add(new TextComponentTranslation("消耗速率：%s mb", 10*Math.pow(2,thresholdPercentage)));
                    }

                });
    }

    protected void addInfo2(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(tl -> {
                    if (coolantHandler != null) {
                        FluidStack STACKB = coolantHandler.drain(PCBCoolant.getFluid(Integer.MAX_VALUE), false);
                        int liquidPCBAmount = STACKB == null ? 0 : STACKB.amount;

                        textList.add(new TextComponentTranslation("冷却液种类-冷却液："));
                        textList.add(new TextComponentTranslation("流体缓存：%s mb", TextFormattingUtil.formatNumbers((liquidPCBAmount))));
                        textList.add(new TextComponentTranslation("消耗速率：%s mb", 5*Math.pow(2,thresholdPercentage)));
                    }
                });
    }

    protected void addInfo3(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(tl -> {
                    if (coolantHandler != null) {

                        FluidStack STACKC = coolantHandler.drain(LiquidNitrogen.getFluid(Integer.MAX_VALUE), false);
                        int liquidNITAmount = STACKC == null ? 0 : STACKC.amount;
                        textList.add(new TextComponentTranslation("冷却液种类-液氮："));
                        textList.add(new TextComponentTranslation("流体缓存：%s mb", TextFormattingUtil.formatNumbers((liquidNITAmount))));
                        textList.add(new TextComponentTranslation("消耗速率：%s mb", 1*Math.pow(1,thresholdPercentage)));
                    }
                });
    }

    protected void addInfo4(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(tl -> {
                    textList.add(new TextComponentTranslation("能源模块"));
                    textList.add(new TextComponentTranslation("耗电：%s EU/t", CWTT()*15*thresholdPercentage));
                    textList.add(new TextComponentTranslation("缓存：%s",energyContainer.getEnergyStored()));
                    textList.add(new TextComponentTranslation("容量：%s",energyContainer.getEnergyCapacity()));
                });
    }

    protected void ventInfo(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        final int xDir = this.getFrontFacing().getOpposite().getXOffset();
        final int zDir = this.getFrontFacing().getOpposite().getZOffset();
        textList.add(new TextComponentTranslation("安装组件："));
        textList.add(new TextComponentTranslation("%s | %s | %s | %s",getStringFromPos(this.getPos().add(xDir, -1, zDir)),getStringFromPos(this.getPos().add(xDir * 2, -1, zDir * 2)),getStringFromPos(this.getPos().add(xDir * 3, -1, zDir * 3)),getStringFromPos(this.getPos().add(xDir * 4, -1, zDir * 4))));
        textList.add(new TextComponentTranslation("%s | %s | %s | %s",getStringFromPos(this.getPos().add(xDir, 0, zDir)),getStringFromPos(this.getPos().add(xDir * 2, 0, zDir * 2)),getStringFromPos(this.getPos().add(xDir * 3, 0, zDir * 3)),getStringFromPos(this.getPos().add(xDir * 4, 0, zDir * 4))));
        textList.add(new TextComponentTranslation("%s | %s | %s | %s",getStringFromPos(this.getPos().add(xDir, 1, zDir)),getStringFromPos(this.getPos().add(xDir * 2, 1, zDir * 2)),getStringFromPos(this.getPos().add(xDir * 3, 1, zDir * 3)),getStringFromPos(this.getPos().add(xDir * 4, 1, zDir * 4))));
        textList.add(new TextComponentTranslation("数据汇总："));
        textList.add(new TextComponentTranslation("CPU： %s | GPU： %s | RAM： %s", CPU,GPU,RAM));
    }
    protected void addHeatManager(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        textList.add(new TextComponentTranslation("设备内热：%s", HOT));
        if(thresholdPercentage==0) textList.add(new TextComponentTranslation("实际产热：%s", 0));
        if(thresholdPercentage==1) textList.add(new TextComponentTranslation("实际产热：%s", HEAT()));
        if(thresholdPercentage==2) textList.add(new TextComponentTranslation("实际产热：%s", HEAT()*4));
    }

    protected void addCwtManager(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        if(thresholdPercentage==0)textList.add(new TextComponentTranslation("工作状态:关机"));
        if(thresholdPercentage==1)textList.add(new TextComponentTranslation("工作状态:开机"));
        if(thresholdPercentage==2)textList.add(new TextComponentTranslation("工作状态:超频"));

        if((RAM)>=(GPU)&&(RAM)>=(CPU))
            textList.add(new TextComponentTranslation("gregtech.multiblock.kqcc.normal"));
        else
            textList.add(new TextComponentTranslation("gregtech.multiblock.kqcc.lack"));


        textList.add(new TextComponentTranslation("算力输出： %s / %s CWU/t", returncwt(),CWTT()*2));


    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
}

    int CWTT()
    {
        if((RAM)>=(GPU)&&(RAM)>=(CPU)) return (GPU)+(CPU);
        else return (RAM);
    };
    int HEAT()
    {
        return (GPU)*(CPU);
    };
    int returncwt()
    {
        if(isWorkingEnabled) {
            if(HOT<30000) return CWTT()*(thresholdPercentage);
            else return CWTT()*thresholdPercentage/2;
        }
        else return 0;
    }
    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed()) {
            if (HOT>=30000) {
                textList.add(new TextComponentTranslation("gtqtcore.multiblock.kqcc.hot"));
            }
        }
    }
    @Override
    public int requestCWUt(int cwut, boolean simulate, Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return returncwt();
    }

    @Override
    public int getMaxCWUt(Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return (GPU+CPU)*thresholdPercentage;
    }

    @Override
    public boolean canBridge(Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityKQCC(metaTileEntityId);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), true,
                isStructureFormed());
    }
    @SideOnly(Side.CLIENT)
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
                return Textures.INERT_PTFE_CASING;
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.coolantHandler = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));

    }
    int shortCPU;
    int shortGPU;
    int shortRAM;
    @Override
    protected void updateFormedValid() {
        //每次提供算力都检查
        final int xDir = this.getFrontFacing().getOpposite().getXOffset();
        final int zDir = this.getFrontFacing().getOpposite().getZOffset();
        int h;

        shortCPU=0;
        shortGPU=0;
        shortRAM=0;
        for(int i=1;i<5;i++) {
            for (h = -1; h < 2; h++) {
                if(!cehckPart(this.getPos().add(xDir * i, h, zDir * i)))return;
            }
        }
        if(shortCPU!=CPU)CPU=shortCPU;
        if(shortGPU!=GPU)GPU=shortGPU;
        if(shortRAM!=RAM)RAM=shortRAM;
        shortCPU=0;
        shortGPU=0;
        shortRAM=0;

        //能量消耗 如果能力多就开放算力输出功能
        consumeEnergy();

        //主动冷却
        if(HOT>10) HOT=HOT-10;

        //被动冷却
        if(HOT>5000) {
            if(thresholdPercentage!=2) {
                if (COLD_STACK.isFluidStackIdentical(coolantHandler.drain(COLD_STACK, false))) {
                    coolantHandler.drain(COLD_STACK, true);
                    if(HOT>770) HOT = HOT - 770;
                }
                if (COLD_STACKA.isFluidStackIdentical(coolantHandler.drain(COLD_STACKA, false))) {
                    coolantHandler.drain(COLD_STACKA, true);
                    if(HOT>1440) HOT = HOT - 1440;
                }
                if (COLD_STACKB.isFluidStackIdentical(coolantHandler.drain(COLD_STACKB, false))) {
                    coolantHandler.drain(COLD_STACKB, true);
                    if(HOT>2880) HOT = HOT - 2880;
                }
            }

            if(thresholdPercentage==2) {
                if (COLD_STACK1.isFluidStackIdentical(coolantHandler.drain(COLD_STACK1, false))){
                    coolantHandler.drain(COLD_STACK1, true);
                    if(HOT>1440)HOT = HOT - 1440;
                }
                if (COLD_STACKA1.isFluidStackIdentical(coolantHandler.drain(COLD_STACKA1, false))) {
                    coolantHandler.drain(COLD_STACKA1, true);
                    if(HOT>2880)HOT = HOT - 2880;
                }
                if (COLD_STACKB1.isFluidStackIdentical(coolantHandler.drain(COLD_STACKB1, false))) {
                    coolantHandler.drain(COLD_STACKB1, true);
                    if(HOT>5760)HOT = HOT - 5760;
                }
            }
        }

    }
    public String getStringFromPos(BlockPos poss)
    {
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_VENT)) return"NULL  ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMI))return "RAM I  ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMII))return "RAM II ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMIII))return "RAM III";

        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUI))return "CPU I  ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUII))return "CPU II ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUIII))return "CPU III";

        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUI))return "GPU I  ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUII))return "GPU II ";
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUIII))return "GPU III";
        return "       ";
    }
    public boolean cehckPart(BlockPos poss)
    {
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_VENT))return true;
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMI)){shortRAM+=1;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMII)){shortRAM+=2;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_RAMIII)){shortRAM+=3;return true;}

        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUI)){shortCPU+=1;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUII)){shortCPU+=2;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_CPUIII)){shortCPU+=3;return true;}

        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUI)){shortGPU+=1;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUII)){shortGPU+=2;return true;}
        if(this.getWorld().getBlockState(poss)== GTQTMetaBlocks.KQCC.getState(COMPUTER_GPUIII)){shortGPU+=3;return true;}
        return false;
    }
    @Override
    public void checkStructurePattern() {
        super.checkStructurePattern();
    }
}