package keqing.gtqtcore.common.block.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class GTQTHCasing extends VariantBlock<GTQTHCasing.CasingType> {


    public GTQTHCasing() {
        super(Material.IRON);
        this.setTranslationKey("h_casing");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
    }

    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public static enum CasingType implements IStringSerializable {


        MACHINE_CASING_FUSION("machine_casing_fusion"),
        MACHINE_CASING_FUSION_2("machine_casing_fusion_2"),
        MACHINE_CASING_FUSION_3("machine_casing_fusion_3"),
        YELLOW_CASING("yellow_casing"),
        DARK_CASING("dark_casing"),
        RED_CASING("red_casing"),
        BLUE_CASING("blue_casing"),
        GREEN_CASING("green_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }
}