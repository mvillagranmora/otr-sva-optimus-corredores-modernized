SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
AS
SELECT  
  "SolicitudAporteRescate"."IdSolicitudAporteRescate"
, "SolicitudAporteRescate"."IdCliente"
, "SolicitudAporteRescate"."IdPortafolio"
, "SolicitudAporteRescate"."TipoSolicitud"
, "SerieDeFondo"."IdSerieFondo"
, "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo"
, "SolicitudAporteRescate"."IdDividendo"
, "SolicitudAporteRescate"."IdEmisorOrigen"
, "SolicitudAporteRescate"."IdSerieFondoDestino"
, "SolicitudAporteRescate"."IdEmisorDestino"
, "SolicitudAporteRescate"."IdClienteDestino"
, "SolicitudAporteRescate"."IdPortafolioDestino"
, "SolicitudAporteRescate"."IndicadorMLT"
, CASE "SolicitudAporteRescate"."IndicadorMLT" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsIndicadorMLTUI"
, "SolicitudAporteRescate"."Overnight"
, CASE "SolicitudAporteRescate"."Overnight" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsOvernightUI"
, "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado"
, "SolicitudAporteRescate"."IdTipoCartera"
, "SolicitudAporteRescate"."CalculoPorMontoCuota"
, Case
    When "SolicitudAporteRescate"."CalculoPorMontoCuota" = 'C' Then 'CUOTA'
    when "SolicitudAporteRescate"."CalculoPorMontoCuota" = 'M' Then 'MONTO'
  Else ''
  End As "CalculoMontoCuotaUI"
, CASE "SolicitudAporteRescate"."CalculoPorMontoCuota" WHEN 'C' THEN 'UI_NUMERO_CUOTAS' WHEN 'T' THEN 'UI_TOTAL' WHEN 'P' THEN 'UI_PORCENTAJE' ELSE 'UI_MONTO' END AS "CalculoPorMontoCuotaUI"
, "SolicitudAporteRescate"."FechaEmision"
, "SolicitudAporteRescate"."FechaAceptacion"
, "SolicitudAporteRescate"."FechaCertificacion"
, "SolicitudAporteRescate"."FechaLiquidacion"
, "SolicitudAporteRescate"."FechaAnulacion"
, "SolicitudAporteRescate"."FechaOriginalInversion"
, "SolicitudAporteRescate"."FechaIngresoRegistro"
, "SolicitudAporteRescate"."FechaAceptacionRegistro"
, "SolicitudAporteRescate"."FechaCertificacionRegistro"
, "SolicitudAporteRescate"."FechaLiquidacionRegistro"
, "SolicitudAporteRescate"."FechaAnulacionRegistro"
--, "SolicitudAporteRescate"."Monto"
--, "SolicitudAporteRescate"."NumeroCuotas"
,CASE 
    WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'APO' And "SolicitudAporteRescate"."Estado" != 'CRT' THEN ("SolicitudAporteRescate"."Monto" / (CASE WHEN "SolicitudAporteRescate"."Estado" != 'CRT' THEN "ValorCuota"."ValorCuota" ELSE "SolicitudAporteRescate"."ValorCuota" END))
    WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'RES' And "SolicitudAporteRescate"."CalculoPorMontoCuota" = 'M' And "SolicitudAporteRescate"."Estado" != 'CRT' THEN ("SolicitudAporteRescate"."Monto" / (CASE WHEN "SolicitudAporteRescate"."Estado" != 'CRT' THEN "ValorCuota"."ValorCuota" ELSE "SolicitudAporteRescate"."ValorCuota" END))
  ELSE "SolicitudAporteRescate"."NumeroCuotas" 
  END AS "NumeroCuotas" 
, "ValorCuota"."ValorCuota" As "ValorCuota"
, CASE 
	WHEN "SolicitudAporteRescate"."Estado" != 'CRT' AND "ValorCuota"."ValorCuota" IS NOT NULL THEN "ValorCuota"."ValorCuota" 
  ELSE "SolicitudAporteRescate"."ValorCuota" 
  END As "ValorCuotaSolicitud"
