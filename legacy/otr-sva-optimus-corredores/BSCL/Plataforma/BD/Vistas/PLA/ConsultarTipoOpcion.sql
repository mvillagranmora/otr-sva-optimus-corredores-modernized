SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOpcion";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOpcion"  
AS
	SELECT 	CONF.*
	FROM 	"BSCL.Plataforma.BD::DER.Configuracion" CONF
	WHERE	"NombreTipoParametro" = 'TipoOpcion';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOpcion"   TO  BCS_DESARROLLADOR WITH GRANT OPTION;
