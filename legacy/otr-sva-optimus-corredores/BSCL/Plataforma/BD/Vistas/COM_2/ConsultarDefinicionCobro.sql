SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobro"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobro"  
AS

SELECT 
	"IdEmpresa",
	DEF."IdClaseCobro",
	DEF."IdTipoCobro",
	"CodigoDefinicionCobro",
	"CodigoDefinicionCobroBase",
	DEF."IdEstado",
	"Descripcion",
	"CheckCobroMonedaOperacion",
	"IdMonedaCobroOtraMoneda",
	DEF."IdTipoProducto",
	PRO."Nombre" as "NombreTipoProducto",
	"MontoFijo_MontoCobro",
	"MontoFijo_CheckMontoOperacionMenor",
	"MontoFijo_ValorMontoOperacionMenor",
	"MontoFijo_CheckSiNo",
	"MontoFijo_ValorMontoSiNo",
	"MontoFijoPorUnidad_MontoCobro",
	"MontoFijoPorUnidad_AllInCheckValor",
	"PorcentualFijo_ValorCobro",
	"PorcentualFijo_CheckSobreMonto",
	"PorcentualFijo_CheckSobreCantidad",
	"PorcentualFijo_CheckMayorEntreDos",
	"PorcentualFijo_CheckMenorEntreDos",
	"PorcentualFijo_CheckMontoMinimo",
	"PorcentualFijo_ValorMontoMinimo",
	"PorcentualFijo_CheckMontoMaximo",
	"PorcentualFijo_ValorMontoMaximo",
	"PorcentualFijo_IdMonedaMinimoMaximo",
	"PorcentualTasaPlazo_ValorCobro",
	"Otros_CheckOperacionContado",
	"Otros_CheckOperacionPlazo",
	"Otros_CheckBaseTasa",
	"Otros_ValorBaseTasa",
	"Otros_CheckInteresSimple",
	"Otros_CheckInteresCompuesto",
	"Otros_CheckAllIn",
	"Otros_ValorAllInMontoOperacionBase",
	"Otros_CheckAfectoImpuesto",
	"Otros_CheckAplicaPortafolio",
	"Otros_CheckAplicaCorresponsal",
	"Otros_CheckPermiteAumentarValor",
	"Otros_CheckPermiteDisminuirValor",
	"PorcentualRango_IdMonedaRangoMonto",
	"PorcentualRango_IdTipoRangoCobro",
	"Otros_IncluyeExcluyeEmisoresCheck",
	"Otros_IncluyeExcluyeInstrumentosCheck",
	"Otros_IncluyeExcluyeTiposDeClienteCheck",
	"Otros_IncluyeExcluyeClientesCheck",
	"Otros_IncluyeExcluyeCorresponsalesCheck",
	"Otros_IncluyeExcluyeEmisoresValor",
	"Otros_IncluyeExcluyeInstrumentosValor",
	"Otros_IncluyeExcluyeTiposDeClienteValor",
	"Otros_IncluyeExcluyeClientesValor",
	"Otros_IncluyeExcluyeCorresponsalesValor",
	
	CLASE."NombreClaseCobro",
	TIPO."NombreTipoCobro",
	
	
	CASE DEF."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN DEF."MontoFijo_MontoCobro"
		WHEN 'PORCENTAJEFIJO' THEN DEF."PorcentualFijo_ValorCobro"
		WHEN 'PORCENTAJERANGOS' THEN null
		WHEN 'PORCENTAJETASAPLAZO' THEN DEF."PorcentualTasaPlazo_ValorCobro"
		WHEN 'MONTOFIJOPORUNIDAD' THEN DEF."MontoFijoPorUnidad_MontoCobro"
		WHEN 'COMISIONINDIRECTA' THEN 0
		END AS "ValorCobroCombinado",
	
	CASE DEF."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN
			CASE
				WHEN DEF."CheckCobroMonedaOperacion" = 'S' THEN 'MONEDA OPERACION'
				WHEN DEF."CheckCobroMonedaOperacion" = 'N' THEN DEF."IdMonedaCobroOtraMoneda" END
		WHEN 'MONTOFIJOPORUNIDAD' THEN
			CASE
				WHEN DEF."CheckCobroMonedaOperacion" = 'S' THEN 'MONEDA OPERACION'
				WHEN DEF."CheckCobroMonedaOperacion" = 'N' THEN DEF."IdMonedaCobroOtraMoneda" END
				
		WHEN 'PORCENTAJEFIJO' THEN '%'
		WHEN 'PORCENTAJERANGOS' THEN 'RANGO'
		WHEN 'PORCENTAJETASAPLAZO' THEN '%'
		WHEN 'COMISIONINDIRECTA' THEN ''
		END AS "ValorCobroMonedaPorcentaje",
	
	
	CASE DEF."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN
			CASE
				WHEN DEF."CheckCobroMonedaOperacion" = 'S' THEN 'PEN'
				WHEN DEF."CheckCobroMonedaOperacion" = 'N' THEN DEF."IdMonedaCobroOtraMoneda" END
		WHEN 'MONTOFIJOPORUNIDAD' THEN
			CASE
				WHEN DEF."CheckCobroMonedaOperacion" = 'S' THEN 'PEN'
				WHEN DEF."CheckCobroMonedaOperacion" = 'N' THEN DEF."IdMonedaCobroOtraMoneda" END
		WHEN 'PORCENTAJEFIJO' THEN 'PEN'
		WHEN 'PORCENTAJERANGOS' THEN 'PEN'
		WHEN 'PORCENTAJETASAPLAZO' THEN 'PEN'
		WHEN 'COMISIONINDIRECTA' THEN 'PEN'
		END AS "IdMonedaCobroUI",
		
	CASE DEF."IdTipoCobro" 
		WHEN 'MONTOFIJO' THEN 'Monto'
		WHEN 'MONTOFIJOPORUNIDAD' THEN 'Monto'
		WHEN 'PORCENTAJEFIJO' THEN 'Tasa'
		WHEN 'PORCENTAJERANGOS' THEN 'Tasa'
		WHEN 'PORCENTAJETASAPLAZO' THEN 'Tasa'
		WHEN 'COMISIONINDIRECTA' THEN 'Monto'
		END AS "UsoMonedaFormato"
	
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DEF
LEFT JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" CLASE ON DEF."IdClaseCobro" = CLASE."IdClaseCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" TIPO ON DEF."IdTipoCobro" = TIPO."IdTipoCobro"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO ON DEF."IdTipoProducto" = PRO."IdTipoProducto"
WHERE DEF."CodigoDefinicionCobroBase" = '';

GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO