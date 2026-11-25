package ar.edu.utn.odontologia.comunicaciones;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "email")
@Validated
public class ConfiguracionEmail {
    private String resendApiKey = "";
    private String from = "";
    private String cancelacionUrlBase = "";
    private List<String> testAllowlist = List.of();
    private int maxRetries = 2;
    private Duration timeout = Duration.ofSeconds(10);

    public String resendApiKey() { return resendApiKey; }
    public void setResendApiKey(String resendApiKey) { this.resendApiKey = resendApiKey; }

    public String from() { return from; }
    public void setFrom(String from) { this.from = from; }

    public List<String> testAllowlist() { return testAllowlist; }
    public void setTestAllowlist(List<String> testAllowlist) { this.testAllowlist = testAllowlist; }

    public int maxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public String cancelacionUrlBase() { return cancelacionUrlBase; }
    public void setCancelacionUrlBase(String cancelacionUrlBase) { this.cancelacionUrlBase = cancelacionUrlBase; }

    public Duration timeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
}