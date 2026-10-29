SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.CrearPosicionInicial";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.CrearPosicionInicial"(
    IN p_IdEmpresa  BIGINT,
	IN p_IdUsuario	NVARCHAR(50),
	IN p_IdMoneda	NVARCHAR(3),
	IN p_Fecha		NVARCHAR(10),
    IN p_DatosPosicion	TABLE (	 "IdPosicionInicial"			BIGINT
								,"Fecha"						NVARCHAR(10)
								,"IdMoneda"						NVARCHAR(3)
								,"TipoCambioInicial"			DECIMAL(26,8)
								,"CierreIngresado"				DECIMAL(26,8)
								,"PosicionInicial"				DECIMAL(26,8)
								,"TipoInstrumento"				NVARCHAR(50)
								,"Estado"						NVARCHAR(3) 
								,"ResultadoPosicionInicial"  	DECIMAL(26,8)),

     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE valida_existencia INT;
	DECLARE v_FechaHoraSistema SECONDDATE;
	DECLARE varMonedaEquivalente NVARCHAR(3);
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" BIGINT, 
											"Codigo" nvarchar(100),
											"RequiereAutorizacion" NVARCHAR(1),
											"ValorParametro" nvarchar(100),
											"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT 0  AS "IdInterno" 
			                                    , ''  AS "Codigo"
			                                    , ''  AS "RequiereAutorizacion"
			                                    , ''  AS "ValorParametro"
			                                    , ''  AS "NombreParametro" 
			                               FROM dummy 
                           					WHERE 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, 
												"Codigo" nvarchar(100),
												"ValorParametro" nvarchar(100),
												"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno" 
				                                    , ''  AS "Codigo"
				                                    , ''  AS "ValorParametro"
				                                    , ''  AS "NombreParametro" 
				                               FROM dummy 
                               						WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle; 	
		
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
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									errores."Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT 	adv."IdInterno",
											adv."Codigo",
											adv."RequiereAutorizacion",
										msj."Descripcion"
									FROM :v_out_AdvertenciasDetalle adv
									INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle 	= SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle 		= SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"( 
    	:p_IdEmpresa
        ,v_FechaHoraSistema
    );
    
    SELECT "Valor" into varMonedaEquivalente  
	FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	WHERE "IdTipoProducto" = 'MEME'
	  AND "CodigoParametro" = 'IDMONEDAEQUIVALENTE'
	  AND "IdEmpresa" = :p_IdEmpresa;
	
	--------------------------------------------------------
	-- buscar si existe 
	--valida_existencia = 
	SELECT COUNT (*) INTO valida_existencia
	FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa" AS PI
	LEFT JOIN :p_DatosPosicion AS DP
		ON PI."IdPosicionInicial" = DP."IdPosicionInicial"
	WHERE PI."IdMoneda" = DP."IdMoneda"
		--AND PI."Estado" = 'ACT'
		AND PI."IdEmpresa" = :p_IdEmpresa;
   
   -----------------------------------------------------------
    BEGIN       
		--*******************************
		--*INSERT TABLAS DE TEMPORALES  *
		--*******************************
		v_tblPosicion =
			 SELECT 
				 "BSCL.Mesa.BD.Secuencias::SPT.IdPosicionInicial".NEXTVAL AS "IdPosicionInicial" 
				,:p_IdEmpresa as "IdEmpresa"
				,TO_SECONDDATE("Fecha"||' '||TO_VARCHAR(:v_FechaHoraSistema,'HH24:MI:SS'),'YYYY-MM-DD HH24:MI:SS') AS "Fecha"
				,"IdMoneda"
				,"TipoCambioInicial"
				,"CierreIngresado"
				,"PosicionInicial"
				,"TipoInstrumento"
				,:p_IdUsuario as "IdUsuario"
				,'ACT' as "Estado"
				,"ResultadoPosicionInicial"
			FROM :p_DatosPosicion;
		
		select 'v_tblPosicion', * from :v_tblPosicion;
		
		--**************************************
		--*Validar si Existe Posicion Inicial  *
		--**************************************	
		--IF (valida_existencia = 0) THEN
		--**************************************
		--*INSERT PUNTAS SI NO EXISTE MONEDA   *
		--**************************************
		
			/*INSERT INTO "BSCL.Mesa.BD::SPT.PosicionInicialMesa"
			("IdPosicionInicial", "IdEmpresa", "Fecha" , "IdMoneda", "TipoCambioInicial",
			 "CierreIngresado", "PosicionInicial", "TipoInstrumento", "IdUsuario", "Estado","ResultadoPosicionInicial" )
			SELECT
				  "Posicion"."IdPosicionInicial"
				, "Posicion"."IdEmpresa"
				, "Posicion"."Fecha"
				, "Posicion"."IdMoneda"
				, "Posicion"."TipoCambioInicial"
				, "Posicion"."CierreIngresado"	
				, "Posicion"."PosicionInicial"
				, "Posicion"."TipoInstrumento"
				, "Posicion"."IdUsuario"
				, "Posicion"."Estado"
				, "Posicion"."ResultadoPosicionInicial" 
			FROM :v_tblPosicion AS "Posicion";*/
				
		--ELSE IF (valida_existencia > 0) THEN
		--******************************************************************
		--*UPDATE CAMBIO ESTADO 'INACTIVO' A POSICION INICAL ANTERIOR      *
		--******************************************************************
			/*select "IdPosicionInicial", "Estado"
			from "BSCL.Mesa.BD::SPT.PosicionInicialMesa" AS PI
		    WHERE PI."Estado"		= 'ACT'
			  AND PI."IdMoneda"		= :p_IdMoneda
			  AND PI."IdEmpresa"	= :p_IdEmpresa
			  AND TO_VARCHAR(PI."Fecha",'YYYY-MM-DD') = :p_Fecha;*/
			
			UPDATE PI
			SET PI."Estado" = 'INA'
			FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa" AS PI
			--LEFT JOIN :p_DatosPosicion AS DP
			--	ON PI."IdPosicionInicial" = DP."IdPosicionInicial"
		    WHERE PI."Estado"		= 'ACT'
			  AND PI."IdMoneda"		= :p_IdMoneda
			  AND PI."IdEmpresa"	= :p_IdEmpresa
			  AND TO_VARCHAR(PI."Fecha",'YYYY-MM-DD') = :p_Fecha;
			
		
		--*******************************
		--*CREAR NUEVA POSICION INICIAL *
		--*******************************
		
			INSERT INTO "BSCL.Mesa.BD::SPT.PosicionInicialMesa"
			("IdPosicionInicial", "IdEmpresa", "Fecha" , "IdMoneda", "TipoCambioInicial",
			 "CierreIngresado", "PosicionInicial", "TipoInstrumento", "IdUsuario", "Estado","ResultadoPosicionInicial" )
			SELECT
				  "Posicion"."IdPosicionInicial"
				, "Posicion"."IdEmpresa"
				, "Posicion"."Fecha"
				, "Posicion"."IdMoneda"
				, "Posicion"."TipoCambioInicial"
				, "Posicion"."CierreIngresado"	
				, "Posicion"."PosicionInicial"
				, "Posicion"."TipoInstrumento"
				, "Posicion"."IdUsuario"
				, "Posicion"."Estado"
				, "Posicion"."ResultadoPosicionInicial" 
			FROM :v_tblPosicion AS "Posicion";
			
			--	v_out_ErroresDetalle = SELECT "NumeroOperacion" as "IdInterno"
			--								 ,"Codigo"
			--								 ,"ValorParametro"
			--								 ,"NombreParametro"
			--							FROM dummy;
		--END IF;
		--END IF;
		
		--IMPCREDOPT-2549 para las operacioes Spot ingresadas con fecha p_Fecha, el campo ResultadoMonedaLocalTCCierreMesa se recalcula al modificar el TipoCambioCierre
		v_RecalcularResultadoMonedaLocalTCCierreMesa =
			SELECT
				 SPT."IdOperacionSpot"
				,CASE
					-- Compra && Existe PosicionCierre
					WHEN SPT."IdTipoOperacion" = 'COMSPT' AND COALESCE(PosMonPri."CierreIngresado",0) != 0 THEN
						(COALESCE(PosMonPri."CierreIngresado",0) - SPT."TipoCambioCierre") * SPT."MontoMonedaPrincipal"
					
					-- Venta && Existe PosicionCierre
					WHEN SPT."IdTipoOperacion" = 'VENSPT' AND COALESCE(PosMonPri."CierreIngresado",0) != 0 THEN
						(SPT."TipoCambioCierre" - COALESCE(PosMonPri."CierreIngresado",0)) * SPT."MontoMonedaPrincipal"
					
					-- Compra && NO existe PosicionCierre para la monedaExtranjera
					WHEN SPT."IdTipoOperacion" = 'COMSPT' AND COALESCE(PosMonPri."CierreIngresado",0) = 0 AND COALESCE(PosMonEqui."CierreIngresado",0) != 0 THEN
						CASE WHEN COALESCE(PAR."ParidadBID",0) = 0 THEN 0
							 WHEN SPT."IdMonedaPrincipal" = varMonedaEquivalente THEN
								(COALESCE(PosMonEqui."CierreIngresado",0) - (SPT."TipoCambioCierre" / PAR."ParidadBID")) * SPT."MontoMonedaPrincipal"
						  	 ELSE (COALESCE(PosMonEqui."CierreIngresado",0) - (SPT."TipoCambioCierre" / PAR."ParidadBID")) * SPT."MontoMonedaPrincipal" * COALESCE(PAR."ParidadMercado",0) END
					
					-- Venta && NO existe PosicionCierre para la monedaExtranjera
					WHEN SPT."IdTipoOperacion" = 'VENSPT' AND COALESCE(PosMonPri."CierreIngresado",0) = 0 AND COALESCE(PosMonEqui."CierreIngresado",0) != 0 THEN
						CASE WHEN COALESCE(PAR."ParidadASK",0) = 0 THEN 0
							 WHEN SPT."IdMonedaPrincipal" = varMonedaEquivalente THEN
								((SPT."TipoCambioCierre" / PAR."ParidadASK") - COALESCE(PosMonEqui."CierreIngresado",0)) * SPT."MontoMonedaPrincipal"
							 ELSE ((SPT."TipoCambioCierre" / PAR."ParidadASK") - COALESCE(PosMonEqui."CierreIngresado",0)) * SPT."MontoMonedaPrincipal" * COALESCE(PAR."ParidadMercado",0) END
					ELSE 0 END as "ResultadoMonedaLocalTCCierreMesa"
			FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
		 		ON (SPT."IdInstrumento" = INS."IdInstrumento")
		 	--LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonPri
		 	LEFT JOIN :v_tblPosicion as PosMonPri
				ON (SPT."IdMonedaPrincipal" = PosMonPri."IdMoneda")
			--LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonEqui
			LEFT JOIN :v_tblPosicion as PosMonEqui
				ON (PosMonEqui."IdMoneda" = varMonedaEquivalente)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
				ON (PAR."IdMoneda" = SPT."IdMonedaPrincipal" AND
					PAR."Fecha" = SPT."FechaOperacion")
		 	WHERE INS."Nemotecnico" = 'INTERB'
		 	  AND TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = :p_Fecha
		 	  AND SPT."IdMonedaPrincipal" = :p_IdMoneda
		 	  AND SPT."IdEmpresa" = :p_IdEmpresa;
		
		SELECT COUNT(1) INTO valida_existencia FROM :v_RecalcularResultadoMonedaLocalTCCierreMesa;
		
		IF :valida_existencia > 0 THEN
			UPDATE SPT
			SET SPT."ResultadoMonedaLocalTCCierreMesa" = REC."ResultadoMonedaLocalTCCierreMesa"
			FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
			INNER JOIN :v_RecalcularResultadoMonedaLocalTCCierreMesa REC
				ON (SPT."IdOperacionSpot" = REC."IdOperacionSpot");	
		END IF;
		
		--INSERT WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS
			INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket" ("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
			VALUES(1,CURRENT_UTCTIMESTAMP,'ACTSPOT','Refresco Movimientos Spot','N');
		
    END;
 END;