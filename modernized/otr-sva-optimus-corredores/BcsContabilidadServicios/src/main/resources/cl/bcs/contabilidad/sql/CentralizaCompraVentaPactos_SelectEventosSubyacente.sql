WITH consultaCarteraPacto AS (
SELECT
pac."FolioCartera" as "FolioPacto",
pac."FechaOperacion" as "FechaPacto",
pac."IdEstado",
pac."IdMonedaSistema",
pac."IdTipoOperacion",
pac."IdMonedaEmision" 
FROM "BSCL.Custodia.BD::CUS.Cartera" as pac
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (pac."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente") 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = pac."IdPortafolio")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = pac."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = pac."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (pac."IdConcepto" = Concepto."IdConcepto")
WHERE pac."IdTipoInstrumento" = 'PAC' and pac."IdEstado"IN ('VIG','VCD','ANT')
)
,v_BaseCalculos AS (
    SELECT 
         TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,Subyacente."IdSubyacente" AS "IdMovimiento"
        ,Subyacente."MontoSubyacenteMonedaInstrumento"
        ,Subyacente."IdMonedaInstrumento"
        ,Subyacente."INGMontoATasaSubyacenteActual"
        ,CarteraPacto."IdMonedaSistema"
        ,CarteraPacto."IdTipoOperacion"
        ,CarteraOrigen."TipoCarteraCorredor"
        --Monto Origen: Solo se calcula si se cumplen todas las condiciones
        ,CASE 
            WHEN :IncluirSpreadVentaPacto = 'S' 
                AND CarteraOrigen."EGRIdTipoOperacion" = 'VENPAC' 
                AND CarteraOrigen."TipoCarteraCorredor" <> 'CC' 
            THEN 
                ROUND(
                    CASE 
                        WHEN CarteraOrigen."IdClasificacionTipoInstrumento" = 'IF' 
                        THEN CarteraOrigen."EGRValorRescate" / CarteraOrigen."INGValorRescate"
                        ELSE CarteraOrigen."EGRCantidad" / CarteraOrigen."INGCantidad"
                    END 
                    * CarteraOrigen."INGMontoATasaCompraActual"
                )
            ELSE 0 
        END AS "MontoOrigen"
        ,CarteraOrigen."INGMontoATasaCompraActual"
        ,CarteraOrigen."EGRSpread"
        ,((CASE WHEN CarteraPacto."IdMonedaEmision" <> 'CLP' THEN reserva."INGMontoAPlazo" ELSE reserva."INGMontoAPlazoMonedaInstrumento" END ) - reserva."MontoValorizado") AS "Diferencia"
    FROM 
        consultaCarteraPacto CarteraPacto
    INNER JOIN 
        "BSCL.Custodia.BD::CUS.Subyacente" Subyacente 
        ON CarteraPacto."FolioPacto" = Subyacente."FolioCarteraRelacionada"
    LEFT JOIN 
        "BSCL.Custodia.BD::CUS.Cartera" CarteraOrigen 
        ON Subyacente."IdCarteraRetirada" = CarteraOrigen."IdCartera"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" reserva ON reserva."FolioSubyacente" = Subyacente."FolioSubyacente" 
    WHERE 
        CarteraPacto."IdEstado" IN ('VIG','VCD','ANT')
        AND CarteraPacto."FechaPacto" = :FechaContable
),
v_TabEventosPaso AS (
    SELECT 
         "IdTipoCentralizacion"
        ,"IdMovimiento"
        ,"IdMonedaSistema"
        ,"IdMonedaInstrumento"
        ,"MontoSubyacenteMonedaInstrumento"
        ,"IdTipoOperacion"
        ,"TipoCarteraCorredor"
        ,"MontoOrigen"
        ,"INGMontoATasaCompraActual"
        ,"EGRSpread" AS "Spread"
        , "Diferencia"
    FROM v_BaseCalculos
)
SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,'MONTO_PAPEL' AS "IdEvento"
	,'Monto Papel' AS "Nombre"
	,"MontoSubyacenteMonedaInstrumento" AS "MontoEvento"
	,"IdMonedaSistema" AS "CodigoMoneda"
FROM v_TabEventosPaso

UNION ALL

SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,'VALOR_PAPEL' AS "IdEvento"
	,'Valor Papel' AS "Nombre"
	,"MontoSubyacenteMonedaInstrumento"	AS "MontoEvento"
	,"IdMonedaInstrumento" AS "CodigoMoneda"
FROM v_TabEventosPaso

UNION ALL

SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,'MONTO_ORIGEN' AS "IdEvento"
	,'Monto Origen' AS "Nombre"
    , "MontoOrigen" AS "MontoEvento"
    ,"IdMonedaSistema" AS "CodigoMoneda"
FROM v_TabEventosPaso 
WHERE "IdTipoOperacion" = 'VENPAC'
AND "TipoCarteraCorredor" <> 'CC'
AND "Spread" <> 0

UNION ALL 

SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,'MONTO_A_TASA_COMPRA' AS "IdEvento"
	,'Monto a Tasa Compra' AS "Nombre"
    , "INGMontoATasaCompraActual" AS "MontoEvento"
    ,"IdMonedaSistema" AS "CodigoMoneda"
FROM v_TabEventosPaso 
WHERE "IdTipoOperacion" = 'VENPAC'
AND "TipoCarteraCorredor" <> 'CC'
AND "Spread" <> 0

UNION ALL

SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE WHEN "Spread" > 0 THEN 'SPREAD_POSITIVO_TASA_COMPRA_RFIF' ELSE 'SPREAD_NEGATIVO_TASA_COMPRA_RFIF' END AS "IdEvento"
	,CASE WHEN "Spread" > 0 THEN 'Spread Positivo a Tasa Compra RF IF' ELSE 'Spread Negativo a Tasa Compra RF IF' END AS "Nombre"
	,ABS("Spread") AS "MontoEvento"
	,"IdMonedaSistema" AS "CodigoMoneda"
FROM v_TabEventosPaso 
WHERE "IdTipoOperacion" = 'VENPAC'
AND "TipoCarteraCorredor" <> 'CC'
AND "Spread" <> 0
UNION ALL 
SELECT "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE WHEN "Diferencia" > 0 THEN 'UTILIDAD_SPREAD_DETALLE_PACTO' ELSE 'PERDIDA_SPREAD_DETALLE_PACTO' END AS "IdEvento"
	,CASE WHEN "Diferencia" > 0 THEN 'Utilidad Spread Detalle Pacto' ELSE 'Perdida Spread Detalle Pacto' END AS "Nombre"
	,ABS("Diferencia") AS "MontoEvento"
	,"IdMonedaSistema" AS "CodigoMoneda"
FROM v_TabEventosPaso 
WHERE  "Diferencia" <> 0;