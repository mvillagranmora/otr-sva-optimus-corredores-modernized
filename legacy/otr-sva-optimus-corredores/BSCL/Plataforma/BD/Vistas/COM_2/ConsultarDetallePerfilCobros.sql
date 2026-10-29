SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros"; 
GO		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros"  
AS

SELECT 
	DETALLE."IdPerfilCobros",
	PERFIL."DescripcionPerfilCobros",
	PERFIL."IdEmpresa",
	PERFIL."EsPerfilPersonalizado",
	DETALLE."IdDetallePerfilCobros",
	DETALLE."IdBolsa",
	CASE WHEN DETALLE."IdBolsa" IS NULL THEN 'TODOS' ELSE BOL."Nombre" END as "NombreBolsa",
	DETALLE."IdTipoProducto",
	PRODUCTO."Nombre" as "NombreTipoProducto",
	DETALLE."IdTipoOperacion",
	OPERACION."Nombre" as "NombreTipoOperacion",
	DETALLE."IdTipoCanalIngreso",
	CASE WHEN DETALLE."IdTipoCanalIngreso" IS NULL THEN 'TODOS' ELSE CANAL."Nombre" END as "NombreTipoCanalIngreso",
	
	DEFCOBRO."IdClaseCobro",
	CLASECOBRO."NombreClaseCobro",
	DEFCOBRO."IdTipoCobro",
	TIPOCOBRO."NombreTipoCobro",
	DEFCOBRO."CodigoDefinicionCobro",
	DEFCOBRO."CodigoDefinicionCobroBase",
	DEFCOBRO."Descripcion" AS "NombreDefinicionCobro",
	DEFCOBRO."MontoFijo_MontoCobro",
	DEFCOBRO."PorcentualFijo_ValorCobro",
	DEFCOBRO."IdEstado",
	ESTDEF."Nombre" AS "NombreEstado",
	CASE DEFCOBRO."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN DEFCOBRO."MontoFijo_MontoCobro"
		WHEN 'MONTOFIJOPORUNIDAD' THEN DEFCOBRO."MontoFijoPorUnidad_MontoCobro"
		WHEN 'PORCENTAJEFIJO' THEN DEFCOBRO."PorcentualFijo_ValorCobro"
		WHEN 'PORCENTAJERANGOS' THEN null
		WHEN 'PORCENTAJETASAPLAZO' THEN DEFCOBRO."PorcentualTasaPlazo_ValorCobro"
		WHEN 'COMISIONINDIRECTA' THEN 0
		END AS "ValorCobroCombinado",
	
	CASE DEFCOBRO."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN
			CASE
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'S' THEN 'MONEDA OPERACION'
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'N' THEN DEFCOBRO."IdMonedaCobroOtraMoneda" END
		WHEN 'MONTOFIJOPORUNIDAD' THEN
			CASE
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'S' THEN 'MONEDA OPERACION'
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'N' THEN DEFCOBRO."IdMonedaCobroOtraMoneda" END
		WHEN 'PORCENTAJEFIJO' THEN '%'
		WHEN 'PORCENTAJERANGOS' THEN 'RANGO'
		WHEN 'PORCENTAJETASAPLAZO' THEN '%'
		WHEN 'COMISIONINDIRECTA' THEN ''
		END AS "ValorCobroMonedaPorcentaje",
	
	
	CASE DEFCOBRO."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN
			CASE
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'S' THEN 'PEN'
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'N' THEN DEFCOBRO."IdMonedaCobroOtraMoneda" END
		WHEN 'MONTOFIJOPORUNIDAD' THEN
			CASE
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'S' THEN 'PEN'
				WHEN DEFCOBRO."CheckCobroMonedaOperacion" = 'N' THEN DEFCOBRO."IdMonedaCobroOtraMoneda" END
		WHEN 'PORCENTAJEFIJO' THEN 'PEN'
		WHEN 'PORCENTAJERANGOS' THEN 'PEN'
		WHEN 'PORCENTAJETASAPLAZO' THEN 'PEN'
		WHEN 'COMISIONINDIRECTA' THEN 'PEN'
		END AS "IdMonedaCobroUI",
		
	CASE DEFCOBRO."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN 'Monto'
		WHEN 'MONTOFIJOPORUNIDAD' THEN 'Monto'
		WHEN 'PORCENTAJEFIJO' THEN 'Tasa'
		WHEN 'PORCENTAJERANGOS' THEN 'Tasa'
		WHEN 'PORCENTAJETASAPLAZO' THEN 'Tasa'
		WHEN 'COMISIONINDIRECTA' THEN 'Monto'
		END AS "UsoMonedaFormato"
	
	FROM "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" as DETALLE
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DEFCOBRO ON DEFCOBRO."CodigoDefinicionCobro" = DETALLE."CodigoDefinicionCobro"
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" CLASECOBRO ON CLASECOBRO."IdClaseCobro" = DEFCOBRO."IdClaseCobro"
	INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" TIPOCOBRO ON TIPOCOBRO."IdTipoCobro" = DEFCOBRO."IdTipoCobro"
	LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" as PERFIL 	on (PERFIL."IdPerfilCobros" = DETALLE."IdPerfilCobros")
	LEFT JOIN  "BSCL.Plataforma.BD::INS.TipoOperacion"  as OPERACION on (OPERACION."IdTipoOperacion" = DETALLE."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" as CANAL on (CANAL."IdTipoCanalIngreso" = DETALLE."IdTipoCanalIngreso")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"  as PRODUCTO on (PRODUCTO."IdTipoProducto" = DETALLE."IdTipoProducto")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa"  as BOL on (BOL."IdBolsa" = DETALLE."IdBolsa")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTDEF ON (ESTDEF."IdEstado" = DEFCOBRO."IdEstado")
	WHERE "Borrado" = 'N'
	;
--ORDER BY DPC."Prioridad"
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO