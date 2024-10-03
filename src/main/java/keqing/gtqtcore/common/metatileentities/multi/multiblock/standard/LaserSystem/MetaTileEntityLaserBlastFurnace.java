package keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.LaserSystem;

import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;
import keqing.gtqtcore.api.capability.ILaser;
import keqing.gtqtcore.api.capability.impl.MultiblockLaserRecipeLogic;
import keqing.gtqtcore.api.metaileentity.multiblock.GTQTMultiblockAbility;
import keqing.gtqtcore.api.metaileentity.multiblock.RecipeMapLaserMultiblockController;
import keqing.gtqtcore.api.unification.GTQTMaterials;
import keqing.gtqtcore.client.textures.GTQTTextures;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static keqing.gtqtcore.common.block.blocks.GTQTTurbineCasing.TurbineCasingType.NQ_MACHINE_CASING;
import static keqing.gtqtcore.common.block.blocks.GTQTTurbineCasing.TurbineCasingType.NQ_TURBINE_CASING;
import static keqing.gtqtcore.common.metatileentities.GTQTMetaTileEntities.LASER_BLAST_FURNACE;
import static keqing.gtqtcore.common.metatileentities.GTQTMetaTileEntities.LASER_INPUT;

public class MetaTileEntityLaserBlastFurnace extends RecipeMapLaserMultiblockController implements IHeatingCoil {

