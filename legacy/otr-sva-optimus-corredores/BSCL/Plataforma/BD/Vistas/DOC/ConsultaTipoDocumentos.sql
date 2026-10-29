SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaTipoDocumentos";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaTipoDocumentos" AS
SELECT
	"TDO"."IdTipoDocumento",
	"TDO"."IdEmpresa",
	"TDO"."NombreDocumento",
	"TDO"."NumeroFirmasMinimas",
	"TDO"."NumeroFirmasMaximas",
	"TDO"."Glosa",
	"TDO"."ValidaFechaVigencia",
	"TDO"."CodigoTipoIndicador",
	"TDO"."IdTipoPersona",
	"TDO"."SolicitarFechaFirma",
	"CDO"."IdClase",
	"CDO"."Descripcion",
	"TDO"."TipoDocumentoNormativa",
	"TDO"."FechaFirma",
    "TDO"."CodigoArchivo",
	"TDO"."ValidaClienteFull",
	"TDO"."RequiereFirmaCliente",
	"TDO"."BloquearAlVencer",
	"TDO"."FirmaAutomaticaDocumento"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" "TDO"
JOIN "BSCL.Plataforma.BD::DOC.ClaseDocumento" "CDO" ON ("TDO"."IdClase" = "CDO"."IdClase");
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultaTipoDocumentos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO