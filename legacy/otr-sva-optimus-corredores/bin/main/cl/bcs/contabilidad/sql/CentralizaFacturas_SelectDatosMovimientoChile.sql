SELECT 
Cmbte."IdComprobante"                                  	AS "IdMovimiento"
,Cmbte."IdTipoComprobante"                              AS "TipoDocumento"
,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"
,YEAR("FechaSistema")       ||'-'|| RIGHT('0' || 
 MONTH("FechaSistema"),2)   ||'-'|| RIGHT('0' || 
 DAYOFMONTH("FechaSistema"),2)                          AS "FechaDocumento"
, CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
    CASE Cmbte."AccionTipoOperacion" 
    	WHEN 'COMPRA' THEN 'V' 
    	WHEN 'VENTA'  THEN 'C' END 
    ELSE 	
    	CASE Cmbte."AccionTipoOperacion" 
    	WHEN 'VENTA CORTA' THEN 'V' 
    	ELSE LEFT(Cmbte."AccionTipoOperacion",1) END
END                                                             AS "CompraVenta"
,Cmbte."IdTipoOperacion"                                AS "Operacion"
,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
    WHEN '' THEN Fac."CentroCosto"
    ELSE Cmbte."CodigoTipoProducto"                             
END                                         			AS "Producto"
,Cmbte."IdClasificacionTipoInstrumento"                 AS "ClaseInstrumento"
,Cmbte."IdTipoInstrumento"                              AS "TipoInstrumento"
,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
,Cmbte."IdBolsa"                                        AS "Bolsa"
,Cmbte."DocumentoIdentidad"                             AS "DocumentoIdentidad"
,LEFT(CASE Per."Tipo" 
			WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
			ELSE Per."RazonSocial"
		END,60)                          AS "NombreCliente"
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
,Cmbte."IdMonedaNegociacion"							AS "MonedaNegociacion"
,'N'                                                    AS "EsContraparte"
,CASE 
    WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
                THEN 'CP'
    WHEN IndCli."ValorIndicador" = 'S'  
                THEN 'RE'
    ELSE    'NR'
  END                                                  	AS "Relacion",
Per."Tipo"                                              AS "TipoPersona",
''                                           			AS "TipoInstrumentoVenta",
''                                             			AS "TipoInstrumentoCompra",

"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", Cmbte."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                        AS "ClaseEmisor",
CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN '' ELSE Ins."IdEmisor" END AS "Emisor",
Emi."IdTipoEmisor"                                      AS "TipoEmisor",
Ins."Nemotecnico"                                       AS "Nemotecnico",
Ins."TipoBonoIRF"                                       AS "TipoBonoIRF",
TRIM(Cmbte."CodigoIFRS")                                AS "CodigoIFRS",
Ins."OrigenInstrumento"                                 AS "OrigenInstrumento",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                        WHEN 'VENTA'  THEN ''
                        END                             AS "IndicadorRelacionadoGrupo",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
      END )     
        WHEN 'COMPRA' THEN '' WHEN 'VENTA'  THEN 
            CASE Cmbte."IdBolsa"
                WHEN 'BVL' THEN 'C'
                WHEN 'FRD' THEN 'C'
                ELSE 'E'
            END 
        END                                             AS "Plaza",
Ins."PactoAndino"                   AS "PactoAndino",   
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
, COALESCE(Cmbte."FolioComprobante", 0) AS "FolioDTE"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, ''                                        AS "DocumentoIdentidadCAVALI"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE               'NO' 
  END   AS "EsArbitraje"
