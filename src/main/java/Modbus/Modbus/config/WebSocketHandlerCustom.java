package Modbus.Modbus.config;

import Modbus.Modbus.entity.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandlerCustom extends TextWebSocketHandler {
  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
  private final Map<String, Integer> sessionDeviceMap = new ConcurrentHashMap<>(); // sessionId -> deviceId mapping

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.put(session.getId(), session);
  }


  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session.getId());

    sessionDeviceMap.remove(session.getId()); // Remove the device association on disconnect
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    // Parse the incoming JSON message
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Integer> messageData = objectMapper.readValue(message.getPayload(), Map.class);
    Integer deviceId = messageData.get("deviceId");

    // Optional basic check: Ensure the device exists in the system
    if (isDeviceAvailable(deviceId)) {
      // Update the session-device map to associate the session with the valid deviceId
      updateSessionDevice(session, deviceId);
    } else {
      // Send an error message back to the client if the deviceId is no longer valid
      sendErrorMessage(session, "Device no longer available.");
    }
  }

  public void updateSessionDevice(WebSocketSession session, int deviceId) {
    // Check if the session is still open before updating the map
    if (session.isOpen()) {
      sessionDeviceMap.put(session.getId(), deviceId);
    } else {
      System.out.println("Attempted to update session with deviceId " + deviceId + " but session is closed.");
    }
  }

  // New method to get WebSocketSession by deviceId
  public WebSocketSession getSessionByDeviceId(int deviceId) {
    for (Map.Entry<String, Integer> entry : sessionDeviceMap.entrySet()) {
      if (entry.getValue() == deviceId) {
        return sessions.get(entry.getKey());
      }
    }
    return null; // Return null if no session is found for the deviceId
  }

  // Basic check: Does the device exist in the system?
  private boolean isDeviceAvailable(int deviceId) {
    Device device = deviceService.getDeviceById(deviceId);
    return device != null; // Checks if device exists in the database
  }

  private void sendErrorMessage(WebSocketSession session, String errorMessage) {
    try {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", errorMessage);
      session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(errorResponse)));
      session.close(CloseStatus.BAD_DATA); // Optionally close the connection if the deviceId is invalid
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void pushEnergyDataToClient(Map<String, Object> energyData) {
    Integer deviceId = (Integer) energyData.get("deviceID");

    // Find the session linked to this deviceId
    WebSocketSession session = getSessionByDeviceId(deviceId);
    if (session != null && session.isOpen()) {
      try {
        session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(energyData)));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


}

//  public void updateSessionDevice(WebSocketSession session, int deviceId) {
//    session.getAttributes().put("deviceId", deviceId);
//  }
//
//  @Override
//  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//    System.out.println("Received: " + message.getPayload());
//    session.sendMessage(new TextMessage("Server received: " + message.getPayload()));
//  }
