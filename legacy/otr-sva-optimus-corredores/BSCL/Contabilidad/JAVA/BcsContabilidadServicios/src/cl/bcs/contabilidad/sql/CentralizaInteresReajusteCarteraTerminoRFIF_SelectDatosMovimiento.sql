SELECT
     ConsultarCartera."IdCartera"                       AS "IdMovimiento"        
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)         AS "NombreCliente"                                       
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)       AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"             AS "Moneda"                                                      
    ,ConsultarCartera."IdTipoProducto"                  AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"               AS "TipoInstrumento"
    ,ConsultarCartera."Nemotecnico"                     AS "Nemo"
    ,ConsultarCartera."IdBoveda"                        AS "Boveda" 
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
    ,ConsultarCartera."IdEmisor"                        AS "Emisor"
    ,ConsultarCartera."IdTipoReajuste"                  AS "TipoReajuste"
    ,ConsultarCartera."CodigoIFRS"                      AS "CodigoIFRS"
    ,'CUSTODIA'                                         AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
     ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
                                                        AS "ClaseEmisor"
    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'')                       AS "Agente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'')              AS "NombreAgente"
    ,COALESCE(ConsultarEjecutivo."CodigoSucursal",'')                       AS "SucursalAgente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'')              AS "NombreSucursalAgente"
    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')    AS "CentroCostoAgente"
    ,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
                        THEN 'CP'
            WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'     
                        THEN 'RE'
            ELSE    'NR'
          END                                           AS "Relacion"
    ,ConsultarCartera."IdBolsa"                         AS "Bolsa"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
                                ELSE ConsultarCartera."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
                                ELSE ConsultarCartera."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarCartera."IdConcepto"                              AS "IdConcepto"
    ,DocInd."DocumentoIdentidad"                            AS "DocumentoIdentidadCAVALI"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
        ConsultarCartera."IdClasificacionTipoInstrumento",
        ConsultarCartera."IdTipoInstrumento", 
        ConsultarCartera."Nemotecnico",  
        ConsultarCartera."IdEmisor" )       AS "TipoEmisor"
    ,COALESCE(MES."Codigo",'')	AS "Mesa"
    ,COALESCE(OPE."FolioOperacion",TO_BIGINT(0)) AS  "FolioOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
        
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SoCNeg
ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")

LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"              AS DocInd 
ON DocInd."IdPersona"                   = SoCNeg."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
			((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
			(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"	
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = ConsultarCartera."IdOperacion"
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa
AND (
    (ConsultarCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarCartera."EGRCantidad" = 0) OR (ConsultarCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarCartera."EGRCantidad" <> 0 AND ConsultarCartera."INGFechaOperacion" > :p_FechaContable)
    )
AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RF'
AND ConsultarCartera."IdTipoInstrumento"             <> 'PACTO'
AND ConsultarCartera."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora;