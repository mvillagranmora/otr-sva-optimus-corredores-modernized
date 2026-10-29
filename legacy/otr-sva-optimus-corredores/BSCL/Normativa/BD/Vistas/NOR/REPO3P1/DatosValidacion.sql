SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.DatosValidacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.DatosValidacion" (

	"ID",
	"IdDocumento",
	"FuenteDatos",
    "Estado",
    "EstadoSICAM",
    "SICAM",
    "SICAMDescripcion",
	--camos layout    
    "TipoRegistro",
	"TipoRegistroDescripcion",
    "RUTReportante",
    "FechaOperacion",
    "HoraOperacion",
    "TipoOperacion",
	"TipoOperacionDescripcion",
	"EventoReporte",
	"EventoReporteDescripcion",
	"Identificador",
	"RUTContraparte",
	"LEIContraparte",
	"NombreContraparte",
	"PlataformaNegociacion",
	"PlataformaNegociacionDescripcion",
	"MontoMonedaComprada",
	"MontoMonedaVendida",
	"TipoCambio",
	--campos de control interno especifico
	"IdCliente",
	"IdOperacion",
	"IdTipoOperacion",
	"Nemo",
	"IdMonedaPrincipal",
	"IdMonedaSecundaria"
) AS
	SELECT 
		REP."ID",
		REP."IdDocumento",
		REP."FuenteDatos",
		REP."Estado",
		REP."EstadoSICAM",
		REP."SICAM",
		REP."SICAMDescripcion",
		--camos layout    
		REP."TipoRegistro",
		REP."TipoRegistroDescripcion",
		REP."RUTReportante",
		REP."FechaOperacion",
		REP."HoraOperacion",
		REP."TipoOperacion",
		REP."TipoOperacionDescripcion",
		REP."EventoReporte",
		REP."EventoReporteDescripcion",
		REP."Identificador",
		REP."RUTContraparte",
		REP."LEIContraparte",
		REP."NombreContraparte",
		REP."PlataformaNegociacion",
		REP."PlataformaNegociacionDescripcion",
		REP."MontoMonedaComprada",
		REP."MontoMonedaVendida",
		REP."TipoCambio",
		--campos de control interno especifico
		REP."IdCliente",
		REP."IdOperacion",
		REP."IdTipoOperacion",
		REP."Nemo",
		REP."IdMonedaPrincipal",
		REP."IdMonedaSecundaria"
	FROM "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos" REP
	INNER JOIN "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte" AS FEC ON (TO_DATE(REP."FechaOperacion") =TO_DATE(FEC."FechaReporte")) --Para validar solo registros del dia definido en la vista de la fecha del reporte

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.DatosValidacion" TO bcs_desarrollador WITH GRANT OPTION;