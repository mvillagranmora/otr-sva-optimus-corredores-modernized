SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAnticiposEmprestito";


CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAnticiposEmprestito"
AS 
SELECT distinct
	sby."IdSubyacente",
	emp."IdEmpresa",
	emp."IdCartera",
	emp."FolioEmprestito",
	emp."IdCliente",
	emp."IdPortafolio",
	emp."CodigoPortafolio",
	emp."DocumentoIdentidad",
	emp."RazonSocial",
	sby."IdEstado",
--	sby."NombreEstado",
	emp."IdBolsa",
	emp."IdSucursal",
	emp."IdAgente",
	emp."IdTipoComprobante",
	emp."FolioComprobante",
	emp."FechaComprobante",
	Presta."IdInstrumentoPrestamo",
	Presta."NemotecnicoPrestamo" 	as "NombreTipoPrestamo",
	emp."IdInstrumentoEmprestito",
	sby."IdInstrumento" 		as "IdInstrumentoSubyacente",
	sby."Nemotecnico" 			as "NemoSubyacente",
	sby."IdTipoInstrumento" 	as "IdTipoInstrumentoSubyacente",
--	sby."NombreTipoInstrumento" as "NombreTipoInstrumentoSubyacente",
	sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoSubyacente",
	sby."EGRFechaOperacion", 
	car."EGRCantidad", 
	car."EGRPrecio",
	car."EGRMontoAPlazo" - car."EGRMonto" as "EGRPremio", 
	car."EGRMonto",
	car."EGRMontoAPlazo",
	car."EGRTasa", 
	car."EGRValorRescate", 
	car."EGRIdTipoComprobante",
	car."EGRFolioComprobante",
	car."EGRFechaComprobante",
	Presta."IdCliente" as "IdClientePrestamo",
	Presta."IdPortafolio" as "IdPortafolioPrestamo",
	Presta."FolioPrestamo",
	"BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(car."EGRFechaOperacion", Presta."FechaVencimientoPrestamo", tp."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = car."IdEmpresa")) AS "DiasRestantes",
	CASE WHEN (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'CUS.UsaFechaLiquidacionEnVencimiento' AND conf."IdEmpresa" = car."IdEmpresa") = 'S'
		THEN "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(CASE WHEN emp."INGFechaOperacion" > Presta."FechaLiquidacion" THEN emp."INGFechaOperacion" ELSE Presta."FechaLiquidacion" END, car."EGRFechaOperacion", tp."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = car."IdEmpresa"))
		ELSE "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(CASE WHEN emp."INGFechaOperacion" > Presta."FechaPrestamo" THEN emp."INGFechaOperacion" ELSE Presta."FechaPrestamo" END, car."EGRFechaOperacion", tp."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = car."IdEmpresa"))
	END AS "PlazoEnDiasOperacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" as car--
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico" as emp ON ( car."IdCartera" = emp."IdCartera")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = car."IdTipoOperacion")
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" ultSub on (ultSub."FolioCarteraRelacionada" = emp."FolioEmprestito")
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" as Presta ON (Presta."FolioPrestamo" = emp."FolioPrestamo")
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" tp ON tp."IdTipoPrestamo" = car."IdTipoPrestamo"
WHERE 
	car."IdTipoInstrumento" IN ('EMP','ERF') 
	AND car."EGRIdTipoOperacion" = 'ANTEMP'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarAnticiposEmprestito" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

