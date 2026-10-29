SET SCHEMA "BSCL";
GO


DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSaldos";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSaldos" AS
SELECT
	car."FechaRespaldo",
	Empresa."Nombre" AS "NombreEmpresa",
	Mon1."Nombre" AS "Moneda",
	Mon2."Nombre" AS "MonedaInstrumento",
	Estado."Nombre" AS "NombreEstado",
	TipoOperacion."Nombre" AS "NombreTipoOperacion",	
	Boveda."Nombre" AS "NombreBoveda",
	TipoReajuste."Nombre" AS "NombreTipoReajuste",
	(  ( TipoReajuste."IdTipoReajuste" || ' - ' )  || TipoReajuste."Nombre" )  AS "NombreLargoTipoReajuste",
	Bolsa."Nombre" AS "NombreBolsa",
	Concepto."Nombre" AS "NombreConcepto",
	CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion",
--	Modulo."Nombre" AS "NombreModulo",
	cli."DocumentoIdentidad",
	Port."CodigoPortafolio",
	Port."Proposito" as "PropositoPortafolio",
	cli."NombreCompleto" AS "RazonSocial",
	case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','RRV','RRF') then
			--(select distinct "Nemotecnico" from "BSCL.Custodia.BD::CUS.Subyacente" as Suby where (car."IdEmpresa" = Suby."IdEmpresa" and car."FolioCartera" = Suby."FolioCarteraRelacionada"))
			(select "Nemotecnico" 
			from (
					select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
					from "BSCL.Custodia.BD::HIS.Subyacente" as miSub
					where car."FolioCartera" = miSub."FolioCarteraRelacionada"
						AND car."FechaRespaldo" = miSub."FechaRespaldo"
				 ) as Suby
			where Suby."NroFila" = 1
			)

		else car."Nemotecnico"
	end as "Nemotecnico",
--	Riesgo."ClasificacionRiesgo",
--	Ins."ValorClasificacionRiesgo",
--	Ins."Reajustable",
	ClasInst."Nombre" as "ClasificacionTipoInstrumento",
	TipoInstrumento."Descripcion" AS "TipoInstrumento",
	car."IdMonedaEmision" AS "EsPagaderoEn",
	car."IdMonedaEmision",
