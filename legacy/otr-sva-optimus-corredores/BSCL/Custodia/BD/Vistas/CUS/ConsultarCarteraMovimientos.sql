SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraMovimientos";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraMovimientos"  AS 
	 
SELECT
	 car."IdCartera",
	 car."IdEmpresa",
	 Empresa."Nombre" AS "NombreEmpresa",
	 car."FolioCartera",
	 car."FolioCarteraRelacionada",
	 car."IdEstado",
	 car."IdMonedaSistema",
	 Mon1."Nombre" AS "MonedaSistema",
	 car."IdMonedaInstrumento",
	 Mon2."Nombre" AS "MonedaInstrumento",
	 Estado."Nombre" AS "NombreEstado",
	 car."IdMovimientoCarteraEnTransito",
	 car."IdMesa",
	 car."IdOperacion",
	 car."IdTipoOperacion",
	 TipoOperacion."Nombre" AS "NombreTipoOperacion",
	 car."IdBoveda",
	 Boveda."Nombre" AS "NombreBoveda",
	 car."IdTipoReajuste",
	 TipoReajuste."Nombre" AS "NombreTipoReajuste",
	 car."IdTipoProducto",
	 car."IdSucursal",
	 car."IdAgente",
	 car."IdBolsa",
	 Bolsa."Nombre" AS "NombreBolsa",
	 car."IdConcepto",
	 Concepto."Nombre" as "NombreConcepto",
	 car."IdCondicionLiquidacion",
	 CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion",
	 car."IdModulo",
	 Modulo."Nombre" AS "NombreModulo",
	 car."IdCliente" AS "IdCliente",
	 cli."DocumentoIdentidad" AS "DocumentoIdentidad",
	 car."IdPortafolio",
	 Port."CodigoPortafolio",
	 cli."RazonSocial" AS "RazonSocial",
--	 car."Nemotecnico",

	case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','RRV','RRF') then
			--(select distinct "Nemotecnico" from "BSCL.Custodia.BD::CUS.Subyacente" as Suby where (car."IdEmpresa" = Suby."IdEmpresa" and car."FolioCartera" = Suby."FolioCarteraRelacionada"))
			(select "Nemotecnico" 
			from (
					select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
					from "BSCL.Custodia.BD::CUS.Subyacente" as miSub
					where car."FolioCartera" = miSub."FolioCarteraRelacionada"
				 ) as Suby
			where Suby."NroFila" = 1
			)

		else car."Nemotecnico"
	end as "Nemotecnico",

	 car."NemoSVS", 
	 car."IdInstrumento",
	 car."IdClasificacionTipoInstrumento",
	 ClasInst."Nombre" as "ClasificacionTipoInstrumento",
	 car."IdTipoInstrumento",
	 TipoInstrumento."Descripcion" AS "TipoInstrumento",
	 car."IdEmisor",
