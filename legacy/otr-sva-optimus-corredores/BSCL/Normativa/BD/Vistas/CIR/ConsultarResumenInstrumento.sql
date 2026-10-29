
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento" AS

SELECT
   RES."IdDocumento",
   RES."IdCliente",
   RES."IdPortafolio",
   RES."IdInstrumento",
   RES."IdEmisor",
   RES."Nemotecnico",
   RES."IdClasificacionTipoInstrumento",
   RES."EsCtaPropia",
   RES."TotalValorPresente",
   RES."FechaIngreso",
   RES."FechaCreacion",
   RES."FechaModificacion",
   RES."IdUsuarioCreador",
   DOC."AnoMes" AS "Periodo",
   --Datos Descripcion
   COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuarioCreador"	
FROM "BSCL.Normativa.BD::CIR.ResumenInstrumento"  AS RES
	INNER JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOC
	ON (RES."IdDocumento"=DOC."IdDocumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU
	ON (RES."IdUsuarioCreador"=USU."IdUsuario");
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


