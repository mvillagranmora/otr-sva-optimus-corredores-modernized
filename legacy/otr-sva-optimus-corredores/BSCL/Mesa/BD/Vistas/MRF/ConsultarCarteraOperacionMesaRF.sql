SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraOperacionMesaRF";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraOperacionMesaRF" AS
SELECT 	car."IdOperacion"
		,CASE WHEN res."FolioCarteraOrigen" = 0  THEN res."FolioCarteraCustodia" ELSE res."FolioCarteraOrigen" END as "FolioOperacion" 
		,car."FolioCartera"
		,car."FechaOperacion"
		,car."IdTipoProducto"
		,car."IdTipoOperacion"
		,tOpe."IdInstrumento"
		,tOper."Nombre" as "NombreTipoOperacion"
		,car."IdOperacionRelacionada"
		,car."Intermesa"
		,car."IdMesaOrigen"
		,mesOrigen."Nombre" as "NombreMesaOrigen"
		,car."IdMesaDestino"
		,mesDestino."Nombre" as "NombreMesaDestino"
		,soc."IdSocioDeNegocio"
		,car."IdCliente"
		,cli."NombreCompleto"
		,cli."DocumentoIdentidad"
		,car."IdClienteCorredor"
		,car."IdPortafolio"
		,por."CodigoPropositoPortafolio"
		,car."AfectaCustodia"
		,car."IdPortafolioCorredor"
		,car."IdEmpresa"
		,CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END as "RazonSocial"--car."RazonSocial"
		,car."MontoOperacion"
		,car."MontoOperacionInicial"
		,car."MontoOperacionFinal"
		,car."IdTipoReajuste"
		,car."Tasa"
		,car."IdMoneda"
		,car."MontoOperacionMoneda"
		,car."MontoOperacionInicialMoneda"
		,car."MontoOperacionFinalMoneda"
		,car."IdMonedaLiquidacion"
		
		,car."PlazoEnDias"
		,car."FechaVencimiento"
		,car."FechaLiquidacion"
		,car."IdCondicionLiquidacion"
		,cond."Nombre" as "NombreCondicionLiquidacion"
		,car."FechaCondicionLiquidacion"
		,car."IdAgente"
		,per."NombreCompleto" as "NombreEjecutivo"
		,car."IdEstado"
		,est."NombreEstado"
		,car."FechaCreacion"
		,car."TasaAgente"
		,car."IdUsuarioCreacion"
		,usr."CodigoUsuario"
		,car."IdTipoCanalIngreso"
		,car."Glosa"
		,car."MontoOperacionAnticipo"
		,car."MontoOperacionInicialAnticipo"
		,car."MontoOperacionFinalAnticipo"
		,car."TasaAnticipo"
		,car."MontoOperacionMonedaAnticipo"
		,car."MontoOperacionInicialMonedaAnticipo"
		,car."MontoOperacionFinalMonedaAnticipo"
		,car."IdMonedaLiquidacionAnticipo"
		,car."IdCondicionLiquidacionAnticipo"
		,condAnt."Nombre" as "NombreCondicionLiquidacionAnticipo"
		,car."FechaCondicionLiquidacionAnticipo"
		,car."IdEmisor"
		,res."IdConcepto"				as "IdConceptoDetalle"
		,res."IdReserva"				as "IdReservaDetalle"
		,CASE WHEN res."FolioCarteraOrigen" = 0 then 'Nominal' ELSE 'Cartera' END  as  "CarteraNominal"
		,res."FolioCarteraOrigen"		as "FolioCarteraDetalle"
		,res."FolioCarteraCustodia"		as "FolioCarteraCustodiaDetalle"
		,res."IdEmisor"					as "IdEmisorDetalle"
		,res."CorteMinimo"				as "CorteMinimoDetalle"
		,res."IdTipoReajuste"			as "IdTipoReajusteDetalle"
		,res."IdMoneda"                 as "IdMonedaDetalle"
		,res."IdMonedaEmisionInstrumento" as "IdMonedaEmisionInstrumentoDetalle"
		,res."IdMonedaLiquidacionInstrumento" as "IdMonedaLiquidacionInstrumentoDetalle"
		,res."IdMonedaLiquidacionOperacion" as "IdMonedaLiquidacionOperacionDetalle"
		,res."CantidadInicial"			as "CantidadDetalle"
		,res."IdInstrumento"			as "IdInstrumentoDetalle"
		,CASE
			WHEN carr."NemoSVS" != '' OR carr."NemoSVS" IS NOT NULL  THEN carr."NemoSVS"
			ELSE res."Nemotecnico"
		END AS "NemotecnicoDetalle"
		,ins."TipoValorizacion"			as "TipoValorizacionDetalle"
		,cla."ValorClasificacionRiesgo" as "ValorClasificacionRiesgoDetalle"
		,res."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoDetalle"		     	 			
		,res."IdTipoInstrumento"		as "IdTipoInstrumentoDetalle"
		,res."Precio"					as "PrecioCompraDetalle"
		,res."PrecioMercado"                 as "PrecioMercadoDetalle"
		,res."Tasa"						as "TasaCompraDetalle"
		,res."TasaMercado"                       as "TasaMercadoDetalle"
		,res."MontoValorizado"			as "MontoCompraDetalle"
		,res."MontoLiquidacion"            as "MontoLiquidacionDetalle"
		,res."MontoLiquidacionOperacion"            as "MontoLiquidacionOperacionDetalle"
		,res."FechaVencimiento"			as "FechaVencimientoInstrumentoDetalle"
		,CASE WHEN res."EsFungible" = 'S' then 'Si' ELSE 'No' END  as  "EsFungibleDetalle"
		,res."IngCantidad"				as "CantidadReservadaDetalle"
		,CASE WHEN res."IdClasificacionTipoInstrumento" = 'IF' then res."IngValorRescate" ELSE res."IngCantidad" END  AS "ValorRescateDetalle"
		,res."IdUsuarioCreacion"		as "IdUsrReservaDetalle"
		,res."IdOperacion"				as "IdOperacionDetalle"
		,res."IdBolsa"					as "IdBolsaDetalle"
		,res."IdBoveda"					as "IdBovedaDetalle"
		,res."INGMontoAPlazo"           as "INGMontoAPlazoDetalle"
		,res."INGMontoAPlazoMonedaInstrumento" as "INGMontoAPlazoMonedaInstrumentoDetalle"
		,COALESCE(CatIFRS."CodigoCarteraIFRS",'') 				as "CodigoIFRSDetalle"
		,COALESCE(CatIFRS."CarteraIFRS",'')			as "CodigoDescIFRSDetalle"
		,Boveda."Nombre"				as "NombreBovedaDetalle"
		,ClasInst."Nombre"				as "ClasificacionTipoInstrumentoDetalle"
		,res."MontoValorizadoReserva"
		,res."IdCarteraOrigen" as "IdCarteraDetalle"
		,car."TipoDocumento" as "TipoDocumento"
		,car."IdContacto" as "IdContacto"
		,CASE 
			WHEN car."IdTipoOperacion" IN ('VENTER','VENPAC')
				THEN COALESCE (carr."EGRSpread", 0)
			ELSE 0
		END AS "Spread"
		,COALESCE (subya."EGRValorPresente", 0) AS "ValorPresente"
