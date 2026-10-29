SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica"  AS

	SELECT 
		PER."IdPersona",
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
		CAST(TIND."IdTipoPersona" AS NVARCHAR(10)) AS "IdTipoPersona",
		CAST(IFNULL(IND."ValorIndicador",TIND."ValorIndicadorDefecto") AS NVARCHAR(255)) as "ValorIndicador",
		CAST(IFNULL(DETIND."Codigo",'') AS NVARCHAR(40)) as "CodigoValorIndicador", -- nuevo
		CAST(IFNULL(DETIND."DetalleIndicador",'') AS NVARCHAR(255)) as "NombreValorIndicador", -- nuevo
		IFNULL(IND."IdIndicador",0) as "IdIndicador"
	FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
	LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON 	TIND."IdTipoEntidad" = 'PERSONA' AND TIND."IdEstado" != 'INA'--cartesiano
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdEntidad" = PER."IdPersona" AND TIND."IdTipoIndicador" = IND."IdTipoIndicador"
	LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETIND ON 	TIND."ClaseDato" = 'COMBO'
			AND CAST(DETIND."IdDetalleIndicador" as NVARCHAR) = IND."ValorIndicador"	
			AND DETIND."IdTipoIndicador" = IND."IdTipoIndicador"
	--WHERE  PER."IdPersona"= 213888	
	;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
