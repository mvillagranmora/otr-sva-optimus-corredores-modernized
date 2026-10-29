SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS
SELECT	 IE."IdIngresoEgreso"
		,IE."IdEmpresa"
		,EMP."Nombre" AS "NombreEmpresa"
		,IE."IdModulo"
		,IE."IdUsuario"
		,IE."IdTipoMovimiento"
		,COALESCE(TM."Nombre",'') AS "NombreTipoMovimiento"
		,IE."IdConcepto"
		,COALESCE(CON."Nombre",'') AS "NombreConcepto"
		,IE."IdCaja"
		,COALESCE(CAJ."Nombre",'') AS "NombreCaja"
		,IE."IdMoneda"		
		,MON."Nombre" AS "NombreMoneda"
		,IE."Monto"
		,(CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN IE."Monto" ELSE 0 END) AS "MontoEgreso"
		,(CASE WHEN IE."IdTipoMovimiento" = 'MOVING' THEN IE."Monto" ELSE 0 END) AS "MontoIngreso"
		,(CASE WHEN IE."IdTipoMovimiento" = 'MOVING' THEN IE."Monto" ELSE -1*IE."Monto" END) AS "MontoConSigno"
		,IE."FechaLiquidacion"
		,IE."FechaIngresoRegistro"
		,IE."FechaMovimiento"
		,IE."IdTipoPersona"     -- Tipo Socio de Negocio
		,COALESCE(TSN."NombreTipoSocioDeNegocio",'') AS "NombreTipoSocioDeNegocio"
		,IE."IdPersona"
		--,CPOR."IdCliente"
		,POR."IdCliente"
		,SN."IdSocioDeNegocio"
		,IE."NombrePersona"
		,IE."IdPortafolio"
		,POR."CodigoPortafolio" ||' '|| POR."Proposito" as "NombrePortafolio"	
		,IE."IdTipoDocumentoIdentidad"
		,IE."DocumentoIdentidad"
		,IE."IdTipoPersonaTitularPago"
		,IE."IdPersonaTitularPago"
		,IE."NombrePersonaTitularPago"
		,CASE 
			WHEN IE."IdTipoPersonaTitularPago" = 'CLI' THEN 'SI' 
			WHEN IE."IdTipoPersonaTitularPago" = 'CNT' AND IE."IdConcepto" = 'RESCFM' AND IE."IdPortafolioTitularPago"<>IE."IdPortafolio" AND IE."IdTipoMovimiento" = 'MOVEGR' THEN 'SI'	-- Para Rescates de FFMM donde el titular Pago es un AGF se usa cuenta inversion
			ELSE 'NO' 
		END "MueveCuentaInversion"
		,IE."IdPortafolioTitularPago"
		,IE."IdTipoDocumentoIdentidadTitularPago"
		,IE."DocumentoIdentidadTitularPago" 
		,PORL."CodigoPortafolio" ||' '|| PORL."Proposito" as "NombrePortafolioTitularPago"	
		,PORL."IdCliente" AS "IdClienteTitularPago" 
		,IE."SecuenciaAgrupacion"
		,IE."SecuenciaRelacionada"
		,IE."Glosa"		
		,IE."IdEmpleadoDigitador"
		,CE."NombreCompleto" AS "NombreEmpleadoDigitador"
		,IE."IdSucursalEmpleadoDigitador"		
		,IE."IdTipoComprobanteOrigen"
		,IE."IdComprobanteOrigen"
		,COALESCE(FAC."FolioComprobante", 0) AS "FolioComprobanteDTE"
		,IE."FechaComprobanteOrigen"
		,IE."FechaLiquidacionComprobanteOrigen"
		,IE."IdTipoProducto"
		,PROD."Nombre" AS "NombreTipoProducto"
		,IE."IdTipoOperacion"
		,OPER."Nombre" AS "NombreTipoOperacion"
		,IE."IdBolsa"
		,IE."IdMonedaOperacion"
		,IE."MontoOperacion"
		,IE."FechaParidad"
		,IE."IdTipoReajuste"
		,IE."ValorParidad"
		,IE."FolioCartera"
		,IE."IdInstrumento"
		,INS."Nemotecnico"
		,IE."IdTipoInstrumento"		
		,CASE
			WHEN IE."IdTipoInstrumento" = 'FFM' THEN TINS."IdClasificacionTipoInstrumento"
			ELSE IE."IdClasificacionTipoInstrumento" 
			END AS "IdClasificacionTipoInstrumento"
		,IE."EsBilateral"
		,IE."TitularPago"		
		,IE."IdUsuarioEmision"
		,UC."NombreCompleto" AS "NombreUsuarioEmision"
		,IE."FechaHoraEmision"		
		,IE."IdUsuarioBloqueo"
		,IE."FechaHoraBloqueo"
		,IE."IdCarga"
		,IE."IdEstado"
		,EST."Nombre" AS "NombreEstado"
		,COALESCE(CD."IdComprobante", 0) AS "IdComprobante"  -- Secuencia Comprobante Tesoreria
        ,CAST (IE."FechaOperacion" AS DATE) AS "FechaOperacion"
		,IE."IdCondicionLiquidacion"
		,IE."IdOperacion"
		,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsIngresoAutomatico") as "EsIngresoAutomatico"
		,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("AgrupaComprobante") as "AgrupaComprobante"
		,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("NeteaComprobante") as "NeteaComprobante"
		,CASE WHEN IE."IdModulo" = 'FAC' THEN 'DetalleComprobanteFacturacion'
			  WHEN IE."IdModulo" = 'FFM' THEN 'DetalleFondosMutuos'
		  END as "NombreVentana"

		-- Datos de la forma de pago(Sólo cuando hay 1 medio de pago por movimiento)
		,FPMC."IdMedioDePago"
		,FPMC."NombreMedioDePago"
		,FPMC."IdModalidadDePago"
		,FPMC."NombreModalidadDePago"
		,FPMC."IdCuentaBancoCorredora"
		,FPMC."IdChequera"
		,FPMC."NombreBancoCorredora"
		,FPMC."NumeroCuentaBancariaCorredora"
		,FPMC."NombreTipoCuentaBancariaCorredora"
		,FPMC."NombreBancoSocio"
		,FPMC."NumeroCuentaBancariaSocio"
		,FPMC."NombreTipoCuentaBancariaSocio"
		,FPMC."NumeroSerieSocio"
        	,FPMC."NumeroDocumentoSocio"
        	,FPMC."FechaDocumentoPagoSocio"
		,FPMC."IdBancoSocio"
		,CD."IdEstado" AS "IdEstadoComprobante"
		,ESTCOMP."Nombre" AS "NombreEstadoComprobante"
		,IE."IdUsuarioAnulacion" 
		,IE."FechaHoraAnulacion"
		,EVE."IdTipoEvento"
		,TEV."Nombre" AS "Autorizacion"
		,CONCAT('SOLICITUD ', TEV."Nombre") AS "SolicitudAutorizacion"
		,EVE."Glosa" AS "MotivoAutorizacion"
		,EAU."IdEstado" AS "IdEstadoAutorizacion"
		,EAU."NombreEstado" AS "NombreEstadoAutorizacion"
        ,m."Codigo" AS "RegimenMatrizAgrupadorTitularPago"
        ,FPMC."AliasBanco"
        ,IE."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
        ,CASE WHEN FPMC."IdMedioDePago" = 'TDP' THEN
			(SELECT "Valor" 
			  FROM "BSCL.Custodia.BD::CUS.Configuracion" 
		   	  WHERE "CodigoParametro" = (CASE WHEN IE."IdTipoProducto" = 'MERF' 
			   	  								   AND (IE."IdTipoOperacion" = 'COMPAC'
			   	  								    OR  IE."IdTipoOperacion" = 'COMTER'
			   	  								    OR  IE."IdTipoOperacion" = 'ANTPAC') THEN
			   	  								'CuentaDCVCuentaPropia'
			   	  							 ELSE
				   	  							'CuentaDCVCuentaLiquidacion'
			   	  							 END)
			        AND "IdEmpresa" = IE."IdEmpresa") 
			 ELSE ''
			 END AS "CuentaDCVOrigen"
		 ,CASE WHEN FPMC."IdMedioDePago" = 'TDP' THEN ctaPortafolioDcv."Codigo"
		 END AS "CuentaDCVDestino"
		 ,COALESCE(CONLIQFAC."Nombre",CONLIQ."Nombre", '')  AS "CondicionDeLiquidacion"
		 ,COALESCE(FAC."EstadoCaja", 'NRE') AS "EstadoCaja"
  FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"   AS IE
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TM ON (TM."IdTipoMovimiento" = IE."IdTipoMovimiento")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (CON."IdConcepto" = IE."IdConcepto")
  LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CAJ ON (CAJ."IdCaja" = IE."IdCaja")  
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = IE."IdMoneda")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (IE."IdEstado" = EST."IdEstado")  
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON (POR."IdPortafolio" = IE."IdPortafolio")
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORL ON (PORL."IdPortafolio" = IE."IdPortafolioTitularPago")
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TSN  ON (TSN."IdTipoSocioDeNegocio" = IE."IdTipoPersona")  
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SN ON (SN."IdPersona" = IE."IdPersona" AND IE."IdEmpresa" = SN."IdPersona")
  LEFT OUTER JOIN (SELECT TOP 1 CDO."IdIngresoEgreso", CD."IdComprobante",CD."IdEstado"
  					FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
  					LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle")
  					WHERE (COALESCE(CD."IdEstado",'') NOT IN ('MOD','ANU') OR COALESCE(CD."IdEstado",'') = '') -- No considerar cuando comprobante fue agrupado
  					GROUP BY CDO."IdIngresoEgreso", CD."IdComprobante",CD."IdEstado"
  				  ) as CD 
  			   ON (CD."IdIngresoEgreso" = IE."IdIngresoEgreso")  
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON (EMP."IdEmpresa" = IE."IdEmpresa") 
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = IE."IdInstrumento"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER ON OPER."IdTipoOperacion" = IE."IdTipoOperacion"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS PROD ON PROD."IdTipoProducto" = IE."IdTipoProducto"
  LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS FPMC ON FPMC."IdInterno" = IE."IdIngresoEgreso" AND FPMC."Tipo" = 'IngresoEgreso'
  --LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CPOR ON CPOR."IdPortafolio" = IE."IdPortafolio"
  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UC ON UC."IdUsuario" = IE."IdUsuarioEmision"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVE ON EVE."IdEvento" = IE."IdEvento"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TEV ON (EVE."IdTipoEvento" = TEV."IdTipoEvento" AND TEV."IdEmpresa" = EVE."IdEmpresa")   
  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS FAC ON FAC."IdComprobante" = IE."IdOperacion" AND FAC."IdTipoOperacion" = IE."IdTipoOperacion"
  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion" AS EAU ON (EAU."IdEstado" = EVE."IdEstado" AND upper(EAU."NombreEntidad") = upper('AUT.Evento'))
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS m ON (m."IdMatrizAgrupadorAPV" = PORL."IdMatrizAgrupadorAPV")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCOMP ON (CD."IdEstado" = ESTCOMP."IdEstado") 
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" ctaPortafolioDcv ON  IE."IdPortafolio" = ctaPortafolioDcv."IdPortafolio"  AND ctaPortafolioDcv."IdBoveda" = 'DCV' AND ctaPortafolioDcv."IdEstado" = 'ACT' AND ctaPortafolioDcv."IdEstadoAutorizacion" = 'AUT'
  LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CONLIQFAC ON CONLIQFAC."IdCondicionDeLiquidacion" = FAC."IdCondicionLiquidacion"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CONLIQ ON CONLIQ."IdCondicionDeLiquidacion" = IE."IdCondicionLiquidacion"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = IE."IdEmpleadoDigitador"
  WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



