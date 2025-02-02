package Modbus.Modbus.entity;

import java.util.List;
import org.springframework.stereotype.Component;


public class Device {

  private String ipAddress;
  private int port;
  private int startAddress;
  private int slaveId;
  private String modbusName;
  List<SubDevice> subDevices;


  public Device(String ipAddress, int port, int startAddress, int slaveId, String modbusName,
      List<SubDevice> subDevices) {
    this.ipAddress = ipAddress;
    this.port = port;
    this.startAddress = startAddress;
    this.slaveId = slaveId;
    this.modbusName = modbusName;
    this.subDevices = subDevices;
  }

  public List<SubDevice> getSubDevices() {
    return subDevices;
  }

  public void setSubDevices(List<SubDevice> subDevices) {
    this.subDevices = subDevices;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getStartAddress() {
    return startAddress;
  }

  public void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }

  public int getSlaveId() {
    return slaveId;
  }

  public void setSlaveId(int slaveId) {
    this.slaveId = slaveId;
  }

  public String getModbusName() {
    return modbusName;
  }

  public void setModbusName(String modbusName) {
    this.modbusName = modbusName;
  }
}
