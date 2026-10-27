package com.logitrack.config_server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigServerUnavailableGitTest {
    @TempDir Path directory;

    @Test
    @Timeout(20)
    void refusesToStartWhenTheGitRepositoryIsUnavailable() throws Exception {
        var git = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        git.createContext("/", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        git.start();
        try {
            assertThatThrownBy(() -> {
            try (var context = new SpringApplicationBuilder(ConfigServerApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run("--server.port=0",
                         "--spring.cloud.config.server.git.uri=http://127.0.0.1:" + git.getAddress().getPort() + "/missing.git",
                         "--spring.cloud.config.server.git.basedir=" + directory.resolve("clone"),
                         "--spring.cloud.config.server.git.clone-on-start=true",
                         "--spring.cloud.config.server.git.timeout=1",
                         "--logging.level.root=OFF",
                         "--spring.main.banner-mode=off")) {
                // Un arranque exitoso debe hacer fallar esta aserción.
            }
            }).isInstanceOf(BeanCreationException.class)
              .hasRootCauseInstanceOf(org.eclipse.jgit.errors.TransportException.class)
              .hasStackTraceContaining("503 Service Unavailable");
        } finally {
            git.stop(0);
        }
    }
}
