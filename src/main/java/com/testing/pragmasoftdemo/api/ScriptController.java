package com.testing.pragmasoftdemo.api;

import com.testing.pragmasoftdemo.entity.Script;
import com.testing.pragmasoftdemo.entity.ScriptStatus;
import com.testing.pragmasoftdemo.entity.repository.ScriptRepository;
import com.testing.pragmasoftdemo.service.impl.ScriptServiceImpl;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request handler that provides functionality for
 * executing scripts, retrieving script or list of scripts, interrupt
 * running scripts and delete it from DB.
 */

@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
public class ScriptController {

  private final ScriptServiceImpl scriptServiceImpl;
  private final ScriptRepository scriptRepository;

  @PostMapping
  public ResponseEntity<Long> submitScript(@RequestBody String code) throws IOException {
    Script script = new Script();
    script.setCode(code);
    script.setStatus(ScriptStatus.QUEUED);
    scriptRepository.saveAndFlush(script);

    scriptServiceImpl.executeScript(script);

    return ResponseEntity.ok(script.getId());
  }

  @GetMapping
  public ResponseEntity<List<Script>> listScripts(@RequestParam Optional<ScriptStatus> status,
                                                  @RequestParam Optional<String> orderParam) {
    final var result = scriptServiceImpl.getScripts(status, orderParam);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Script> getScript(@PathVariable Long id) {
    final var result = scriptServiceImpl.getScriptById(id);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/{id}/interrupt")
  public ResponseEntity<Boolean> interruptScript(@PathVariable Long id) {
    scriptServiceImpl.interruptScript(id);
    return ResponseEntity.ok(true);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Script> removeScript(@PathVariable Long id) {
    final var result = scriptServiceImpl.deleteScript(id);
    return ResponseEntity.ok(result);
  }
}

