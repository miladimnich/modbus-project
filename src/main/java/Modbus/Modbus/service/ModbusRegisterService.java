package Modbus.Modbus.service;

import Modbus.Modbus.service.ModbusClientService;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModbusRegisterService {

  @Autowired
  private ModbusClientService modbusClientService;



  // Method to fetch and process holding registers for a device
  public List<Map<String, Object>> getRegisters(int deviceIndex, int startAddress, int quantity) {
    int[] registerValues = readHoldingRegisters(deviceIndex, startAddress, quantity);
    return (registerValues == null) ? new ArrayList<>() : processRegisterValues(registerValues, startAddress);
  }

  // Private method to read holding registers from a device
  private int[] readHoldingRegisters(int deviceIndex, int startAddress, int quantity) {
    ModbusMaster modbusMaster = modbusClientService.getModbusMaster(deviceIndex);

    try {
      // Create the Modbus request for reading holding registers
      ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(1, startAddress, quantity); // Slave ID, starting address, quantity
      ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusMaster.send(request); // Send request

      if (response.isException()) {
        System.err.println("Modbus Exception: " + response.getExceptionMessage());
        return null;
      }

      // Convert short[] to int[] and return
      short[] shortData = response.getShortData();
      int[] intData = new int[shortData.length];
      for (int i = 0; i < shortData.length; i++) {
        intData[i] = shortData[i] & 0xFFFF; // Convert short to unsigned int
      }

      return intData;

    } catch (ModbusTransportException e) {
      e.printStackTrace();
      return null;  // Return null if there was an exception while reading
    }
  }

  // Private method to process register values and return as a list of maps
  private List<Map<String, Object>> processRegisterValues(int[] registerValues, int startAddress) {
    List<Map<String, Object>> processedRegisters = new ArrayList<>();

    for (int i = 0; i < registerValues.length; i++) {
      Map<String, Object> register = new HashMap<>();
      register.put("address", startAddress + i);  // Register address
      register.put("value", registerValues[i]);  // Register value
      processedRegisters.add(register);
    }

    return processedRegisters;
  }


















//  // Fetch and process holding registers for a device
//  public List<Map<String, Object>> getRegisters(int deviceIndex, int startAddress, int quantity) {
//    int[] registerValues = readHoldingRegisters(deviceIndex, startAddress, quantity);
//    return (registerValues == null) ? new ArrayList<>() : processRegisterValues(registerValues, startAddress);
//  }
//
//  // Read holding registers from Modbus device
//  private int[] readHoldingRegisters(int deviceIndex, int startAddress, int quantity) {
//    ModbusMaster modbusMaster = modbusClientService.getModbusMaster(deviceIndex);
//
//    try {
//      ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(1, startAddress, quantity);  // Slave ID, start address, quantity
//      ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusMaster.send(request); // Send request
//
//      if (response.isException()) {
//        System.err.println("Modbus Exception: " + response.getExceptionMessage());
//        return null;
//      }
//
//      // Convert short[] to int[] and return
//      return convertToIntArray(response.getShortData());
//    } catch (ModbusTransportException e) {
//      e.printStackTrace();
//      return null;  // Return null in case of error
//    }
//  }
//
//  // Convert short array to int array
//  private int[] convertToIntArray(short[] shortData) {
//    int[] intData = new int[shortData.length];
//    for (int i = 0; i < shortData.length; i++) {
//      intData[i] = shortData[i] & 0xFFFF;  // Convert short to unsigned int
//    }
//    return intData;
//  }
//
//  // Process register values and return them as a list of maps
//  private List<Map<String, Object>> processRegisterValues(int[] registerValues, int startAddress) {
//    List<Map<String, Object>> processedRegisters = new ArrayList<>();
//
//    for (int i = 0; i < registerValues.length; i++) {
//      Map<String, Object> register = new HashMap<>();
//      register.put("address", startAddress + i);  // Register address
//      register.put("value", registerValues[i]);  // Register value
//      processedRegisters.add(register);
//    }
//
//    return processedRegisters;
//  }
}
