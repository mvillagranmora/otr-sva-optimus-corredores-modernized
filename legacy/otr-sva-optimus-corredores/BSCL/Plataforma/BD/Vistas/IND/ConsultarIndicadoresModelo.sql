SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo"  AS

	SELECT 
		TIND."IdTipoCliente",
		TIND."IdTipoIndicador",
		TIND."IdTipoEntidad",
		TIND."IdEmpresa",
		TIND."ClaseDato",
		TIND."CantidadDecimales",
		TIND."LongitudMinima",
		TIND."LongitudMaxima",
		TIND."NombreTipoIndicador",
		TIND."GrupoIndicador",
		TIND."IdUso",
		TIND."IdEstado",
		TIND."CodigoTipoIndicador",
		TIND."IdTipoPersona",
		IFNULL(IND."IdEntidad",0) as "IdEntidad",
		IFNULL(IND."IdIndicador",0) as "IdIndicador",
		IFNULL(IND."ModeloCliente",'DEFAULT') as "ModeloCliente",
		IFNULL(IND."ValorIndicador",TIND."ValorIndicadorDefecto") as "ValorIndicador",
		IFNULL(DETIND."Codigo",'') as "CodigoValorIndicador", -- nuevo
		IFNULL(DETIND."DetalleIndicador",'') as "NombreValorIndicador" -- nuevo
	 FROM 
		(SELECT 
			TCLI."IdTipoCliente",
			TIND.*
		FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente" as TCLI,
		     "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND
		     WHERE TIND."IdEstado" != 'INA' AND TCLI."IdEmpresa" = TIND."IdEmpresa") as TIND
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
			ON IND."IdTipoIndicador" = TIND."IdTipoIndicador"
			AND IND."ModeloCliente" = TIND."IdTipoCliente"
		LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETIND
			ON  TIND."ClaseDato" = 'COMBO'
			AND	CAST(DETIND."IdDetalleIndicador" as NVARCHAR) = IND."ValorIndicador"	
			AND DETIND."IdTipoIndicador" = IND."IdTipoIndicador"
	;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

