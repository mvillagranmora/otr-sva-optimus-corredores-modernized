SELECT 
Cmbte."IdComprobante"                                  AS "IdMovimiento"
,Cmbte."IdTipoComprobante"                                  AS "TipoDocumento"
,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"
,YEAR("FechaSistema")       ||'-'|| RIGHT('0' || 
 MONTH("FechaSistema"),2)   ||'-'|| RIGHT('0' || 
 DAYOFMONTH("FechaSistema"),2)                          AS "FechaDocumento"
, CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
    CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'V' WHEN 'VENTA'  THEN 'C' END 
    ELSE LEFT(Cmbte."AccionTipoOperacion",1)
END                                                             AS "CompraVenta"
,Cmbte."IdTipoOperacion"                                AS "Operacion"
,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
    WHEN '' THEN Cmbte."IdConcepto"--Fac."CentroCosto"
    ELSE Cmbte."CodigoTipoProducto"                             
END                                         AS "Producto"
,IFNULL(Cmbte."IdClasificacionTipoInstrumento",'X')     AS "ClaseInstrumento"
,IFNULL(Cmbte."IdTipoInstrumento",'X')                  AS "TipoInstrumento"
,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
,Cmbte."IdBolsa"                                        AS "Bolsa"
,Cmbte."DocumentoIdentidad"                             AS "DocumentoIdentidad"
,LEFT(Cli."NombreCompleto",60)                          AS "NombreCliente"
,LEFT(Por."CodigoPortafolio",60)                        AS "Portafolio"
,Cmbte."IdConcepto"                                     AS "Concepto"
,LEFT(Con."Nombre",60)                                  AS "NombreConcepto"
,Eje."CodigoEmpleado"                                   AS "Agente"
,LEFT(Eje."NombreCompleto",60)                          AS "NombreAgente"
,Eje."CodigoSucursal"                                   AS "SucursalAgente"
,LEFT(Eje."NombreSucursal",60)                          AS "NombreSucursalAgente"
,LEFT(Indemp."ValorIndicador",60)                       AS "CentroCostoAgente"
,"IdEstadoSII"                                          AS "EstadoSII"
,"IdEstadoSII"                                          AS "EstadoSUNAT"
,CASE "EsExento" WHEN 'N' THEN 'NO' ELSE 'SI' END       AS "EsExento"
,(CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMoneda" END)   AS "Moneda"
,Cmbte."IdMonedaLiquidacion"                            AS "MonedaLiquidacion"
,'NO'                                                    AS "EsContraparte"
,CASE 
    WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
                THEN 'CP'
    WHEN UPPER(IndCli."CodigoValorIndicador") = 'VINCULADO'  
                THEN 'RE'
    ELSE    'NR'
  END                                                   AS "Relacion",
  
Cli."Tipo"                                              AS "TipoPersona",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
        END ) 
        WHEN 'COMPRA' THEN ''
        WHEN 'VENTA'  THEN ClasEmi."ClaseEmisor"
        END                                             AS "TipoInstrumentoVenta",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
        END ) 
        WHEN 'COMPRA' THEN ClasEmi."ClaseEmisor"
        WHEN 'VENTA'  THEN ''
        END                                             AS "TipoInstrumentoCompra"                              ,

"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", CmbteDet."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                        AS "ClaseEmisor",
CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN
CASE Cmbte."IdInstrumento" WHEN 0 THEN ''
ELSE COALESCE((SELECT  MOV."IdEmisor" FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV WHERE Cmbte."IdComprobante" = MOV."IdComprobante" AND Cmbte."IdClasificacionTipoInstrumento" = 'IF' AND Cmbte."IdInstrumento" != 0),'') END
ELSE Ins."IdEmisor" END                                 AS "Emisor",
Emi."IdTipoEmisor"                                      AS "TipoEmisor",
Ins."Nemotecnico"                                       AS "Nemotecnico",
IFNULL(Ins."TipoBonoIRF",'X')                           AS "TipoBonoIRF",
TRIM(IFNULL(Cmbte."CodigoIFRS",'X'))                    AS "CodigoIFRS",
IFNULL(Ins."OrigenInstrumento",'X')                     AS "OrigenInstrumento",
IFNULL(
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                        WHEN 'VENTA'  THEN 'X'
                        END,'X')                             AS "IndicadorRelacionadoGrupo",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
      END )     
        WHEN 'COMPRA' THEN 'X' WHEN 'VENTA'  THEN 
            CASE Cmbte."IdBolsa"
                WHEN 'BVL' THEN 'C'
                WHEN 'FRD' THEN 'C'
                ELSE 'E'
            END 
        END                                             AS "Plaza",
