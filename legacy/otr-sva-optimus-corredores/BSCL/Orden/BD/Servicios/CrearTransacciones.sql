SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearTransacciones";
GO
CREATE PROCEDURE
--  Proposito: crear transacciones masiva
--  Autor: - TEAM ORDENES F**K YEAH
--  Fecha Creacion: 20/07/2015  
"BSCL.Orden.BD.Servicios::CrearTransacciones" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.

	IN p_Transacciones				TABLE(
										"IdInterno"	 						BIGINT,
										"FolioTransaccion"					NVARCHAR(40),
										"FechaCreacion"						NVARCHAR(10),
										"HoraCreacion"						NVARCHAR(10),
										"FechaEmision"						NVARCHAR(10),
										"Plazo"								BIGINT,
										"EsCuentaPropia"					NVARCHAR(1),
										"EsFungible"						NVARCHAR(1),
										"EsVentaCorta"						NVARCHAR(1),
										"CodigoOperadorContraparte"			NVARCHAR(40),
										"CodigoOperadorCorredor"			NVARCHAR(40),
										"IdSistemaBursatil"					NVARCHAR(40),
										"IdEmisor"							NVARCHAR(40),
										"Cantidad"							DECIMAL(26,8),	--NOMINALES EN RF, CORTES EN IF, 
										
										"Monto"								DECIMAL(26,8),
										
										"Precio"							DECIMAL(26,8),
										"Tasa"								DECIMAL(26,8), --SIMULTANEA, RENTA FIJA, IF, FUTURO BCU
										"MontoPlazo"						DECIMAL(26,8),
										"PrecioPlazo"						DECIMAL(26,8),
										"ValorRescate"						DECIMAL(26,8),
										"IdTipoReajuste"					NVARCHAR(3),
										"PagaderoEn"						NVARCHAR(3),
										"IdEmpleado"						BIGINT,
										"IdTipoOperacion"					NVARCHAR(6),
										"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
										"FechaLiquidacionContado"			NVARCHAR(10),
										"FechaVencimiento"					NVARCHAR(10),
										"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
										"FechaLiquidacionPlazo"				NVARCHAR(10),
										"IdInstrumento"						BIGINT,
										"IdCorredorContraparte"				BIGINT,
										"IdBolsa"							NVARCHAR(3),
										"IdLibroOrdenes"					BIGINT,
										"IdInstrumentoSubyacente"			BIGINT,
										"EsFueraRueda"						NVARCHAR(1),
										"IndicadorDCV"						NVARCHAR(1),										
										"EsBilateral"						NVARCHAR(1),  -- indicador bilateral
										"MarcaNegociacion"					NVARCHAR(40),
										"TipoCambioCorresponsal"			DECIMAL(26,8),
										"IdCorredorCorresponsal"			BIGINT,
										"MontoLiquidacionCorresponsal"		DECIMAL(26,8),
										"IdMonedaLiquidacionCorresponsal"	NVARCHAR(3),
										"FolioTransaccionPlazo"				NVARCHAR(40),
										"MejorPrecioCompra"					DECIMAL(26,8),
										"MejorPrecioVenta"					DECIMAL(26,8),
										"NemotecnicoEspecificoBolson" 		NVARCHAR(100),										
										"Intereses" 						DECIMAL(26,8),  -- Para RF PERU
										"Reajustes" 						DECIMAL(26,8)   -- Para RF PERU
										),

    OUT p_out_Resultado            	TABLE(
    									"IdInterno"				BIGINT,
    									"IdTransaccion"			BIGINT,
    									"EsAPlazo"				NVARCHAR(1)
    								),
    
    OUT p_out_Errores 						"BSCL.Orden.BD.Tipos::SalidaDetalle" 	DEFAULT EMPTY,
    OUT p_out_InformacionContador			INT,
    OUT p_out_InformacionDetalle 			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)),
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--MANEJO DE ERRORES
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :p_out_Errores,
			
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		
		DROP TABLE #TRANSACIONESSSSSS;
				 					 	
	END;
	
	BEGIN
		
		DECLARE v_out_AdvertenciasContadorAgregarPrograma BIGINT;
		DECLARE v_out_ErroresContadorAgregarPrograma BIGINT;
		DECLARE v_Transacciones				NVARCHAR(5000) := '';
		DECLARE v_TranArray					BIGINT ARRAY;
		DECLARE v_TranIndex					BIGINT;
		DECLARE v_IdPaisSistema				BIGINT;
		DECLARE v_IdMonedaSistema			NVARCHAR(3);
		DECLARE v_IdMonedaSimultanea		NVARCHAR(3);
		
		DECLARE v_IdCorredorActual			BIGINT;
		
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count_trans 				BIGINT;
				
		CREATE LOCAL TEMPORARY TABLE #TRANSACIONESSSSSS (
			"IdTransaccion"				BIGINT,
			"IdInterno"					BIGINT,
			"FolioTransaccion"			NVARCHAR(40),
			"FechaSistemaCreacion"		DATE,
			"HoraSistemaCreacion"		TIME,
			"FechaOperacion"			SECONDDATE,
			"FechaLiquidacion"			DATE,
			"FechaLiquidacionPlazo"		DATE,
			"FechaEmision"				DATE,
			"FechaVencimiento"			DATE,
			"FechaIngresoOferta"		TIMESTAMP,
			"FechaIngresoRegistro"		TIMESTAMP,
			"IdSistemaBursatil"			NVARCHAR(40),
			"EsOD"						NVARCHAR(1),
			"EsCuentaPropia"			NVARCHAR(1),
			"EsFungible"				NVARCHAR(1),
			"EsAPlazo"					NVARCHAR(1),
			"EsVentaCorta"				NVARCHAR(1),
		    "CodigoOperadorContraparte" NVARCHAR(40),
		    "CodigoOperadorCorredor"	NVARCHAR(40),
		    "IdEmisor"					NVARCHAR(40),
		    "Cantidad"					DECIMAL(26,8),
		    "Monto"						DECIMAL(26,8),
		    "MontoPlazo"				DECIMAL(26,8),
		    
		    "Precio"					DECIMAL(26,8),
		    "PrecioPlazo"				DECIMAL(26,8),
		    "Tasa"						DECIMAL(26,8),
		    
		    "Nemotecnico"				NVARCHAR(40),
		    "NemotecnicoSVS"			NVARCHAR(40),
		    "ValorRescate"				DECIMAL(26,8),
		    "IdEstado"					NVARCHAR(3),
		    "IdTransaccionPlazo"		BIGINT,
		    "IdTransaccionOd"			BIGINT,
		    "IdTipoInstrumento"			NVARCHAR(3),
		    "IdClasificacionTipoInstrumento"	NVARCHAR(2),
		    "IdTipoReajuste"			NVARCHAR(3),
		    "PagaderoEn"				NVARCHAR(3),
		    "IdEmpleado"				BIGINT,
		    "IdTipoOperacion"			NVARCHAR(6),
		    "IdCondicionDeLiquidacion"	NVARCHAR(2),
		    "IdCondicionDeLiquidacionContado"	NVARCHAR(2),
		    "IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
		    "IdMoneda"							NVARCHAR(3),
		    "IdCorredorContraparte"				BIGINT,
		    "IdCorredorLocal"					BIGINT,
		    "IdEmpresa"							BIGINT,
		    "IdBolsa"							NVARCHAR(3),
		    "IdPaisBolsa"						NVARCHAR(3),
		    "IdLibroOrdenes"					BIGINT,
		    "IdInstrumento"						BIGINT,
		    "NemotecnicoSubyacente"				NVARCHAR(40),
		    "IdInstrumentoSubyacente"			BIGINT,
		    "IdMonedaSubyacente"				NVARCHAR(3),
		    "FechaParidad"						DATE,
		    "ValorParidad"						DECIMAL(26,8),
		    "FechaTipoCambio"					DATE,
		    "ValorTipoCambio"					DECIMAL(26,8),
			"Plazo"								BIGINT,
			"FechaModificacionOperacion"		SECONDDATE,
			"MarcaNegociacion"					NVARCHAR(40),
			
			"TipoCambioCorresponsal"			DECIMAL(26,8),
			"IdCorredorCorresponsal"			BIGINT,
 			"MontoLiquidacionCorresponsal"		DECIMAL(26,8),
			"IdMonedaLiquidacionCorresponsal"	NVARCHAR(3),
			"MejorPrecioCompra"					DECIMAL(26,8),
			"MejorPrecioVenta"					DECIMAL(26,8),
			
			"EsFueraRueda"						NVARCHAR(1),
			"IndicadorDCV"						NVARCHAR(1),
			"EsBilateral"						NVARCHAR(1),
			
			"FolioTransaccionPlazo"				NVARCHAR(40),
			"CorteMinimo"						DECIMAL(26,8),
			"IdEstadoModulo"					NVARCHAR(3),
			"NombreModulo"						NVARCHAR(100),
			"Cumple"							NVARCHAR(1),
			"ProcesaAutomatico"					NVARCHAR(1),
			"ExisteFolioContado"				NVARCHAR(1),
			"ExisteFolioPlazo"					NVARCHAR(1),
			"InstrumentoPermiteVentaCorta"		NVARCHAR(1),
			"IdProgramaProcesaAutomatico"		BIGINT,
			"IdEstadoLibroOrdenes"				NVARCHAR(3),
			"BaseTasa"							BIGINT,
			"TipoInteres"						NVARCHAR(1),
			"IndicadorOrdenDirecta"				NVARCHAR(2),
			"Intereses" 						DECIMAL(26,8),  -- Para RF PERU
			"Reajustes" 						DECIMAL(26,8)   -- Para RF PERU
			);
		
		----VALIDACION REQUISITOS BASICOS
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			p_out_Errores = SELECT "IdInterno",0 as "IdOperacion", 'RECHAZO' AS "Resultado","Codigo", '' as "CodigoDato1",'' as "ValorDato1", '' as "CodigoDato2",'' as "ValorDato2" 
							FROM :v_out_ErroresDetalle_Basicos,:p_Transacciones;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		----FIN VALIDACION REQUISITOS BASICOS
			
		if (IS_EMPTY(:p_Transacciones)) THEN
			p_out_Errores = SELECT 0 as "IdInterno",0 AS "IdOperacion",'RECHAZO' as "Resultado", 'NO_SE_INGRESARON_TRANSACCIONES' AS "Codigo", '' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT(1) as "count" into v_count_trans
		FROM :p_Transacciones 
		GROUP BY "IdInterno"
		ORDER BY "count" DESC;
		
		if (v_count_trans > 1) THEN
			p_out_Errores = SELECT "IdInterno", 0 as "IdOperacion",'RECHAZO' AS "Resultado",'CORRELATIVO_INCORRECTO' AS "Codigo",  '' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM :p_Transacciones;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--TODO AGREGAR VALIDACION CORREDORA PROPIA NO CONFIGURADA
		SELECT emp."IdPais",corr."IdCorredora" into v_IdPaisSistema,v_IdCorredorActual
		FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc on emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio" and soc."IdEmpresa" = :p_IdEmpresa
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."IdPersona" = soc."IdPersona"
		INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" corr on corr."IdPersona" = soc."IdPersona";
		

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'PLA.MonedaSistema',v_IdMonedaSistema);
		
		SELECT "MonedaSimultanea" INTO v_IdMonedaSimultanea
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"
		WHERE "IdEmpresa" = :p_IdEmpresa;
		
		--TRANSACCIONES INICIALES
		INSERT INTO #TRANSACIONESSSSSS("IdTransaccion","IdInterno","FolioTransaccion","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaLiquidacionPlazo","FechaEmision",
				"FechaVencimiento","FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","IndicadorOrdenDirecta","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
				"CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","MontoPlazo","Precio","PrecioPlazo","Tasa",
				"Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
			    "IdTipoReajuste","PagaderoEn","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdCondicionDeLiquidacionContado","IdCondicionDeLiquidacionPlazo","IdMoneda","IdCorredorContraparte","IdCorredorLocal",
			    "IdEmpresa","IdBolsa","IdPaisBolsa","IdLibroOrdenes","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad",
			    "FechaTipoCambio","ValorTipoCambio","Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","FolioTransaccionPlazo","CorteMinimo","IdEstadoModulo","NombreModulo","Cumple","ProcesaAutomatico","IdProgramaProcesaAutomatico",
			    "TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","MejorPrecioCompra","MejorPrecioVenta","InstrumentoPermiteVentaCorta","IdEstadoLibroOrdenes"
			    ,"BaseTasa","TipoInteres","Intereses","Reajustes")
										
		SELECT "BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL as "IdTransaccion",tran."IdInterno",tran."FolioTransaccion",tran."FechaCreacion" as "FechaSistemaCreacion",tran."HoraCreacion" as "HoraSistemaCreacion",
				CONCAT (tran."FechaCreacion", CONCAT(' ',"HoraCreacion")) as "FechaOperacion",tran."FechaLiquidacionContado" as "FechaLiquidacion",tran."FechaLiquidacionPlazo",
				CASE tipoins."IdClasificacionTipoInstrumento" WHEN 'IF' THEN tran."FechaEmision" ELSE inst."FechaEmision" END AS "FechaEmision",
				CASE  
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' AND inst."Nemotecnico" IN ('CERO','ZERO','XERO') THEN tran."FechaVencimiento"
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN inst."FechaVencimiento" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN tran."FechaVencimiento" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RV' AND inst."IdTipoInstrumento" = 'SIM' THEN tran."FechaVencimiento" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN tran."FechaVencimiento" 
					ELSE '0001-01-01'
				END AS "FechaVencimiento",TO_TIMESTAMP(CONCAT (tran."FechaCreacion", tran."HoraCreacion"),'YYYY-MM-DDHH24:MI:SS') as "FechaIngresoOferta",
				CURRENT_TIMESTAMP AS "FechaIngresoRegistro",
				tran."IdSistemaBursatil",
				CASE tran."IdCorredorContraparte" WHEN :v_IdCorredorActual THEN 'OD' ELSE '' END AS "IndicadorOrdenDirecta",tran."EsCuentaPropia",tran."EsFungible",'N' as "EsAPlazo",
				CASE tipoope."AccionOrden" WHEN 'COMPRA' THEN 'N' ELSE tran."EsVentaCorta" END AS "EsVentaCorta",
				tran."CodigoOperadorContraparte",tran."CodigoOperadorCorredor",
				CASE  
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN tran."IdEmisor" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RV' AND inst."IdTipoInstrumento" = 'SIM' THEN sub."IdEmisor"
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN sub."IdEmisor"
					ELSE inst."IdEmisor" 
				END AS "IdEmisor",
				tran."Cantidad",tran."Monto",tran."MontoPlazo",tran."Precio",tran."PrecioPlazo",tran."Tasa",
				CASE  
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' AND inst."Nemotecnico" IN ('CERO','ZERO','XERO','BR') THEN tran."NemotecnicoEspecificoBolson"
					ELSE inst."Nemotecnico"
				END AS "Nemotecnico",
				'' as "NemotecnicoSVS",tran."ValorRescate",'ING' AS "IdEstado",0 as "IdTransaccionPlazo",0 as "IdTransaccionOd",
				inst."IdTipoInstrumento",tipoins."IdClasificacionTipoInstrumento",
				CASE  
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN inst."IdTipoReajuste" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' AND tipoins."IdTipoInstrumento" = 'RRF' THEN inst."IdTipoReajuste" 
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN tran."IdTipoReajuste"
					ELSE ''
				END AS "IdTipoReajuste",
				COALESCE(tran."PagaderoEn",inst."IdMoneda") as "PagaderoEn",
				tran."IdEmpleado",tran."IdTipoOperacion",tran."IdCondicionDeLiquidacionContado" as "IdCondicionDeLiquidacion",tran."IdCondicionDeLiquidacionContado",tran."IdCondicionDeLiquidacionPlazo",
				
				CASE  
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN :v_IdMonedaSistema
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RV' AND inst."IdTipoInstrumento" = 'SIM' THEN :v_IdMonedaSimultanea
					WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN sub."IdMoneda"
					ELSE inst."IdMoneda"
				END AS "IdMoneda",
				
				
				tran."IdCorredorContraparte",
				:v_IdCorredorActual as "IdCorredorLocal",:p_IdEmpresa as "IdEmpresa",tran."IdBolsa",bol."IdPais" as "IdPaisBolsa",tran."IdLibroOrdenes",
				tran."IdInstrumento",COALESCE(sub."Nemotecnico",'') as "NemotecnicoSubyacente",
				COALESCE(sub."IdInstrumento",0) as "IdInstrumentoSubyacente",COALESCE(sub."IdMoneda",'') as "IdMonedaSubyacente",
				'0001-01-01' as "FechaParidad",0 as "ValorParidad",'0001-01-01' as "FechaTipoCambio",0 as "ValorTipoCambio",
				tran."Plazo",CONCAT (tran."FechaCreacion", CONCAT(' ',"HoraCreacion")) as "FechaModificacionOperacion",
				tran."MarcaNegociacion",tran."IndicadorDCV",tran."EsBilateral",tran."EsFueraRueda",tran."FolioTransaccionPlazo",
				instr."CorteMinimo",CRE."TipoEstado" AS "IdEstadoModulo",CRE."Nombre" as "NombreModulo",'S' as "Cumple",'N' as "ProcesaAutomatico",0 as "IdProgramaProcesaAutomatico",
				
				CASE WHEN :v_IdPaisSistema != bol."IdPais" THEN COALESCE(tran."TipoCambioCorresponsal",0) ELSE 0 END AS "TipoCambioCorresponsal", 
				CASE WHEN :v_IdPaisSistema != bol."IdPais" THEN COALESCE(corr."IdCorredorCorresponsal",0) ELSE 0 END AS "IdCorredorCorresponsal", 
				CASE WHEN :v_IdPaisSistema != bol."IdPais" THEN COALESCE(tran."MontoLiquidacionCorresponsal",0) ELSE 0 END AS "MontoLiquidacionCorresponsal", 
				CASE WHEN :v_IdPaisSistema != bol."IdPais" THEN COALESCE(tran."IdMonedaLiquidacionCorresponsal",'') ELSE '' END AS "IdMonedaLiquidacionCorresponsal",
				"MejorPrecioCompra","MejorPrecioVenta",
				CASE WHEN subprt."IdInstrumento" IS NULL THEN 'N' ELSE 'S' END AS "InstrumentoPermiteVentaCorta",lib."IdEstado" AS "IdEstadoLibroOrdenes",
				CASE WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN 360 WHEN tipoins."IdTipoInstrumento" = 'SIM' THEN 30 ELSE 0 END AS "BaseTasa",
				CASE WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN 'C' WHEN tipoins."IdTipoInstrumento" = 'SIM' THEN 'S' ELSE '' END AS "TipoInteres",
				CASE WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN tran."Intereses"  ELSE 0 END AS "Intereses", 
				CASE WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN tran."Reajustes" ELSE 0 END AS "Reajustes"
				
				
			FROM :p_Transacciones tran
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on tran."IdInstrumento" = inst."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" sub on tran."IdInstrumentoSubyacente" = sub."IdInstrumento"
			LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on tran."IdLibroOrdenes" = lib."IdLibroOrdenes"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" bol on bol."IdBolsa" = tran."IdBolsa"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipoope on tran."IdTipoOperacion" = tipoope."IdTipoOperacion"
			LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr on corr."IdCorredorCorresponsal" = tran."IdCorredorCorresponsal"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = tran."IdInstrumento" and instr."IdTipoReajuste" = tran."IdTipoReajuste"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE ON CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = :p_IdEmpresa
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" subprt on tran."IdInstrumento" = subprt."IdInstrumento" and subprt."IdTipoInstrumento" = 'PRT'
			
			;
		
		v_Folios = SELECT tran."IdTransaccion",COUNT(CASE WHEN p_tran."IdTransaccion" IS NULL THEN NULL ELSE 1 END) as "CantidadFolios"
					FROM #TRANSACIONESSSSSS tran
					LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" p_tran on p_tran."Folio" = tran."FolioTransaccion" and p_tran."FechaSistemaCreacion" = tran."FechaSistemaCreacion" 
						and tran."IdEmpresa" = p_tran."IdEmpresa"
					GROUP BY tran."IdTransaccion";
		
		UPDATE TRAN
		SET "ExisteFolioContado" = COALESCE(CASE WHEN fol."CantidadFolios" > 0 THEN 'S' ELSE 'N' END,'N')
		FROM #TRANSACIONESSSSSS tran
		INNER JOIN :v_Folios fol on tran."IdTransaccion" = fol."IdTransaccion";
					
		v_FoliosPlazo = SELECT tran."IdTransaccion",COUNT(CASE WHEN p_tranPlazo."IdTransaccion" IS NULL THEN NULL ELSE 1 END) as "CantidadFolios"
					FROM #TRANSACIONESSSSSS tran
					INNER JOIN  "BSCL.Plataforma.BD::INS.Instrumento" inst on tran."IdInstrumento" = inst."IdInstrumento" AND inst."IdTipoInstrumento" = 'SIM'
					LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" p_tranPlazo on p_tranPlazo."Folio" = tran."FolioTransaccionPlazo" and p_tranPlazo."FechaSistemaCreacion" = tran."FechaSistemaCreacion" 
						and tran."IdEmpresa" = p_tranPlazo."IdEmpresa"
					GROUP BY tran."IdTransaccion"
					;
		
		v_FoliosPlazoReporte = SELECT tran."IdTransaccion",COUNT(CASE WHEN p_tranPlazo."IdTransaccion" IS NULL THEN NULL ELSE 1 END) as "CantidadFolios"
					FROM #TRANSACIONESSSSSS tran
					INNER JOIN  "BSCL.Plataforma.BD::INS.Instrumento" inst on tran."IdInstrumento" = inst."IdInstrumento"
					INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento" AND tipoins."IdClasificacionTipoInstrumento" = 'RE'
					LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" p_tranPlazo on p_tranPlazo."Folio" = tran."FolioTransaccionPlazo" and p_tranPlazo."FechaSistemaCreacion" = tran."FechaSistemaCreacion" 
						and tran."IdEmpresa" = p_tranPlazo."IdEmpresa"
					GROUP BY tran."IdTransaccion"
					;
		
		UPDATE TRAN
		SET "ExisteFolioPlazo" = COALESCE(CASE WHEN fol."CantidadFolios" > 0 THEN 'S' ELSE 'N' END,'N')
		FROM #TRANSACIONESSSSSS tran
		INNER JOIN :v_FoliosPlazo fol on tran."IdTransaccion" = fol."IdTransaccion";

		v_TransaccionesValidacion = SELECT "IdInterno","IdTransaccion","FolioTransaccion","FolioTransaccionPlazo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Cantidad",
											"CorteMinimo","Plazo","IdPaisBolsa",:v_IdPaisSistema as "IdPaisSistema","IdCorredorCorresponsal","IdEstadoModulo","NombreModulo","ExisteFolioContado","ExisteFolioPlazo",
											"EsFungible","ValorRescate","EsVentaCorta","InstrumentoPermiteVentaCorta","IdEstadoLibroOrdenes"
										FROM #TRANSACIONESSSSSS;
		
		CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearTransaccionMM"(:v_TransaccionesValidacion,:v_out_SalidaDetalle,:v_out_Salida);
		
		--SELECT 'v_out_SalidaDetalle',* FROM :v_out_SalidaDetalle;
		--SELECT 'v_out_Salida',* FROM :v_out_Salida;
											
		p_out_Errores =
			SELECT "IdInterno","IdOperacion","Resultado","Codigo", "CodigoDato1", "ValorDato1" , "CodigoDato2","ValorDato2"
			FROM :v_out_SalidaDetalle where "Resultado" != 'OK'
			UNION ALL SELECT * FROM :p_out_Errores;
		
		--ACTUALIZA REQUISITOS
		UPDATE TRAN 
		SET "Cumple" = 'N' 
		FROM #TRANSACIONESSSSSS TRAN
		INNER JOIN :v_out_Salida SD ON TRAN."IdInterno" = SD."IdInterno"
		WHERE "Resultado" = 'RECHAZO';
		
		v_in_ObtenerNemoSVSMM = SELECT "IdInterno","IdEmisor","FechaLiquidacion" as "Fecha","Plazo","Nemotecnico","IdTipoReajuste","PagaderoEn"
								FROM #TRANSACIONESSSSSS tran 
								WHERE tran."Cumple" = 'S' and tran."IdClasificacionTipoInstrumento" = 'IF';
		
		CALL "BSCL.Plataforma.BD.Servicios::ObtenerNemoSvsMM"(:v_in_ObtenerNemoSVSMM,:v_out_nemosSVS);
		
		--ACTUALIZA NEMO SVS
		UPDATE TRAN
		SET "NemotecnicoSVS" = COALESCE(nemos."NemoSVS",'')
		FROM #TRANSACIONESSSSSS TRAN
		LEFT JOIN :v_out_nemosSVS nemos on tran."IdInterno" = nemos."IdInterno";
	
		--TRANSACCIONES OD
		INSERT INTO #TRANSACIONESSSSSS("IdTransaccion","IdInterno","FolioTransaccion","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaLiquidacionPlazo","FechaEmision",
				"FechaVencimiento","FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","IndicadorOrdenDirecta","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
				"CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","MontoPlazo","Precio","PrecioPlazo","Tasa",
				"Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
			    "IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdCondicionDeLiquidacionContado","IdCondicionDeLiquidacionPlazo","IdMoneda","IdCorredorContraparte","IdCorredorLocal",
			    "IdEmpresa","IdBolsa","IdPaisBolsa","IdLibroOrdenes","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad",
			    "FechaTipoCambio","ValorTipoCambio","Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","FolioTransaccionPlazo","CorteMinimo","Cumple","ProcesaAutomatico","IdProgramaProcesaAutomatico",
			    "TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","IdEstadoModulo","NombreModulo","MejorPrecioCompra","MejorPrecioVenta","IdEstadoLibroOrdenes",
			    "BaseTasa","TipoInteres","Intereses","Reajustes")
		
		SELECT "BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL as "IdTransaccion",tran."IdInterno",tran."FolioTransaccion",tran."FechaSistemaCreacion",tran."HoraSistemaCreacion",tran."FechaOperacion",tran."FechaLiquidacion",tran."FechaLiquidacionPlazo",tran."FechaEmision",
				tran."FechaVencimiento",tran."FechaIngresoOferta",tran."FechaIngresoRegistro",tran."IdSistemaBursatil",tran."IndicadorOrdenDirecta",tran."EsCuentaPropia",tran."EsFungible",tran."EsAPlazo",'N' as "EsVentaCorta",
				tran."CodigoOperadorCorredor" AS "CodigoOperadorContraparte",tran."CodigoOperadorContraparte" AS "CodigoOperadorCorredor",tran."IdEmisor",tran."Cantidad",tran."Monto",tran."MontoPlazo",tran."Precio",tran."PrecioPlazo",tran."Tasa",
				CASE  
					WHEN tran."IdTipoOperacion" IN ('COMSIM','VENSIM','COMRRV','COMRRF','VENRRV','VENRRF') THEN tipoopeins."Nemotecnico" 
					WHEN tran."IdTipoInstrumento" = 'SIM' THEN tipoopeins."Nemotecnico" --BORRAR EVENTUALMENTE, SE CUBRE CON EL CASO DE ARRIBA
					ELSE tran."Nemotecnico" END AS "Nemotecnico",
					tran."NemotecnicoSVS",tran."ValorRescate",tran."IdEstado",tran."IdTransaccionPlazo",
				tran."IdTransaccion" as "IdTransaccionOd",tran."IdTipoInstrumento",tran."IdClasificacionTipoInstrumento",
			    tran."IdTipoReajuste",tran."IdEmpleado",tipoope."IdTipoOperacionRelacionada" as "IdTipoOperacion",tran."IdCondicionDeLiquidacionContado" AS "IdCondicionDeLiquidacion",tran."IdCondicionDeLiquidacionContado",tran."IdCondicionDeLiquidacionPlazo",tran."IdMoneda",tran."IdCorredorContraparte",tran."IdCorredorLocal",
			    tran."IdEmpresa",tran."IdBolsa",tran."IdPaisBolsa",tran."IdLibroOrdenes",
			    CASE
				    WHEN tran."IdTipoOperacion" IN ('COMSIM','VENSIM','COMRRV','COMRRF','VENRRV','VENRRF') THEN tipoopeins."IdInstrumento" 
			    	WHEN tran."IdTipoInstrumento" = 'SIM' THEN tipoopeins."IdInstrumento" 
			    	ELSE tran."IdInstrumento" 
			    END AS "IdInstrumento",
			    tran."NemotecnicoSubyacente",tran."IdInstrumentoSubyacente",tran."IdMonedaSubyacente",tran."FechaParidad",tran."ValorParidad",
			    tran."FechaTipoCambio",tran."ValorTipoCambio",tran."Plazo",tran."FechaModificacionOperacion",tran."MarcaNegociacion",tran."IndicadorDCV",tran."EsBilateral",tran."EsFueraRueda",tran."FolioTransaccionPlazo",tran."CorteMinimo",tran."Cumple",'N' as "ProcesaAutomatico",0 as "IdProgramaProcesaAutomatico",
			    tran."TipoCambioCorresponsal",tran."IdCorredorCorresponsal",tran."MontoLiquidacionCorresponsal",tran."IdMonedaLiquidacionCorresponsal",tran."IdEstadoModulo",tran."NombreModulo",tran."MejorPrecioCompra",tran."MejorPrecioVenta",tran."IdEstadoLibroOrdenes",
			    tran."BaseTasa",tran."TipoInteres",tran."Intereses",tran."Reajustes"
		FROM #TRANSACIONESSSSSS tran
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipoope on tran."IdTipoOperacion" = tipoope."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoopeins on tipoopeins."IdTipoOperacion" = tipoope."IdTipoOperacionRelacionada" and tipoopeins."IdTipoInstrumento" = tran."IdTipoInstrumento"						
		WHERE tran."Cumple" = 'S' AND tran."EsOD" = 'S';
		
		--ACTUALIZA TRANSACCIONES OD
	    UPDATE TRAN
	    SET "IdTransaccionOd" = TRAN_OD."IdTransaccion"
	    FROM #TRANSACIONESSSSSS TRAN
	    LEFT JOIN #TRANSACIONESSSSSS TRAN_OD ON TRAN."IdTransaccion" = TRAN_OD."IdTransaccionOd"
	    WHERE TRAN."Cumple" = 'S' AND TRAN."IdTransaccionOd" = 0 AND TRAN."EsOD" = 'S';
	    
	    --TRANSACCIONES SIMULTANEA Y REPORTES
		INSERT INTO #TRANSACIONESSSSSS("IdTransaccion","IdInterno","FolioTransaccion","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaEmision",
				"FechaVencimiento","FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","IndicadorOrdenDirecta","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
				"CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","Precio","Tasa",
				"Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
			    "IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredorContraparte","IdCorredorLocal",
			    "IdEmpresa","IdBolsa","IdPaisBolsa","IdLibroOrdenes","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad",
			    "FechaTipoCambio","ValorTipoCambio","Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","Cumple","ProcesaAutomatico","IdProgramaProcesaAutomatico",
			    "TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","IdEstadoModulo","NombreModulo","MejorPrecioCompra","MejorPrecioVenta","IdEstadoLibroOrdenes",
			    "BaseTasa","TipoInteres","Intereses","Reajustes")
		
		SELECT "BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL as "IdTransaccion",TRAN."IdInterno",TRAN."FolioTransaccionPlazo" as "FolioTransaccion",TRAN."FechaSistemaCreacion",TRAN."HoraSistemaCreacion",TRAN."FechaOperacion",TRAN."FechaLiquidacionPlazo" as "FechaLiquidacion",TRAN."FechaEmision",
				TRAN."FechaVencimiento",TRAN."FechaIngresoOferta",TRAN."FechaIngresoRegistro",TRAN."IdSistemaBursatil",TRAN."IndicadorOrdenDirecta",TRAN."EsCuentaPropia",TRAN."EsFungible",'S' as "EsAPlazo",TRAN."EsVentaCorta",
				TRAN."CodigoOperadorContraparte",TRAN."CodigoOperadorCorredor",TRAN."IdEmisor",TRAN."Cantidad",TRAN."MontoPlazo" as "Monto",TRAN."PrecioPlazo" as "Precio",TRAN."Tasa",
				tipoins."Nemotecnico",TRAN."NemotecnicoSVS",TRAN."ValorRescate",TRAN."IdEstado",TRAN."IdTransaccion" as "IdTransaccionPlazo",TRAN."IdTransaccionOd",TRAN."IdTipoInstrumento",TRAN."IdClasificacionTipoInstrumento",
				TRAN."IdTipoReajuste",TRAN."IdEmpleado",tipoope."IdTipoOperacionRelacionada" as "IdTipoOperacion",TRAN."IdCondicionDeLiquidacionPlazo" as "IdCondicionDeLiquidacion",TRAN."IdMoneda",TRAN."IdCorredorContraparte",TRAN."IdCorredorLocal",
				TRAN."IdEmpresa",TRAN."IdBolsa",TRAN."IdPaisBolsa",TRAN."IdLibroOrdenes",tipoins."IdInstrumento",TRAN."NemotecnicoSubyacente",TRAN."IdInstrumentoSubyacente",TRAN."IdMonedaSubyacente",TRAN."FechaParidad",TRAN."ValorParidad",
				TRAN."FechaTipoCambio",TRAN."ValorTipoCambio",TRAN."Plazo",TRAN."FechaModificacionOperacion",TRAN."MarcaNegociacion",TRAN."IndicadorDCV",TRAN."EsBilateral",TRAN."EsFueraRueda",TRAN."Cumple",'N' as "ProcesaAutomatico",0 as "IdProgramaProcesaAutomatico",
				TRAN."TipoCambioCorresponsal",TRAN."IdCorredorCorresponsal",TRAN."MontoLiquidacionCorresponsal",TRAN."IdMonedaLiquidacionCorresponsal",TRAN."IdEstadoModulo",TRAN."NombreModulo",TRAN."MejorPrecioCompra",TRAN."MejorPrecioVenta",TRAN."IdEstadoLibroOrdenes",
				TRAN."BaseTasa",TRAN."TipoInteres",TRAN."Intereses",TRAN."Reajustes"
				FROM #TRANSACIONESSSSSS TRAN 
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on TRAN."IdTipoOperacion" = tipoope."IdTipoOperacion"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento" tipoins on tipoope."IdTipoOperacionRelacionada" = tipoins."IdTipoOperacion" AND tipoins."IdBolsa" = TRAN."IdBolsa"
				WHERE tran."IdTipoOperacion" IN ('COMSIM','VENSIM','COMRRV','COMRRF','VENRRV','VENRRF') AND TRAN."Cumple" = 'S';
		
		--ACTUALIZA TRANSACCIONES SIMULTANEA
		UPDATE TRAN
	    SET "IdTransaccionPlazo" = COALESCE(TRAN_PLAZO."IdTransaccion",0)
	    FROM #TRANSACIONESSSSSS TRAN
	    LEFT JOIN #TRANSACIONESSSSSS TRAN_PLAZO ON TRAN."IdTransaccion" = TRAN_PLAZO."IdTransaccionPlazo"
	    WHERE TRAN."Cumple" = 'S' AND TRAN."IdTransaccionPlazo" = 0 AND TRAN_PLAZO."EsAPlazo" = 'S';
	   	
	   	UPDATE TRAN
	   	SET "ProcesaAutomatico" = CASE WHEN COP."UtilizaPortafolio" = 'S' AND COP."IdPortafolio" > 0 AND "AsignacionAuto" = 'S' THEN 'S' 
	   									WHEN COP."ReservaTransaccion" = 'S' THEN 'S' 
	   									ELSE 'N' END
	   	FROM #TRANSACIONESSSSSS TRAN
	   	INNER JOIN "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" as COP ON COP."CodigoOperador" = TRAN."CodigoOperadorCorredor"
	   	INNER JOIN "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" DERP ON COP."CodigoOperador" = DERP."CodigoOperador" AND DERP."IdEstado" = 'ACT' AND COP."IdEmpresa" = :p_IdEmpresa AND COP."IdEstado" = 'ACT';
	   	
	   	
	   	 IF (p_simularOperacion = 'N') THEN
	   	
		   	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","FechaProcesamiento","Procesado","Log")
		   	SELECT 'TRANSACCION_OPERADOR',"FechaSistemaCreacion" AS "IdMensaje1", "IdTransaccion" AS "IdMensaje2",
		   	CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",'0001-01-01 00:00:00','N' AS "Procesado",'' as "Log"
		   	FROM #TRANSACIONESSSSSS WHERE "Cumple" = 'S' AND "ProcesaAutomatico" = 'S' AND "EsAPlazo" = 'N'; 	  
	   	
		   	INSERT INTO "BSCL.Orden.BD::ORD.Transaccion"
				    ("IdTransaccion","Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaEmision","FechaVencimiento",
				    "FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","IndicadorOrdenDirecta","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
				    "CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","Precio",
				    "Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
				    "IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredorContraparte","IdEmpresa","IdBolsa","IdLibroOrdenes","IdInstrumento",
				    "NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio",
				    "Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","IdCorredorLocal","IdProgramaProcesaAutomatico",
				    "TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","MejorPrecioCompra","MejorPrecioVenta","BaseTasa","TipoInteres","Intereses","Reajustes","FechaPago")
	   
			SELECT 	"IdTransaccion","FolioTransaccion" as "Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaEmision","FechaVencimiento",
		   			"FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","IndicadorOrdenDirecta","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
		   			"CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","Precio",
		   			"Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
		   			"IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredorContraparte","IdEmpresa","IdBolsa","IdLibroOrdenes","IdInstrumento",
		   			"NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio",
		   			"Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","IdCorredorLocal","IdProgramaProcesaAutomatico",
		   			"TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","MejorPrecioCompra","MejorPrecioVenta","BaseTasa","TipoInteres","Intereses","Reajustes","FechaLiquidacion"
			FROM #TRANSACIONESSSSSS WHERE "Cumple" = 'S';
			
			INSERT INTO "BSCL.Orden.BD::ORD.LogCambioTransaccion"
			("TipoLogCambio", "FechaLogCambio", "FechaIngresoRegistroLog", "Motivo", "Observaciones", "IdTransaccion", "Folio", "FechaSistemaCreacion", "HoraSistemaCreacion", "FechaOperacion", "FechaLiquidacion", 
			"FechaEmision", "FechaVencimiento", "FechaIngresoRegistro", "EsOD", "EsCuentaPropia", "EsFungible", "EsAPlazo", "IdEmisor", "Cantidad", "Monto", "Precio", "Tasa", "Nemotecnico", "NemotecnicoSVS", 
			"ValorRescate", "ValorCaptacion", "IdEstado", "IdTransaccionPlazo", "IdTransaccionOd", "IdTipoInstrumento", "IdClasificacionTipoInstrumento", "IdTipoReajuste", "IdEmpleado", "IdTipoOperacion", 
			"IdCondicionDeLiquidacion", "IdMoneda", "IdEmpresa", "IdBolsa", "IdInstrumento", "NemotecnicoSubyacente", "IdInstrumentoSubyacente", "IdMonedaSubyacente", "EsVentaCorta", "Plazo", "FechaIngresoOferta", 
			"CodigoOperadorContraparte", "IdLibroOrdenes", "MarcaNegociacion", "IndicadorDCV","EsBilateral", "EsFueraRueda", "IdCorredorLocal", "TipoCambioCorresponsal", "IdCorredorCorresponsal", "MontoLiquidacionCorresponsal", 
			"IdMonedaLiquidacionCorresponsal", "IdCorredorContraparte", "CodigoOperadorCorredor", "IdSistemaBursatil", "MejorPrecioVenta", "MejorPrecioCompra")
			SELECT 'N' AS "TipoLogCambio", "FechaOperacion" AS "FechaLogCambio",CURRENT_TIMESTAMP AS  "FechaIngresoRegistroLog", '' as "Motivo",'' as  "Observaciones", "IdTransaccion", "FolioTransaccion" as "Folio", "FechaSistemaCreacion", "HoraSistemaCreacion", "FechaOperacion", "FechaLiquidacion", 
			"FechaEmision", "FechaVencimiento", "FechaIngresoRegistro", "EsOD", "EsCuentaPropia", "EsFungible", "EsAPlazo", "IdEmisor", "Cantidad", "Monto", "Precio", "Tasa", "Nemotecnico", "NemotecnicoSVS", 
			"ValorRescate", 0 AS "ValorCaptacion", "IdEstado", "IdTransaccionPlazo", "IdTransaccionOd", "IdTipoInstrumento", "IdClasificacionTipoInstrumento", "IdTipoReajuste", "IdEmpleado", "IdTipoOperacion", 
			"IdCondicionDeLiquidacion", "IdMoneda", "IdEmpresa", "IdBolsa", "IdInstrumento", "NemotecnicoSubyacente", "IdInstrumentoSubyacente", "IdMonedaSubyacente", "EsVentaCorta", "Plazo", "FechaIngresoOferta", 
			"CodigoOperadorContraparte", "IdLibroOrdenes", "MarcaNegociacion", "IndicadorDCV","EsBilateral","EsFueraRueda", "IdCorredorLocal", "TipoCambioCorresponsal", "IdCorredorCorresponsal", "MontoLiquidacionCorresponsal", 
			"IdMonedaLiquidacionCorresponsal", "IdCorredorContraparte", "CodigoOperadorCorredor", "IdSistemaBursatil", "MejorPrecioVenta", "MejorPrecioCompra"
			from #TRANSACIONESSSSSS WHERE "Cumple" = 'S';
			
			v_DetalleLog = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			v_log = SELECT	"IdTransaccion" as "IdCabeceraLog", 'INGRESO' AS "IdTipoLog", 'ORD' AS "IdModulo", 0 AS "IdUsuario", :p_IdEmpresa AS "IdEmpresa", TO_NVARCHAR("FechaOperacion", 'YYYY-MM-DD') as "FechaSistemaLog",
				 		0 as "IdUsuarioAtendido", 0 AS "IdPersona", 0 AS "IdCliente", '' AS "EstadoCliente", '' AS "NombreCliente", '' as "IdTipoDocumentoIdentidad", '' as "IdDocumentoIdentidad", 0 as "IdPortafolio",
				 		'' as "EstadoPortafolio", 'TRANSACCION' as "IdTipoRegistro", "IdTransaccion" as "IdRegistro", '' as "EstadoRegistro", "IdTipoOperacion", "FolioTransaccion" AS "Folio","Cantidad","Precio" as "PrecioTasa",
				 		"Monto","IdMoneda" as "IdMonedaOrigen","IdMoneda" as "IdMonedaLiquidacion", 0 AS "TipoCambio", "Nemotecnico" as "Nemo",0 as "IdProceso",'' as "EstadoProceso",'' as "NombreProceso"
						FROM #TRANSACIONESSSSSS WHERE "Cumple" = 'S';
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DetalleLog);
	   	
		END IF;
	   
	   -- SELECT 'TRANSACIONESSSSSS',* FROM #TRANSACIONESSSSSS;
	   --  SELECT 'p_out_Errores',* FROM :p_out_Errores;
		
		p_out_Resultado = SELECT "IdInterno", "IdTransaccion","EsAPlazo" FROM #TRANSACIONESSSSSS WHERE "Cumple" = 'S';
		p_out_Errores = SELECT "IdInterno","IdTransaccion" aS "IdOperacion",'INFORMACION' AS "Resultado",'TRANSACCION_INGRESADA' AS "Codigo", 'IdTransaccion' AS "CodigoDato1", CAST("IdTransaccion" AS NVARCHAR(100)) AS "ValorDato1" , '' AS "CodigoDato2",'' AS "ValorDato2" 
						FROM #TRANSACIONESSSSSS WHERE "Cumple" = 'S' 
						UNION ALL SELECT * FROM :p_out_Errores;

		SIGNAL SALIDA_CONTROLADA;
	END;
		
END;