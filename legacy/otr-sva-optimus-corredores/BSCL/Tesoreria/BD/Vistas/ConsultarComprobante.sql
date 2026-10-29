SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS
SELECT DISTINCT
	 Comprobante."IdComprobante"
	,Comprobante."IdEmpresa"
    ,Empresa."Nombre" AS "NombreEmpresa"
	,Comprobante."IdUsuario"
	,Comprobante."TipoComprobante"
	,Comprobante."IdTipoMovimiento"
	,TipoMovimiento."Nombre" AS "NombreTipoMovimiento"
	,Comprobante."IdConcepto"
	,Concepto."Nombre" AS "NombreConcepto"
	,Comprobante."IdCaja"
	,Caja."Nombre" AS "NombreCaja"
	,Caja."Codigo" AS "CodigoCaja"
	,Comprobante."IdMoneda"
	,Moneda."Nombre" AS "NombreMoneda"
	,Comprobante."Monto"
	,(CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVEGR' THEN Comprobante."Monto" ELSE 0 END) AS "MontoEgreso"
	,(CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVING' THEN Comprobante."Monto" ELSE 0 END) AS "MontoIngreso"
	,(CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVING' THEN Comprobante."Monto" ELSE -1*Comprobante."Monto" END) AS "MontoConSigno"
	,Comprobante."FechaLiquidacion"
	,Comprobante."FechaIngresoRegistro"
	,Comprobante."FechaMovimiento"
	,Comprobante."IdTipoPersona"
	,COALESCE(TSN."NombreTipoSocioDeNegocio",'') AS "NombreTipoSocioDeNegocio"
	,Comprobante."IdPersona"
	,Comprobante."NombrePersona"
	,Comprobante."IdTipoDocumentoIdentidad"
	,Comprobante."DocumentoIdentidad"
	,Comprobante."TitularPago"
	,Comprobante."IdTipoPersonaTitularPago"
	,Comprobante."IdPersonaTitularPago"
	,Comprobante."NombrePersonaTitularPago"
	,CASE
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CLI' THEN 'SI'
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CNT' AND Comprobante."IdConcepto" = 'RESCFM' AND Comprobante."IdPortafolioTitularPago"<>IngresoEgreso."IdPortafolio" AND Comprobante."IdTipoMovimiento" = 'MOVEGR' THEN 'SI'	-- Para Rescates de FFMM donde el titular Pago es un AGF se usa cuenta inversion
		ELSE 'NO'
	END "MueveCuentaInversion"
	,Comprobante."IdPortafolioTitularPago"
	,PortafolioTitularPago."CodigoPortafolio" ||' '|| PortafolioTitularPago."Proposito" as "NombrePortafolioTitularPago"
	,Comprobante."SecuenciaAgrupacion"
	,Comprobante."EsBilateral"
	,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("ComprobanteNeteado") as "ComprobanteNeteado"
	,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("SeparadoPorConcepto") as "SeparadoPorConcepto"
	,Comprobante."IdUsuarioBloqueo"
	,Comprobante."FechaHoraBloqueo"
	,Comprobante."IdCarga"
	,Comprobante."IdEstado"
	,Estado."Nombre" AS "NombreEstado"
	,Evento."IdTipoEvento"
	,TipoEvento."Nombre" AS "Autorizacion"
	,CONCAT('SOLICITUD ', TipoEvento."Nombre") AS "SolicitudAutorizacion"
	,Evento."Glosa" AS "MotivoAutorizacion"
	,EstadoAutorizacion."IdEstado" AS "IdEstadoAutorizacion"
	,EstadoAutorizacion."NombreEstado" AS "NombreEstadoAutorizacion"
	-- Datos de la forma de pago(Solo cuando hay 1 medio de pago por comprobante)
	,FPMC."IdMedioDePago"
	,FPMC."NombreMedioDePago"
	,FPMC."IdModalidadDePago"
	,FPMC."NombreModalidadDePago"
	,FPMC."IdCuentaBancoCorredora"
	,FPMC."IdChequera"
	,FPMC."NombreBancoCorredora"
	,FPMC."NumeroCuentaBancariaCorredora"
	,FPMC."NombreTipoCuentaBancariaCorredora"
	,FPMC."NumeroSerieSocio"
    ,FPMC."NumeroDocumentoSocio"
    ,FPMC."FechaDocumentoPagoSocio"
	,FPMC."IdBancoSocio"
	,FPMC."NombreBancoSocio"
	,FPMC."NumeroCuentaBancariaSocio"
	,FPMC."NombreTipoCuentaBancariaSocio"
	,FPMC."AliasBanco"
	,CASE WHEN TipoMovimiento."IdModulo" = 'TES' AND FPMC."IdMedioDePago" = 'TDP' THEN
		(SELECT "Valor"
		  FROM "BSCL.Custodia.BD::CUS.Configuracion"
	   	  WHERE "CodigoParametro" = (CASE WHEN ComprobanteDetalle."IdTipoProducto" = 'MERF'
	   	  								   AND (ComprobanteDetalle."IdTipoOperacion" = 'COMPAC'
	   	  								    OR  ComprobanteDetalle."IdTipoOperacion" = 'COMTER'
	   	  								    OR  ComprobanteDetalle."IdTipoOperacion" = 'ANTPAC') THEN
	   	  								'CuentaDCVCuentaPropia'
	   	  							 ELSE
		   	  							'CuentaDCVCuentaLiquidacion'
	   	  							 END)
	        AND "IdEmpresa" = Comprobante."IdEmpresa")
	 ELSE ''
	 END AS "CuentaDCVOrigen"
	,CASE WHEN TipoMovimiento."IdModulo" = 'TES' AND FPMC."IdMedioDePago" = 'TDP' THEN
		CuentaCustodio."Codigo"
	 ELSE ''
	 END AS "CuentaDCVDestino"
	,UsuarioLiquidacion."NombreCompleto" AS "NombreUsuarioLiquidacion"
	,Comprobante."IdUsuarioAnulacion"
	,Comprobante."FechaHoraAnulacion"
	,ComprobanteDetalle."IdComprobanteOrigen"     -- Solo del primer origen(ya que pueden ser n)
	,COALESCE(FAC."FolioComprobante", 0) AS "FolioComprobanteDTE"
	,CAST(COALESCE(ComprobanteDetalle."FechaComprobanteOrigen",'0001-01-01') AS DATE) AS "FechaComprobanteOrigen"  -- Solo del primer origen(ya que pueden ser n)
	,CASE
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CLI' THEN IngresoEgreso."IdPortafolio"
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CNT' AND Comprobante."IdConcepto" = 'RESCFM' AND Comprobante."IdPortafolioTitularPago"<>IngresoEgreso."IdPortafolio" AND Comprobante."IdTipoMovimiento" = 'MOVEGR' THEN IngresoEgreso."IdPortafolio"	-- Para Rescates de FFMM donde el titular Pago es un AGF se usa cuenta inversion
		ELSE TO_BIGINT(0)
	END AS "IdPortafolio"  -- IdPortafolio Socio de Negocio
	,CASE
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CLI' THEN PortafolioSocioDeNegocio."CodigoPortafolio" ||' '|| PortafolioSocioDeNegocio."Proposito"
		WHEN Comprobante."IdTipoPersonaTitularPago" = 'CNT' AND Comprobante."IdConcepto" = 'RESCFM' AND Comprobante."IdPortafolioTitularPago"<>IngresoEgreso."IdPortafolio" AND Comprobante."IdTipoMovimiento" = 'MOVEGR' THEN PortafolioSocioDeNegocio."CodigoPortafolio" ||' '|| PortafolioSocioDeNegocio."Proposito"	-- Para Rescates de FFMM donde el titular Pago es un AGF se usa cuenta inversion
		ELSE ''
	END AS "NombrePortafolio"
	,IngresoEgreso."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
	,CASE
		WHEN  Comprobante."IdTipoMovimiento" <> 'PAREGR' AND Comprobante."IdTipoMovimiento" <> 'PARING' THEN PortafolioTitularPago."Alias"
		ELSE ''
		END AS "Alias"
	,CASE
		WHEN  Comprobante."TipoComprobante" <> 'PAREGR' AND Comprobante."TipoComprobante" <> 'PARING' THEN COALESCE(INS."Nemotecnico", '')
		ELSE ''
		END AS "Nemotecnico"
	,COALESCE (NominaTransferencia."IdComprobantePartidaEgreso", 0) AS "IdComprobantePartidaEgreso"
	,EstadoNomina."NombreEstado" AS "NombreEstadoNomina"
	,upper(conLiq."Nombre") AS "CondicionDeLiquidacion"
	,COALESCE(Comprobante."Glosa", '') AS "Glosa"
	,CASE
		WHEN IngresoEgreso."IdTipoInstrumento" = 'FFM' THEN TINS."IdClasificacionTipoInstrumento"
		ELSE IngresoEgreso."IdClasificacionTipoInstrumento"
		END AS "IdClasificacionTipoInstrumento"
	,IngresoEgreso."IdTipoInstrumento"
	,COALESCE(FAC."EstadoCaja", 'NRE') AS "EstadoCaja"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON Comprobante."IdEstado" = Estado."IdEstado"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TipoMovimiento ON (TipoMovimiento."IdModulo" = 'TES' and  TipoMovimiento."IdTipoMovimiento" = Comprobante."IdTipoMovimiento")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON (Concepto."IdConcepto" = Comprobante."IdConcepto")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS Caja ON Caja."IdCaja" = Comprobante."IdCaja"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON (Moneda."IdMoneda" = Comprobante."IdMoneda")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioTitularPago ON PortafolioTitularPago."IdPortafolio" = Comprobante."IdPortafolioTitularPago"
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CuentaCustodio ON CuentaCustodio."IdPortafolio" = PortafolioTitularPago."IdPortafolio" AND CuentaCustodio."IdBoveda" = 'DCV' AND CuentaCustodio."IdEstado" = 'ACT' AND CuentaCustodio."IdEstadoAutorizacion" = 'AUT'
LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS Evento ON Evento."IdEvento" = Comprobante."IdEvento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON (Evento."IdTipoEvento" = TipoEvento."IdTipoEvento" AND TipoEvento."IdEmpresa" = Evento."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion" AS EstadoAutorizacion ON (EstadoAutorizacion."IdEstado" = Evento."IdEstado" AND upper(EstadoAutorizacion."NombreEntidad") = upper('AUT.Evento'))
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TSN  ON (TSN."IdTipoSocioDeNegocio" = Comprobante."IdTipoPersona")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa ON (Empresa."IdEmpresa" = Comprobante."IdEmpresa")
LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS FPMC ON FPMC."IdInterno" = Comprobante."IdComprobante" AND FPMC."Tipo" = 'Comprobante'
LEFT OUTER JOIN
(
	SELECT "IdComprobante" , MIN("NroLinea") AS "NroLinea"
	FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	GROUP BY "IdComprobante"
) AS COMDETMEN ON  (COMDETMEN."IdComprobante" = Comprobante."IdComprobante")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle ON  (COMDETMEN."IdComprobante" = ComprobanteDetalle."IdComprobante" AND COMDETMEN."NroLinea" = ComprobanteDetalle."NroLinea")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS ComprobanteDetalleOrigen ON (ComprobanteDetalleOrigen."IdComprobanteDetalle" = ComprobanteDetalle."IdComprobanteDetalle")
LEFT OUTER JOIN
				(
				SELECT "IdComprobante" , MIN("IdComprobanteFormaDePago") AS "IdComprobanteFormaDePago"
				FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
				WHERE "IdEstado" <> 'MOD'
				GROUP BY "IdComprobante"
				) AS ComprobanteFormaPago ON (ComprobanteFormaPago."IdComprobante" = Comprobante."IdComprobante")

LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NominaTransferenciaDetalle ON (NominaTransferenciaDetalle."IdComprobanteFormaDePago" = ComprobanteFormaPago."IdComprobanteFormaDePago")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NominaTransferencia ON (NominaTransferencia."IdNominaTransferencia" = NominaTransferenciaDetalle."IdNominaTransferencia")
LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" as EstadoNomina ON EstadoNomina."IdEstado" = NominaTransferencia."IdEstado"
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IngresoEgreso ON (IngresoEgreso."IdIngresoEgreso" = ComprobanteDetalleOrigen."IdIngresoEgreso")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = IngresoEgreso."IdInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS FAC ON FAC."IdComprobante" = ComprobanteDetalle."IdComprobanteOrigen"
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioSocioDeNegocio ON (PortafolioSocioDeNegocio."IdPortafolio" = IngresoEgreso."IdPortafolio")
LEFT OUTER JOIN (
				SELECT
					UL."IdComprobante",
					MAX(PER."NombreCompleto") AS "NombreCompleto"
				FROM (
					SELECT FP."IdComprobante", FP."FechaHoraLiquidacion", SOC."IdPersona"
					FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS FP
					JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = FP."IdUsuarioLiquidacion" AND USU."TipoEntidad" = 'EMP'
					LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdEmpleado" = USU."IdEntidad"
					WHERE FP."IdEstado" = 'LIQ'
					UNION ALL
					SELECT FP."IdComprobante", FP."FechaHoraLiquidacion", SOC."IdPersona"
					FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS FP
					JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = FP."IdUsuarioLiquidacion" AND USU."TipoEntidad" = 'CLI'
					LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdCliente" = USU."IdEntidad"
					WHERE FP."IdEstado" = 'LIQ'
					UNION ALL
					SELECT FP."IdComprobante", FP."FechaHoraLiquidacion", SOC."IdPersona"
					FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS FP
					JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = FP."IdUsuarioLiquidacion" AND USU."TipoEntidad" = 'PER'
					LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdPersona" = USU."IdEntidad"
					WHERE FP."IdEstado" = 'LIQ'
				) AS UL
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PER ON PER."IdPersona" = UL."IdPersona"
				GROUP BY UL."IdComprobante", UL."FechaHoraLiquidacion"
				) as UsuarioLiquidacion ON UsuarioLiquidacion."IdComprobante" = Comprobante."IdComprobante" AND Comprobante."IdEstado" IN ('LIQ')
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS conLiq ON conLiq."IdCondicionDeLiquidacion" = Comprobante."IdCondicionDeLiquidacion"
WHERE Comprobante."IdEstado" <> 'MOD'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;