--	 Emisor."Nombre" AS "NombreEmisor",
	 car."IdMonedaEmision",
	 car."IdModalidadCumplimiento",
	 car."FechaOperacion",
	 car."Cantidad",
	 car."ValorRescate",
	 car."CorteMinimo",
	 car."PrecioCompra",
	 car."MontoCompra",
	 car."MontoCompraMonedaInstrumento",
	 car."TasaCompra",
	 car."MontoAPlazo",
	 car."PrecioAPlazo",

	 car."FechaVencimientoOperacion",
	 car."FechaEmisionInstrumento",
	 --car."FechaVencimientoInstrumento",
	 case 
	 	when car."IdTipoInstrumento" in ('PRT','EMP') then car."FechaVencimientoOperacion"
	 	else car."FechaVencimientoInstrumento"
	 end as "FechaVencimientoInstrumento",
	 
	 case
	 	when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
	 	when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
	 	else 0
	 end as "PlazoEnDiasOperacion", 
	 car."EsFungible",
	 car."CodigoIFRS",
	 car."FechaIngresoRegistro",
	 car."FechaModificacionRegistro",
	 car."TasaAgente",
	 car."EsIntraday",
	 car."FechaLiquidacion",
	 CASE car."FechaLiquidacion" 
		 WHEN '0001-01-01' THEN '' 
			ELSE TO_NVARCHAR ( car."FechaLiquidacion", 'DD-MM-YYYY' ) 
			END AS "FechaLiquidacionStr" ,
	 car."CobraPrimaAlInicio",
	 car."IdTipoCanalIngreso",
	 car."IdTipoComprobante",
	 car."FolioComprobante",
	 car."FechaComprobante",

	 
	 (car."INGCantidad" - car."EGRCantidad") as "Saldo"
	 
	,car."FechaEfectiva"			
	,car."IdPlaza"				
	,car."IdFixing"				
	,car."IdReferenciaMercado"	
	,car."IdMonedaPrincipal"		
	,car."IdMonedaSecundaria"	
	,car."ValorTipoCambioCierre"	
	,car."ValorParidadCierre"	
	,car."ValorTipoCambioCosto"	
	,car."ValorParidadCosto",
	 
	 
	 
	 car."INGGlosa"		   				 		,
	 car."INGFechaOperacion" 					,
	 car."INGCantidad"							,
	 car."INGValorRescate" 						,
	 car."INGTasaAgente" 						,
	 car."INGMontoAPlazo" 						,
	 car."INGPrecioAPlazo" 						,

	 car."INGPrecioCompra"						,
	 car."INGTasaCompra" 						,

	 car."INGFechaPrecioTasaMercadoActual"		,	 
	 car."INGPrecioMercadoActual"				,
	 car."INGTasaMercadoActual" 				,
	 car."INGMontoATasaMercadoActual" 			,

	 car."INGTasaCorredora" 					,
	 car."INGFechaTasaCorredora" 				,
	 car."INGMontoATasaCorredora" 				,
	 
	 car."INGMontoCorteCupon"					,
	 car."INGFechaCorteCupon"					
	,car."INGIdMonedaPagoCupon"					
	,car."INGFechaPagoCorteCupon"				
	,car."INGNumeroCupon"						
	,car."INGMontoCapitalCorteCupon"				
	,car."INGMontoInteresCorteCupon"				
	,car."INGMontoReajusteCorteCupon"			
	

	,car."INGMontoMonedaPrincipal"				
	,car."INGMontoMonedaSecundaria"				
	,car."INGMontoDiferido"						
	,car."INGMontoUtilidadDiferida"				
	,car."INGMontoPerdidaDiferida"				
	,car."INGMontoMargen"						
	,car."INGMontoValorRazonableActivo"			
	,car."INGMontoValorRazonablePasivo"			
	,car."INGMontoValorRazonableNeto"			
	,car."INGTasaValorRazonableActivo"			
	,car."INGTasaValorRazonablePasivo"			
	,car."INGMontoDevengoTipoCambio"				
	,car."INGMontoDevengoTipoCambioAnterior"		
	,car."INGMontoDiferidoDiario"				
	,car."INGMontoDiferidoAcumulado"				
	,car."INGValorTipoCambioSpot"				
	,car."INGValorTipoCambioCotizador"			
	,car."INGEsPuntos"							
	,car."INGTasaPuntosMonedaPrincipal"			
	,car."INGTasaPuntosMonedaSecundaria"			
	,car."INGMontoMatriz"
	,car."INGPrecioPromedioCompra"
	,car."INGMontoPrecioPromedioCompra"
	,car."INGMontoPrecioPromedioMonedaInstrumento",

	 -- ***EGRESO***	                        ,
	car."EGRGlosa",
	car."EGRFechaOperacion",
	car."EGRIdTipoOperacion",
	car."EGRIdOperacion",
	car."EGRIdMovimientoCarteraEnTransito",
	car."EGRIdTipoCanalIngreso",
	car."EGRIdCondicionLiquidacion",
	car."EGRFechaLiquidacion",
	car."EGRIdTipoComprobante",
	car."EGRFolioComprobante",
	car."EGRFechaComprobante",
	car."EGRFolioExternoReferencia",
	car."EGRIdModulo",
	car."EGRIdConcepto",
	car."EGRIdUsuario",
	car."EGRIdSucursal",
	car."EGRIdAgente",
	car."EGRFechaCurseRetiro",
	car."EGRCostoRetiroenFactorAjuste",
	car."EGRValorFactorAjusteaFechaRetiro",
	car."EGRCantidad",
	car."EGRValorRescate",
	car."EGRPrecio",
	car."EGRTasa",
	car."EGRMonto",
	car."EGRMontoMonedaInstrumento",
	car."EGRSpread",
	car."EGRPrecioAPlazo",
	car."EGRMontoAPlazo",
	car."EGRMontoAPlazoMonedaInstrumento",
	car."EGRMontoPresenteTIF",
	car."EGRMontoPresenteTIFMonedaInstrumento",
	car."EGRMontoPresenteTIFMonedaAdicional",
	car."EGRIdSpotAsociado",
	car."EGRMontoMonedaPrincipal",
	car."EGRMontoMonedaSecundaria",
	car."EGRIdMonedaLiquidacion",
	car."EGRMontoLiquidacion",
	car."EGRMontoLiquidacionMonedaPrincipal",
	car."EGRValorTipoCambioSpot",
	car."EGRValorParidad",
	car."EGRValorTipoCambioCotizador",
	car."EGREsPuntos",
	car."EGRTasaPuntosMonedaPrincipal",
	car."EGRTasaPuntosMonedaSecundaria",
	 

	 car."IdUsuario",
	 Usuario."CodigoUsuario" as "NombreUsuario",

	 case 
	 	when coalesce(car."EGRIdConcepto",'') <> '' then coalesce(car."EGRIdConcepto",'')
	 	else coalesce(car."IdConcepto",'')
	 end as "IdConceptoGrilla",
	 
	 case 
	 	when coalesce(car."EGRIdConcepto",'') <> '' then coalesce(ConceptoEGR."Nombre",'')
	 	else coalesce(Concepto."Nombre",'')
	 end as "NombreConceptoGrilla"
	 
	 
	 
FROM "BSCL.Custodia.BD::CUS.Cartera" AS car 
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
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = Instrumento."IdClasificacionTipoInstrumento" 
--	LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" as Emisor ON car."IdEmisor" = Emisor."IdEmisor" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON car."IdMonedaSistema" = Mon1."IdMoneda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON car."IdMonedaInstrumento" = Mon2."IdMoneda" 
WHERE
	(car."EGRCantidad" > 0) or (car."INGCantidad" = car."Cantidad")
ORDER BY "FolioCartera", "IdCartera" 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraMovimientos"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
