SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosEjercicio";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosEjercicio"  
AS
	SELECT 	CONF.*
	FROM 	"BSCL.Plataforma.BD::DER.Configuracion" CONF
	WHERE	"NombreTipoParametro" = 'ParametroEjercicio';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosEjercicio"   TO  BCS_DESARROLLADOR WITH GRANT OPTION;