, CASE
    WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'RES' And "SolicitudAporteRescate"."CalculoPorMontoCuota" = 'C' And "SolicitudAporteRescate"."Estado" != 'CRT' THEN ((CASE WHEN "SolicitudAporteRescate"."Estado" != 'CRT' THEN "ValorCuota"."ValorCuota" ELSE "SolicitudAporteRescate"."ValorCuota" END) * "SolicitudAporteRescate"."NumeroCuotas")
  ELSE "SolicitudAporteRescate"."Monto"
  END AS "Monto"
, "SolicitudAporteRescate"."RentabilidadReal"
, "SolicitudAporteRescate"."CostoUF"
, "SolicitudAporteRescate"."CostoMonedaLocal"
, "SolicitudAporteRescate"."ValorUFFechaInversion"
, "SolicitudAporteRescate"."ParidadFechaInversion"
, "SolicitudAporteRescate"."IdMoneda"
, "SolicitudAporteRescate"."IdMonedaPago"
, "SolicitudAporteRescate"."MontoComision"
, "SolicitudAporteRescate"."MontoPago"
, "SolicitudAporteRescate"."IdEmpresa"
, "SolicitudAporteRescate"."Glosa"
, "SolicitudAporteRescate"."IdUsuario"
, CASE WHEN COALESCE("ConsultarComprobanteFormaDePago"."IdMedioDePago",'') = '' THEN "SolicitudAporteRescate"."IdMedioDePago" ELSE "ConsultarComprobanteFormaDePago"."IdMedioDePago" END AS "IdMedioDePago"
, "SolicitudAporteRescate"."EstadoLiquidacion"
, "SolicitudAporteRescate"."MueveCta"
, "SolicitudAporteRescate"."FolioCartaLiquidacion"
, "SolicitudAporteRescate"."MontoInversion"
, "SolicitudAporteRescate"."NumeroCuotasInversion"
, "SolicitudAporteRescate"."ValorCuotaInversion"
, "SolicitudAporteRescate"."CostoMonedaLocalInversion"
, "SolicitudAporteRescate"."ParidadFechaCertificacion"
, "SolicitudAporteRescate"."MontoPagoLocal"
, "SolicitudAporteRescate"."MontoComisionLocal"
, "SolicitudAporteRescate"."Spread"
, CASE WHEN "SolicitudAporteRescate"."IdMoneda" != "Fondo"."IdMoneda" THEN 'S' ELSE 'N' END AS "EsDiferido"
, CASE WHEN "SolicitudAporteRescate"."IdMoneda" != "Fondo"."IdMoneda" THEN 'UI_SI' ELSE 'UI_NO' END AS "EsDiferidoUI"
, "SolicitudAporteRescate"."OrigenOperacion"
, "SolicitudAporteRescate"."SolicitudBatch"
, "SolicitudAporteRescate"."ComisionInformada"
---SPOT 
, "SolicitudAporteRescate"."IdIngresoEgreso"
, "EstadoTesoreriaSpot"."Nombre" 		AS "NombreEstadoTesoreria"
, "TesoreriaSpot"."FechaLiquidacion" 	AS "FechaLiquidacionTesoreria"
, "SolicitudAporteRescate"."IdOperacionSpot"
, COALESCE("EstadoSpot"."Nombre",'') AS "NombreEstadoSpot"
, COALESCE("EstadoConfirmacionSpot"."Nombre",'') AS "NombreEstadoConfirmacionSpot"
, "Spot"."FechaLiquidacion" AS "FechaLiquidacionSpot"
, CASE WHEN "SolicitudAporteRescate"."IdOperacionSpot" > 0 THEN  (CASE WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'APO' THEN 'VENSPT' ELSE  'COMSPT' END) ELSE '' END AS "IdTipoOperacionSpot" 
, CASE WHEN "SolicitudAporteRescate"."IdOperacionSpot" > 0 THEN  "TipoOperacionSpot"."Nombre"  ELSE '' END AS "NombreTipoOperacionSpot" 
--Estado de la soicitud pendiente para SPOT relacionados
, "SolicitudAporteRescate"."Estado" AS "EstadoSolicitud"
, "SerieDeFondo"."IdInstrumento"
, "SerieDeFondo"."NombreSerieFondo"
, "SerieDeFondo"."CodigoSerie"
, "SerieDeFondo"."Alias"
, "SerieDeFondo"."MontoMinimo"
, "SerieDeFondo"."MontoMaximo"
, "SerieDeFondo"."DiasRetencionesRescate"
, "SerieDeFondo"."ExcluyeGarantia"
, "SerieDeFondo"."IdSerieFondoOrigen"
, "SerieDeFondo"."CobraComision"
, "SerieDeFondo"."Omnibus"
, "SerieDeFondo"."Diferido"
, "SerieDeFondo"."DecimalesValorCuota"
, "SerieDeFondo"."DecimalesCuota"
, "SerieDeFondo"."IdCliente" AS "IdClienteAGF"
, "SerieDeFondo"."IdPortafolio" AS "IdPortafolioAGF"
, "SerieDeFondo"."IdMedioDePagoAbono" AS "IdMedioDePagoAbonoAGF"
, "SerieDeFondo"."IdMedioDePagoCargo" AS "IdMedioDePagoCargoAGF"
, "SerieDeFondo"."IdEstado" AS "IdEstadoSerieFondo"
, "Fondo"."IdFondo"
, "Fondo"."IdEmisor"
, "Fondo"."Nemotecnico"
, "Fondo"."NombreFondo"
, "Fondo"."IdTipoFondo"
, "Fondo"."IdPaisOrigen"
, "Fondo"."CodigoSVS"
, "Fondo"."MoneyMarket"
, "Fondo"."IdCategoriaPerfilFondoMutuo"
, "Fondo"."FechaCreacion"
, "Fondo"."FechaConstitucion"
, "Fondo"."FechaCierre"
, "Fondo"."Vigencia"
, "Fondo"."IdTipoInstrumento"
, "Fondo"."IdMoneda" AS "IdMonedaFondo"
, "ClienteAGF"."IdPersona" AS "IdPersonaAGF"
, "ConsultarPortafolio"."CodigoPortafolio"
, "ConsultarPortafolio"."DescripcionPortafolio"
, "ConsultarPortafolio"."Proposito" AS "PropositoPortafolio"
, "ConsultarCliente"."RazonSocial"
, "ConsultarCliente"."NombreCompleto"
--
, "ConsultarCliente"."Nombres"
, "ConsultarCliente"."ApellidoPaterno"
, "ConsultarCliente"."ApellidoMaterno"
--
, "ConsultarCliente"."DocumentoIdentidad"
, "ConsultarCliente"."IdPersona"
, "ConsultarPortafolioDestino"."CodigoPortafolio" AS "CodigoPortafolioDestino"
, "ConsultarPortafolioDestino"."DescripcionPortafolio" AS "DescripcionPortafolioDestino"
, "ConsultarClienteDestino"."RazonSocial"    AS "RazonSocialDestino"
, "ConsultarClienteDestino"."NombreCompleto" AS "NombreCompletoDestino"
, "ConsultarClienteDestino"."DocumentoIdentidad" AS "DocumentoIdentidadDestino"
, "TipoFondo"."NombreCorto"
, "TipoFondo"."Descripcion"
, "TipoMovimientoFondoMutuo"."Descripcion" AS "DescripcionTipoMovimiento"
, "Estado"."Nombre" AS "NombreEstado"
, "ConsultarEmpleado"."NombreCompleto" AS "NombreEmpleado"
, CASE WHEN COALESCE("ConsultarComprobanteFormaDePago"."IdMedioDePago",'') = '' THEN "MedioDePago"."Nombre" ELSE "ConsultarComprobanteFormaDePago"."NombreMedioDePago" END AS "TextoPreferenciaPago"
, CASE WHEN "SolicitudAporteRescate"."EstadoLiquidacion" = 'ING' THEN 'INGRESADO' 
	   WHEN "SolicitudAporteRescate"."EstadoLiquidacion" = 'LIQ' THEN 'LIQUIDADO'
	   ELSE '' 
   END AS "NombreEstadoLiquidacion"
