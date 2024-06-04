package com.testing.pragmasoftdemo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testing.pragmasoftdemo.entity.Script;
import com.testing.pragmasoftdemo.entity.ScriptStatus;
import com.testing.pragmasoftdemo.entity.repository.ScriptRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class ScriptServiceImplTest {

  @Mock
  private ScriptRepository scriptRepository;

  @InjectMocks
  private ScriptServiceImpl scriptService;

  @Test
  void getScriptByIdSuccess() {
    long scriptId = 1L;
    Script mockScript = new Script();
    mockScript.setId(scriptId);
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.of(mockScript));

    Script result = scriptService.getScriptById(scriptId);

    assertNotNull(result);
    assertEquals(scriptId, result.getId());
    verify(scriptRepository, times(1)).findById(scriptId);
  }

  @Test
  void getScriptByIdNotFound() {
    long scriptId = 1L;
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> scriptService.getScriptById(scriptId));

    assertTrue(exception.getMessage().contains("Script " + scriptId + " is not found."));
    verify(scriptRepository, times(1)).findById(scriptId);
  }

  @Test
  void getScriptsWithStatusAndOrdering() {
    ScriptStatus status = ScriptStatus.COMPLETED;
    String orderParam = "id";
    List<Script> mockScripts = List.of(new Script(), new Script());
    when(scriptRepository.findAllByStatus(status, Sort.by(Sort.Direction.DESC, orderParam))).thenReturn(mockScripts);

    List<Script> result = scriptService.getScripts(Optional.of(status), Optional.of(orderParam));

    assertNotNull(result);
    assertEquals(mockScripts.size(), result.size());
    verify(scriptRepository, times(1)).findAllByStatus(status, Sort.by(Sort.Direction.DESC, orderParam));
  }

  @Test
  void getScriptsWithStatusOnly() {
    ScriptStatus status = ScriptStatus.COMPLETED;
    List<Script> mockScripts = List.of(new Script(), new Script());
    when(scriptRepository.findAllByStatus(status)).thenReturn(mockScripts);

    List<Script> result = scriptService.getScripts(Optional.of(status), Optional.empty());

    assertNotNull(result);
    assertEquals(mockScripts.size(), result.size());
    verify(scriptRepository, times(1)).findAllByStatus(status);
  }

  @Test
  void getScriptsNoStatusOrOrdering() {
    List<Script> mockScripts = List.of(new Script(), new Script());
    when(scriptRepository.findAll()).thenReturn(mockScripts);

    List<Script> result = scriptService.getScripts(Optional.empty(), Optional.empty());

    assertNotNull(result);
    assertEquals(mockScripts.size(), result.size());
    verify(scriptRepository, times(1)).findAll();
  }

  @Test
  void executeScriptSuccess() throws Exception {
    Script script = new Script();
    script.setCode("print('Hello, World!');");
    script.setStatus(ScriptStatus.QUEUED);

    CompletableFuture<Void> result = scriptService.executeScript(script);

    assertNotNull(result);
    assertEquals(ScriptStatus.COMPLETED, script.getStatus());
    verify(scriptRepository, times(2)).saveAndFlush(script);
  }

  @Test
  void executeScriptFailure() throws Exception {
    Script script = new Script();
    script.setCode("throw new Error('Test Error');");
    script.setStatus(ScriptStatus.QUEUED);

    CompletableFuture<Void> result = scriptService.executeScript(script);

    assertNotNull(result);
    assertEquals(ScriptStatus.FAILED, script.getStatus());
    assertEquals("Error: Test Error", script.getFailureCause());
    verify(scriptRepository, times(2)).saveAndFlush(script);
  }

  @Test
  void interruptScriptSuccess() {
    long scriptId = 1L;
    Script script = new Script();
    script.setId(scriptId);
    script.setThreadId(Thread.currentThread().getId());
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.of(script));

    scriptService.interruptScript(scriptId);

    verify(scriptRepository, times(1)).findById(scriptId);
  }

  @Test
  void interruptScriptNotFound() {
    long scriptId = 1L;
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> scriptService.interruptScript(scriptId));

    assertTrue(exception.getMessage().contains("Script " + scriptId + " is not found."));
    verify(scriptRepository, times(1)).findById(scriptId);
  }

  @Test
  void deleteScriptSuccess() {
    long scriptId = 1L;
    Script script = new Script();
    script.setId(scriptId);
    script.setStatus(ScriptStatus.COMPLETED);
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.of(script));

    Script result = scriptService.deleteScript(scriptId);

    assertNotNull(result);
    assertEquals(scriptId, result.getId());
    verify(scriptRepository, times(1)).delete(script);
  }

  @Test
  void deleteScriptNotAllowed() {
    long scriptId = 1L;
    Script script = new Script();
    script.setId(scriptId);
    script.setStatus(ScriptStatus.EXECUTING);
    when(scriptRepository.findById(scriptId)).thenReturn(Optional.of(script));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> scriptService.deleteScript(scriptId));

    assertTrue(exception.getMessage().contains("cannot be deleted while its running/queued"));
    verify(scriptRepository, never()).delete(script);
  }
}

