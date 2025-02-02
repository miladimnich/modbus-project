package Modbus.Modbus.entity;

public class Register {
  private int address;
  private int value;

  public Register(int address, int value) {
    this.address = address;
    this.value = value;
  }

  public int getAddress() {
    return address;
  }

  public void setAddress(int address) {
    this.address = address;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
