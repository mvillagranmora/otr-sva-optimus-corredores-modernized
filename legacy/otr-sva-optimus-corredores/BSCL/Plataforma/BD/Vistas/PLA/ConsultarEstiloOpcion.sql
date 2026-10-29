SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstiloOpcion";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstiloOpcion"  
AS
	SELECT 	CONF.*
	FROM 	"BSCL.Plataforma.BD::DER.Configuracion" CONF
	WHERE	"NombreTipoParametro" = 'EstiloOpcion';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstiloOpcion"   TO  BCS_DESARROLLADOR WITH GRANT OPTION;
