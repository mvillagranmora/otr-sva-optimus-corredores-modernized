SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidadJson";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidadJson" as

	SELECT
		I."IdEntidad",
		TI."CodigoTipoIndicador",
		TI."IdEmpresa",
		STRING_AGG(DI."Codigo" || '-' || DI."DetalleIndicador",'||') AS "CodigoDetalleIndicador",
		I."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TI
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DI ON DI."IdTipoIndicador" = TI."IdTipoIndicador"
	GROUP BY I."IdEntidad",TI."CodigoTipoIndicador",TI."IdEmpresa",I."ValorIndicador"
    
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidadJson" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO