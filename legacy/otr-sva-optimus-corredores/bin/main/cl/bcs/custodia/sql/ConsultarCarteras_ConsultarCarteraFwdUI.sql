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
    , CASE Per."Tipo"
    	WHEN 'N' THEN Per."Nombres" ||' '|| Per."ApellidoPaterno" || ' ' ||Per."ApellidoMaterno" 
    	ELSE Per."RazonSocial" END as "RazonSocial"
    , Port."CodigoPortafolio"
    , Port."DescripcionPortafolio"
    , Port."IdMatrizAgrupadorAPV"
    , car."IdEstado"
    , Estado."Nombre" AS "NombreEstado"
    , car."IdMonedaSistema"
    , Mon1."Nombre" AS "MonedaSistema"
    , ifrs."Descripcion" AS "CodigoIFRS"
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
    , car."INGMontoCompraProximoMonedaInstrumento"
    , car."INGMontoMercadoProximoMonedaInstrumento"
    , car."Nemotecnico"
    , car."IdEmisor"
    , car."NemoSVS"
    , CASE WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
           WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
           ELSE car."IdMonedaInstrumento"
      END AS "IdMonedaEmision"
    , car."IdTipoReajuste"
    , TipoReajuste."Nombre" AS "NombreTipoReajuste"
    , case when ifnull(car."IdTipoReajuste",'') <> '' then car."IdTipoReajuste"
           when ifnull(car."IdMonedaInstrumento",'') <> '' then car."IdMonedaInstrumento" 
           when ifnull(car."IdMonedaPrincipal",'') <> '' then car."IdMonedaPrincipal" 
           else ''
      end as "IdMonedaInstrReajuste"
    , case when ifnull(car."IdMonedaInstrumento",'') <> '' then Mon2."Nombre" else ifnull(TipoReajuste."Nombre",'') end as "NombreMonedaInstrReajuste"
    , car."FechaEmisionInstrumento"
    , car."FechaVencimientoOperacion" as "FechaVencimientoInstrumento"
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
    , car."Cantidad"
    , car."CorteMinimo"
    , car."ValorRescate"
    , car."PrecioCompra"
    , CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
    		(CAR."INGCantidad" - resTot."IngCantidad") * CAR."MontoCompra" / CAR."INGCantidad"
    	   ELSE CAR."MontoCompra" END AS "MontoCompra"
    , car."INGFechaOperacion"
    , car."INGFechaLiquidacion"
    , CASE WHEN COALESCE(resTot."IngCantidad",0) > 0 THEN
    		car."INGCantidad" - resTot."IngCantidad"
    	   ELSE car."INGCantidad" END AS "INGCantidad"  
    , car."TasaCompra"
    , car."INGTasaCompra"
    , CAR."INGValorRescate" - COALESCE(resTot."IngValorRescate",0) AS "INGValorRescate"    
    , car."INGPrecioCompra"
    , car."INGPrecioPromedioCompra"
    , car."INGMontoPrecioPromedioCompra"
    , CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
    		(CAR."INGCantidad" - resTot."IngCantidad") * CAR."INGMontoATasaCompraActual" / CAR."INGCantidad"
    	   ELSE CAR."INGMontoATasaCompraActual" END AS "INGMontoATasaCompraActual"
    , car."INGFechaValorizacionCartera"
    , (CASE WHEN COALESCE(car."INGMontoATasaMercadoActual",0) = 0 THEN
    			COALESCE(PREMER."PrecioCierre",0) * "INGCantidad"
    		ELSE CAR."INGMontoATasaMercadoActual" END) - CAR."INGMontoATasaCompraActual" AS "DiferenciaInversion"
	, CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
			(CAR."INGCantidad" - resTot."IngCantidad") * CAR."INGMontoCompraActualMonedaInstrumento" / CAR."INGCantidad"
		   ELSE CAR."INGMontoCompraActualMonedaInstrumento" END AS "INGMontoCompraActualMonedaInstrumento"
    , car."INGFuenteTasaPrecioMercadoActual"
    , car."INGPrecioMercadoActual"
    , (car."INGMontoATasaMercadoActual" - car."INGMontoATasaCompraActual") /
      (CASE WHEN car."INGMontoATasaCompraActual" > 0 THEN car."INGMontoATasaCompraActual" ELSE 1 END) * 100 AS "Rentabilidad"
    , car."MontoCompraFactor" AS "RentabilidadReal"
    , car."INGTasaMercadoActual"
    , CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
    		(CAR."INGCantidad" - resTot."IngCantidad") * CAR."INGMontoATasaMercadoActual" / CAR."INGCantidad"
    	   ELSE CAR."INGMontoATasaMercadoActual" END AS "INGMontoATasaMercadoActual"
    , CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
    		(CAR."INGCantidad" - resTot."IngCantidad") * CAR."INGMontoMercadoActualMonedaInstrumento" / CAR."INGCantidad"
    	   ELSE CAR."INGMontoMercadoActualMonedaInstrumento" END AS "INGMontoMercadoActualMonedaInstrumento"
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
    , '0001-01-01' AS "FechaRespaldo"
    , car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "INGMontoReajusteCompraActual"
    , car."INGMontoInteresCompraProximoMonedaInstrumento" AS "INGMontoInteresCompraActual"
    , car."INGMontoInteresCorteCuponMonedaInstrumento" AS "InteresCorridoMonedaInstrumento"
    , car."INGMontoReajusteCorteCuponMonedaInstrumento" AS "ReajusteCorridoMonedaInstrumento"
    , Instrumento."EsPutEsCall" AS "TipoOpcion"
    , COALESCE (REG."Descripcion",'') AS "RegimenTributario"
    , car."FechaOrigenCompra"
    , car."INGCostoInversionEnFactorAjuste" as "CostoUF"
    , ProductoAgrupador."NombreProductoAgrupador"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS car 
