package Modbus.Modbus.config;

import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import Modbus.Modbus.enumes.SubDeviceType;
import Modbus.Modbus.service.DeviceService;
import Modbus.Modbus.service.ModbusCalculationService;
import Modbus.Modbus.service.ModbusRegisterService;
import Modbus.Modbus.utillity.ModbusBitwiseUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ModbusWebSocketHandler extends TextWebSocketHandler {
  private static ModbusCalculationService modbusCalculationService;
  private static ModbusRegisterService modbusRegisterService;
  private static final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();
  private static final ObjectMapper objectMapper = new ObjectMapper(); //convert Java objects to JSON and parse JSON into Java objects.


  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    subscriptions.put(session, new HashSet<>(Arrays.asList("GenutztEnergy", "Heating")));

   // subscriptions.put(session, new HashSet<>()); // Initialize empty subscription for the client
  }
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    try {
      Map<String, Object> request = objectMapper.readValue(message.getPayload(), Map.class);

      if (request.containsKey("subscribe")) {
        List<String> energyTypes = (List<String>) request.get("subscribe");
        subscriptions.get(session).clear();  // Clear previous subscription
        subscriptions.get(session).addAll(energyTypes);
        session.sendMessage(new TextMessage("Subscribed to: " + energyTypes));
      }
    } catch (Exception e) {
      session.sendMessage(new TextMessage("Invalid request format."));
    }
  }
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    subscriptions.remove(session);
    System.out.println("WebSocket closed: " + session.getId());
  }

  public static void sendEnergyData(int deviceId) throws IOException {
    // Call the EnergyCalculationService to get the energy data
    Map<String, Long> energyData = modbusCalculationService.getEnergyCalculations(deviceId);

    // Send energy data to WebSocket clients (you can keep the WebSocket logic here)
    for (WebSocketSession session : subscriptions.keySet()) {
      if (session.isOpen()) {
        Set<String> subscribedEnergyTypes = subscriptions.get(session);
        Map<String, Object> sessionEnergyData = new HashMap<>();

        // Only send the energy types that the client has subscribed to
        for (String energyType : subscribedEnergyTypes) {
          if (energyData.containsKey(energyType)) {
            sessionEnergyData.put(energyType, energyData.get(energyType));
          }
        }

        // If there's data to send, send it
        if (!sessionEnergyData.isEmpty()) {
          session.sendMessage(new TextMessage(objectMapper.writeValueAsString(sessionEnergyData)));
        }
      }
    }
  }

}


//How the Frontend Subscribes
//
//const socket = new WebSocket("ws://localhost:8080/modbus");
//
//socket.onopen = function() {
//  console.log("Connected to WebSocket");
//
//  // Subscribe to all energy calculations
//  socket.send(JSON.stringify({ subscribe: ["GenutztEnergy", "WirkLeistungEnergy", "SomeOtherEnergy", "Heating"] }));
//};
//
//socket.onmessage = function(event) {
//    const energyData = JSON.parse(event.data);
//  console.log("Received Energy Data:", energyData);
//
//  // Process each energy calculation separately
//  if (energyData.GenutztEnergy !== undefined) {
//    console.log("Genutzt Energy:", energyData.GenutztEnergy);
//  }
//  if (energyData.WirkLeistungEnergy !== undefined) {
//    console.log("WirkLeistung Energy:", energyData.WirkLeistungEnergy);
//  }
//  if (energyData.SomeOtherEnergy !== undefined) {
//    console.log("Some Other Energy:", energyData.SomeOtherEnergy);
//  }
//  if (energyData.Heating !== undefined) {
//    console.log("Heating:", energyData.Heating);
//  }
//};
//
//socket.onclose = function() {
//  console.log("WebSocket closed");
//};



