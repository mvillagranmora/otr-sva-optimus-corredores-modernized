SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMoneda";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMoneda" AS
SELECT
	INSMON."IdInstrumentoMoneda"
	,INSMON."IdInstrumento"
	,INSMON."IdTipoProducto"
	,INSMON."IdMonedaPrincipal"
	,INSMON."IdMonedaSecundaria"
	,INSMON."IdEstado"
	,INSMON."IdEmpresa"
	,INS."IdTipoInstrumento"
	,INS."RazonSocial"				AS "Instrumento"
	,INS."Nemotecnico"
	,TIPINS."IdClasificacionTipoInstrumento"
	,MONPRI."MontoDecimales"		AS "MonedaPrincipalDecimales"
	,MONPRI."TipoCambioDecimales"	AS "TCPrincipalDecimales"
	,MONPRI."CantidadDecimales" 	AS "MonedaPrincipalCantidadDecimales"
	,MONSEC."MontoDecimales"		AS "MonedaSecundariaDecimales"
	,MONSEC."CantidadDecimales" 	AS "MonedaSecundariaCantidadDecimales"
	,MONSEC."TipoCambioDecimales"	AS "TCSecundariaDecimales"
	,MONSEC."ParidadDecimales"		AS "ParidadDecimalesSecundaria"
	,MON."Nombre"					AS "NombreMonedaPrincipal"
	,MON."Orden"					AS "Orden"
	,TPMM."Nombre"					AS "NombreProducto"
	,TIPMON."MultiplicaODivide"
	,(CASE (INSMON."FeriadoMonedaPrincipal")
	 	WHEN 'S' THEN 'SI'
	 	WHEN 'N' THEN 'NO'
	 	ELSE ''
	END) AS "FeriadoMonedaPrincipal" 
	,TPMM."Codigo" AS "IdTipoProductoMesaModulo" 
FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" AS INSMON
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
	ON (INS."Nemotecnico" = INSMON."Nemotecnico" AND INS."IdTipoInstrumento" = INSMON."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPINS
	ON (INS."IdTipoInstrumento" = TIPINS."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS MONPRI 
    ON INSMON."IdMonedaPrincipal" = MONPRI."IdMoneda"
        AND ((INS."IdTipoInstrumento" IN ('SPT') AND MONPRI."IdTipoInstrumento" = INS."IdTipoInstrumento")
            OR (MONPRI."IdUsoMonedaFormato" = INS."IdTipoInstrumento"))
        AND (MONPRI."IdTipoOperacion" = INSMON."Nemotecnico" OR MONPRI."IdUsoMonedaFormato" = 'GEN')
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS MONSEC 
    ON INSMON."IdMonedaSecundaria" = MONSEC."IdMoneda"
        AND ((INS."IdTipoInstrumento" IN ('SPT') AND MONSEC."IdTipoInstrumento" = INS."IdTipoInstrumento")
            OR (MONSEC."IdUsoMonedaFormato" = INS."IdTipoInstrumento"))
        AND (MONSEC."IdTipoOperacion" = INSMON."Nemotecnico" OR MONSEC."IdUsoMonedaFormato" = 'GEN')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (INSMON."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" TPMM
	ON (INS."IdTipoInstrumento" = TPMM."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda");
	
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMoneda" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
