SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.CrearPuntasSpot";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.CrearPuntasSpot"
  ( 
    IN p_IdEmpresa  BIGINT,
	IN p_Fecha		DATE,
	IN p_IdMoneda	NVARCHAR(3),
	 
    IN p_DatosPuntas     	TABLE 	("IdPuntas"				BIGINT
									, "AperturaPrecio"		DECIMAL(26,8)
									, "IdUsuario"			BIGINT
									, "PuntaCompra"			DECIMAL(26,8)
									, "PuntaVenta"			DECIMAL(26,8)
									, "IdMonedaPunta"		NVARCHAR(3)
									, "CheckPrecioPorTramo" NVARCHAR(2)
									, "Estado"				NVARCHAR(3) 
									),
	IN p_DatosPuntasTramo 	TABLE 	(
										"IdPuntasPorTramo"		BIGINT
										, "IdPuntas"			BIGINT
										, "MontoPorTramo" 		DECIMAL(26,8)
										, "PorcentajeTramo"		DECIMAL(26,8) 	
										, "MontoCentavos"  		DECIMAL(26,8) 	
										, "ValorCompra"  		DECIMAL(26,8) 	
										, "ValorVenta"  		DECIMAL(26,8) 	
										, "Estado"				NVARCHAR(3)	
										, "IdUsuario"			NVARCHAR(50)
										, "FechaModificacion" 	SECONDDATE	
										, "Canal"				NVARCHAR(100)
									),
     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
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
	
   --------------------------------------------------------
   -- buscar si existe la moneda
	--valida_existencia = 
	SELECT COUNT (*) INTO valida_existencia
		FROM "BSCL.Mesa.BD::SPT.Puntas"
		WHERE "IdMonedaPunta" = :p_IdMoneda 
			AND "Estado" = 'VIG';
   
   -----------------------------------------------------------
    BEGIN       
		--*******************************
		--*INSERT TABLAS DE TEMPORALES  *
		--*******************************
		v_tblPuntas =
			 SELECT 
				 "BSCL.Mesa.BD.Secuencias::SPT.IdPuntas".NEXTVAL as "IdPuntas" 
				, :p_IdEmpresa as "IdEmpresa"			
				, :p_Fecha as "Fecha" 				
				, "IdUsuario"
				, "AperturaPrecio"
				, "PuntaCompra"		
				, "PuntaVenta"
				, "IdMonedaPunta"
				, "CheckPrecioPorTramo"
				, 'VIG' as "Estado"
			FROM :p_DatosPuntas;
		--**************************************
		--*Validar si Existe la moneda de pago *
		--**************************************	
		IF (valida_existencia = 0) THEN
		--**************************************
		--*INSERT PUNTAS SI NO EXISTE MONEDA   *
		--**************************************
		
			INSERT INTO "BSCL.Mesa.BD::SPT.Puntas"
			("IdPuntas","IdEmpresa","Fecha","IdUsuario","AperturaPrecio","PuntaCompra","PuntaVenta",
			"IdMonedaPunta","CheckPrecioPorTramo","Estado")
			SELECT
				  "Puntas"."IdPuntas"
				, "Puntas"."IdEmpresa"
				, "Puntas"."Fecha"
				, "Puntas"."IdUsuario"
				, "Puntas"."AperturaPrecio"
				, "Puntas"."PuntaCompra"	
				, "Puntas"."PuntaVenta"
				, "Puntas"."IdMonedaPunta"
				, "Puntas"."CheckPrecioPorTramo"
				, "Puntas"."Estado"
			FROM :v_tblPuntas AS "Puntas";
				
		ELSE IF (valida_existencia = 1) THEN
		--*****************************
		--*UPDATE TABLAS PUNTAS       *
		--*****************************
			UPDATE "PTA" --"BSCL.Mesa.BD::SPT.Puntas"
				SET	 
					  "Fecha"			= :p_Fecha
					, "IdUsuario"		= "DP"."IdUsuario"
					, "AperturaPrecio"	= "DP"."AperturaPrecio"
					, "PuntaCompra"		= "DP"."PuntaCompra"
					, "PuntaVenta"		= "DP"."PuntaVenta"
					, "CheckPrecioPorTramo" = "DP"."CheckPrecioPorTramo"
			FROM "BSCL.Mesa.BD::SPT.Puntas" AS "PTA"
				INNER JOIN :p_DatosPuntas AS "DP" ON "PTA"."IdPuntas" = "DP"."IdPuntas"
			WHERE "PTA"."IdPuntas" = "DP"."IdPuntas"
				AND "PTA"."IdEmpresa" = :p_IdEmpresa;
		--*****************************
		--*UPDATE TABLAS PUNTAS X TRAMO *
		--*****************************		
			UPDATE "PTT"
				SET   "ValorCompra"			= "DP"."PuntaCompra" - "PTT"."MontoCentavos"
					, "ValorVenta" 			= "DP"."PuntaVenta" + "PTT"."MontoCentavos"
					, "IdUsuario"			= "DP"."IdUsuario"
					, "FechaModificacion" 	= :p_Fecha
			FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS "PTT"
				INNER JOIN :p_DatosPuntas AS "DP" ON "PTT"."IdPuntas" = "DP"."IdPuntas"
				INNER JOIN :p_DatosPuntasTramo AS "DPT" ON "PTT"."IdPuntas" = "DPT"."IdPuntas"
			WHERE  "PTT"."IdPuntas" = "DP"."IdPuntas"
				AND "PTT"."IdEmpresa" = :p_IdEmpresa
				AND "PTT"."Canal" = "DPT"."Canal";
		END IF;
		END IF;	 
		
		-- Inserto mensaje de aviso de actualización
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		VALUES(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,'ACTPTA',	'',	'N'	);    
		
		-- Inserto mensaje de aviso de actualización
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		VALUES(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,'AVISO','Puntas Actualizadas', 'N');    
			
    END;
 END;
 