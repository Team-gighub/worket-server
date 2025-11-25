package gighub.worketserver.global.util;

public class StateParser {

  public record ParsedState(String role, String transactionId) {
  }

  /**
   * state 예: "client:tx=1234"
   * - role = "client"
   * - transactionId = "1234"
   */
  public static ParsedState parse(String rawState) {
    if (rawState == null || rawState.isBlank()) {
      return new ParsedState(null, null);
    }

    String role = null;
    String txId = null;

    String[] parts = rawState.split(":");

    if (parts.length >= 1 && !parts[0].isBlank()) {
      role = parts[0];  // "client" or "freelancer"
    }

    if (parts.length >= 2 && parts[1].startsWith("tx=")) {
      txId = parts[1].substring(3);   // "1234"
    }

    return new ParsedState(role, txId);
  }
}

