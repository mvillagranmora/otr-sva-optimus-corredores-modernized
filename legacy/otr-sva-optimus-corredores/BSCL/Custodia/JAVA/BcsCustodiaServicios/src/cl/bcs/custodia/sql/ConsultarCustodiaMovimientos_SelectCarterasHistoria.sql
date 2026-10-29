SELECT
   USRCREA."CodigoUsuario" AS "UsuarioIngresoOperacion"
   ,USRCERT."CodigoUsuario" AS "UsuarioCertificaOperacion"
   ,COALESCE(MT."FechaCertificacionRegistro", '0001-01-01') AS "FechaCertificacion"
   ,car."IdCartera" as "IdCartera"
   ,car."FechaIngresoRegistro" as "FechaRegistro"
   ,car."INGFechaOperacion" as "FechaOperacion"
   ,car."INGFechaLiquidacion" as "FechaLiquidacion"
   ,car."INGIdTipoOperacion" AS "IdTipoOperacion"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(TipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
   		ELSE TipOper."Nombre" 
   	END AS "TipoOperacion"
   ,car."IdCliente" as "IdCliente"
   ,car."IdPortafolio" as "IdPortafolio"
   ,car."INGIdConcepto" as "IdConcepto"
   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   ,COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") AS "IdTipoInstrumento"
   ,CASE
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" = 'PAC' THEN SBY."IdInstrumento"
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."IdInstrumento"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."IdInstrumento" 
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."IdInstrumento"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."IdInstrumento"
   		ELSE car."IdInstrumento"
   	END AS "IdInstrumento"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."NemoSVS"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."Nemotecnico"
   		ELSE car."Nemotecnico"
	END AS "Nemotecnico"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."IdEmisor"
   		ELSE car."IdEmisor"
   	END AS "IdEmisor"
   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
   ,car."IdTipoReajuste" as "IdTipoReajuste"
   ,CASE
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" = 'PAC' THEN SBY."NemoSVS"
   		ELSE car."NemoSVS"
   	END AS "NemoSVS"
   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
   ,car."IdBoveda" as "IdBoveda"
   ,car."INGPrecioCompra" as "Precio"
   ,car."INGTasaCompra" as "Tasa"
   ,car."INGMontoATasaCompraActual" AS "Monto"
   ,car."INGPrecioMercadoActual" AS "PrecioMercado"
   ,car."INGTasaMercadoActual" AS "TasaMercado"
   ,car."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaLocal"
   ,car."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
   ,car."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
   ,car."ValorRescate" as "ValorRescate"
   ,cast(0 as DECIMAL(26,8)) as "Saldo"
   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "Cantidad"
   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "CantidadNeg"
   ,case when car."IdClasificacionTipoInstrumento" IN ('RF','IF') then car."INGTasaCompra" else car."INGPrecioCompra" end as "PrecioTir"
   ,car."FolioCartera"
   ,car."FolioComprobante"
   ,car."FechaComprobante"
   ,car."IdTipoComprobante"
   ,car."ISIN"
   ,car."IdCuentaCustodioPortafolio"
   ,'' as "NumeroCuentaBoveda"
   ,'CUS' as "IdTipoCustodia"
   ,'CUSTODIA' as "NombreTipoCustodia"
   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
   ,CAR."IdEstado" as "IdEstado" --AE 2
   ,Concepto."Nombre" AS "NombreConcepto"
   ,Concepto."TipoConcepto" AS "TipoConcepto"
   ,CAR."INGIdTipoOperacion" AS "INGIdTipoOperacion"
   ,CAR."EGRFechaOperacion"
   ,CAR."EGRFechaLiquidacion"
   ,CAR."EGRIdTipoOperacion"
   ,CAR."EGRIdConcepto"
   ,CAR."EGRPrecio"
   ,CAR."EGRTasa"
   ,CAR."EGRMonto"
   ,CAR."EGRPrecioMercado"
   ,CAR."EGRTasaMercado"
   ,CAR."EGRMontoATasaMercado"
   ,CAR."EGRMontoMercadoMonedaInstrumento"
   ,CAR."EGRMontoMercadoMonedaAdicional"
   ,CAR."EGRValorRescate"
   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' then CAR."EGRValorRescate" ELSE CAR."EGRCantidad" END as "EGRCantidad"
   ,CAST((CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' then CAR."EGRValorRescate" ELSE CAR."EGRCantidad" END*-1) as DECIMAL(26,8)) as "EGRCantidadNeg"
   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN CAR."EGRTasa" ELSE CAR."EGRPrecio" END as "EGRPrecioTir"   
   ,CAR."EGRFolioComprobante"
   ,CAR."EGRFechaComprobante"
   ,CAR."EGRIdTipoComprobante"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(EGRTipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
   		ELSE EGRTipOper."Nombre" 
   	END AS "EGRTipoOperacion"
   ,EGRConcepto."Nombre" AS "EGRNombreConcepto"
   ,cli."RazonSocial" as "NombreCliente"
   ,cli."DocumentoIdentidad" as "DocumentoIdentidad"
   ,PORTAFOLIO."CodigoPortafolio"
   ,CASE WHEN COALESCE(CCP."IdCuentaCustodioPortafolio",0) > 0 THEN CCP."IdBoveda" ELSE CAR."IdBoveda" END "IdBoveda"
   ,CAR."IdMonedaPrincipal"
   ,CAR."IdMonedaSecundaria"
   ,CAR."ValorTipoCambioCierre"
   ,CASE
		WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaSecundaria" 
		ELSE CAR."INGMontoMonedaSecundaria" END	 AS "MontoMonedaSecundaria"
   ,CAR."IdOperacion"
   ,CAR."FechaVencimientoOperacion" AS "FechaVencimiento"
   ,SUM(case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoIngreso"
   ,SUM(CAST((CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' then CAR."EGRValorRescate" ELSE CAR."EGRCantidad" END*-1) as DECIMAL(26,8))) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoRetiro"
   ,CAR."EGRSpread" AS "Spread"
   ,CAR."EGRSpreadMercado" AS "SpreadMercado"
   ,COALESCE(CAR."IdCarga",0) AS "IdCarga"
   ,CAR."EGRMontoARecibirAnticipo"
   ,MT."IdEstado" as "IdEstadoIdMovimientoCarteraEnTransito"
   ,COALESCE(CAR."EGRIdMovimientoCarteraEnTransito", 0) AS "EGRIdMovimientoCarteraEnTransito"
   ,COALESCE(UsrEgrOpe."CodigoUsuario",'') AS "UsuarioEgresoOperacion"
   ,COALESCE(UsrEgrCert."CodigoUsuario",'') AS "UsuarioEgresoCertificacion"   
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Custodia.BD::CUS.ConsultarCustodiaMovimientos_Historia" HST ON HST."FolioCartera" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (CAR."FolioCartera" = ULTSUB."FolioCarteraRelacionada")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SBY ON SBY."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = CAR."INGIdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As EGRTipOper ON EGRTipOper."IdTipoOperacion" = CAR."EGRIdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CAR."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCP ON CCP."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON Concepto."IdConcepto" = CAR."INGIdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as EGRConcepto ON EGRConcepto."IdConcepto" = CAR."EGRIdConcepto"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORTAFOLIO."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO ON TipoInstrumento."IdTipoInstrumento" = CAR."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USRCREA ON USRCREA."IdUsuario" = CAR."IdUsuario"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT ON MT."IdMovimientoCarteraEnTransito" = CAR."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT2 ON MT2."IdMovimientoCarteraEnTransito" = CAR."EGRIdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USRCERT ON USRCERT."IdUsuario" = MT."IdUsuarioCertificacion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = CAR."EGRFolioComprobante" AND CMBTE."IdCliente" = CAR."IdCliente" AND CAR."EGRFolioComprobante"!= 0
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrOpe ON UsrEgrOpe."IdUsuario" = CAR."EGRIdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrCert ON UsrEgrCert."IdUsuario" = MT2."IdUsuarioCertificacion"
WHERE CAR."IdEmpresa" = :IdEmpresa
AND ((:TipoOperacion = 'INGRESO' AND CAR."INGFechaOperacion" >= :FechaInicio AND CAR."INGFechaOperacion" <= :FechaTermino) OR
	 (:TipoOperacion = 'RETIRO' AND CAR."EGRFechaOperacion" >= :FechaInicio AND CAR."EGRFechaOperacion" <= :FechaTermino AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N') OR
	 (:TipoOperacion = ''))
AND CAR."INGIdConcepto" = CASE WHEN :IdConcepto <> '' THEN :IdConcepto ELSE CAR."INGIdConcepto" END
AND CAR."IdTipoInstrumento" != 'SIM'
AND CAR."IdClasificacionTipoInstrumento" != 'DV'
AND car."IdEstado" != 'ANU'
AND  COALESCE (CMBTE."IdEstadoComprobante",'') != 'ANU'
UNION ALL
SELECT 
USRCREA."CodigoUsuario" AS "UsuarioIngresoOperacion",
USRCERT."CodigoUsuario" AS "UsuarioCertificaOperacion",
COALESCE(MT."FechaCertificacionRegistro", '0001-01-01') AS "FechaCertificacion",
SUB."IdCarteraRetirada" as "IdCartera",
SUB."FechaIngresoRegistro" as "FechaRegistro",
SUB."INGFechaOperacion" as "FechaOperacion",
CAR."FechaLiquidacion" as "FechaLiquidacion",
car."INGIdTipoOperacion" AS "IdTipoOperacion",
CASE
   		WHEN SUB."IdTipoInstrumento" = 'PRT' AND CAR."IdTipoPrestamo" = 'PRTVTC' THEN SUB."Nemotecnico"
   		WHEN SUB."IdTipoInstrumento" = 'PRT' THEN CONCAT(TipOper."Nombre" , CONCAT(' ', SUB."Nemotecnico")) 
   		ELSE TipOper."Nombre" 
   	END AS "TipoOperacion",
OPE."IdCliente",
CAR."IdPortafolio" as "IdPortafolio",
Concepto."IdConcepto" as "IdConcepto",
SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento",
COALESCE(sby."IdTipoInstrumento", SUB."IdTipoInstrumento") AS "IdTipoInstrumento",
CASE
   		WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."IdTipoInstrumento" = 'PAC' THEN SBY."IdInstrumento"
   		WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."IdTipoInstrumento" <> 'PAC' THEN SUB."IdInstrumento"
   		WHEN SUB."IdTipoInstrumento" = 'PRT' THEN SBY."IdInstrumento" 
   		WHEN SUB."IdTipoInstrumento" = 'SIM' THEN SBY."IdInstrumento"
   		WHEN SUB."IdTipoInstrumento" = 'EMP' THEN SBY."IdInstrumento"
   		ELSE SUB."IdInstrumento"
   	END AS "IdInstrumento",
   	CASE
   		WHEN SUB."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."IdTipoInstrumento" <> 'PAC' THEN SUB."NemoSVS"
   		WHEN SUB."IdTipoInstrumento" = 'PRT' THEN SBY."Nemotecnico"
   		WHEN SUB."IdTipoInstrumento" = 'SIM' THEN SBY."Nemotecnico"
   		WHEN SUB."IdTipoInstrumento" = 'EMP' THEN SBY."Nemotecnico"
   		ELSE SUB."Nemotecnico"
	END AS "Nemotecnico",
	CASE
   		WHEN SUB."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."IdTipoInstrumento" <> 'PAC' THEN SUB."IdEmisor"
   		WHEN SUB."IdTipoInstrumento" = 'PRT' THEN SBY."IdEmisor"
   		WHEN SUB."IdTipoInstrumento" = 'SIM' THEN SBY."IdEmisor"
   		WHEN SUB."IdTipoInstrumento" = 'EMP' THEN SBY."IdEmisor"
   		ELSE SUB."IdEmisor"
   	END AS "IdEmisor",
   	SUB."FechaVencimientoInstrumento" as "FechaVencimientoInstr",
   	SUB."IdTipoReajuste" as "IdTipoReajuste",
   	CASE
   		WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."IdTipoInstrumento" = 'PAC' THEN SBY."NemoSVS"
   		ELSE SUB."NemoSVS"
   	END AS "NemoSVS",
   	SUB."IdMonedaInstrumento" as "IdMonedaInstr",
   	RES."IdBoveda" as "IdBoveda",
   	CAR."INGPrecioCompra" as "Precio",
   	car."INGTasaCompra" as "Tasa",
   	SUB."INGMontoATasaCompraActual" AS "Monto",
   	SUB."INGPrecioMercadoActual" AS "PrecioMercado",
   	SUB."INGTasaMercadoActual" AS "TasaMercado",
   	SUB."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaLocal",
   	SUB."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
   	,SUB."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional",
   	SUB."ValorRescate" as "ValorRescate",
   	cast(0 as DECIMAL(26,8)) as "Saldo",
   	case when SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."INGValorRescate" else SUB."INGCantidad" end as "Cantidad",
   	case when SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."INGValorRescate" else SUB."INGCantidad" end as "CantidadNeg"
   ,case when SUB."IdClasificacionTipoInstrumento" IN ('RF','IF') then CAR."INGTasaCompra" else CAR."INGPrecioCompra" end as "PrecioTir"
 , SUB."FolioCarteraRelacionada" 
 ,car."FolioComprobante"
 ,car."FechaComprobante"
  ,car."IdTipoComprobante"
   ,car."ISIN"
   ,car."IdCuentaCustodioPortafolio"
    ,'' as "NumeroCuentaBoveda"
   ,'CUS' as "IdTipoCustodia"
   ,'NOMINATIVA' as "NombreTipoCustodia"
   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
   ,SUB."IdEstado" as "IdEstado" 
   ,Concepto."Nombre" AS "NombreConcepto"
    ,Concepto."TipoConcepto" AS "TipoConcepto"
    ,CAR."INGIdTipoOperacion" AS "INGIdTipoOperacion"
     ,CAR."EGRFechaOperacion"
   ,CAR."EGRFechaLiquidacion"
   ,CAR."EGRIdTipoOperacion"
   ,CAR."EGRIdConcepto"
   ,CAR."EGRPrecio"
   ,CAR."EGRTasa"
   ,CAR."EGRMonto"
   ,CAR."EGRPrecioMercado"
   ,CAR."EGRTasaMercado"
   ,CAR."EGRMontoATasaMercado"
   ,CAR."EGRMontoMercadoMonedaInstrumento"
   ,CAR."EGRMontoMercadoMonedaAdicional"
   ,CAR."EGRValorRescate"
      ,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."EGRValorRescate" ELSE SUB."EGRCantidad" END as "EGRCantidad"
   ,CAST((CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."EGRValorRescate" ELSE SUB."EGRCantidad" END*-1) as DECIMAL(26,8)) as "EGRCantidadNeg"
   ,CASE WHEN SUB."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN SUB."EGRTasa" ELSE SUB."EGRPrecio" END as "EGRPrecioTir"   
   ,SUB."EGRFolioComprobante"
,SUB."EGRFechaComprobante"
   ,SUB."EGRIdTipoComprobante"
    ,CASE
   		WHEN SUB."IdTipoInstrumento" = 'PRT' AND CAR."IdTipoPrestamo" = 'PRTVTC' THEN SUB."Nemotecnico"
   		WHEN SUB."IdTipoInstrumento" = 'PRT' THEN CONCAT(EGRTipOper."Nombre" , CONCAT(' ', SUB."Nemotecnico")) 
   		ELSE EGRTipOper."Nombre" 
   	END AS "EGRTipoOperacion"
   ,EGRConcepto."Nombre" AS "EGRNombreConcepto"
   ,cli."NombreCompleto" as "NombreCliente"
      ,cli."DocumentoIdentidad" as "DocumentoIdentidad"
         ,PORTAFOLIO."CodigoPortafolio"
   ,CASE WHEN COALESCE(CCP."IdCuentaCustodioPortafolio",0) > 0 THEN CCP."IdBoveda" ELSE RES."IdBoveda" END "IdBoveda"
,CAR."IdMonedaPrincipal"
   ,CAR."IdMonedaSecundaria"
   ,CAR."ValorTipoCambioCierre"
    ,CASE
		WHEN SUB."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaSecundaria" 
		ELSE CAR."INGMontoMonedaSecundaria" END	 AS "MontoMonedaSecundaria"
		,CAR."IdOperacion"
   ,CAR."FechaVencimientoOperacion" AS "FechaVencimiento"
      ,SUM(case when SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."INGValorRescate" else SUB."INGCantidad" end) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoIngreso"
   ,SUM(CAST((CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' AND SUB."EsFungible" = 'S' then SUB."EGRValorRescate" ELSE SUB."EGRCantidad" END*-1) as DECIMAL(26,8))) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoRetiro"
  ,CAR."EGRSpread" AS "Spread"
   ,CAR."EGRSpreadMercado" AS "SpreadMercado"
   ,COALESCE(CAR."IdCarga",0) AS "IdCarga"
   ,CAR."EGRMontoARecibirAnticipo"
   ,MT2."IdEstado" as "IdEstadoIdMovimientoCarteraEnTransito"
   ,COALESCE(CAR."EGRIdMovimientoCarteraEnTransito", 0) AS "EGRIdMovimientoCarteraEnTransito"
   ,COALESCE(UsrEgrOpe."CodigoUsuario",'') AS "UsuarioEgresoOperacion"
   ,COALESCE(UsrEgrCert."CodigoUsuario",'') AS "UsuarioEgresoCertificacion"   
FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE 
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES
ON OPE."IdOperacion" = RES."IdOperacion"
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
ON SUB."FolioSubyacente" = RES."FolioSubyacente"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
ON CAR."IdOperacion" = RES."IdOperacion" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USRCREA ON USRCREA."IdUsuario" = OPE."IdUsuarioCreacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT ON MT."IdMovimientoCarteraEnTransito" = SUB."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT2 ON MT2."IdMovimientoCarteraEnTransito" = CAR."EGRIdMovimientoCarteraEnTransito"
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USRCERT ON USRCERT."IdUsuario" = MT."IdUsuarioCertificacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = CAR."INGIdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON Concepto."IdConcepto" = CAR."IdConcepto"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SBY ON SBY."FolioCarteraRelacionada" = SUB."FolioCarteraRelacionada" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CAR."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORTAFOLIO."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As EGRTipOper ON EGRTipOper."IdTipoOperacion" = SUB."EGRIdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as EGRConcepto ON EGRConcepto."IdConcepto" = CAR."EGRIdConcepto"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = OPE."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCP ON CCP."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrOpe ON UsrEgrOpe."IdUsuario" = CAR."EGRIdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrCert ON UsrEgrCert."IdUsuario" = MT2."IdUsuarioCertificacion"
WHERE OPE."AfectaCustodia" = 'N'
AND OPE."IdCliente" = :IdCliente
AND CAR."IdTipoOperacion" IN ('VENPAC')
AND CAR."IdEmpresa" = :IdEmpresa
AND ((:TipoOperacion = 'INGRESO' AND CAR."INGFechaOperacion" >= :FechaInicio AND CAR."INGFechaOperacion" <= :FechaTermino) OR
	 (:TipoOperacion = 'RETIRO' AND CAR."EGRFechaOperacion" >= :FechaInicio AND CAR."EGRFechaOperacion" <= :FechaTermino AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N') OR
	 (:TipoOperacion = ''))
AND CAR."INGIdConcepto" = CASE WHEN :IdConcepto <> '' THEN :IdConcepto ELSE CAR."INGIdConcepto" END
AND CAR."IdTipoInstrumento" != 'SIM'
AND CAR."IdClasificacionTipoInstrumento" != 'DV'
AND ( :IdClasificacionTipoInstrumento IN ('IF', 'RF') OR :IdClasificacionTipoInstrumento = '' )
AND OPE."IdEstado" != 'ANU'
UNION ALL
SELECT 
 USRCREA."CodigoUsuario" AS "UsuarioIngresoOperacion"
     ,USRCERT."CodigoUsuario" AS "UsuarioCertificaOperacion"
   ,COALESCE(MT."FechaCertificacionRegistro", '0001-01-01') AS "FechaCertificacion"
   ,car."IdCartera" as "IdCartera"
   ,car."FechaIngresoRegistro" as "FechaRegistro"
   ,car."INGFechaOperacion" as "FechaOperacion"
   ,car."INGFechaLiquidacion" as "FechaLiquidacion"
   ,car."INGIdTipoOperacion" AS "IdTipoOperacion"
   ,UPPER(tope."NombreEvento") AS "TipoOperacion"
   ,car."IdCliente" as "IdCliente"
   ,car."IdPortafolio" as "IdPortafolio"
   ,car."INGIdConcepto" as "IdConcepto"
   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   ,COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") AS "IdTipoInstrumento"
   ,CASE
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" = 'PAC' THEN SBY."IdInstrumento"
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."IdInstrumento"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."IdInstrumento" 
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."IdInstrumento"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."IdInstrumento"
   		ELSE car."IdInstrumento"
   	END AS "IdInstrumento"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."NemoSVS"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."Nemotecnico"
   		ELSE car."Nemotecnico"
	END AS "Nemotecnico"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PAC' THEN ''
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN SBY."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'SIM' THEN SBY."IdEmisor"
   		WHEN car."IdTipoInstrumento" = 'EMP' THEN SBY."IdEmisor"
   		ELSE car."IdEmisor"
   	END AS "IdEmisor"
   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
   ,car."IdTipoReajuste" as "IdTipoReajuste"
   ,CASE
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" = 'PAC' THEN SBY."NemoSVS"
   		ELSE car."NemoSVS"
   	END AS "NemoSVS"
   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
   ,car."IdBoveda" as "IdBoveda"
   ,car."INGPrecioCompra" as "Precio"
   ,car."INGTasaCompra" as "Tasa"
   ,car."INGMontoATasaCompraActual" AS "Monto"
   ,car."INGPrecioMercadoActual" AS "PrecioMercado"
   ,car."INGTasaMercadoActual" AS "TasaMercado"
   ,car."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaLocal"
   ,car."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
   ,car."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
   ,car."ValorRescate" as "ValorRescate"
   ,cast(0 as DECIMAL(26,8)) as "Saldo"
   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "Cantidad"
   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "CantidadNeg"
   ,case when car."IdClasificacionTipoInstrumento" IN ('RF','IF') then car."INGTasaCompra" else car."INGPrecioCompra" end as "PrecioTir"
   ,car."FolioCartera"
   ,car."FolioComprobante"
   ,car."FechaComprobante"
   ,car."IdTipoComprobante"
   ,car."ISIN"
   ,car."IdCuentaCustodioPortafolio"
   ,'' as "NumeroCuentaBoveda"
   ,'CUS' as "IdTipoCustodia"
   ,'NOMINATIVA' as "NombreTipoCustodia"
   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
   ,CAR."IdEstado" as "IdEstado" --AE 2
   ,Concepto."Nombre" AS "NombreConcepto"
   ,Concepto."TipoConcepto" AS "TipoConcepto"
   ,CAR."INGIdTipoOperacion" AS "INGIdTipoOperacion"
   ,CAR."EGRFechaOperacion"
   ,CAR."EGRFechaLiquidacion"
   ,CAR."EGRIdTipoOperacion"
   ,CAR."EGRIdConcepto"
   ,CAR."EGRPrecio"
   ,CAR."EGRTasa"
   ,CAR."EGRMonto"
   ,CAR."EGRPrecioMercado"
   ,CAR."EGRTasaMercado"
   ,CAR."EGRMontoATasaMercado"
   ,CAR."EGRMontoMercadoMonedaInstrumento"
   ,CAR."EGRMontoMercadoMonedaAdicional"
   ,CAR."EGRValorRescate"
   ,CAR."EGRValorRescate" as "EGRCantidad"
   ,CAST((CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' then CAR."EGRValorRescate" ELSE CAR."EGRCantidad" END*-1) as DECIMAL(26,8)) as "EGRCantidadNeg"
   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN CAR."EGRTasa" ELSE CAR."EGRPrecio" END as "EGRPrecioTir"   
   ,CAR."EGRFolioComprobante"
   ,CAR."EGRFechaComprobante"
   ,CAR."EGRIdTipoComprobante"
   ,CASE
   		WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
   		WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(EGRTipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
   		ELSE EGRTipOper."Nombre" 
   	END AS "EGRTipoOperacion"
   ,EGRConcepto."Nombre" AS "EGRNombreConcepto"
   ,cli."RazonSocial" as "NombreCliente"
   ,cli."DocumentoIdentidad" as "DocumentoIdentidad"
   ,PORTAFOLIO."CodigoPortafolio"
   ,CASE WHEN COALESCE(CCP."IdCuentaCustodioPortafolio",0) > 0 THEN CCP."IdBoveda" ELSE RES."IdBoveda" END "IdBoveda"
   ,CAR."IdMonedaPrincipal"
   ,CAR."IdMonedaSecundaria"
   ,CAR."ValorTipoCambioCierre"
   ,CASE
		WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaSecundaria" 
		ELSE CAR."INGMontoMonedaSecundaria" END	 AS "MontoMonedaSecundaria"
   ,CAR."IdOperacion"
   ,CAR."FechaVencimientoOperacion" AS "FechaVencimiento"
   ,SUM(case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoIngreso"
   ,SUM(CAST((CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' then CAR."EGRValorRescate" ELSE CAR."EGRCantidad" END*-1) as DECIMAL(26,8))) OVER (partition by car."FolioCartera" order by car."FechaIngresoRegistro") AS "SaldoRetiro"
   ,CAR."EGRSpread" AS "Spread"
   ,CAR."EGRSpreadMercado" AS "SpreadMercado"
   ,COALESCE(CAR."IdCarga",0) AS "IdCarga"
   ,CAR."EGRMontoARecibirAnticipo" 
   ,MT2."IdEstado" as "IdEstadoIdMovimientoCarteraEnTransito"
   ,COALESCE(CAR."EGRIdMovimientoCarteraEnTransito", 0) AS "EGRIdMovimientoCarteraEnTransito"
   ,COALESCE(UsrEgrOpe."CodigoUsuario",'') AS "UsuarioEgresoOperacion"
   ,COALESCE(UsrEgrCert."CodigoUsuario",'') AS "UsuarioEgresoCertificacion"      
FROM "BSCL.Mesa.BD::MES.Operacion" OPE
INNER JOIN "BSCL.Mesa.BD::MES.TipoOperacionPosicion" TOPE ON TOPE."IdTipoOperacion" = 'VENTER'
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES
ON OPE."IdOperacion" = RES."IdOperacion"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
ON RES."IdCarteraCustodia" = CAR."IdCartera"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (CAR."FolioCartera" = ULTSUB."FolioCarteraRelacionada")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SBY ON SBY."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = CAR."INGIdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As EGRTipOper ON EGRTipOper."IdTipoOperacion" = CAR."EGRIdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = OPE."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = OPE."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCP ON CCP."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON Concepto."IdConcepto" = 'VNTRME'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as EGRConcepto ON EGRConcepto."IdConcepto" = CAR."EGRIdConcepto"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORTAFOLIO."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO ON TipoInstrumento."IdTipoInstrumento" = CAR."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USRCREA ON USRCREA."IdUsuario" = CAR."IdUsuario"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT ON MT."IdMovimientoCarteraEnTransito" = CAR."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MT2 ON MT2."IdMovimientoCarteraEnTransito" = CAR."EGRIdMovimientoCarteraEnTransito"
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USRCERT ON USRCERT."IdUsuario" = MT."IdUsuarioCertificacion"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrOpe ON UsrEgrOpe."IdUsuario" = CAR."EGRIdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsrEgrCert ON UsrEgrCert."IdUsuario" = MT2."IdUsuarioCertificacion"
WHERE 
 OPE."IdTipoOperacion" IN  ('VENTER')
AND OPE."AfectaCustodia" = 'N'
AND CAR."IdEmpresa" = :IdEmpresa
AND ((:TipoOperacion = 'INGRESO' AND OPE."FechaOperacion" >= :FechaInicio AND OPE."FechaOperacion" <= :FechaTermino) OR
	 (:TipoOperacion = 'RETIRO' AND OPE."FechaOperacion" >= :FechaInicio AND OPE."FechaOperacion" <= :FechaTermino AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N') OR
	 (:TipoOperacion = ''))
AND CAR."INGIdConcepto" = CASE WHEN :IdConcepto <> '' THEN :IdConcepto ELSE CAR."INGIdConcepto" END
AND CAR."IdTipoInstrumento" != 'SIM'
AND CAR."IdClasificacionTipoInstrumento" != 'DV'
AND OPE."IdEstado" != 'ANU'
AND ( :IdClasificacionTipoInstrumento IN ('IF', 'RF') OR :IdClasificacionTipoInstrumento = '' )
AND OPE."IdCliente" = :IdCliente
ORDER BY "IdCartera" DESC