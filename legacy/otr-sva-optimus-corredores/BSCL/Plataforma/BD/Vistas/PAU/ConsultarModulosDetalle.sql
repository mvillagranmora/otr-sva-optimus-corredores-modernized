SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" 
AS

SELECT MODULO."IdEmpresa",
	   MODULO."CodigoModulo",
	   MODULOPLA."Nombre" AS "NombreModulo",
	   ESTADO."Nombre" AS "NombreEstado",
	   MODULO."CodigoUsuario",
	   MODULO."FechaUltimaActualizacion"	,
	   MODULO."IdEstado"
FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON (ESTADO."IdEstado" = MODULO."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO