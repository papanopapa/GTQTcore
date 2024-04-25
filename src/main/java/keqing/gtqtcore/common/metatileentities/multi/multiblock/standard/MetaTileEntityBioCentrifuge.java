package keqing.gtqtcore.common.metatileentities.multi.multiblock.standard;

import gregicality.multiblocks.api.capability.IParallelMultiblock;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqtcore.api.blocks.impl.WrappedIntTired;
import keqing.gtqtcore.api.metaileentity.GTQTRecipeMapMultiblockController;
import keqing.gtqtcore.api.metaileentity.multiblock.GTQTRecipeMapMultiblockOverwrite;
import keqing.gtqtcore.api.predicate.TiredTraceabilityPredicate;
import keqing.gtqtcore.api.recipes.GTQTcoreRecipeMaps;
import keqing.gtqtcore.api.utils.GTQTUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.GTValues.VA;


public class MetaTileEntityBioCentrifuge extends GTQTRecipeMapMultiblockController {
    private int glass_tier;
    private int clean_tier;
    private int tubeTier;
    public int ParallelLim;
    public int ParallelNumA;
    public int modern;
    public int P;
    int tier;
    public MetaTileEntityBioCentrifuge(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap[] {
                RecipeMaps.CENTRIFUGE_RECIPES,
                GTQTcoreRecipeMaps.BIO_CENTRIFUGE
        });
        this.recipeMapWorkable = new BioCentrifugeWorkableHandler(this);
    }
    private class BioCentrifugeWorkableHandler extends MultiblockRecipeLogic {

        public BioCentrifugeWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return ParallelNum;
        }

        @Override
        public void setMaxProgress(int maxProgress)
        {
                this.maxProgressTime = maxProgress/(10-clean_tier)/10;
        }
    }

    int ParallelNum;


    @Override
    public void update() {
        super.update();
        if (modern == 0)
        {
            ParallelNum=ParallelNumA;
        }
        if (modern == 1)
        {
            P = (int) ((this.energyContainer.getEnergyStored() + energyContainer.getInputPerSec()) / getMinVa());
            ParallelNum = Math.min(P, ParallelLim);
        }
    }
    public int getMinVa()
    {
        if((Math.min(this.energyContainer.getEnergyCapacity()/32,VA[tier])*20)==0)return 1;
        return (int)(Math.min(this.energyContainer.getEnergyCapacity()/32,VA[tier]));

    }

    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 9, 9, "", this::decrementThrottle)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("gregtech.multiblock.parr.throttle_decrement"));
        group.addWidget(new ClickButtonWidget(9, 0, 9, 9, "", this::incrementThrottle)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("gregtech.multiblock.parr.throttle_increment"));

        group.addWidget(new ClickButtonWidget(0, 9, 9, 9, "", this::decrementThrottle1)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("gregtech.multiblock.hand.throttle_decrement"));
        group.addWidget(new ClickButtonWidget(9, 9, 9, 9, "", this::incrementThrottle1)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("gregtech.multiblock.hand.throttle_increment"));
        return group;
    }
    private void incrementThrottle(Widget.ClickData clickData) {
        if(ParallelLim<16) this.ParallelNumA = MathHelper.clamp(ParallelNumA +1, 1, ParallelLim);
        this.ParallelNumA = MathHelper.clamp(ParallelNumA + ParallelLim/16, 1, ParallelLim);
    }

    private void decrementThrottle(Widget.ClickData clickData) {
        if(ParallelLim<16) this.ParallelNumA = MathHelper.clamp(ParallelNumA -1, 1, ParallelLim);
        this.ParallelNumA = MathHelper.clamp(ParallelNumA - ParallelLim/16, 1, ParallelLim);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("modern", modern);
        data.setInteger("ParallelNumA", ParallelNumA);
        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        modern = data.getInteger("modern");
        ParallelNumA = data.getInteger("ParallelNumA");
    }
    private void incrementThrottle1(Widget.ClickData clickData) {
        this.modern = MathHelper.clamp(modern + 1, 0, 1);
    }

    private void decrementThrottle1(Widget.ClickData clickData) {
        this.modern = MathHelper.clamp(modern - 1, 0, 1);
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtqt.tooltip.update")};
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBioCentrifuge(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", " P P ", " P P ", " P P ", "XXXXX")
                .aisle("XXXXX", "PPGPP", "PPGPP", "PPGPP", "XXXXX")
                .aisle("XGGGX", " F F ", " FcF ", " F F ", "XGGGX")
                .aisle("XGGGX", " F F ", " FcF ", " F F ", "XGGGX")
                .aisle("XGGGX", " F F ", " FcF ", " F F ", "XGGGX")
                .aisle("XGGGX", " F F ", " FcF ", " F F ", "XGGGX")
                .aisle("XXXXX", "PPGPP", "PPGPP", "PPGPP", "XXXXX")
                .aisle("XXXXX", " P P ", " PSP ", " P P ", "XXXXX")
                .where('S', selfPredicate())

                .where('P', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE)))
                .where('F', TiredTraceabilityPredicate.CP_LGLASS)
                .where('G', TiredTraceabilityPredicate.CP_ZJ_CASING)
                .where('c', TiredTraceabilityPredicate.CP_TUBE)
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(2))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1).setPreviewCount(1)))
                .build();
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        Object glass_tier = context.get("LGLTiredStats");
        Object clean_tier = context.get("ZJTiredStats");
        Object tubeTier = context.get("ChemicalPlantTubeTiredStats");
        this.tubeTier = GTQTUtil.getOrDefault(() -> tubeTier instanceof WrappedIntTired,
                () -> ((WrappedIntTired)tubeTier).getIntTier(),
                0);
        this.glass_tier = GTQTUtil.getOrDefault(() -> glass_tier instanceof WrappedIntTired,
                () -> ((WrappedIntTired)glass_tier).getIntTier(),
                0);
        this.clean_tier = GTQTUtil.getOrDefault(() -> clean_tier instanceof WrappedIntTired,
                () -> ((WrappedIntTired)clean_tier).getIntTier(),
                0);

        ParallelLim=(int)Math.pow(2, Math.max(this.tubeTier-4,1));
        ParallelNum=ParallelLim;
        tier=Math.min(this.glass_tier, this.tubeTier*2);
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("Glass:%s Tube:%s Clean:%s",glass_tier,tubeTier,clean_tier));
        if(modern==0) textList.add(new TextComponentTranslation("gtqtcore.tire1",tier));
        if(modern==1) textList.add(new TextComponentTranslation("gtqtcore.tire2",tier));
        textList.add(new TextComponentTranslation("gtqtcore.parr",ParallelNum,ParallelLim));
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FUSION_REACTOR_OVERLAY;
    }
}