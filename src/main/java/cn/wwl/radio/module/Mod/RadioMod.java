package cn.wwl.radio.module.Mod;

import cn.wwl.radio.utils.TextMarker;
import cn.wwl.radio.module.Module;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class RadioMod extends Module {

    @Override
    public void onHookCommand(String commands) {
        switch (commands) {
            case "0radioYes" -> pushToConsole("playerradio Agree \"" + TextMarker.绿色.getCode() + "我觉得彳亍 (*･ω< )\"");
            case "0radioNo" -> pushToConsole("playerradio Disagree \"" + TextMarker.淡红色.getCode() + "我觉得不彳亍 ヽ(。>д<)ｐ\"");
            case "0radioThanks" -> pushToConsole("playerradio Radio.Thanks \"" + TextMarker.浅绿色.getCode() + "感谢HXD ٩(๑>◡<๑)۶ \"");
            case "0radioNice" -> pushToConsole("playerradio ThrillEmote \"" + TextMarker.浅紫色.getCode() + "芜湖~起飞~ ヾ(≧∇≦*)ヾ\"");
            case "0radioRush" -> pushToConsole("playerradio WonRoundQuickly \"" + TextMarker.绿色.getCode() + "来波P90 Rush B如何  |´・ω・)ノ \"");
            case "0radioGoGo" -> pushToConsole("playerradio Radio.GoGoGo \"" + TextMarker.金色.getCode() + "冲鸭! (ﾉﾟ∀ﾟ)ﾉ \"");
            case "0radioECO" -> pushToConsole("playerradio Radio.EcoRound \"" + TextMarker.蓝色.getCode() + "别冲动,ECO一局 (<ゝω・)☆\"");
            case "0radioFullBuy" -> pushToConsole("playerradio Radio.SpendRound \"" + TextMarker.蓝色.getCode() + "全起吧,杀穿他们 ヾ(ｏ･ω･)ﾉ\"");
            case "0radioHelp" -> pushToConsole("playerradio ScaredEmote \"" + TextMarker.淡红色.getCode() + "救救孩子吧 顶不住啦!/(ㄒoㄒ)/~~\"");
            case "0radioHowTODO" -> pushToConsole("playerradio Radio.NeedPlan \"" + TextMarker.浅绿色.getCode() + "这回合准备怎么打? u･ω･u\"");
            case "0radioNeedDrop" -> pushToConsole("playerradio Radio.NeedDrop \"" + TextMarker.灰色.getCode() + "HXD发把枪呗? d(´ω｀*) \"");
            case "0radioGoA" -> pushToConsole("playerradio Radio.GoA \"" + TextMarker.金色.getCode() + "一起去A点如何? ヾ(ｏ･ω･)ﾉ\"");
            case "0radioGoB" -> pushToConsole("playerradio Radio.GoB \"" + TextMarker.金色.getCode() + "我们去B点吧! ヽ(･ω･´ﾒ)\"");
            case "0radioVerynice" -> pushToConsole("playerradio Radio.Compliment \"" + TextMarker.金色.getCode() + "好哎~ (❀ฺ´∀`❀ฺ)ﾉ\"");
            default -> {
                if (commands.length() == 6) {
                    //commands == 0radio, echo Usage.
                    echo("Unknown cmd. Try like this > 0radio_[msg]");
                    echo("Please use [_] instead [ ](Space bar)");
                    echo("Color List: ");
                    StringBuilder builder = new StringBuilder();
                    Arrays.stream(TextMarker.values()).forEach(c -> builder.append(c.getHumanCode()).append(","));
                    echo(builder.substring(0,builder.length() - 1));
                } else {
                    if (!commands.contains("_")) {
                        echo("UnderLine not found. Follow the Code format!");
                        return;
                    }

                    String[] split = commands.split("_");
                    System.out.println(Arrays.toString(split));
                    //按照下划线进行切分,去除第一段,后面的进行组合来修复无法传递空格的问题
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < split.length; i++) {
                        builder.append(split[i]).append(" ");
                    }
                    String cmd = TextMarker.replaceHumanCode(builder.toString());
                    pushToConsole("playerradio hi \"" + cmd + "\"");
                }
            }
        }
    }

    @Override
    public List<String> hookedCommands() {
        return List.of("0radio");
    }


}
