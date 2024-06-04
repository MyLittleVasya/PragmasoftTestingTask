package com.testing.pragmasoftdemo.service.impl;

import com.testing.pragmasoftdemo.entity.Script;
import com.testing.pragmasoftdemo.entity.ScriptStatus;
import com.testing.pragmasoftdemo.entity.repository.ScriptRepository;
import com.testing.pragmasoftdemo.service.ScriptService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation if {@link ScriptService} with business logic implemented.
 */
@Service
@RequiredArgsConstructor
public class ScriptServiceImpl implements ScriptService {

  private final ScriptRepository scriptRepository;

  public Script getScriptById(long id) {
    return scriptRepository.findById(id).orElseThrow(() -> new RuntimeException(
        String.format("Script %s is not found. Time: %s", id,
            LocalDateTime.now(ZoneOffset.UTC))));
  }

  public List<Script> getScripts(Optional<ScriptStatus> scriptStatus,
                                 Optional<String> orderingParam) {
    if (scriptStatus.isPresent()) {
      ScriptStatus status = scriptStatus.get();
      if (orderingParam.isPresent()) {
        String order = orderingParam.get();
        return scriptRepository.findAllByStatus(status, Sort.by(Sort.Direction.DESC, order));
      } else {
        return scriptRepository.findAllByStatus(status);
      }
    } else {
      return scriptRepository.findAll();
    }
  }

  @Async
  public CompletableFuture<Void> executeScript(Script script) throws IOException {
    script.setStatus(ScriptStatus.EXECUTING);
    long startTime = System.nanoTime();
    script.setThreadId(Thread.currentThread().getId());
    script.setStartTime(LocalDateTime.now());
    scriptRepository.saveAndFlush(script);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    try {
      Context context = Context.newBuilder("js")
          .out(outputStream)
          .err(errStream).build();
      context.eval("js", script.getCode());
      script.setOutput(outputStream.toString());
      script.setStatus(ScriptStatus.COMPLETED);
      if (!errStream.toString().equals("")) {
        throw new Exception(errStream.toString());
      }
      context.close();
    } catch (PolyglotException e) {
      if (!e.getMessage().contains("interrupted")) {
        script.setFailureCause(e.getMessage());
        script.setStatus(ScriptStatus.FAILED);
      }
      else {
        script.setFailureCause(e.getMessage());
        script.setStatus(ScriptStatus.STOPPED);
      }
    } catch (Exception e) {
      script.setFailureCause(e.getMessage());
      script.setStatus(ScriptStatus.FAILED);
    } finally {
      script.setError(errStream.toString());
      script.setOutput(outputStream.toString());
      script.setThreadId(null);
      long endTime = System.nanoTime();
      long duration = endTime - startTime;
      script.setExecutionTime(duration);
      scriptRepository.saveAndFlush(script);
      outputStream.close();
      errStream.close();
    }
    return CompletableFuture.completedFuture(null);
  }

  public void interruptScript(long scriptId) {
    final var script = scriptRepository.findById(scriptId).orElseThrow(() -> new RuntimeException(
        String.format("Script %s is not found. Time: %s", scriptId,
            LocalDateTime.now(ZoneOffset.UTC))));
    final var setOfThreads = Thread.getAllStackTraces().keySet();
    setOfThreads.stream().filter(stream -> stream.getId() == script.getThreadId()).findFirst()
        .ifPresent(Thread::interrupt);
  }

  public Script deleteScript(long id) {
    final var script = scriptRepository.findById(id).orElseThrow(() -> new RuntimeException(
        String.format("Script %s is not found. Time: %s", id,
            LocalDateTime.now(ZoneOffset.UTC))));
    if (!script.getStatus().equals(ScriptStatus.QUEUED) &&
        !script.getStatus().equals(ScriptStatus.EXECUTING)) {
      scriptRepository.delete(script);
      return script;
    }
    throw new RuntimeException(String.format(
        "Script %s cannot be deleted while its running/queued. Stop script to delete it. Time: %s.",
        script.getId(), LocalDateTime.now(ZoneOffset.UTC)));
  }
}

