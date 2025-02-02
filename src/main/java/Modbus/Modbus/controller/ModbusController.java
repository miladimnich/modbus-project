package Modbus.Modbus.controller;

import Modbus.Modbus.entity.Device;
import Modbus.Modbus.entity.SubDevice;
import Modbus.Modbus.service.DeviceService;
import Modbus.Modbus.service.ModbusClientService;
import Modbus.Modbus.service.ModbusRegisterService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/api/devices")
public class ModbusController {

  @Autowired
  private ModbusClientService modbusClientService;

  @Autowired
  private ModbusRegisterService modbusRegisterService; // Injecting the reusable register service


  @Autowired
  private DeviceService deviceService;

  @GetMapping()
  public String getDevices(Model model) {
    List<Device> devices = deviceService.getAllDevices();
    model.addAttribute("devices", devices);
    return "devices";
  }

  @GetMapping("/meter")
  public String getMeterPage(@RequestParam int deviceIndex, Model model) {
    Device device = deviceService.getDeviceByIndex(deviceIndex);
    if (device != null) {
      model.addAttribute("device", device);
      return "meter";  // Load meter.html
    }
    return "redirect:/api/devices";  // Redirect if invalid device
  }

  @GetMapping("/subdevices")
  @ResponseBody
  public List<SubDevice> getSubDevices(@RequestParam int deviceIndex) {
    Device device = deviceService.getDeviceByIndex(deviceIndex);
    return (device != null) ? device.getSubDevices() : Collections.emptyList();
  }







//
//  @PostMapping("/process")
//  public String processRegisters(@RequestParam int deviceIndex, @RequestParam int startAddress, @RequestParam int quantity, Model model) {
//    // Fetch registers from the Modbus device
//    List<Map<String, Object>> registers = modbusRegisterService.getRegisters(deviceIndex, startAddress, quantity);
//
//    if (registers != null) {
//      // Add the registers to the model to display in the table
//      model.addAttribute("processedRegisters", registers);
//    } else {
//      model.addAttribute("error", "No registers found or error fetching registers");
//    }
//
//    return "meter"; // Return to the same page to display the table with values
//  }
//
//  @PostMapping("/subdevice-process")
//  public String processSubdeviceRegisters(@RequestParam int deviceIndex, @RequestParam int subDeviceIndex, @RequestParam int startAddress, @RequestParam int quantity, Model model) {
//    // Fetch subdevice
//    Device device = deviceService.getDeviceByIndex(deviceIndex);
//    if (device != null && subDeviceIndex < device.getSubDevices().size()) {
//      SubDevice subDevice = device.getSubDevices().get(subDeviceIndex);
//
//      // Fetch registers from the subdevice using the ModbusRegisterService
//      List<Map<String, Object>> registers = modbusRegisterService.getRegisters(subDevice.getIndex(), startAddress, quantity);
//
//      if (registers != null) {
//        // Add the subdevice registers to the model
//        model.addAttribute("processedRegisters", registers);
//        model.addAttribute("subDevice", subDevice);
//      } else {
//        model.addAttribute("error", "No registers found or error fetching subdevice registers");
//      }
//    }
//
//    return "subdevice-meter"; // Return to a page to display subdevice register table
//  }
//
//
//

















//  // Endpoint to handle the reading of Modbus registers
//  @GetMapping("/readRegisters")
//  public String readRegisters(@RequestParam int deviceIndex, Model model) {
////    List<Map<String, Object>> registers = modbusClientService.getRegisters(deviceIndex, 0, 10); // Adjust startAddress and quantity
////    model.addAttribute("registers", registers);
////    model.addAttribute("deviceIndex", deviceIndex);
//    return "readRegisters"; // Name of your HTML template for displaying register data
//  }
//  @GetMapping("/{deviceIndex}/subdevices")
//  @ResponseBody
//  public List<SubDevice> getSubDevices(@PathVariable int deviceIndex) {
//    Device device = deviceService.getDeviceByIndex(deviceIndex);
//    return device.getSubDevices();
//  }


//

//  @GetMapping("/readRegisters")
//  public List<Map<String, Object>> readRegisters(@RequestParam int deviceIndex,
//      @RequestParam int startAddress,
//      @RequestParam int quantity) {
//
//// Use the service to read and process the registers from the Modbus slave
//    List<Map<String, Object>> registerData = modbusClientService.getRegisters(deviceIndex, startAddress, quantity);
//
//    if (registerData == null || registerData.isEmpty()) {
//      // Handle the case where no data is returned (for example, a failed Modbus read)
//      return new ArrayList<>();  // Return an empty list or a suitable response indicating failure
//    }
//
//    return registerData;  // Return the processed register data as a response (JSON)
//  }
  }


//http://localhost:8080/readRegisters?startAddress=0&quantity=5