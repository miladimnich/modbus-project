package Modbus.Modbus.service;

import Modbus.Modbus.config.DeviceConfig;
import Modbus.Modbus.config.ModbusWebSocketHandler;
import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import Modbus.Modbus.service.ModbusClientService;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModbusRegisterService {

  private final ModbusClientService modbusClientService;
  private final DeviceService deviceService;

  public ModbusRegisterService(ModbusClientService modbusClientService,
      DeviceService deviceService) {
    this.modbusClientService = modbusClientService;
    this.deviceService = deviceService;
  }

  public List<Map<String, Object>> getRegistersForDevice(int deviceId) {
    List<Map<String, Object>> allRegisters = new ArrayList<>();

    for (SubDevice subDevice : deviceService.getDeviceById(deviceId).getSubDevices()) {
      List<Map<String, Object>> subDeviceRegisters = readHoldingRegisters(deviceId, subDevice.getSlaveId(),
          subDevice.getStartAddress(), subDevice.getQuantity());
      allRegisters.addAll(subDeviceRegisters);
    }

    return allRegisters;
  }

  private List<Map<String, Object>> readHoldingRegisters(int deviceId, int slaveId, int startAddress, int quantity) {
    List<Map<String, Object>> allProcessedRegisters = new ArrayList<>();

      ModbusMaster modbusMaster = modbusClientService.getModbusMasterById(deviceId);

      if (modbusMaster != null) {
        try {
          ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, startAddress, quantity);
          ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusMaster.send(request);
          short[] shortData = response.getShortData();

          // Process registers and add them to the list
          for (int i = 0; i < shortData.length; i++) {
            Map<String, Object> register = new HashMap<>();
            register.put("address", startAddress + i);
            register.put("value", shortData[i] & 0xFFFF);
            allProcessedRegisters.add(register); // Add directly to the main list
          }
          // Push only requested energy calculations to WebSocket clients
          ModbusWebSocketHandler.sendEnergyData(deviceId);
        } catch (ModbusTransportException e) {
          e.printStackTrace();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    return allProcessedRegisters;
  }

}


