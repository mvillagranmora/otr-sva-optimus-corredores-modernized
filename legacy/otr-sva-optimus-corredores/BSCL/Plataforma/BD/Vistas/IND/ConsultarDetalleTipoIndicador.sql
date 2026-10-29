SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" as
	SELECT 
		D."IdDetalleIndicador", 
		D."IdTipoIndicador",
		D."DetalleIndicador", 
		D."Codigo", 
		D."IdCarga",
		TI."CodigoTipoIndicador", 
		TI."IdEmpresa" 
	FROM "BSCL.Plataforma.BD::IND.DetalleIndicador" D
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI
	ON TI."IdTipoIndicador" = D."IdTipoIndicador"
	LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobro" OC
	ON OC."OpcionCobro" = D."IdDetalleIndicador" 
	AND TI."CodigoTipoIndicador" IN ('COBROCUST') AND OC."IdEstado" <> 'INA'
	WHERE TI."CodigoTipoIndicador" <> 'COBROCUST' OR OC."OpcionCobro" IS NOT NULL;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