,COALESCE(IndCli2."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
,area."Descripcion" AS "AreaCliente"
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa")

LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"               				AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" 								AS Per 
ON Per."IdPersona" =  Cli."IdPersona"

LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio"                 			AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli2
ON IndCli2."IdCliente" =  Cmbte."IdCliente" AND IndCli2."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"                             AS Ins 
ON Ins."IdInstrumento" = Cmbte."IdInstrumento" 

LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor"                                  AS Emi
ON Ins."IdEmisor" = Emi."IdEmisor"

LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker"                             AS MarkMak
           On MarkMak."IdInstrumento" = Cmbte."IdInstrumento"
          AND ((Cmbte."FechaComprobante" >= MarkMak."FechaInicio") AND (Cmbte."FechaComprobante" <= MarkMak."FechaFin"))
          AND MarkMak."IdEstado" = 'ACT' and
		 ((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))

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
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS Fac 
ON ( Cmbte."IdConcepto" = Fac."IdConcepto")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS area ON Por."AreaCliente" = area."Codigo"
WHERE Cmbte."IdEmpresa" = :IdEmpresa AND
Cmbte."FechaSistema" = :FechaContable AND 
Cmbte."IdEstadoComprobante" <> 'ANU' AND
Cmbte."DocumentoIdentidad"      != :DocumentoIdentidad  AND
Cmbte."EsVentaCorta" != 'S'
---- ANULADOS   
UNION ALL           
SELECT 
Cmbte."IdComprobante"                                   AS "IdMovimiento"
,concat('A',Cmbte."IdTipoComprobante")                  AS "TipoDocumento"              
,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"

,YEAR("FechaSistema")       ||'-'|| RIGHT('0' || 
 MONTH("FechaSistema"),2)   ||'-'|| RIGHT('0' || 
 DAYOFMONTH("FechaSistema"),2)                          AS "FechaDocumento"
, CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
    CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
    --ELSE Cmbte."AccionTipoOperacion"
    ELSE CASE Cmbte."AccionTipoOperacion" WHEN 'VENTA CORTA' THEN 'VENTA' ELSE Cmbte."AccionTipoOperacion" END
END                                                             AS "CompraVenta"
,Cmbte."IdTipoOperacion"                                AS "Operacion"
,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
    WHEN '' THEN Fac."CentroCosto"
    ELSE Cmbte."CodigoTipoProducto"                             
END                                         AS "Producto"
,Cmbte."IdClasificacionTipoInstrumento"                 AS "ClaseInstrumento"
,Cmbte."IdTipoInstrumento"                              AS "TipoInstrumento"
,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
,Cmbte."IdBolsa"                                        AS "Bolsa"
,Cmbte."DocumentoIdentidad"                             AS "DocumentoIdentidad"
,LEFT(CASE Per."Tipo" 
			WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
			ELSE Per."RazonSocial"
		END,60)                          AS "NombreCliente"
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
,Cmbte."IdMonedaNegociacion"							AS "MonedaNegociacion"
,'N'                                                    AS "EsContraparte"
,CASE 
        WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
                    THEN 'CP'
        WHEN IndCli."ValorIndicador" = 'S'  
                    THEN 'RE'
        ELSE    'NR'
      END                                               AS "Relacion",
Per."Tipo"                                              AS "TipoPersona",
''                                            			AS "TipoInstrumentoVenta",
''                                          			AS "TipoInstrumentoCompra",

"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", Cmbte."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                        AS "ClaseEmisor",
CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN '' ELSE Ins."IdEmisor" END AS "Emisor",
Emi."IdTipoEmisor"                                      AS "TipoEmisor",
Ins."Nemotecnico"                                       AS "Nemotecnico",
Ins."TipoBonoIRF"                                       AS "TipoBonoIRF",
TRIM(Cmbte."CodigoIFRS")                                AS "CodigoIFRS",
Ins."OrigenInstrumento"                                 AS "OrigenInstrumento",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                        WHEN 'VENTA'  THEN ''
                        END                             AS "IndicadorRelacionadoGrupo",
CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
      END )     
        WHEN 'COMPRA' THEN '' WHEN 'VENTA'  THEN 
            CASE Cmbte."IdBolsa"
                WHEN 'BVL' THEN 'C'
                WHEN 'FRD' THEN 'C'
                ELSE 'E'
            END 
        END                                             AS "Plaza",
Ins."PactoAndino"                   AS "PactoAndino",   
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
, COALESCE(Cmbte."FolioComprobante", 0) AS "FolioDTE"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, ''                                        AS "DocumentoIdentidadCAVALI"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE               'NO' 
  END   AS "EsArbitraje" 
,COALESCE(IndCli2."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
,area."Descripcion" AS "AreaCliente"
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa")

LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"               				AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" 								AS Per 
ON Per."IdPersona" =  Cli."IdPersona"

LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio"                 			AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli2
ON IndCli2."IdCliente" =  Cmbte."IdCliente" AND IndCli2."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"                             AS Ins 
ON Ins."IdInstrumento" = Cmbte."IdInstrumento" 

LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor"                                  AS Emi
ON Ins."IdEmisor" = Emi."IdEmisor"

LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker"                             AS MarkMak
                   On MarkMak."IdInstrumento" = Cmbte."IdInstrumento"
                  AND ((Cmbte."FechaComprobante" >= MarkMak."FechaInicio") AND (Cmbte."FechaComprobante" <= MarkMak."FechaFin"))
                  AND MarkMak."IdEstado" = 'ACT' and
				 ((length(Ins."IdInstrumento") > 0 OR Ins."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0))
        
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
                            END)
                            
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS Fac 
ON ( Cmbte."IdConcepto" = Fac."IdConcepto")

INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Movimiento" Mov
ON (Cmbte."IdTipoComprobante"   = Mov."IdTipoMovimiento"    AND
    Cmbte."IdComprobante"       = Mov."Numero")
INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Centralizacion" Cent
ON (Cent."IdCentralizacion"     = Mov."IdCentralizacion"    AND 
    Cent."FechaContable"        = Mov."Fecha")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS area ON Por."AreaCliente" = area."Codigo"
WHERE   Cmbte."IdEmpresa"       = :IdEmpresa AND
Cmbte."FechaSistema"            = :FechaContable AND 
Cmbte."IdEstadoComprobante"     = 'ANU' AND 
Cent."IdTipoCentralizacion"     = :IdTipoCentralizacion AND 
Cent."FechaContable"            = :FechaContable AND 
Cent."Estado"                   = 'V' AND
Cmbte."DocumentoIdentidad"      != :DocumentoIdentidad AND
Cmbte."EsVentaCorta" != 'S'