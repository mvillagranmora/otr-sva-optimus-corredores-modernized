SELECT
    CTE."IdTarjetaExistencia"                                      AS "IdMovimiento"
    ,CASE WHEN "Perdida"<> 0 THEN 'PER' ELSE 'GAN' END      AS "TipoDocumento"
    ,CTE."FolioCartera"                                             AS "FolioCartera"
    ,YEAR(CTE."FechaSistema") ||'-'|| 
    RIGHT('0' || MONTH(CTE."FechaSistema"),2) ||'-'|| 
    RIGHT('0' || DAYOFMONTH(CTE."FechaSistema"),2)                  AS "FechaDocumento"
    ,CTE."IdEmpresa"                                                AS "IdEmpresa"                          
    ,YEAR(CTE."FechaOperacion") ||'-'||
    RIGHT('0' || MONTH(CTE."FechaOperacion"),2) ||'-'|| 
    RIGHT('0' || DAYOFMONTH(CTE."FechaOperacion"),2)                AS "Fecha"                                                      
    ,LEFT(CTE."NombreCliente",60)                                   AS "NombreCliente"
    ,LEFT(CTE."DocumentoIdentidad",60)                              AS "DocumentoIdentidad"
    ,LEFT(CTE."DocumentoIdentidadAdicional",60)                     AS "DocumentoIdentidadAdicional"
    ,LEFT(CTE."CodigoPortafolio",60)                                AS "Portafolio"
    ,CTE."IdConcepto"                                               AS "Concepto"
    ,LEFT(CTE."NombreConcepto",60)                                  AS "NombreConcepto"
    ,CTE."IdMoneda"                                                 AS "Moneda"
    ,CTE."IdClasificacionTipoInstrumento"                           AS "ClaseInstrumento"
    ,CTE."IdTipoInstrumento"                                        AS "TipoInstrumento"    
    ,CTE."ISIN"     
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(CTE."IdEmisor", CTE."IdClasificacionTipoInstrumento", CTE."IdTipoInstrumento", CTE."Nemotecnico", CTE."IdTipoReajuste") AS "ClaseEmisor"
    ,IFNULL(LEFT(EJE."CodigoEmpleado",60),'')                       AS "Agente"
    ,IFNULL(LEFT(EJE."NombreCompletoEjecutivo",60),'')              AS "NombreAgente"               
    ,IFNULL(LEFT(EJE."CodigoSucursal",60),'')                       AS "SucursalAgente"
    ,IFNULL(LEFT(EJE."NombreSucursal",60),'')                       AS "NombreSucursalAgente"
    ,IFNULL(LEFT(INDEMP."ValorIndicador",60),'')                    AS "CentroCostoAgente"
    
    ,''                                                             AS "Producto"
    ,CASE WHEN CTE."DocumentoIdentidad" = EMP."DocumentoIdentidad"
        THEN 'CP'
        WHEN  UPPER(CIC."CodigoValorIndicador") = 'VINCULADO'      
        THEN 'RE'
        ELSE        'NR'
    END                                                             AS "Relacion"
    ,''                                                             AS "Bolsa"
    ,CLI."Tipo"                                                     AS "TipoPersona"
    ,CTE."Nemotecnico"                                              AS "Nemo"
    ,CTE."TipoUI"                                                   AS "CompraVenta"
    ,DocInd."DocumentoIdentidad"                                    AS "DocumentoIdentidadCAVALI"
    ,CAR."FolioComprobante"                                         AS "FolioComprobante"
    ,CAR."IdBoveda"													AS "Boveda"
    ,CAR."CodigoIFRS"												AS "CodigoIFRS"
    ,CTE."IdTipoReajuste"											AS "IdTipoReajuste"
            
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia"           AS CTE
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                 AS CAR
    ON (CTE."FolioCartera" = CAR."FolioCartera")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"               AS EMP
    ON EMP."IdEmpresa"     = CTE."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EJE
    ON EJE."IdPortafolio"  = CTE."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"   AS INDEMP
    ON INDEMP."IdEmpleado" =  EJE."IdEmpleadoEjecutivoPortafolio" AND INDEMP."CodigoTipoIndicador"  = 'CENCOSTO'
    
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC
    ON CIC."IdCliente" =  CTE."IdCliente" AND CIC."CodigoTipoIndicador"  = 'GENPRIMREL'
    
    
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"             AS CLI
    ON CLI."IdCliente"= CTE."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"    AS SoCNeg
    ON (CTE."IdCliente" = SoCNeg."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"          AS DocInd 
    ON DocInd."IdPersona"                   = SoCNeg."IdPersona" AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'
  
WHERE CTE."FechaProceso"   = :FechaContable
AND CTE."IdEmpresa" 	   = :IdEmpresa
AND CTE."Tipo"             = 'EGRESO'
AND CTE."IdEstado"         = 'VIG'
--AND CAR."IdEstado"       = 'VIG';