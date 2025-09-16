package org.ek.cloud_storage.domain.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DownloadResource {
    String fileName;
    StreamingResponseBody body;
}
