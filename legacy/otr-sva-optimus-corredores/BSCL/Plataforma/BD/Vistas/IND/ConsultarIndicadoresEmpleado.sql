SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"  AS

	SELECT 
		EMP."IdEmpleado",
		TIND."IdTipoEntidad",
		TIND."NombreTipoIndicador",
		TIND."GrupoIndicador",
		TIND."IdUso",
		TIND."IdTipoIndicador", -- nuevo
		TIND."CodigoTipoIndicador",
		TIND."ClaseDato", -- nuevo
		TIND."CantidadDecimales",
		TIND."LongitudMinima",
		TIND."LongitudMaxima",
		TIND."IdTipoPersona",
		IFNULL(IND."ValorIndicador",TIND."ValorIndicadorDefecto") as "ValorIndicador",
		IFNULL(DETIND."Codigo",'') as "CodigoValorIndicador", -- nuevo
		IFNULL(DETIND."DetalleIndicador",'') as "NombreValorIndicador", -- nuevo
		IFNULL(IND."IdIndicador",0) as "IdIndicador",
		TIND."IdEmpresa"
	FROM "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP
		INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND
			ON 	TIND."IdTipoEntidad" = 'EMPLEADO' AND TIND."IdEstado" != 'INA'--cartesiano
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
			ON IND."IdTipoEntidad" = TIND."IdTipoEntidad"
				AND IND."IdEntidad" = EMP."IdEmpleado"
				AND TIND."IdTipoIndicador" = IND."IdTipoIndicador"
		LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETIND
			ON 	TIND."ClaseDato" = 'COMBO'
			AND CAST(DETIND."IdDetalleIndicador" as NVARCHAR) = IND."ValorIndicador"	
			AND DETIND."IdTipoIndicador" = IND."IdTipoIndicador"
	;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
