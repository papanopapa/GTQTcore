package keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.LaserSystem;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqtcore.api.capability.ILaser;
import keqing.gtqtcore.api.utils.GTQTLog;
import keqing.gtqtcore.client.particle.LaserBeamParticle;
import keqing.gtqtcore.client.textures.GTQTTextures;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.MetaTileEntityBaseWithControl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.V;
import static gregtech.api.GTValues.VN;
import static keqing.gtqtcore.api.metaileentity.multiblock.GTQTMultiblockAbility.LASER_INPUT;
import static keqing.gtqtcore.api.metaileentity.multiblock.GTQTMultiblockAbility.LASER_OUTPUT;
import static keqing.gtqtcore.api.unification.GTQTMaterials.MaragingSteel250;
import static keqing.gtqtcore.common.block.blocks.GTQTTurbineCasing.TurbineCasingType.NQ_TURBINE_CASING;

public class MetaTileEntitySBPRO extends MetaTileEntityBaseWithControl {

    private long Laser;//当前的激光
    private long More;
    private long Cost;
    private int length;
    private int circuit;
    int[][] io = new int[6][2];
    long[] La = new long[6];
    long[] Se = new long[6];
    long[] Ma = new long[6];

    public MetaTileEntitySBPRO(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    BlockPos TargetPos;
    boolean findTarget = false;
    public void setMachinePos(BlockPos pos)
    {
        findTarget=true;
        TargetPos=pos;
    }
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("length", length);
        data.setLong("Laser", Laser);
        data.setLong("Cost", Cost);
        data.setLong("More", More);
        data.setInteger("circuit", circuit);
        for (int i = 0; i < 6; i++) {
            data.setLong("La" + i, La[i]);
            data.setLong("Se" + i, Se[i]);
            data.setLong("Ma" + i, Ma[i]);
        }
        if(TargetPos!=null) {
            data.setInteger("X1", TargetPos.getX());
            data.setInteger("Y1", TargetPos.getY());
            data.setInteger("Z1", TargetPos.getZ());
        }
        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        length = data.getInteger("length");
        Laser = data.getLong("Laser");
        Cost = data.getLong("Cost");
        More = data.getLong("More");
        circuit = data.getInteger("circuit");
        for (int i = 0; i < 6; i++) {
            La[i] = data.getLong("La" + i);
            Se[i] = data.getLong("Se" + i);
            Ma[i] = data.getLong("Ma" + i);
        }
        TargetPos= new BlockPos(data.getInteger("X1"), data.getInteger("Y1"), data.getInteger("Z1"));
    }

    @Override
    protected void updateFormedValid() {
        MetaTileEntity mte = GTUtility.getMetaTileEntity(this.getWorld(), TargetPos);
        if (mte instanceof MetaTileEntitySBPRC && ((MetaTileEntitySBPRC) mte).isStructureFormed()) {
            findTarget=true;
            writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
        }
        else
        {
            findTarget=false;
            TargetPos=null;
        }

        long c = 0;
        for (int p = 0; p < length; p++) {
            c += this.getAbilities(LASER_OUTPUT).get(p).Laser();
            io[p][1] = this.getAbilities(LASER_OUTPUT).get(p).tier();
            La[p] = this.getAbilities(LASER_OUTPUT).get(p).Laser();
            Se[p] = this.getAbilities(LASER_OUTPUT).get(p).SetLaser();
            Ma[p] = this.getAbilities(LASER_OUTPUT).get(p).MaxLaser();
            this.getAbilities(LASER_OUTPUT).get(p).setMachinePos(this.getPos());

        }

        if (Cost != c) Cost = c;

        More = Laser - Cost;
        for (int p = 0; p < length; p++) {
            if (Se[p] > La[p] && More > 0) {
                if (More > Se[p] - La[p]) {
                    this.getAbilities(LASER_OUTPUT).get(p).setLaser(Se[p]);

                } else {
                    this.getAbilities(LASER_OUTPUT).get(p).setLaser(La[p] + More);

                }
            }
        }
        if (More < 0) {
            for (int p = 0; p < length; p++) {
                if(More>=0)return;
                if (Math.abs(More)  <= La[p]) {
                    this.getAbilities(LASER_OUTPUT).get(p).setLaser(La[p] + More);
                    More = 0;
                    return;
                } else {
                    More += La[p];
                    this.getAbilities(LASER_OUTPUT).get(p).setLaser(0);
                }
            }
        }
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 300, 240);

