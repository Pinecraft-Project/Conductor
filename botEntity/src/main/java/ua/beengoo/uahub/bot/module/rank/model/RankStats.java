package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;

/**
 * Per-member ranking statistics including chat, voice and prime points.
 *
 * <p>Also tracks extended voice-time metrics used for richer analytics.
 */
@Entity
@Data
@Table(name = "rank_stats")
public class RankStats {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "members_id", nullable = false, unique = true)
  private ServerMember serverMember;

  @Column(name = "prime_points")
  private double primePoints;

  @Column(name = "voice_points")
  private double voicePoints;

  @Column(name = "level")
  private double level;

  @Column(name = "chat_points")
  private double chatPoints;

  @Column(name = "multiplier")
  private double memberMultiplier;

  // Extended statistics
  @Column(name = "messages_sent")
  private Long messagesSent;

  @Column(name = "voice_joins")
  private Long voiceJoins;

  // Milliseconds spent in voice alone (only human in channel)
  @Column(name = "voice_ms_alone")
  private Long voiceMsAlone;

  // Milliseconds spent in voice with 2+ human participants (bots excluded)
  @Column(name = "voice_ms_with_others")
  private Long voiceMsWithOthers;

  // Milliseconds while muted (server/self)
  @Column(name = "voice_ms_muted")
  private Long voiceMsMuted;

  // Milliseconds while deafened (server/self)
  @Column(name = "voice_ms_deafened")
  private Long voiceMsDeafened;

  // Milliseconds considered potentially active (with others and not muted/deafened)
  @Column(name = "voice_ms_active")
  private Long voiceMsActive;

  /** Adds prime (manual) points. */
  public void addPrimePoints(double points) {
    setPrimePoints(getPrimePoints() + points);
  }

  /** Adds points earned through voice activity. */
  public void addVoicePoints(double points) {
    setVoicePoints(getVoicePoints() + points);
  }

  /** Adds points earned through chat activity. */
  public void addChatPoints(double points) {
    setChatPoints(getChatPoints() + points);
  }

  /** Adjusts individual member multiplier. */
  public void addMultiplier(double multiplier) {
    setMemberMultiplier(getMemberMultiplier() + multiplier);
  }

  /** Competitive points used for leaderboards (chat + voice). */
  public double getCompetitivePoints() {
    return getChatPoints() + getVoicePoints();
  }

  private long z(Long v) {
    return v == null ? 0L : v;
  }

  private long nz(long d) {
    return Math.max(d, 0);
  }

  /** Increments sent messages counter by 1. */
  public void incMessagesSent() {
    this.messagesSent = z(messagesSent) + 1;
  }

  /** Increments voice join events counter by 1. */
  public void incVoiceJoins() {
    this.voiceJoins = z(voiceJoins) + 1;
  }

  /** Adds milliseconds spent alone in a voice channel. */
  public void addVoiceMsAlone(long delta) {
    this.voiceMsAlone = z(voiceMsAlone) + nz(delta);
  }

  /** Adds milliseconds spent with other humans in a voice channel. */
  public void addVoiceMsWithOthers(long delta) {
    this.voiceMsWithOthers = z(voiceMsWithOthers) + nz(delta);
  }

  /** Adds milliseconds spent muted. */
  public void addVoiceMsMuted(long delta) {
    this.voiceMsMuted = z(voiceMsMuted) + nz(delta);
  }

  /** Adds milliseconds spent deafened. */
  public void addVoiceMsDeafened(long delta) {
    this.voiceMsDeafened = z(voiceMsDeafened) + nz(delta);
  }

  /** Adds milliseconds considered potentially active (with others, not muted/deafened). */
  public void addVoiceMsActive(long delta) {
    this.voiceMsActive = z(voiceMsActive) + nz(delta);
  }

  public long getMessagesSent() {
    return z(messagesSent);
  }

  public long getVoiceJoins() {
    return z(voiceJoins);
  }

  public long getVoiceMsAlone() {
    return z(voiceMsAlone);
  }

  public long getVoiceMsWithOthers() {
    return z(voiceMsWithOthers);
  }

  public long getVoiceMsMuted() {
    return z(voiceMsMuted);
  }

  public long getVoiceMsDeafened() {
    return z(voiceMsDeafened);
  }

  public long getVoiceMsActive() {
    return z(voiceMsActive);
  }

  /** Total milliseconds spent in voice (alone + with others). */
  public long getVoiceMsTotal() {
    return getVoiceMsAlone() + getVoiceMsWithOthers();
  }

  /** Ratio [0..1] of active voice time over total voice time. */
  public double getActiveVoiceRatio() {
    long total = getVoiceMsTotal();
    if (total <= 0) return 0.0;
    return (double) getVoiceMsActive() / (double) total;
  }
}
