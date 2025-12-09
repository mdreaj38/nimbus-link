package com.nimbuslink.protocol;

public class NbpParser {

    public static NbpMessage parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.trim().split("\\s+", 2);
        String commandStr = parts[0].toUpperCase();
        String payload = parts.length > 1 ? parts[1] : "";

        try {
            NbpCommand command = NbpCommand.valueOf(commandStr);
            return new NbpMessage(command, payload);
        } catch (IllegalArgumentException e) {
            return new NbpMessage(NbpCommand.ERR, "Unknown command: " + commandStr);
        }
    }

    public static String serialize(NbpMessage message) {
        return message.toString() + "\n";
    }
}
