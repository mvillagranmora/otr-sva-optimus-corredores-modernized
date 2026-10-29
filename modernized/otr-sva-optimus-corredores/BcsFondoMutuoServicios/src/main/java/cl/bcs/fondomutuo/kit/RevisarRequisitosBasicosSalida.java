package cl.bcs.fondomutuo.kit;

import java.util.ArrayList;
import java.util.List;

/** Legacy {@code RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida}: the messages of the basic requirements (empty list = all good). */
public class RevisarRequisitosBasicosSalida {

    public List<MensajeSalida> p_out_Mensajes;

    public RevisarRequisitosBasicosSalida() {
        this.p_out_Mensajes = new ArrayList<>();
    }

    @Override
    public String toString() {
        return JsonLegacy.conNulos().toJson(this);
    }
}
