SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConcepto";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConcepto"
AS
SELECT
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConcepto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
