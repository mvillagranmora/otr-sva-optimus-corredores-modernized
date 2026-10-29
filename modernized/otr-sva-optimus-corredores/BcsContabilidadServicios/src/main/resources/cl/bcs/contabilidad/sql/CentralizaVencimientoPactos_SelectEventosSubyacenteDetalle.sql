WITH v_TabEventosPaso AS (
	SELECT TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,ConsultaSubyacente."IdSubyacente" AS "IdMovimiento"
	,ConsultaSubyacente."MontoSubyacente"
	,ConsultaSubyacente."MontoSubyacenteMonedaInstrumento"
	,ConsultarCartera."IdMonedaSistema"
	,ConsultaSubyacente."IdMonedaInstrumento"
	,Reserva."INGMontoAPlazoMonedaInstrumento"
	,COALESCE(CarteraValorizada."MontoProximo", 0) AS "ValorPresente"
	,COALESCE(CarteraValorizadaPac."MontoProximo", 0) AS "ValorPresentePacto"
	,ConsultarCartera."IdTipoOperacion"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
	ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" Reserva 
	ON Reserva."IdOperacion" = Operacion."IdOperacion" AND ConsultaSubyacente."Nemotecnico" = Reserva."Nemotecnico"
	AND (ConsultaSubyacente."IdCarteraRetirada" = 0 OR ConsultaSubyacente."IdCarteraRetirada" = Reserva."IdCarteraOrigen")
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados" ConsultarInformes ON ConsultarInformes."Key" = 'CARTERA.PACTOCOMP' || :p_FechaAnterior || :p_FechaContable
	LEFT JOIN "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CarteraValorizada ON CarteraValorizada."IdInforme" = ConsultarInformes."IdInforme" AND CarteraValorizada."IdCartera" =Reserva."IdCarteraOrigen" AND CarteraValorizada."FolioOperacion" = Operacion."FolioOperacion"
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados" ConsultarInformesPac ON ConsultarInformesPac."Key" = 'CARTERA.PACTOPAC' || :p_FechaAnterior || :p_FechaContable
	LEFT JOIN "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CarteraValorizadaPac ON CarteraValorizadaPac."IdInforme" = ConsultarInformesPac."IdInforme" AND CarteraValorizadaPac."IdCartera"  = Reserva."IdCarteraOrigen" AND CarteraValorizadaPac."FolioOperacion" = Operacion."FolioOperacion"
	WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable              
    AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
    AND Operacion."IdEstado" = 'LIQ'
)
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_PAPEL' 			AS "IdEvento" ,'Monto Papel' 			AS "Nombre" ,"MontoSubyacente" 							AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_PAPEL' 			AS "IdEvento" ,'Valor Papel' 			AS "Nombre" ,"MontoSubyacenteMonedaInstrumento" 		AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_FINAL_PAPEL'		AS "IdEvento" ,'Monto Final Papel'		AS "Nombre" ,"INGMontoAPlazoMonedaInstrumento"			AS "MontoEvento","IdMonedaSistema"		AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_FINAL_PAPEL'		AS "IdEvento" ,'Valor Final Papel'		AS "Nombre" ,"INGMontoAPlazoMonedaInstrumento"			AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_PRESENTE' 		AS "IdEvento" ,'Valor Presente' 		AS "Nombre" ,"ValorPresente" 							AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'MONTO_PRESENTE_TP_PACTO_VENTA' ELSE 'MONTO_PRESENTE_TP_PACTO_COMPRA' END 	AS "IdEvento"   ,'Valor Presente A Tasa Pacto' 		AS "Nombre" ,"ValorPresentePacto" 		AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso; 
