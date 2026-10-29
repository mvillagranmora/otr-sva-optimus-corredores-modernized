SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" AS

SELECT 
		CLI."IdCliente",
		CLI."IdPersona",
		CAST(CLI."IdTipoCliente" AS NVARCHAR(40)) AS "IdTipoCliente",
		POR."IdPortafolio",
		CAST(TIND."IdTipoEntidad" AS NVARCHAR(40)) AS "IdTipoEntidad",
		CAST(TIND."NombreTipoIndicador" AS NVARCHAR(255)) AS "NombreTipoIndicador",
		CAST(TIND."GrupoIndicador" AS NVARCHAR(40)) AS "GrupoIndicador",
		CAST(TIND."IdUso" AS NVARCHAR(3)) AS "IdUso",
		TIND."IdTipoIndicador", -- nuevo
		CAST(TIND."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
		CAST(TIND."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato", -- nuevo
		TIND."CantidadDecimales",
		TIND."LongitudMinima",
		TIND."LongitudMaxima",
		CAST(TIND."IdTipoPersona" AS NVARCHAR(3)) AS "IdTipoPersona",
		CAST(CLI."IdTipoCliente" AS NVARCHAR(40)) AS "ModeloCliente",
		CAST(COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") AS NVARCHAR(255)) as "ValorIndicador",
		CAST(COALESCE(DETINDPROPIO."Codigo",DETINDMODELO."Codigo",'') AS NVARCHAR(40)) as "CodigoValorIndicador", -- nuevo
		CAST(COALESCE(DETINDPROPIO."DetalleIndicador",DETINDMODELO."Codigo",'') AS NVARCHAR(255)) as "NombreValorIndicador", -- nuevo
		IFNULL(INDPROPIO."IdIndicador",0) AS "IdIndicador",
		TO_CHAR(COALESCE(INDPROPIO."FechaCreacion",'0001-01-01'),'YYYY-MM-DD') AS "FechaCreacion",
		TO_CHAR(COALESCE(INDPROPIO."FechaModificacion",'0001-01-01'),'YYYY-MM-DD') AS "FechaModificacion"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
--LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND ON TIND."IdTipoEntidad" = 'PORTAFOLIO'
	--AND (TIND."IdTipoPersona" = 'NJ' OR PER."Tipo" = TIND."IdTipoPersona") 
	AND TIND."IdEstado" != 'INA'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDMODELO ON INDMODELO."IdTipoIndicador" = TIND."IdTipoIndicador" AND 
	INDMODELO."ModeloCliente" = CLI."IdTipoCliente" AND INDMODELO."IdEntidad" = 0
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDPROPIO ON INDPROPIO."IdEntidad" = POR."IdPortafolio" AND INDPROPIO."IdTipoIndicador" = TIND."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETINDMODELO ON  TIND."ClaseDato" = 'COMBO' 
	AND	CAST(DETINDMODELO."IdDetalleIndicador" as NVARCHAR) = INDMODELO."ValorIndicador" AND DETINDMODELO."IdTipoIndicador" = INDMODELO."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETINDPROPIO ON  TIND."ClaseDato" = 'COMBO' 
	AND	CAST(DETINDPROPIO."IdDetalleIndicador" as NVARCHAR) = INDPROPIO."ValorIndicador" AND DETINDPROPIO."IdTipoIndicador" = INDPROPIO."IdTipoIndicador"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
