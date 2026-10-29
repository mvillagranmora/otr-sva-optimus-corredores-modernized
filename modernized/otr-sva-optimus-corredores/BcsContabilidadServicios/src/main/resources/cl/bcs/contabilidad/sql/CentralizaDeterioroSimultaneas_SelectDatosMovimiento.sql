SELECT
 	MOV."IdMovimiento"        
    ,MOV."DocumentoIdentidad"                                    
    ,MOV."NombreCliente"                                       
    ,MOV."Portafolio"
    ,MOV."Moneda"                                                                                                          
    ,MOV."Producto"
    ,MOV."ClaseInstrumento"
    ,MOV."TipoInstrumento"
    ,MOV."Nemo"
    ,MOV."Boveda" 
    ,MOV."IdCartera"      
    ,MOV."Emisor"
    ,MOV."TipoCartera"
    ,MOV."ClaseEmisor"
    ,MOV."Agente"
    ,MOV."NombreAgente"
    ,MOV."SucursalAgente"
    ,MOV."NombreSucursalAgente"
    ,MOV."CentroCostoAgente"
    ,MOV."Relacion"
    ,MOV."Bolsa"
    ,MOV."FechaLiquidacion"
    ,MOV."TipoPersona"
    ,MOV."FechaOperacion"
    ,MOV."IdConcepto"
 	,DET."Fase"
FROM 
(SELECT
     ConsultarCartera."IdCartera"                           AS "IdMovimiento"        
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)         AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)             AS "NombreCliente"                                       
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)           AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                 AS "Moneda"                                                                                                          
    ,ConsultarCartera."IdTipoProducto"                      AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"      AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"                   AS "TipoInstrumento"
    ,ConsultarCarteraSubyacenteBasica."Nemotecnico"         AS "Nemo"
    ,ConsultarCartera."IdBoveda"                            AS "Boveda" 
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))     AS "IdCartera"      
    ,ConsultarCarteraSubyacenteBasica."IdEmisor"            AS "Emisor"
    ,'CUSTODIA'                                             AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
     ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
                                                            AS "ClaseEmisor"
    ,ConsultarEjecutivo."CodigoEmpleado"                    AS "Agente"
    ,LEFT(ConsultarEjecutivo."NombreCompleto",60)           AS "NombreAgente"
    ,ConsultarEjecutivo."CodigoSucursal"                    AS "SucursalAgente"
    ,LEFT(ConsultarEjecutivo."NombreSucursal",60)           AS "NombreSucursalAgente"
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
    ,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
            THEN 'CP'
          WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'   
            THEN 'RE'
          ELSE  'NR'
          END                                               AS "Relacion"
    ,ConsultarCartera."IdBolsa"                             AS "Bolsa"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
                                ELSE ConsultarCartera."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
                                ELSE ConsultarCartera."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarCartera."IdConcepto"                              AS "IdConcepto"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" ConsultarCarteraSubyacenteBasica
        ON ConsultarCarteraSubyacenteBasica."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND (ConsultarCartera."FechaVencimientoOperacion" > :p_FechaContable AND ConsultarCartera."EGRCantidad" = 0) 
    AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RV' 
    AND ConsultarCartera."IdTipoInstrumento" = 'SIM'
    AND ConsultarCartera."IdEstado" = 'VIG'
    AND ConsultarCarteraSubyacenteBasica."IdEstado" = 'VIG'
UNION ALL
SELECT
     ConsultarCartera."IdCartera"                           AS "IdMovimiento"        
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)         AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)             AS "NombreCliente"                                       
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)           AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                 AS "Moneda"                                                      
    ,ConsultarCartera."IdTipoProducto"                      AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"      AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"                   AS "TipoInstrumento"
    ,ConsultarCarteraSubyacenteBasica."Nemotecnico"         AS "Nemo"
    ,ConsultarCartera."IdBoveda"                            AS "Boveda" 
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))     AS "IdCartera"      
    ,ConsultarCarteraSubyacenteBasica."IdEmisor"                            AS "Emisor"
    ,'CUSTODIA'                                             AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
     ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
                                                            AS "ClaseEmisor"
    ,ConsultarEjecutivo."CodigoEmpleado"                    AS "Agente"
    ,LEFT(ConsultarEjecutivo."NombreCompleto",60)           AS "NombreAgente"
    ,ConsultarEjecutivo."CodigoSucursal"                    AS "SucursalAgente"
    ,LEFT(ConsultarEjecutivo."NombreSucursal",60)           AS "NombreSucursalAgente"
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
    ,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
            THEN 'CP'
          WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'   
            THEN 'RE'
          ELSE  'NR'
          END                                               AS "Relacion"
    ,ConsultarCartera."IdBolsa"                             AS "Bolsa"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
                                ELSE ConsultarCartera."EGRFechaLiquidacion" END
                                                                AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                                    AS "TipoPersona"
    ,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
                                ELSE ConsultarCartera."EGRFechaOperacion" END
                                                                AS "FechaOperacion"
    ,ConsultarCartera."IdConcepto"                              AS "IdConcepto"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" ConsultarCarteraSubyacenteBasica
        ON ConsultarCarteraSubyacenteBasica."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ConsultarCartera."EGRFechaOperacion" = :p_FechaContable 
    AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RV' 
    AND ConsultarCartera."IdTipoInstrumento" = 'SIM'
    AND ConsultarCartera."IdEstado" IN ('VCD','ANT')) AS MOV
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = MOV."Emisor" AND DET."IdEstado" = 'ACT'
WHERE DET."FlagDeterioro" = 'S' AND DET."PorcentajeDeterioro" > 0;