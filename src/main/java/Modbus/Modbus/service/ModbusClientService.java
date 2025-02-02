package Modbus.Modbus.service;

import Modbus.Modbus.config.DeviceConfig;
import Modbus.Modbus.entity.Device;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;



import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModbusClientService {

  @Autowired
  private DeviceConfig deviceConfig;

  private List<Device> devices;
  private final List<ModbusMaster> modbusMasters = new ArrayList<>();
  private final ModbusFactory modbusFactory = new ModbusFactory();


  @Autowired
  public ModbusClientService(DeviceConfig deviceConfig) {
    this.devices = deviceConfig.getDevices();  // Initialize devices using DeviceConfig
  }

  @PostConstruct
  public void init() {
    setupModbusMasters();  // Initialize Modbus masters after devices are loaded
  }



  public void setupModbusMasters() {
    for (Device device : devices) {
      try {
        IpParameters params = new IpParameters();
        params.setHost(device.getIpAddress());
        params.setPort(device.getPort());
        params.setEncapsulated(false);

        ModbusMaster modbusMaster = modbusFactory.createTcpMaster(params, true);
        modbusMaster.init();
        modbusMasters.add(modbusMaster);

      } catch (Exception e) {
        e.printStackTrace();

      }
    }
  }

  @PreDestroy
  public void shutdown() {
    for (ModbusMaster modbusMaster : modbusMasters) {
      try {
        modbusMaster.destroy();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // Provide a method to get ModbusMaster for the device index
  public ModbusMaster getModbusMaster(int deviceIndex) {
    if (deviceIndex >= 0 && deviceIndex < modbusMasters.size()) {
      return modbusMasters.get(deviceIndex);
    } else {
      throw new IndexOutOfBoundsException("Invalid device index");
    }
  }

//
////
//  public List<Map<String, Object>> getRegisters(int deviceIndex, int startAddress, int quantity) {
//    short[] registerValues = readHoldingRegisters(deviceIndex, startAddress, quantity);
//    return (registerValues == null) ? new ArrayList<>() : processRegisterValues(registerValues, startAddress);
//  }
//
//  private short[] readHoldingRegisters(int deviceIndex, int startAddress, int quantity) {
//    if (deviceIndex < 0 || deviceIndex >= modbusMasters.size()) return null;
//
//    try {
//      Device device = devices.get(deviceIndex);
//      ModbusMaster modbusMaster = modbusMasters.get(deviceIndex);
//      ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(device.getSlaveId(), startAddress, quantity);
//      ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusMaster.send(request);
//      return response.isException() ? null : response.getShortData();
//    } catch (ModbusTransportException e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
////
//  private List<Map<String, Object>> processRegisterValues(short[] registerValues, int startAddress) {
//    List<Map<String, Object>> registerData = new ArrayList<>();
//    for (int i = 0; i < registerValues.length; i++) {
//      registerData.add(Map.of("register", startAddress + i, "value", registerValues[i]));
//    }
//    return registerData;
//  }


}
