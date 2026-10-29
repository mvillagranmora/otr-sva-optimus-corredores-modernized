SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulos" 
AS

SELECT MODULO."IdEmpresa",
	   MODULO."CodigoModulo",
	   MODULOPLA."Nombre"
FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;