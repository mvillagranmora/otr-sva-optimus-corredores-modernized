SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites"  
AS

SELECT
	PL."IdPlantillaLimite",
	PL."DescripcionPlantillaLimite",
	PL."FechaCreacion",
	PL."FechaModificacion",
	--PL."IdEstado",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITE',PL."IdEstado",PL."IdEstadoAutorizacion") AS "IdEstado",
	PL."PorDefecto",
	PL."IdEmpresa",
	PL."EsPerfilPersonalizado"
FROM "BSCL.Plataforma.BD::LIM.PlantillaLimite" AS PL;
--WHERE "IdEstado" = 'ACT';
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO