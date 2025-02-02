package Modbus.Modbus.service;

import Modbus.Modbus.config.DeviceConfig;
import Modbus.Modbus.entity.Device;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  @Autowired
  DeviceConfig deviceConfig;


  public List<Device> getAllDevices() {
    return deviceConfig.getDevices();
  }

  public Device getDeviceByIndex(int index) {
    return deviceConfig.getDevices().get(index);
  }

}
