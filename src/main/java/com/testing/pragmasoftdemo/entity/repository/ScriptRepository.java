package com.testing.pragmasoftdemo.entity.repository;

import com.testing.pragmasoftdemo.entity.Script;
import com.testing.pragmasoftdemo.entity.ScriptStatus;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for {@link Script}.
 */
public interface ScriptRepository extends JpaRepository<Script, Long> {

  List<Script> findAllByStatus(ScriptStatus status, Sort sort);

  List<Script> findAllByStatus(ScriptStatus status);
}
