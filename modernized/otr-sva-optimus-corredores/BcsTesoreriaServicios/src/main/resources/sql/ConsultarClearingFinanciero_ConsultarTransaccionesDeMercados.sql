SELECT     
     TRA."IdTransaccion"
    ,TRA."IdClasificacionTipoInstrumento"   -- RV, RF, IF, DV, FM
    ,TRA."FechaSistemaCreacion"           -- fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora)
    ,TRA."IdTipoInstrumento"                -- ACN /ACX /CFI / CFM / SIM   
    ,TRA."FolioTransaccion"                
    ,TRA."IndicadorOrdenDirecta"             -- S/N
    ,TRA."IdBolsa"
    ,TRA."MontoTransaccion"              
    ,TRA."IdTipoOperacion"                -- COMACN  / VENACX
    ,"TipoOperacion"."AccionOperacion" AS "AccionTipoOperacion"            -- COMPRA / VENTA
    ,TRA."IdCondicionDeLiquidacion"       -- T0/T1/T2
    ,"CondicionDeLiquidacion"."Codigo" AS "CodigoCondicionDeLiquidacion"     -- PH/PM/CN
    , CASE
	   WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN TRA."IdInstrumentoSubyacente"
	   WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN TRA."IdInstrumentoSubyacente"
	   ELSE TRA."IdInstrumento"
      END AS "IdInstrumento"
  	,TRA."InstrumentoUI" AS "Instrumento"
    ,TRA."IdCorredorContraparte"
    ,CNT."DocumentoIdentidad" AS "RutContraparte"
    ,CNT."NombreCorto" AS "RazonSocialContraparte"
    ,CNT."CodigoInscripcionRegulador" AS "CodigoCorredorContraparte"           
    ,TRA."CodigoOperadorContraparte"
    ,TRA."CodigoOperadorCorredor" AS "CodigoOperadorCorredora"        
    ,TRA."FechaLiquidacion"
    ,TRA."FechaVencimiento"  -- Para Simultaneas
    ,TRA."EsAPlazo"
    ,TRA."EsBilateral"     -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
    ,TRA."IdCamara"        -- Se carga desde TBCLOPER con WS de actualizacion de transacciones
    ,TRA."IdComprobanteTesoreria"       -- Si existe este valor, ya se proceso la liquidacion
    ,TRA."CantidadTransaccion"
    ,TRA."PrecioTransaccion"
    ,TRA."Tasa"
    ,TRA."IdMonedaTransaccion"
    ,TRA."MontoRetencion" -- PERU: Monto retencion cavali(informado por archivo IGK)
FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA -- aca no importa que no esten facturados
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ( "TipoOperacion"."IdTipoOperacion" = TRA."IdTipoOperacion" )
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ( "CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion" )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ( "Instrumento"."IdInstrumento" = TRA."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON ( "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento" )
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS "CNT" ON ("CNT"."IdCorredora" = TRA."IdCorredorContraparte")
WHERE TRA."EsBilateral" = 'N'      -- No sea bilateral
AND (TRA."FechaLiquidacion" = :p_FechaLiquidacion)  -- Vencimiento para reportes(simultáneas)           
AND TRA."IdEstadoTransaccion"  <> 'ANU'    -- No considerar anulados
AND TRA."BloqueoLiquidacion"    = 'N'      -- Que no este bloqueada para liquidacion
AND TRA."IndicadorOrdenDirecta" = ''       -- Que no sea OD, OE(OD con operador externo)
AND TRA."IdMonedaTransaccion"   = 'CLP'    -- Solo se compensa CLP                 
AND TRA."IdEmpresa" = :p_IdEmpresa
AND TRA."IdCamara" <> ''
AND (    -- Esta logica debe ser la misma en:  CrearTransaccion.java / ModificarTransaccion.js / ConsultarClearingFinanciero.SQL
	(TRA."IdClasificacionTipoInstrumento"='RV' AND TRA."IdCondicionDeLiquidacion"='T2')                                                -- Toda operacion RV, CN (Incluye simultaneas)) --> Camara RV
    OR ((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T1')  -- Toda operacion RF o IF  T1 --> Camara PM
    OR ((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T0')  -- Toda operacion RF o IF  T0 --> Camara PH
    OR ((TRA."IdClasificacionTipoInstrumento"='RF' OR TRA."IdClasificacionTipoInstrumento"='IF') AND TRA."IdCondicionDeLiquidacion"='T2')  -- NUEVO: Toda operacion RF o IF  T2
    OR (TRA."IdClasificacionTipoInstrumento"='RV' AND TRA."IdTipoInstrumento" = 'SIM' AND TRA."IdCondicionDeLiquidacion"='T0')            -- Toda operacion de SIMULTANEA, T0 --> Camara SM
    OR (TRA."IdClasificacionTipoInstrumento" = 'DV')
);
