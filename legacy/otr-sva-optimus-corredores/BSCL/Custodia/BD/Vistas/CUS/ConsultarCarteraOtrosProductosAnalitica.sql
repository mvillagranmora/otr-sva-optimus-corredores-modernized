SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraOtrosProductosAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraOtrosProductosAnalitica" AS 
SELECT    
 	 
     car."IdEmpresa" 								AS "IdEmpresa"
	, Empresa."Nombre" 								AS "NombreEmpresa"
	, car."IdCartera"								AS "IdCartera"
	, car."FolioCartera"							AS "FolioCartera"
	, TO_VARCHAR(IFNULL(car."FechaIngresoRegistro",'0001-01-01 00:00:00.0'))   	    AS "FechaIngresoRegistro"
	, TO_VARCHAR(IFNULL(car."FechaModificacionRegistro",'0001-01-01 00:00:00.0'))    AS "FechaModificacionRegistro"
--Cliente
		,CASE WHEN car."IdCliente" = 0 THEN NULL ELSE car."IdCliente" END AS "IdCliente" 
		,CAST(cli."NombreCompleto" AS NVARCHAR(302)) AS "NombreCliente"
		,CAST(IFNULL(cli."DocumentoIdentidad",'') AS NVARCHAR(40)) AS "DocumentoIdentidad"
		,CASE WHEN car."IdPortafolio" = 0 THEN NULL ELSE car."IdPortafolio" END AS "IdPortafolio"  
		,CAST(IFNULL(Port."CodigoPortafolio",'') AS NVARCHAR(100)) AS "CodigoPortafolio"
		,CAST(IFNULL(Port."DescripcionPortafolio",'') AS NVARCHAR(100)) AS "DescripcionPortafolio"
		,CASE WHEN car."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE car."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento" 
		,CAST(coalesce(ClasInst."Nombre",'') AS NVARCHAR(100)) AS "ClasificacionTipoInstrumento"
		,CASE WHEN car."IdTipoInstrumento" = '' THEN NULL ELSE car."IdTipoInstrumento" END AS "IdTipoInstrumento" 
		,CAST(coalesce(TipoInstrumento."Descripcion",'') AS NVARCHAR(100)) AS "TipoInstrumento"
		,CASE WHEN car."IdInstrumento" = 0 THEN NULL ELSE car."IdInstrumento" END AS "IdInstrumento" 
		,CAST(car."Nemotecnico" AS NVARCHAR(40)) AS "Nemotecnico"
		,CASE WHEN "IdMonedaInstrumento" = '' THEN NULL ELSE "IdMonedaInstrumento" END AS "IdMonedaInstrumento"
		,CAST(IFNULL(Mon2."Nombre",'') AS NVARCHAR(100)) AS "MonedaInstrumento"
		,CASE WHEN car."IdTipoPrestamo" = '' THEN NULL ELSE car."IdTipoPrestamo" END AS "IdTipoPrestamo" 
		,CAST(IFNULL(tpres."IdTipoPrestamo",'') AS NVARCHAR(100)) AS "TipoPrestamo"
		,IFNULL(car."CobraPrimaAlInicio", '') AS "CobraPrimaAlInicio"
		, CASE
	  		WHEN ifnull(CAR."IdTipoReajuste", '') <> '' THEN CAR."IdTipoReajuste"
	  		WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN CAR."IdMonedaPrincipal"
	  		ELSE ifnull(CAR."IdMonedaInstrumento",'')
	   	  end AS "IdTipoReajuste"
		, CASE
	  		WHEN ifnull(TipoReajuste."Nombre", '') <> '' THEN coalesce(TipoReajuste."Nombre",'')
	  		WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN coalesce(Mon3."Nombre",'')
	  		ELSE ifnull(Mon4."Nombre",'')
	   	  end 											AS "TipoReajuste"
		,CASE WHEN RTRIM(car."IdEmisor") = '' THEN 'OPTIMUS' ELSE CAST(car."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor"
		,LEFT(TO_VARCHAR(IFNULL(car."FechaEmisionInstrumento",'0001-01-01')),10) AS "FechaEmisionInstrumento"
		,case 
			when car."IdTipoInstrumento" IN ('SIM','PRT','EMP') then LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10)
			else LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoInstrumento",'0001-01-01')),10)    
	 	end 											AS "FechaVencimientoInstrumento"
		,"EsFungible"
		,CASE WHEN RTRIM(car."IdBoveda") = '' THEN 'DCV' ELSE car."IdBoveda" END AS "IdBoveda"    
		,CAST(IFNULL(Boveda."Nombre",'')  AS NVARCHAR(100)) AS "NombreBoveda"
	, LEFT(TO_VARCHAR(IFNULL(car."FechaOperacion",'0001-01-01')),10) AS "FechaOperacionInicial"
	, LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10) AS "FechaVencimientoOperacionInicial"	
	, case
		when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
		when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
		else 0
		end 										AS "PlazoEnDiasOperacionInicial" 
--Cantidades y Montos
	, car."Cantidad"								AS "CantidadInicial" 
	, car."PrecioCompra"							AS "PrecioCompraInicial"
	, car."TasaCompra"								AS "TasaCompraInicial"
	, car."CorteMinimo"								AS "CorteMinimoInicial"
	, car."ValorRescate"							AS "ValorRescateInicial"
	, car."MontoCompra"								AS "MontoCompraInicial"
	, car."MontoCompraMonedaInstrumento"			AS "MontoCompraMonedaInstrumentoInicial"
	, car."PrecioAPlazo"							AS "PrecioAPlazoInicial"
	, car."MontoAPlazo"								AS "MontoAPlazoInicial"
	, car."MontoAPlazoMonedaInstrumento"			AS "MontoAPlazoMonedaInstrumentoInicial"
	
	, LEFT(TO_VARCHAR(IFNULL(car."FechaEfectiva",'0001-01-01')),10) AS "FechaEfectivaInicial"
	, car."IdCondicionLiquidacion"					AS "IdCondicionLiquidacionInicial"
	, coalesce(CondicionLiquidacion."Nombre",'')	AS "NombreCondicionLiquidacionInicial"
	, LEFT(TO_VARCHAR(IFNULL(car."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacionInicial"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaOperacion",'0001-01-01')),10) AS "FechaOperacion"
	, LEFT(TO_VARCHAR(IFNULL(car."EGRFechaOperacion",'0001-01-01')),10) AS "EGRFechaOperacion"
	, LEFT(TO_VARCHAR(IFNULL(car."EGRFechaLiquidacion",'0001-01-01')),10) AS "EGRFechaLiquidacion"
	, car."INGCantidad"								AS "Cantidad"
	, car."INGPrecioCompra"							AS "PrecioCompra"
	, car."INGMontoCompraActualMonedaInstrumento"	AS "MontoCompraActualMonedaInstrumento"
	
	, car."INGPrecioPromedioCompra"					AS "PrecioPromedioCompra"
	, car."INGMontoPrecioPromedioCompra"			AS "MontoPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioMonedaInstrumento"	AS "MontoPrecioPromedioMonedaInstrumento"

	, car."INGTasaCompra"							AS "TasaCompra"
	, car."INGMontoATasaCompraActual"				AS "MontoATasaCompraActual"

	, car."INGValorRescate"							AS "ValorRescate"

	, car."INGPrecioAPlazo"							AS "PrecioAPlazo"
	, car."INGMontoAPlazo"							AS "MontoAPlazo"
	, car."INGMontoAPlazoMonedaInstrumento"			AS "MontoAPlazoMonedaInstrumento"

	, car."INGIdCondicionLiquidacion"				AS "IdCondicionLiquidacion"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacion"

	, car."INGDurationCompraActual"					AS "DurationCompraActual"
	, car."INGConvexidadCompraActual"				AS "ConvexidadCompraActual"

	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaPrecioTasaMercadoActual",'0001-01-01')),10) 	AS "FechaPrecioTasaMercadoActual"
	, car."INGFuenteTasaPrecioMercadoActual"		AS "FuenteTasaPrecioMercadoActual"
	, car."INGPrecioMercadoActual"					AS "PrecioMercadoActual"
	, car."INGTasaMercadoActual"					AS "TasaMercadoActual"
	, car."INGMontoATasaMercadoActual"				AS "MontoATasaMercadoActual"
	, car."INGMontoMercadoActualMonedaInstrumento"	AS "MontoMercadoActualMonedaInstrumento"
	, car."INGDurationMercadoActual"				AS "DurationMercadoActual"
	, car."INGConvexidadMercadoActual"				AS "ConvexidadMercadoActual"
	, (car."INGMontoATasaCompraActual" -  
		car."INGMontoATasaMercadoActual") 			AS "DiferenciaMercado"
--Tasa Corredora
	, car."INGTasaCorredora"						AS "TasaCorredora"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaTasaCorredora",'0001-01-01')),10) 	 AS "FechaTasaCorredora"
	, car."INGMontoATasaCorredora"					AS "MontoATasaCorredora"
	, car."INGMontoATasaCorredoraMonedaInstrumento"	AS "MontoATasaCorredoraMonedaInstrumento"
--Datos Cupon
	, TO_VARCHAR(IFNULL(car."INGFechaCorteCupon",'0001-01-01'),'YYYY-MM-DD')  AS "FechaCorteCupon"
	, car."INGNumeroCupon"							AS "NumeroCupon"
	, CASE WHEN car."INGIdMonedaPagoCupon" = '' THEN NULL ELSE car."INGIdMonedaPagoCupon"  END	AS "IdMonedaPagoCupon"
	, car."INGMontoCorteCupon"						AS "MontoCorteCupon"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaPagoCorteCupon",'0001-01-01')),10) 	AS "FechaPagoCorteCupon"
	, car."INGMontoCapitalCorteCupon"				AS "MontoCapitalCorteCupon"
	, car."INGMontoInteresCorteCupon"				AS "MontoInteresCorteCupon"
	, car."INGMontoReajusteCorteCupon"				AS "MontoReajusteCorteCupon"
	, car."INGMontoCorteCuponMonedaInstrumento"		AS "MontoCorteCuponMonedaInstrumento"
	, car."INGMontoCapitalCorteCuponMonedaInstrumento"		AS "MontoCapitalCorteCuponMonedaInstrumento"
	, car."INGMontoInteresCorteCuponMonedaInstrumento"		AS "MontoInteresCorteCuponMonedaInstrumento"
	, car."INGMontoReajusteCorteCuponMonedaInstrumento"		AS "MontoReajusteCorteCuponMonedaInstrumento"

--Tipo operacion de ingreso
		,CASE WHEN ifnull(car."IdConcepto",'') = '' THEN NULL ELSE ifnull(car."IdConcepto",'') END AS "IdConcepto" 
		,CAST(ifnull(Concepto."Nombre",'') AS NVARCHAR(100)) AS "NombreConcepto"
		,CASE WHEN car."IdTipoOperacion" = '' THEN NULL ELSE car."IdTipoOperacion" END AS "IdTipoOperacion" 
		,CAST(TipoOperacion."Nombre" AS NVARCHAR(100)) AS "NombreTipoOperacion"
		,CASE WHEN TipoOperacion."IdTipoProducto" = '' THEN NULL ELSE TipoOperacion."IdTipoProducto" END AS "IdTipoProductoTIPOPE" 
		,CASE WHEN "IdTipoComprobante" = '' THEN NULL ELSE "IdTipoComprobante" END AS "IdTipoComprobante" 

	, car."FolioComprobante"						AS "FolioComprobante"
	, LEFT(TO_VARCHAR(IFNULL(car."FechaComprobante",'0001-01-01')),10)  AS "FechaComprobante"


		,CASE WHEN "IdMesa" = 0 THEN NULL ELSE "IdMesa" END AS "IdMesa" 
		,CAST("CodigoIFRS" AS NVARCHAR(40)) AS "CodigoIFRS"
		,CASE WHEN car."IdBolsa" = '' THEN NULL ELSE car."IdBolsa" END AS "IdBolsa" 
		,CAST(Bolsa."Nombre" AS NVARCHAR(100)) AS "NombreBolsa"
		,CASE WHEN "IdSucursal" = 0 THEN NULL ELSE "IdSucursal" END AS "IdSucursal" 
		,CASE WHEN "IdAgente" = 0 THEN NULL ELSE "IdAgente" END AS "IdAgente" 
		,"FolioCarteraRelacionada"
		,CASE WHEN car."IdEstado" = '' THEN NULL ELSE car."IdEstado" END AS "IdEstado" 
		,CAST(IFNULL(Estado."Nombre",'') AS NVARCHAR(100)) AS "NombreEstado"
		,CASE WHEN car."IdTipoProducto" = '' THEN NULL ELSE car."IdTipoProducto" END AS "IdTipoProducto" 
		,CASE WHEN car."IdTipoCanalIngreso" = '' THEN NULL ELSE car."IdTipoCanalIngreso" END AS "IdTipoCanalIngreso" 
		,CASE WHEN car."IdMonedaSistema" = '' THEN NULL ELSE car."IdMonedaSistema" END AS "IdMonedaSistema" 
		,CAST(Mon1."Nombre"  AS NVARCHAR(100)) AS "MonedaSistema"
		,"UsaGarantiaEspecifica"
		,CASE WHEN car."IdUsuario" = 0 THEN NULL ELSE car."IdUsuario" END AS "IdUsuario" 
		,CAST(Usuario."CodigoUsuario" AS NVARCHAR(302)) AS "NombreUsuario"


	, car."Tamano"									AS "TamanoContrato"
	
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionActual",'0001-01-01')),10) AS "FechaValorizacionActual"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionAnterior",'0001-01-01')),10) AS "FechaValorizacionAnterior"
	, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionCartera",'0001-01-01')),10) AS "FechaValorizacionCartera"

--Datos FW
	,CASE WHEN "IdPlaza" = 0 THEN NULL ELSE "IdPlaza" 		END AS "IdPlaza" 
	,CASE WHEN "IdFixing" = 0 THEN NULL ELSE "IdFixing" 	END AS "IdFixing"
	, LEFT(TO_VARCHAR(IFNULL(car."FechaFixing",'0001-01-01')),10) AS "FechaFixing"
	, IFNULL(car."HoraFixing",'00:00:00') 			AS "HoraFixing"
	,CASE WHEN "IdModalidadCumplimiento" = 0 THEN NULL ELSE "IdModalidadCumplimiento" END AS "IdModalidadCumplimiento" 
	,CASE WHEN "IdReferenciaMercado" = 0 THEN NULL ELSE "IdReferenciaMercado" END AS "IdReferenciaMercado" 
	,CASE WHEN "IdMonedaPrincipal" = '' THEN NULL ELSE "IdMonedaPrincipal" END AS "IdMonedaPrincipal" 
	,CASE WHEN "IdMonedaSecundaria" = '' THEN NULL ELSE "IdMonedaSecundaria" END AS "IdMonedaSecundaria" 
	, car."ValorTipoCambioCierre"					AS "ValorTipoCambioCierre"
	, car."ValorParidadCierre"						AS "ValorParidadCierre"
	, car."ValorTipoCambioCosto"					AS "ValorTipoCambioCosto"
	, car."ValorParidadCosto"						AS "ValorParidadCosto"
	, car."MontoMonedaPrincipal"					AS "MontoMonedaPrincipal"
	, car."MontoMonedaSecundaria"					AS "MontoMonedaSecundaria"
	, car."INGEsPuntos"								AS "EsPuntos"
	, car."INGValorTipoCambioSpot"					AS "ValorTipoCambioSpot"
	, car."INGValorTipoCambioCotizador"				AS "ValorTipoCambioCotizador"
	, car."INGTasaPuntosMonedaPrincipal"			AS "TasaPuntosMonedaPrincipal"
	, car."INGTasaPuntosMonedaSecundaria"			AS "TasaPuntosMonedaSecundaria"
	, car."INGMontoMonedaPrincipal"					AS "INGMontoMonedaPrincipal"
	, car."INGMontoMonedaSecundaria"				AS "INGMontoMonedaSecundaria"
	, car."INGMontoDiferido"						AS "MontoDiferido"
	, car."INGMontoUtilidadDiferida"				AS "MontoUtilidadDiferida"
	, car."INGMontoPerdidaDiferida"					AS "MontoPerdidaDiferida"
	, car."INGMontoMargen"							AS "MontoMargen"
	, car."INGMontoValorRazonableActivo"			AS "MontoValorRazonableActivo"
	, car."INGMontoValorRazonablePasivo"			AS "MontoValorRazonablePasivo"
	, car."INGMontoValorRazonableNeto"				AS "MontoValorRazonableNeto"
	, car."INGTasaValorRazonableActivo"				AS "TasaValorRazonableActivo"
	, car."INGTasaValorRazonablePasivo"				AS "TasaValorRazonablePasivo"
	, car."INGMontoDevengoTipoCambio"				AS "MontoDevengoTipoCambio"
	, car."INGMontoDevengoTipoCambioAnterior"		AS "MontoDevengoTipoCambioAnterior"
	, car."INGMontoDiferidoDiario"					AS "MontoDiferidoDiario"
	, car."INGMontoDiferidoAcumulado"				AS "MontoDiferidoAcumulado"
	, car."INGMontoMatriz"							AS "MontoMatriz"
	, CLA."ValorClasificacionRiesgo"                AS "ClasificacionRiesgoInstrumento"

FROM "BSCL.Custodia.BD::CUS.Cartera" AS car 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONFIGURACION ON "CodigoParametro" = 'PLA.SegundosZonaHoraria' and CONFIGURACION."IdEmpresa" = Cli."IdEmpresa"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON car."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON car."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as Usuario ON car."IdUsuario" = Usuario."IdUsuario" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" as Modulo ON car."IdModulo" = Modulo."IdModulo" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = car."IdClasificacionTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON Mon1."IdMoneda" = car."IdMonedaSistema"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON Mon2."IdMoneda" = car."IdMonedaInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon3 ON Mon3."IdMoneda" = car."IdMonedaPrincipal"  
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon4 ON Mon4."IdMoneda" = car."IdMonedaInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA 
			ON (CLA."IdInstrumento" = car."IdInstrumento" AND CLA."IdEntidadClasificadoraRiesgo" ='BCS')					
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres on tpres."IdInstrumentoPrestamo" = car."IdInstrumento"
	   WHERE car."IdClasificacionTipoInstrumento" not in ('RV', 'IF','RF' ,'FM' )  AND car."IdEstado" = 'VIG';
   

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraOtrosProductosAnalitica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
