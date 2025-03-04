package Modbus.Modbus.service;

import Modbus.Modbus.config.WebSocketHandlerCustom;
import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import Modbus.Modbus.enumes.EnergyCalculationType;
import Modbus.Modbus.enumes.SubDeviceType;
import Modbus.Modbus.utillity.ModbusBitwiseUtil;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

@Service
public class ModbusCalculationService {

  private final ModbusRegisterService modbusRegisterService;
  private final DeviceService deviceService;
  WebSocketHandlerCustom webSocketHandler;

  public ModbusCalculationService(ModbusRegisterService modbusRegisterService,
      DeviceService deviceService) {
    this.modbusRegisterService = modbusRegisterService;
    this.deviceService = deviceService;
  }


  public long calculateGenutztEnergy(List<Map<String, Object>> registers, int startAddress) {
    return ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress + 10);
  }

  public long calculateWirkLeistungEnergy(List<Map<String, Object>> registers, int startAddress) {
    return ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress + 20);
  }

  public long calculateSomeOtherEnergy(List<Map<String, Object>> registers, int startAddress) {
    return ModbusBitwiseUtil.bitwiseShiftCalculation(registers, startAddress + 30);
  }

  public long calculateHeating(List<Map<String, Object>> registers, int startAddress) {
    return bitwiseShiftCalculation(registers, startAddress);

  }

  public long calculateSomeOtherHeating(List<Map<String, Object>> registers, int startAddress) {
    return bitwiseShiftCalculation(registers, startAddress + 10);

  }

  private long bitwiseShiftCalculation(List<Map<String, Object>> registers, int startAddress) {
    // Filter and sort registers based on their address
    List<Map<String, Object>> sortedRegisters = registers.stream()
        .filter(reg -> (int) reg.get("address") >= startAddress
            && (int) reg.get("address") < startAddress + 4)
        .sorted(Comparator.comparingInt(reg -> (int) reg.get("address")))
        .toList();

    // Ensure we have exactly 4 registers
    if (sortedRegisters.size() < 4) {
      throw new IllegalStateException("Not enough registers for bitwise shift calculation");
    }

    // Extract register values
    long reg1 = (int) sortedRegisters.get(0).get("value");
    long reg2 = (int) sortedRegisters.get(1).get("value");
    long reg3 = (int) sortedRegisters.get(2).get("value");
    long reg4 = (int) sortedRegisters.get(3).get("value");

    // Perform bitwise shift calculation
    return (reg1 << 48) | (reg2 << 32) | (reg3 << 16) | reg4;
  }


  public Map<String, Long> getEnergyCalculations(int deviceId) {
    List<Map<String, Object>> registers = modbusRegisterService.getRegistersForDevice(deviceId);
    Map<String, Long> results = new HashMap<>();
    Device device = deviceService.getDeviceById(deviceId);

    // Process all subdevices for energy
    for (SubDevice subDevice : device.getSubDevices()) {
      int startAddress = subDevice.getStartAddress();

      if (subDevice.getType() == SubDeviceType.ENERGY) {
        results.put("genutztEnergy", calculateGenutztEnergy(registers, startAddress));
        results.put("wirkLeistungEnergy", calculateWirkLeistungEnergy(registers,
            startAddress + 10));
      }
    }
    return results;
  }

  public Map<String, Long> getHeatingCalculations(int deviceId) {
    List<Map<String, Object>> registers = modbusRegisterService.getRegistersForDevice(deviceId);
    Map<String, Long> results = new HashMap<>();
    Device device = deviceService.getDeviceById(deviceId);

    // Process all subdevices for heating
    for (SubDevice subDevice : device.getSubDevices()) {
      int startAddress = subDevice.getStartAddress();

      if (subDevice.getType() == SubDeviceType.HEATING) {
        results.put("heatingValue", calculateHeating(registers, startAddress));
        // If you have other types of heating, like cooling, you can add more logic here.
        // Example:
        // if (subDevice.getName().equalsIgnoreCase("Cooling")) {
        //     results.put("coolingValue", calculateCooling(registers, startAddress));
        // }
      }
    }
    return results;
  }

  Map<String, Long> firstEnergyResult = new ConcurrentHashMap<>();
  Map<String, Long> currentEnergyResult = new ConcurrentHashMap<>();
  Map<String, Long> lastEnergyResult = new ConcurrentHashMap<>();
  boolean firstResultSaved = false;


  public Map<String, Long> startResults (){
    if(!firstResultSaved){
      firstEnergyResult.putAll(currentEnergyResult);
      firstResultSaved=true;
    }else {
      System.out.println("First result has already been saved");
    }
    return firstEnergyResult;
  }

  public Map<String, Long> lastResult (){
   return lastEnergyResult;
  }

  public void processAndPush(int deviceId, String key, long value) {
    // Store the current value as the last value before updating it
    lastEnergyResult.put(key, currentEnergyResult.getOrDefault(key, value));

    // Update the current energy result with the new value
    currentEnergyResult.put(key, value);

    // Push updates only when the current result has changed (not the same as the last result)
    if (!currentEnergyResult.get(key).equals(lastEnergyResult.get(key))) {
      Map<String, Object> update = new LinkedHashMap<>();
      update.put(key, currentEnergyResult.get(key));
      update.put("deviceID", deviceId);

      // Send the updated energy data to the client
      webSocketHandler.pushEnergyDataToClient(update);
    }
  }


  public void processEnergyData (int deviceId){
    Device device=deviceService.getDeviceById(deviceId);

      for (SubDevice subDevice: device.getSubDevices()){
        int startAddress = subDevice.getStartAddress();
        if(subDevice.getType()==SubDeviceType.ENERGY){
          processAndPush(deviceId, EnergyCalculationType.ERZEUGTE_ENERGY.name(),calculatErzeugteEnergy(deviceId,startAddress));
          processAndPush(deviceId, EnergyCalculationType.GENUTZTE_ENERGY.name(),calculateGenutztEnergy(deviceId,startAddress));
          processAndPush(deviceId, EnergyCalculationType.WIRKLEISTUNG.name(),calculateWirkLeistungEnergy(deviceId,startAddress));


        }
    }

  }
}
