SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMercado"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMercado" AS 
	SELECT	DISTINCT "CodigoClasificacion", "GlosaCodigoClasificacion"
	FROM	"BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"
	WHERE	"IdGrupoProducto" = 'OP'
	ORDER BY "GlosaCodigoClasificacion";
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMercado" TO BCS_DESARROLLADOR WITH GRANT OPTION;