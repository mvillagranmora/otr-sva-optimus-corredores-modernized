SELECT
     ConsultaSubyacente."IdSubyacente"                          AS "IdMovimiento"        
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)             AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)                 AS "NombreCliente"                                       
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)               AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                     AS "Moneda"                                                      
    ,'EsRelacionado'                                            AS "EsRelacionado"
    ,CASE WHEN ConsultarCartera."IdTipoOperacion" = 'COMPAC' THEN 'C'
		  ELSE 'V'
     END 														AS "CompraVenta"
    
    ,CASE ConsultarCartera."DocumentoIdentidad" 
            WHEN ConsultarEmpresa."DocumentoIdentidad" 
            THEN 'S' ELSE 'N' END                               AS "EsCuentaPropia"                                                    
    ,ConsultarCartera."IdTipoProducto"                          AS "Producto"
    ,ConsultaSubyacente."IdClasificacionTipoInstrumento"        AS "ClaseInstrumento"
    ,ConsultaSubyacente."IdTipoInstrumento"                     AS "TipoInstrumento"
    ,ConsultaSubyacente."Nemotecnico"                           AS "Nemo"
    ,ConsultarCartera."IdBoveda"                                AS "Boveda" 
    ,CAST(ConsultarCartera."IdOperacion" AS NVARCHAR(60))         AS "IdOperacion"      
    ,ConsultaSubyacente."IdEmisor"                              AS "Emisor"
    ,ConsultaSubyacente."IdTipoReajuste"                        AS "TipoReajuste"
    ,ConsultarCartera."CodigoIFRS"                              AS "CodigoIFRS"
    ,'CUSTODIA'                                                 AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultaSubyacente."IdEmisor", ConsultaSubyacente."IdClasificacionTipoInstrumento", 
     ConsultaSubyacente."IdTipoInstrumento", ConsultaSubyacente."Nemotecnico", ConsultaSubyacente."IdTipoReajuste")
                                                                AS "ClaseEmisor"
    ,ConsultarEjecutivo."CodigoEmpleado"                        AS "Agente"
    ,LEFT(ConsultarEjecutivo."NombreCompleto",60)               AS "NombreAgente"
    ,ConsultarEjecutivo."CodigoSucursal"                        AS "SucursalAgente"
    ,LEFT(ConsultarEjecutivo."NombreSucursal",60)               AS "NombreSucursalAgente"
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)     AS "CentroCostoAgente"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
         ConsultaSubyacente."IdClasificacionTipoInstrumento"
        ,ConsultaSubyacente."IdTipoInstrumento"
        ,ConsultaSubyacente."Nemotecnico"
        ,ConsultaSubyacente."IdEmisor")                                 AS "TipoEmisor"
    ,CASE 
        WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
        WHEN UPPER(IndCli."CodigoValorIndicador") = 'VINCULADO' THEN 'RE'
        ELSE    'NR'
    END                                                       AS "Relacion"
    ,COALESCE(MES."Codigo",'')	AS "Mesa"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
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
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
        ON IndCli."IdCliente" =  ConsultarCartera."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'    
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"    
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable
AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
AND Operacion."IdEstado" = 'LIQ'
UNION ALL
SELECT
     ConsultarCartera."IdCartera"                          		AS "IdMovimiento"        
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)             AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)                 AS "NombreCliente"                                       
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)               AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                     AS "Moneda"                                                      
    ,'EsRelacionado'                                            AS "EsRelacionado"
    ,CASE WHEN ConsultarCartera."IdTipoOperacion" = 'COMPAC' THEN 'C'
		  ELSE 'V'
     END 														AS "CompraVenta"
    
    ,CASE ConsultarCartera."DocumentoIdentidad" 
            WHEN ConsultarEmpresa."DocumentoIdentidad" 
            THEN 'S' ELSE 'N' END                               AS "EsCuentaPropia"                                                    
    ,ConsultarCartera."IdTipoProducto"                          AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"          AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"                       AS "TipoInstrumento"
    ,ConsultarCartera."Nemotecnico"                             AS "Nemo"
    ,ConsultarCartera."IdBoveda"                                AS "Boveda" 
    ,CAST(ConsultarCartera."IdOperacion" AS NVARCHAR(60))         AS "IdOperacion"      
    ,ConsultarCartera."IdEmisor"                                AS "Emisor"
    ,ConsultarCartera."IdTipoReajuste"                          AS "TipoReajuste"
    ,ConsultarCartera."CodigoIFRS"                              AS "CodigoIFRS"
    ,'CUSTODIA'                                                 AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
     ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
                                                                AS "ClaseEmisor"
    ,ConsultarEjecutivo."CodigoEmpleado"                        AS "Agente"
    ,LEFT(ConsultarEjecutivo."NombreCompleto",60)               AS "NombreAgente"
    ,ConsultarEjecutivo."CodigoSucursal"                        AS "SucursalAgente"
    ,LEFT(ConsultarEjecutivo."NombreSucursal",60)               AS "NombreSucursalAgente"
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)     AS "CentroCostoAgente"
    ,''                               AS "TipoEmisor"
    ,CASE 
        WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
        WHEN IndCli."ValorIndicador" = 'S'  THEN 'RE'
        ELSE    'NR'
    END                                                       AS "Relacion"
    ,COALESCE(MES."Codigo",'')	AS "Mesa"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")

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
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
        ON IndCli."IdCliente" =  ConsultarCartera."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'    
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"    
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable
AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
AND Operacion."IdEstado" = 'LIQ';
