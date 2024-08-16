package keqing.gtqtcore.api.utils;

public class GTQTKQnetHelper {
    public static String getInfo(int kind) {

        //0 普通
        if (kind == 0) return"无需设备";
        //1 物理-透射电镜
        if (kind == 1) return"物理-透射电镜";
        //2 物理-粒子加速器
        if (kind == 2) return"物理-粒子加速器";
        //3 物理-量子计算机
        if (kind == 3) return"物理-量子计算机";

        //11 生物-DNA解析计算机
        if (kind == 11) return"生物-DNA解析计算机";
        //12 生物-基因测序计算机
        if (kind == 12) return"生物-基因测序计算机";

        //21 光学-质谱仪
        if (kind == 21) return"光学-质谱仪";

        //设备升级区
        //30 设备-辅助计算机I
        if (kind == 30) return"设备-辅助计算机I";
        //31 设备-辅助计算机II
        if (kind == 31) return"设备-辅助计算机II";
        //32 设备-辅助计算机III
        if (kind == 32) return"设备-辅助计算机III";
        //33 设备-辅助数据库
        if (kind == 33) return"设备-辅助数据库I";
        //34 设备-辅助数据库
        if (kind == 34) return"设备-辅助数据库II";
        return "null";
    }
}
