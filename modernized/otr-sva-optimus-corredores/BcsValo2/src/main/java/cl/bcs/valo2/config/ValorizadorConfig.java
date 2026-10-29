package cl.bcs.valo2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "valo2.config")
public class ValorizadorConfig {
    private int escalaPrecios = 6;
    private int escalaTasas  = 6;

    public int getEscalaPrecios() { return escalaPrecios; }
    public void setEscalaPrecios(int escalaPrecios) { this.escalaPrecios = escalaPrecios; }
    public int getEscalaTasas() { return escalaTasas; }
    public void setEscalaTasas(int escalaTasas) { this.escalaTasas = escalaTasas; }
}
