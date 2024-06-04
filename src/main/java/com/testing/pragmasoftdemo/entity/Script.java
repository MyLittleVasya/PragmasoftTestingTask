package com.testing.pragmasoftdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Script {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long id;

  Long threadId;

  @Column(length = 100000)
  String code;

  ScriptStatus status;

  @Column(length = 100000)
  String output;

  String error;

  String failureCause;

  LocalDateTime startTime;

  long executionTime;

}
