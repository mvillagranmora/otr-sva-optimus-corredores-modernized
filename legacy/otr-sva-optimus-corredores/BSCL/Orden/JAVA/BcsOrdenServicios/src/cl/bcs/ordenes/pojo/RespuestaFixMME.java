package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class RespuestaFixMME {
	public String NRO_ENTRADA;
	public String FOLIO_TRANS;
	public String FECHA;
	public String NEMO;
	public String SEC_ORDEN;
	public BigDecimal CANTIDAD;
	public BigDecimal PRECIO;
	public BigDecimal CAN_ASIG_ACUM;
	public BigDecimal PRECIO_PROM;
	public String MENSAJE;
	public String ESTADO;
	public String PROCESADO;
	public String IN_SGC;
	public String MONEDA;
	public String BOLSA;
	public String CONDICION_LIQUIDACION;
	public String SPONSORING_FIRM;
	public String EXEC_ID;
	public String ORDER_ID;
	public String MERCADO;
	public String SUB_MERCADO;
	public String PREPAGO;
	public BigDecimal TASA_MAXIMA;
	public BigDecimal TASA_PROPIA;
	public BigDecimal TASA_ADJUDICACION;
	public String MEJOR_POSTURA;
	public String ID_REFERENCIA;
	public String FECHA_VENCIMIENTO;
	public String DIVISIBLE;
	public String OD;
	public String HORA_ADJUDICACION;
	public BigDecimal SEC_ORDER_OD;
	public String TIPO_OPERAC;
	public String OP_INTERNO;
	public String SEC_ORDEN_2;
	public String ID_CLIENTE;
	public BigDecimal SECUENCIA;
	public String ESOPTIMUS;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
