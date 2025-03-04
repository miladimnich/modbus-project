package Modbus.Modbus.controller;

import Modbus.Modbus.config.WebSocketHandlerCustom;
import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import Modbus.Modbus.enumes.SubDeviceType;
import Modbus.Modbus.service.DeviceService;
import Modbus.Modbus.service.ModbusCalculationService;
import Modbus.Modbus.service.ModbusClientService;
import Modbus.Modbus.service.ModbusRegisterService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

@Controller
@RequestMapping("/devices")
public class ModbusController {


  private final DeviceService deviceService;
  private final ModbusCalculationService modbusCalculationService;
  private final ModbusRegisterService modbusRegisterService;
  WebSocketHandlerCustom webSocketHandlerCustom;

  public ModbusController(DeviceService deviceService,
      ModbusCalculationService modbusCalculationService,
      ModbusRegisterService modbusRegisterService) {
    this.deviceService = deviceService;
    this.modbusCalculationService = modbusCalculationService;
    this.modbusRegisterService = modbusRegisterService;
  }


  @PostMapping("/{deviceId}")
  public ResponseEntity<String> getCurrentValue(@PathVariable int deviceId) {
    if (deviceService.getDeviceById(deviceId) == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found.");
    }
    modbusPollingService.startPolling(deviceId);
    return ResponseEntity.ok("Start pushing energy data  for device"
        + deviceId); // Return the energy calculations as a response
  }

  @PostMapping("/{deviceId}/startMeasure")
  public ResponseEntity<Map<String, Long>> start(@PathVariable int deviceId) {
    Map<String, Long> startResults = modbusCalculationService.startResults();
    return ResponseEntity.ok(startResults);
  }
  @PostMapping("/{deviceId}/stopResults")
  public ResponseEntity<Map<String, Long>> stop(@PathVariable int deviceId) {
    Map<String, Long> startResults = modbusCalculationService.lastResult();
    modbusPollingService.stopPulling();
    return ResponseEntity.ok(startResults);
  }


  @GetMapping
  public String getDevices(Model model) {
    List<Device> devices = deviceService.getDevices();
    model.addAttribute("devices", devices);
    return "devices";
  }

  @GetMapping("/{deviceId}/energy")
  public ResponseEntity<Map<String, Long>> getEnergyValues(@PathVariable int deviceId) {
    Map<String, Long> energyValues = modbusCalculationService.getEnergyCalculations(deviceId);
    return ResponseEntity.ok(energyValues); // Return the energy calculations as a response
  }

  @GetMapping("/{deviceId}/heating")
  public ResponseEntity<Map<String, Long>> getHeatingValues(@PathVariable int deviceId) {
    Map<String, Long> heatingValues = modbusCalculationService.getHeatingCalculations(deviceId);
    return ResponseEntity.ok(heatingValues); // Return the heating calculations as a response
  }

  @GetMapping("/{deviceId}/energy/WirkLeistungEnergy")
  public ResponseEntity<Long> getWirkLeistungEnergy(@PathVariable int deviceId) {
    List<Map<String, Object>> registers = modbusRegisterService.getRegistersForDevice(deviceId);
    Device device = deviceService.getDeviceById(deviceId);

    for (SubDevice subDevice : device.getSubDevices()) {
      if (subDevice.getType() == SubDeviceType.ENERGY) {
        long wirkLeistungEnergy = modbusCalculationService.calculateWirkLeistungEnergy(registers,
            subDevice.getStartAddress());
        return ResponseEntity.ok(wirkLeistungEnergy);
      }
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{deviceId}/energy/GenutztEnergy")
  public ResponseEntity<Long> getGenutztEnergy(@PathVariable int deviceId) {
    List<Map<String, Object>> registers = modbusRegisterService.getRegistersForDevice(deviceId);
    Device device = deviceService.getDeviceById(deviceId);

    for (SubDevice subDevice : device.getSubDevices()) {
      if (subDevice.getType() == SubDeviceType.HEATING) {
        long genutztEnergy = modbusCalculationService.calculateGenutztEnergy(registers,
            subDevice.getStartAddress());
        return ResponseEntity.ok(genutztEnergy);
      }
    }
    return ResponseEntity.notFound().build();
  }

}