// Store subscribed energy types
//let subscribedEnergyTypes = [];
//
//    document.getElementById("startButton").addEventListener("click", function () {
//  // Define energy types to subscribe to (based on user selection)
//  subscribedEnergyTypes = ["GenutztEnergy", "Heating"];  // Modify based on user selection
//
//  // Send subscription message
//    const subscriptionMessage = JSON.stringify({
//      subscribe: subscribedEnergyTypes,
//      deviceId: "TestStation1"
//    });
//  socket.send(subscriptionMessage);  // Send to WebSocket server
//
//  console.log("Subscribed to:", subscribedEnergyTypes);
//});
//
//    document.getElementById("stopButton").addEventListener("click", function () {
//  // Send unsubscription message for the same energy types the user subscribed to
//    const unsubscriptionMessage = JSON.stringify({
//      unsubscribe: subscribedEnergyTypes,
//      deviceId: "TestStation1"
//    });
//  socket.send(unsubscriptionMessage);  // Send to WebSocket server
//
//  console.log("Unsubscribed from:", subscribedEnergyTypes);
//  subscribedEnergyTypes = [];  // Clear the list after unsubscribing
//});
//
//    import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.TextMessage;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class ModbusWebSocketHandler extends TextWebSocketHandler {
//
//  private static final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();
//
//  @Override
//  public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//    String msg = message.getPayload();
//
//    if (msg.contains("subscribe")) {
//      handleSubscription(session, msg);
//    } else if (msg.contains("unsubscribe")) {
//      handleUnsubscription(session, msg);
//    }
//  }
//
//  // Handle subscription (client subscribes to specific energy types)
//  public void handleSubscription(WebSocketSession session, String message) {
//    try {
//      ObjectMapper objectMapper = new ObjectMapper();
//      JsonNode jsonNode = objectMapper.readTree(message);
//
//      // Extract subscription details
//      List<String> energyTypes = objectMapper.convertValue(jsonNode.get("subscribe"), new TypeReference<List<String>>(){});
//      String deviceId = jsonNode.get("deviceId").asText();
//
//      // Add or update subscription for the session
//      subscriptions.putIfAbsent(session, new HashSet<>());
//      subscriptions.get(session).addAll(energyTypes);
//
//      // Send acknowledgment to the client
//      session.sendMessage(new TextMessage("Subscribed to: " + energyTypes.toString()));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  // Handle unsubscription (client unsubscribes from specific energy types)
//  public void handleUnsubscription(WebSocketSession session, String message) {
//    try {
//      ObjectMapper objectMapper = new ObjectMapper();
//      JsonNode jsonNode = objectMapper.readTree(message);
//
//      // Extract unsubscription details
//      List<String> energyTypesToUnsubscribe = objectMapper.convertValue(jsonNode.get("unsubscribe"), new TypeReference<List<String>>(){});
//      String deviceId = jsonNode.get("deviceId").asText();
//
//      // If the session is subscribed to these energy types, remove them
//      if (subscriptions.containsKey(session)) {
//        Set<String> currentSubscriptions = subscriptions.get(session);
//        currentSubscriptions.removeAll(energyTypesToUnsubscribe);
//
//        // If the client unsubscribes from all energy types, remove the session
//        if (currentSubscriptions.isEmpty()) {
//          subscriptions.remove(session);
//        } else {
//          subscriptions.put(session, currentSubscriptions);
//        }
//
//        // Send acknowledgment to the client
//        session.sendMessage(new TextMessage("Unsubscribed from: " + energyTypesToUnsubscribe.toString()));
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  // Send data to all subscribed clients
//  public static void sendEnergyData(Device device, List<Map<String, Object>> registers) throws IOException {
//    for (WebSocketSession session : subscriptions.keySet()) {
//      if (session.isOpen()) {
//        Set<String> subscribedEnergyTypes = subscriptions.get(session);
//        Map<String, Object> energyData = new HashMap<>();
//
//        // Check the energy types subscribed to and send relevant data
//        for (SubDevice subDevice : device.getSubDevices()) {
//          int startAddress = subDevice.getStartAddress();
//
//          if (subscribedEnergyTypes.contains("GenutztEnergy")) {
//            energyData.put("GenutztEnergy", ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress + 10));
//          }
//          if (subscribedEnergyTypes.contains("WirkLeistungEnergy")) {
//            energyData.put("WirkLeistungEnergy", ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress + 20));
//          }
//          if (subscribedEnergyTypes.contains("Heating")) {
//            energyData.put("Heating", ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress));
//          }
//        }
//
//        // Send the data if there is any
//        if (!energyData.isEmpty()) {
//          session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(energyData)));
//        }
//      }
//    }
//  }
//}
