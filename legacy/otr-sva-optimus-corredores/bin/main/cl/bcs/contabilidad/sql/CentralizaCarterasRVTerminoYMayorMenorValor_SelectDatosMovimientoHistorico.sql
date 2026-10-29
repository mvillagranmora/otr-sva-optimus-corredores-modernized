SELECT
     ConsultarCartera."IdCartera"                               AS "IdMovimiento"        
    ,LEFT(ConsultarCliente."DocumentoIdentidad",60)             AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)                 AS "NombreCliente"                                       
    ,LEFT(Port."CodigoPortafolio",60)               			AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                     AS "Moneda"                                                                                                         
    ,ConsultarCartera."IdTipoProducto"                          AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"          AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"                       AS "TipoInstrumento"
    ,ConsultarCartera."Nemotecnico"                             AS "Nemo"
    ,ConsultarCartera."IdBoveda"                                AS "Boveda" 
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))         AS "IdCartera"      
    ,ConsultarCartera."IdEmisor"                                AS "Emisor"
    ,ConsultarCartera."IdTipoReajuste"                          AS "TipoReajuste"
    ,ConsultarCartera."CodigoIFRS"                              AS "CodigoIFRS"
    ,CASE WHEN "IdTipoInstrumento" = 'SIM' THEN 'Simultanea'
    	  ELSE 'Libre'
     END                                                        AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
     ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
                                                                AS "ClaseEmisor"
    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'')           AS "Agente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'')  AS "NombreAgente"
    ,COALESCE(ConsultarEjecutivo."CodigoSucursal",'')           AS "SucursalAgente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'')  AS "NombreSucursalAgente"
    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')        AS "CentroCostoAgente"
    ,ConsultarCartera."INGPrecioMercadoAnterior"                AS "PrecioMercadoAnterior"
    ,ConsultarCartera."INGPrecioMercadoActual"                  AS "PrecioMercadoActual"
    ,ConsultarCartera."ValorTipoCambioCierre"                   AS "TipoCambio"
    ,ConsultarCartera."INGFechaValorizacionAnterior"            AS "FechaAnterior"
    ,ConsultarCartera."INGFechaValorizacionActual"              AS "FechaActual"
    ,CASE WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
                        THEN 'CP'
            WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'     
                        THEN 'RE'
            ELSE    'NR'
          END                                                   AS "Relacion"
    ,ConsultarCartera."IdBolsa"                                 AS "Bolsa"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
                                ELSE ConsultarCartera."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
                                ELSE ConsultarCartera."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarCartera."IdConcepto"                              AS "IdConcepto"
    ,DocInd."DocumentoIdentidad"                                AS "DocumentoIdentidadCAVALI"
FROM "BSCL.Custodia.BD::HIS.Cartera"                        	AS ConsultarCartera
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"               AS ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Port ON Port."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"                AS SoCNeg
ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                      AS DocInd 
ON DocInd."IdPersona"                   = SoCNeg."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'             
        
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                 AS ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa
    AND ConsultarCartera."FechaOperacion" <= :p_FechaContable
    AND ConsultarCartera."FechaRespaldo" = :p_FechaContable
    AND ConsultarCartera."EGRCantidad" = 0
    AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RV'
    AND (ConsultarCartera."IdTipoInstrumento" NOT IN ('PRT','EMP','PAC', 'FWD','RRV','RRF') OR (ConsultarCartera."IdTipoInstrumento" = 'SIM' AND ConsultarCartera."IdTipoOperacion" = 'VENSIM'))
    AND ConsultarCliente."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
    AND ConsultarCartera."IdEstado" = 'VIG'
    
UNION ALL

