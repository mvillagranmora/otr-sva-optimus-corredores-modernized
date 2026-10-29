SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Normativa.BD.Vistas::PRO.UIConsultarEstadisticaOperacionCliente"; 
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::PRO.UIConsultarEstadisticaOperacionCliente" AS

SELECT
   PRO."IdEstadistica",
   PRO."IdEmpresa",
   PRO."IdCliente",
   CLI."DocumentoIdentidad",
   CLI."NombreCompleto" 			AS "NombreCliente",
   PRO."MontoTotalDias",
   PRO."CantidadDias",
   PRO."MontoPromedioDiario",
   PRO."MontoMaximo",
   PRO."IdMoneda",
   COALESCE(Moneda."Nombre",'')		AS "Moneda",
   PRO."IdTipoInstrumento",
   COALESCE(INS."Nombre",'')		AS "TipoInstrumento",
   PRO."IdClasificacionTipoInstrumento",
   COALESCE(CLAS."Nombre",'')		AS "ClasificacionTipoInstrumento",
   PRO."FechaIngreso",
   PRO."FechaCreacion",
   PRO."FechaModificacion"
FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" AS PRO
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
		ON (CLI."IdCliente"=PRO."IdCliente") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda
		ON (Moneda."IdMoneda" = PRO."IdMoneda")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS INS 
	   	ON INS."IdTipoInstrumento" = PRO."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLAS 
     	ON CLAS."IdClasificacionTipoInstrumento" = PRO."IdClasificacionTipoInstrumento"		
ORDER BY PRO."IdEstadistica";
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::PRO.UIConsultarEstadisticaOperacionCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
