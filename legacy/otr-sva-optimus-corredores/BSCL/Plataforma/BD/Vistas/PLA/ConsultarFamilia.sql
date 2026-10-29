SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarFamilia"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarFamilia" AS 
	SELECT	INS.*, GRP."CodigoClasificacion"
	FROM	"BSCL.Plataforma.BD::INS.TipoInstrumento" INS
			INNER JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" GRP
			--ON GRP."IdTipoInstrumento" = INS."Codigo" AND GRP."IdGrupoProducto" = 'OP'
			ON GRP."IdTipoInstrumento" = INS."IdTipoInstrumento" AND GRP."IdGrupoProducto" = 'OP'
	WHERE	INS."IdClasificacionTipoInstrumento" = 'DV';

	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarFamilia" TO BCS_DESARROLLADOR WITH GRANT OPTION;