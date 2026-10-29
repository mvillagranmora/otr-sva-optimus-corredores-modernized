SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionEntrada";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionEntrada"
AS
SELECT 
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'COMISION_ENTRADA'
AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionEntrada" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