    private int blastFurnaceTemperature=0;
    private int simBlastFurnaceTemperature;
    public MetaTileEntityLaserBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.BLAST_RECIPES);
        this.recipeMapWorkable = new MultiblockLaserRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserBlastFurnace(metaTileEntityId);
    }
    /////////////////////////*//////////////////////////////
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if (LaserToEu() > 0 && blastFurnaceTemperature < Math.min(simBlastFurnaceTemperature, getTemp())) {
                blastFurnaceTemperature += getTier() * inputNum;
            }
            if (LaserToEu() == 0 && blastFurnaceTemperature > 0) {
                blastFurnaceTemperature -= blastFurnaceTemperature / 10;
            }
        }
    }
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("blastFurnaceTemperature", this.blastFurnaceTemperature);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.blastFurnaceTemperature = data.getInteger("blastFurnaceTemperature");
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(tl -> {
                    // Coil heat capacity line
                    if (isStructureFormed()) {
                        ITextComponent heatString = TextComponentUtil.stringWithColor(
                                TextFormatting.RED,
                                TextFormattingUtil.formatNumbers(blastFurnaceTemperature) + "K");
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.blast_furnace.max_temperature",
                                heatString));
                    }
                });
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        blastFurnaceTemperature=0;

        if (type instanceof IHeatingCoilBlockStats) {
            this.simBlastFurnaceTemperature = ((IHeatingCoilBlockStats) type).getCoilTemperature();
        } else {
            this.simBlastFurnaceTemperature = CoilType.CUPRONICKEL.getCoilTemperature();
        }
        // the subtracted tier gives the starting level (exclusive) of the +100K heat bonus
        this.simBlastFurnaceTemperature += 100 *
                Math.max(0, GTUtility.getFloorTierByVoltage(getTier() - GTValues.MV));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.simBlastFurnaceTemperature = 0;
        this.blastFurnaceTemperature = 0;
    }

    @Override
    public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
        return this.blastFurnaceTemperature >= recipe.getProperty(TemperatureProperty.getInstance(), 0);
    }
    /////////////////////////*//////////////////////////////
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  GGG  ", "  AAA  ", "   A   ", "       ", "       ", "       ", "       ", "       ", "   A   ", "  BBB  ", "  AAA  ")
                .aisle(" GAAAG ", " AAAAA ", " CADAC ", " CA AC ", " C   C ", " C   C ", " C   C ", " CA AC ", " CADAC ", " BAAAB ", " AAAAA ")
                .aisle("GAAAAAG", "AAEEEAA", " AFFFA ", " AEDEA ", "  EDE  ", "  EDE  ", "  EDE  ", " AEDEA ", " AFFFA ", "BAEEEAB", "AAAAAAA")
                .aisle("GAAAAAG", "AAEEEAA", "ADFFFDA", "  DFD  ", "  DFD  ", "  DFD  ", "  DFD  ", "  DFD  ", "ADFFFDA", "BAEEEAB", "AAAPAAA")
                .aisle("GAAAAAG", "AAEEEAA", " AFFFA ", " AEDEA ", "  EDE  ", "  EDE  ", "  EDE  ", " AEDEA ", " AFFFA ", "BAEEEAB", "AAAAAAA")
                .aisle(" GAAAG ", " AAAAA ", " CADAC ", " CA AC ", " C   C ", " C   C ", " C   C ", " CA AC ", " CADAC ", " BAAAB ", " AAAAA ")
                .aisle("  GGG  ", "  ASA  ", "   Q   ", "       ", "       ", "       ", "       ", "       ", "   A   ", "  BBB  ", "  AAA  ")

                .where('S', selfPredicate())
                .where('A', states(getCasingState()))
                .where('G', states(getCasingState()).setMinGlobalLimited(9)
                        .or(autoAbilities(false, false, true, true, true, true, false))
                        .or(abilities(GTQTMultiblockAbility.LASER_INPUT).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                )
                .where('B', states(getVentState()))//散热片
                .where('C', states(getFrameState()))//框架
                .where('D', states(getGlassState()))//玻璃
                .where('E', states(getSecondCasingState()))//管道
                .where('F', heatingCoils())//线圈
                .where('P', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('Q', abilities(MultiblockAbility.MAINTENANCE_HATCH))
                .where(' ', any())
                .build();
    }
    @Nonnull
    private static IBlockState getVentState() {
        return GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT);
    }
    private static IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(GTQTMaterials.MaragingSteel250).getBlock(GTQTMaterials.MaragingSteel250);
    }
    private static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }
    private static IBlockState getCasingState() {
        return GTQTMetaBlocks.TURBINE_CASING.getState(NQ_TURBINE_CASING);
    }

    private static IBlockState getSecondCasingState() {
        return GTQTMetaBlocks.TURBINE_CASING.getState(NQ_MACHINE_CASING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return GTQTTextures.NQ_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("实际线圈温度为 激光最大换热温度温度 与 线圈理论 温度的最大值，初次工作由0开始增长至最大值"));
    }

    @Override
    public int getCurrentTemperature() {
        return this.blastFurnaceTemperature;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("  GGG  ", "  AAA  ", "   A   ", "       ", "       ", "       ", "       ", "       ", "   A   ", "  BBB  ", "  AAA  ")
                .aisle(" GAAAG ", " AAAAA ", " CADAC ", " CA AC ", " C   C ", " C   C ", " C   C ", " CA AC ", " CADAC ", " BAAAB ", " AAAAA ")
                .aisle("GAAAAAG", "AAEEEAA", " AFFFA ", " AEDEA ", "  EDE  ", "  EDE  ", "  EDE  ", " AEDEA ", " AFFFA ", "BAEEEAB", "AAAAAAA")
                .aisle("KAAAAAI", "AAEEEAA", "ADFFFDA", "  DFD  ", "  DFD  ", "  DFD  ", "  DFD  ", "  DFD  ", "ADFFFDA", "BAEEEAB", "AAAPAAA")
                .aisle("LAAAAAJ", "AAEEEAA", " AFFFA ", " AEDEA ", "  EDE  ", "  EDE  ", "  EDE  ", " AEDEA ", " AFFFA ", "BAEEEAB", "AAAAAAA")
                .aisle(" GAAAG ", " AAAAA ", " CADAC ", " CA AC ", " C   C ", " C   C ", " C   C ", " CA AC ", " CADAC ", " BAAAB ", " AAAAA ")
                .aisle("  HGH  ", "  ASA  ", "   Q   ", "       ", "       ", "       ", "       ", "       ", "   A   ", "  BBB  ", "  AAA  ")
                .where(' ', Blocks.AIR.getDefaultState())

                .where('S', LASER_BLAST_FURNACE, EnumFacing.SOUTH)

                .where('A', getCasingState())
                .where('B', getVentState())//散热片
                .where('C', getFrameState())//框架
                .where('D', getGlassState())//玻璃
                .where('E', getSecondCasingState())//管道
                .where('G', getCasingState())

                .where('H', LASER_INPUT[GTValues.LV], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.EAST)
                .where('J', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.EAST)
                .where('K', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.WEST)
                .where('L', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.WEST)

                .where('P', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP)
                .where('Q', MetaTileEntities.MAINTENANCE_HATCH, EnumFacing.SOUTH);

        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('F', entry.getKey()).build()));
        return shapeInfo;
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(blastFurnaceTemperature) + "K")
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        return list;
    }
}