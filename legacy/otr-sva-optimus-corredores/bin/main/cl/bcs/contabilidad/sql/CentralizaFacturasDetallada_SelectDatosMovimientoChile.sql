SELECT 
	IFNULL( MovCmbte."IdMovimientoComprobante", Cmbte."IdComprobante")                     AS "IdMovimiento"
--	Cmbte."IdComprobante"								AS "IdMovimiento"
	,Cmbte."IdTipoComprobante"                              AS "TipoDocumento"
	,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"
	,YEAR(Cmbte."FechaSistema")       ||'-'|| RIGHT('0' || 
	 MONTH(Cmbte."FechaSistema"),2)   ||'-'|| RIGHT('0' || 
	 DAYOFMONTH(Cmbte."FechaSistema"),2)                          AS "FechaDocumento"
	,IFNULL( CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
	    CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
	    ELSE CASE Cmbte."AccionTipoOperacion" WHEN 'VENTA CORTA' THEN 'VENTA' ELSE Cmbte."AccionTipoOperacion" END
	END,'')                                                             AS "CompraVenta"
	,Cmbte."IdTipoOperacion"                                AS "Operacion"
	,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
	    WHEN '' THEN Fac."CentroCosto"
	    ELSE Cmbte."CodigoTipoProducto"                             
	END                                         			AS "Producto"
	,IFNULL(Cmbte."IdClasificacionTipoInstrumento",'')      AS "ClaseInstrumento"
	,IFNULL(MovCmbte."IdTipoInstrumento",'')                AS "TipoInstrumento"
	,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
	,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
	,IFNULL(Cmbte."IdBolsa",'')                                        AS "Bolsa"
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
	,'NO'                                                   AS "EsContraparte"
	,CASE 
	    WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
	                THEN 'CP'
	    WHEN IndCli."ValorIndicador" = 'S'  
	                THEN 'RE'
	    ELSE    'NR'
	  END                                                  	AS "Relacion",
	Cli."Tipo"                                              AS "TipoPersona",
	''                                           			AS "TipoInstrumentoVenta",
	''                                             			AS "TipoInstrumentoCompra",

"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", MovCmbte."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                        AS "ClaseEmisor",
CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN
CASE Cmbte."IdInstrumento" WHEN 0 THEN ''
ELSE COALESCE((SELECT  MOV."IdEmisor" FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV WHERE Cmbte."IdComprobante" = MOV."IdComprobante" AND Cmbte."IdClasificacionTipoInstrumento" = 'IF' AND Cmbte."IdInstrumento" != 0),'') END
ELSE IFNULL(Ins."IdEmisor",'') END                                 AS "Emisor",
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
 Cmbte."IdClasificacionTipoInstrumento"
,MovCmbte."IdTipoInstrumento"
,MovCmbte."NemotecnicoInstrumento"
,(SELECT max("IdEmisor") FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 WHERE Cmbte."IdComprobante" = "IdComprobante"))							AS "TipoEmisor",
ifnull(MovCmbte."NemotecnicoInstrumento",'')                       AS "Nemotecnico",
IFNULL(Ins."TipoBonoIRF",'')                                       AS "TipoBonoIRF",
TRIM(Cmbte."CodigoIFRS")                                AS "CodigoIFRS",
IFNULL(Ins."OrigenInstrumento",'')                                 AS "OrigenInstrumento",
IFNULL(CASE (CASE WHEN IFNULL(Cmbte."IdTipoComprobante",'') = 'NCE' OR IFNULL(Cmbte."IdTipoComprobante",'') = 'NCR' THEN
                        CASE IFNULL(Cmbte."AccionTipoOperacion",'')    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE IFNULL(Cmbte."AccionTipoOperacion",'')
                        END ) 
                        WHEN 'COMPRA' THEN Ins."RelacionadoGrupo"
                        WHEN 'VENTA'  THEN ''
                        END,'')                             AS "IndicadorRelacionadoGrupo",
IFNULL(CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
      END )     
        WHEN 'COMPRA' THEN '' WHEN 'VENTA'  THEN 
            CASE Cmbte."IdBolsa"
                WHEN 'BVL' THEN 'C'
                WHEN 'FRD' THEN 'C'
                ELSE 'E'
            END 
        END ,'')                                            AS "Plaza",
IFNULL(Ins."PactoAndino",'')                   AS "PactoAndino",   
IFNULL(CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN ''
                        WHEN 'VENTA'  THEN Ins."FormadorMercado"
END,'')                                                     AS "FormadorMercado",
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
    ELSE IFNULL(CenCos."Business",'')                          
END                                         AS "OpCentros"
,Cmbte."IdEstadoComprobante"                AS "EstadoComprobante"
, CASE Cmbte."IdTipoProducto"
        WHEN '' THEN 'SI'
        ELSE 'NO'   
        END                                 AS "EsOtrosConceptos"
, Cmbte."FolioComprobante"                  AS "FolioPoliza"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, ''                                        AS "DocumentoIdentidadCAVALI"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE               'NO' 
  END   AS "EsArbitraje"  
  
,IFNULL(Cor."DocumentoIdentidad",'')										AS "CorredoraContraparte"
,IFNULL(Cor."NombreCompleto",'') 											AS "NombreCorredoraContraparte" 
 
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
    LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 				AS MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"	Cor 
 ON ( MovCmbte."IdCorredoraContraparte" = Cor."IdCorredora")
	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"               AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                 AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'

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
WHERE Cmbte."IdEmpresa" = :IdEmpresa AND
Cmbte."FechaSistema" = :FechaContable AND 
Cmbte."IdEstadoComprobante" <> 'ANU' AND
Cmbte."DocumentoIdentidad"      != :DocumentoIdentidad
--AND Cmbte."IdComprobante" = 2436
---- ANULADOS   
UNION ALL           
SELECT 
IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")                     AS "IdMovimiento"
--Cmbte."IdComprobante"								AS "IdMovimiento"
,concat('A',Cmbte."IdTipoComprobante")                  AS "TipoDocumento"              
,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"

,YEAR(Cmbte."FechaSistema")       ||'-'|| RIGHT('0' || 
 MONTH(Cmbte."FechaSistema"),2)   ||'-'|| RIGHT('0' || 
 DAYOFMONTH(Cmbte."FechaSistema"),2)                          AS "FechaDocumento"
, ifnull(CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
    CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
    --ELSE Cmbte."AccionTipoOperacion"
    ELSE CASE Cmbte."AccionTipoOperacion" WHEN 'VENTA CORTA' THEN 'VENTA' ELSE Cmbte."AccionTipoOperacion" END
END,'')                                                             AS "CompraVenta"
,Cmbte."IdTipoOperacion"                                AS "Operacion"
,CASE IFNULL(Cmbte."CodigoTipoProducto",'')
    WHEN '' THEN Fac."CentroCosto"
    ELSE Cmbte."CodigoTipoProducto"                             
END                                         			AS "Producto"
,IFNULL(Cmbte."IdClasificacionTipoInstrumento",'')      AS "ClaseInstrumento"
,IFNULL(MovCmbte."IdTipoInstrumento",'')                AS "TipoInstrumento"
,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
,IFNULL(Cmbte."IdBolsa",'')                                        AS "Bolsa"
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
,'NO'                                                   AS "EsContraparte"
,CASE 
        WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
                    THEN 'CP'
        WHEN IndCli."ValorIndicador" = 'S'  
                    THEN 'RE'
        ELSE    'NR'
      END                                               AS "Relacion",
Cli."Tipo"                                              AS "TipoPersona",
''                                            			AS "TipoInstrumentoVenta",
''                                          			AS "TipoInstrumentoCompra",

"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", MovCmbte."NemotecnicoInstrumento", Ins."IdTipoReajuste")
                                                        AS "ClaseEmisor",
CASE Cmbte."IdClasificacionTipoInstrumento" WHEN  'IF' THEN
CASE Cmbte."IdInstrumento" WHEN 0 THEN ''
ELSE COALESCE((SELECT  MOV."IdEmisor" FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV WHERE Cmbte."IdComprobante" = MOV."IdComprobante" AND Cmbte."IdClasificacionTipoInstrumento" = 'IF' AND Cmbte."IdInstrumento" != 0),'') END
ELSE IFNULL(Ins."IdEmisor",'') END                                 AS "Emisor",

		"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
			 Cmbte."IdClasificacionTipoInstrumento"
			,MovCmbte."IdTipoInstrumento"
			,MovCmbte."NemotecnicoInstrumento"
			,(SELECT max("IdEmisor") FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
			WHERE Cmbte."IdComprobante" = "IdComprobante"))							AS "TipoEmisor",