IFNULL(Ins."PactoAndino",'X') 			                AS "PactoAndino",   
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN 'X'
                        WHEN 'VENTA'  THEN Ins."FormadorMercado"
END                                                     AS "FormadorMercado",
CASE WHEN (((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0)AND Ins."PresenciaBursatilAjustada" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))
        THEN 'S'
        ELSE 'X'
END                                                     AS "PresenciaBursatil"
,Cmbte."CodigoTipoComprobante"          AS "CodigoTipoComprobante"
,Cmbte."FechaLiquidacion"               AS "FechaLiquidacion"
,Cmbte."IdTipoComprobante"              AS "TipoComprobante"
,Cmbte."FechaComprobante"               AS "FechaComprobante"
,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
    WHEN '' THEN Fac."CentroCosto"
    ELSE CenCos."Business"                          
END                                         AS "OpCentros"
,Cmbte."IdEstadoComprobante"                AS "EstadoComprobante"
, CASE Cmbte."IdTipoProducto"
        WHEN '' THEN 'SI'
        ELSE 'NO'   
        END                                 AS "EsOtrosConceptos"
, Cmbte."FolioComprobante"                  AS "FolioPoliza"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, Cmbte."DocumentoIdentidadCAVALI"          AS "DocumentoIdentidadCAVALI"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE               'NO' 
  END   AS "EsArbitraje"
, '' AS "ClienteBancario"            
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
LEFT JOIN   "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"       AS CmbteDet
ON          CmbteDet."IdComprobante" = Cmbte."IdComprobante"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa" AND 
Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"               AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                 AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
--ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoSupervisor" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'GENPRIMREL'--'RELCORR'

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"                             AS Ins 
ON Ins."IdInstrumento" = CmbteDet."IdInstrumento" 

LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor"                         AS Emi
ON Ins."IdEmisor" = Emi."IdEmisor"

LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker"                             AS MarkMak
                   On MarkMak."IdInstrumento" = CmbteDet."IdInstrumento"
                  AND ((CmbteDet."FechaComprobante" >= MarkMak."FechaInicio") AND (CmbteDet."FechaComprobante" <= MarkMak."FechaFin"))
                  AND MarkMak."IdEstado" = 'ACT' and
				 ((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))

LEFT JOIN   "BSCL.Contabilidad.BD::ITZ.ClaseEmisor"                     AS ClasEmi
ON (ClasEmi."IdEmpresa"     = Cmbte."IdEmpresa" AND
    ClasEmi."TipoOperacion" = CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA'WHEN 'VENTA'  THEN 'COMPRA' END 
                                ELSE Cmbte."AccionTipoOperacion"
                              END                           
    AND ClasEmi."ClaseInstrumento"  = Cmbte."IdClasificacionTipoInstrumento"
    AND trim(ClasEmi."TipoBonoIRF")     = trim(Ins."TipoBonoIRF")                         
    AND ClasEmi."OrigenInstrumento" = Ins."OrigenInstrumento"
    AND ClasEmi."Plaza"             = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN 'X'
                                            WHEN 'VENTA'  THEN 
                                                CASE Cmbte."IdBolsa"
                                                    WHEN 'BVL' THEN 'C'
                                                    WHEN 'FRD' THEN 'C'
                                                    ELSE 'E'
                                                    END 
                                            END
                                            )
    AND TRIM(ClasEmi."CodigoIFRS")          =  TRIM(Cmbte."CodigoIFRS") --'D'                                   
    AND ClasEmi."RelacionadoGrupo"      = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                                            WHEN 'VENTA'  THEN 'X'
                                            END
    AND ClasEmi."FormadorMercado"       =  CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN 'X'
                                            WHEN 'VENTA'  THEN Ins."FormadorMercado"
                                            END
    AND ClasEmi."PresenciaBursatil"      = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                                    CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                                    ELSE Cmbte."AccionTipoOperacion"
                                                    END ) 
                                                WHEN 'COMPRA' THEN 'X'
                                                WHEN 'VENTA'  THEN
                                                    CASE WHEN (((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) ) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))
                                                        THEN 'S' ELSE 'N'
                                                    END 
                                            END
    AND ClasEmi."PactoAndino"           = Ins."PactoAndino"
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentroCosto"           CenCos
ON (CenCos."IdEmpresa"  = Cmbte."IdEmpresa" AND CenCos."ProductoConcepto" = Cmbte."CodigoTipoProducto" AND
    CenCos."Relacionado"= CASE Cmbte."DocumentoIdentidad" 
                            WHEN Emp."DocumentoIdentidad"   
                            THEN 'CP' 
                            ELSE 'CL' 
                            END AND 
    CenCos."IdBolsa" = Cmbte."IdBolsa"      AND
    CenCos."TipoCentro" = CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END 
                                ELSE CASE Cmbte."AccionTipoOperacion"
                                        WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END
                            END 
    
    )
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"                    AS Fac 
ON ( Cmbte."IdConcepto" = Fac."IdConcepto")
 
WHERE   Cmbte."IdEmpresa" = :IdEmpresa AND
        Cmbte."FechaSistema" = :FechaContable AND 
        Cmbte."IdEstadoComprobante" <> 'ANU' AND
        Cmbte."DocumentoIdentidad" != :DocumentoIdentidad AND 
		Cmbte."EsVentaCorta" <> 'S'
---- ANULADOS   
UNION ALL           
SELECT 
         Cmbte."IdComprobante"                                  AS "IdMovimiento"
    ,concat('A',Cmbte."IdTipoComprobante")                      AS "TipoDocumento"              
    ,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"
    ,YEAR("FechaSistema")       ||'-'|| RIGHT('0' || 
     MONTH("FechaSistema"),2)   ||'-'|| RIGHT('0' || 
     DAYOFMONTH("FechaSistema"),2)                          AS "FechaDocumento"
    , CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'V' WHEN 'VENTA'  THEN 'C' END 
        ELSE LEFT(Cmbte."AccionTipoOperacion",1)
    END                                                             AS "CompraVenta"
    ,Cmbte."IdTipoOperacion"                                AS "Operacion"
    ,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
        WHEN '' THEN Fac."CentroCosto"
        ELSE Cmbte."CodigoTipoProducto"                             
    END                                         AS "Producto"
    ,IFNULL(Cmbte."IdClasificacionTipoInstrumento",'X')     AS "ClaseInstrumento"
    ,IFNULL(Cmbte."IdTipoInstrumento",'X')                  AS "TipoInstrumento"
    ,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
    ,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
    ,Cmbte."IdBolsa"                                        AS "Bolsa"
    ,Cmbte."DocumentoIdentidad"                             AS "DocumentoIdentidad"
    ,LEFT(Cli."NombreCompleto",60)                          AS "NombreCliente"
    ,LEFT(Por."CodigoPortafolio",60)                        AS "Portafolio"
    ,Cmbte."IdConcepto"                                     AS "Concepto"
    ,LEFT(Con."Nombre",60)                                  AS "NombreConcepto"
    ,Eje."CodigoEmpleado"                                   AS "Agente"
    ,LEFT(Eje."NombreCompleto",60)                          AS "NombreAgente"
    ,Eje."CodigoSucursal"                                   AS "SucursalAgente"
    ,LEFT(Eje."NombreSucursal",60)                          AS "NombreSucursalAgente"
    ,LEFT(Indemp."ValorIndicador",60)                       AS "CentroCostoAgente"
    ,"IdEstadoSII"                                          AS "EstadoSII"
    ,"IdEstadoSII"                                          AS "EstadoSUNAT"
    ,CASE "EsExento" WHEN 'N' THEN 'NO' ELSE 'SI' END       AS "EsExento"
    ,(CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMoneda" END)   AS "Moneda"
    ,Cmbte."IdMonedaLiquidacion"                            AS "MonedaLiquidacion"
    ,'NO'                                                    AS "EsContraparte"
    ,CASE 
        WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
                    THEN 'CP'
        WHEN UPPER(IndCli."CodigoValorIndicador") = 'VINCULADO'  
                    THEN 'RE'
        ELSE    'NR'
      END                                                   AS "Relacion",
    Cli."Tipo"                                              AS "TipoPersona",
    CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
            ELSE Cmbte."AccionTipoOperacion"
            END ) 
            WHEN 'COMPRA' THEN ''
            WHEN 'VENTA'  THEN ClasEmi."ClaseEmisor"
            END                                             AS "TipoInstrumentoVenta",
    CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
            ELSE Cmbte."AccionTipoOperacion"
            END ) 
            WHEN 'COMPRA' THEN ClasEmi."ClaseEmisor"
            WHEN 'VENTA'  THEN ''
            END                                             AS "TipoInstrumentoCompra"                              ,

    "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
    (Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
    Cmbte."IdTipoInstrumento", CmbteDet."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                            AS "ClaseEmisor",
    CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN
	CASE Cmbte."IdInstrumento" WHEN 0 THEN ''
	ELSE COALESCE((SELECT  MOV."IdEmisor" FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV WHERE Cmbte."IdComprobante" = MOV."IdComprobante" AND Cmbte."IdClasificacionTipoInstrumento" = 'IF' AND Cmbte."IdInstrumento" != 0),'') END
	ELSE Ins."IdEmisor" END                                 AS "Emisor",
    Emi."IdTipoEmisor"                                      AS "TipoEmisor",
    Ins."Nemotecnico"                                       AS "Nemotecnico",
    IFNULL(Ins."TipoBonoIRF",'X')                           AS "TipoBonoIRF",
    TRIM(IFNULL(Cmbte."CodigoIFRS",'X'))                    AS "CodigoIFRS",
    IFNULL(Ins."OrigenInstrumento",'X')                     AS "OrigenInstrumento",
    IFNULL(
    CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                            ELSE Cmbte."AccionTipoOperacion"
                            END ) 
                            WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                            WHEN 'VENTA'  THEN 'X'
                            END,'X')                             AS "IndicadorRelacionadoGrupo",
    CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
            CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
            ELSE Cmbte."AccionTipoOperacion"
          END )     
            WHEN 'COMPRA' THEN 'X' WHEN 'VENTA'  THEN 
                CASE Cmbte."IdBolsa"
                    WHEN 'BVL' THEN 'C'
                    WHEN 'FRD' THEN 'C'
                    ELSE 'E'
                END 
            END                                             AS "Plaza",
    IFNULL(Ins."PactoAndino",'X')		                    AS "PactoAndino",   
    CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                            ELSE Cmbte."AccionTipoOperacion"
                            END ) 
                            WHEN 'COMPRA' THEN ''
                            WHEN 'VENTA'  THEN Ins."FormadorMercado"
    END                                                     AS "FormadorMercado",
    CASE WHEN (((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0)AND Ins."PresenciaBursatilAjustada" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))
            THEN 'S'
            ELSE ''
    END                                                     AS "PresenciaBursatil"
    ,Cmbte."CodigoTipoComprobante"          AS "CodigoTipoComprobante"
    ,Cmbte."FechaLiquidacion"               AS "FechaLiquidacion"
    ,Cmbte."IdTipoComprobante"              AS "TipoComprobante"
    ,Cmbte."FechaComprobante"               AS "FechaComprobante"
    ,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
        WHEN '' THEN Fac."CentroCosto"
        ELSE CenCos."Business"                          
    END                                         AS "OpCentros"
    ,Cmbte."IdEstadoComprobante"                AS "EstadoComprobante"
    , CASE Cmbte."IdTipoProducto"
            WHEN '' THEN 'SI'
            ELSE 'NO'   
            END                                 AS "EsOtrosConceptos"
    , Cmbte."FolioComprobante"                  AS "FolioPoliza"
    , Cmbte."EsAPlazo"                          AS "EsAPlazo"
    , Cmbte."DocumentoIdentidadCAVALI"          AS "DocumentoIdentidadCAVALI"
    , CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
    , CASE Cmbte."NemotecnicoInstrumento" 
        WHEN 'ARBDIS' THEN 'SI' 
        WHEN 'ARBINT' THEN 'SI'               
        ELSE               'NO' 
      END   AS "EsArbitraje"
    , '' AS "ClienteBancario"
    
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
LEFT JOIN   "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"       AS CmbteDet
ON          CmbteDet."IdComprobante" = Cmbte."IdComprobante"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa" AND 
Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad")


LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"               AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                 AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
--ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoSupervisor" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'GENPRIMREL'--'RELCORR'
    

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"                             AS Ins 
ON Ins."IdInstrumento" = CmbteDet."IdInstrumento" 
--      AND Ins."PresenciaBursatilAjustada" > 0

LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor"                         AS Emi
ON Ins."IdEmisor" = Emi."IdEmisor"

LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker"                             AS MarkMak
               On MarkMak."IdInstrumento" = CmbteDet."IdInstrumento"
              AND ((CmbteDet."FechaComprobante" >= MarkMak."FechaInicio") AND (CmbteDet."FechaComprobante" <= MarkMak."FechaFin"))
              AND MarkMak."IdEstado" = 'ACT' and
			((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))

--
LEFT JOIN   "BSCL.Contabilidad.BD::ITZ.ClaseEmisor"                     AS ClasEmi
ON (ClasEmi."IdEmpresa"     = Cmbte."IdEmpresa" AND
    ClasEmi."TipoOperacion" = CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA'WHEN 'VENTA'  THEN 'COMPRA' END 
                                ELSE Cmbte."AccionTipoOperacion"
                              END                           
    AND ClasEmi."ClaseInstrumento"  = Cmbte."IdClasificacionTipoInstrumento"
    AND trim(ClasEmi."TipoBonoIRF")     = trim(Ins."TipoBonoIRF")                           
    AND ClasEmi."OrigenInstrumento" = Ins."OrigenInstrumento"
    AND ClasEmi."Plaza"             = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN 'X'
                                            WHEN 'VENTA'  THEN 
                                                CASE Cmbte."IdBolsa"
                                                    WHEN 'BVL' THEN 'C'
                                                    WHEN 'FRD' THEN 'C'
                                                    ELSE 'E'
                                                    END 
                                            END
                                            )
    AND TRIM(ClasEmi."CodigoIFRS")          =  TRIM(Cmbte."CodigoIFRS") --'D'                                   
    AND ClasEmi."RelacionadoGrupo"      = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                                            WHEN 'VENTA'  THEN 'X'
                                            END
    AND ClasEmi."FormadorMercado"       =  CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                            CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                            ELSE Cmbte."AccionTipoOperacion"
                                            END ) 
                                            WHEN 'COMPRA' THEN ''
                                            WHEN 'VENTA'  THEN Ins."FormadorMercado"
                                            END
    AND ClasEmi."PresenciaBursatil"      = CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                                    CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                                                    ELSE Cmbte."AccionTipoOperacion"
                                                    END ) 
                                                WHEN 'COMPRA' THEN ''
                                                WHEN 'VENTA'  THEN
                                                    CASE WHEN (((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) ) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))
                                                        THEN 'S' ELSE 'N'
                                                    END 
                                            END
    AND ClasEmi."PactoAndino"           = Ins."PactoAndino"

LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentroCosto"           CenCos
ON (CenCos."IdEmpresa"  = Cmbte."IdEmpresa" AND CenCos."ProductoConcepto" = Cmbte."CodigoTipoProducto" AND
    CenCos."Relacionado"= CASE Cmbte."DocumentoIdentidad" 
                            WHEN Emp."DocumentoIdentidad"   
                            THEN 'CP' 
                            ELSE 'CL' 
                            END AND 
    CenCos."IdBolsa" = Cmbte."IdBolsa"      AND
    CenCos."TipoCentro" = CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                                CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END 
                                ELSE CASE Cmbte."AccionTipoOperacion"
                                        WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END
                            END 
    
    )
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"                    AS Fac 
ON ( Cmbte."IdConcepto" = Fac."IdConcepto")

INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Movimiento"                  Mov
ON (Cmbte."IdTipoComprobante"   = Mov."IdTipoMovimiento"    AND
    Cmbte."IdComprobante"       = Mov."Numero")
INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Centralizacion"              Cent
ON (Cent."IdCentralizacion"     = Mov."IdCentralizacion"    AND 
    Cent."FechaContable"        = Mov."Fecha")              
                
            
WHERE   Cmbte."IdEmpresa"               = :IdEmpresa           		AND
        Cmbte."FechaSistema"            = :FechaContable       		AND 
        Cmbte."IdEstadoComprobante"     = 'ANU'                 	AND 
        Cent."IdTipoCentralizacion"     = :IdTipoCentralizacion    	AND 
        Cent."FechaContable"            = :FechaContable           	AND 
        Cent."Estado"                   = 'V' 						AND
        Cmbte."DocumentoIdentidad" 		!= :DocumentoIdentidad AND 
		Cmbte."EsVentaCorta" <> 'S'