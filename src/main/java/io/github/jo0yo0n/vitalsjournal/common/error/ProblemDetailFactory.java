package io.github.jo0yo0n.vitalsjournal.common.error;

import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailFactory {

  public ProblemDetail create(ErrorCode errorCode, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
    problemDetail.setType(errorCode.type());
    problemDetail.setTitle(errorCode.title());
    problemDetail.setProperty("errorCode", errorCode.name());

    return problemDetail;
  }

  public ProblemDetail create(ErrorCode errorCode, String detail, URI instance) {
    ProblemDetail problemDetail = create(errorCode, detail);
    problemDetail.setInstance(instance);

    return problemDetail;
  }
}