, "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoFondoMutuoRelacionado"
, "SolicitudAporteRescateRelacionado"."Monto" AS "MontoMovimientoAsociado"
, "SolicitudAporteRescateRelacionado"."NumeroCuotas" AS "NumeroCuotasMovimientoAsociado"
, "TipoMovimientoFondoMutuoRelacionado"."Descripcion" AS "DescripcionTipoMovimientoRelacionado"
, CASE "Fondo"."FondoExtranjero" WHEN 'S' THEN 'N' ELSE 'S' END AS "EsFondoNacional"
, "TipoMovimientoFondoMutuo"."IdTipoOperacion" AS "IdTipoOperacion"
, "FondoRelacionado"."IdFondo" AS "IdFondoRelacionado"
, "SerieDeFondoRelacionado"."CodigoSerie" AS "CodigoSerieRelacionado"
, COALESCE("RegimenFondoMutuo"."Descripcion",'') AS "DescripcionRegimenTributario"
, "SolicitudAporteRescate"."IdRegimenTributario"
, "SolicitudAporteRescate"."TipoCarteraNormativa"
, "SolicitudAporteRescate"."IdRegimenTributarioDestino"
, "SolicitudAporteRescate"."TipoCarteraNormativaDestino"
, "SolicitudAporteRescate"."IdPreferenciaPagoPortafolio"
, CASE WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'APO' THEN 'APORTE'
       WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'RES' THEN 'RESCATE'
       ELSE '' 
   END AS "DescripcionTipoSolicitud"