from "BSCL.Mesa.BD::MES.Operacion" car
	inner join "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa"  est
		on (car."IdTipoProducto" = est."IdTipoProducto" and est."NombreEntidad" = 'INGRESO' and car."IdEstado" = est."IdEstado")
	inner join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" cond
		on (car."IdTipoOperacion" = cond."IdTipoOperacion" and car."IdCondicionLiquidacion" = cond."IdCondicionDeLiquidacion")
	left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condAnt
		on (car."IdTipoOperacion" = condAnt."IdTipoOperacion" and car."IdCondicionLiquidacionAnticipo" = condAnt."IdCondicionDeLiquidacion")
	left join "BSCL.Mesa.BD::MES.Mesa" mesOrigen
		on (car."IdMesaOrigen" = mesOrigen."IdMesa")
	left join "BSCL.Mesa.BD::MES.Mesa" mesDestino
		on (car."IdMesaDestino" = mesDestino."IdMesa")
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" res
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" carr 
		ON (res."IdCarteraOrigen" = carr."IdCartera")
		ON (car."IdOperacion" = res."IdOperacion")
	LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" subya 
			ON subya."IdCarteraRetirada" = res."IdCarteraOrigen" 
			AND subya."FolioSubyacente" = res."FolioSubyacente" 
			AND subya."EGRIdTipoOperacion" IN ('ANTPAC','VCDPAC') 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins
		on (res."IdInstrumento" = ins."IdInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" cla ON (cla."IdInstrumento" = res."IdInstrumento")
	inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc
		on (car."IdCliente" = soc."IdCliente")
	inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
		on (car."IdCliente" = cli."IdCliente")
	inner join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
		on (car."IdPortafolio" = por."IdPortafolio")
	inner join "BSCL.Plataforma.BD::INS.TipoOperacion" tOper
		on (car."IdTipoOperacion" = tOper."IdTipoOperacion")
	left join "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tOpe
		on (car."IdTipoOperacion" = tOpe."IdTipoOperacion" and res."IdTipoInstrumento" = tOpe."IdTipoInstrumento")
	left join "BSCL.Plataforma.BD::ACC.Usuario" usr
		on (car."IdUsuarioCreacion" = usr."IdUsuario")
	left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per
		on (car."IdAgente" = per."IdPersona")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda 
			  ON (Res."IdBoveda" = Boveda."IdBoveda")		
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst 
			  ON (ClasInst."IdClasificacionTipoInstrumento" = res."IdClasificacionTipoInstrumento")
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" as CatIFRS
			  ON (Res."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS");

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;