IFNULL(MovCmbte."NemotecnicoInstrumento",'')                       AS "Nemotecnico",
IFNULL(Ins."TipoBonoIRF",'')                                       AS "TipoBonoIRF",
TRIM(Cmbte."CodigoIFRS")                                AS "CodigoIFRS",
IFNULL(Ins."OrigenInstrumento",'')                                 AS "OrigenInstrumento",
IFNULL(CASE (CASE WHEN IFNULL(Cmbte."IdTipoComprobante",'') = 'NCE' OR IFNULL(Cmbte."IdTipoComprobante",'') = 'NCR' THEN
                        CASE IFNULL(Cmbte."AccionTipoOperacion",'')    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE IFNULL(Cmbte."AccionTipoOperacion",'')
                        END ) 
                        WHEN 'COMPRA' THEN IFNULL(Ins."RelacionadoGrupo",'')
                        WHEN 'VENTA'  THEN ''
                        END,'')                             AS "IndicadorRelacionadoGrupo",
IFNULL(CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
        CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
        ELSE Cmbte."AccionTipoOperacion"
      END )     
        WHEN 'COMPRA' THEN '' WHEN 'VENTA'  THEN 
            CASE Cmbte."IdBolsa"
                WHEN 'BVL' THEN 'C'
                WHEN 'FRD' THEN 'C'
                ELSE 'E'
            END 
        END,'')                                             AS "Plaza",
IFNULL(Ins."PactoAndino",'')                   AS "PactoAndino",   
IFNULL(CASE (CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
                        CASE Cmbte."AccionTipoOperacion"    WHEN 'COMPRA' THEN 'VENTA' WHEN 'VENTA'  THEN 'COMPRA' END 
                        ELSE Cmbte."AccionTipoOperacion"
                        END ) 
                        WHEN 'COMPRA' THEN ''
                        WHEN 'VENTA'  THEN Ins."FormadorMercado"
END,'')                                                     AS "FormadorMercado",
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
    ELSE IFNULL(CenCos."Business",'')                          
END                                         AS "OpCentros"
,Cmbte."IdEstadoComprobante"                AS "EstadoComprobante"
, CASE Cmbte."IdTipoProducto"
        WHEN '' THEN 'SI'
        ELSE 'NO'   
        END                                 AS "EsOtrosConceptos"
, Cmbte."FolioComprobante"                  AS "FolioPoliza"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, ''                                        AS "DocumentoIdentidadCAVALI"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END    AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE               'NO' 
  END   AS "EsArbitraje"
,IFNULL(Cor."DocumentoIdentidad",'')										AS "CorredoraContraparte"
,IFNULL(Cor."NombreCompleto",'') 											AS "NombreCorredoraContraparte" 

    
FROM        "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"              AS Cmbte
    LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 				AS MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"	Cor 
 ON ( MovCmbte."IdCorredoraContraparte" = Cor."IdCorredora")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                 AS Emp
ON (Emp."IdEmpresa" = Cmbte."IdEmpresa")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"               AS Cli 
ON Cli."IdCliente" =  Cmbte."IdCliente"

LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                 AS Por 
ON Por."IdPortafolio" = Cmbte."IdPortafolio" 

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"         AS Con 
ON Con."IdConcepto" = Cmbte."IdConcepto"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"             AS Eje 
ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"     AS Indemp
ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'

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
WHERE   Cmbte."IdEmpresa"       = :IdEmpresa AND
Cmbte."FechaSistema"            = :FechaContable AND 
Cmbte."IdEstadoComprobante"     = 'ANU' AND 
Cent."IdTipoCentralizacion"     = :IdTipoCentralizacion AND 
Cent."FechaContable"            = :FechaContable AND 
Cent."Estado"                   = 'V' AND
Cmbte."DocumentoIdentidad"      != :DocumentoIdentidad
--AND Cmbte."IdComprobante" = 2436