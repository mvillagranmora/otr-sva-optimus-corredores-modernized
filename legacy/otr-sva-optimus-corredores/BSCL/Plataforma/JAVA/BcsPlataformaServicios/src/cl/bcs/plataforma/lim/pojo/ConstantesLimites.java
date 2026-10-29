package cl.bcs.plataforma.lim.pojo;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConstantesLimites {
	public final String SI = "S";
	public final String NO = "N";
	
	public final Long COMODIN_INSTRUMENTO = 0L;
	public final String COMODIN_CANAL = "0";
	public final String COMODIN_OPERADOR = "0";
	
	public final String CODIGO_NO_APLICA = "_NA";
	public final Long CODIGO_NUMERICO_NO_APLICA = -1L;
	
	public final String TIPO_FECHA_CONSULTA_ADQUISICION = "ADQ";
	public final String TIPO_FECHA_CONSULTA_LIQUIDACION = "LIQ";
	
	public final String TIPO_LIMITE_SPOT = "LIMCTPSPOT";
	public final String TIPO_LIMITE_FWD = "LIMCTPFORWARD";
	public final String TIPO_LIMITE_CUENTA_PROPIA_PLAZO_VENCIMIENTO = "LIMCTPPLAZO";
	public final String TIPO_LIMITE_CUENTA_PROPIA_EXCLUIR_EMISOR = "LIMCTPEXCLUIREMISOR";
	public final String TIPO_LIMITE_CUENTA_PROPIA_CLASIFICACION_RIESGO = "LIMCTPCLASIFRIESGO";
	public final String TIPO_LIMITE_CUENTA_PROPIA_MONEDA = "LIMCTPMONEDA";
	public final String TIPO_LIMITE_CUENTA_PROPIA_MARGEN_TASAS = "LIMCTPMARGENTASAS";
	
	public final String CLASIFICACION_TIPO_INSTRUMENTO_SPOT = "SP";
	public final String CLASIFICACION_TIPO_INSTRUMENTO_FORWARD = "DV";
	public final String CLASIFICACION_RF = "RF";
	public final String CLASIFICACION_IF = "IF";
	
	public final String TIPO_INSTRUMENTO_SPOT = "SPT";
	public final String TIPO_INSTRUMENTO_FWD  = "FWD";
	
	
	public final String PROCESO_PAUTA_VENCIMIENTO = "PAUTA_VENCIMIENTO";
	public final String PROCESO_INGRESAR = "INGRESAR";
	public final String PROCESO_MODIFICACION = "MODIFICAR";
	public final String PROCESO_ANULAR = "ANULAR";
	public final String PROCESO_ANTICIPAR = "ANTICIPAR";
	public final String PROCESO_ASIGNAR = "ASIGNAR";
	public final String PROCESO_DESASIGNAR = "DESASIGNAR";
	public final String PROCESO_MODIFICACION_MESA = "MODIFICACION_MESA";
	
	public final String ORIGEN_ENTRADA = "ENTRADA";
	public final String ORIGEN_BD = "BD";
	
	public final String ESTADO_VIGENTE = "VIG";
	
	public final String TIPO_VALIDACION_OMITIR = "OK";
	public final String TIPO_VALIDACION_ADVERTENCIA = "ADVERTENCIA";
	public final String TIPO_VALIDACION_AUTORIZACION = "AUTORIZACION";
	public final String TIPO_VALIDACION_RECHAZAR = "RECHAZO";
	
	public final String TIPO_OPERACION_COMPRA = "COMSPT";
	public final String TIPO_OPERACION_VENTA = "VENSPT";
	
	public final String TIPO_OPERACION_COMPRA_FWD = "COMFWD";
	public final String TIPO_OPERACION_VENTA_FWD = "VENFWD";
	public final String TIPO_OPERACION_ANTICIPA_FWD = "ANTFWD";
	
	public final String ENTIDAD_CLASIFICADORA_RIESGO_TODOS = "0";
	public final String CLASIFICACION_RIESGO_TODOS = "0";
	
	public final String CODIGO_COMPRA = "COM";
	public final String CODIGO_VENTA = "VEN";
	public final Set<String> TIPO_OPERACION_MESA_VALIDO = Stream.of("COMTER","VENTER").collect(Collectors.toSet());
}