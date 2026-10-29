SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRFIFHisAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRFIFHisAnalitica" AS
SELECT
 	    CAST(TO_VARCHAR(car."FechaRespaldo",'YYYY-MM-DD')	AS NVARCHAR(10))							AS "FechaRespaldo"
 	    , car."IdEmpresa" 								AS "IdEmpresa"
		, car."IdCartera"								AS "IdCartera"
		, car."FolioCartera"							AS "FolioCartera"
		, CAST(TO_VARCHAR(IFNULL(car."FechaIngresoRegistro",'0001-01-01 00:00:00.0')) AS CHAR(27))   	    AS "FechaIngresoRegistro"
		, TO_VARCHAR(IFNULL(car."FechaModificacionRegistro",'0001-01-01 00:00:00.0'))    AS "FechaModificacionRegistro"
--Cliente
		, CASE WHEN car."IdCliente" = 0 THEN NULL ELSE car."IdCliente" END								AS "IdCliente"
		, IFNULL(cli."DocumentoIdentidad",'')			AS "DocumentoIdentidad"
		, CASE WHEN car."IdPortafolio" = 0 THEN NULL ELSE car."IdPortafolio" END							AS "IdPortafolio"
		, IFNULL(Port."CodigoPortafolio",'')			AS "CodigoPortafolio"
--Instrumento
		, CASE WHEN car."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE car."IdClasificacionTipoInstrumento" END			AS "IdClasificacionTipoInstrumento"
		, CASE WHEN car."IdTipoInstrumento" = '' THEN NULL ELSE car."IdTipoInstrumento" END						AS "IdTipoInstrumento"
		, coalesce(TipoInstrumento."Descripcion",'')	AS "TipoInstrumento"
		, CASE WHEN car."IdInstrumento" = 0 THEN NULL ELSE car."IdInstrumento" END							AS "IdInstrumento"
		,IFNULL(case
			when car."IdTipoInstrumento" = 'SIM' or car."IdTipoInstrumento" = 'PRT' or car."IdTipoInstrumento" = 'EMP' then
				sub."Nemotecnico"
			when car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
			else car."Nemotecnico"
		end, '') 										AS "Nemotecnico"
		, CASE WHEN car."IdMonedaInstrumento" = '' THEN NULL ELSE car."IdMonedaInstrumento" END						AS "IdMonedaInstrumento"
		, IFNULL(Mon2."Nombre",'') 						AS "MonedaInstrumento"
		
		, CASE
	  		WHEN ifnull(CAR."IdTipoReajuste", '') <> '' THEN CAR."IdTipoReajuste"
	  		WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN CAR."IdMonedaPrincipal"
	  		When ifnull(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento"
	  		ELSE 'CLP'
	   	  end AS "IdTipoReajuste"
		, CASE
	  		WHEN ifnull(TipoReajuste."Nombre", '') <> '' THEN coalesce(TipoReajuste."Nombre",'')
	  		WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN coalesce(Mon3."Nombre",'')
	  		ELSE ifnull(Mon4."Nombre",'')
	   	  end 											AS "TipoReajuste"
		, CASE WHEN RTRIM(car."IdEmisor") = '' THEN 'OPTIMUS' ELSE CAST(car."IdEmisor" AS NVARCHAR(40)) END								AS "IdEmisor"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaEmisionInstrumento",'0001-01-01')),10) AS "FechaEmisionInstrumento"
		, case 
			when car."IdTipoInstrumento" IN ('SIM','PRT','EMP') then LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10)
			else LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoInstrumento",'0001-01-01')),10)    
		 end 											AS "FechaVencimientoInstrumento"
		, car."EsFungible"								AS "EsFungible"
		, CASE WHEN RTRIM(car."IdBoveda") = '' THEN 'COR' ELSE car."IdBoveda" END								AS "IdBoveda"
	
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
		, LEFT(TO_VARCHAR(IFNULL(car."INGFechaCorteCupon",'0001-01-01')),10)  AS "FechaCorteCupon"
		, car."INGNumeroCupon"							AS "NumeroCupon"
		, CASE WHEN car."INGIdMonedaPagoCupon" = '' THEN NULL ELSE car."INGIdMonedaPagoCupon" END					AS "IdMonedaPagoCupon"
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
		, car."IdConcepto"					AS "IdConcepto"
		, ifnull(Concepto."Nombre",'')					AS "NombreConcepto"
		, CASE WHEN car."IdTipoOperacion" = '' THEN NULL ELSE car."IdTipoOperacion" END			AS "IdTipoOperacion"
		, TipoOperacion."Nombre" 						AS "NombreTipoOperacion"
		, CASE WHEN TipoOperacion."IdTipoProducto" = '' THEN NULL ELSE TipoOperacion."IdTipoProducto" END 				AS "IdTipoProductoTIPOPE"
	
		, CASE WHEN car."IdTipoComprobante" = '' THEN NULL ELSE car."IdTipoComprobante" END						AS "IdTipoComprobante"
		, car."FolioComprobante"						AS "FolioComprobante"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaComprobante",'0001-01-01')),10)  AS "FechaComprobante"
	
		, CASE WHEN car."IdMesa" = 0 THEN NULL ELSE car."IdMesa" END									AS "IdMesa"
		, car."CodigoIFRS"								AS "CodigoIFRS"
		, CASE WHEN car."IdBolsa" = '' THEN NULL ELSE car."IdBolsa" END									AS "IdBolsa"
		, CASE WHEN car."IdSucursal" = 0 THEN NULL ELSE car."IdSucursal" END								AS "IdSucursal"
		, CASE WHEN car."IdAgente" = 0 THEN NULL ELSE car."IdAgente" END								AS "IdAgente"
		, car."FolioCarteraRelacionada"					AS "FolioCarteraRelacionada"
		, CASE WHEN car."IdEstado" = '' THEN NULL ELSE car."IdEstado" END								AS "IdEstado"
		
		, CASE WHEN car."IdTipoProducto" = '' THEN NULL ELSE car."IdTipoProducto" END							AS "IdTipoProducto"
		, CASE WHEN car."IdTipoCanalIngreso" = '' THEN NULL ELSE car."IdTipoCanalIngreso" END						AS "IdTipoCanalIngreso"
		, CASE WHEN car."IdMonedaSistema" = '' THEN NULL ELSE car."IdMonedaSistema" END							AS "IdMonedaSistema"
		, Mon1."Nombre" 								AS "MonedaSistema"
	
		, car."UsaGarantiaEspecifica"					AS "UsaGarantiaEspecifica"
		, CASE WHEN car."IdUsuario" = 0 THEN NULL ELSE car."IdUsuario" END								AS "IdUsuario"
		, car."Tamano"									AS "TamanoContrato"
		
		, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionActual",'0001-01-01')),10) AS "FechaValorizacionActual"
		, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionAnterior",'0001-01-01')),10) AS "FechaValorizacionAnterior"
		, LEFT(TO_VARCHAR(IFNULL(car."INGFechaValorizacionCartera",'0001-01-01')),10) AS "FechaValorizacionCartera"
	
--Datos FW
		, CASE WHEN car."IdPlaza" = 0 THEN NULL ELSE car."IdPlaza" END									AS "IdPlaza"
		, CASE WHEN car."IdModalidadCumplimiento" = 0 THEN NULL ELSE car."IdModalidadCumplimiento" END					AS "IdModalidadCumplimiento"
		, CASE WHEN car."IdReferenciaMercado" = 0 THEN NULL ELSE car."IdReferenciaMercado" END						AS "IdReferenciaMercado"
		, CASE WHEN car."IdMonedaPrincipal" = '' THEN NULL ELSE car."IdMonedaPrincipal" END						AS "IdMonedaPrincipal"
		, CASE WHEN car."IdMonedaSecundaria" = '' THEN NULL ELSE car."IdMonedaSecundaria" END						AS "IdMonedaSecundaria"
		, car."ValorTipoCambioCierre"					AS "ValorTipoCambioCierre"
		, car."ValorParidadCierre"						AS "ValorParidadCierre"
		, car."ValorTipoCambioCosto"					AS "ValorTipoCambioCosto"
		, car."ValorParidadCosto"						AS "ValorParidadCosto"
		, car."MontoMonedaPrincipal"					AS "MontoMonedaPrincipal"
		, car."MontoMonedaSecundaria"					AS "MontoMonedaSecundaria"
		, car."INGValorTipoCambioCotizador"				AS "ValorTipoCambioCotizador"
		, car."INGTasaPuntosMonedaPrincipal"			AS "TasaPuntosMonedaPrincipal"
		, car."INGTasaPuntosMonedaSecundaria"			AS "TasaPuntosMonedaSecundaria"
		, car."INGMontoMonedaPrincipal"					AS "INGMontoMonedaPrincipal"
		, car."INGMontoMonedaSecundaria"				AS "INGMontoMonedaSecundaria"
		, car."INGMontoDiferido"						AS "MontoDiferido"
		, car."INGMontoUtilidadDiferida"				AS "MontoUtilidadDiferida"
		, car."INGMontoPerdidaDiferida"					AS "MontoPerdidaDiferida"
		, car."INGMontoMargen"							AS "MontoMargen"
		, car."INGMontoDiferidoDiario"					AS "MontoDiferidoDiario"
		, car."INGMontoDiferidoAcumulado"				AS "MontoDiferidoAcumulado"
		, car."INGMontoMatriz"							AS "MontoMatriz"
		, CAST(COALESCE(CLA."ValorClasificacionRiesgo",'') AS NVARCHAR(20))                AS "ClasificacionRiesgoInstrumento"
	
	FROM "BSCL.Custodia.BD::HIS.Cartera" AS car
		LEFT JOIN (
			select "Nemotecnico","FolioCarteraRelacionada", rank() over (partition by "FolioCarteraRelacionada" order by "IdSubyacente" asc)  as "NroFila" 
			from "BSCL.Custodia.BD::HIS.Subyacente" as miSub
		) AS Sub on Sub."FolioCarteraRelacionada" = car."FolioCartera" and Sub."NroFila" = 1
		
		
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
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

		WHERE car."IdClasificacionTipoInstrumento" in ('RF' , 'IF')  AND car."IdEstado" = 'VIG'
		
UNION ALL 
SELECT 
		 CAST(TO_VARCHAR(car."FechaRespaldo",'YYYY-MM-DD')	AS NVARCHAR(10))	AS "FechaRespaldo"
		, car."IdEmpresa" 								AS "IdEmpresa"
		, car."IdCarteraNegativa"						AS "IdCartera"
		, car."IdCarteraNegativa"						AS "FolioCartera"
		, TO_VARCHAR(IFNULL(car."FechaIngresoRegistro",'0001-01-01 00:00:00.0'))   	    AS "FechaIngresoRegistro"
		, '0001-01-01 00:00:00.0'    AS "FechaModificacionRegistro"
		, car."IdCliente"								AS "IdCliente"
		, IFNULL(cli."DocumentoIdentidad",'')			AS "DocumentoIdentidad"
		, car."IdPortafolio"							AS "IdPortafolio"
		, IFNULL(Port."CodigoPortafolio",'')			AS "CodigoPortafolio"
		, ClasInst."IdClasificacionTipoInstrumento"		AS "IdClasificacionTipoInstrumento"
		, TipoInstrumento."IdTipoInstrumento"			AS "IdTipoInstrumento"
		, coalesce(TipoInstrumento."Descripcion",'')	AS "TipoInstrumento"
		, car."IdInstrumento"							AS "IdInstrumento"
		, IFNULL(case
           		 	when ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
            		else car."Nemotecnico"
        		end, '')                                        AS "Nemotecnico"
		, car."IdMonedaInstrumento"						AS "IdMonedaInstrumento"
		, IFNULL(Mon2."Nombre",'') 						AS "MonedaInstrumento"
		, CASE
	  		WHEN ifnull(CAR."IdTipoReajuste", '') <> '' THEN CAR."IdTipoReajuste"
	  		ELSE ''
	   	  end 											AS "IdTipoReajuste"
		, CASE
	  		WHEN ifnull(TipoReajuste."Nombre", '') <> '' THEN coalesce(TipoReajuste."Nombre",'')
	  		ELSE ifnull(Mon2."Nombre",'')
	   	  end 											AS "TipoReajuste"
	   	, car."IdEmisor"								AS "IdEmisor"  
	   	, LEFT(TO_VARCHAR(IFNULL(Instrumento."FechaEmision",'0001-01-01')),10) AS "FechaEmisionInstrumento"
	   	, case 
            when TipoInstrumento."IdTipoInstrumento" IN ('SIM','PRT','EMP') then LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10)
            else LEFT(TO_VARCHAR(IFNULL(Instrumento."FechaVencimiento",'0001-01-01')),10)    
         end                                            AS "FechaVencimientoInstrumento"
		, car."EsFungible"								AS "EsFungible"
		, car."IdBoveda"								AS "IdBoveda"  
	   	, LEFT(TO_VARCHAR(IFNULL(car."FechaOperacion",'0001-01-01')),10) AS "FechaOperacionInicial"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10) AS "FechaVencimientoOperacionInicial"  
	   	, case
			when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
			else 0
			end 										AS "PlazoEnDiasOperacionInicial" 
	   	, -1*car."Cantidad"								AS "CantidadInicial" 
		, car."PrecioOperacion"							AS "PrecioCompraInicial"
		, car."TasaOperacion"							AS "TasaCompraInicial"
		, car."CorteMinimo"								AS "CorteMinimoInicial"
		, car."ValorRescate"							AS "ValorRescateInicial"
		, car."MontoMercadoMonedaInstrumento"			AS "MontoCompraInicial"
		, car."MontoOperacionMonedaInstrumento"			AS "MontoCompraMonedaInstrumentoInicial"
		, 0												AS "PrecioAPlazoInicial"
		, 0												AS "MontoAPlazoInicial"
		, 0												AS "MontoAPlazoMonedaInstrumentoInicial"
		, '0001-01-01' AS "FechaEfectivaInicial"
		, car."IdCondicionLiquidacion"					AS "IdCondicionLiquidacionInicial"
		, coalesce(CondicionLiquidacion."Nombre",'')	AS "NombreCondicionLiquidacionInicial"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacionInicial"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaOperacion",'0001-01-01')),10) AS "FechaOperacion"
		, '0001-01-01' 									AS "EGRFechaOperacion"
		, '0001-01-01' 									AS "EGRFechaLiquidacion"
		, -1*car."Cantidad"								AS "Cantidad"
		, car."PrecioOperacion"							AS "PrecioCompra"
		, car."MontoOperacionMonedaInstrumento"			AS "MontoCompraActualMonedaInstrumento"
		, 0							AS "PrecioPromedioCompra"
		, 0							AS "MontoPrecioPromedioCompra"
		, 0							AS "MontoPrecioPromedioMonedaInstrumento"
		, car."TasaOperacion"							AS "TasaCompra"
		, car."MontoOperacionMonedaInstrumento"							AS "MontoATasaCompraActual"
		, car."ValorRescate"		AS "ValorRescate"
		, 0							AS "PrecioAPlazo"
		, 0							AS "MontoAPlazo"
		, 0							AS "MontoAPlazoMonedaInstrumento"
		, car."IdCondicionLiquidacion"				AS "IdCondicionLiquidacion"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacion"
		, 0							AS "DurationCompraActual"
		, 0							AS "ConvexidadCompraActual"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaPrecioTasa",'0001-01-01')),10) 	AS "FechaPrecioTasaMercadoActual"
		, ''						AS "FuenteTasaPrecioMercadoActual"
		, car."PrecioMercado"					AS "PrecioMercadoActual"
		, car."TasaMercado"						AS "TasaMercadoActual"
		, car."MontoMercadoMonedaInstrumento"	AS "MontoATasaMercadoActual"
		, car."MontoMercadoMonedaInstrumento"	AS "MontoMercadoActualMonedaInstrumento"
		, 0				AS "DurationMercadoActual"
		, 0				AS "ConvexidadMercadoActual"
		, 0 			AS "DiferenciaMercado"
		--Tasa Corredora
		, 0					AS "TasaCorredora"
		, '0001-01-01'	 	AS "FechaTasaCorredora"
		, 0					AS "MontoATasaCorredora"
		, 0					AS "MontoATasaCorredoraMonedaInstrumento"
		--Datos Cupon
		, LEFT(TO_VARCHAR(IFNULL(car."FechaCorteCupon",'0001-01-01')),10)  AS "FechaCorteCupon"
		, car."NumeroCupon"							AS "NumeroCupon"
		, car."IdMonedaPagoCupon"					AS "IdMonedaPagoCupon"
		, car."MontoCorteCupon"						AS "MontoCorteCupon"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaPagoCorteCupon",'0001-01-01')),10) 	AS "FechaPagoCorteCupon"
		, car."MontoCapitalCorteCupon"				AS "MontoCapitalCorteCupon"
		, car."MontoInteresCorteCupon"				AS "MontoInteresCorteCupon"
		, car."MontoReajusteCorteCupon"				AS "MontoReajusteCorteCupon"
		, car."MontoCorteCuponMonedaInstrumento"		AS "MontoCorteCuponMonedaInstrumento"
		, car."MontoCapitalCorteCuponMonedaInstrumento"		AS "MontoCapitalCorteCuponMonedaInstrumento"
		, car."MontoInteresCorteCupon"		AS "MontoInteresCorteCuponMonedaInstrumento"
		, 0		AS "MontoReajusteCorteCuponMonedaInstrumento"
		--Tipo operacion de ingreso
		, ifnull(car."IdConcepto",'')					AS "IdConcepto"
		, ifnull(Concepto."Nombre",'')					AS "NombreConcepto"
		, car."IdTipoOperacion"							AS "IdTipoOperacion"
		, TipoOperacion."Nombre" 						AS "NombreTipoOperacion"
		, TipoOperacion."IdTipoProducto" 				AS "IdTipoProductoTIPOPE"
		, car."IdTipoComprobante"						AS "IdTipoComprobante"
		, car."FolioComprobante"						AS "FolioComprobante"
		, '0001-01-01'  								AS "FechaComprobante"
		, car."IdMesa"									AS "IdMesa"
		, car."CodigoIFRS"								AS "CodigoIFRS"
		, car."IdBolsa"									AS "IdBolsa"
		, car."IdSucursal"								AS "IdSucursal"
		, car."IdAgente"								AS "IdAgente"
		, 0												AS "FolioCarteraRelacionada"
		, car."IdEstado"								AS "IdEstado"
		, car."IdTipoProducto"							AS "IdTipoProducto"
		, car."IdTipoCanalIngreso"						AS "IdTipoCanalIngreso"
		, CASE WHEN cfms."Valor" = '' THEN NULL ELSE cfms."Valor" END AS "IdMonedaSistema"
		, Mon1."Nombre" 								AS "MonedaSistema"
		, ''											AS "UsaGarantiaEspecifica"
		, car."IdUsuarioIngreso"						AS "IdUsuario"
		, 0												AS "TamanoContrato"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaValorizacion",'0001-01-01')),10) AS "FechaValorizacionActual"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaValorizacion",'0001-01-01')),10) AS "FechaValorizacionAnterior"
		, LEFT(TO_VARCHAR(IFNULL(car."FechaValorizacion",'0001-01-01')),10) AS "FechaValorizacionCartera"
		--Datos FW
        , 0                                AS "IdPlaza"
		, 0                 	AS "IdModalidadCumplimiento"
        , 0                   	 AS "IdReferenciaMercado"
        , 0                      AS "IdMonedaPrincipal"
        , 0                      AS "IdMonedaSecundaria"
        , 0                   	AS "ValorTipoCambioCierre"
        , 0                      AS "ValorParidadCierre"
        , 0                    	AS "ValorTipoCambioCosto"
		, 0                       AS "ValorParidadCosto"
        , 0                    AS "MontoMonedaPrincipal"
        , 0                   AS "MontoMonedaSecundaria"
        , 0             AS "ValorTipoCambioCotizador"
		, 0            AS "TasaPuntosMonedaPrincipal"
        , 0           AS "TasaPuntosMonedaSecundaria"
        , 0                AS "INGMontoMonedaPrincipal"
        , 0                AS "INGMontoMonedaSecundaria"
        , 0                        AS "MontoDiferido"
        , 0               AS "MontoUtilidadDiferida"
        , 0                 AS "MontoPerdidaDiferida"
        , 0                          AS "MontoMargen"
        , 0                  AS "MontoDiferidoDiario"
        , 0              AS "MontoDiferidoAcumulado"
        , 0                         AS "MontoMatriz"
        , CLA."ValorClasificacionRiesgo"                AS "ClasificacionRiesgoInstrumento"
	FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" AS car 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON car."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as Usuario ON car."IdUsuarioIngreso" = Usuario."IdUsuario" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" cfms on cfms."IdEmpresa" = car."IdEmpresa" AND cfms."CodigoParametro" = 'PLA.MonedaSistema'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON Mon1."IdMoneda" = cfms."Valor"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON Mon2."IdMoneda" = car."IdMonedaInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA 
			ON (CLA."IdInstrumento" = car."IdInstrumento" AND CLA."IdEntidadClasificadoraRiesgo" ='BCS')					
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres on tpres."IdInstrumentoPrestamo" = car."IdInstrumento"
	WHERE ClasInst."IdClasificacionTipoInstrumento" in ('RF' , 'IF')  AND car."IdEstado" = 'VIG';					  			  

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRFIFHisAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;