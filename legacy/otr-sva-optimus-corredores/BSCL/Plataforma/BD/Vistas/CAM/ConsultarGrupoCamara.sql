SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CAM.ConsultarGrupoCamara"; 
GO
CREATE VIEW 
-- Proposito : Consulta la forma en que agrupan las camaras en las entidades de compensacion(como por ejemplo el CCLV)
-- Fecha     : 09/11/2016
-- Nombre    : JVILLAGRAN - BCS 
"BSCL.Plataforma.BD.Vistas::CAM.ConsultarGrupoCamara" AS

SELECT 
    GCA."IdGrupoCamara"
   ,GCA."SecuenciaAgrupacion"
   ,GCA."NombreGrupoCamara"
   ,GCA."CodigoGrupo"
   ,GCA."Camaras"      -- Camaras que incluye el grupo
   ,GCA."IdEstado" As "IdEstadoGrupoCamara"
   ,GCA."VisibleConCamaraHS"
   ,CAM."IdCamaraDeCompensacion"       -- Ej.: CCLV
   ,CAM."Codigo" AS "CodigoCamaraDeCompensacion"
   ,CAM."Nombre" AS "NombreCamaraDeCompensacion"
   ,CAM."IdPersona" AS "IdPersonaCamaraDeCompensacion"
   ,CAM."IdPais" AS "IdPaisCamaraDeCompensacion"
   ,CAM."IdEstado" AS "IdEstadoCamaraDeCompensacion"   
FROM "BSCL.Plataforma.BD::CAM.GrupoCamaraDeCompensacion" AS GCA
INNER JOIN "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion" AS CAM ON (CAM."IdCamaraDeCompensacion" = GCA."IdCamaraDeCompensacion");
GO 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CAM.ConsultarGrupoCamara" TO  BCS_DESARROLLADOR WITH GRANT OPTION;