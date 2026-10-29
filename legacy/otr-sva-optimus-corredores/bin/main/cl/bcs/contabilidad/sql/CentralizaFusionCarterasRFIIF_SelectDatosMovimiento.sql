SELECT
     ConsultarFusionCartera."IdCartera"                       AS "IdMovimiento"        
    ,LEFT(ConsultarFusionCartera."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)         AS "NombreCliente"                                       
    ,LEFT(ConsultarFusionCartera."CodigoPortafolio",60)       AS "Portafolio"
    ,ConsultarFusionCartera."IdMonedaInstrumento"             AS "Moneda"                                                                                                        
    ,ConsultarFusionCartera."IdTipoProducto"                  AS "Producto"
    ,ConsultarFusionCartera."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
    ,ConsultarFusionCartera."IdTipoInstrumento"               AS "TipoInstrumento"
    ,ConsultarFusionCartera."Nemotecnico"                     AS "Nemo"
    ,ConsultarFusionCartera."IdBoveda"                        AS "Boveda" 
    ,CAST(ConsultarFusionCartera."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
    ,ConsultarFusionCartera."IdEmisor"                        AS "Emisor"
    ,ConsultarFusionCartera."IdTipoReajuste"                  AS "TipoReajuste"
    ,ConsultarFusionCartera."CodigoIFRS"                      AS "CodigoIFRS"
    ,'CUSTODIA'                                         AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarFusionCartera."IdEmisor", ConsultarFusionCartera."IdClasificacionTipoInstrumento", 
     ConsultarFusionCartera."IdTipoInstrumento", ConsultarFusionCartera."Nemotecnico", ConsultarFusionCartera."IdTipoReajuste")
                                                        AS "ClaseEmisor"
    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'')                       AS "Agente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'')              AS "NombreAgente"
    ,COALESCE(ConsultarEjecutivo."CodigoSucursal",'')                       AS "SucursalAgente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'')              AS "NombreSucursalAgente"
    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')    AS "CentroCostoAgente"
    ,CASE WHEN ConsultarFusionCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
                        THEN 'CP'
            WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'     
                        THEN 'RE'
            ELSE    'NR'
          END                                           AS "Relacion"
    ,ConsultarFusionCartera."IdBolsa"                         AS "Bolsa"
    ,CASE WHEN ConsultarFusionCartera."EGRCantidad" = 0 THEN ConsultarFusionCartera."INGFechaLiquidacion"
                                ELSE ConsultarFusionCartera."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarFusionCartera."EGRCantidad" = 0 THEN ConsultarFusionCartera."INGFechaOperacion"
                                ELSE ConsultarFusionCartera."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarFusionCartera."IdConcepto"                              AS "IdConcepto"
    ,DocInd."DocumentoIdentidad"                            AS "DocumentoIdentidadCAVALI"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
        ConsultarFusionCartera."IdClasificacionTipoInstrumento",
        ConsultarFusionCartera."IdTipoInstrumento", 
        ConsultarFusionCartera."Nemotecnico",  
        ConsultarFusionCartera."IdEmisor" )       AS "TipoEmisor"
    ,COALESCE(MES."Codigo",'')	AS "Mesa"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarFusionCartera 
LEFT JOIN "BSCL.Custodia.BD::HIS.FusionCartera" DatoFusion 
        ON DatoFusion."IdCarteraResultante" = ConsultarFusionCartera."IdCartera"
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas" CarteraFusionada 
        ON CarteraFusionada."FolioCartera" = ConsultarFusionCartera."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarFusionCartera."IdCliente"
        
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SoCNeg
ON (ConsultarFusionCartera."IdCliente" = SoCNeg."IdCliente")

LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"              AS DocInd 
ON DocInd."IdPersona"                   = SoCNeg."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarFusionCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarFusionCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarFusionCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarFusionCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarFusionCartera."IdMesa" = MES."IdMesa"
WHERE ConsultarFusionCartera."IdEmpresa" = :p_IdEmpresa
AND (
    (ConsultarFusionCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarFusionCartera."EGRCantidad" = 0) OR (ConsultarFusionCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarFusionCartera."EGRCantidad" <> 0 AND ConsultarFusionCartera."INGFechaOperacion" > :p_FechaContable)
    )
AND ConsultarFusionCartera."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
AND CarteraFusionada."DiferenciaInversion" <> 0
AND ConsultarFusionCartera."IdEstado" = 'VIG';