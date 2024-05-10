package keqing.gtqtcore.common.block.blocks;


import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class GTQTParticleAccelerator extends VariantBlock<GTQTParticleAccelerator.MachineType> {

    public GTQTParticleAccelerator() {
        super(Material.IRON);
        setTranslationKey("particle_accelerator");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(MachineType.GRANULAR_SOURCE_A));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum MachineType implements IStringSerializable, IStateHarvestLevel {
        //粒子源 三个
        //探针 三级
        //加速器外壳 预留三级
        //加速器束流器 预留三级

        GRANULAR_SOURCE_A("granular_source_a",3),
        GRANULAR_SOURCE_B("granular_source_b",3),
        GRANULAR_SOURCE_C("granular_source_c",3);

        private final String name;
        private final int harvestLevel;

        MachineType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return ToolClasses.WRENCH;
        }
    }

}