        // Display
        builder.image(132, 4, 162, 115, GuiTextures.DISPLAY);
        builder.dynamicLabel(135, 8, () -> "高能激光离散控制器", 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(135, 20, this::addDisplayText, 16777215)).setMaxWidthLimit(160).setClickHandler(this::handleDisplayClick));

        int j = 0;
        //1号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo1, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 0));
        j++;
        //2号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo2, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 1));
        j++;
        //3号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo3, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 2));
        j++;
        //4号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo4, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 3));
        j++;
        //5号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo5, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 4));
        j++;
        //6号
        builder.image(3, 4 + j * 30, 130, 30, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 8 + j * 30, this::addInfo6, 16777215)).setMaxWidthLimit(120).setClickHandler(this::handleDisplayClick));
        builder.widget(new ClickButtonWidget(118, 4 + j * 30, 15, 30, "->", data -> circuit = 5));


        // Display
        builder.image(3, 184, 130, 52, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(7, 188, this::addTotal, 16777215)).setMaxWidthLimit(130).setClickHandler(this::handleDisplayClick));


        builder.widget(new ClickButtonWidget(132, 120, 60, 18, "V +1", this::incrementThresholdV));
        builder.widget(new ClickButtonWidget(200, 120, 60, 18, "V -1", this::decrementThresholdV));
        builder.widget(new ClickButtonWidget(132, 140, 60, 18, "A +1", this::incrementThresholdA));
        builder.widget(new ClickButtonWidget(200, 140, 60, 18, "A -1", this::decrementThresholdA));


        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 132, 160);
        return builder;
    }

    protected void addInfo1(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[0][0] == 0) return;
                if (io[0][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[0][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[0], VN[GTUtility.getTierByVoltage(La[0])]));
            }
        });
    }

    protected void addInfo2(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[1][0] == 0) return;
                if (io[1][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[1][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[1], VN[GTUtility.getTierByVoltage(La[1])]));
            }
        });
    }

    protected void addInfo3(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[2][0] == 0) return;
                if (io[2][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[2][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[2], VN[GTUtility.getTierByVoltage(La[2])]));
            }
        });
    }

    protected void addInfo4(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[3][0] == 0) return;
                if (io[3][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[3][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[3], VN[GTUtility.getTierByVoltage(La[3])]));
            }
        });
    }

    protected void addInfo5(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[4][0] == 0) return;
                if (io[4][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[4][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[4], VN[GTUtility.getTierByVoltage(La[4])]));
            }
        });
    }

    protected void addInfo6(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed()).addCustom(tl -> {
            if (isStructureFormed()) {
                if (io[5][0] == 0) return;
                if (io[5][0] == 2)
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[5][1]));
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光：%s %s", La[5], VN[GTUtility.getTierByVoltage(La[5])]));
            }
        });
    }

    private void incrementThresholdV(Widget.ClickData clickData) {
        if (circuit < length) {
            if (More >= this.getAbilities(LASER_OUTPUT).get(circuit).Voltage() * 4L) {
                this.getAbilities(LASER_OUTPUT).get(circuit).addVoltage(1);
                this.getAbilities(LASER_OUTPUT).get(circuit).setLaser(this.getAbilities(LASER_OUTPUT).get(circuit).Amperage() * V[this.getAbilities(LASER_OUTPUT).get(circuit).Voltage()]);
            }
        }
    }

    private void decrementThresholdV(Widget.ClickData clickData) {
        if (circuit < length) this.getAbilities(LASER_OUTPUT).get(circuit).addVoltage(-1);
    }

    private void incrementThresholdA(Widget.ClickData clickData) {
        if (circuit < length) {
            if (More >= this.getAbilities(LASER_OUTPUT).get(circuit).Voltage()) {
                this.getAbilities(LASER_OUTPUT).get(circuit).addAmperage(1);
                this.getAbilities(LASER_OUTPUT).get(circuit).setLaser(this.getAbilities(LASER_OUTPUT).get(circuit).Amperage() * V[this.getAbilities(LASER_OUTPUT).get(circuit).Voltage()]);
            }
        }
    }

    private void decrementThresholdA(Widget.ClickData clickData) {
        if (circuit < length)
            this.getAbilities(LASER_OUTPUT).get(circuit).addAmperage(-1);
    }

    protected void addTotal(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("输入激光能量:%s", Laser));
        textList.add(new TextComponentTranslation("输出激光能量:%s", Cost));
        textList.add(new TextComponentTranslation("盈余激光能量:%s", More));
        textList.add(new TextComponentTranslation("激光输出IO数量：%s", length));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("控制器绑定状态:%s", findTarget));
        textList.add(new TextComponentTranslation("仓号:%s", circuit + 1));
        if (io[circuit][0] == 2)
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "激光输出-等级：%s", io[circuit][1]));
        if (circuit < length)
            textList.add(new TextComponentTranslation("电压：%s 电流：%s", this.getAbilities(LASER_OUTPUT).get(circuit).Voltage(), this.getAbilities(LASER_OUTPUT).get(circuit).Amperage()));
        textList.add(new TextComponentTranslation("激光：%s %s", La[circuit], VN[GTUtility.getTierByVoltage(La[circuit])]));
        textList.add(new TextComponentTranslation("阈值：%s %s", Se[circuit], VN[GTUtility.getTierByVoltage(Se[circuit])]));
        textList.add(new TextComponentTranslation("极值：%s %s", Ma[circuit], VN[GTUtility.getTierByVoltage(Ma[circuit])]));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<ILaser> i = getAbilities(LASER_OUTPUT);
        this.length = i.size();
        int p;
        for (p = 0; p < length; p++) {
            io[p][0] = 2;
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" AAA ", "  A  ", "  A  ", "     ", "     ")
                .aisle("AAAAA", " AAA ", " BAB ", " B B ", " B B ")
                .aisle("AAAAA", "AAAAA", "AAAAA", "     ", "     ")
                .aisle("AAAAA", " AAA ", " BAB ", " B B ", " B B ")
                .aisle(" AAA ", "  S  ", "  A  ", "     ", "     ")
                .where('S', selfPredicate())
                .where('A', states(getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(LASER_OUTPUT).setMinGlobalLimited(1).setMaxGlobalLimited(6)))
                .where('B', frames(MaragingSteel250))
                .where(' ', any())
                .build();
    }


    private static IBlockState getCasingState() {
        return GTQTMetaBlocks.TURBINE_CASING.getState(NQ_TURBINE_CASING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GTQTTextures.NQ_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return GTQTTextures.CVD_UNIT_OVERLAY;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySBPRO(metaTileEntityId);
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.emptyList();
    }

    public void setLaser(long amount) {
        this.Laser=amount;
    }

    public int getLaser() {
        return (int) Laser;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("使用 坐标绑定卡 绑定即可链接至棱镜网络"));
        tooltip.add(I18n.format("最多安装六个IO设备"));
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeParticles(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            readParticles(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            try {
                readParticles(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    private void writeParticles(PacketBuffer buf) {
        NBTTagCompound tag = new NBTTagCompound();
        if(findTarget)
        {
            tag.setInteger("Sx",this.getPos().getX());
            tag.setInteger("Sy",this.getPos().getY());
            tag.setInteger("Sz",this.getPos().getZ());
            tag.setInteger("Ex",TargetPos.getX());
            tag.setInteger("Ey",TargetPos.getY());
            tag.setInteger("Ez",TargetPos.getZ());

        }
        buf.writeCompoundTag(tag);
    }
    @SideOnly(Side.CLIENT)
    private void readParticles( PacketBuffer buf) throws IOException {
        NBTTagCompound tag = buf.readCompoundTag();
        if(tag.hasKey("Ex"))
        {
            BlockPos Spos = new BlockPos(tag.getInteger("Sx"),tag.getInteger("Sy"),tag.getInteger("Sz"));
            BlockPos Epos = new BlockPos(tag.getInteger("Ex"),tag.getInteger("Ey"),tag.getInteger("Ez"));
            operateClient(Spos,Epos,20);
        }

    }
    @SideOnly(Side.CLIENT)
    public void operateClient(BlockPos Spos,BlockPos Epos,int age) {
        GTParticleManager.INSTANCE.addEffect(new LaserBeamParticle(this,Spos,Epos.add(0,150,0),age));
    }
}
