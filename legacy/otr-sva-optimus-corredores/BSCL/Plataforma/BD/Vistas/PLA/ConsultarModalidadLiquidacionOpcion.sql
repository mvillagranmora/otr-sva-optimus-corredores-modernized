SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModalidadLiquidacionOpcion"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModalidadLiquidacionOpcion"  
AS
	SELECT 	CONF.*
	FROM 	"BSCL.Plataforma.BD::DER.Configuracion" CONF
	WHERE	"NombreTipoParametro" = 'ModalidadOpcion';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModalidadLiquidacionOpcion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
