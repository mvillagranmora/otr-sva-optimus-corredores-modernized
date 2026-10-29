SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRescate";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRescate"
AS
SELECT 
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'TIPORESCATE'
AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
