package ua.beengoo.uahub.bot.module.rank.data;

import lombok.Data;

@Data
public class VoiceMember {
  private long timeConnected;
  private long timeDisconnected;
  private long lastSampleTime;

  public VoiceMember(long timeConnected) {
    this.timeConnected = timeConnected;
    this.lastSampleTime = timeConnected;
  }
}
