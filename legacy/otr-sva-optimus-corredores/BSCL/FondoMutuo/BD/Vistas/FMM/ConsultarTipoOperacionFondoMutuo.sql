SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoOperacionFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoOperacionFondoMutuo"
AS
SELECT
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'TIPOOPERACION' AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoOperacionFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
