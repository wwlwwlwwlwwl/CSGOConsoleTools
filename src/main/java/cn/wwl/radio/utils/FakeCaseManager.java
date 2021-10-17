package cn.wwl.radio.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakeCaseManager {

    public static CSGOCaseDrop openFakeCase(CSGOCases cases) {
        if (cases == null) {
            return CSGOCaseDrop.ERROR_ITEM;
        }

        Random random = new Random();
        float seed = random.nextFloat(100);
        Rarity rarity = Rarity.Rare;
        if (seed <= 20.077) {
            rarity = Rarity.Mythical;
            if (seed <= 4.092) {
                rarity = Rarity.Legendary;
                if (seed <= 0.895) {
                    rarity = Rarity.Immortal;
                    if (seed <= 0.256) {
                        rarity = Rarity.Contraband;
                    }
                }
            }
        }

        List<CSGOItem> rarityItems = cases.getItemAsRarity(rarity);
        if (rarityItems.size() == 0) {
            if (rarity == Rarity.Contraband) {
                return new CSGOCaseDrop(CSGOItem.KNIFE);
            }
            return CSGOCaseDrop.ERROR_ITEM;
        }

        return new CSGOCaseDrop(rarityItems.get(random.nextInt(rarityItems.size() - 1)));
    }

    public static class CSGOCaseDrop {
        public static final CSGOCaseDrop ERROR_ITEM = new CSGOCaseDrop(CSGOItem.ERROR,0.0F);

        private static Random random = new Random();
        private final CSGOItem caseItem;
        private final float skinFloat;

        public CSGOCaseDrop(CSGOItem item) {
            this.caseItem = item;
            this.skinFloat = random.nextFloat(item.getMinFloat(),item.getMaxFloat());
        }

        public CSGOCaseDrop(CSGOItem item, float skinFloat) {
            this.caseItem = item;
            this.skinFloat = skinFloat;
        }

        public CSGOItem getCaseItem() {
            return caseItem;
        }

        public float getSkinFloat() {
            return skinFloat;
        }

        public Rarity getSkinRarity() {
            return caseItem.getRarity();
        }

        public String getSkinName() {
            return caseItem.getName();
        }

        public String getColorSkinName() {
            String color = "";
            switch (getSkinRarity()) {
                case Common -> color = TextMarker.灰色.getHumanCode();
                case Uncommon -> color = TextMarker.灰蓝色.getHumanCode();
                case Rare -> color = TextMarker.蓝色.getHumanCode();
                case Mythical,Legendary -> color = TextMarker.紫色.getHumanCode();
                case Immortal -> color = TextMarker.淡红色.getHumanCode();
                case Contraband -> color = TextMarker.金色.getHumanCode();
            }
            return color + getSkinName();
        }

        @Override
        public String toString() {
            return "Skin: " + getSkinName() + " ,Float: " + getSkinFloat();
        }
    }

    public static class CSGOItem {
        public static final CSGOItem KNIFE = create(CSGOWeapons.CUSTOM,"蝴蝶刀 (★) | 伽马多普勒",EasyCodeRarity.红色 ,0.08);
        public static final CSGOItem ERROR = create(CSGOWeapons.CUSTOM,"参数有误",EasyCodeRarity.白色);

        private final CSGOWeapons weaponName;
        private final String skinName;
        private final Rarity rarity;
        private float minFloat;
        private float maxFloat;


        public CSGOItem(CSGOWeapons weaponName,String skinName,Rarity rarity,float minFloat,float maxFloat) {
            this.weaponName = weaponName;
            this.skinName = skinName;
            this.rarity = rarity;
            this.minFloat = minFloat;
            this.maxFloat = maxFloat;
        }

        public String getSkinName() {
            return skinName;
        }

        public String getWeaponName() {
            return weaponName == CSGOWeapons.CUSTOM ? skinName : weaponName.getRealName();
        }

        public String getName() {
            return weaponName == CSGOWeapons.CUSTOM ? skinName : weaponName + " | " + skinName;
        }

        public Rarity getRarity() {
            return rarity;
        }

        public float getMinFloat() {
            return minFloat;
        }

        public float getMaxFloat() {
            return maxFloat;
        }

        public static CSGOItem create(CSGOWeapons weapons, String skinName, Rarity rarity) {
            return new CSGOItem(weapons,skinName,rarity,0F,1F);
        }

        public static CSGOItem create(CSGOWeapons weapons,String skinName,EasyCodeRarity rarity) {
            return new CSGOItem(weapons,skinName,rarity.getRarity(),0F,1F);
        }

        public static CSGOItem create(CSGOWeapons weapons, String skinName, Rarity rarity,double maxFloat) {
            return new CSGOItem(weapons,skinName,rarity,0,(float)maxFloat);
        }

        public static CSGOItem create(CSGOWeapons weapons,String skinName,EasyCodeRarity rarity,double maxFloat) {
            return new CSGOItem(weapons,skinName,rarity.getRarity(),0,(float)maxFloat);
        }

        public static CSGOItem create(CSGOWeapons weapons, String skinName, Rarity rarity,double minFloat,double maxFloat) {
            return new CSGOItem(weapons,skinName,rarity,(float)minFloat,(float)maxFloat);
        }

        public static CSGOItem create(CSGOWeapons weapons,String skinName,EasyCodeRarity rarity,double minFloat,double maxFloat) {
            return new CSGOItem(weapons,skinName,rarity.getRarity(),(float)minFloat,(float)maxFloat);
        }
    }


    public enum CSGOCases {
        DANGER_ZONE_CASE("头号特训",
                CSGOItem.create(CSGOWeapons.MP9,"中度威胁",EasyCodeRarity.蓝色,0.75),
                CSGOItem.create(CSGOWeapons.GLOCK,"锈蚀烈焰",EasyCodeRarity.蓝色,0.85),
                CSGOItem.create(CSGOWeapons.NOVA,"灼木",EasyCodeRarity.蓝色,0.75),
                CSGOItem.create(CSGOWeapons.M4A4,"镁元素",EasyCodeRarity.蓝色),
                CSGOItem.create(CSGOWeapons.SAWED_OFF,"黑砂",EasyCodeRarity.蓝色,0.9),
                CSGOItem.create(CSGOWeapons.SG553,"危险距离",EasyCodeRarity.蓝色,0.02,0.8),
                CSGOItem.create(CSGOWeapons.TEC9,"废铜烂铁",EasyCodeRarity.蓝色,0.14,1),

                CSGOItem.create(CSGOWeapons.G3SG1,"净化者",EasyCodeRarity.紫色,0.65),
                CSGOItem.create(CSGOWeapons.GRLIL,"信号灯",EasyCodeRarity.紫色,0.5),
                CSGOItem.create(CSGOWeapons.MAC10,"销声",EasyCodeRarity.紫色,0.9),
                CSGOItem.create(CSGOWeapons.P250,"影魔",EasyCodeRarity.紫色,0.4),
                CSGOItem.create(CSGOWeapons.USP,"闪回",EasyCodeRarity.紫色,0.5),

                CSGOItem.create(CSGOWeapons.UMP45,"动量",EasyCodeRarity.粉色,0.5),
                CSGOItem.create(CSGOWeapons.DEAGLE,"机械工业",EasyCodeRarity.粉色,0.6),
                CSGOItem.create(CSGOWeapons.MP5,"磷光体",EasyCodeRarity.粉色,0.8),

                CSGOItem.create(CSGOWeapons.AK47,"二西莫夫",EasyCodeRarity.红色,0.05,0.7),
                CSGOItem.create(CSGOWeapons.AWP,"黑色魅影",EasyCodeRarity.红色,0.5)
        ),
        OPERATION_RIPTIDE_CASE("激流大行动",
                CSGOItem.create(CSGOWeapons.AUG,"瘟疫",EasyCodeRarity.蓝色,0.76),
                CSGOItem.create(CSGOWeapons.DUAL_BERETTAS,"胶面花纹",EasyCodeRarity.蓝色),
                CSGOItem.create(CSGOWeapons.G3SG1,"特训地图",EasyCodeRarity.蓝色,0.7),
                CSGOItem.create(CSGOWeapons.MP7,"游击队",EasyCodeRarity.蓝色),
                CSGOItem.create(CSGOWeapons.PP_BIZON,"特训手电",EasyCodeRarity.蓝色,0.77),
                CSGOItem.create(CSGOWeapons.USP,"蓝莲花",EasyCodeRarity.蓝色,0.9),
                CSGOItem.create(CSGOWeapons.XM1014,"狻猊",EasyCodeRarity.蓝色),

                CSGOItem.create(CSGOWeapons.MAG7,"秘晶体",EasyCodeRarity.紫色,0.6),
                CSGOItem.create(CSGOWeapons.FAMAS,"ZX81彩色",EasyCodeRarity.紫色),
                CSGOItem.create(CSGOWeapons.FN57,"同步立场",EasyCodeRarity.紫色,0.41),
                CSGOItem.create(CSGOWeapons.MP9,"富士山",EasyCodeRarity.紫色,0.55),
                CSGOItem.create(CSGOWeapons.M4A4,"彼岸花",EasyCodeRarity.紫色),

                CSGOItem.create(CSGOWeapons.MAC10,"玩具盒子",EasyCodeRarity.粉色),
                CSGOItem.create(CSGOWeapons.GLOCK,"零食派对",EasyCodeRarity.粉色),
                CSGOItem.create(CSGOWeapons.SSG08,"速度激情",EasyCodeRarity.粉色,0.6),

                CSGOItem.create(CSGOWeapons.DEAGLE,"纵横波涛",EasyCodeRarity.红色),
                CSGOItem.create(CSGOWeapons.AK47,"抽象派1337",EasyCodeRarity.红色,0.65)
                );


        private final String caseName;
        private final List<CSGOItem> items;

        CSGOCases(String caseName,CSGOItem... items) {
            this.caseName = caseName;
            this.items = List.of(items);
        }

        public String getCaseName() {
            return caseName;
        }

        public List<CSGOItem> getItems() {
            return new ArrayList<>(items);
        }

        public List<CSGOItem> getItemAsRarity(Rarity rarity) {
            return this.items.stream().filter(item -> item.getRarity() == rarity).toList();
        }
    }

    public enum CSGOWeapons {
        P2000("P2000"),
        USP("USP 消音版"),
        GLOCK("格洛克18型"),
        P250("P250"),
        FN57("FN57"),
        CZ75("CZ75 自动手枪"),
        DEAGLE("沙漠之鹰"),
        TEC9("Tec-9"),
        R8("R8 左轮手枪"),
        DUAL_BERETTAS("双持贝瑞塔"),

        NOVA("新星"),
        XM1014("XM1014"),
        MAG7("MAG-7"),
        SAWED_OFF("截短霰弹枪"),
        M249("M249"),
        NEGEV("Negev"),

        MAC10("MAC-10"),
        MP9("MP9"),
        MP7("MP7"),
        UMP45("UMP-45"),
        PP_BIZON("PP-野牛"),
        P90("P90"),
        MP5("MP5-SD"),

        FAMAS("法玛斯"),
        GRLIL("加利尔 AR"),
        M4A4("M4A4"),
        M4A1("M4A1 消音版"),
        AK47("AK-47"),
        AWP("AWP"),
        AUG("AUG"),
        SG553("SG 553"),
        SSG08("SSG 08"),
        SCAR20("SCAR-20"),
        G3SG1("G3SG1"),

        CUSTOM("");


        private final String realName;
        CSGOWeapons(String realName) {
            this.realName = realName;
        }

        public String getRealName() {
            return realName;
        }

        @Override
        public String toString() {
            return realName;
        }
    }

    /**
     * Sorry for this, but IDE is so bad working for Rarity names.
     */
    public enum EasyCodeRarity {
        白色(Rarity.Common),
        淡蓝(Rarity.Uncommon),
        蓝色(Rarity.Rare),
        紫色(Rarity.Mythical),
        粉色(Rarity.Legendary),
        红色(Rarity.Immortal),
        金色(Rarity.Contraband);


        private final Rarity rarity;
        EasyCodeRarity(Rarity rarity) {
            this.rarity = rarity;
        }

        public Rarity getRarity() {
            return rarity;
        }

        @Override
        public String toString() {
            return rarity.toString();
        }
    }

    public enum Rarity {
        Common("Consumer","消费级"),
        Uncommon("Industrial","工业级"),

        Rare("MilSpec","军规级"),
        Mythical("Restricted","受限"),
        Legendary("Covert","保密"),
        Immortal("Clandestine","隐秘"),
        Contraband("Contraband","违禁");

        private final String gameName;
        private final String chineseName;

        Rarity(String gameName,String chineseName) {
            this.gameName = gameName;
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getGameName() {
            return gameName;
        }

        public int getRarityLevel() {
            switch (this) {
                case Common -> {
                    return 0;
                }
                case Uncommon -> {
                    return 1;
                }
                case Rare -> {
                    return 2;
                }
                case Mythical -> {
                    return 3;
                }
                case Legendary -> {
                    return 4;
                }
                case Immortal -> {
                    return 5;
                }
                case Contraband -> {
                    return 6;
                }
                default -> {
                    return -1;
                }
            }
        }

        @Override
        public String toString() {
            return chineseName;
        }
    }
}
