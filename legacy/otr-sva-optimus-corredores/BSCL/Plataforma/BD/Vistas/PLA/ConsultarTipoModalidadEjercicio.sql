SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoModalidadEjercicio";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoModalidadEjercicio"  
AS
	SELECT 	CONF.*
	FROM 	"BSCL.Plataforma.BD::DER.Configuracion" CONF
	WHERE	"NombreTipoParametro" = 'ModalidadEjercicio';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoModalidadEjercicio"   TO  BCS_DESARROLLADOR WITH GRANT OPTION;
