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

        private Random random;
        private final CSGOItem caseItem;
        private final float skinFloat;

        public CSGOCaseDrop(CSGOItem item) {
            this.caseItem = item;
            this.random = new Random();
            this.skinFloat = random.nextFloat(item.getMinFloat(),item.getMaxFloat());
        }

        public CSGOCaseDrop(CSGOItem item, float skinFloat) {
            this.caseItem = item;
            this.random = new Random();
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
                case Common -> color = TextMarker.Grey.getHumanCode();
                case Uncommon -> color = TextMarker.CTBlue.getHumanCode();
                case Rare -> color = TextMarker.Blue.getHumanCode();
                case Mythical,Legendary -> color = TextMarker.Purple.getHumanCode();
                case Immortal -> color = TextMarker.LightRed.getHumanCode();
                case Contraband -> color = TextMarker.Gold.getHumanCode();
            }
            return color + getSkinName();
        }

        @Override
        public String toString() {
            return "Skin: " + getSkinName() + " ,Float: " + getSkinFloat();
        }
    }

    public record CSGOItem(CSGOWeapons weaponName, String skinName,
                           Rarity rarity, float minFloat, float maxFloat) {
        public static final CSGOItem KNIFE = create(CSGOWeapons.CUSTOM, "????????? (???) | ???????????????", Rarity.Immortal, 0.08);
        public static final CSGOItem ERROR = create(CSGOWeapons.CUSTOM, "????????????", Rarity.Common);

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
            return new CSGOItem(weapons, skinName, rarity, 0F, 1F);
        }

        public static CSGOItem create(CSGOWeapons weapons, String skinName, Rarity rarity, double maxFloat) {
            return new CSGOItem(weapons, skinName, rarity, 0, (float) maxFloat);
        }

        public static CSGOItem create(CSGOWeapons weapons, String skinName, Rarity rarity, double minFloat, double maxFloat) {
            return new CSGOItem(weapons, skinName, rarity, (float) minFloat, (float) maxFloat);
        }
    }


    public enum CSGOCases {
        DANGER_ZONE_CASE("????????????",
                CSGOItem.create(CSGOWeapons.MP9,"????????????",Rarity.Rare,0.75),
                CSGOItem.create(CSGOWeapons.GLOCK,"????????????",Rarity.Rare,0.85),
                CSGOItem.create(CSGOWeapons.NOVA,"??????",Rarity.Rare,0.75),
                CSGOItem.create(CSGOWeapons.M4A4,"?????????",Rarity.Rare),
                CSGOItem.create(CSGOWeapons.SAWED_OFF,"??????",Rarity.Rare,0.9),
                CSGOItem.create(CSGOWeapons.SG553,"????????????",Rarity.Rare,0.02,0.8),
                CSGOItem.create(CSGOWeapons.TEC9,"????????????",Rarity.Rare,0.14,1),

                CSGOItem.create(CSGOWeapons.G3SG1,"?????????",Rarity.Mythical,0.65),
                CSGOItem.create(CSGOWeapons.GRLIL,"?????????",Rarity.Mythical,0.5),
                CSGOItem.create(CSGOWeapons.MAC10,"??????",Rarity.Mythical,0.9),
                CSGOItem.create(CSGOWeapons.P250,"??????",Rarity.Mythical,0.4),
                CSGOItem.create(CSGOWeapons.USP,"??????",Rarity.Mythical,0.5),

                CSGOItem.create(CSGOWeapons.UMP45,"??????",Rarity.Legendary,0.5),
                CSGOItem.create(CSGOWeapons.DEAGLE,"????????????",Rarity.Legendary,0.6),
                CSGOItem.create(CSGOWeapons.MP5,"?????????",Rarity.Legendary,0.8),

                CSGOItem.create(CSGOWeapons.AK47,"????????????",Rarity.Immortal,0.05,0.7),
                CSGOItem.create(CSGOWeapons.AWP,"????????????",Rarity.Immortal,0.5)
        ),
        OPERATION_RIPTIDE_CASE("???????????????",
                CSGOItem.create(CSGOWeapons.AUG,"??????",Rarity.Rare,0.76),
                CSGOItem.create(CSGOWeapons.DUAL_BERETTAS,"????????????",Rarity.Rare),
                CSGOItem.create(CSGOWeapons.G3SG1,"????????????",Rarity.Rare,0.7),
                CSGOItem.create(CSGOWeapons.MP7,"?????????",Rarity.Rare),
                CSGOItem.create(CSGOWeapons.PP_BIZON,"????????????",Rarity.Rare,0.77),
                CSGOItem.create(CSGOWeapons.USP,"?????????",Rarity.Rare,0.9),
                CSGOItem.create(CSGOWeapons.XM1014,"??????",Rarity.Rare),

                CSGOItem.create(CSGOWeapons.MAG7,"?????????",Rarity.Mythical,0.6),
                CSGOItem.create(CSGOWeapons.FAMAS,"ZX81??????",Rarity.Mythical),
                CSGOItem.create(CSGOWeapons.FN57,"????????????",Rarity.Mythical,0.41),
                CSGOItem.create(CSGOWeapons.MP9,"?????????",Rarity.Mythical,0.55),
                CSGOItem.create(CSGOWeapons.M4A4,"?????????",Rarity.Mythical),

                CSGOItem.create(CSGOWeapons.MAC10,"????????????",Rarity.Legendary),
                CSGOItem.create(CSGOWeapons.GLOCK,"????????????",Rarity.Legendary),
                CSGOItem.create(CSGOWeapons.SSG08,"????????????",Rarity.Legendary,0.6),

                CSGOItem.create(CSGOWeapons.DEAGLE,"????????????",Rarity.Immortal),
                CSGOItem.create(CSGOWeapons.AK47,"?????????1337",Rarity.Immortal,0.65)
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
        USP("USP ?????????"),
        GLOCK("?????????18???"),
        P250("P250"),
        FN57("FN57"),
        CZ75("CZ75 ????????????"),
        DEAGLE("????????????"),
        TEC9("Tec-9"),
        R8("R8 ????????????"),
        DUAL_BERETTAS("???????????????"),

        NOVA("??????"),
        XM1014("XM1014"),
        MAG7("MAG-7"),
        SAWED_OFF("???????????????"),
        M249("M249"),
        NEGEV("Negev"),

        MAC10("MAC-10"),
        MP9("MP9"),
        MP7("MP7"),
        UMP45("UMP-45"),
        PP_BIZON("PP-??????"),
        P90("P90"),
        MP5("MP5-SD"),

        FAMAS("?????????"),
        GRLIL("????????? AR"),
        M4A4("M4A4"),
        M4A1("M4A1 ?????????"),
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

    public enum Rarity {
        Common("Consumer","?????????"),
        Uncommon("Industrial","?????????"),

        Rare("MilSpec","?????????"),
        Mythical("Restricted","??????"),
        Legendary("Covert","??????"),
        Immortal("Clandestine","??????"),
        Contraband("Contraband","??????");

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
