SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.FondoMutuo.CrystalReports.Servicios::TrapasoMLT";
GO
CREATE PROCEDURE
"BSCL.FondoMutuo.CrystalReports.Servicios::TrapasoMLT" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
	IN p_IdTraspaso  							BIGINT
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	BEGIN
	
		BEGIN
		
			---------------------
			--    Variables    --
			---------------------
			DECLARE v_NombreCompletoEmisorOrigen,v_NombreCompletoEmisorDestino			NVARCHAR(520) 	:='';
			DECLARE v_DocumentoIdentidadEmisorOrigen,v_DocumentoIdentidadEmisorDestino	NVARCHAR(15) 	:='';
			DECLARE v_SegundosZonaHoraria       BIGINT;
			
			SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
			
			  	SELECT TOP 1 
					 COALESCE(EMISOR."NombreCompleto",'')   				AS "NombreCompletoEmisor"
					,COALESCE(EMISOR."DocumentoIdentidad",'') 				AS "DocumentoIdentidadEmisor"
			  	INTO v_NombreCompletoEmisorOrigen, v_DocumentoIdentidadEmisorOrigen
			  	FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" 		AS O 
				  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   ON  ( SERIEFONDO."IdSerieFondo" 			=  O."IdSerieFondo")
				  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 		ON  ( FONDO."IdFondo" 						=  SERIEFONDO."IdFondo")
				  LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"		AS EMISOR		ON  ( EMISOR."IdEmisor" 					=  FONDO."IdEmisor")
				WHERE O."IdTraspaso"= :p_IdTraspaso;
			
			 	SELECT TOP 1 
					 COALESCE(EMISOR."NombreCompleto",'')   				AS "NombreCompletoEmisor"
					,COALESCE(EMISOR."DocumentoIdentidad",'') 				AS "DocumentoIdentidadEmisor"
			  	INTO v_NombreCompletoEmisorDestino, v_DocumentoIdentidadEmisorDestino
			  	FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" 		AS O 
				  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   ON  ( SERIEFONDO."IdSerieFondo" 			=  O."IdSerieFondo")
				  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 		ON  ( FONDO."IdFondo" 						=  SERIEFONDO."IdFondo")
				  LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"		AS EMISOR		ON  ( EMISOR."IdEmisor" 					=  FONDO."IdEmisor")
				WHERE O."IdTraspaso"= :p_IdTraspaso;
			
			
			
			
				/*DETALLE ORIGEN */
				v_resultado = 	
							SELECT 
										 TRASPASO."IdTraspaso"
										,DETALLE."ROW",DETALLE."Destino_ROW"
									    ,TRASPASO."IdEmpresa"
									    ,TRASPASO."IdModulo"
										,TRASPASO."IdUsuario"
									    ,TRASPASO."IdCliente"
									    ,TRASPASO."IdPortafolio"
									 	,TRASPASO."IdEmisor"
										,TRASPASO."IdMotivoTraspaso"
										,TRASPASO."IdTipoMovimientoTraspaso"
									    ,TRASPASO."IdTipoTraspaso"
									    ,COALESCE(TIPOTRAS."Descripcion",'')		AS "TipoTraspaso"
									    ,TRASPASO."Glosa"
									    ,TRASPASO."FechaHoraSuscripcion"
									    ,TO_VARCHAR(TRASPASO."FechaHoraSuscripcion",'DD-MM-YYYY')	AS "FechaSuscripcion"
										,TO_VARCHAR(ADD_SECONDS(TRASPASO."FechaHoraSuscripcion",:v_SegundosZonaHoraria), 'HH24:MI')		AS "HoraSuscripcion"
									    ,TRASPASO."IdDivisionPais"
									    ,COALESCE(LUG."NombreDivisionPaisClasificacion1",'')  AS "LugarSuscripcion"
									    ,TRASPASO."FolioTraspaso"
									    ,TRASPASO."IdEstado"
									    ,TRASPASO."Monto" AS "MontoTraspaso"
									    ,TRASPASO."FechaIngreso"
									    ,TRASPASO."FechaAceptacion"
									    ,TRASPASO."FechaAnulacion"
									    ,TRASPASO."FechaModificacion"
									    ,TRASPASO."FechaIngresoRegistro"
									    ,TRASPASO."FechaAceptacionRegistro" 
									    ,TRASPASO."FechaAnulacionRegistro" 
									    ,TRASPASO."FechaModificacionRegistro"
									    ,TRASPASO."FolioMLT"
										,TRASPASO."FolioCSC"
										,TRASPASO."IdEmpleadoEjecutivo"
										,CLIENTE."RazonSocial"
										,CLIENTE."NombreCompleto"
										,CLIENTE."DocumentoIdentidad"
										,CLIENTE."IdPersona"
										,COALESCE(DIR."NombreLargo",'') 					AS "DireccionFisicaCliente"
										,COALESCE(REP."NombreCompleto",'')					AS "NombreRepresentante"
										,COALESCE(REP."DocumentoIdentidad",'')				AS "DocumentoIdentidadRepresentante"
								    	---DETALLE ORIGEN
										,COALESCE(DETALLE."IdSerieFondo",'')				AS "IdSerieFondo"
										,COALESCE(DETALLE."Cantidad",0)						AS "Cantidad"
										,COALESCE(DETALLE."NombreSerieFondo",'')			AS "NombreSerieFondo"
										,COALESCE(DETALLE."CodigoSerie",'')					AS "CodigoSerie"
										,COALESCE(DETALLE."DecimalesValorCuota",0)			AS "DecimalesValorCuota"
										,COALESCE(DETALLE."DecimalesCuota",0)				AS "DecimalesCuota"
										,v_NombreCompletoEmisorOrigen   					AS "NombreCompletoEmisor"
										,v_DocumentoIdentidadEmisorOrigen				 	AS "DocumentoIdentidadEmisor"
										,COALESCE(DETALLE."IdSolicitudAporteRescate",0)		AS "IdSolicitudAporteRescate"
										,COALESCE(DETALLE."IdTipoMovimientoFondoMutuo",'')	AS "IdTipoMovimientoFondoMutuo"
										,COALESCE(DETALLE."FechaCertificacion",'')			AS "FechaCertificacion"
										,COALESCE(DETALLE."FechaLiquidacion",'')			AS "FechaLiquidacion"
										,COALESCE(DETALLE."IdMoneda",'')					AS "IdMoneda"
										,COALESCE(DETALLE."Monto",0)						AS "Monto"
										,COALESCE(DETALLE."NumeroCuotas",0)					AS "NumeroCuotas"
										,COALESCE(DETALLE."ValorCuota",0)					AS "ValorCuota"
										,COALESCE(DETALLE."CostoUF",0)						AS "CostoUF"
										,COALESCE(DETALLE."IdMonedaSistema",'')				AS "IdMonedaSistema"
										,COALESCE(DETALLE."MontoMonedaSistema",0)			AS "MontoMonedaSistema"
										,COALESCE(DETALLE."EstadoSolicitud",'')				AS "EstadoSolicitud"
										,COALESCE(DETALLE."EstadoLiquidacion",'')			AS "EstadoLiquidacion"
										---DETALLE DESTINO
										,COALESCE(DETALLE."Destino_IdSerieFondo",'')				AS "Destino_IdSerieFondo"
										,COALESCE(DETALLE."Destino_Porcentaje",0)					AS "Destino_Porcentaje"
										,COALESCE(DETALLE."Destino_NombreSerieFondo",'')			AS "Destino_NombreSerieFondo"
										,COALESCE(DETALLE."Destino_CodigoSerie",'')					AS "Destino_CodigoSerie"
										,COALESCE(DETALLE."Destino_DecimalesValorCuota",0)			AS "Destino_DecimalesValorCuota"
										,COALESCE(DETALLE."Destino_DecimalesCuota",0)				AS "Destino_DecimalesCuota"
										,v_NombreCompletoEmisorDestino   							AS "Destino_NombreCompletoEmisor"
										,v_DocumentoIdentidadEmisorDestino						 	AS "Destino_DocumentoIdentidadEmisor"
										,COALESCE(DETALLE."Destino_IdSolicitudAporteRescate",0)		AS "Destino_IdSolicitudAporteRescate"
										,COALESCE(DETALLE."Destino_IdTipoMovimientoFondoMutuo",'')	AS "Destino_IdTipoMovimientoFondoMutuo"
										,COALESCE(DETALLE."Destino_FechaCertificacion",'')			AS "Destino_FechaCertificacion"
										,COALESCE(DETALLE."Destino_FechaLiquidacion",'')			AS "Destino_FechaLiquidacion"
										,COALESCE(DETALLE."Destino_IdMoneda",'')					AS "Destino_IdMoneda"
										,COALESCE(DETALLE."Destino_Monto",0)						AS "Destino_Monto"
										,COALESCE(DETALLE."Destino_NumeroCuotas",0)					AS "Destino_NumeroCuotas"
										,COALESCE(DETALLE."Destino_ValorCuota",0)					AS "Destino_ValorCuota"
										,COALESCE(DETALLE."Destino_CostoUF",0)						AS "Destino_CostoUF"
										,COALESCE(DETALLE."Destino_IdMonedaSistema",'')				AS "Destino_IdMonedaSistema"
										,COALESCE(DETALLE."Destino_MontoMonedaSistema",0)			AS "Destino_MontoMonedaSistema"
										,COALESCE(DETALLE."Destino_EstadoSolicitud",'')				AS "Destino_EstadoSolicitud"
										,COALESCE(DETALLE."Destino_EstadoLiquidacion",'')			AS "Destino_EstadoLiquidacion"
										, CASE CLIENTE."Tipo" WHEN 'N' THEN 'X' ELSE '' END 		AS "PersonaNatural"
										, CASE CLIENTE."Tipo" WHEN 'J' THEN 'X' ELSE '' END 		AS "PersonaJuridica"
										,CASE TRASPASO."IdTipoMovimientoTraspaso" WHEN 'TRAING' THEN  COALESCE(TRASPASO."Monto",0) ELSE  COALESCE(DETALLE."Monto",0)	END AS "MontoOrigen"
										--,CASE TRASPASO."IdTipoMovimientoTraspaso" WHEN 'TRAING' THEN  COALESCE(TRASPASO."Monto",0 ELSE  COALESCE(DETALLE."Monto",0)	END AS "PorcentajeOrigen"
										
							FROM 	"BSCL.FondoMutuo.BD::TRA.Traspaso" 							AS TRASPASO
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		AS CLIENTE 			ON  ( CLIENTE."IdCliente" = TRASPASO."IdCliente" )
							INNER JOIN
							(
							
										SELECT ORI.*
											,DES."ROW"							AS "Destino_ROW"
											,DES."IdTraspaso"					AS "Destino_IdTraspaso"
											,DES."IdSerieFondo" 				AS "Destino_IdSerieFondo"
											,DES."Porcentaje"					AS "Destino_Porcentaje"
											,DES."NombreSerieFondo"				AS "Destino_NombreSerieFondo"
											,DES."CodigoSerie" 					AS "Destino_CodigoSerie"
											,DES."DecimalesValorCuota" 			AS "Destino_DecimalesValorCuota"
											,DES."DecimalesCuota" 				AS "Destino_DecimalesCuota"
											---Solicitud
											,DES."IdSolicitudAporteRescate" 	AS "Destino_IdSolicitudAporteRescate"
											,DES."IdTipoMovimientoFondoMutuo" 	AS "Destino_IdTipoMovimientoFondoMutuo"
											,DES."FechaCertificacion" 			AS "Destino_FechaCertificacion"
											,DES."FechaLiquidacion" 			AS "Destino_FechaLiquidacion"
											,DES."IdMoneda" 					AS "Destino_IdMoneda"
											,DES."Monto" 						AS "Destino_Monto"
											,DES."NumeroCuotas" 				AS "Destino_NumeroCuotas"
											,DES."ValorCuota" 					AS "Destino_ValorCuota"
											,DES."CostoUF" 						AS "Destino_CostoUF"
											,DES."IdMonedaSistema" 				AS "Destino_IdMonedaSistema"
											,DES."MontoMonedaSistema" 			AS "Destino_MontoMonedaSistema"
											,DES."EstadoSolicitud" 				AS "Destino_EstadoSolicitud"
											,DES."EstadoLiquidacion" 			AS "Destino_EstadoLiquidacion"	
										FROM 
												(SELECT ROW_NUMBER() OVER (PARTITION BY O."IdTraspaso")AS "ROW"
														---Datos del Origen
														,O."IdTraspaso"
														,O."IdDetalleTraspaso"
														,O."IdSerieFondo"
														,O."Cantidad"
														,COALESCE(SERIEFONDO."NombreSerieFondo",'')				AS "NombreSerieFondo"
														,COALESCE(SERIEFONDO."CodigoSerie",'')					AS "CodigoSerie"
														,COALESCE(SERIEFONDO."DecimalesValorCuota",0)			AS "DecimalesValorCuota"
														,COALESCE(SERIEFONDO."DecimalesCuota",0)				AS "DecimalesCuota"
														---Solicitud
														,COALESCE(SOLICITUD."IdSolicitudAporteRescate",0)		AS "IdSolicitudAporteRescate"
														,COALESCE(SOLICITUD."IdTipoMovimientoFondoMutuo",'')	AS "IdTipoMovimientoFondoMutuo"
														,COALESCE(SOLICITUD."FechaCertificacion",'')			AS "FechaCertificacion"
														,COALESCE(SOLICITUD."FechaLiquidacion",'')				AS "FechaLiquidacion"
														,COALESCE(SOLICITUD."IdMoneda",'')						AS "IdMoneda"
														,COALESCE(SOLICITUD."Monto",0)							AS "Monto"
														,COALESCE(SOLICITUD."NumeroCuotas",0)					AS "NumeroCuotas"
														,COALESCE(SOLICITUD."ValorCuota",0)						AS "ValorCuota"
														,COALESCE(SOLICITUD."CostoUF",0)						AS "CostoUF"
														,COALESCE(SOLICITUD."IdMonedaSistema",'')				AS "IdMonedaSistema"
														,COALESCE(SOLICITUD."MontoMonedaSistema",0)				AS "MontoMonedaSistema"
														,COALESCE(SOLICITUD."Estado",'')						AS "EstadoSolicitud"
														,COALESCE(SOLICITUD."EstadoLiquidacion",'')				AS "EstadoLiquidacion"
													  FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" 		AS O 
													  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   ON  ( SERIEFONDO."IdSerieFondo" 			=  O."IdSerieFondo")
													  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 		ON  ( FONDO."IdFondo" 						=  SERIEFONDO."IdFondo")
													  LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"   	AS SOLICITUD 	ON  ( SOLICITUD."IdSolicitudAporteRescate" 	=  O."IdSolicitudAporteRescate")
													  WHERE O."IdEstado"='ACT'
												  ORDER BY O."IdDetalleTraspaso"
												  
												) AS ORI
										FULL JOIN 
												(SELECT ROW_NUMBER() OVER (PARTITION BY D."IdTraspaso")AS "ROW"
														--Datos del Destino
														,D."IdTraspaso"
													    ,D."IdDetalleTraspaso"
														,D."IdSerieFondo"
														,D."Porcentaje"
														,COALESCE(SERIEFONDO."NombreSerieFondo",'')				AS "NombreSerieFondo"
														,COALESCE(SERIEFONDO."CodigoSerie",'')					AS "CodigoSerie"
														,COALESCE(SERIEFONDO."DecimalesValorCuota",0)			AS "DecimalesValorCuota"
														,COALESCE(SERIEFONDO."DecimalesCuota",0)				AS "DecimalesCuota"
														---Solicitud
														,COALESCE(SOLICITUD."IdSolicitudAporteRescate",0)		AS "IdSolicitudAporteRescate"
														,COALESCE(SOLICITUD."IdTipoMovimientoFondoMutuo",'')	AS "IdTipoMovimientoFondoMutuo"
														,COALESCE(SOLICITUD."FechaCertificacion",'')			AS "FechaCertificacion"
														,COALESCE(SOLICITUD."FechaLiquidacion",'')				AS "FechaLiquidacion"
														,COALESCE(SOLICITUD."IdMoneda",'')						AS "IdMoneda"
														,COALESCE(SOLICITUD."Monto",0)							AS "Monto"
														,COALESCE(SOLICITUD."NumeroCuotas",0)					AS "NumeroCuotas"
														,COALESCE(SOLICITUD."ValorCuota",0)						AS "ValorCuota"
														,COALESCE(SOLICITUD."CostoUF",0)						AS "CostoUF"
														,COALESCE(SOLICITUD."IdMonedaSistema",'')				AS "IdMonedaSistema"
														,COALESCE(SOLICITUD."MontoMonedaSistema",0)				AS "MontoMonedaSistema"
														,COALESCE(SOLICITUD."Estado",'')						AS "EstadoSolicitud"
														,COALESCE(SOLICITUD."EstadoLiquidacion",'')				AS "EstadoLiquidacion"
												     FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino"		AS D 
												     LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   ON  ( SERIEFONDO."IdSerieFondo" 			=  D."IdSerieFondo")
													 LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 		ON  ( FONDO."IdFondo" 						=  SERIEFONDO."IdFondo")
													 LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"   	AS SOLICITUD 	ON  ( SOLICITUD."IdSolicitudAporteRescate" 	=  D."IdSolicitudAporteRescate")
													 WHERE D."IdEstado"='ACT'
												  ORDER BY D."IdDetalleTraspaso") AS DES
										ON (ORI."IdTraspaso"=DES."IdTraspaso" AND  DES."ROW"= ORI."ROW")
										
							) AS DETALLE ON  ( COALESCE(DETALLE."IdTraspaso",DETALLE."Destino_IdTraspaso")	=  TRASPASO."IdTraspaso")
							LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::TRA.TipoTraspaso"		AS TIPOTRAS		ON  ( TIPOTRAS."IdTipoTraspaso" =  TRASPASO."IdTipoTraspaso")
							LEFT OUTER  JOIN  "BSCL.SocioNegocio.BD::PER.Persona" 		AS PER 			ON  ( CLIENTE."IdPersona" = PER."IdPersona")
							LEFT OUTER  JOIN  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON  ( DIR."IdDireccionFisica" = PER."IdDireccionFisicaPrincipal")
							LEFT OUTER  JOIN  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 				 AS REP ON  ( PER."IdPersonaRepresentante" = REP."IdPersona")
							LEFT OUTER  JOIN  "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" 			 AS LUG ON  ( LUG."IdDivisionPais" = TRASPASO."IdDivisionPais")
							WHERE TRASPASO."IdTraspaso"= :p_IdTraspaso
							ORDER BY TRASPASO."IdTraspaso",COALESCE(DETALLE."ROW",-1) + COALESCE(DETALLE."Destino_ROW",-1) DESC;
		
	    
	    v_datosGenerales = select 
        "RazonSocial" AS "RazonCorredora", 
        "Direccion" AS "DireccionCorredora", 
        "RUT" AS "RutCorredora", 
        "Telefono" AS "TelefonoCorredora" 
        from "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
        WHERE "IdEmpresa" = p_IdEmpresa;
        
	    v_logo = select TOP 1
        "Binario" AS "LogoCorredora"
        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		
	    SELECT *
		FROM :v_resultado
		CROSS JOIN :v_logo
		CROSS JOIN :v_datosGenerales;
		END; 
	END;
END;
GO
		