SELECT
     ConsultarGarantias."IdGarantia"                            AS "IdMovimiento"        
    ,LEFT(ConsultarGarantias."DocumentoIdentidad",60)           AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)                 AS "NombreCliente"                                       
    ,LEFT(ConsultarGarantias."CodigoPortafolio",60)             AS "Portafolio"
    ,ConsultarGarantias."IdMonedaInstrumento"                   AS "Moneda"                                                      
    ,ConsultarGarantias."IdTipoProductoOperacionOrigen"         AS "Producto"
    ,ConsultarGarantias."IdClasificacionTipoInstrumento"        AS "ClaseInstrumento"
    ,ConsultarGarantias."IdTipoInstrumento"                     AS "TipoInstrumento"
    ,ConsultarGarantias."Nemotecnico"                           AS "Nemo"
    ,ConsultarGarantias."IdBovedaOperacionOrigen"               AS "Boveda" 
    ,CAST(ConsultarGarantias."IdGarantia" AS NVARCHAR(60))      AS "IdCartera"      
    ,ConsultarGarantias."IdEmisor"                              AS "Emisor"
    ,ConsultarGarantias."IdTipoReajuste"                        AS "TipoReajuste"
    ,ConsultarGarantias."CodigoIFRS"                            AS "CodigoIFRS"
    ,'Garantia'                                                 AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarGarantias."IdEmisor", ConsultarGarantias."IdClasificacionTipoInstrumento", 
     ConsultarGarantias."IdTipoInstrumento", ConsultarGarantias."Nemotecnico", ConsultarGarantias."IdTipoReajuste")
                                                                AS "ClaseEmisor"
    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'')           AS "Agente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'')  AS "NombreAgente"
    ,COALESCE(ConsultarEjecutivo."CodigoSucursal", '')          AS "SucursalAgente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'')  AS "NombreSucursalAgente"
    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')        AS "CentroCostoAgente"
    ,COALESCE(ConsultarGarantias."INGPrecioMercadoAnterior",0)  AS "PrecioMercadoAnterior"
    ,ConsultarGarantias."INGPrecioMercadoActual"                AS "PrecioMercadoActual"
    ,COALESCE(ConsultarGarantias."ValorTipoCambioCierre",0)     AS "TipoCambio"
    ,ConsultarGarantias."INGFechaValorizacionAnterior"          AS "FechaAnterior"
    ,ConsultarGarantias."INGFechaValorizacionActual"            AS "FechaActual"
    ,CASE WHEN ConsultarGarantias."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
                        THEN 'CP'
            WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'     
                        THEN 'RE'
            ELSE    'NR'
          END                                                   AS "Relacion"
    ,ConsultarGarantias."IdBolsaOperacionOrigen"                AS "Bolsa"
    ,CASE WHEN ConsultarGarantias."EGRCantidad" = 0 THEN ConsultarGarantias."INGFechaLiquidacion"
                                ELSE ConsultarGarantias."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarGarantias."EGRCantidad" = 0 THEN ConsultarGarantias."INGFechaOperacion"
                                ELSE ConsultarGarantias."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarGarantias."IdConcepto"                            AS "IdConcepto"
    ,DocInd."DocumentoIdentidad"                                AS "DocumentoIdentidadCAVALI"

FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias"                              AS ConsultarGarantias
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"                       AS ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarGarantias."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"                AS SoCNeg
ON (ConsultarGarantias."IdCliente" = SoCNeg."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                      AS DocInd 
ON DocInd."IdPersona"                   = SoCNeg."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'             
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                         AS ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarGarantias."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                         AS ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarGarantias."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"                     AS ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarGarantias."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"              AS ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarGarantias."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
WHERE ConsultarGarantias."IdEmpresa" = :p_IdEmpresa
AND ConsultarGarantias."FechaOperacion" <= :p_FechaContable
AND ConsultarGarantias."FechaRespaldo" = :p_FechaContable
AND ConsultarGarantias."IdClasificacionTipoInstrumento" = 'RV'
AND ConsultarGarantias."EGRCantidad" = 0
AND ConsultarGarantias."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
AND ConsultarGarantias."NemoGarantizado" != 'SIMULTANEA VENTA'
AND ConsultarGarantias."IdEstado" = 'VIG';