LEFT JOIN (
    SELECT res."FolioCarteraOrigen"
        ,SUM("IngCantidad") AS "IngCantidad"
        ,SUM("IngValorRescate") AS "IngValorRescate"
        ,SUM("MontoValorizado") AS "IngMontoValorizado"
    FROM "BSCL.Custodia.BD::CUS.Reserva" AS res 
    WHERE res."IdEstado"='VIG' AND res."EgrCantidad" = 0
    GROUP BY res."FolioCarteraOrigen") AS resTot ON resTot."FolioCarteraOrigen" = car."FolioCartera"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" AND Port."IdEstado" = 'ACT'
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
LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoIFRS" AS ifrs ON ifrs."Codigo" = car."CodigoIFRS"
LEFT JOIN (
            SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"   
            FROM
                (   
                                
                    SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
                    ,prec."IdInstrumento",prec."Fecha", prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
                    FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
                    INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS 
                    ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(SIS."Valor")) 
                     where prec."IdBolsa" = 'BCS'
            ) PRE   
            WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0 
        )PREMER
	ON (car."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa")    
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = car."IdRegimenFondoMutuo"   
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
WHERE car."IdEmpresa" = :p_IdEmpresa 
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE 'VIG' END
AND car."IdCliente" <> :p_IdCliente
AND car."IdTipoInstrumento" = 'FWD'
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND car."IdMonedaInstrumento" = CASE WHEN :p_IdMoneda <> '' THEN :p_IdMoneda ELSE car."IdMonedaInstrumento" END
AND ((:p_tipoFecha = 'LIQ' AND car."INGFechaLiquidacion" <= :p_FechaConsulta AND car."EGRFechaLiquidacion" >= :p_FechaConsulta) OR
	 (:p_tipoFecha <> 'LIQ' AND car."FechaVencimientoOperacion" > :p_FechaConsulta))
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
AND car."IdTipoProducto" = 'MEFW';