--	Emisor."Nombre" AS "NombreEmisor",
	(CASE
		when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
		WHEN car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
		ELSE 0
	END) AS "PlazoEnDiasOperacion", 
	tpres."IdTipoPrestamo",
	tpres."MinimoDiasAnticipo",
	tpres."MaximoDiasPrestamo",
	case when IFNULL(tpres."IdTipoPrestamo",'') <> '' then car."Nemotecnico" else '' end as "NombreTipoPrestamo",
	tpres."EsDiaHabilPlazoPrestamo",
	tpres."EsDiaHabilParaCalculoInteres",
	Usuario."CodigoUsuario" as "NombreUsuario"
	
	
	, car."IdCartera"
	, car."FolioCartera"
	, car."FolioCarteraRelacionada"
	, car."FechaIngresoRegistro"
	, car."FechaModificacionRegistro"
	, car."IdEmpresa"
	, car."FechaOperacion"
	, car."FechaVencimientoOperacion"
	, car."IdCliente"
	, car."IdPortafolio"
	, car."IdClienteRelacionado"
	, car."IdPortafolioRelacionado"
	, car."IdEstado"
	, car."IdTipoProducto"
	, car."IdTipoCanalIngreso"
	, car."IdMonedaSistema"
	, car."IdConcepto"
	, car."IdTipoOperacion"
	, car."IdOperacion"
	, car."IdTipoComprobante"
	, car."FolioComprobante"
	, car."FechaComprobante"
	, car."FolioExternoReferencia" as "FolioEntidad"
	, car."IdMesa"
	, car."IdBolsa"
	, car."IdSucursal"
	, car."IdAgente"
	, car."IdCondicionLiquidacion"
	, car."FechaLiquidacion"
	, car."UsaGarantiaEspecifica"
	, car."CobraPrimaAlInicio"
	, car."Glosa"
	, car."IdUsuario"
	, car."IdModulo"
	, car."ValorFactorAjusteaFechaCurse"
	, car."CostoInversionEnFactorAjuste"
	, car."IdClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, car."IdInstrumento"
	, car."IdEmisor"
	, car."NemoSVS"
	, car."IdTipoReajuste"
	, car."FechaEmisionInstrumento"
	, car."FechaVencimientoInstrumento"
	, car."EsFungible"
	, car."CodigoIFRS"
	, car."IdBoveda"
	, car."IdMonedaInstrumento"
	, car."Tamano"
	, car."Cantidad"
	, car."CorteMinimo"
	, car."ValorRescate"
	, car."PrecioCompra"
	, car."TasaCompra"
	, car."MontoCompra"
	, car."MontoCompraMonedaInstrumento"
	, car."PrecioAPlazo"
	, car."MontoAPlazo"
	, car."MontoAPlazoMonedaInstrumento"
	, car."FechaEfectiva"
	, car."IdPlaza"
	, car."IdFixing"
	, car."FechaFixing"
	, car."HoraFixing"
	, car."IdModalidadCumplimiento"
	, car."IdReferenciaMercado"
	, car."IdMonedaPrincipal"
	, car."IdMonedaSecundaria"
	, car."ValorTipoCambioCierre"
	, car."ValorParidadCierre"
	, car."ValorTipoCambioCosto"
	, car."ValorParidadCosto"
	, car."MontoMonedaPrincipal"
	, car."MontoMonedaSecundaria"
	, car."IdCarga"
	, car."INGGlosa"
	, car."INGTasaAgente"
	, car."INGFechaOperacion"
	, car."INGIdCondicionLiquidacion"
	, car."INGFechaLiquidacion"
	, (car."INGCantidad" - COALESCE(resTot."IngCantidad",0)) AS "INGCantidad"
	, (car."INGValorRescate" - COALESCE(resTot."IngValorRescate",0)) AS "INGValorRescate"
	, car."INGPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioMonedaInstrumento"
	, car."INGPrecioAPlazo"
	, car."INGMontoAPlazo"
	, car."INGMontoAPlazoMonedaInstrumento"
	, car."INGEsPuntos"
	, car."INGValorTipoCambioSpot"
	, car."INGValorTipoCambioCotizador"
	, car."INGTasaPuntosMonedaPrincipal"
	, car."INGTasaPuntosMonedaSecundaria"
	, car."INGFechaValorizacionCartera"
	, car."INGFechaValorizacionAnterior"
	, car."INGFechaValorizacionActual"
	, case when ifnull(car."INGPrecioCompra",0) = 0 then car."PrecioCompra" else ifnull(car."INGPrecioCompra",0) end as "INGPrecioCompra"
	, car."INGTasaCompra"
	, car."INGMontoATasaCompraAnterior"
	, car."INGMontoInteresCompraAnterior"
	, car."INGMontoCompraAnteriorMonedaInstrumento"
	, car."INGMontoInteresCompraAnteriorMonedaInstrumento"
	, car."INGMontoReajusteCompraAnteriorMonedaInstrumento"
	, (car."INGMontoATasaCompraActual" - COALESCE(resTot."IngMontoValorizado",0)) AS "INGMontoATasaCompraActual"
	, car."INGMontoCompraActualMonedaInstrumento"
	, car."INGDurationCompraActual"
	, car."INGConvexidadCompraActual"
	, car."INGMontoATasaCompraProximo"
	, car."INGMontoInteresCompraProximo"
	, car."INGMontoReajusteCompraProximo"
	, car."INGMontoCompraProximoMonedaInstrumento"
	, car."INGMontoInteresCompraProximoMonedaInstrumento"
	, car."INGMontoReajusteCompraProximoMonedaInstrumento"
	, car."INGPrecioMercadoAnterior"
	, car."INGTasaMercadoAnterior"
	, car."INGMontoATasaMercadoAnterior"
	, car."INGMontoInteresMercadoAnterior"
	, car."INGMontoMercadoAnteriorMonedaInstrumento"
	, car."INGMontoInteresMercadoAnteriorMonedaInstrumento"
	, car."INGFechaPrecioTasaMercadoActual"
	, car."INGFuenteTasaPrecioMercadoActual"
	, car."INGPrecioMercadoActual"
	, car."INGTasaMercadoActual"
	, car."INGMontoATasaMercadoActual"
	, car."INGMontoMercadoActualMonedaInstrumento"
	, car."INGDurationMercadoActual"
	, car."INGConvexidadMercadoActual"
	, car."INGPrecioMercadoProximo"
	, car."INGTasaMercadoProximo"
	, car."INGMontoATasaMercadoProximo"
	, car."INGMontoInteresMercadoProximo"
	, car."INGMontoReajusteMercadoProximo"
	, car."INGMontoMercadoProximoMonedaInstrumento"
	, car."INGMontoInteresMercadoProximoMonedaInstrumento"
	, car."INGTasaCorredora"
	, car."INGFechaTasaCorredora"
	, car."INGMontoATasaCorredora"
	, car."INGMontoATasaCorredoraMonedaInstrumento"
	, car."INGFechaCorteCupon"
	, car."INGNumeroCupon"
	, car."INGIdMonedaPagoCupon"
	, car."INGMontoCorteCupon"
	, car."INGFechaPagoCorteCupon"
	, car."INGMontoCapitalCorteCupon"
	, car."INGMontoInteresCorteCupon"
	, car."INGMontoReajusteCorteCupon"
	, car."INGMontoCorteCuponMonedaInstrumento"
	, car."INGMontoCapitalCorteCuponMonedaInstrumento"
	, car."INGMontoInteresCorteCuponMonedaInstrumento"
	, car."INGMontoReajusteCorteCuponMonedaInstrumento"
	, car."INGMontoMonedaPrincipal"
	, car."INGMontoMonedaSecundaria"
	, car."INGMontoDiferido"
	, car."INGMontoUtilidadDiferida"
	, car."INGMontoPerdidaDiferida"
	, car."INGMontoMargen"
	, car."INGMontoValorRazonableActivo"
	, car."INGMontoValorRazonablePasivo"
	, car."INGMontoValorRazonableNeto"
	, car."INGTasaValorRazonableActivo"
	, car."INGTasaValorRazonablePasivo"
	, car."INGMontoDevengoTipoCambio"
	, car."INGMontoDevengoTipoCambioAnterior"
	, car."INGMontoDiferidoDiario"
	, car."INGMontoDiferidoAcumulado"
	, car."INGMontoMatriz"
	, car."TipoCarteraCorredor"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS car
