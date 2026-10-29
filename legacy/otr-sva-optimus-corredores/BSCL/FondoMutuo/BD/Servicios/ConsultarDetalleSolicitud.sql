SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarDetalleSolicitud";
GO
CREATE PROCEDURE
--  Proposito: Consultar detalle de una solicitud, si no esta certificado simula su cetificacion .
--  Autor: Gat-Blac 
--  Fecha Creacion: 03/08/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarDetalleSolicitud" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    
    IN p_SolicitudesAporteRescate		TABLE(
    										"IdInterno"					BIGINT,
    										"IdSolicitudAporteRescate"	BIGINT,
    										"SimularCertificacion"		NVARCHAR(1)
    									),
    OUT p_out_Resultado					"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI",
    
    OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 		NVARCHAR(40);
		DECLARE v_count					BIGINT;
		DECLARE v_count_solicitudes		BIGINT;
		
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR DETALLE SOLICITUD','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) as "Count" into v_count_solicitudes
		FROM :p_SolicitudesAporteRescate;
		
		if (v_count_solicitudes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_SolicitudesAporteRescate
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_solicitud = SELECT
							  "p_solicitudes"."IdInterno"
							, "solicitudes"."IdSolicitudAporteRescate"
							, "solicitudes"."IdCliente"
							, "solicitudes"."IdPortafolio"
							, "solicitudes"."FechaEmision"
							, "solicitudes"."FechaIngresoRegistro"
							, "solicitudes"."IdTipoMovimientoFondoMutuo"
							, "solicitudes"."TipoSolicitud"
							, "solicitudes"."DescripcionTipoMovimiento"
							, "solicitudes"."CalculoMontoCuotaUI"
							, "solicitudes"."EstadoSolicitud"
							, "solicitudes"."NombreEstado"
							, "solicitudes"."NombreFondo"
							, "solicitudes"."NombreFondoReal"
							, "solicitudes"."IdSerieFondo"
							, "solicitudes"."NombreSerieFondo"
							, "solicitudes"."CalculoPorMontoCuotaUI"
							, "solicitudes"."NumeroCuotas"
							, "solicitudes"."ValorCuota"
							, "solicitudes"."Monto"
							, "solicitudes"."DocumentoIdentidad"
							, "solicitudes"."NombreCompleto"
							--
							, "solicitudes"."Nombres"
							, "solicitudes"."ApellidoPaterno"
							, "solicitudes"."ApellidoMaterno"
							, "solicitudes"."IdInstrumento"
							, "solicitudes"."IdRegimenTributario"
							--
							, "solicitudes"."DescripcionPortafolio"
							, "solicitudes"."FechaCertificacion"
							, "solicitudes"."FechaLiquidacion"
							, "solicitudes"."FechaAceptacion"
							, "solicitudes"."FechaAnulacion"
							, "solicitudes"."FechaAceptacionRegistro"
							, "solicitudes"."FechaCertificacionRegistro"
							, "solicitudes"."FechaLiquidacionRegistro"
							, "solicitudes"."FechaAnulacionRegistro"
							, "solicitudes"."NombreEmpleado"
							, "solicitudes"."IdEmisor"
							, "solicitudes"."IdEmpresa"
							, "solicitudes"."IdMoneda"
							, "solicitudes"."TextoPreferenciaPago"
							, "solicitudes"."Glosa"
							, "solicitudes"."DecimalesValorCuota"
							, "solicitudes"."DecimalesCuota"
							, "solicitudes"."EsOvernightUI"
							, "solicitudes"."IdSolicitudAporteRescateRelacionado"
							, "solicitudes"."DescripcionPortafolioDestino"
							, "solicitudes"."IdPortafolioDestino"
							, "solicitudes"."IdClienteDestino"
							, "solicitudes"."DocumentoIdentidadDestino"
							, "solicitudes"."NombreCompletoDestino"
							, "solicitudes"."EsIndicadorMLTUI"
							, "solicitudes"."MontoComision"
							, "solicitudes"."IdTipoMovimientoFondoMutuoRelacionado"
							, "solicitudes"."DescripcionTipoMovimientoRelacionado"
							, "solicitudes"."IdMonedaFondo"
							, "solicitudes"."EsDiferido"
							, "solicitudes"."EsDiferidoUI"
							, "solicitudes"."CostoMonedaLocal"
							, "solicitudes"."ParidadFechaInversion"
							, "solicitudes"."MontoInversion"
							, "solicitudes"."NumeroCuotasInversion"
							, "solicitudes"."ValorCuotaInversion"
							, "solicitudes"."CostoMonedaLocalInversion"
							, "solicitudes"."ParidadFechaCertificacion"
							, "solicitudes"."CalculoPorMontoCuota"
							, "solicitudes"."IdSerieFondoDestino"
							, "solicitudes"."IdEmisorDestino"
							, "p_solicitudes"."SimularCertificacion"
							, "solicitudes"."SolicitudBatch"
							, "solicitudes"."OrigenOperacion"
							, "solicitudes"."ComisionInformada"
							, "solicitudes"."EstadoLiquidacion"
							, "solicitudes"."NombreEstadoLiquidacion"
							, "solicitudes"."CostoUF"
							, "solicitudes"."ValorUFFechaInversion"
							, "solicitudes"."Spread"
							, "solicitudes"."FechaOriginalInversion"
							, "solicitudes"."MontoMovimientoAsociado"
							, "solicitudes"."NumeroCuotasMovimientoAsociado"
							, "solicitudes"."IndicadorMLT"
							, "solicitudes"."IdEmisorOrigen"
							, "solicitudes"."CodigoPortafolio"
							, "solicitudes"."CodigoSerie"
							, "solicitudes"."IdFondoRelacionado"
							, "solicitudes"."CodigoSerieRelacionado"
							, "solicitudes"."DescripcionRegimenTributario"
							, "solicitudes"."IdFondo"
							, "solicitudes"."DescripcionTipoSolicitud"
							, "solicitudes"."MontoMonedaSistema"
							, "solicitudes"."Switch"
                            , "solicitudes"."SwitchUI"
                            , "solicitudes"."IdTipoCanalIngreso","solicitudes"."FolioContrato"
                            , "solicitudes"."Producto"
                            , "solicitudes"."IdProductoAgrupador"
                            , "solicitudes"."IdDividendo"
                            , "solicitudes"."FolioCartaLiquidacion"
                            , "solicitudes"."CodigoUsuario"
                            ---SPOT
						 	, "solicitudes"."IdIngresoEgreso"
							, "solicitudes"."NombreEstadoTesoreria" 	
							, "solicitudes"."FechaLiquidacionTesoreria"
							, "solicitudes"."IdOperacionSpot"
							, "solicitudes"."NombreEstadoSpot"
							, "solicitudes"."NombreEstadoConfirmacionSpot"
							, "solicitudes"."FechaLiquidacionSpot"
							, "solicitudes"."IdTipoOperacionSpot"
							, "solicitudes"."NombreTipoOperacionSpot"
							--OMNIBUS
							, "solicitudes"."IdOmnibus"
							--REINVERSION 1N
							, "solicitudes"."IdAgrupador"
							FROM :p_SolicitudesAporteRescate AS "p_solicitudes"
							INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" AS "solicitudes" ON ("p_solicitudes"."IdSolicitudAporteRescate" = "solicitudes"."IdSolicitudAporteRescate");
		
		v_valores_simulados = SELECT "Solicitudes"."IdInterno"
									 ,CASE "Solicitudes"."TipoSolicitud" WHEN 'RES' THEN "FechaCertificacion" ELSE "FechaEmision" END AS "Fecha"
									 ,"Solicitudes"."IdSerieFondo"
									 ,"Solicitudes"."CalculoPorMontoCuota"
									 ,"Solicitudes"."CostoMonedaLocal" AS "Monto"
									 ,"Solicitudes"."NumeroCuotas"
									 ,"Solicitudes"."IdMonedaFondo" AS "IdMonedaFondo"
									 ,"Solicitudes"."IdMoneda" AS "IdMonedaInversion"
									 ,"Solicitudes"."TipoSolicitud"
									 ,'UF' AS "TipoFactorAjuste"
									 ,"Solicitudes"."IdEmpresa"
							  FROM :v_solicitud AS "Solicitudes"
							  WHERE "Solicitudes"."SimularCertificacion" = 'S';
							  
		CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(:p_IdEmpresa,:v_valores_simulados,v_valores_cuota);
		
		p_out_Resultado = SELECT 
							  "solicitudes"."IdSolicitudAporteRescate"
							, "solicitudes"."IdCliente"
							, "solicitudes"."IdPortafolio"
							, "solicitudes"."FechaEmision"
							, "solicitudes"."FechaIngresoRegistro"
							, "solicitudes"."IdTipoMovimientoFondoMutuo"
							, "solicitudes"."TipoSolicitud"
							, "solicitudes"."DescripcionTipoMovimiento"
							, "solicitudes"."CalculoMontoCuotaUI"
							, "solicitudes"."EstadoSolicitud"
							, "solicitudes"."NombreEstado"
							, "solicitudes"."NombreFondo"
							, "solicitudes"."NombreFondoReal"
							, "solicitudes"."IdSerieFondo"
							, "solicitudes"."NombreSerieFondo"
							, "solicitudes"."CalculoPorMontoCuotaUI"
							
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."NumeroCuotas" ELSE "solicitudes"."NumeroCuotas" 	END AS "NumeroCuotas"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."ValorCuota"   ELSE "solicitudes"."ValorCuota" 	END AS "ValorCuota"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."Monto"	    ELSE "solicitudes"."Monto"		  	END	AS "Monto"
							, "solicitudes"."DocumentoIdentidad"
							--
							, "solicitudes"."NombreCompleto"
							, "solicitudes"."Nombres"
							, "solicitudes"."ApellidoPaterno"
							, "solicitudes"."ApellidoMaterno"
							, "solicitudes"."IdInstrumento"
							, "solicitudes"."IdRegimenTributario"
							--
							, "solicitudes"."DescripcionPortafolio"
							, "solicitudes"."FechaCertificacion"
							, "solicitudes"."FechaLiquidacion"
							, "solicitudes"."FechaAceptacion"
							, "solicitudes"."FechaAnulacion"
							, "solicitudes"."FechaAceptacionRegistro"
							, "solicitudes"."FechaCertificacionRegistro"
							, "solicitudes"."FechaLiquidacionRegistro"
							, "solicitudes"."FechaAnulacionRegistro"
							, "solicitudes"."NombreEmpleado"
							, "solicitudes"."IdEmisor"
							, "solicitudes"."IdEmpresa"
							, "solicitudes"."IdMoneda"
							, "solicitudes"."TextoPreferenciaPago"
							, "solicitudes"."Glosa"
							, "solicitudes"."DecimalesValorCuota"
							, "solicitudes"."DecimalesCuota"
							, "solicitudes"."EsOvernightUI"
							, "solicitudes"."IdSolicitudAporteRescateRelacionado"
							, "solicitudes"."DescripcionPortafolioDestino"
							, "solicitudes"."IdPortafolioDestino"
							, "solicitudes"."IdClienteDestino"
							, "solicitudes"."DocumentoIdentidadDestino"
							, "solicitudes"."NombreCompletoDestino"
							, "solicitudes"."EsIndicadorMLTUI"
							, "solicitudes"."MontoComision"
							, "solicitudes"."IdTipoMovimientoFondoMutuoRelacionado"
							, "solicitudes"."DescripcionTipoMovimientoRelacionado"
							, "solicitudes"."IdMonedaFondo"
							, "solicitudes"."EsDiferido"
							, "solicitudes"."EsDiferidoUI"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."MontoDestino" ELSE "solicitudes"."CostoMonedaLocal" END AS "CostoMonedaLocal"
							, "solicitudes"."ParidadFechaInversion"
							, "solicitudes"."MontoInversion"
							, "solicitudes"."NumeroCuotasInversion"
							, "solicitudes"."ValorCuotaInversion"
							, "solicitudes"."CostoMonedaLocalInversion"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."ValorParidad" ELSE "solicitudes"."ParidadFechaCertificacion" END AS "ParidadFechaCertificacion"
							, "solicitudes"."CalculoPorMontoCuota"
							, "solicitudes"."IdSerieFondoDestino"
							, "solicitudes"."IdEmisorDestino"
							, "solicitudes"."SolicitudBatch"
							, "solicitudes"."OrigenOperacion"
							, "solicitudes"."ComisionInformada"
							, "solicitudes"."EstadoLiquidacion"
							, "solicitudes"."NombreEstadoLiquidacion"
							, "solicitudes"."FolioCartaLiquidacion"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."CostoEnFactorAjuste" ELSE "solicitudes"."CostoUF" END AS "CostoUF"
							, CASE "solicitudes"."SimularCertificacion" WHEN 'S' THEN "ValoresSimulados"."ValorFactorAjuste" ELSE "solicitudes"."ValorUFFechaInversion" END AS "ValorUFFechaInversion"
							, "solicitudes"."Spread"
							, "solicitudes"."FechaOriginalInversion"
							, "solicitudes"."MontoMovimientoAsociado"
							, "solicitudes"."NumeroCuotasMovimientoAsociado"
							, "solicitudes"."IndicadorMLT"
							, "solicitudes"."IdEmisorOrigen"
							, "solicitudes"."CodigoPortafolio"
							, "solicitudes"."CodigoSerie"
							, "solicitudes"."IdFondoRelacionado"
							, "solicitudes"."CodigoSerieRelacionado"
							, "solicitudes"."DescripcionRegimenTributario"
							, "solicitudes"."IdFondo"
							, "solicitudes"."DescripcionTipoSolicitud"
							, "solicitudes"."MontoMonedaSistema"
							, "solicitudes"."IdDividendo"
							, "solicitudes"."Switch"
                            , "solicitudes"."SwitchUI"
                            , "solicitudes"."IdTipoCanalIngreso","solicitudes"."FolioContrato"
                            , "solicitudes"."Producto"
                            , "solicitudes"."IdProductoAgrupador"
                            , "solicitudes"."CodigoUsuario"
                            ---SPOT
						 	, "solicitudes"."IdIngresoEgreso"
							, "solicitudes"."NombreEstadoTesoreria" 	
							, "solicitudes"."FechaLiquidacionTesoreria"
							, "solicitudes"."IdOperacionSpot"
							, "solicitudes"."NombreEstadoSpot"
							, "solicitudes"."NombreEstadoConfirmacionSpot"
							, "solicitudes"."FechaLiquidacionSpot"
							, "solicitudes"."IdTipoOperacionSpot"
							, "solicitudes"."NombreTipoOperacionSpot"
							--OMNIBUS
							, "solicitudes"."IdOmnibus"
							, "solicitudes"."IdAgrupador"
					FROM :v_solicitud AS "solicitudes"
					INNER JOIN :p_SolicitudesAporteRescate AS "p_solicitudes" ON ("solicitudes"."IdInterno" = "p_solicitudes"."IdInterno")
					LEFT OUTER JOIN :v_valores_cuota AS "ValoresSimulados" ON ("ValoresSimulados"."IdInterno" = "p_solicitudes"."IdInterno");
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR DETALLE SOLICITUD','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
GO
