package cl.bcs.plataforma.frm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCampoFormulario implements JavaCall{
	
	private final String SQL_SELECT_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_MONEDA = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Moneda.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PAIS = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Pais.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_TELEFONO = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Telefono.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_GIRO = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Giro.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_SEGMENTO = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Segmento.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_AREA_CLIENTE = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Area_Cliente.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_CLIENTE_CMF = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Cliente_Cmf.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_EJECUTIVO = loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Ejecutivo.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_RANGO_PATRIMONIO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_RangoPatrimonio.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_TARJETA_BANCO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Tarjeta_Banco.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_BANCO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Banco.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_CONTACTO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Proposito_Contacto.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_RELACION= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Relacion.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ESTADO_CIVIL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Estado_Civil.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_CAUSA_NO_TIN= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Causa_No_Tin.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_CLASIFICACION_FATCA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Clasificacion_Fatca.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_BANCO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Banco.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ESTADO_TELEFONO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Estado_Telefono.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ESTADO_DIRECCION_FISICA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Estado_Direccion_Fisica.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ESTADO_DIRECCION_ELECTRONICA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Estado_Direccion_Electronica.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_BOVEDA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Boveda.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_CUENTA_CUSTODIA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Cuenta_Custodia.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PLAZA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Plaza.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_SUCURSAL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Sucursal.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_CUENTA_BANCO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Cuenta_Banco.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PROFESION= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Profesion.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ESTADO_NOMBRE_FANTASIA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Estado_Nombre_Fantasia.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_OPERACIONES_PORTAFOLIO_CLIENTE= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Operaciones_Portafolio_Cliente.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PREFERENCIA_PAGO_MEDIO_PAGO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Preferencia_Pago_Medio_Pago.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PREFERENCIA_PAGO_MONEDA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Preferencia_Pago_Moneda.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_OPERACION_ORIGEN_FONDOS= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Operacion_Origen_Fondos.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ATRIBUTO_ANOTACION_CLIENTE= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Atributo_Anotacion_Cliente.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PERFIL_COMERCIAL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Perfil_Comercial.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_CLASIFICACION_EMPRESA_FATCA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Clasificacion_Empresa_Fatca.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_AUTORIZADO_PORTAFOLIO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Autorizado_Portafolio.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_GRADO_VINCULACION_PARENTAL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Grado_Vinculacion_Parental.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_PORTAFOLIO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Proposito_Portafolio.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_SITUACION_LABORAL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Situacion_Laboral.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_DIVISION_PAIS= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Division_Pais.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PERFIL_COBROS= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Perfil_Cobros.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PLANTILLA_LIMITES= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Plantilla_Limites.sql");	
	private final String SQL_SELECT_CAMPO_FORMULARIO_PERFIL_PRODUCTOS= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Perfil_Productos.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PERSONA_DIRECCION_FISICA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Persona_Direccion_Fisica.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_NOMBRE_FANTASIA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Nombre_Fantasia.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PERSONA_TELEFONO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Persona_Telefono.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_TIPO_CARGO_PERSONA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Tipo_Cargo_Persona.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_CIUDAD= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Ciudad.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_CATEGORIA_INVERSIONISTA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Categoria_Inversionista.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_DE_SOCIO_NEGOCIO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Socio_De_Negocio.sql");	
	private final String SQL_SELECT_CAMPO_FORMULARIO_REPRESENTANTE_LEGAL= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Representante_Legal.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_RELACIONADO= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Relacionado.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_ATRIBUTO_ANOTACION= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Atributo_Anotacion.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_DETALLE_TIPO_INDICADOR= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Detalle_Tipo_Indicador.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_RANGO_RENTA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Rango_Renta.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_RANGO_NIVEL_VENTA= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Rango_Nivel_Venta.sql");
	private final String SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_TRANSACCION= loadResourceToString("cl/bcs/plataforma/frm/sql/ConsultarCampoFormulario_Select_Proposito_Transaccion.sql");



	static final Logger logger = LogManager.getLogger(ConsultarCampoFormulario.class.getName());
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarCampoFormulario_Entrada in = gson.fromJson(jin.toString(), ConsultarCampoFormulario_Entrada.class);
		ConsultarCampoFormulario_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class ConsultarCampoFormulario_Entrada{
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;

		//Parametros de ventana
		public String p_CodigoFormulario;
		public String p_CodigoCampo;
		public String p_IdTipoCanal;
				
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Moneda extends BcsPojo {
		public String IdMoneda;
		public String NombreLargo;
	}
	
	public static class Pais extends BcsPojo {
		public String IdPais;
		public String NombrePais;
	}	
	
	public static class TipoTelefono extends BcsPojo {
		public String IdTipoTelefono;
		public String Nombre;
	}

	public static class SocioDeNegocio extends BcsPojo {
		public String IdSocioDeNegocio;
		public String NombreCompleto;
	}
	
	public static class RepresentanteLegal extends BcsPojo {
		public String IdPersonaRepresentante;
		public String NombreCompletoRepresentante;
	}	
	
	public static class Relacionado extends BcsPojo {
		public String IdPersonaDestino;
		public String NombreCompleto;
	}	
		
	
	public static class Giro extends BcsPojo {
		public String IdGiro;
		public String Nombre;
	}	

	public static class TipoSegmento extends BcsPojo {
		public String IdSegmentoCliente;
		public String DescripcionSegmento;
	}		
	
	public static class AreaCliente extends BcsPojo {
		public String Codigo;
		public String Descripcion;
	}
		
	public static class TipoClienteCmf extends BcsPojo {
		public String IdDetalleIndicador;
		public String DetalleIndicador;
	}
	
	public static class Ejecutivo extends BcsPojo {
		public String IdEmpleadoEjecutivo;
		public String NombreCompleto;
	}
	
	public static class RangoPatrimonio extends BcsPojo {
		public String IdRangoPatrimonio;
		public String DescripcionRango;
	}
	
	public static class TipoTarjetaBanco extends BcsPojo {
		public String IdTipoTarjetaBancaria;
		public String NombreTipoTarjetaBancaria;
	}

	public static class Banco extends BcsPojo {
		public String IdBanco;
		public String NombreBancoPais;
	}	
	
	public static class PropositoContacto extends BcsPojo {
		public String IdPropositoMedioContacto;
		public String Descripcion;
	}	
	
	public static class TipoRelacion extends BcsPojo {
		public String IdTipoRelacion;
		public String NombreTipoRelacion;
	}	
	
	public static class EstadoCivil extends BcsPojo {
		public String IdTipoEstadoCivil;
		public String Nombre;
	}		
	
	public static class CausaNoTin extends BcsPojo {
		public String IdCausaNoTin;
		public String Descripcion;
	}
	
	public static class ClasificacionFatca extends BcsPojo {
		public String IdClasificacionFatca;
		public String Nombre;
	}	
	
	public static class TipoBanco extends BcsPojo {
		public String IdTipoBanco;
		public String NombreTipoBanco;
	}	
			
	public static class EstadoTelefono extends BcsPojo {
		public String IdEstado;
		public String Nombre;
	}	
	
	public static class EstadoDireccionFisica extends BcsPojo {
		public String IdEstado;
		public String Nombre;
	}	
	
	public static class EstadoDireccionElectronica extends BcsPojo {
		public String IdEstado;
		public String Nombre;
	}		
	
	public static class Boveda extends BcsPojo {
		public String IdBoveda;
		public String Nombre;
	}		
	
	public static class TipoCuentaCustodia extends BcsPojo {
		public String IdTipoCuentaCustodia;
		public String NombreTipoCuentaCustodia;
	}
	
	public static class Plaza extends BcsPojo {
		public String IdPlaza;
		public String NombrePlaza;
	}	
	
	public static class Sucursal extends BcsPojo {
		public String IdSucursal;
		public String NombreSucursal;
	}
	
	public static class TipoCuentaBanco extends BcsPojo {
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
	}	
	
	public static class Profesion extends BcsPojo {
		public String IdProfesion;
		public String Nombre;
	}	
	
	public static class EstadoNombreFantasia extends BcsPojo {
		public String IdEstado;
		public String Nombre;
	}
	
	public static class OperacionesPortafolioCliente extends BcsPojo {
		public String IdPreferenciaPago;		
		public String Nombre;
	}	
	
	public static class PreferenciaPagoMedioPago extends BcsPojo {
		public String IdMedioDePago;		
		public String Nombre;
	}
	
	public static class PreferenciaPagoMoneda extends BcsPojo {
		public String IdMoneda;		
		public String NombreMoneda;
	}	

	
	public static class TipoOperacionOrigenFondos extends BcsPojo {
		public String IdTipoOperacionOrigenFondos;		
		public String Nombre;
	}	

	
	public static class AtributoAnotacionCliente extends BcsPojo {
		public String IdAtributoAnotacion;		
		public String NombreAtributoAnotacion;
	}
	
	public static class AtributoAnotacion extends BcsPojo {
		public String IdAtributoAnotacion;		
		public String NombreAtributoAnotacion;
	}
	
	public static class PerfilComercial extends BcsPojo {
		public String IdPerfilComercialBase;		
		public String DescripcionPerfilComercialBase;
	}	

	public static class ClasificacionEmpresaFatca extends BcsPojo {
		public String IdClasificacionEmpresaFatca;		
		public String Nombre;
	}
	
	public static class TipoAutorizadoPortafolio extends BcsPojo {
		public String IdTipoAutorizado;		
		public String NombreTipoAutorizado;
	}
		
	public static class GradoVinculacionParental extends BcsPojo {
		public String IdGradoVinculacionParental;		
		public String Nombre;
	}	
	
	public static class PropositoPortafolio extends BcsPojo {
		public String IdPropositoPortafolio;		
		public String Descripcion;
	}
	
	public static class SituacionLaboral extends BcsPojo {
		public String CodigoSituacionLaboral;		
		public String SituacionLaboral;
	}
	
	public static class DivisionPais extends BcsPojo {
		public String IdDivisionPais;		
		public String NombreDivisionPaisClasificacion1;
	}	

	public static class PerfilCobros extends BcsPojo {
		public String IdPerfilCobros;		
		public String PerfilCobros;
	}
	
	public static class PlantillaLimites extends BcsPojo {
		public String IdPlantillaLimite;
		public String DescripcionPlantillaLimite;
	}	
	
	public static class PerfilProductos extends BcsPojo {
		public String IdPerfilProductos;		
		public String DescripcionPerfilProductos;
	}	
	
	
	public static class PersonaDireccionFisica extends BcsPojo {
		public String IdDireccionFisica;		
		public String NombreLargo;
	}
	
	public static class NombreFantasia extends BcsPojo {
		public String IdNombreFantasia;		
		public String DescripcionNombreFantasia;
	}
	
	public static class PersonaTelefono extends BcsPojo {
		public String IdTelefono;		
		public String Numero;
	}
	
	public static class TipoCargoPersona extends BcsPojo {
		public String CodigoTipoCargoPersona;		
		public String NombreTipoCargoPersona;
	}	

	public static class Ciudad extends BcsPojo {
		public String IdCiudad;		
		public String NombreCiudad;
	}		

	public static class CategoriaInversionista extends BcsPojo {
		public String IdCategoriaInversionista;		
		public String NombreCategoriaInversionista;
	}
	
	public static class DetalleTipoAtributo extends BcsPojo {
		public String IdDetalleIndicador;		
		public String DetalleIndicador;
	}	
	
	public static class DetalleTipoIndicador extends BcsPojo {
		public String CodigoTipoIndicador;		
		public String DetalleIndicador;
		public String Key;
		
	}	

	public static class RangoRenta extends BcsPojo{
		public String CodigoRangoSeleccion;
		public String Descripcion;

	}
	public static class RangoNivelVenta extends BcsPojo{
		public String CodigoRangoSeleccion;
		public String Descripcion;
	}
	public static class PropositoTransaccion extends BcsPojo{
		public String CodigoRangoSeleccion;
		public String Descripcion;
	}

	
	//Estructura de datos para la Grilla

	
	public static class consultarFormulario{
		public String CodigoFormulario;
		public String NombreFormulario;
		public String CodigoCampo;
		public String CodigoCampoFrontend;
		public String CodigoValorFrontend;
		public String CodigoCampoBackend;
		public String NombreCampo;
		public String TipoDato;
		public String ValorDefecto;
		public Long CantidadMinima;
		public String Requerido;
		public String RegeneraContratos;
		public String IdTipoCanal;
		public String NombreCanal;
		public String TipoCampoFrontend;
		public String ConfiguracionFrontend;
		public String ValorDefectoCampoFormulario;
		public String NombreValorDefectoCampoFormulario;
		public String Campo;
		public String AplicaRequerido;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Salida para la grilla
	public static class ConsultarCampoFormulario_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;		
		public List<consultarFormulario> P_OUT_FORMULARIO;		
		
		public ConsultarCampoFormulario_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_FORMULARIO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	public ConsultarCampoFormulario_Salida logica(Connection conn, ConsultarCampoFormulario_Entrada entrada) throws Exception{
		logger.info("INICIO CONSULTA CAMPO FORMULARIO");
		
		SQL _sql = new SQL();
		ConsultarCampoFormulario_Salida salida = new ConsultarCampoFormulario_Salida();
		
		HashMap<String,Object> params =  new HashMap<>();		
		List<consultarFormulario> consultarFormulario = new ArrayList <consultarFormulario>();				
		params.put("CodigoFormulario", entrada.p_CodigoFormulario);
		params.put("CodigoCampo", entrada.p_CodigoCampo);
		params.put("IdTipoCanal", entrada.p_IdTipoCanal);
		consultarFormulario = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO, params, consultarFormulario.class);
		

		if (consultarFormulario.size() > 0){						
			//combobox-moneda
			List<consultarFormulario> formularioMoneda = new ArrayList <consultarFormulario>();
			formularioMoneda = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-moneda") && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsMoneda = formularioMoneda.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsMoneda", idsMoneda);		
			
			List<Moneda> listaMoneda = new ArrayList <Moneda>();
			if (idsMoneda.size()> 0) {			
				listaMoneda = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_MONEDA, params, Moneda.class);
			}

			//combobox-pais
			List<consultarFormulario> formularioPais= new ArrayList <consultarFormulario>();
			formularioPais = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-pais")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsPais = formularioPais.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsPais", idsPais);		
			
			List<Pais> listaPais = new ArrayList <Pais>();
			if  (idsPais.size()> 0){
				listaPais = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PAIS, params, Pais.class);
			}
		
			//combobox-tipo-telefono
			List<consultarFormulario> formularioTipoTelefono= new ArrayList <consultarFormulario>();
			formularioTipoTelefono = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-telefono")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsTipoTelefono = formularioTipoTelefono.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsTipoTelefono", idsTipoTelefono);		
			
			List<TipoTelefono> listaTipoTelefono = new ArrayList <TipoTelefono>();
			if (idsTipoTelefono.size() > 0 ) {
				listaTipoTelefono = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_TELEFONO, params, TipoTelefono.class);
			}
		
			//combobox-giro		
			List<consultarFormulario> formularioGiro= new ArrayList <consultarFormulario>();
			formularioGiro = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-giro")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsGiro = formularioGiro.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsGiro", idsGiro);		
			
			List<Giro> listaGiro = new ArrayList <Giro>();
			if (idsGiro.size() > 0) {
				listaGiro = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_GIRO, params, Giro.class);
			}
		
			//combobox-tipo-segmento		
			List<consultarFormulario> formularioTipoSegmento= new ArrayList <consultarFormulario>();
			formularioTipoSegmento = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-segmento")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsSegmentoCliente = formularioTipoSegmento.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("idsSegmentoCliente", idsSegmentoCliente);		
			
			List<TipoSegmento> listaTipoSegmento = new ArrayList <TipoSegmento>();
			if (idsSegmentoCliente.size() > 0) {
				listaTipoSegmento = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_SEGMENTO, params, TipoSegmento.class);
			}
	
			//combobox-area-cliente		
			List<consultarFormulario> formularioAreaCliente= new ArrayList <consultarFormulario>();
			formularioAreaCliente = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-area-cliente")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> IdsCodigo = formularioAreaCliente.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsCodigo", IdsCodigo);		
			
			List<AreaCliente> listaAreaCliente = new ArrayList <AreaCliente>();
			if (IdsCodigo.size() > 0) {
				listaAreaCliente = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_AREA_CLIENTE, params, AreaCliente.class);
			}
	
			//combobox-tipo-cliente-cmf		
			List<consultarFormulario> formularioTipoClienteCmf= new ArrayList <consultarFormulario>();
			formularioTipoClienteCmf = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-cliente-cmf")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> IdsDetalleIndicador = formularioTipoClienteCmf.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("IdsDetalleIndicador", IdsDetalleIndicador);
				
			List<TipoClienteCmf> listaTipoClienteCmf = new ArrayList <TipoClienteCmf>();
			if (IdsDetalleIndicador.size() > 0) {
				listaTipoClienteCmf = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_CLIENTE_CMF, params, TipoClienteCmf.class);
			}	
			
		//combobox-ejecutivo			
			List<consultarFormulario> formularioEjecutivo= new ArrayList <consultarFormulario>();
			formularioEjecutivo = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-ejecutivo")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
		
			List<String> IdsEmpleadoEjecutivo = formularioEjecutivo.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsEmpleadoEjecutivo", IdsEmpleadoEjecutivo);
			
			List<Ejecutivo> listaEjecutivo = new ArrayList <Ejecutivo>();
			if (IdsEmpleadoEjecutivo.size() > 0) {					
				listaEjecutivo = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_EJECUTIVO, params, Ejecutivo.class);			
			}			
						
			//combobox-rango-patrimonio			
			List<consultarFormulario> formularioRangoPatrimonio= new ArrayList <consultarFormulario>();
			formularioRangoPatrimonio = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-rango-patrimonio")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
		
			List<String> IdsRangoPatrimonio = formularioRangoPatrimonio.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsRangoPatrimonio", IdsRangoPatrimonio);
			
			List<RangoPatrimonio> listaRangoPatrimonio = new ArrayList <RangoPatrimonio>();
			if (IdsRangoPatrimonio.size() > 0) {
				listaRangoPatrimonio = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_RANGO_PATRIMONIO, params, RangoPatrimonio.class);			
			}			
					
			//combobox-tipo-tarjeta-banco
			List<consultarFormulario> formularioTipoTarjetaBanco= new ArrayList <consultarFormulario>();
			formularioTipoTarjetaBanco = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-tarjeta-banco")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
		
			List<String> IdsTipoTarjetaBancaria = formularioTipoTarjetaBanco.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoTarjetaBancaria", IdsTipoTarjetaBancaria);
			List<TipoTarjetaBanco> listaTipoTarjetaBanco = new ArrayList <TipoTarjetaBanco>();
			if (IdsTipoTarjetaBancaria.size() > 0) {				
				listaTipoTarjetaBanco = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_TARJETA_BANCO, params, TipoTarjetaBanco.class);
			}			

			//combobox-banco
			List<consultarFormulario> formularioBanco= new ArrayList <consultarFormulario>();
			formularioBanco = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-banco")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> IdsBanco = formularioBanco.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsBanco", IdsBanco);
			List<Banco> listaBanco = new ArrayList <Banco>();
			if (IdsBanco.size() > 0) {						
				listaBanco = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_BANCO, params, Banco.class);
			}			
				
			//combobox-proposito-contacto
			List<consultarFormulario> formularioPropositoContacto= new ArrayList <consultarFormulario>();
			formularioPropositoContacto = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-proposito-contacto")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> IdsPropositoMedioContacto = formularioPropositoContacto.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPropositoMedioContacto", IdsPropositoMedioContacto);
			List<PropositoContacto> listaPropositoContacto = new ArrayList <PropositoContacto>();
			if (IdsPropositoMedioContacto.size() > 0) {				
				listaPropositoContacto = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_CONTACTO, params, PropositoContacto.class);
			}		
			
			//combobox-tipo-relacion			
			List<consultarFormulario> formularioTipoRelacion= new ArrayList <consultarFormulario>();
			formularioTipoRelacion = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-relacion")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoRelacion = formularioTipoRelacion.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoRelacion", IdsTipoRelacion);
			List<TipoRelacion> listaTipoRelacion = new ArrayList <TipoRelacion>();
			if (IdsTipoRelacion.size() > 0) {				
				listaTipoRelacion = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_RELACION, params, TipoRelacion.class);
			}			

			//combobox-estado-civil
			List<consultarFormulario> formularioEstadoCivil= new ArrayList <consultarFormulario>();
			formularioEstadoCivil = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-estado-civil")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoEstadoCivil = formularioEstadoCivil.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoEstadoCivil", IdsTipoEstadoCivil);

			List<EstadoCivil> listaEstadoCivil = new ArrayList <EstadoCivil>();
			if (IdsTipoEstadoCivil.size() > 0) {			
				listaEstadoCivil = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ESTADO_CIVIL, params, EstadoCivil.class);
			}			

			//combobox-causa-no-tin			
			List<consultarFormulario> formularioCausaNoTin= new ArrayList <consultarFormulario>();
			formularioCausaNoTin = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-causa-no-tin")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsCausaNoTin = formularioCausaNoTin.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsCausaNoTin", IdsCausaNoTin);
			List<CausaNoTin> listaCausaNoTin = new ArrayList <CausaNoTin>();
			if (IdsCausaNoTin.size() > 0) {				
				listaCausaNoTin = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_CAUSA_NO_TIN, params, CausaNoTin.class);
			}			
			
			//combobox-clasificacion-fatca			
			List<consultarFormulario> formularioClasificacionFatca= new ArrayList <consultarFormulario>();
			formularioClasificacionFatca = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-clasificacion-fatca")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsClasificacionFatca = formularioClasificacionFatca.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsClasificacionFatca", IdsClasificacionFatca);

			List<ClasificacionFatca> listaClasificacionFatca = new ArrayList <ClasificacionFatca>();
			if (IdsClasificacionFatca.size() > 0) {				
				listaClasificacionFatca = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_CLASIFICACION_FATCA, params, ClasificacionFatca.class);
			}			
			
			//combobox-tipo-banco
			List<consultarFormulario> formularioTipoBanco= new ArrayList <consultarFormulario>();
			formularioTipoBanco = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-banco")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoBanco = formularioTipoBanco.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoBanco", IdsTipoBanco);

			List<TipoBanco> listaTipoBanco = new ArrayList <TipoBanco>();
			if (IdsTipoBanco.size() > 0) {			
				listaTipoBanco = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_BANCO, params, TipoBanco.class);
			}			
			
			//combobox-estado-telefono
			List<consultarFormulario> formularioEstadoTelefono= new ArrayList <consultarFormulario>();
			formularioEstadoTelefono = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-estado-telefono")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsEstado = formularioEstadoTelefono.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsEstado", IdsEstado);
			
			List<EstadoTelefono> listaEstadoTelefono = new ArrayList <EstadoTelefono>();
			if (IdsEstado.size() > 0) {				
				listaEstadoTelefono = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ESTADO_TELEFONO, params, EstadoTelefono.class);
			}			
			
			//combobox-estado-direccion-fisica

			List<consultarFormulario> formularioEstadoDireccionFisica= new ArrayList <consultarFormulario>();
			formularioEstadoDireccionFisica = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-estado-direccion-fisica")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsEstadoDireccionFisica = formularioEstadoDireccionFisica.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsEstado", IdsEstadoDireccionFisica);
			
			List<EstadoDireccionFisica> listaEstadoDireccionFisica = new ArrayList <EstadoDireccionFisica>();
			if (IdsEstadoDireccionFisica.size() > 0) {				
				listaEstadoDireccionFisica = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ESTADO_DIRECCION_FISICA, params, EstadoDireccionFisica.class);
			}			

			//combobox-estado-direccion-electronica
			List<consultarFormulario> formularioEstadoDireccionElectronica= new ArrayList <consultarFormulario>();
			formularioEstadoDireccionElectronica = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-estado-direccion-electronica")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsEstadoDireccionElectronica = formularioEstadoDireccionElectronica.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsEstado", IdsEstadoDireccionElectronica);
			
			List<EstadoDireccionElectronica> listaEstadoDireccionElectronica = new ArrayList <EstadoDireccionElectronica>();
			if (IdsEstadoDireccionElectronica.size() > 0) {				
				listaEstadoDireccionElectronica = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ESTADO_DIRECCION_ELECTRONICA, params, EstadoDireccionElectronica.class);

			}			
			
			//combobox-boveda			
			List<consultarFormulario> formularioBoveda= new ArrayList <consultarFormulario>();
			formularioBoveda = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-boveda")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsBoveda = formularioBoveda.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsBoveda", IdsBoveda);
			
			List<Boveda> listaBoveda = new ArrayList <Boveda>();
			if (IdsBoveda.size() > 0) {				
				listaBoveda = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_BOVEDA, params, Boveda.class);
			}			

			//combobox-tipo-cuenta-custodia
			List<consultarFormulario> formularioTipoCuentaCustodia= new ArrayList <consultarFormulario>();
			formularioTipoCuentaCustodia = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-cuenta-custodia")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoCuentaCustodia = formularioTipoCuentaCustodia.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoCuentaCustodia", IdsTipoCuentaCustodia);

			List<TipoCuentaCustodia> listaTipoCuentaCustodia = new ArrayList <TipoCuentaCustodia>();
			if (IdsTipoCuentaCustodia.size() > 0) {			
				listaTipoCuentaCustodia = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_CUENTA_CUSTODIA, params, TipoCuentaCustodia.class);
			}			
			
			//combobox-plaza
			List<consultarFormulario> formularioPlaza= new ArrayList <consultarFormulario>();
			formularioPlaza = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-plaza")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPlaza = formularioPlaza.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPlaza", IdsPlaza);
			
			List<Plaza> listaPlaza = new ArrayList <Plaza>();
			if (IdsPlaza.size() > 0) {							
				listaPlaza = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PLAZA, params, Plaza.class);
			}			
			
			//combobox-sucursal
			List<consultarFormulario> formularioSucursal= new ArrayList <consultarFormulario>();
			formularioSucursal = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-sucursal")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());


			List<String> IdsSucursal = formularioSucursal.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsSucursal", IdsSucursal);

			List<Sucursal> listaSucursal = new ArrayList <Sucursal>();
			if (IdsSucursal.size() > 0) {				
				listaSucursal = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_SUCURSAL, params, Sucursal.class);
			}			
			
			//combobox-tipo-cuenta-banco
			List<consultarFormulario> formularioTpoCuentaBanco= new ArrayList <consultarFormulario>();
			formularioTpoCuentaBanco = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-cuenta-banco")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoCuentaBancaria = formularioTpoCuentaBanco.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoCuentaBancaria", IdsTipoCuentaBancaria);

			List<TipoCuentaBanco> listaTipoCuentaBanco = new ArrayList <TipoCuentaBanco>();
			if (IdsTipoCuentaBancaria.size() > 0) {				
				listaTipoCuentaBanco = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_CUENTA_BANCO, params, TipoCuentaBanco.class);
			}				

			//combobox-profesion
			List<consultarFormulario> formularioProfesion= new ArrayList <consultarFormulario>();
			formularioProfesion = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-profesion")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsProfesion = formularioProfesion.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsProfesion", IdsProfesion);
			
			List<Profesion> listaProfesion = new ArrayList <Profesion>();
			if (IdsProfesion.size() > 0) {				
				listaProfesion = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PROFESION, params, Profesion.class);
			}			

			//combobox-estado-nombre-fantasia			
			List<consultarFormulario> formularioEstadoNombreFantasia= new ArrayList <consultarFormulario>();
			formularioEstadoNombreFantasia = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-estado-nombre-fantasia")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsEstadoNombreFantasia = formularioEstadoNombreFantasia.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsEstadoNombreFantasia", IdsEstadoNombreFantasia);
			
			List<EstadoNombreFantasia> listaEstadoNombreFantasia = new ArrayList <EstadoNombreFantasia>();
			if (IdsEstadoNombreFantasia.size() > 0) {			
				listaEstadoNombreFantasia = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ESTADO_NOMBRE_FANTASIA, params, EstadoNombreFantasia.class);
			}			
			
			//combobox-operaciones-portafolio-cliente
			List<consultarFormulario> formularioOperacionesPortafolioCliente= new ArrayList <consultarFormulario>();
			formularioOperacionesPortafolioCliente = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-operaciones-portafolio-cliente")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPreferenciaPago = formularioOperacionesPortafolioCliente.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPreferenciaPago", IdsPreferenciaPago);

			List<OperacionesPortafolioCliente> listaOperacionesPortafolioCliente = new ArrayList <OperacionesPortafolioCliente>();
			if (IdsPreferenciaPago.size() > 0) {				
				listaOperacionesPortafolioCliente = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_OPERACIONES_PORTAFOLIO_CLIENTE, params, OperacionesPortafolioCliente.class);
			}			
			
			//combobox-preferencia-pago-medio-pago'
			List<consultarFormulario> formularioPreferenciaPagoMedioPago= new ArrayList <consultarFormulario>();
			formularioPreferenciaPagoMedioPago = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-preferencia-pago-medio-pago")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());


			List<String> IdsMedioDePago = formularioPreferenciaPagoMedioPago.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsMedioDePago", IdsMedioDePago);

			List<PreferenciaPagoMedioPago> listaPreferenciaPagoMedioPago = new ArrayList <PreferenciaPagoMedioPago>();
			if (IdsMedioDePago.size() > 0) {				
				listaPreferenciaPagoMedioPago = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PREFERENCIA_PAGO_MEDIO_PAGO, params, PreferenciaPagoMedioPago.class);
			}			

			//combobox-preferencia-pago-moneda
			List<consultarFormulario> formularioPreferenciaPagoMoneda= new ArrayList <consultarFormulario>();
			formularioPreferenciaPagoMoneda = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-preferencia-pago-moneda")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());


			List<String> IdsMoneda = formularioPreferenciaPagoMoneda.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsMoneda", IdsMoneda);

			List<PreferenciaPagoMoneda> listaPreferenciaPagoMoneda = new ArrayList <PreferenciaPagoMoneda>();
			if (IdsMoneda.size() > 0) {			
				listaPreferenciaPagoMoneda = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PREFERENCIA_PAGO_MONEDA, params, PreferenciaPagoMoneda.class);
			}			
			
			//combobox-tipo-operacion-origen-fondos'
			List<consultarFormulario> formularioTipoOperacionOrigenFondos= new ArrayList <consultarFormulario>();
			formularioTipoOperacionOrigenFondos = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-operacion-origen-fondos")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoOperacionOrigenFondos = formularioTipoOperacionOrigenFondos.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoOperacionOrigenFondos", IdsTipoOperacionOrigenFondos);
			params.put("IdsEmpresa", entrada.p_IdEmpresa);
			
			List<TipoOperacionOrigenFondos> listaTipoOperacionOrigenFondos = new ArrayList <TipoOperacionOrigenFondos>();
			if (IdsTipoOperacionOrigenFondos.size() > 0) {			
				listaTipoOperacionOrigenFondos = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_OPERACION_ORIGEN_FONDOS, params, TipoOperacionOrigenFondos.class);
			}			
			
			//combobox-atributo-anotacion-cliente
			List<consultarFormulario> formularioAtributoAnotacionCliente= new ArrayList <consultarFormulario>();
			formularioAtributoAnotacionCliente = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-atributo-anotacion-cliente")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsAtributoAnotacion = formularioAtributoAnotacionCliente.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsAtributoAnotacion", IdsAtributoAnotacion);
			
			List<AtributoAnotacionCliente> listaAtributoAnotacionCliente = new ArrayList <AtributoAnotacionCliente>();
			if (IdsAtributoAnotacion.size() > 0) {							
				listaAtributoAnotacionCliente = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ATRIBUTO_ANOTACION_CLIENTE, params, AtributoAnotacionCliente.class);
			}			

			//combobox-perfil-comercial
			List<consultarFormulario> formularioPerfilComercial= new ArrayList <consultarFormulario>();
			formularioPerfilComercial = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-perfil-comercial")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPerfilComercialBase = formularioPerfilComercial.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPerfilComercialBase", IdsPerfilComercialBase);

			List<PerfilComercial> listaPerfilComercial = new ArrayList <PerfilComercial>();
			if (IdsPerfilComercialBase.size() > 0) {					
				listaPerfilComercial = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PERFIL_COMERCIAL, params, PerfilComercial.class);
			}			
			
			//combobox-clasificacion-empresa-fatca
			List<consultarFormulario> formularioClasificacionEmpresaFatca= new ArrayList <consultarFormulario>();
			formularioClasificacionEmpresaFatca = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-clasificacion-empresa-fatca")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsClasificacionEmpresaFatca = formularioClasificacionEmpresaFatca.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsClasificacionEmpresaFatca", IdsClasificacionEmpresaFatca);

			List<ClasificacionEmpresaFatca> listaClasificacionEmpresaFatca = new ArrayList <ClasificacionEmpresaFatca>();
			if (IdsClasificacionEmpresaFatca.size() > 0) {							
				listaClasificacionEmpresaFatca = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_CLASIFICACION_EMPRESA_FATCA, params, ClasificacionEmpresaFatca.class);
			}			
			
			//combobox-tipo-autorizado-portafolio			
			List<consultarFormulario> formularioTipoAutorizadoPortafolio= new ArrayList <consultarFormulario>();
			formularioTipoAutorizadoPortafolio = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-autorizado-portafolio")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTipoAutorizado = formularioTipoAutorizadoPortafolio.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTipoAutorizado", IdsTipoAutorizado);
			params.put("IdsEmpresa", entrada.p_IdEmpresa);

			List<TipoAutorizadoPortafolio> listaTipoAutorizadoPortafolio = new ArrayList <TipoAutorizadoPortafolio>();
			if (IdsTipoAutorizado.size() > 0) {				
				listaTipoAutorizadoPortafolio = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_AUTORIZADO_PORTAFOLIO, params, TipoAutorizadoPortafolio.class);
			}			

			//combobox-grado-vinculacion-parental
			List<consultarFormulario> formularioGradoVinculacionParental= new ArrayList <consultarFormulario>();
			formularioGradoVinculacionParental = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-grado-vinculacion-parental")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsGradoVinculacionParental = formularioGradoVinculacionParental.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsGradoVinculacionParental", IdsGradoVinculacionParental);
			
			List<GradoVinculacionParental> listaGradoVinculacionParental = new ArrayList <GradoVinculacionParental>();
			if (IdsGradoVinculacionParental.size() > 0) {				
				listaGradoVinculacionParental = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_GRADO_VINCULACION_PARENTAL, params, GradoVinculacionParental.class);
			}			
			
			//combobox-proposito-portafolio
			List<consultarFormulario> formularioPropositoPortafolio= new ArrayList <consultarFormulario>();
			formularioPropositoPortafolio = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-proposito-portafolio")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPropositoPortafolio = formularioPropositoPortafolio.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPropositoPortafolio", IdsPropositoPortafolio);

			List<PropositoPortafolio> listaPropositoPortafolio = new ArrayList <PropositoPortafolio>();
			if (IdsPropositoPortafolio.size() > 0) {				
				listaPropositoPortafolio = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_PORTAFOLIO, params, PropositoPortafolio.class);
			}			

			//combobox-situacion-laboral			
			List<consultarFormulario> formularioSituacionLaboral= new ArrayList <consultarFormulario>();
			formularioSituacionLaboral = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-situacion-laboral")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsCodigoSituacionLaboral = formularioSituacionLaboral.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsCodigoSituacionLaboral", IdsCodigoSituacionLaboral);

			List<SituacionLaboral> listaSituacionLaboral = new ArrayList <SituacionLaboral>();
			if (IdsCodigoSituacionLaboral.size() > 0) {
				listaSituacionLaboral = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_SITUACION_LABORAL, params, SituacionLaboral.class);

			}			
			
			//combobox-division-pais
			List<consultarFormulario> formularioDivisionPais= new ArrayList <consultarFormulario>();
			formularioDivisionPais = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-division-pais")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsDivisionPais = formularioDivisionPais.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsDivisionPais", IdsDivisionPais);
			
			List<DivisionPais> listaDivisionPais = new ArrayList <DivisionPais>();
			if (IdsDivisionPais.size() > 0) {			
				listaDivisionPais = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_DIVISION_PAIS, params, DivisionPais.class);
			}			
			
			//combobox-perfil-cobros
			List<consultarFormulario> formularioPerfilCobros= new ArrayList <consultarFormulario>();
			formularioPerfilCobros = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-perfil-cobros")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPerfilCobros = formularioPerfilCobros.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPerfilCobros", IdsPerfilCobros);

			List<PerfilCobros> listaPerfilCobros = new ArrayList <PerfilCobros>();
			if (IdsPerfilCobros.size() > 0) {				
				listaPerfilCobros = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PERFIL_COBROS, params, PerfilCobros.class);
			}			
			
			//combobox-plantilla-limites
			List<consultarFormulario> formularioPlantillaLimites= new ArrayList <consultarFormulario>();
			formularioPlantillaLimites = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-plantilla-limites")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPlantillaLimite = formularioPlantillaLimites.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPlantillaLimite", IdsPlantillaLimite);

			List<PlantillaLimites> listaPlantillaLimites = new ArrayList <PlantillaLimites>();
			if (IdsPlantillaLimite.size() > 0) {			
				listaPlantillaLimites = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PLANTILLA_LIMITES, params, PlantillaLimites.class);
			}			

			//combobox-perfil-productos
			List<consultarFormulario> formularioPerfilProductos= new ArrayList <consultarFormulario>();
			formularioPerfilProductos = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-perfil-productos")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsPerfilProductos = formularioPerfilProductos.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsPerfilProductos", IdsPerfilProductos);

			List<PerfilProductos> listaPerfilProductos = new ArrayList <PerfilProductos>();
			if (IdsPerfilProductos.size() > 0) {				
				listaPerfilProductos = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PERFIL_PRODUCTOS, params, PerfilProductos.class);
			}			
			
			//combobox-persona-direccion-fisica
			List<consultarFormulario> formularioPersonaDireccionFisica= new ArrayList <consultarFormulario>();
			formularioPersonaDireccionFisica = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-persona-direccion-fisica")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsDireccionFisica = formularioPersonaDireccionFisica.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsDireccionFisica", IdsDireccionFisica);

			List<PersonaDireccionFisica> listaPersonaDireccionFisica = new ArrayList <PersonaDireccionFisica>();
			if (IdsDireccionFisica.size() > 0) {
				listaPersonaDireccionFisica = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PERSONA_DIRECCION_FISICA, params, PersonaDireccionFisica.class);
			}			
			

			//combobox-nombre-fantasia
			List<consultarFormulario> formularioNombreFantasia= new ArrayList <consultarFormulario>();
			formularioNombreFantasia = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-nombre-fantasia")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsNombreFantasia = formularioNombreFantasia.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsNombreFantasia", IdsNombreFantasia);

			List<NombreFantasia> listaNombreFantasia = new ArrayList <NombreFantasia>();
			if (IdsNombreFantasia.size() > 0) {
				listaNombreFantasia = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_NOMBRE_FANTASIA, params, NombreFantasia.class);
			}			
			
			//combobox-persona-telefono
			List<consultarFormulario> formularioPersonaTelefono= new ArrayList <consultarFormulario>();
			formularioPersonaTelefono = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-persona-telefono")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsTelefono = formularioPersonaTelefono.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsTelefono", IdsTelefono);

			List<PersonaTelefono> listaPersonaTelefono = new ArrayList <PersonaTelefono>();
			if (IdsTelefono.size() > 0) {
				listaPersonaTelefono = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PERSONA_TELEFONO, params, PersonaTelefono.class);
			}		

			//combobox-tipo-cargo-persona
			List<consultarFormulario> formularioTipoCargoPersona= new ArrayList <consultarFormulario>();
			formularioTipoCargoPersona = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-tipo-cargo-persona")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsCodigoTipoCargoPersona = formularioTipoCargoPersona.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsCodigoTipoCargoPersona", IdsCodigoTipoCargoPersona);

			List<TipoCargoPersona> listaTipoCargoPersona = new ArrayList <TipoCargoPersona>();
			if (IdsCodigoTipoCargoPersona.size() > 0) {
				listaTipoCargoPersona = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_TIPO_CARGO_PERSONA, params, TipoCargoPersona.class);
			}			
			
			//combobox-ciudad
			List<consultarFormulario> formularioCiudad= new ArrayList <consultarFormulario>();
			formularioCiudad = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-ciudad")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsCiudad = formularioCiudad.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsCiudad", IdsCiudad);

			List<Ciudad> listaCiudad = new ArrayList <Ciudad>();
			if (IdsCiudad.size() > 0) {
				listaCiudad = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_CIUDAD, params, Ciudad.class);
			}			
			
			//combobox-categoria-inversionista
			List<consultarFormulario> formularioCategoriaInversionista= new ArrayList <consultarFormulario>();
			formularioCategoriaInversionista = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-categoria-inversionista")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdsCategoriaInversionista = formularioCategoriaInversionista.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdsCategoriaInversionista", IdsCategoriaInversionista);
			params.put("IdsEmpresa", entrada.p_IdEmpresa);

			List<CategoriaInversionista> listaCategoriaInversionista = new ArrayList <CategoriaInversionista>();
			if (IdsCategoriaInversionista.size() > 0) {
				listaCategoriaInversionista = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_CATEGORIA_INVERSIONISTA, params, CategoriaInversionista.class);
			}	
			
			//combobox-socio-negocio			
			List<consultarFormulario> formularioSocioNegocio= new ArrayList <consultarFormulario>();
			formularioSocioNegocio = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-socio-negocio")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdSocioDeNegocio = formularioSocioNegocio.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdSocioDeNegocio", IdSocioDeNegocio);

			List<SocioDeNegocio> listaSocioDeNegocio = new ArrayList <SocioDeNegocio>();
			if (IdSocioDeNegocio.size() > 0) {
				listaSocioDeNegocio = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_DE_SOCIO_NEGOCIO, params, SocioDeNegocio.class);
			}	
			
			//combobox-representante-legal			
			List<consultarFormulario> formulariorRepresentanteLegal= new ArrayList <consultarFormulario>();
			formulariorRepresentanteLegal = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-representante-legal")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdPersonaRepresentante = formulariorRepresentanteLegal.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdPersonaRepresentante", IdPersonaRepresentante);

			List<RepresentanteLegal> listaRepresentanteLegal= new ArrayList <RepresentanteLegal>();
			if (IdPersonaRepresentante.size() > 0) {
				listaRepresentanteLegal = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_REPRESENTANTE_LEGAL, params, RepresentanteLegal.class);
			}
			
			//combobox-relacionado			
			List<consultarFormulario> formulariorRelacionado= new ArrayList <consultarFormulario>();
			formulariorRelacionado = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-relacionado")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdPersonaDestino = formulariorRelacionado.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdPersonaDestino", IdPersonaDestino);

			List<Relacionado> listaRelacionado= new ArrayList <Relacionado>();
			if (IdPersonaDestino.size() > 0) {
				listaRelacionado = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_RELACIONADO, params, Relacionado.class);
			}
			
			//combobox-atributo-anotacion
			List<consultarFormulario> formularioAtributoAnotacion= new ArrayList <consultarFormulario>();
			formularioAtributoAnotacion = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-atributo-anotacion")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdAtributoAnotacion = formularioAtributoAnotacion.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdAtributoAnotacion", IdAtributoAnotacion);
			
			
			List<AtributoAnotacion> listaAtributoAnotacion = new ArrayList <AtributoAnotacion>();
			if (IdAtributoAnotacion.size() > 0) {							
				listaAtributoAnotacion = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_ATRIBUTO_ANOTACION, params, AtributoAnotacion.class);
			}	
			
			//combobox-detalle-tipo-atributo
			List<consultarFormulario> formularioDetalleTipoIndicador= new ArrayList <consultarFormulario>();
			formularioDetalleTipoIndicador = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-detalle-tipo-atributo")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());

			List<String> IdDetalleIndicador = formularioDetalleTipoIndicador.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
				.collect(Collectors.toList());
			params.clear();
			params.put("IdDetalleIndicador", IdDetalleIndicador);
			
			
			List<DetalleTipoIndicador> listaDetalleTipoIndicador = new ArrayList <DetalleTipoIndicador>();
			if (IdDetalleIndicador.size() > 0) {							
				listaDetalleTipoIndicador = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_DETALLE_TIPO_INDICADOR, params, DetalleTipoIndicador.class);
			}
//			//combobox-rango-renta
			List<consultarFormulario> formularioRangoRenta= new ArrayList <consultarFormulario>();
			formularioRangoRenta = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-rango-renta")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsRangoRenta = formularioRangoRenta.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("idsRangoRenta", idsRangoRenta);		
			
			List<RangoRenta> listaRangoRenta = new ArrayList <RangoRenta>();
			if (idsRangoRenta.size() > 0 ) {
				listaRangoRenta = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_RANGO_RENTA, params, RangoRenta.class);
			}

			//combobox-rango-nivel-venta
			List<consultarFormulario> formularioRangoNivelVenta= new ArrayList <consultarFormulario>();
			formularioRangoNivelVenta = consultarFormulario.stream()
			.filter(s -> s.Campo.equals("combobox-rango-nivel-venta")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsRangonivelVenta = formularioRangoNivelVenta.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("idsRangonivelVenta", idsRangonivelVenta);		
			
			List<RangoNivelVenta> listaRangoNivelVenta = new ArrayList <RangoNivelVenta>();
			if (idsRangonivelVenta.size() > 0 ) {
				listaRangoNivelVenta = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_RANGO_NIVEL_VENTA, params, RangoNivelVenta.class);
			}

			//combobox-proposito-transaccion
			List<consultarFormulario> formularioPropositoTransaccion= new ArrayList <consultarFormulario>();
		formularioPropositoTransaccion = consultarFormulario.stream()
		.filter(s -> s.Campo.equals("combobox-proposito-transaccion")  && !s.ValorDefectoCampoFormulario.equals(""))
			.collect(Collectors.toList());
			
			List<String> idsPropositoTransaccion = formularioPropositoTransaccion.parallelStream().map(s -> s.ValorDefectoCampoFormulario)
					.collect(Collectors.toList());
				params.clear();
				params.put("idsPropositoTransaccion", idsPropositoTransaccion);		
			
		List<PropositoTransaccion> listaPropositoTransaccion = new ArrayList <PropositoTransaccion>();
			if (idsPropositoTransaccion.size() > 0 ) {
				listaPropositoTransaccion = _sql.query(conn, SQL_SELECT_CAMPO_FORMULARIO_PROPOSITO_TRANSACCION, params, PropositoTransaccion.class);
			}	
			
			HashMap<String, Object> hashMonedas = Utils.List2Hash(listaMoneda, "IdMoneda");
			HashMap<String, Object> hashPais = Utils.List2Hash(listaPais, "IdPais");
			HashMap<String, Object> hashTipoTelefono = Utils.List2Hash(listaTipoTelefono, "IdTipoTelefono");
			HashMap<String, Object> hashGiro = Utils.List2Hash(listaGiro, "IdGiro");			
			HashMap<String, Object> hashTipoSegmento = Utils.List2Hash(listaTipoSegmento, "IdSegmentoCliente");
			HashMap<String, Object> hashAreaCliente = Utils.List2Hash(listaAreaCliente, "Codigo");
			HashMap<String, Object> hashTipoClienteCmf = Utils.List2Hash(listaTipoClienteCmf, "IdDetalleIndicador");			
			HashMap<String, Object> hashEjecutivo = Utils.List2Hash(listaEjecutivo, "IdEmpleadoEjecutivo");
			HashMap<String, Object> hashTipoTarjetaBanco = Utils.List2Hash(listaTipoTarjetaBanco, "IdTipoTarjetaBancaria");
			HashMap<String, Object> hashBanco = Utils.List2Hash(listaBanco, "IdBanco");
			HashMap<String, Object> hashPropositoContacto = Utils.List2Hash(listaPropositoContacto, "IdPropositoMedioContacto");
			HashMap<String, Object> hashTipoRelacion = Utils.List2Hash(listaTipoRelacion, "IdTipoRelacion");		
			HashMap<String, Object> hashEstadoCivil = Utils.List2Hash(listaEstadoCivil, "IdTipoEstadoCivil");
			HashMap<String, Object> hashCausaNoTin = Utils.List2Hash(listaCausaNoTin, "IdCausaNoTin");
			HashMap<String, Object> hashClasificacionFatca = Utils.List2Hash(listaClasificacionFatca, "IdClasificacionFatca");
			HashMap<String, Object> hashRangoPatrimonio = Utils.List2Hash(listaRangoPatrimonio, "IdRangoPatrimonio");			
			HashMap<String, Object> hashTipoBanco = Utils.List2Hash(listaTipoBanco, "IdTipoBanco");			
			HashMap<String, Object> hashEstadoTelefono = Utils.List2Hash(listaEstadoTelefono, "IdEstado");		
			HashMap<String, Object> hashEstadoDireccionFisica = Utils.List2Hash(listaEstadoDireccionFisica, "IdEstado");
			HashMap<String, Object> hashEstadoDireccionElectronica = Utils.List2Hash(listaEstadoDireccionElectronica, "IdEstado");
			HashMap<String, Object> hashBoveda = Utils.List2Hash(listaBoveda, "IdBoveda");
			HashMap<String, Object> hashTipoCuentaCustodia = Utils.List2Hash(listaTipoCuentaCustodia, "IdTipoCuentaCustodia");
			HashMap<String, Object> hashPlaza = Utils.List2Hash(listaPlaza, "IdPlaza");
			HashMap<String, Object> hashSucursal = Utils.List2Hash(listaSucursal, "IdSucursal");
			HashMap<String, Object> hashTipoCuentaBanco = Utils.List2Hash(listaTipoCuentaBanco, "IdTipoCuentaBancaria");
			HashMap<String, Object> hashProfesion = Utils.List2Hash(listaProfesion, "IdProfesion");
			HashMap<String, Object> hashEstadoNombreFantasia = Utils.List2Hash(listaEstadoNombreFantasia, "IdEstado");
			HashMap<String, Object> hashOperacionesPortafolioCliente = Utils.List2Hash(listaOperacionesPortafolioCliente, "IdPreferenciaPago");
			HashMap<String, Object> hashPreferenciaPagoMedioPago = Utils.List2Hash(listaPreferenciaPagoMedioPago, "IdMedioDePago");
			HashMap<String, Object> hashPreferenciaPagoMoneda = Utils.List2Hash(listaPreferenciaPagoMoneda, "IdMoneda");
			HashMap<String, Object> hashTipoOperacionOrigenFondos = Utils.List2Hash(listaTipoOperacionOrigenFondos, "IdTipoOperacionOrigenFondos");
			HashMap<String, Object> hashAtributoAnotacionCliente = Utils.List2Hash(listaAtributoAnotacionCliente, "IdAtributoAnotacion");
			HashMap<String, Object> hashAtributoAnotacion = Utils.List2Hash(listaAtributoAnotacion, "IdAtributoAnotacion");			
			HashMap<String, Object> hashPerfilComercial = Utils.List2Hash(listaPerfilComercial, "IdPerfilComercialBase");
			HashMap<String, Object> hashClasificacionEmpresaFatca = Utils.List2Hash(listaClasificacionEmpresaFatca, "IdClasificacionEmpresaFatca");
			HashMap<String, Object> hashTipoAutorizadoPortafolio = Utils.List2Hash(listaTipoAutorizadoPortafolio, "IdTipoAutorizado");
			HashMap<String, Object> hashGradoVinculacionParental = Utils.List2Hash(listaGradoVinculacionParental, "IdGradoVinculacionParental");
			HashMap<String, Object> hashPropositoPortafolio = Utils.List2Hash(listaPropositoPortafolio, "IdPropositoPortafolio");
			HashMap<String, Object> hashSituacionLaboral = Utils.List2Hash(listaSituacionLaboral, "CodigoSituacionLaboral");
			HashMap<String, Object> hashDivisionPais = Utils.List2Hash(listaDivisionPais, "IdDivisionPais");
			HashMap<String, Object> hashPerfilCobros = Utils.List2Hash(listaPerfilCobros, "IdPerfilCobros");
			HashMap<String, Object> hashPlantillaLimites = Utils.List2Hash(listaPlantillaLimites, "IdPlantillaLimite");
			HashMap<String, Object> hashPerfilProductos = Utils.List2Hash(listaPerfilProductos, "IdPerfilProductos");
			HashMap<String, Object> hashPersonaDireccionFisica = Utils.List2Hash(listaPersonaDireccionFisica, "IdDireccionFisica");
			HashMap<String, Object> hashNombreFantasia = Utils.List2Hash(listaNombreFantasia, "IdNombreFantasia");
			HashMap<String, Object> hashPersonaTelefono = Utils.List2Hash(listaPersonaTelefono, "IdTelefono");
			HashMap<String, Object> hashTipoCargoPersona = Utils.List2Hash(listaTipoCargoPersona, "CodigoTipoCargoPersona");
			HashMap<String, Object> hashCiudad = Utils.List2Hash(listaCiudad, "IdCiudad");
			HashMap<String, Object> hashCategoriaInversionista = Utils.List2Hash(listaCategoriaInversionista, "IdCategoriaInversionista");
			HashMap<String, Object> hashSocioDeNegocio = Utils.List2Hash(listaSocioDeNegocio, "IdSocioDeNegocio");
			HashMap<String, Object> hashRepresentanteLegal = Utils.List2Hash(listaRepresentanteLegal, "IdPersonaRepresentante");
			HashMap<String, Object> hashRelacionado = Utils.List2Hash(listaRelacionado, "IdPersonaDestino");
			HashMap<String, Object> hashDetalleTipoIndicador = Utils.List2Hash(listaDetalleTipoIndicador, "Key");			
			HashMap<String, Object> hashRangoRenta = Utils.List2Hash(listaRangoRenta, "CodigoRangoSeleccion");
			HashMap<String, Object> hashRangoNivelVenta = Utils.List2Hash(listaRangoNivelVenta, "CodigoRangoSeleccion");
			HashMap<String, Object> hashPropositoTransaccion = Utils.List2Hash(listaPropositoTransaccion, "CodigoRangoSeleccion");

			
			
			

			List<consultarFormulario> salidaFormulario = new ArrayList <consultarFormulario>();			
			for (consultarFormulario recorreFormulario: consultarFormulario) {				
				recorreFormulario.NombreValorDefectoCampoFormulario = recorreFormulario.ValorDefectoCampoFormulario;				
				if (recorreFormulario.TipoCampoFrontend.equals("bcs-combobox-base") && !recorreFormulario.ValorDefectoCampoFormulario.equals("") && !recorreFormulario.ValorDefectoCampoFormulario.equals("false")) {
					
					switch(recorreFormulario.Campo)
					{					
					   case "combobox-moneda" ://PERSONA CUENTA BANCARIA FULL NATURAL - MONEDA - OPTIMUS		
						  Moneda moneda = (Moneda) hashMonedas.get(recorreFormulario.ValorDefectoCampoFormulario); 
						  if( moneda != null && recorreFormulario.ValorDefectoCampoFormulario.equals(moneda.IdMoneda)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = moneda.NombreLargo;							  
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;					   
					   case "combobox-clasificacion-fatca" ://SIN DATA
						  ClasificacionFatca clasificacionFatca = (ClasificacionFatca) hashClasificacionFatca.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( clasificacionFatca != null && recorreFormulario.ValorDefectoCampoFormulario.equals(clasificacionFatca.IdClasificacionFatca)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = clasificacionFatca.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-pais" ://PERSONA FATCA CRS PAIS DE RESIDENCIA FULL NATURAL - 	PAIS RESIDENCIA - OPTIMUS
						  Pais pais = (Pais) hashPais.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( pais != null && recorreFormulario.ValorDefectoCampoFormulario.equals(pais.IdPais)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = pais.NombrePais;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-telefono" ://PERSONA TELEFONO FULL NATURAL - TIPO TELEFONO -	WEB SERVICE  
						  TipoTelefono tipoTelefono = (TipoTelefono) hashTipoTelefono.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoTelefono != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoTelefono.IdTipoTelefono)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoTelefono.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
						  break;
					   case "combobox-giro" ://PERSONA DATOS GENERALES FULL NATURAL - GIRO - OPTIMUS
						  Giro giro = (Giro) hashGiro.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( giro != null && recorreFormulario.ValorDefectoCampoFormulario.equals(giro.IdGiro)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = giro.Nombre;
						  }						  
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-segmento" ://CLIENTE DATOS GENERALES FULL NATURAL - SEGMENTO CLIENTE - OPTIMUS
						  TipoSegmento tipoSegmento = (TipoSegmento) hashTipoSegmento.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoSegmento != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoSegmento.IdSegmentoCliente)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoSegmento.DescripcionSegmento;
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-area-cliente" ://INGRESAR CLIENTE FULL NATURAL - AREA CLIENTE - OPTIMUS
						  AreaCliente areaCliente = (AreaCliente) hashAreaCliente.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( areaCliente != null && recorreFormulario.ValorDefectoCampoFormulario.equals(areaCliente.Codigo)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = areaCliente.Descripcion;							  
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-cliente-cmf" ://INGRESAR CLIENTE FULL JURIDICA - TIPO CLIENTE CMF - 	OPTIMUS
						  TipoClienteCmf tipoClienteCmf = (TipoClienteCmf) hashTipoClienteCmf.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoClienteCmf != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoClienteCmf.IdDetalleIndicador)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoClienteCmf.DetalleIndicador;
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-ejecutivo" ://INGRESAR CLIENTE FULL JURIDICA + EJECUTIVO + OPENWORKPLACE
						  Ejecutivo ejecutivo = (Ejecutivo) hashEjecutivo.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( ejecutivo != null && recorreFormulario.ValorDefectoCampoFormulario.equals(ejecutivo.IdEmpleadoEjecutivo)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = ejecutivo.NombreCompleto;
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-rango-patrimonio" ://PERSONA DATOS GENERALES FULL NATURAL - RANGO PATRIMONIO - 	OPTIMUS
						  RangoPatrimonio rangoPatrimonio = (RangoPatrimonio) hashRangoPatrimonio.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( rangoPatrimonio != null && recorreFormulario.ValorDefectoCampoFormulario.equals(rangoPatrimonio.IdRangoPatrimonio)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = rangoPatrimonio.DescripcionRango;
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-tarjeta-banco" ://PERSONA TARJETAS BANCARIAS FULL JURIDICA - TIPO TARJETA - OPENWORKPLACE 
						  TipoTarjetaBanco tipoTarjetaBanco = (TipoTarjetaBanco) hashTipoTarjetaBanco.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoTarjetaBanco != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoTarjetaBanco.IdTipoTarjetaBancaria)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoTarjetaBanco.NombreTipoTarjetaBancaria;						  
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-banco" ://PERSONA TARJETAS BANCARIAS FULL NATURAL - BANCO - OPENWORKPLACE
						   Banco banco = (Banco) hashBanco.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( banco != null && recorreFormulario.ValorDefectoCampoFormulario.equals(banco.IdBanco)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = banco.NombreBancoPais;							  
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-proposito-contacto" ://PERSONA DIRECCION ELECTRONICA FULL NATURAL - PROPOSITO EMAIL - WEB SERVICE
						  PropositoContacto propositoContacto = (PropositoContacto) hashPropositoContacto.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( propositoContacto != null && recorreFormulario.ValorDefectoCampoFormulario.equals(propositoContacto.IdPropositoMedioContacto)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = propositoContacto.Descripcion;
						  }				
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-relacion" ://PERSONA RELACIONES FULL JURIDICA - 	Tipo Relacion - OPENWORKPLACE
						  TipoRelacion tipoRelacion = (TipoRelacion) hashTipoRelacion.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoRelacion != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoRelacion.IdTipoRelacion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoRelacion.NombreTipoRelacion;
						  }	
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-estado-civil" ://SIN DATA
						   EstadoCivil estadoCivil = (EstadoCivil) hashEstadoCivil.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( estadoCivil != null && recorreFormulario.ValorDefectoCampoFormulario.equals(estadoCivil.IdTipoEstadoCivil)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = estadoCivil.Nombre;
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-causa-no-tin" ://SIN DATA
						  CausaNoTin causaNoTin = (CausaNoTin) hashCausaNoTin.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( causaNoTin != null && recorreFormulario.ValorDefectoCampoFormulario.equals(causaNoTin.IdCausaNoTin)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = causaNoTin.Descripcion;
						  }		
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-banco" ://PERSONA CUENTA BANCARIA FULL NATURAL - NOMBRE BANCO - OPTIMUS
						  TipoBanco tipoBanco = (TipoBanco) hashTipoBanco.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoBanco != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoBanco.IdTipoBanco)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = tipoBanco.NombreTipoBanco;
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-estado-telefono" ://PERSONA TELEFONO FULL NATURAL - ESTADO - 	OPTIMUS
						   EstadoTelefono estadoTelefono = (EstadoTelefono) hashEstadoTelefono.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( estadoTelefono != null && recorreFormulario.ValorDefectoCampoFormulario.equals(estadoTelefono.IdEstado)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = estadoTelefono.Nombre;							  
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-estado-direccion-fisica" ://PERSONA DIRECCION FISICA FULL NATURAL - ESTADO - OPTIMUS
						   EstadoDireccionFisica estadoDireccionFisica = (EstadoDireccionFisica) hashEstadoDireccionFisica.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( estadoDireccionFisica != null && recorreFormulario.ValorDefectoCampoFormulario.equals(estadoDireccionFisica.IdEstado)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = estadoDireccionFisica.Nombre;
						  }				
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-estado-direccion-electronica" ://PERSONA DIRECCION ELECTRONICA FULL NATURAL - ESTADO EMAIL - 	OPTIMUS
						   EstadoDireccionElectronica estadoDireccionElectronica = (EstadoDireccionElectronica) hashEstadoDireccionElectronica.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( estadoDireccionElectronica != null && recorreFormulario.ValorDefectoCampoFormulario.equals(estadoDireccionElectronica.IdEstado)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = estadoDireccionElectronica.Nombre;
						  }	
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-boveda" ://PORTAFOLIO CUENTA CUSTODIO FULL NATURAL - 	BOVEDA - OPTIMUS
						   Boveda boveda = (Boveda) hashBoveda.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( boveda != null && recorreFormulario.ValorDefectoCampoFormulario.equals(boveda.IdBoveda)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = boveda.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-cuenta-custodia" ://PORTAFOLIO CUENTA CUSTODIO FULL NATURAL - TIPO CUENTA - 	APP
						   TipoCuentaCustodia tipoCuentaCustodia= (TipoCuentaCustodia) hashTipoCuentaCustodia.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoCuentaCustodia != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoCuentaCustodia.IdTipoCuentaCustodia)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =tipoCuentaCustodia.NombreTipoCuentaCustodia;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-plaza" ://PERSONA CUENTA BANCARIA FULL NATURAL - PLAZA - 	OPTIMUS
						   Plaza plaza= (Plaza) hashPlaza.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( plaza != null && recorreFormulario.ValorDefectoCampoFormulario.equals(plaza.IdPlaza)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =plaza.NombrePlaza;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-sucursal" ://CLIENTE DATOS GENERALES FULL NATURAL - SUCURSAL DE PREFERENCIA - 	APP
						   Sucursal sucursal= (Sucursal) hashSucursal.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( sucursal != null && recorreFormulario.ValorDefectoCampoFormulario.equals(sucursal.IdSucursal)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =sucursal.NombreSucursal;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-cuenta-banco" :// SIN DATA
						   TipoCuentaBanco tipoCuentaBanco= (TipoCuentaBanco) hashTipoCuentaBanco.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoCuentaBanco != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoCuentaBanco.IdTipoCuentaBancaria)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =tipoCuentaBanco.NombreTipoCuentaBancaria;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-profesion" ://SIN DATA
						  Profesion profesion= (Profesion) hashProfesion.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( profesion != null && recorreFormulario.ValorDefectoCampoFormulario.equals(profesion.IdProfesion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =profesion.Nombre;
						  }	
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-estado-nombre-fantasia" ://PERSONA NOMBRE FANTASIA FULL JURIDICA - ESTADO - 	OPENWORKPLACE
						  EstadoNombreFantasia estadoNombreFantasia= (EstadoNombreFantasia) hashEstadoNombreFantasia.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( estadoNombreFantasia != null && recorreFormulario.ValorDefectoCampoFormulario.equals(estadoNombreFantasia.IdEstado)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =estadoNombreFantasia.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-operaciones-portafolio-cliente" ://CLIENTE PREFERENCIAS PAGO FULL NATURAL - OPERACION - APP
						  OperacionesPortafolioCliente operacionesPortafolioCliente= (OperacionesPortafolioCliente) hashOperacionesPortafolioCliente.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( operacionesPortafolioCliente != null && recorreFormulario.ValorDefectoCampoFormulario.equals(operacionesPortafolioCliente.IdPreferenciaPago)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =operacionesPortafolioCliente.Nombre;
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break; 
					   case "combobox-preferencia-pago-medio-pago" ://CLIENTE FULL PERSONA JURIDICA (CLIENTE PREFERENCIAS PAGO FULL JURIDICA) - IDMEDIOPAGO - 	OPTIMUS
						  PreferenciaPagoMedioPago preferenciaPagoMedioPago= (PreferenciaPagoMedioPago) hashPreferenciaPagoMedioPago.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( preferenciaPagoMedioPago != null && recorreFormulario.ValorDefectoCampoFormulario.equals(preferenciaPagoMedioPago.IdMedioDePago)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =preferenciaPagoMedioPago.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-preferencia-pago-moneda" ://CLIENTE FULL PERSONA NATURAL (CLIENTE PREFERENCIAS PAGO) - IDMONEDA - 	OPTIMUS
						  PreferenciaPagoMoneda preferenciaPagoMoneda= (PreferenciaPagoMoneda) hashPreferenciaPagoMoneda.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( preferenciaPagoMoneda != null && recorreFormulario.ValorDefectoCampoFormulario.equals(preferenciaPagoMoneda.IdMoneda)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =preferenciaPagoMoneda.NombreMoneda;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-operacion-origen-fondos" ://CLIENTE DECLARACION ORIGEN FONDOS FULL JURIDICO - TIPO OPERACION - OPTIMUS
						  TipoOperacionOrigenFondos tipoOperacionOrigenFondos= (TipoOperacionOrigenFondos) hashTipoOperacionOrigenFondos.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoOperacionOrigenFondos != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoOperacionOrigenFondos.IdTipoOperacionOrigenFondos)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =tipoOperacionOrigenFondos.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-atributo-anotacion-cliente" ://CLIENTE ANOTACIONES FULL NATURAL - RELEVANCIA - OPTIMUS
						  AtributoAnotacionCliente atributoAnotacionCliente= (AtributoAnotacionCliente) hashAtributoAnotacionCliente.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( atributoAnotacionCliente != null && recorreFormulario.ValorDefectoCampoFormulario.equals(atributoAnotacionCliente.IdAtributoAnotacion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =atributoAnotacionCliente.NombreAtributoAnotacion;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-atributo-anotacion" ://CLIENTE ANOTACIONES FULL NATURAL - RELEVANCIA - OPTIMUS
						  AtributoAnotacion atributoAnotacion= (AtributoAnotacion) hashAtributoAnotacion.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( atributoAnotacion != null && recorreFormulario.ValorDefectoCampoFormulario.equals(atributoAnotacion.IdAtributoAnotacion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =atributoAnotacion.NombreAtributoAnotacion;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-perfil-comercial" ://PORTAFOLIO PERFIL COMERCIAL MATRIZ OPERACIONAL FULL_JURIDICA - MODELO COMERCIAL ORIGEN - WEB SERVICE
						  PerfilComercial perfilComercial= (PerfilComercial) hashPerfilComercial.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( perfilComercial != null && recorreFormulario.ValorDefectoCampoFormulario.equals(perfilComercial.IdPerfilComercialBase)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =perfilComercial.DescripcionPerfilComercialBase;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-clasificacion-empresa-fatca" ://PERSONA FACTA CRS FULL JURIDICA - CLASIFICACION EMPRESA FATCA - OPTIMUS
						  ClasificacionEmpresaFatca clasificacionEmpresaFatca= (ClasificacionEmpresaFatca) hashClasificacionEmpresaFatca.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( clasificacionEmpresaFatca != null && recorreFormulario.ValorDefectoCampoFormulario.equals(clasificacionEmpresaFatca.IdClasificacionEmpresaFatca)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =clasificacionEmpresaFatca.Nombre;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-autorizado-portafolio" ://PORTAFOLIO AUTORIZACIONES FULL NATURAL - TIPO AUTORIZACION - OPTIMUS
						  TipoAutorizadoPortafolio tipoAutorizadoPortafolio= (TipoAutorizadoPortafolio) hashTipoAutorizadoPortafolio.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoAutorizadoPortafolio != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoAutorizadoPortafolio.IdTipoAutorizado)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =tipoAutorizadoPortafolio.NombreTipoAutorizado;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-grado-vinculacion-parental" ://PERSONA RELACIONES FULL NATURAL - Grado Vinculacion Parental - OPTIMUS
						  GradoVinculacionParental gradoVinculacionParental= (GradoVinculacionParental) hashGradoVinculacionParental.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( gradoVinculacionParental != null && recorreFormulario.ValorDefectoCampoFormulario.equals(gradoVinculacionParental.IdGradoVinculacionParental)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =gradoVinculacionParental.Nombre;
						  }	
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-proposito-portafolio" ://PORTAFOLIO DATOS GENERALES FULL NATURAL - PROPOSITO - WEB SERVICE
						  PropositoPortafolio propositoPortafolio= (PropositoPortafolio) hashPropositoPortafolio.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( propositoPortafolio != null && recorreFormulario.ValorDefectoCampoFormulario.equals(propositoPortafolio.IdPropositoPortafolio)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =propositoPortafolio.Descripcion;
						  }				
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-situacion-laboral" ://PERSONA DATOS GENERALES FULL NATURAL - SITUACION LABORAL
						  SituacionLaboral situacionLaboral= (SituacionLaboral) hashSituacionLaboral.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( situacionLaboral != null && recorreFormulario.ValorDefectoCampoFormulario.equals(situacionLaboral.CodigoSituacionLaboral)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =situacionLaboral.SituacionLaboral;							  
						  }	
						  salidaFormulario.add(recorreFormulario);
					      break;				      
					   case "combobox-division-pais" ://PERSONA DIRECCION FISICA FULL NATURAL - Division pais (ej. Comuna) - OPTIMUS
						  DivisionPais divisionPais= (DivisionPais) hashDivisionPais.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( divisionPais != null && recorreFormulario.ValorDefectoCampoFormulario.equals(divisionPais.IdDivisionPais)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =divisionPais.NombreDivisionPaisClasificacion1;							  
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-perfil-cobros" ://PORTAFOLIO PERFIL COMERCIAL PERFIL COBROS FULL NATURAL - PLANTILLA COBROS - OPTIMUS
						  PerfilCobros perfilCobros= (PerfilCobros) hashPerfilCobros.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( perfilCobros != null && recorreFormulario.ValorDefectoCampoFormulario.equals(perfilCobros.IdPerfilCobros)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =perfilCobros.PerfilCobros;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-plantilla-limites" ://PORTAFOLIO PERFIL COMERCIAL PLANTILLA LIMITES FULL JURIDICA - PLANTILLA LIMITES - OPTIMUS
						  PlantillaLimites plantillaLimites= (PlantillaLimites) hashPlantillaLimites.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( plantillaLimites != null && recorreFormulario.ValorDefectoCampoFormulario.equals(plantillaLimites.IdPlantillaLimite)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =plantillaLimites.DescripcionPlantillaLimite;						  
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-perfil-productos" ://PORTAFOLIO PERFIL COMERCIAL PERFIL PRODUCTOS FULL NATURAL - PLANTILLA DE PRODUCTOS - OPENWORKPLACE
						  PerfilProductos perfilProductos= (PerfilProductos) hashPerfilProductos.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( perfilProductos != null && recorreFormulario.ValorDefectoCampoFormulario.equals(perfilProductos.IdPerfilProductos)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =perfilProductos.DescripcionPerfilProductos;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-persona-direccion-fisica" ://PORTAFOLIO DATOS GENERALES FULL JURIDICO - 	DIRECCION ENVIO CORRESPONDENCIA - OPTIMUS
						  PersonaDireccionFisica personaDireccionFisica= (PersonaDireccionFisica) hashPersonaDireccionFisica.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( personaDireccionFisica != null && recorreFormulario.ValorDefectoCampoFormulario.equals(personaDireccionFisica.IdDireccionFisica)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =personaDireccionFisica.NombreLargo;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;								      
					   case "combobox-nombre-fantasia" ://PORTAFOLIO DATOS GENERALES FULL JURIDICO - NOMBRE FANTASIA - 	OPTIMUS
						   NombreFantasia nombreFantasia= (NombreFantasia) hashNombreFantasia.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( nombreFantasia != null && recorreFormulario.ValorDefectoCampoFormulario.equals(nombreFantasia.IdNombreFantasia)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =nombreFantasia.DescripcionNombreFantasia;							  							  
						  }			
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-persona-telefono" ://PORTAFOLIO DATOS GENERALES FULL JURIDICO - TELEFONO CONTACTO - 	WEB SERVICE
						  PersonaTelefono personaTelefono= (PersonaTelefono) hashPersonaTelefono.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( personaTelefono != null && recorreFormulario.ValorDefectoCampoFormulario.equals(personaTelefono.IdTelefono)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =personaTelefono.Numero;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-tipo-cargo-persona" ://PERSONA DATOS GENERALES FULL NATURAL - CARGO - OPTIMUS
						  TipoCargoPersona tipoCargoPersona= (TipoCargoPersona) hashTipoCargoPersona.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( tipoCargoPersona != null && recorreFormulario.ValorDefectoCampoFormulario.equals(tipoCargoPersona.CodigoTipoCargoPersona)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =tipoCargoPersona.NombreTipoCargoPersona;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-ciudad" ://PERSONA DIRECCION FISICA FULL NATURAL - CIUDAD - WEBSERVICE
						  Ciudad ciudad= (Ciudad) hashCiudad.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( ciudad != null &&  recorreFormulario.ValorDefectoCampoFormulario.equals(ciudad.IdCiudad)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =ciudad.NombreCiudad;							  
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-categoria-inversionista" ://INGRESAR CLIENTE FULL JURIDICA - CATEGORIA INVERSIONISTA - OPTIMUS
						  CategoriaInversionista categoriaInversionista= (CategoriaInversionista) hashCategoriaInversionista.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( categoriaInversionista != null && recorreFormulario.ValorDefectoCampoFormulario.equals(categoriaInversionista.IdCategoriaInversionista)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =categoriaInversionista.NombreCategoriaInversionista;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-socio-negocio" ://INGRESAR CLIENTE FULL JURIDICA - CATEGORIA INVERSIONISTA - OPTIMUS
						   SocioDeNegocio socioDeNegocio= (SocioDeNegocio) hashSocioDeNegocio.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( socioDeNegocio != null && recorreFormulario.ValorDefectoCampoFormulario.equals(socioDeNegocio.IdSocioDeNegocio)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =socioDeNegocio.NombreCompleto;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-representante-legal" ://INGRESAR CLIENTE FULL JURIDICA - CATEGORIA INVERSIONISTA - OPTIMUS
						   RepresentanteLegal representanteLegal= (RepresentanteLegal) hashRepresentanteLegal.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( representanteLegal != null && recorreFormulario.ValorDefectoCampoFormulario.equals(representanteLegal.IdPersonaRepresentante)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario =representanteLegal.NombreCompletoRepresentante;
						  }
						  salidaFormulario.add(recorreFormulario);
					      break;
					   case "combobox-relacionado" ://PORTAFOLIO AUTORIZACIONES FULL JURIDICA - 	AUTORIZADO A DAR ORDENES - 	OPTIMUS
						   Relacionado relacionado= (Relacionado) hashRelacionado.get(recorreFormulario.ValorDefectoCampoFormulario);
							  if( relacionado != null && recorreFormulario.ValorDefectoCampoFormulario.equals(relacionado.IdPersonaDestino)) {
								  recorreFormulario.NombreValorDefectoCampoFormulario =relacionado.NombreCompleto;
							  }
							  salidaFormulario.add(recorreFormulario);
						      break;					      
					   case "combobox-transparencia-fiscal" :
						   recorreFormulario.NombreValorDefectoCampoFormulario = recorreFormulario.ValorDefectoCampoFormulario;
						   salidaFormulario.add(recorreFormulario);
					      break;					      
					   case "combobox-detalle-tipo-atributo" :
						   DetalleTipoIndicador detalleTipoIndicador= (DetalleTipoIndicador) hashDetalleTipoIndicador.get(recorreFormulario.ValorDefectoCampoFormulario.concat(recorreFormulario.CodigoCampoFrontend));
							  if( detalleTipoIndicador!=null && recorreFormulario.ValorDefectoCampoFormulario.concat(recorreFormulario.CodigoCampoFrontend).equals(detalleTipoIndicador.Key)) {
								  recorreFormulario.NombreValorDefectoCampoFormulario =detalleTipoIndicador.DetalleIndicador;
							  }
							  salidaFormulario.add(recorreFormulario);
							  break;
					   case "combobox-proposito-transaccion" : 
						  PropositoTransaccion propositoTransaccion = (PropositoTransaccion) hashPropositoTransaccion.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( propositoTransaccion != null && recorreFormulario.ValorDefectoCampoFormulario.equals(propositoTransaccion.CodigoRangoSeleccion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = propositoTransaccion.Descripcion;
						  }
						  salidaFormulario.add(recorreFormulario);
						  break;
					   case "combobox-rango-renta" : 
						  RangoRenta rangoRenta = (RangoRenta) hashRangoRenta.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( rangoRenta != null && recorreFormulario.ValorDefectoCampoFormulario.equals(rangoRenta.CodigoRangoSeleccion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = rangoRenta.Descripcion;
						  }
						  salidaFormulario.add(recorreFormulario);
						  break;
					   case "combobox-rango-nivel-venta" : 
						  RangoNivelVenta rangoNivelVenta = (RangoNivelVenta) hashRangoNivelVenta.get(recorreFormulario.ValorDefectoCampoFormulario);
						  if( rangoNivelVenta != null && recorreFormulario.ValorDefectoCampoFormulario.equals(rangoNivelVenta.CodigoRangoSeleccion)) {
							  recorreFormulario.NombreValorDefectoCampoFormulario = rangoNivelVenta.Descripcion;
						  }
						  salidaFormulario.add(recorreFormulario);
						  break;
						
						   //ok
						  // recorreFormulario.NombreValorDefectoCampoFormulario = recorreFormulario.ValorDefectoCampoFormulario;
						  // salidaFormulario.add(recorreFormulario);
					      //break;
					}	
				}
				else
				{
					salidaFormulario.add(recorreFormulario);

				}
				
			}			
			consultarFormulario = salidaFormulario;			
		}
		
	
		logger.info("TERMINO CONSULTA CAMPO FORMULARIO");		
		
		salida.P_OUT_FORMULARIO = 	consultarFormulario;
		return salida;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();
			
			//Datos simulados, obtenidos desde ventana
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"PLA\",\"p_CodigoFormulario\":0,\"p_CodigoCampo\":DATOS_GENERALES_PERSONA_FULL_NATURAL_RANGO_RENTA,\"p_IdTipoCanal\":0}";
			ConsultarCampoFormulario_Entrada Objin = gson.fromJson(json, ConsultarCampoFormulario_Entrada.class);
			ConsultarCampoFormulario demo = new ConsultarCampoFormulario();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				ConsultarCampoFormulario_Salida ObjOut = demo.logica(con, Objin);
				System.out.println("SALIDA:" + ObjOut.P_OUT_FORMULARIO.size());
				System.out.println("SALIDA DATA:" + ObjOut.P_OUT_FORMULARIO);
				
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
		
}
