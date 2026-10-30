package cl.bcs.replica.kit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class Compuertas {
    private final boolean triggers;
    private final boolean respaldoSebra;
    private final boolean respaldoOptimus;

    @Autowired
    public Compuertas(@Value("${replica.compuertas.triggers:false}") boolean triggers, @Value("${replica.compuertas.respaldo-sebra:false}") boolean respaldoSebra,
            @Value("${replica.compuertas.respaldo-optimus:false}") boolean respaldoOptimus) {
        this.triggers = triggers;
        this.respaldoSebra = respaldoSebra;
        this.respaldoOptimus = respaldoOptimus;
    }

    public void exigirTriggers() {
        if (!triggers) throw new ServicioNoServidoException("G-TRG cerrada: CREATE/DROP TRIGGER no se ejecutan");
    }

    public void exigirRespaldoSebra() {
        if (!respaldoSebra) throw new ServicioNoServidoException("G-SEB cerrada: el respaldo Sebra no se ejecuta");
    }

    public void exigirRespaldoOptimus() {
        if (!respaldoOptimus) throw new ServicioNoServidoException("G-OPT cerrada: el respaldo Optimus no se ejecuta");
    }
}
