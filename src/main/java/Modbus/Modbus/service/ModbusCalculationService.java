package Modbus.Modbus.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ModbusCalculationService {
  // Method to perform some calculations based on the registers
  public List<Map<String, Object>> processRegisters(List<Map<String, Object>> registers) {
    List<Map<String, Object>> processedRegisters = new ArrayList<>();

    for (Map<String, Object> register : registers) {
      int registerValue = (Integer) register.get("value");
      int calculatedValue = registerValue * 2; // Sample calculation (e.g., double the register value)

      // Create a new map with the register and calculated value
      Map<String, Object> processedRegister = new HashMap<>();
      processedRegister.put("register#", register.get("address"));
      processedRegister.put("value", registerValue);
      processedRegister.put("calculatedValue", calculatedValue); // Add calculated value to map

      processedRegisters.add(processedRegister);
    }

    return processedRegisters;
  }

}
