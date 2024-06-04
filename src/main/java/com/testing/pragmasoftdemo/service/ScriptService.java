package com.testing.pragmasoftdemo.service;

import com.testing.pragmasoftdemo.entity.Script;
import com.testing.pragmasoftdemo.entity.ScriptStatus;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for {@link Script}.
 *<p>Defines functionality for fetching, executing, deleting, interrupting scripts.</p>
 */
public interface ScriptService {

  /**
   * Get script info by identifier.
   *
   * @param id identifier of the script.
   * @return script data.
   */
  Script getScriptById(long id);

  /**
   * Get list of scripts by optional params..
   * <p>
   *   Method defines optional params for filtering and ordering.
   *   If neither of params was provided, returns list of all scripts.
   * </p>
   *
   * @param scriptStatus optional param to filter scripts by it`s status.
   * @param orderingParam optional param to order script list in DESC order.
   *                      <p>Param should be named as any field of the script entity.</p>
   * @return list of scripts.
   */
  List<Script> getScripts(Optional<ScriptStatus> scriptStatus,
                          Optional<String> orderingParam);

  /**
   * Start executing script in a separate thread.
   *
   * @param script script entity.
   * @return null
   * @throws IOException can be thrown in case of failure with closing output streams.
   */
  CompletableFuture<Void> executeScript(Script script) throws IOException;

  /**
   * Stop executing of delete from queue some script.
   *
   * @param scriptId script identifier.
   */
  void interruptScript(long scriptId);

  /**
   * Delete script from DB if he is already inactive(STOPPED, FAILED, COMPLETED).
   *
   * @param id script identifier.
   * @return data of deleted script.
   */
  Script deleteScript(long id);

}
