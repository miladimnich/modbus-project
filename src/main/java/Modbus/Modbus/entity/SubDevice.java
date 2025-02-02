package Modbus.Modbus.entity;

public class SubDevice {
  private String name;
  private int startAddress;

  public SubDevice(String name, int startAddress) {
    this.name = name;
    this.startAddress = startAddress;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getStartAddress() {
    return startAddress;
  }

  public void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }
}
