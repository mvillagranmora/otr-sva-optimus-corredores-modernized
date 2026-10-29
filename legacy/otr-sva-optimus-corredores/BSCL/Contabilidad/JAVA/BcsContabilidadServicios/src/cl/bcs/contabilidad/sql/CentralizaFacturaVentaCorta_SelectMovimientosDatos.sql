SELECT
Cmbte."IdComprobante"                                  	AS "IdMovimiento"
,Cmbte."IdTipoComprobante"                              AS "TipoDocumento"
,COALESCE(Cmbte."IdComprobante",0)                      AS "Folio"
,YEAR("FechaSistema") ||'-'|| RIGHT('0' || MONTH("FechaSistema"),2)  ||'-'|| RIGHT('0' || DAYOFMONTH("FechaSistema"),2) AS "FechaDocumento",
CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
    CASE Cmbte."AccionTipoOperacion" 
    	WHEN 'COMPRA CORTA' THEN CASE WHEN CMBTE."EsVentaCorta" = 'S' THEN 'CC' ELSE 'C' END
    	WHEN 'VENTA CORTA'  THEN CASE WHEN CMBTE."EsVentaCorta" = 'S' THEN 'VC' ELSE 'V' END
    	END 
    ELSE 	
    	CASE Cmbte."AccionTipoOperacion" 
    	WHEN 'COMPRA CORTA' THEN CASE WHEN CMBTE."EsVentaCorta" = 'S' THEN 'CC' ELSE 'C' END
    	WHEN 'VENTA CORTA'  THEN CASE WHEN CMBTE."EsVentaCorta" = 'S' THEN 'VC' ELSE 'V' END
    END
END AS "CompraVenta"
,Cmbte."IdTipoOperacion"                                AS "Operacion"
,IFNULL(Cmbte."IdTipoProducto",'')           			      AS "Producto"
,Cmbte."IdClasificacionTipoInstrumento"                 AS "ClaseInstrumento"
,Cmbte."IdTipoInstrumento"                              AS "TipoInstrumento"
,Cmbte."IdCondicionLiquidacion"                         AS "CondicionLiquidacion"
,Cmbte."IdMonedaComprobante"                            AS "MonedaComprobante"
,Cmbte."IdBolsa"                                        AS "Bolsa"
,Cmbte."DocumentoIdentidad"                             AS "DocumentoIdentidad"
,LEFT(CASE Per."Tipo" 
			WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
			ELSE Per."RazonSocial"
		END,60) AS "NombreCliente"
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
    WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad" THEN 'CP'
    WHEN IndCli."ValorIndicador" = 'S'  THEN 'RE'
    ELSE 'NR'
  END                                                  	AS "Relacion",
Per."Tipo"                                              AS "TipoPersona",
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
Cmbte."IdTipoInstrumento", Cmbte."NemotecnicoInstrumento", Ins."IdTipoReajuste") AS "ClaseEmisor",
Ins."IdEmisor" 											                    AS "Emisor",
Emi."IdTipoEmisor"                                      AS "TipoEmisor",
Ins."Nemotecnico"                                       AS "Nemotecnico",
Ins."TipoBonoIRF"                                       AS "TipoBonoIRF",
TRIM(Cmbte."CodigoIFRS")                                AS "CodigoIFRS",
Ins."OrigenInstrumento"                                 AS "OrigenInstrumento",
Cmbte."CodigoTipoComprobante"             AS "CodigoTipoComprobante"
,Cmbte."FechaLiquidacion"               AS "FechaLiquidacion"
,Cmbte."IdTipoComprobante"              AS "TipoComprobante"
,Cmbte."FechaComprobante"               AS "FechaComprobante"
,Cmbte."IdEstadoComprobante"                AS "EstadoComprobante"
, CASE Cmbte."IdTipoProducto" WHEN '' THEN 'SI' ELSE 'NO' END AS "EsOtrosConceptos"
, Cmbte."FolioComprobante"                  AS "FolioPoliza"
, Cmbte."EsAPlazo"                          AS "EsAPlazo"
, CASE Cmbte."EsVentaCorta" WHEN 'S' THEN 'SI' ELSE 'NO' END AS "EsVentaCorta"
, CASE Cmbte."NemotecnicoInstrumento" 
    WHEN 'ARBDIS' THEN 'SI' 
    WHEN 'ARBINT' THEN 'SI'               
    ELSE 'NO' 
  END AS "EsArbitraje"
FROM  "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"   AS Cmbte
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS Emp ON Emp."IdEmpresa" = Cmbte."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON Cli."IdCliente" =  Cmbte."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" =  Cli."IdPersona"
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" AS Por ON Por."IdPortafolio" = Cmbte."IdPortafolio" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS Con ON Con."IdConcepto" = Cmbte."IdConcepto"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS Eje ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS Indemp ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IndCli ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Ins ON Ins."IdInstrumento" = Cmbte."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS Emi ON Ins."IdEmisor" = Emi."IdEmisor"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS Fac ON ( Cmbte."IdConcepto" = Fac."IdConcepto")  
WHERE Cmbte."IdEmpresa" = :IdEmpresa AND
Cmbte."FechaSistema" = :FechaContable AND 
Cmbte."IdEstadoComprobante" <> 'ANU' AND
Cmbte."DocumentoIdentidad" != :DocumentoIdentidad  AND 
Cmbte."EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento;