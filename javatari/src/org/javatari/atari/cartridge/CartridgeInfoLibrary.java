// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.
// ROM information library based on Rom Hunter�s collection

package org.javatari.atari.cartridge;

import org.javatari.atari.cartridge.formats.CartridgeSavestate;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


class CartridgeInfoLibrary {

    private static HashMap<String, CartridgeInfo> library;
    private static MessageDigest digest;

    static CartridgeInfo getInfo(ROM rom) {
        // Special case for Savestates
        if (CartridgeSavestate.checkIdentifier(rom.content))
            return getInfoForSavestate(rom.content);
        else
            return getInfo(computeHash(rom.content));
    }

    private static CartridgeInfo getInfo(String romHash) {
        if (library == null) initLibrary();
        CartridgeInfo info = library.get(romHash);
        if (info != null) {
            System.out.println("Cartridge: " + info.name);
            return info;
        }
        System.out.println("Cartridge unknown: " + romHash);
        info = new CartridgeInfo();
        info.hash = romHash;
        return info;
    }

    private static String computeHash(byte[] content) {
        if (digest == null)
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Could not create MD5 Digest");
                return "NO_HASH";    // give up
            }
        digest.reset();
        digest.update(content);
        BigInteger d = new BigInteger(1, digest.digest());
        return String.format("%032x", d);
    }

    private static CartridgeInfo getInfoForSavestate(byte[] content) {
        CartridgeInfo info = new CartridgeInfo();
        info.name = "Savestate";
        System.out.println("Cartridge: " + info.name);
        return info;
    }

    private static void initLibrary() {
        library = new HashMap<>();
        try {
            InputStream fileStream = CartridgeInfoLibrary.class
                    .getClassLoader().getResourceAsStream("org/javatari/atari/cartridge/CartridgeInfoLibrary.dat");
            ObjectInputStream objStream = new ObjectInputStream(fileStream);
            CartridgeInfo[] infos = (CartridgeInfo[]) objStream.readObject();
            objStream.close();
            for (CartridgeInfo info : infos)
                library.put(info.hash, info);
        } catch (Exception e) {
            System.out.println("Could not load Cartridge Info library");    // give up
        }
    }

}