, "SolicitudAporteRescate"."IdMonedaSistema"
, "SolicitudAporteRescate"."MontoMonedaSistema"
,"SolicitudAporteRescate"."EsPorMontoLiquido","SolicitudAporteRescate"."MontoLiquido","SolicitudAporteRescate"."MontoLiquidoLocal"
,"SolicitudAporteRescate"."Switch"
,"SolicitudAporteRescate"."IdTipoCanalIngreso","SolicitudAporteRescate"."FolioContrato"
,"SolicitudAporteRescate"."FolioOmnibus"
,"OmnibusDetalle"."IdOmnibus"
,"OmnibusDetalle"."IdEstado" AS "IdEstadoOmnibusDetalle"
,"SolicitudAporteRescate"."IdEmpleadoEjecutivo"
,"SolicitudAporteRescate"."IdCartera"
,"Usuario"."CodigoUsuario"
,"TipoMovimientoFondoMutuo"."TieneCertificacion"
,"SolicitudAporteRescate"."IdAgrupador"
,"SolicitudAporteRescate"."FechaValorCuota"
,"SerieDeFondo"."IdFamiliaFondo"
,"ConsultarPortafolio"."IdMatrizAgrupadorAPV"
,"SolicitudAporteRescate"."IngresoAutomatico"
,"SolicitudAporteRescate"."FolioContratoOperacion"
,"SolicitudAporteRescate"."TipoContratoOperacion"
,"ConsultarClienteAGF"."DocumentoIdentidad" AS "DocumentoIdentidadAGF"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS "TesoreriaSpot" ON ("SolicitudAporteRescate"."IdIngresoEgreso"="TesoreriaSpot"."IdIngresoEgreso")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoTesoreriaSpot" ON ("EstadoTesoreriaSpot"."IdEstado" =  "TesoreriaSpot"."IdEstado")
LEFT OUTER  JOIN 
				  (	 select OperSpot.* from 
					  (SELECT  ROW_NUMBER() OVER(PARTITION BY "IdOperacionSpotOrigen" ORDER BY "IdOperacionSpot" DESC ) AS r ,*
						FROM "BSCL.Mesa.BD::SPT.OperacionSpot" ) as OperSpot
					 where OperSpot.r=1
				  ) AS "Spot" ON ("Spot"."IdOperacionSpotOrigen"=CASE WHEN "SolicitudAporteRescate"."IdOperacionSpot" > 0 THEN "SolicitudAporteRescate"."IdOperacionSpot" ELSE NULL END)
