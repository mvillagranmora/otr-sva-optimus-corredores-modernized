package cl.bcs.plataforma.tests.utils.builders;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Arrays;

import cl.bcs.mesa.MRF.AnticiparOperacion;
import cl.bcs.mesa.MRF.AnticiparOperacion.AnticiparOperacionMesa_IN;
import cl.bcs.mesa.MRF.AnticiparOperacion.AnticiparOperacionMesa_OUT;
import cl.bcs.mesa.MRF.AnticiparOperacion.Operacion;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnticiparPactoBuilder {
	private AnticiparOperacionMesa_IN entradaAnticipo;
	private AnticiparOperacionMesa_OUT anticipo;
	
	public AnticiparPactoBuilder(Long idEmpresa) {
		String jsonAnticipo = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdInternoOperacion\":1,\"IdOperacion\":10865,\"FolioOperacion\":1845,\"IdCliente\":100024,\"IdPortafolio\":100039,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"Intermesa\":\"N\",\"IdMesaOrigen\":3,\"IdMesaDestino\":3,\"AfectaCustodia\":\"ENV\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"USD\",\"Monto\":23407845,\"MontoMoneda\":34946.47069361,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"USD\",\"Tasa\":0.5,\"ValorFinal\":23446858,\"ValorFinalMoneda\":34951,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-21\",\"IdEstadoSiguiente\":\"ANT\"}]}";
		this.entradaAnticipo = Utils.getGsonBuilder().fromJson(jsonAnticipo, AnticiparOperacionMesa_IN.class);
		this.entradaAnticipo.p_IdEmpresa = idEmpresa;
	}
	
	public AnticiparPactoBuilder setOperacion(CrearOperacionMesa_Operacion_OUT orig) {
		Operacion dest = entradaAnticipo.p_Operacion.get(0);

		dest.IdInternoOperacion = orig.IdInternoOperacion;
		dest.IdOperacion = orig.IdOperacion;
		dest.FolioOperacion = orig.FolioOperacion;
		dest.IdCliente = orig.IdCliente;
		dest.IdPortafolio = orig.IdPortafolio;
		dest.IdClienteCorredor = orig.IdClienteCorredor;
		dest.IdPortafolioCorredor = orig.IdPortafolioCorredor;
		dest.Intermesa = orig.Intermesa;
		dest.IdMesaOrigen = orig.IdMesaOrigen;
		dest.IdMesaDestino = orig.IdMesaDestino;
		dest.AfectaCustodia = orig.AfectaCustodia;
		dest.IdTipoProducto = orig.IdTipoProducto;
		dest.IdTipoOperacion = orig.IdTipoOperacion;
		dest.IdTipoReajuste = orig.IdTipoReajuste;
		dest.Monto = orig.Monto;
		dest.MontoMoneda = orig.MontoMoneda;
		dest.IdMoneda = orig.IdMoneda;
		dest.IdMonedaLiquidacion = orig.IdMonedaLiquidacion;
		dest.Tasa = orig.Tasa;
		dest.ValorFinal = orig.ValorFinal;
		dest.ValorFinalMoneda = orig.ValorFinalMoneda;
		dest.IdCondicionLiquidacion = orig.IdCondicionLiquidacion;
		dest.FechaLiquidacion = Fecha.toFormat(orig.FechaLiquidacion, "yyyy-MM-dd");
		dest.IdEstadoSiguiente = "ANT";
		
		System.out.println(dest);
		this.entradaAnticipo.p_Operacion = Arrays.asList(dest);
		return this;
	}
	
	public AnticiparOperacionMesa_OUT build(Connection conn) {
		AnticiparOperacion anticiparOperacion = new AnticiparOperacion();
		try {
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), this.entradaAnticipo.p_IdEmpresa);
			LocalDate ld = LocalDate.parse(fechaSistema, Fecha.YYYYMMDD);
			//1 - Cambiar fecha operacion a dia anterior
			SQL.exec(conn, "UPDATE BSCL.\"BSCL.Mesa.BD::MES.Operacion\" SET \"FechaOperacion\"='"+ld.minusDays(1).format(Fecha.YYYYMMDD)+" 00:00:00' WHERE \"IdOperacion\"="+entradaAnticipo.p_Operacion.get(0).IdOperacion);
			//2 - anticipar operacion
			this.anticipo  = anticiparOperacion.logica(conn, entradaAnticipo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.anticipo;
	}

}
