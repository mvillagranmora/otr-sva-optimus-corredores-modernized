SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"; 
GO 
CREATE VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"   AS 
SELECT 	
	EntidadRecaudadora."IdEntidadRecaudadora"
   ,EntidadRecaudadora."IdPersona"
   ,EntidadRecaudadora."Codigo"
   ,DocumentoIdentidad."DocumentoIdentidad" || ' ' || EntidadRecaudadora."Nombre" AS "DocumentoIdentidadNombre"
   ,EntidadRecaudadora."Nombre" AS "Nombre"
   ,EntidadRecaudadora."Estado"
   ,EntidadRecaudadora."IdTipoEntidadRecaudadora"
   ,DocumentoIdentidad."DocumentoIdentidad"
   ,TipoEntidadRecaudadora."Nombre" as "NombreTipoEntidadRecaudadora"
   ,EntidadRecaudadora."Estado" AS "IdEstado"
FROM
	"BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
  INNER JOIN "BSCL.APV.BD::SCS.TipoEntidadRecaudadora" AS TipoEntidadRecaudadora
     ON TipoEntidadRecaudadora."IdTipoEntidadRecaudadora" = EntidadRecaudadora."IdTipoEntidadRecaudadora"
   INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
     ON DocumentoIdentidad."IdPersona" = EntidadRecaudadora."IdPersona"
WHERE EntidadRecaudadora."Estado" = 'ACT';
GO
GRANT SELECT ON "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 