LEFT JOIN (
    SELECT res."FolioCarteraOrigen", res."FechaRespaldo"
        ,SUM("IngCantidad") AS "IngCantidad"
        ,SUM("IngValorRescate") AS "IngValorRescate"
        ,SUM("IngMontoValorizado") AS "IngMontoValorizado"
    FROM "BSCL.Custodia.BD::HIS.Reserva" AS res 
    WHERE res."IdEstado"='VIG' AND res."EgrCantidad" = 0
    GROUP BY res."FolioCarteraOrigen", res."FechaRespaldo") AS resTot ON resTot."FolioCarteraOrigen" = car."FolioCartera" AND resTot."FechaRespaldo" = car."FechaRespaldo"  
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = car."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = car."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON (car."IdTipoOperacion" = TipoOperacion."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON (car."IdBoveda" = Boveda."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON (car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste")
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON (car."IdBolsa" = Bolsa."IdBolsa")
--LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON (car."IdInstrumento" = Ins."IdInstrumento")


LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (car."IdConcepto" = Concepto."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON (car."EGRIdConcepto" = ConceptoEGR."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON (car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON (car."IdEmpresa" = Empresa."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as Usuario ON (car."IdUsuario" = Usuario."IdUsuario")

LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON (car."IdEstado" = Estado."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Instrumento ON (car."IdInstrumento" = Instrumento."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON (TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON (ClasInst."IdClasificacionTipoInstrumento" = Instrumento."IdClasificacionTipoInstrumento")

LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON (car."IdMonedaSistema" = Mon1."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON (car."IdMonedaInstrumento" = Mon2."IdMoneda")
LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres ON (car."IdInstrumento" = tpres."IdInstrumentoPrestamo")

WHERE car."IdEstado" = 'VIG'
	and car."EGRCantidad" = 0
WITH READ ONLY;
GO


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSaldos"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
