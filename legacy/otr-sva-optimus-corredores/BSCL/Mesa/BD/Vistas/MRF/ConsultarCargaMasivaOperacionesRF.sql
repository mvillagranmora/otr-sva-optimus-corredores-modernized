SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaOperacionesRF";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaOperacionesRF" 
AS
	SELECT
			CAR."FechaProceso",
			CAR."IdRegistroCarga",
			CAR."FolioOperacion",
			CAR."RutCliente",
			CAR."SecuenciaRut",
			CAR."IdEstadoCarga",
			CAR."IdTipoReajusteCabecera",
			CAR."IdTipoCustodia",
			CAR."IdFormaPago",
			CAR."IdIFRS",
			CAR."IdCondicionLiquidacion",
			CAR."IdIngreso",
			CAR."IdClasificacionTipoInstrumento",
			CAR."Nemo",
			CAR."Emisor",
			CAR."IdTipoReajusteDetalle",
			COALESCE(CAR."FechaVencimiento", '') AS "FechaVencimiento",
			CAR."Cantidad",
			CAR."MontoNominal",
			CAR."MontoValorizado",
			TO_VARCHAR(CAR."FechaCarga") AS "FechaCarga",
			CAR."ArchivoCarga",
			EST."Descripcion" AS "EstadoRegistro",
			CAR."Glosa",
			CAR."IdEmpresa"
	FROM 	"BSCL.Mesa.BD::MES.CargaMasivaOperaciones" AS CAR
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLV ON (CAR."RutCliente"=CLV."DocumentoIdentidad")
			INNER JOIN 	"BSCL.Mesa.BD::MES.ConfiguracionMesa"	AS CONF ON (CONF."CodigoParametro" = 'TIPODOCTOIDENTCARGAARCHIVO')
			LEFT JOIN 	"BSCL.Mesa.BD::MES.EstadoDeposito"		AS EST	ON (EST."IdEstado" = CAR."IdEstadoCarga")
	ORDER BY CAR."FechaProceso" DESC, CAR."IdRegistroCarga" DESC;

GRANT SELECT 	ON 	"BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaOperacionesRF" 
				TO	BCS_DESARROLLADOR WITH GRANT OPTION; 
