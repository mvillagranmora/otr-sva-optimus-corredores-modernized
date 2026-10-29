SELECT 
	RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
	, car."IdCartera"
	, car."FolioCartera"
	, car."IdEmpresa"
	, Empresa."Nombre" AS "NombreEmpresa"
	, car."FechaOperacion"
	, car."IdCliente"
	, car."IdPortafolio"
	, Ident."DocumentoIdentidad" AS "DocumentoIdentidad"
	,	CASE Per."Tipo" 
			WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
			ELSE Per."RazonSocial"
		END  as "RazonSocial"
	, Port."CodigoPortafolio"
	, Port."DescripcionPortafolio"
	, car."IdEstado"
	, Estado."Nombre" AS "NombreEstado"
	, car."IdMonedaSistema"
	, Mon1."Nombre" AS "MonedaSistema"
	, car."CodigoIFRS"
	, car."IdConcepto"
	, Concepto."Nombre" as "NombreConcepto"
	, car."IdTipoOperacion"
	, car."IdTipoComprobante"
	, car."FolioComprobante"
	, car."FechaComprobante"
	, car."IdMesa"
	, car."IdBolsa"
	, Bolsa."Nombre" AS "NombreBolsa"
	, car."IdSucursal"
	, car."IdAgente"
	, car."IdCondicionLiquidacion"
	, CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion"
	, car."FechaLiquidacion"
	, car."IdClasificacionTipoInstrumento"
	, ClasInst."Nombre" as "ClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, TipoInstrumento."Descripcion" AS "TipoInstrumento"
	, car."IdInstrumento"
	, Instrumento."RazonSocial" AS "RazonSocialInstrumento"
	, car."ISIN"
	,case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PRF','ERF','PAC','RRV','RRF') then
			(	select "Nemotecnico" 
				from (
						select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
						from "BSCL.Custodia.BD::CUS.Subyacente" as miSub
						where car."FolioCartera" = miSub."FolioCarteraRelacionada"
					 ) as Suby
				where Suby."NroFila" = 1
			)
		when car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		else car."Nemotecnico"
	 end as "Nemotecnico"
	, car."IdEmisor"
	, car."NemoSVS"
	, CASE 
		WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
	   	WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
	   	ELSE car."IdMonedaInstrumento"
	  END AS "IdMonedaEmision"
	, car."IdTipoReajuste"
	, TipoReajuste."Nombre" AS "NombreTipoReajuste"
	, case 
		when ifnull(car."IdTipoReajuste",'') <> '' then car."IdTipoReajuste"
	   	when ifnull(car."IdMonedaInstrumento",'') <> '' then car."IdMonedaInstrumento" 
	   	when ifnull(car."IdMonedaPrincipal",'') <> '' then car."IdMonedaPrincipal" 
	  else ''
	  end as "IdMonedaInstrReajuste"
	, case when ifnull(car."IdMonedaInstrumento",'') <> '' then Mon2."Nombre" else ifnull(TipoReajuste."Nombre",'') end as "NombreMonedaInstrReajuste"
	, car."FechaEmisionInstrumento"
	, case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PRF','ERF','PAC','RRV','RRF','FWD') then car."FechaVencimientoOperacion" 
		else car."FechaVencimientoInstrumento"
	  end as "FechaVencimientoInstrumento"
	, car."EsFungible"
	---------------------------------------------------------
	, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
	, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN BOV."Nombre" ELSE Boveda."Nombre" END AS "NombreBoveda"
	, COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
	, CCPI."IdTipoCustodia"
	, TIPOCUSTODIA."NombreTipoCustodia"
	---------------------------------------------------------
	, car."IdMonedaInstrumento"
	, Mon2."Nombre" AS "MonedaInstrumento"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."ValorRescate" ELSE car."Cantidad" END AS "Cantidad"
	, car."CorteMinimo"
	, car."ValorRescate"
	, car."PrecioCompra"
	, car."MontoCompra"
	, CASE 
  			WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN
  				COALESCE(((car."INGCantidad" * car."MontoCompra") / NULLIF(car."Cantidad",0)),0)
  			WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
  				(car."INGCantidad" * car."PrecioCompra")
  			WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
  				COALESCE(((car."INGValorRescate" * car."MontoCompra") / NULLIF(car."ValorRescate",0)), 0)
  			ELSE 0
  		  END AS "MontoCompraInicial"
	, car."INGFechaOperacion"
	, car."INGFechaLiquidacion"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "INGCantidad"
	, car."TasaCompra"
	, car."INGTasaCompra"
	, car."INGValorRescate"
	, car."INGPrecioCompra"
	, car."INGPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioCompra"
	, car."INGMontoATasaCompraActual"
	, car."INGFechaValorizacionCartera"
	, car."INGMontoCompraProximoMonedaInstrumento"
	, car."INGMontoMercadoProximoMonedaInstrumento"
	
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE WHEN COALESCE(car."INGMontoATasaMercadoActual",0.0 )=0 THEN COALESCE(PREMER."PrecioCierre",0)*"INGCantidad" ELSE car."INGMontoATasaMercadoActual" END)  
			ELSE car."INGMontoATasaMercadoActual" 
	   	END) - car."INGMontoATasaCompraActual" 
		AS "DiferenciaInversion"
	, car."INGMontoCompraActualMonedaInstrumento"
	, car."INGFuenteTasaPrecioMercadoActual"
	, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(car."INGPrecioMercadoActual", PREMER."PrecioCierre",0)  ELSE car."INGPrecioMercadoActual" END )	AS "INGPrecioMercadoActual"
	, (((CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 ) * "INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		 END)- car."INGMontoCompraActualMonedaInstrumento")	
		 /  (CASE 
		 		WHEN car."INGMontoCompraActualMonedaInstrumento" >0 THEN car."INGMontoCompraActualMonedaInstrumento"
		 		ELSE 1 
		 	 END))*100 AS "Rentabilidad"
	, car."MontoCompraFactor" AS "RentabilidadReal"
	, car."INGTasaMercadoActual"
	, car."INGMontoATasaMercadoActual"
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(car."INGMontoMercadoActualMonedaInstrumento",0.0 )>0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 )* car."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		END) AS "INGMontoMercadoActualMonedaInstrumento"
	, car."INGFechaPagoCorteCupon"
	, car."INGInteresesPagoCorteCupon"
	, car."INGMontoCorteCupon"
	, car."INGMontoCapitalCorteCupon"
	, car."INGMontoInteresCorteCupon"
	, CASE car."EGRFechaOperacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaOperacion" END AS "EGRFechaOperacion" 
	, CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END AS "EGRFechaLiquidacion" 
	, car."EGRIdTipoComprobante"
	, car."EGRFolioComprobante"
	, car."EGRFechaComprobante"
	, car."EGRIdConcepto"
	, ConceptoEGR."Nombre" as "EGRNombreConcepto"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."EGRValorRescate" ELSE car."EGRCantidad" END AS "EGRCantidad"
	, car."EGRValorRescate"
	, car."EGRPrecio"
	, car."EGRTasa"
	, car."EGRMonto"
	, car."EGRMontoMonedaInstrumento"
	, TO_VARCHAR(car."FechaRespaldo", 'YYYY-MM-DD')
	
	, car."INGMontoInteresCompraProximoMonedaInstrumento" AS  "INGMontoInteresCompraActual" 
	, car."INGMontoReajusteCompraAnteriorMonedaInstrumento" AS "INGMontoReajusteCompraActual"
	, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoInteresCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoInteresCorteCuponMonedaInstrumento" END AS "InteresCorridoMonedaInstrumento"
	, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoReajusteCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoReajusteCorteCuponMonedaInstrumento" END AS "ReajusteCorridoMonedaInstrumento"
	, Instrumento."EsPutEsCall" AS "TipoOpcion"
	, case
		when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
		when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
		else 0
	  end as "PlazoEnDiasOperacion"
	, car."INGDurationCompraActual"
	, car."INGPrecioMercadoProximo"
	, car."INGTasaMercadoProximo"
	, car."TipoCarteraCorredor"
	, COALESCE (REG."Descripcion",'') AS "RegimenTributario"
	, car."FechaOrigenCompra"
	, car."INGCostoInversionEnFactorAjuste" as "CostoUF"
	,ProductoAgrupador."NombreProductoAgrupador"
	, 'N' AS "EsNegativa"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS car 
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON Cli."IdCliente" = Port."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" = Cli."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Ident ON Per."IdDocumentoIdentidad" = Ident."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON car."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = car."IdClasificacionTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON car."IdMonedaSistema" = Mon1."IdMoneda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON car."IdMonedaInstrumento" = Mon2."IdMoneda" 
	LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON BOV."IdBoveda" = CCPI."IdBoveda"
    LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = CCPI."IdTipoCustodia"
	LEFT JOIN (
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
				FROM
					(	SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
								,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(SIS."Valor")) 
				 		WHERE prec."IdBolsa" = 'BCS'
					) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0 ) PREMER ON (car."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa")			
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = car."IdRegimenFondoMutuo"	
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
WHERE car."IdEmpresa" = :p_IdEmpresa 
AND car."IdEstado" NOT IN ('ANU','RPA')
AND car."FechaRespaldo" = :p_FechaConsulta
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE car."IdEstado" END
AND car."IdTipoInstrumento" NOT IN ('SIM','PAC','RRV','RRF','PRT','PRF','EMP','ERF')
AND car."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente ELSE car."IdCliente" END
AND car."IdPortafolio" = CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio ELSE car."IdPortafolio" END
AND car."IdClasificacionTipoInstrumento" = CASE WHEN :p_IdClasificacionTipoInstrumento <> '' THEN :p_IdClasificacionTipoInstrumento ELSE car."IdClasificacionTipoInstrumento" END
AND car."IdTipoInstrumento" = CASE WHEN :p_IdTipoInstrumento <> '' THEN :p_IdTipoInstrumento ELSE car."IdTipoInstrumento" END
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND car."IdMonedaInstrumento" = CASE WHEN :p_IdMoneda <> '' THEN :p_IdMoneda ELSE car."IdMonedaInstrumento" END
AND ((:p_tipoFecha = 'LIQ' AND car."INGFechaLiquidacion" <= :p_FechaConsulta AND  (car."EGRFechaLiquidacion" >= :p_FechaConsulta OR car."EGRFechaLiquidacion" ='0001-01-01')) OR
	 (:p_tipoFecha <> 'LIQ' AND car."FechaOperacion" <= :p_FechaConsulta AND car."EGRCantidad" = 0))
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT 
	RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
	, car."IdCartera"
	, car."FolioCartera"
	, car."IdEmpresa"
	, Empresa."Nombre" AS "NombreEmpresa"
	, car."FechaOperacion"
	, car."IdCliente"
	, car."IdPortafolio"
	, Ident."DocumentoIdentidad" AS "DocumentoIdentidad"
	,	CASE Per."Tipo" 
			WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
			ELSE Per."RazonSocial"
		END  as "RazonSocial"
	, Port."CodigoPortafolio"
	, Port."DescripcionPortafolio"
	, car."IdEstado"
	, Estado."Nombre" AS "NombreEstado"
	, car."IdMonedaSistema"
	, Mon1."Nombre" AS "MonedaSistema"
	, car."CodigoIFRS"
	, car."IdConcepto"
	, Concepto."Nombre" as "NombreConcepto"
	, car."IdTipoOperacion"
	, car."IdTipoComprobante"
	, car."FolioComprobante"
	, car."FechaComprobante"
	, car."IdMesa"
	, car."IdBolsa"
	, Bolsa."Nombre" AS "NombreBolsa"
	, car."IdSucursal"
	, car."IdAgente"
	, car."IdCondicionLiquidacion"
	, CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion"
	, car."FechaLiquidacion"
	, car."IdClasificacionTipoInstrumento"
	, ClasInst."Nombre" as "ClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, TipoInstrumento."Descripcion" AS "TipoInstrumento"
	, car."IdInstrumento"
	, Instrumento."RazonSocial" AS "RazonSocialInstrumento"
	, car."ISIN"
	,case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PRF','ERF','PAC','RRV','RRF') then
			(	select "Nemotecnico" 
				from (
						select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
						from "BSCL.Custodia.BD::CUS.Subyacente" as miSub
						where car."FolioCartera" = miSub."FolioCarteraRelacionada"
					 ) as Suby
				where Suby."NroFila" = 1
			)
		when car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		else car."Nemotecnico"
	 end as "Nemotecnico"
	, car."IdEmisor"
	, car."NemoSVS"
	, CASE 
		WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
	   	WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
	   	ELSE car."IdMonedaInstrumento"
	  END AS "IdMonedaEmision"
	, car."IdTipoReajuste"
	, TipoReajuste."Nombre" AS "NombreTipoReajuste"
	, case 
		when ifnull(car."IdTipoReajuste",'') <> '' then car."IdTipoReajuste"
	   	when ifnull(car."IdMonedaInstrumento",'') <> '' then car."IdMonedaInstrumento" 
	   	when ifnull(car."IdMonedaPrincipal",'') <> '' then car."IdMonedaPrincipal" 
	  else ''
	  end as "IdMonedaInstrReajuste"
	, case when ifnull(car."IdMonedaInstrumento",'') <> '' then Mon2."Nombre" else ifnull(TipoReajuste."Nombre",'') end as "NombreMonedaInstrReajuste"
	, car."FechaEmisionInstrumento"
	, case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PRF','ERF','PAC','RRV','RRF','FWD') then car."FechaVencimientoOperacion" 
		else car."FechaVencimientoInstrumento"
	  end as "FechaVencimientoInstrumento"
	, car."EsFungible"
	---------------------------------------------------------
	, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
	, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN BOV."Nombre" ELSE Boveda."Nombre" END AS "NombreBoveda"
	, COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
	, CCPI."IdTipoCustodia"
	, TIPOCUSTODIA."NombreTipoCustodia"
	---------------------------------------------------------
	, car."IdMonedaInstrumento"
	, Mon2."Nombre" AS "MonedaInstrumento"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."ValorRescate" ELSE car."Cantidad" END AS "Cantidad"
	, car."CorteMinimo"
	, car."ValorRescate"
	, car."PrecioCompra"
	, car."MontoCompra"
	, CASE 
  			WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN
  				COALESCE(((car."INGCantidad" * car."MontoCompra") / NULLIF(car."Cantidad",0)),0)
  			WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
  				(car."INGCantidad" * car."PrecioCompra")
  			WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
  				COALESCE(((car."INGValorRescate" * car."MontoCompra") / NULLIF(car."ValorRescate",0)), 0)
  			ELSE 0
  		  END AS "MontoCompraInicial"
	, car."INGFechaOperacion"
	, car."INGFechaLiquidacion"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "INGCantidad"
	, car."TasaCompra"
	, car."INGTasaCompra"
	, car."INGValorRescate"
	, car."INGPrecioCompra"
	, car."INGPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioCompra"
	, car."INGMontoATasaCompraActual"
	, car."INGFechaValorizacionCartera"
	, car."INGMontoCompraProximoMonedaInstrumento"
	, car."INGMontoMercadoProximoMonedaInstrumento"
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE WHEN COALESCE(car."INGMontoATasaMercadoActual",0.0 )=0 THEN COALESCE(PREMER."PrecioCierre",0)*CAR."INGCantidad" ELSE car."INGMontoATasaMercadoActual" END)  
			ELSE car."INGMontoATasaMercadoActual" 
	   	END) - car."INGMontoATasaCompraActual" 
		AS "DiferenciaInversion"
	, car."INGMontoCompraActualMonedaInstrumento"
	, car."INGFuenteTasaPrecioMercadoActual"
	, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(car."INGPrecioMercadoActual", PREMER."PrecioCierre",0)  ELSE car."INGPrecioMercadoActual" END )	AS "INGPrecioMercadoActual"
	, (((CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 ) * CAR."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		 END)- car."INGMontoCompraActualMonedaInstrumento")	
		 /  (CASE 
		 		WHEN car."INGMontoCompraActualMonedaInstrumento" >0 THEN car."INGMontoCompraActualMonedaInstrumento"
		 		ELSE 1 
		 	 END))*100 AS "Rentabilidad"
	, car."MontoCompraFactor" AS "RentabilidadReal"
	, car."INGTasaMercadoActual"
	, car."INGMontoATasaMercadoActual"
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(car."INGMontoMercadoActualMonedaInstrumento",0.0 )>0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 )* car."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		END) AS "INGMontoMercadoActualMonedaInstrumento"
	, car."INGFechaPagoCorteCupon"
	, car."INGInteresesPagoCorteCupon"
	, car."INGMontoCorteCupon"
	, car."INGMontoCapitalCorteCupon"
	, car."INGMontoInteresCorteCupon"
	, CASE car."EGRFechaOperacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaOperacion" END AS "EGRFechaOperacion" 
	, CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END AS "EGRFechaLiquidacion" 
	, car."EGRIdTipoComprobante"
	, car."EGRFolioComprobante"
	, car."EGRFechaComprobante"
	, car."EGRIdConcepto"
	, ConceptoEGR."Nombre" as "EGRNombreConcepto"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."EGRValorRescate" ELSE car."EGRCantidad" END AS "EGRCantidad"
	, car."EGRValorRescate"
	, car."EGRPrecio"
	, car."EGRTasa"
	, car."EGRMonto"
	, car."EGRMontoMonedaInstrumento"
	, TO_VARCHAR(car."FechaRespaldo", 'YYYY-MM-DD')
	, car."INGMontoInteresCompraProximoMonedaInstrumento" AS  "INGMontoInteresCompraActual" 
	, car."INGMontoReajusteCompraAnteriorMonedaInstrumento" AS "INGMontoReajusteCompraActual"
	, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoInteresCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoInteresCorteCuponMonedaInstrumento" END AS "InteresCorridoMonedaInstrumento"
	, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoReajusteCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoReajusteCorteCuponMonedaInstrumento" END AS "ReajusteCorridoMonedaInstrumento"
	, Instrumento."EsPutEsCall" AS "TipoOpcion"
	, case
		when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
		when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
		else 0
	  end as "PlazoEnDiasOperacion"
	, car."INGDurationCompraActual"
	, car."INGPrecioMercadoProximo"
	, car."INGTasaMercadoProximo"
	, car."TipoCarteraCorredor"
	, COALESCE (REG."Descripcion",'') AS "RegimenTributario"
	, car."FechaOrigenCompra"
	, car."INGCostoInversionEnFactorAjuste" as "CostoUF"
	,ProductoAgrupador."NombreProductoAgrupador"
	, 'N' AS "EsNegativa"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = CAR."IdPortafolioRelacionado" AND PORT."IdCliente" = CAR."IdClienteRelacionado"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON BOV."IdBoveda" = CCPI."IdBoveda"
LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = CCPI."IdTipoCustodia"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento ON Instrumento."IdInstrumento" = SUB."IdInstrumento"
--
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" = Cli."IdPersona"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = car."IdRegimenFondoMutuo"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Ident ON Per."IdDocumentoIdentidad" = Ident."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON car."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = car."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON car."IdMonedaSistema" = Mon1."IdMoneda" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON car."IdMonedaInstrumento" = Mon2."IdMoneda" 
LEFT JOIN (
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
				FROM
					(	SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
								,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(SIS."Valor")) 
				 		WHERE prec."IdBolsa" = 'BCS'
					) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0 ) PREMER ON (car."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa")
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND car."IdMonedaInstrumento" = CASE WHEN :p_IdMoneda <> '' THEN :p_IdMoneda ELSE car."IdMonedaInstrumento" END
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE car."IdEstado" END
AND car."FechaRespaldo" = :p_FechaConsulta
AND	CASE WHEN :p_IdCliente > 0 THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END = case when :p_IdCliente > 0 then :p_IdCliente else CAR."IdCliente" END
AND	CASE WHEN :p_IdCliente > 0 THEN CAR."IdPortafolioRelacionado" ELSE CAR."IdPortafolio" END = case when :p_IdPortafolio > 0 then :p_IdPortafolio else CAR."IdPortafolio" END
AND
(
       (:p_tipoFecha = 'LIQ' AND CAR."INGFechaLiquidacion" <= :p_FechaConsulta)
    OR (:p_tipoFecha = 'ADQ' AND CAR."INGFechaOperacion" <= :p_FechaConsulta)
)
AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE SUB."IdInstrumento" END
AND SUB."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SUB."Nemotecnico" END
AND CAR."IdTipoInstrumento" = 'PAC'
AND SUB."IdEstado"='VIG'
AND CAR."IdTipoOperacion"   IN ('VENPAC', 'COMPAC')
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT  
      1 as "RANKG"
	, car."IdCarteraNegativa" AS "IdCartera"
	, car."IdCarteraNegativa" AS "FolioCartera"
	, car."IdEmpresa"
	, Empresa."Nombre" AS "NombreEmpresa"
	, car."FechaOperacion"
	, car."IdCliente"
	, car."IdPortafolio"
	, Ident."DocumentoIdentidad" AS "DocumentoIdentidad"
	, CASE Per."Tipo" WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" ELSE Per."RazonSocial" END  as "RazonSocial"
    , Port."CodigoPortafolio"
    , Port."DescripcionPortafolio"
    , car."IdEstado"
    , Estado."Nombre" AS "NombreEstado"
    , Mon3."IdMoneda" AS "IdMonedaSistema"
    , Mon3."Nombre" AS "MonedaSistema"
    , ifrs."Descripcion" AS "CodigoIFRS"
    , car."IdConcepto"
    , Concepto."Nombre" as "NombreConcepto"
    , car."IdTipoOperacion"
    , car."IdTipoComprobante"
    , car."FolioComprobante" 
    , TO_VARCHAR(COALESCE(COMP."FechaComprobante", '0001-01-01'), 'YYYY-MM-DD')  AS "FechaComprobante"
    , car."IdMesa"
    , car."IdBolsa"
    , Bolsa."Nombre" AS "NombreBolsa"
    , car."IdSucursal"
    , car."IdAgente"
    , car."IdCondicionLiquidacion"
    , CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion"
    , car."FechaLiquidacion"
    , TipoInstrumento."IdClasificacionTipoInstrumento" 
    , ClasInst."Nombre" as "ClasificacionTipoInstrumento"
    , TipoInstrumento."IdTipoInstrumento"
    , TipoInstrumento."Descripcion" AS "TipoInstrumento"
    , car."IdInstrumento"
    , Instrumento."RazonSocial" AS "RazonSocialInstrumento"
    , INSTRUMENTO."ISIN" AS "ISIN"
    , case when TipoInstrumento."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
			else car."Nemotecnico" 
	  end as "Nemotecnico"
    , car."IdEmisor"
    , car."NemoSVS"
    , CASE WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
           WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
           ELSE car."IdMonedaInstrumento"
      END AS "IdMonedaEmision"
    , car."IdTipoReajuste"
    , TipoReajuste."Nombre" AS "NombreTipoReajuste"
    , case 
		when ifnull(car."IdTipoReajuste",'') <> '' then car."IdTipoReajuste"
	   	when ifnull(car."IdMonedaInstrumento",'') <> '' then car."IdMonedaInstrumento" 
	   	when ifnull(car."IdMonedaEmision",'') <> '' then car."IdMonedaEmision" 
	  else '' end AS "IdMonedaInstrReajuste"
    , TipoReajuste."Nombre" as "NombreMonedaInstrReajuste"
    , Instrumento."FechaEmision" AS "FechaEmisionInstrumento"
    , case 
        when TipoInstrumento."IdTipoInstrumento" IN ('PAG') then car."FechaVencimientoOperacion" 
        else Instrumento."FechaVencimiento" 
      end as "FechaVencimientoInstrumento"
    , car."EsFungible"
    , CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
    , CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN BOV."Nombre" ELSE Boveda."Nombre" END AS "NombreBoveda"
    , COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
    , CCPI."IdTipoCustodia"
    , TIPOCUSTODIA."NombreTipoCustodia"
    , car."IdMonedaInstrumento"
    , Mon2."Nombre" AS "MonedaInstrumento"
    , CASE WHEN CLASINST."IdClasificacionTipoInstrumento"='IF' THEN car."ValorRescate" ELSE cast(IFNULL(car."Cantidad" ,0) as DECIMAL(26,8)) END AS "Cantidad"
    , car."CorteMinimo"
    , car."ValorRescate"
    , car."PrecioOperacion" AS "PrecioCompra"
    , car."MontoOperacionMonedaInstrumento" AS "MontoCompra"
    , 0 AS "MontoCompraInicial"
    , car."FechaOperacion" AS "INGFechaOperacion"
    , car."FechaLiquidacion" AS "INGFechaLiquidacion"
    , -1 * car."Cantidad" AS "INGCantidad"
    , car."TasaOperacion"  AS "TasaCompra"
    , car."TasaOperacion"  AS "INGTasaCompra"
    , - 1 * car."ValorRescate" AS "INGValorRescate"
    , car."PrecioOperacion" AS "INGPrecioCompra"
    , 0 AS "INGPrecioPromedioCompra"
    , 0 AS "INGMontoPrecioPromedioCompra"
    , -1 * cast(car."MontoOperacionMonedaInstrumento" as DECIMAL(26,8)) AS "INGMontoATasaCompraActual"
    , car."FechaValorizacion" AS "INGFechaValorizacionCartera"
    , 0 AS "INGMontoCompraProximoMonedaInstrumento"
    , 0 AS "INGMontoMercadoProximoMonedaInstrumento"
    ,CASE 
    		WHEN ClasInst."IdClasificacionTipoInstrumento" IN ( 'RF', 'IF' ) THEN 
    			CAR."MontoMercadoMonedaInstrumento" - CAR."MontoOperacionMonedaInstrumento"
           	ELSE 
           		(CASE 
           			WHEN COALESCE(car."MontoMercadoMonedaInstrumento",0 ) = 0 THEN 
           				COALESCE(PRECIO_BCS."Precio",0) * car."Cantidad" 
           			ELSE CAR."MontoMercadoMonedaSistema" 
                END) - CAR."MontoOperacionMonedaInstrumento" 
      	END AS "DiferenciaInversion"
    , -1 * car."MontoOperacionMonedaInstrumento" AS "INGMontoCompraActualMonedaInstrumento"
    , car. "FuentePrecioTasa" AS "INGFuenteTasaPrecioMercadoActual"
    , (CASE CLASINST."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(PRECIO_BCS."Precio",car."PrecioMercado" , 0.0)  ELSE car."PrecioMercado"  END ) AS "INGPrecioMercadoActual"
    ,(((CASE ClasInst."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(car."MontoMercadoMonedaInstrumento" ,0.0 )>0 
	                                                                    THEN car."MontoMercadoMonedaInstrumento" 
	                                                                    ELSE COALESCE(PRECIO_BCS."Precio",0 ) * car."Cantidad"  END)  
	                                             ELSE car."MontoMercadoMonedaInstrumento" END)
	     - car."MontoOperacionMonedaInstrumento") /  (CASE WHEN car."MontoOperacionMonedaInstrumento" >0 THEN car."MontoOperacionMonedaInstrumento" ELSE 1 END))*100  
   	AS "Rentabilidad" 
    , 0 AS "RentabilidadReal" --pregunar
    , car."TasaMercado" AS "INGTasaMercadoActual"
    , -1 * car."MontoMercadoMonedaInstrumento" AS "INGMontoATasaMercadoActual"
    , -1 * car."MontoMercadoMonedaInstrumento" AS "INGMontoMercadoActualMonedaInstrumento"
    , car."FechaPagoCorteCupon" AS "INGFechaPagoCorteCupon"
    , car."InteresesPagoCorteCupon"  AS "INGInteresesPagoCorteCupon"
    , car."MontoCorteCupon" AS "INGMontoCorteCupon"
    , car."MontoCapitalCorteCupon" AS "INGMontoCapitalCorteCupon"
    , car."MontoInteresCorteCupon" AS "INGMontoInteresCorteCupon"
    , '' AS "EGRFechaOperacion" 
    , '' AS "EGRFechaLiquidacion" 
    , NULL AS "EGRIdTipoComprobante"
    , NULL AS "EGRFolioComprobante"
    , NULL AS "EGRFechaComprobante"
    , '' AS "EGRIdConcepto"
    , NULL as "EGRNombreConcepto"
    , 0  AS "EGRCantidad"
    , 0 AS "EGRValorRescate"
    , 0 AS "EGRPrecio"
    , 0 AS "EGRTasa"
    , 0 AS "EGRMonto"
    , 0 AS "EGRMontoMonedaInstrumento"
    , CAR."FechaRespaldo" AS "FechaRespaldo"
    , NULL AS "INGMontoInteresCompraActual"
    , NULL AS "INGMontoReajusteCompraActual"
    , NULL AS "InteresCorridoMonedaInstrumento"
    , NULL AS "ReajusteCorridoMonedaInstrumento"
    , Instrumento."EsPutEsCall" AS "TipoOpcion"
	, 0 as "PlazoEnDiasOperacion"
	, 0 AS "INGDurationCompraActual"
	, car."PrecioMercado" AS "INGPrecioMercadoProximo"
	, car."TasaMercado" AS "INGTasaMercadoProximo"
    , '' AS "TipoCarteraCorredor"  
	, '' AS "RegimenTributario"
    , '0001-01-01' AS "FechaOrigenCompra"
    , 0 as "CostoUF"
	, '' as "NombreProductoAgrupador"
	, 'S' AS "EsNegativa"
FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" AS car	
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON Cli."IdCliente" = Port."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" = Cli."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Ident ON Per."IdDocumentoIdentidad" = Ident."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoIFRS" AS ifrs ON ifrs."Codigo" = car."CodigoIFRS"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" as ConfSisMon on ConfSisMon."CodigoParametro" = 'PLA.MonedaSistema' and ConfSisMon."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON car."IdMonedaEmision" = Mon1."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON car."IdMonedaInstrumento" = Mon2."IdMoneda" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon3 ON ConfSisMon."Valor" = Mon3."IdMoneda"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON BOV."IdBoveda" = CCPI."IdBoveda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = CCPI."IdTipoCustodia" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP ON comp."IdComprobante" = car."FolioComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento"  AS PRECIO_BCS
    ON  PRECIO_BCS."IdEmpresa" = :p_IdEmpresa AND PRECIO_BCS."IdInstrumento" = car."IdInstrumento"  AND PRECIO_BCS."FechaSistema" = :p_FechaConsulta

WHERE 
CAR."IdEmpresa" = :p_IdEmpresa
AND car."IdEstado"='VIG'
AND car."IdMonedaInstrumento" = CASE WHEN :p_IdMoneda <> '' THEN :p_IdMoneda ELSE car."IdMonedaInstrumento" END
AND car."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente ELSE car."IdCliente" END
AND car."IdPortafolio" = CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio ELSE car."IdPortafolio" END
AND car."FolioCustodia" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCustodia" END
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND TipoInstrumento."IdTipoInstrumento" = CASE WHEN :p_IdTipoInstrumento <> '' THEN :p_IdTipoInstrumento ELSE TipoInstrumento."IdTipoInstrumento" END
AND ClasInst."IdClasificacionTipoInstrumento" = CASE WHEN :p_IdClasificacionTipoInstrumento <> '' THEN :p_IdClasificacionTipoInstrumento ELSE ClasInst."IdClasificacionTipoInstrumento" END
AND
(
       (:p_tipoFecha = 'LIQ' AND CAR."FechaLiquidacion" <= :p_FechaConsulta)
    OR (:p_tipoFecha = 'ADQ' AND CAR."FechaOperacion" <= :p_FechaConsulta)
)
AND car."FechaRespaldo" = :p_FechaConsulta;