LEFT OUTER  JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS "OperSpotPrin" ON ("Spot"."IdOperacionSpot"="OperSpotPrin"."IdOperacionSpot")				  
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoSpot" ON ("EstadoSpot"."IdEstado" =  "Spot"."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoConfirmacionSpot" ON ("EstadoConfirmacionSpot"."IdEstado" =  "OperSpotPrin"."EstadoConfirmacion")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON "ValorCuota"."IdSerieFondo" = "SerieDeFondo"."IdSerieFondo"
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )
LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio" )
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "ConsultarPortafolioDestino" ON  ( "ConsultarPortafolioDestino"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolioDestino" )
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarClienteDestino" ON  ( "ConsultarClienteDestino"."IdCliente" = "ConsultarPortafolioDestino"."IdCliente" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo") 
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacionSpot"	ON ("TipoOperacionSpot"."IdTipoOperacion" = (CASE WHEN "TipoMovimientoFondoMutuo"."IndicadorAporteRescate" = 'APO' THEN  'VENSPT' ELSE 'COMSPT' END))
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoLiquidacion" ON ("EstadoLiquidacion"."IdEstado" = "SolicitudAporteRescate"."EstadoLiquidacion")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "ConsultarEmpleado" ON ("ConsultarEmpleado"."IdEmpleado" = "SolicitudAporteRescate"."IdEmpleadoEjecutivo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "SolicitudAporteRescate"."IdMedioDePago")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "ClienteAGF" ON ("SerieDeFondo"."IdCliente" = "ClienteAGF"."IdCliente" AND "ClienteAGF"."Borrado"='N')
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescateRelacionado" ON ("SolicitudAporteRescateRelacionado"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondoRelacionado" ON ("SerieDeFondoRelacionado"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondoDestino" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FondoRelacionado" ON ("FondoRelacionado"."IdFondo" = "SerieDeFondoRelacionado"."IdFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuoRelacionado" ON ("TipoMovimientoFondoMutuoRelacionado"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenFondoMutuo" ON ("RegimenFondoMutuo"."IdRegimenFondoMutuo" = "SolicitudAporteRescate"."IdRegimenTributario")
LEFT OUTER  JOIN (SELECT top 1 "IdTipoComprobanteOrigen", "IdComprobanteOrigen", ComprobanteDetalle."IdEstado",Comprobante."IdComprobante"  FROM
					"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle
					INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante"
				) AS "ComprobanteDetalle" ON ("ComprobanteDetalle"."IdTipoComprobanteOrigen" = 'FMM' AND "ComprobanteDetalle"."IdComprobanteOrigen" = "SolicitudAporteRescate"."IdSolicitudAporteRescate" AND "ComprobanteDetalle"."IdEstado" IN ('ING','LIQ','CRT'))
LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS "ConsultarComprobanteFormaDePago" ON ("ConsultarComprobanteFormaDePago"."IdComprobante" = "ComprobanteDetalle"."IdComprobante" AND "ConsultarComprobanteFormaDePago"."IdEstado" IN ('CRT','EBV','EMI','GIR','ING','LIQ'))
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle" AS "OmnibusDetalle" ON ("SolicitudAporteRescate"."FolioOmnibus" = "OmnibusDetalle"."IdOmnibusDetalle")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON ("Usuario"."IdUsuario" = "SolicitudAporteRescate"."IdUsuario")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS "Tesoreria" ON ("SolicitudAporteRescate"."FolioCartaLiquidacion"="Tesoreria"."IdComprobanteOrigen" AND
"Tesoreria"."IdTipoComprobanteOrigen" = 'LFM')
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoTesoreria" ON ("EstadoTesoreria"."IdEstado" =  "Tesoreria"."IdEstado")
LEFT OUTER JOIN (SELECT "ComprobanteDetalleOrigen"."IdIngresoEgreso", "ComprobanteDetalle"."IdComprobante", "Comprobante"."IdEstado", "Estado"."Nombre" as "NombreEstado"
  					FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS "ComprobanteDetalle"
  					INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS "Comprobante"
  					ON ("Comprobante"."IdComprobante" = "ComprobanteDetalle"."IdComprobante")
  					LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" 
  					ON "Comprobante"."IdEstado" = "Estado"."IdEstado"
  					LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS "ComprobanteDetalleOrigen" 
  					ON ("ComprobanteDetalle"."IdComprobanteDetalle" = "ComprobanteDetalleOrigen"."IdComprobanteDetalle")
  					WHERE (COALESCE("ComprobanteDetalle"."IdEstado",'') NOT IN ('MOD','ANU') OR COALESCE("ComprobanteDetalle"."IdEstado",'') = '')
  					GROUP BY "ComprobanteDetalleOrigen"."IdIngresoEgreso", "ComprobanteDetalle"."IdComprobante", "Comprobante"."IdEstado", "Estado"."Nombre"
  				  ) as "CD" 
  			   ON ("CD"."IdIngresoEgreso" = "Tesoreria"."IdIngresoEgreso" and "CD"."IdEstado" = 'LIQ')
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarClienteAGF" ON  ( "ConsultarClienteAGF"."IdCliente" = "SerieDeFondo"."IdCliente" ) ;
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO