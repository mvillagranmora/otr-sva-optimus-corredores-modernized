SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionSalida";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionSalida"
AS
SELECT 
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'COMISION_SALIDA'
AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionSalida" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
