package io.github.jo0yo0n.vitalsjournal.support;

import io.github.jo0yo0n.vitalsjournal.auth.exception.AccountDeletedException;
import io.github.jo0yo0n.vitalsjournal.common.validation.DateRangeValidatable;
import io.github.jo0yo0n.vitalsjournal.common.validation.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionTestController {

  @GetMapping("/test/business")
  String business() {
    throw new AccountDeletedException();
  }

  @PostMapping("/test/body")
  String body(@Valid @RequestBody TestRequest request) {
    return "ok";
  }

  @GetMapping("/test/param")
  String param(@RequestParam @NotBlank String name) {
    return "ok";
  }

  @GetMapping("/test/path/{id}")
  String path(@PathVariable Long id) {
    return "ok";
  }

  @GetMapping("/test/date-range")
  public String getDateRange(@Valid @ModelAttribute DateRangeRequest request) {
    return "ok";
  }


  record TestRequest(@NotBlank String name) {}

  @ValidDateRange(fromField = "startedAt")
  record DateRangeRequest(LocalDateTime startedAt, LocalDateTime endedAt)
      implements DateRangeValidatable {

    @Override
    public LocalDateTime from() {
      return startedAt;
    }

    @Override
    public LocalDateTime to() {
      return endedAt;
    }
  }
}
