SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidad";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidad" as

	SELECT 	I."IdEntidad",
			TI."CodigoTipoIndicador",
			TI."IdEmpresa",
			I."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TI
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I
			ON TI."IdTipoIndicador" = I."IdTipoIndicador"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicadorEntidad" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO