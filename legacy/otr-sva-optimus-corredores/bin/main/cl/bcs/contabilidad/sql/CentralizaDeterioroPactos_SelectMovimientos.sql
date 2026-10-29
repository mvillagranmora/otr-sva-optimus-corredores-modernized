  SELECT
  	TO_BIGINT(:IdTipoCentralizacion)							AS "IdTipoCentralizacion",
	ConsultaSubyacente."IdSubyacente"							AS "IdMovimiento",
	'DETPACTO'													AS "IdTipoMovimiento",
	CCP."IdEmpresa"												AS "IdEmpresa",			
	CCP."FolioPacto" 											AS "Numero", 
	TO_BIGINT(0)												AS "Linea",
	YEAR(CCP."FechaPacto") ||'-'|| 
	RIGHT('0' || MONTH(CCP."FechaPacto"),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(CCP."FechaPacto"),2)				AS "Fecha",
	CCP."DocumentoIdentidad" 									AS "DocumentoIdentidad",
	CCP."IdPortafolio"											AS "IdPortafolio",
	TO_BIGINT(YEAR(CCP."FechaPacto"))							AS "Ejercicio",
	RIGHT('0' || MONTH(CCP."FechaPacto"),2) ||'-'|| 
				 YEAR (CCP."FechaPacto") 						AS "Periodo",
	COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
    		(CASE WHEN ConsultaSubyacente."IdClasificacionTipoInstrumento" = 'IF'
					THEN  COALESCE(ConsultaSubyacente."ValorRescate",0)
					ELSE  COALESCE(ConsultaSubyacente."Cantidad",0) 
					END ) * DET."PorcentajeDeterioro"/100
    		,MF."MontoDecimales"
    		,MF."MontoTrucaRedondea"),0) AS "Monto",
	CASE WHEN ConsultaSubyacente."IdMonedaInstrumento" <> ''
		THEN ConsultaSubyacente."IdMonedaInstrumento"
		ELSE CCP."IdMonedaSistema"
		END AS "IdMoneda",
	CCP."IdEstado"												AS "Estado"     	
	FROM "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"	AS CCP
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
		ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			=  CASE WHEN ConsultaSubyacente."IdMonedaInstrumento" <> '' THEN ConsultaSubyacente."IdMonedaInstrumento" ELSE CCP."IdMonedaSistema" END
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= ''
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = ConsultaSubyacente."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CCP."IdEstado" = 'VIG' 
		AND CCP."FechaPacto" = :FechaContable
		AND CCP."IdTipoOperacion" IN ('COMPAC', 'VENPAC' )
		AND DET."FlagDeterioro" = 'S'
		AND DET."PorcentajeDeterioro" > 0;