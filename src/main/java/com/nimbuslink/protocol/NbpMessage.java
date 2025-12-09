package com.nimbuslink.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NbpMessage {
    private NbpCommand command;
    private String payload;
    
    // Helper for creating responses
    public static NbpMessage ack(String message) {
        return new NbpMessage(NbpCommand.ACK, message);
    }
    
    public static NbpMessage err(String error) {
        return new NbpMessage(NbpCommand.ERR, error);
    }
    
    @Override
    public String toString() {
        if (payload == null || payload.isEmpty()) {
            return command.name();
        }
        return command.name() + " " + payload;
    }
}
