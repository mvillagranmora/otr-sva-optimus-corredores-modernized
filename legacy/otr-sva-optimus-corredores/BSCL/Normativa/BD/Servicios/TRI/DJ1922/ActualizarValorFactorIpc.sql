SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.ActualizarValorFactorIpc";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.ActualizarValorFactorIpc"
(
	-- ID empresa del login.
    IN p_IdEmpresa 	BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 	BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo	NVARCHAR(3)	 ,
    
    IN p_Version	NVARCHAR(10),
    
    OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE sqlscript
    SQL security invoker
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
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_FechaSistema		NVARCHAR(10);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.ActualizarValorFactorIpc','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_montos = SELECT DJ1922."IdOperacion"
							  ,DJ1922."FechaInversion"
							  ,DJ1922."MontoNominalAportes"
							  ,DJ1922."DiferenciaPorRescateOEnajenacion"
							  ,DJ1922."DifPorRescateOEnajenacionCFIAcogido107"
							  ,DJ1922."DiferenciaValorCuotasFechaLiquidacion"
							  ,DJ1922."DividendosAfectosIGCoAdicional"
							  ,DJ1922."DividendosExentosIGCoAdicional"
							  ,DJ1922."DividendosNoConstitutivosDeRenta"
							  ,DJ1922."DividendosAfectosImptoUnicoTasa10"
							  ,DJ1922."DividendosNoAfectosImptoUnicoTasa10"
							  ,DJ1922."DividendosAfectosImptoUnicoTasa4"
							  ,DJ1922."DevolucionesDeCapital"
					   FROM "BSCL.Normativa.BD::TRI.DatosDJ1922" DJ1922
					   INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" DOCUMENTO ON (DJ1922."IdDocumento" = DOCUMENTO."IdDocumento")
					   WHERE DOCUMENTO."Version" = :p_Version;
			
			v_ipc = SELECT "Factor"
						   ,"Agno"
						   ,"AgnoMes"
					FROM "BSCL.Normativa.BD::TRI.FactorIpc" 
					WHERE "AgnoTributario" = :p_Version;
			
			v_montos_actualizados = SELECT MONTOS."IdOperacion",
										   MONTOS."MontoNominalAportes" * IPC."Factor" AS "MontoNominalAportesActualizado",
										   MONTOS."DiferenciaPorRescateOEnajenacion" *  IPC."Factor" AS "DiferenciaPorRescateOEnajenacionActualizado",
										   MONTOS."DifPorRescateOEnajenacionCFIAcogido107" * IPC."Factor" AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado",
										   MONTOS."DiferenciaValorCuotasFechaLiquidacion" * IPC."Factor" AS "DiferenciaValorCuotasFechaLiquidacionActualizado",
										   MONTOS."DividendosAfectosIGCoAdicional" * IPC."Factor" AS "DividendosAfectosIGCoAdicionalActualizado",
										   MONTOS."DividendosExentosIGCoAdicional" * IPC."Factor" AS "DividendosExentosIGCoAdicionalActualizado",
										   MONTOS."DividendosNoConstitutivosDeRenta" * IPC."Factor" AS "DividendosNoConstitutivosDeRentaActualizado",
										   MONTOS."DividendosAfectosImptoUnicoTasa10" * IPC."Factor" AS "DividendosAfectosImptoUnicoTasa10Actualizado",
										   MONTOS."DividendosNoAfectosImptoUnicoTasa10" * IPC."Factor" AS "DividendosNoAfectosImptoUnicoTasa10Actualizado",
										   MONTOS."DividendosAfectosImptoUnicoTasa4" * IPC."Factor" AS "DividendosAfectosImptoUnicoTasa4Actualizado",
										   MONTOS."DevolucionesDeCapital" * IPC."Factor" AS "DevolucionesDeCapitalActualizado",
										   IPC."Factor" AS "FactorActualizacion"
									FROM :v_montos MONTOS
									INNER JOIN :v_ipc IPC ON ((TO_CHAR(MONTOS."FechaInversion",'YYYY-MM') = IPC."AgnoMes"));
									
			UPDATE DJ1922
			SET "MontoNominalAportesActualizado" = DJ."MontoNominalAportesActualizado",
				"DiferenciaPorRescateOEnajenacionActualizado" = DJ."DiferenciaPorRescateOEnajenacionActualizado",
				"DifPorRescateOEnajenacionCFIAcogido107Actualizado" = DJ."DifPorRescateOEnajenacionCFIAcogido107Actualizado",
				"DiferenciaValorCuotasFechaLiquidacionActualizado" = DJ."DiferenciaValorCuotasFechaLiquidacionActualizado",
				"DividendosAfectosIGCoAdicionalActualizado" = DJ."DividendosAfectosIGCoAdicionalActualizado",
				"DividendosExentosIGCoAdicionalActualizado" = DJ."DividendosExentosIGCoAdicionalActualizado",
				"DividendosNoConstitutivosDeRentaActualizado" = DJ."DividendosNoConstitutivosDeRentaActualizado",
				"DividendosAfectosImptoUnicoTasa10Actualizado" = DJ."DividendosAfectosImptoUnicoTasa10Actualizado",
				"DividendosNoAfectosImptoUnicoTasa10Actualizado" = DJ."DividendosNoAfectosImptoUnicoTasa10Actualizado",
				"DividendosAfectosImptoUnicoTasa4Actualizado" = DJ."DividendosAfectosImptoUnicoTasa4Actualizado",
				"DevolucionesDeCapitalActualizado" = DJ."DevolucionesDeCapitalActualizado",
				"FactorActualizacion" = DJ."FactorActualizacion"
			FROM "BSCL.Normativa.BD::TRI.DatosDJ1922" AS DJ1922
			INNER JOIN :v_montos_actualizados AS DJ ON (DJ."IdOperacion" = DJ1922."IdOperacion");
			
			v_log = SELECT	0 AS "IdCabeceraLog"
					,'MODIFICACION' AS "IdTipoLog"
					,:p_IdModulo  AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0 AS "IdPersona"
					,0 AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'ACTUALIZAR_VALOR_IPC_DJ1922' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,'' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,'' AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM DUMMY;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'DJ1922.ActualizarValorFactorIpc','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
