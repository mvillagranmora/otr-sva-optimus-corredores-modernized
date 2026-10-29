SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRegistro";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRegistro"
AS
SELECT 
 "IdConcepto"
, "IdEmpresa"
, "GrupoConcepto"
, "CodigoConcepto"
, "NombreConcepto"
, "Estado"
FROM "BSCL.FondoMutuo.BD::FMM.Concepto" AS "Concepto"
WHERE "GrupoConcepto" = 'TIPOREGISTRO'
AND "Estado" = 'ACT';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRegistro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
