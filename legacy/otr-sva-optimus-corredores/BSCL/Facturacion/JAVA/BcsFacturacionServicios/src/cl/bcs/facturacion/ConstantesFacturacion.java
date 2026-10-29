package cl.bcs.facturacion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantesFacturacion {
	public final List<String> tiposFactura = Arrays.asList(new String[]{"FAC", "FAE", "BOE", "FOE","FOC", "FEE", "FOX","POL","BEE"});
	public final List<String> tiposNotaCredito = Arrays.asList(new String[]{"NCE", "NCR", "NCO"});
	public final List<String> tiposAnticipoSimultanea = Arrays.asList(new String[]{"ANSIMV", "ANSIMC"});
}
