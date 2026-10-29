SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarProposito";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarProposito"
AS
SELECT 
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'PROPOSITO'
AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarProposito" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
