SET SCHEMA "BSCL";

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarDocumentoInstruccion";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarDocumentoInstruccion" AS
SELECT DISTINCT 
	"DOC"."IdDocumentoInstruccion",
    "DOC"."IdInstruccion",
    "DOC"."NombreDocumentoAdjunto",
    "DOC"."Descripcion",
    "DOC"."FechaActualizacion",
    TO_NVARCHAR("DOC"."FechaActualizacion", 'YYYY-MM-DD') AS "FechaActualizacionString",
    "DOC"."Borrado"
FROM "BSCL.Orden.BD::ORD.DocumentoInstruccion" AS "DOC";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarDocumentoInstruccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
