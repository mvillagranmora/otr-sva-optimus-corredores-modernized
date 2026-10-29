SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaDepositos";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaDepositos" AS

SELECT
	CAR."IdCargaMasivaDepositos",
	CAR."FechaProceso",
	CAR."IdSecuencia",
	CAR."RutClienteComprador",
	CAR."SecuenciaRutComprador",
	CAR."CustodiaComprador",
	CAR."RutClienteVendedor",
	CLV."IdCliente" AS "IdClienteVendedor",
	CAR."SecuenciaRutVendedor",
	CAR."CustodiaVendedor",
	CAR."Emisor",
	CAR."FechaInicioOperacion",
	CAR."FechaTerminoPapel",
	CAR."TipoReajuste",
	CAR."MontoFinal",
	CAR."MontoInicial",
	CAR."NumeroCompra",
	CAR."NumeroVenta",
	CAR."TasaCompra",
	CAR."CodigoAgente",
	CAR."IdEstado",
	EST."Descripcion" AS "EstadoDeposito",
	CAR."Borrado",
	CAR."Glosa",
	CAR."IdEmpresa",
	CONF."Valor" AS "TipoDocumentoIdentidad",
	CASE WHEN CAR."TipoReajuste"='NR' THEN 'CLP' ELSE 'USD' END AS "IdMoneda"
FROM "BSCL.Mesa.BD::MES.CargaMasivaDepositos" AS CAR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLV ON (CAR."RutClienteVendedor"=CLV."DocumentoIdentidad")
INNER JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa"	AS CONF ON (CONF."CodigoParametro" = 'TIPODOCTOIDENTCARGAARCHIVO')
LEFT JOIN "BSCL.Mesa.BD::MES.EstadoDeposito"		AS EST	ON (EST."IdEstado" = CAR."IdEstado")
ORDER BY CAR."FechaProceso" DESC, CAR."IdSecuencia" DESC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaDepositos" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
