package Modbus.Modbus.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ModbusPollingService {
  private boolean isPolling = false;  // Track polling state
  private int currentDeviceId = -1;  // Keep track of the currently selected device

  private final WebSocketHandlerCustom webSocketHandlerCustom;

  public ModbusPollingService(WebSocketHandlerCustom webSocketHandlerCustom) {
    this.webSocketHandlerCustom = webSocketHandlerCustom;
  }
  // Start polling for the selected device
  public void startPolling(int deviceId) {
    if (isPolling && currentDeviceId == deviceId) {
      // Already polling for this device, no need to do anything
      return;
    }

    // Stop polling the previous device (if any)
    stopPolling();

    // Now start polling the new device
    currentDeviceId = deviceId;
    isPolling = true;
   processEnergyData(deviceId);  // Start processing and pushing energy data for the device
  }

  // Stop polling for the current device
  public void stopPolling() {
    if (isPolling) {
      isPolling = false;
      currentDeviceId = -1;
    }
  }


}
