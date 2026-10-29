WITH v_TabEventosPaso AS (
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,ConsultaSubyacente."IdSubyacente" AS "IdMovimiento"
	,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
    		(CASE WHEN ConsultaSubyacente."IdClasificacionTipoInstrumento" = 'IF'
					THEN  COALESCE(ConsultaSubyacente."ValorRescate",0)
					ELSE  COALESCE(ConsultaSubyacente."Cantidad",0) 
					END ) * DET."PorcentajeDeterioro"/100
    		,MF."MontoDecimales"
    		,MF."MontoTrucaRedondea"),0) AS "Monto"
	,CASE WHEN ConsultaSubyacente."IdMonedaInstrumento" <> ''
		THEN ConsultaSubyacente."IdMonedaInstrumento"
		ELSE ConsultarCartera."IdMonedaSistema"
		END AS "IdMonedaInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"			ConsultarCartera
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
		ON (ConsultarCartera."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
 		 ON MF."IdMoneda" 			=  CASE WHEN ConsultaSubyacente."IdMonedaInstrumento" <> '' THEN ConsultaSubyacente."IdMonedaInstrumento" ELSE ConsultarCartera."IdMonedaSistema" END
 		AND MF."IdUsoMonedaFormato" = 'GEN'
 		AND MF."IdTipoInstrumento" 	= ''
 		AND MF."IdTipoOperacion" 	= ''
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = ConsultaSubyacente."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE 
		ConsultarCartera."IdEstado" 	= 'VIG' 
		AND ConsultarCartera."FechaPacto" 	= :FechaContable
		AND ConsultarCartera."IdTipoOperacion" IN ('COMPAC', 'VENPAC' )
		AND DET."FlagDeterioro" = 'S'
		AND DET."PorcentajeDeterioro" > 0
)
SELECT "IdMovimiento" AS "IdMovimiento"
,"IdTipoCentralizacion"
,'MONTO_DETERIORO' 	AS "IdEvento"
,'Monto Papel' 	AS "Nombre"
,"Monto" AS "MontoEvento"
,"IdMonedaInstrumento" AS "CodigoMoneda"
FROM v_TabEventosPaso;