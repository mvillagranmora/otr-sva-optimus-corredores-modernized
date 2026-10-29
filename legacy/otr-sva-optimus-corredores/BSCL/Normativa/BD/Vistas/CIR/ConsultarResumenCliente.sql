
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" AS

SELECT
   RES."IdDocumento",
   RES."IdCliente",
   RES."IdPortafolio",
   RES."CodigoPortafolio",
   RES."IdPersona",
   RES."ValorPresente",
   RES."FechaIngreso",
   RES."FechaCreacion",
   RES."FechaModificacion",
   RES."IdUsuarioCreador",
   DOC."AnoMes" AS "Periodo",
   --Datos Descripcion
   COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuarioCreador"	
FROM "BSCL.Normativa.BD::CIR.ResumenCliente"  AS RES
	INNER JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOC
	ON (RES."IdDocumento"=DOC."IdDocumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU
	ON (RES."IdUsuarioCreador"=USU."IdUsuario");
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


