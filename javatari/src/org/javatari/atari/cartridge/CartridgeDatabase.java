// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import org.javatari.atari.cartridge.formats.*;
import org.javatari.parameters.Parameters;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CartridgeDatabase {

    private static final String[] paddlesRomNames = new String[]{
            ".*PADDLES.*",                                        // Generic hint
            ".*BREAKOUT.*",
            ".*SUPER.*BREAKOUT.*", ".*SUPERB.*",
            ".*WARLORDS.*",
            ".*STEEPLE.*CHASE.*", ".*STEPLCHS.*",
            ".*VIDEO.*OLYMPICS.*", ".*VID(|_)OLYM(|P).*",
            ".*CIRCUS.*ATARI.*", ".*CIRCATRI.*",
            ".*KABOOM.*",
            ".*BUGS((?!BUNNY).)*",                                // Bugs, but not Bugs Bunny!
            ".*BACHELOR.*PARTY.*", ".*BACHELOR.*",
            ".*BACHELORETTE.*PARTY.*", ".*BACHLRTT.*",
            ".*BEAT.*EM.*EAT.*EM.*", ".*BEATEM.*",
            ".*PHILLY.*FLASHER.*", ".*PHILLY.*",
            ".*JEDI.*ARENA.*", ".*JEDIAREN.*",
            ".*EGGOMANIA.*", ".*EGGOMANA.*",
            ".*PICNIC.*",
            ".*PIECE.*O.*CAKE.*", ".*PIECECKE.*",
            ".*BACKGAMMON.*", ".*BACKGAM.*",
            ".*BLACKJACK.*", ".*BLACK(|_)J.*",
            ".*CANYON.*BOMBER.*", ".*CANYONB.*",
            ".*CASINO.*",
            ".*DEMONS.*DIAMONDS.*", ".*DEMONDIM.*",
            ".*DUKES.*HAZZARD.*2.*", ".*STUNT.?2.*",
            ".*ENCOUNTER.*L.?5.*", ".*ENCONTL5.*",
            ".*G.*I.*JOE.*COBRA.*STRIKE.*", ".*GIJOE.*",
            ".*GUARDIAN.*",
            ".*MARBLE.*CRAZE.*", ".*MARBCRAZ.*",
            ".*MEDIEVAL.*MAYHEM.*",
            ".*MONDO.*PONG.*",
            ".*NIGHT.*DRIVER.*", ".*NIGHTDRV.*",
            ".*PARTY.*MIX.*",
            ".*POKER.*PLUS.*",
            ".*PONG.*SPORTS.*",
            ".*SCSICIDE.*",
            ".*SECRET.*AGENT.*",
            ".*SOLAR.*STORM.*", ".*SOLRSTRM.*",
            ".*SPEEDWAY.*",
            ".*STREET.*RACER.*", ".*STRTRACE.*",
            ".*STUNT.*CYCLE.*", ".*STUNT.?1.*",
            ".*TAC.?SCAN.*",
            ".*MUSIC.*MACHINE.*", ".*MUSCMACH.*",
            ".*VONG.*",
            ".*WARPLOCK.*"
    };
    private static final String[] crtModeRomNames = new String[]{
            ".*STAR.*CASTLE.*",
            ".*SEAWEED.*",
            ".*ANDREW.*DAVIE.*", ".*DEMO.*IMAGE.*AD.*"        // Various 32K Image demos
    };
    private static final String HINTS_PREFIX_REGEX = "(|.*?(\\W|_|%20))";
    private static final String HINTS_SUFFIX_REGEX = "(|(\\W|_|%20).*)";
    private static final int FORMAT_PRIORITY_BOOST = 50;
    private static List<CartridgeFormat> allFormats = Arrays.asList(CartridgeSavestate.FORMAT,            // 90
            Cartridge4K.FORMAT,                    // 101
            Cartridge2K_CV.FORMAT,                // 102
            Cartridge8K_F8.FORMAT,                // 101
            Cartridge8K_64K_AR.FORMAT,            // 101
            Cartridge12K_FA.FORMAT,                // 101
            Cartridge16K_F6.FORMAT,                // 101
            Cartridge32K_F4.FORMAT,                // 101
            Cartridge24K_28K_32K_FA2.FORMAT,    // 102
            Cartridge64K_F0.FORMAT,                // 101
            Cartridge64K_X07.FORMAT,            // 102
            Cartridge8K_E0.FORMAT,                // 102
            Cartridge8K_FE.FORMAT,                // 103
            Cartridge16K_E7.FORMAT,                // 102
            Cartridge8K_512K_3E.FORMAT,            // 111
            Cartridge8K_512K_3F.FORMAT,            // 112
            Cartridge8K_512K_SB.FORMAT,            // 113
            Cartridge8K_64K_EF.FORMAT,            // 114
            Cartridge8K_UA.FORMAT,                // 115
            Cartridge8K_0840.FORMAT,            // 116
            Cartridge10K_DPCa.FORMAT,            // 101
            Cartridge10K_DPC.FORMAT,            // 110
            Cartridge32K_FA2cu.FORMAT            // 103
    );
    private static FormatRomNameMatcher[] formatRomNames = new FormatRomNameMatcher[]{
            new FormatRomNameMatcher(Cartridge8K_E0.FORMAT, new String[]{
                    ".*MONTEZUMA.*", ".*MONTZREV.*",
                    ".*GYRUS.*",
                    ".*TOOTH.*PROTECTORS.*", ".*TOOTHPRO.*",
                    ".*DEATH.*STAR.*BATTLE.*", ".*DETHSTAR.*",
                    ".*JAMES.*BOND.*", ".*JAMEBOND.*",
                    ".*SUPER.*COBRA.*", ".*SPRCOBRA.*",
                    ".*TUTANKHAM.*", ".*TUTANK.*",
                    ".*POPEYE.*",
                    ".*(SW|STAR.?WARS).*ARCADE.*GAME.*", ".*SWARCADE.*",
                    ".*Q.*BERT.*QUBES.*", ".*QBRTQUBE.*",
                    ".*FROGGER.?(2|II).*",
                    ".*DO.*CASTLE.*"
            }),
            new FormatRomNameMatcher(Cartridge8K_FE.FORMAT, new String[]{
                    ".*ROBOT.*TANK.*", ".*ROBOTANK.*",
                    ".*DECATHLON.*", ".*DECATHLN.*"        // There is also a 16K F6 version
            }),
            new FormatRomNameMatcher(Cartridge16K_E7.FORMAT, new String[]{
                    ".*BUMP.*JUMP.*", ".*BNJ.*",
                    ".*BURGER.*TIME.*", ".*BURGTIME.*",
                    ".*POWER.*HE.?MAN.*", ".*HE_MAN.*"
            }),
            new FormatRomNameMatcher(Cartridge8K_512K_3F.FORMAT, new String[]{
                    ".*POLARIS.*",
                    ".*RIVER.*PATROL.*", ".*RIVERP.*",
                    ".*SPRINGER.*",
                    ".*MINER.*2049.*", ".*MNR2049R.*",
                    ".*MINER.*2049.*VOLUME.*", ".*MINRVOL2.*",
                    ".*ESPIAL.*",
                    ".*ANDREW.*DAVIE.*", ".*DEMO.*IMAGE.*AD.*"        // Various 32K Image demos
            }),
            new FormatRomNameMatcher(Cartridge8K_512K_3E.FORMAT, new String[]{
                    ".*BOULDER.*DASH.*", ".*BLDRDASH.*"
            }),
            new FormatRomNameMatcher(Cartridge10K_DPCa.FORMAT, new String[]{
                    ".*PITFALL.*(2|II).*"
            })
    };
    private static String CARTRIDGE_NAME = Parameters.CARTRIDGE_NAME;
    private static String CARTRIDGE_LABEL = Parameters.CARTRIDGE_LABEL;
    private static int CARTRIDGE_LABEL_COLOR = Parameters.CARTRIDGE_LABEL_COLOR;
    private static int CARTRIDGE_BACK_COLOR = Parameters.CARTRIDGE_BACK_COLOR;
    private static int CARTRIDGE_BORDER_COLOR = Parameters.CARTRIDGE_BORDER_COLOR;
    private static int CARTRIDGE_PADDLES = Parameters.CARTRIDGE_PADDLES;
    private static int CARTRIDGE_CRT_MODE = Parameters.CARTRIDGE_CRT_MODE;
    private static String CARTRIDGE_FORMAT = Parameters.CARTRIDGE_FORMAT;

    public static ArrayList<CartridgeFormatOption> getFormatOptions(ROM rom) throws ROMFormatUnsupportedException {
        ROMFormatDenialDetailException exDenialDetail = null;
        ArrayList<CartridgeFormatOption> options = new ArrayList<>();
        for (CartridgeFormat format : allFormats) {
            CartridgeFormatOption option;
            try {
                option = format.getOption(rom);
            } catch (ROMFormatDenialDetailException e) {
                if (exDenialDetail == null) exDenialDetail = e;        // Save only the first message
                continue;
            }
            if (option == null) continue;        // rejected by format
            boostPriority(option, rom.info);    // adjust priority based on ROM info
            options.add(option);
        }

        // If no Format could be found, throw error
        if (options.isEmpty()) {
            // Special message if needed
            String message = exDenialDetail != null
                    ? exDenialDetail.getMessage()
                    : "Unsupported ROM Format. Size: " + rom.content.length;
            throw new ROMFormatUnsupportedException(message);
        }

        Collections.sort(options);        // Sort according to priority
        return options;
    }

    static CartridgeInfo produceInfo(ROM rom) {
        // Get info from the library
        CartridgeInfo info = CartridgeInfoLibrary.getInfo(rom);
        // If ROM is unknown (name == null), produce name from information found in the ROM URL
        if (info.name == null) info.name = produceCartridgeName(rom.source);
        finishInfo(info, rom);
        return info;
    }

    // Fill absent information based on ROM name
    private static void finishInfo(CartridgeInfo info, ROM rom) {
        // Compute label based on name
        if (info.label == null) info.label = produceCartridgeLabel(info.name);
        String name = info.name.toUpperCase();
        // Adjust Paddles use information if absent
        Paddles:
        if (info.paddles == -1) {
            info.paddles = 0;
            if (!name.matches(HINTS_PREFIX_REGEX + "JOYSTICK(S)?" + HINTS_SUFFIX_REGEX)) {
                if (name.matches(HINTS_PREFIX_REGEX + "PADDLE(S)?" + HINTS_SUFFIX_REGEX))
                    info.paddles = 1;
                else
                    for (String romName : paddlesRomNames)
                        if (name.matches(romName)) {
                            info.paddles = 1;
                            break Paddles;
                        }
            }
        }
        // Adjust CRT Mode use information if absent
        CrtMode:
        if (info.crtMode == -1) {
            if (name.matches(HINTS_PREFIX_REGEX + "CRT([_\\-])?MODE" + HINTS_SUFFIX_REGEX))
                info.crtMode = 1;
            else
                for (String romName : crtModeRomNames)
                    if (name.matches(romName)) {
                        info.crtMode = 1;
                        break CrtMode;
                    }
        }
        // Adjust Format information if absent
        Format:
        if (info.format == null || info.format.isEmpty()) {
            // First by explicit format hint
            String romURL = rom.source.toUpperCase();
            for (CartridgeFormat format : allFormats) {
                if (formatMatchesByHint(format, name) || formatMatchesByHint(format, romURL)) {
                    info.format = format.id;
                    break Format;
                }
            }
            // Then by name
            for (CartridgeFormat format : allFormats)
                if (formatMatchesByName(format, name)) {
                    info.format = format.id;
                    break Format;
                }
        }
    }

    // Force ROM info to reflect properties specified
    //  May MODIFY an Info object from the Library for this ROM, but no problem
    public static void adjustInfoOfROMProvided(ROM rom) {
        CartridgeInfo info = rom.info;
        if (CARTRIDGE_NAME != null) {
            info.name = CARTRIDGE_NAME;
            finishInfo(info, rom);            // Give a chance for the new name to fill absent information
        }
        if (CARTRIDGE_LABEL != null) info.label = CARTRIDGE_LABEL;
        if (CARTRIDGE_LABEL_COLOR != -1) info.labelColor = CARTRIDGE_LABEL_COLOR;
        if (CARTRIDGE_BACK_COLOR != -1) info.labelBackColor = CARTRIDGE_BACK_COLOR;
        if (CARTRIDGE_BORDER_COLOR != -1) info.labelBorderColor = CARTRIDGE_BORDER_COLOR;
        if (CARTRIDGE_PADDLES != -1) info.paddles = CARTRIDGE_PADDLES;
        if (CARTRIDGE_CRT_MODE != -1) info.crtMode = CARTRIDGE_CRT_MODE;
        if (CARTRIDGE_FORMAT != null) info.format = CARTRIDGE_FORMAT;
    }

    private static void boostPriority(CartridgeFormatOption formatOption, CartridgeInfo info) {
        if (formatOption.format.id.equals(info.format))
            formatOption.priority -= FORMAT_PRIORITY_BOOST;
    }

    private static boolean formatMatchesByHint(CartridgeFormat format, String hint) {
        return hint.matches(HINTS_PREFIX_REGEX + format.id + HINTS_SUFFIX_REGEX);
    }

    private static boolean formatMatchesByName(CartridgeFormat format, String name) {
        for (FormatRomNameMatcher matcher : formatRomNames)
            if (matcher.format == format && matcher.matches(name))
                return true;
        return false;
    }

    private static String produceCartridgeName(String url) {
        if (url == null || url.trim().isEmpty()) return "Unknown";
        String name = url;
        try {
            String enc = System.getProperty("file.encoding");
            if (enc != null) name = URLDecoder.decode(url, enc);
        } catch (Exception e) {
            // Give up
        }
        // Get the last part of the URL (file name)
        int slash = name.lastIndexOf("/");
        int bslash = name.lastIndexOf("\\");
        int question = name.lastIndexOf("?");
        int i = Math.max(slash, Math.max(bslash, question));
        if (i >= 0 && i < name.length() - 1) name = name.substring(i + 1);
        // Get only the file name without the extension
        int dot = name.lastIndexOf(".");
        if (dot >= 0) name = name.substring(0, dot);
        name = name.trim();
        if (name.isEmpty()) return "Unknown";
        else return name;
    }

    private static String produceCartridgeLabel(String name) {
        return name.split("([(\\[])")[0].trim();
    }

}


class FormatRomNameMatcher {
    final CartridgeFormat format;
    private final List<String> namePatterns;

    FormatRomNameMatcher(CartridgeFormat format, String[] namePatterns) {
        super();
        this.format = format;
        this.namePatterns = Arrays.asList(namePatterns);
    }

    boolean matches(String romURL) {
        for (String pattern : namePatterns)
            if (romURL.matches(pattern)) return true;
        return false;
    }
}
