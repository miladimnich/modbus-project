package Modbus.Modbus.config;

import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;


@Component

public class DeviceConfig {

  private final List<Device> devices = new ArrayList<>();

  @PostConstruct
  public void init() {
    devices.add(new Device("192.168.25.2", 502, 0, 1, "Modbus A",
        List.of(new SubDevice("subdeviceA", 1000), new SubDevice("subdeviceB", 2000))));
    devices.add(
        new Device("192.168.25.3", 502, 0, 1, "Modbus B", List.of(new SubDevice("modbusB", 2000))));
  }

  public List<Device> getDevices() {
    return devices;
  }
}
