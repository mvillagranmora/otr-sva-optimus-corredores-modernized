SET SCHEMA "BSCL";
GO

DROP PROCEDURE  "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV";
GO

CREATE PROCEDURE "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_Portafolio								BIGINT,		
	IN 	p_IdEntidad									BIGINT,
	IN 	p_TipoEntidad								NVARCHAR(50),
	IN 	p_TipoOperacion								NVARCHAR(1),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA

	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_countPortafolioAPV		INT:=0;
		DECLARE v_countGrupoIndicadores		INT:=0;
		DECLARE v_countIndicadores			INT:=0;
		DECLARE v_countPortafolios			INT:=0;
		DECLARE v_IdProductoAgrupador		INT:=0;
		DECLARE v_TablaPortafolio			TABLE ("IdPortafolio" BIGINT,"IdCliente" BIGINT,"FechaIngreso" SECONDDATE, "IdPerfilLimitesExtra" NVARCHAR(40));
		DECLARE v_TablaMatrizAgrupador   	TABLE ("IdMatrizAgrupadorAPV" BIGINT);
		DECLARE v_IdMoneda					NVARCHAR(3);
		DECLARE v_EsDefecto					NVARCHAR(1);	
		
		--------------------
		--L�gica Negocio
		--------------------
		BEGIN
			
			-- Verificar si existe el producto agrupador
			IF EXISTS(
				SELECT "IdMatrizAgrupadorAPV"
				FROM "BSCL.Portafolio.BD::POR.Portafolio"
				WHERE "IdPortafolio" = :p_Portafolio
				AND "Proposito" = 'PROPOSITOAPV'
				AND "IdEstado" <> 'INA')
			THEN
				-- Si existe, asignar el valor a la variable
				SELECT "IdProductoAgrupador" INTO v_IdProductoAgrupador
				FROM "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"
				WHERE "IdMatrizAgrupadorAPV" = (SELECT "IdMatrizAgrupadorAPV"
												FROM "BSCL.Portafolio.BD::POR.Portafolio"
												WHERE "IdPortafolio" = :p_Portafolio
												AND "Proposito" = 'PROPOSITOAPV'
												AND "IdEstado" <> 'INA');
			ELSE
				-- Si no existe, asignar el valor 0
    			v_IdProductoAgrupador := 0;
			END IF;
			
			v_TablaMatrizAgrupador = SELECT "IdMatrizAgrupadorAPV"
									 FROM "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"
									 WHERE "IdProductoAgrupador" = v_IdProductoAgrupador;  											
			
			
			v_TablaPortafolio = SELECT "IdPortafolio","IdCliente","FechaIngreso","IdPerfilLimitesExtra"
								FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
								INNER JOIN :v_TablaMatrizAgrupador AS MatrizAgrupador
										ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
								WHERE Portafolio."IdCliente" IN (SELECT "IdCliente"
																 FROM "BSCL.Portafolio.BD::POR.Portafolio"
																 WHERE "IdPortafolio" = :p_Portafolio
																 AND "Proposito" = 'PROPOSITOAPV'
																 AND "IdEstado" <> 'INA') 
								AND Portafolio."Proposito" = 'PROPOSITOAPV'
								ORDER BY "FechaIngreso" DESC;
			
			
			SELECT COUNT(*) INTO v_countPortafolios FROM :v_TablaPortafolio;

			IF(:v_countPortafolios > 1) THEN
			
				IF(:p_TipoEntidad = 'AUTORIZACION' AND :p_TipoOperacion = 'I') THEN
					
					--AUTORIZACION INSERT
            
					v_AutorizadoPrincipal = SELECT * 
											FROM "BSCL.Portafolio.BD::POR.Autorizado"
											WHERE "IdAutorizado" = :p_IdEntidad;
									
					INSERT INTO  "BSCL.Portafolio.BD::POR.Autorizado"
					(    "IdAutorizado" 
					   	,"IdPortafolio"
					   	,"IdPersona"
					   	,"IdTipoAutorizado"
					   	,"IdEstado"
					   	,"IdEstadoAutorizacion"
					   	,"FechaIngreso" 
					   	,"FechaCreacion"
						,"FechaModificacion"
						,"IdUsuarioCreador"
						,"NumeroRegistroPublico"
						,"Asiento"
						,"FichaPartida"
						,"TipoFirma"
					) SELECT
						 "BSCL.Portafolio.BD.Secuencias::Autorizado".NEXTVAL
					   	,POR."IdPortafolio"
					   	,AUT."IdPersona"
					   	,AUT."IdTipoAutorizado"
					   	,AUT."IdEstado"
					   	,AUT."IdEstadoAutorizacion"
					   	,AUT."FechaIngreso" 
					   	,AUT."FechaCreacion"
						,AUT."FechaModificacion"
						,AUT."IdUsuarioCreador"
						,AUT."NumeroRegistroPublico"
						,AUT."Asiento"
						,AUT."FichaPartida"
						,AUT."TipoFirma"
					FROM :v_TablaPortafolio AS POR
					INNER JOIN :v_AutorizadoPrincipal AS AUT
					ON POR."IdPortafolio" != AUT."IdPortafolio"; 
				
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Ingreso Autorizacion' as "Descripcion" from dummy where 1=2;
					
				ELSEIF(:p_TipoEntidad = 'AUTORIZACION' AND :p_TipoOperacion = 'L') THEN
				
					--AUTORIZACION LOCK
            
					v_AutorizadoPrincipal = SELECT * 
											FROM "BSCL.Portafolio.BD::POR.Autorizado"
											WHERE "IdAutorizado" = :p_IdEntidad;	
					
					UPDATE  AUT
					SET		AUT."IdEstado" = AUP."IdEstado",
							AUT."FechaModificacion" = AUP."FechaModificacion"		
					FROM "BSCL.Portafolio.BD::POR.Autorizado" AUT
					INNER JOIN :v_AutorizadoPrincipal AUP 
					ON AUT."FechaCreacion" = AUP."FechaCreacion"
					AND AUT."IdPortafolio" != AUP."IdPortafolio"
					INNER JOIN :v_TablaPortafolio AS POR
					ON POR."IdPortafolio" != AUP."IdPortafolio";
											
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Autorizacion bloqueada' as "Descripcion" from dummy where 1=2;
				
				ELSEIF (:p_TipoEntidad = 'ANOTACION' AND :p_TipoOperacion = 'I') THEN
					
					--ANOTACION INSERT.
				
					v_AnotacionPrincipal = 	SELECT * 
											FROM "BSCL.Portafolio.BD::POR.Anotacion"
											WHERE "IdAnotacion" = :p_IdEntidad;
											
					INSERT INTO  "BSCL.Portafolio.BD::POR.Anotacion"
					(    "IdAnotacion"
						,"IdPortafolio"
						,"FechaAnotacion"
						,"IdMotivoAnotacion"
						,"Titulo"
						,"Detalle"
						,"FechaIngreso"
						,"FechaCreacion"
						,"FechaModificacion"
						,"IdUsuarioCreador"
						,"IdRelevanciaAnotacion"
						,"IdAmbitoAnotacion"
						,"ValidoDesde"
						,"ValidoHasta"
					) SELECT
						 "BSCL.Portafolio.BD.Secuencias::Anotacion".NEXTVAL
						,POR."IdPortafolio"
						,AN."FechaAnotacion"
						,AN."IdMotivoAnotacion"
						,AN."Titulo"
						,AN."Detalle"
						,AN."FechaIngreso"
						,AN."FechaCreacion"
						,AN."FechaModificacion"
						,AN."IdUsuarioCreador"
						,AN."IdRelevanciaAnotacion"
						,AN."IdAmbitoAnotacion"
						,AN."ValidoDesde"
						,AN."ValidoHasta"
					FROM :v_TablaPortafolio AS POR
					INNER JOIN :v_AnotacionPrincipal AS AN
					ON POR."IdPortafolio" != AN."IdPortafolio"; 
					
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Ingreso Anotacion' as "Descripcion" from dummy where 1=2;

				ELSEIF (:p_TipoEntidad = 'ANOTACION' AND (:p_TipoOperacion = 'U' OR :p_TipoOperacion = 'D')) THEN
				
					--ANOTACION UPDATE.
					
					v_AnotacionPrincipal = 	SELECT * 
											FROM "BSCL.Portafolio.BD::POR.Anotacion"
											WHERE "IdAnotacion" = :p_IdEntidad;
					
					UPDATE AN
					SET	 AN."Titulo" 				= ANP."Titulo"
						,AN."Detalle"				= ANP."Detalle"
						,AN."IdMotivoAnotacion" 	= ANP."IdMotivoAnotacion"
						,AN."FechaModificacion" 	= ANP."FechaModificacion"
						,AN."IdAmbitoAnotacion"		= ANP."IdAmbitoAnotacion"
						,AN."IdRelevanciaAnotacion"	= ANP."IdRelevanciaAnotacion"
						,AN."Borrado"				= ANP."Borrado"
						,AN."ValidoDesde"			= ANP."ValidoDesde"
					FROM "BSCL.Portafolio.BD::POR.Anotacion" AS AN
				 	INNER JOIN :v_AnotacionPrincipal AS ANP
					ON ANP."FechaCreacion" = AN."FechaCreacion"
					AND ANP."IdPortafolio" != AN."IdPortafolio"
					INNER JOIN :v_TablaPortafolio AS POR
					ON POR."IdPortafolio" != AN."IdPortafolio";
					
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Modificacion o Eliminacion Anotacion' as "Descripcion" from dummy where 1=2;

				ELSEIF (:p_TipoEntidad = 'CUENTACUSTODIO' AND :p_TipoOperacion = 'I') THEN
					
					--INSERT CUENTA CUSTODIO.
					
					v_CuentaCustodioPrincipal = SELECT * 
												FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
												WHERE "IdCuentaCustodioPortafolio" = :p_IdEntidad;
													
					INSERT INTO "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
					(
						 "IdCuentaCustodioPortafolio"
					  	,"IdPortafolio"
					   	,"IdBoveda"
					   	,"Codigo"
					   	,"FechaIngreso"
					   	,"FechaCreacion"
						,"FechaModificacion"
						,"IdUsuarioCreador"
						,"IdEstado"
						,"IdEstadoAutorizacion"
						,"IdTipoCuentaCustodia"
						,"IdTipoCustodia"
						,"IdModeloCustodia"
						,"IdPersonaBancoCustodio"
						,"IdPersonaBancoLiquidador"
						,"LiquidaDividendos"
						,"LiquidaCupones"
						,"LiquidaVencimientos"
						,"LiquidaPrepagos"
					) SELECT
						 "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio".NEXTVAL
						,POR."IdPortafolio"
						,CC."IdBoveda"
					   	,CC."Codigo"
					   	,CC."FechaIngreso"
					   	,CC."FechaCreacion"
						,CC."FechaModificacion"
						,CC."IdUsuarioCreador"
						,CC."IdEstado"
						,CC."IdEstadoAutorizacion"
						,CC."IdTipoCuentaCustodia"
						,CC."IdTipoCustodia"
						,CC."IdModeloCustodia"
						,CC."IdPersonaBancoCustodio"
						,CC."IdPersonaBancoLiquidador"
						,CC."LiquidaDividendos"
						,CC."LiquidaCupones"
						,CC."LiquidaVencimientos"
						,CC."LiquidaPrepagos"
					FROM :v_TablaPortafolio AS POR
					INNER JOIN :v_CuentaCustodioPrincipal AS CC
					ON POR."IdPortafolio" != CC."IdPortafolio"; 
					
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Ingreso Cuentas Custodio' as "Descripcion" from dummy where 1=2;
					
				ELSEIF (:p_TipoEntidad = 'CUENTACUSTODIO' AND :p_TipoOperacion = 'U') THEN
					
					v_CuentaCustodioPrincipal = SELECT * 
												FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
												WHERE "IdCuentaCustodioPortafolio" = :p_IdEntidad;
					
					UPDATE  CC
					SET
						 CC."IdBoveda" 					= CCP."IdBoveda"
					   	,CC."Codigo" 					= CCP."Codigo"
					   	,CC."FechaIngreso" 				= CCP."FechaIngreso"
					   	,CC."FechaCreacion" 			= CCP."FechaCreacion"
						,CC."FechaModificacion" 		= CCP."FechaModificacion"
						,CC."IdUsuarioCreador" 			= CCP."IdUsuarioCreador"
						,CC."IdEstado" 					= CCP."IdEstado"
						,CC."IdEstadoAutorizacion" 		= CCP."IdEstadoAutorizacion"
						,CC."IdTipoCuentaCustodia" 		= CCP."IdTipoCuentaCustodia"
						,CC."IdTipoCustodia" 			= CCP."IdTipoCustodia"
						,CC."IdModeloCustodia" 			= CCP."IdModeloCustodia"
						,CC."IdPersonaBancoCustodio" 	= CCP."IdPersonaBancoCustodio"
						,CC."IdPersonaBancoLiquidador" 	= CCP."IdPersonaBancoLiquidador"
						,CC."LiquidaDividendos" 		= CCP."LiquidaDividendos"
						,CC."LiquidaCupones" 			= CCP."LiquidaCupones"
						,CC."LiquidaVencimientos" 		= CCP."LiquidaVencimientos"
						,CC."LiquidaPrepagos" 			= CCP."LiquidaPrepagos"						
					FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" CC
					INNER JOIN :v_TablaPortafolio POR 
					ON CC."IdPortafolio" = POR."IdPortafolio"
					AND POR."IdPortafolio" != :p_Portafolio
					INNER JOIN :v_CuentaCustodioPrincipal CCP
					ON POR."IdPortafolio" = CCP."IdPortafolio";
					
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Modificacion Cuentas Custodio' as "Descripcion" from dummy where 1=2;
					
				ELSEIF (:p_TipoEntidad = 'PREFERENCIASDEPAGO' AND (:p_TipoOperacion = 'I' OR :p_TipoOperacion = 'U')) THEN
				
					v_PreferenciaPagoPrincipal = 	SELECT * 
													FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
													WHERE "IdPreferenciaPagoPortafolio" = :p_IdEntidad;
					BEGIN
						DECLARE v_IdPreferenciaPagoPortafolio 	BIGINT;
						DECLARE v_countIdPreferenciaPago 		INT:=0;
					
						DECLARE CURSOR c_IdPreferenciaPagoPortafolio FOR
							SELECT "IdPreferenciaPagoPortafolio"
								,POR."IdPortafolio"
								,PPP."IdPreferenciaPago"
								,PPP."IdCuentaBancaria"
								,PPP."IdTarjetaBancaria"
								,PPP."FechaCreacion"
								,PPP."FechaModificacion"
								,PPP."IdUsuarioCreador"
								,PPP."IdEstado"
								,PPP."IdEmpresa"		
								,PPP."FechaIngreso"			 
								,PPP."IdEstadoAutorizacion"
								,PPP."PorDefecto"
								,PP."IdTipoOperacion"
								,PP."IdMoneda"								
						  	FROM :v_TablaPortafolio AS POR
							INNER JOIN :v_PreferenciaPagoPrincipal AS PPP ON POR."IdPortafolio" != PPP."IdPortafolio"
							INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP ON PP."IdPreferenciaPago" = PPP."IdPreferenciaPago" 
							WHERE PP."IdEmpresa" = p_IdEmpresa;
						 
						-- recorre portafolios del grupo excluyendo el actual
						FOR cur_row AS c_IdPreferenciaPagoPortafolio DO	
							
							SELECT COUNT(*) INTO v_countIdPreferenciaPago
							FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
							WHERE "IdPortafolio" 	= cur_row."IdPortafolio"
							AND "IdPreferenciaPago" = cur_row."IdPreferenciaPago"
							AND "IdCuentaBancaria"	= cur_row."IdCuentaBancaria"
							AND "IdEmpresa" 		= p_IdEmpresa;	
						
							IF cur_row."PorDefecto" = 'S' THEN
							
								-- desmarca el campo por defecto para los mismos tipos de operacion y moneda
								UPDATE  DOC
								SET  DOC."PorDefecto" = 'N',
			 						 DOC."FechaModificacion"  = cur_row."FechaModificacion"
								FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC, "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
								WHERE DOC."IdPreferenciaPagoPortafolio" != cur_row."IdPreferenciaPagoPortafolio"
								  AND DOC."IdPreferenciaPago" 	= PP."IdPreferenciaPago"  
								  AND DOC."IdEmpresa"           = PP."IdEmpresa"
								  AND DOC."IdEmpresa"           = p_IdEmpresa								  
								  AND DOC."IdPortafolio"        = cur_row."IdPortafolio"
								  AND PP."IdTipoOperacion"      = cur_row."IdTipoOperacion"
								  AND PP."IdMoneda" 			= cur_row."IdMoneda";
							
							END IF;						
						
							IF (v_countIdPreferenciaPago = 0) THEN  -- valida si no existe el Tipo de Operacion
							
								INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
								(					
									"IdPreferenciaPagoPortafolio",
									"IdPortafolio",
									"IdPreferenciaPago",
									"IdCuentaBancaria",
									"IdTarjetaBancaria",
								   	"FechaCreacion",
									"FechaModificacion",
									"IdUsuarioCreador",
									"IdEstado",
									"IdEmpresa",			
									"FechaIngreso",					 
									"IdEstadoAutorizacion",
									"PorDefecto"
								) VALUES 
								(  
								    "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
									cur_row."IdPortafolio",
									cur_row."IdPreferenciaPago",
									cur_row."IdCuentaBancaria",
									cur_row."IdTarjetaBancaria",
								   	cur_row."FechaCreacion",
									cur_row."FechaModificacion",
									cur_row."IdUsuarioCreador",
									cur_row."IdEstado",
									cur_row."IdEmpresa",
									cur_row."FechaIngreso",
									cur_row."IdEstadoAutorizacion",
									cur_row."PorDefecto"
								);								
							
								SELECT "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".CURRVAL INTO v_IdPreferenciaPagoPortafolio FROM DUMMY;
							
							ELSE	
								IF (v_countIdPreferenciaPago > 0) THEN	-- actualiza el tipo de operacion si existe

									UPDATE  PP
									SET
										 PP."IdTarjetaBancaria"		= cur_row."IdTarjetaBancaria"
									   	,PP."FechaCreacion"			= cur_row."FechaCreacion"
										,PP."FechaModificacion"		= cur_row."FechaModificacion"
										,PP."IdUsuarioCreador"		= cur_row."IdUsuarioCreador"
										,PP."IdEstado"				= cur_row."IdEstado"
										,PP."IdEmpresa"				= cur_row."IdEmpresa"		
										,PP."FechaIngreso"			= cur_row."FechaIngreso"			 
										,PP."IdEstadoAutorizacion"	= cur_row."IdEstadoAutorizacion"
										,PP."PorDefecto"			= cur_row."PorDefecto"			
									FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PP
									WHERE "IdPortafolio" 			= cur_row."IdPortafolio"
									AND "IdPreferenciaPago" 		= cur_row."IdPreferenciaPago"
									AND "IdCuentaBancaria"			= cur_row."IdCuentaBancaria"
									AND "IdEmpresa"					= p_IdEmpresa;	
								
								END IF;
							
							END IF;
						

						END FOR;
						
						p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion", CASE WHEN :p_TipoOperacion = 'I' THEN 'Ingreso Preferencias de Pago' ELSE 'Modificacion Preferencias de Pago' END as "Descripcion" from dummy where 1=2;
				
					END;

				ELSEIF (:p_TipoEntidad = 'NOTIFICACIONES' AND :p_TipoOperacion = 'U') THEN	

					v_PersonalizacionEventoPrincipal = 	SELECT * 
														FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento"
												 		WHERE "IdEntidad" = :p_IdEntidad;
												 
					UPDATE  PE
					SET
						 PE."IdConfiguracionEvento"		= PEP."IdConfiguracionEvento"
					  	,PE."FechaCreacion"				= PEP."FechaCreacion"
					   	,PE."IdUsuario"					= PEP."IdUsuario"
					   	,PE."SuspensionTemporal"		= PEP."SuspensionTemporal"
					   	,PE."FechaInicioSuspension"		= PEP."FechaInicioSuspension"
					   	,PE."FechaTerminoSuspension"	= PEP."FechaTerminoSuspension"	
					FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" PE
					INNER JOIN :v_TablaPortafolio AS POR 
					ON PE."IdEntidad" = POR."IdPortafolio"
					AND POR."IdPortafolio" != :p_Portafolio
					INNER JOIN :v_PersonalizacionEventoPrincipal AS PEP
					ON POR."IdPortafolio" = PEP."IdEntidad"
					WHERE PE."TipoEntidad" = 'POR'
					AND PE."IdEstado" = 'ACT';
					
					p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'Modificacion Notificacion' as "Descripcion" from dummy where 1=2;
								
				ELSEIF (:p_TipoEntidad = 'PORTAFOLIO' AND :p_TipoOperacion = 'U') THEN	
	
					v_portafolioPrincipal = SELECT * FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio" = :p_Portafolio AND "IdEstado" <> 'INA';
					
		            UPDATE PORUPD
					SET	 PORUPD."Proposito" 							= POR."Proposito"
						,PORUPD."IdCliente" 							= POR."IdCliente"
						,PORUPD."IdEstado" 								= POR."IdEstado"
						,PORUPD."FechaBloqueo" 							= POR."FechaBloqueo"
						,PORUPD."ComentarioDesbloqueo" 					= POR."ComentarioDesbloqueo"
						,PORUPD."IdTipoBloqueo" 						= POR."IdTipoBloqueo"
						,PORUPD."FechaIngreso" 							= POR."FechaIngreso"
						,PORUPD."FechaCreacion" 						= POR."FechaCreacion"
						,PORUPD."FechaModificacion" 					= POR."FechaModificacion"
						,PORUPD."IdCarga" 								= POR."IdCarga"
						,PORUPD."IdPerfilComercialBaseOrigen"			= POR."IdPerfilComercialBaseOrigen"
						,PORUPD."IdPerfilProductos"						= POR."IdPerfilProductos"
						,PORUPD."IdPerfilCobros"						= POR."IdPerfilCobros"
						,PORUPD."IdEstadoAutorizacion"					= POR."IdEstadoAutorizacion"
						,PORUPD."DescripcionPortafolio" 				= POR."DescripcionPortafolio"
						,PORUPD."IdPerfilLimites"						= POR."IdPerfilLimites"
						,PORUPD."IdPerfilCondicionesOperativas"			= POR."IdPerfilCondicionesOperativas"
						,PORUPD."IdPerfilProductosExtra"				= POR."IdPerfilProductosExtra"
					 	,PORUPD."IdPerfilCobrosExtra"					= POR."IdPerfilCobrosExtra"
						,PORUPD."IdPerfilLimitesExtra"					= POR."IdPerfilLimitesExtra"
						,PORUPD."IdPerfilCondicionesOperativasExtra"	= POR."IdPerfilCondicionesOperativasExtra"
						,PORUPD."IdTipoDesbloqueo" 						= POR."IdTipoDesbloqueo"
						,PORUPD."FechaDesbloqueo" 						= POR."FechaDesbloqueo"
						,PORUPD."ComentarioBloqueo" 					= POR."ComentarioBloqueo"
						,PORUPD."IdDireccionEnvioCorrespondencia"		= POR."IdDireccionEnvioCorrespondencia"
						,PORUPD."IdTelefonoContacto"					= POR."IdTelefonoContacto"
						,PORUPD."IdEmpleadoEjecutivo"					= POR."IdEmpleadoEjecutivo"
						,PORUPD."EsEjecutivoExclusivo"					= POR."EsEjecutivoExclusivo"
						,PORUPD."NumeroFicha"							= POR."NumeroFicha"
						,PORUPD."IdNombreFantasia"						= POR."IdNombreFantasia"
						,PORUPD."Alias" 								= POR."Alias"
						,PORUPD."IdPersonaBrokerExtranjero" 			= POR."IdPersonaBrokerExtranjero"
						,PORUPD."IdPerfilCobrosPeru" 					= POR."IdPerfilCobrosPeru"
						,PORUPD."IdPerfilCobrosPeruExtra" 				= POR."IdPerfilCobrosPeruExtra"
						--,PORUPD."IdMatrizAgrupadorAPV" 					= POR."IdMatrizAgrupadorAPV"
						--,PORUPD."CodigoPortafolio" 						= POR."CodigoPortafolio"
					FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORUPD
					INNER JOIN :v_TablaPortafolio AS TAB
					ON PORUPD."IdPortafolio" = TAB."IdPortafolio"
			 		INNER JOIN :v_portafolioPrincipal AS POR
					ON PORUPD."IdPortafolio" != POR."IdPortafolio";
					
					
					--INDICADORES.
					
					SELECT 
						COUNT(IND."IdIndicador") INTO v_countGrupoIndicadores
					FROM "BSCL.Plataforma.BD::IND.Indicador" AS IND
					INNER JOIN :v_TablaPortafolio AS TAB
					ON IND."IdEntidad" = TAB."IdPortafolio"
					WHERE TAB."IdPortafolio" != :p_Portafolio;

					IF(v_countGrupoIndicadores > 0) THEN
						DELETE FROM "BSCL.Plataforma.BD::IND.Indicador" 
						WHERE "IdIndicador" IN (							
							SELECT 
								IND."IdIndicador" --,TAB."IdPortafolio",IND."IdTipoIndicador" 
							FROM :v_TablaPortafolio AS TAB
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
							ON IND."IdEntidad" = TAB."IdPortafolio"
							AND "IdPortafolio" != :p_Portafolio
						);
					END IF;					
					
					SELECT COUNT("IdIndicador") INTO v_countIndicadores 
					FROM "BSCL.Plataforma.BD::IND.Indicador";
					
					IF (v_countIndicadores > 0) THEN
						INSERT INTO  "BSCL.Plataforma.BD::IND.Indicador" 
						  (
						   "IdIndicador",
						   "IdEntidad",
						   "IdTipoEntidad",
						   "IdTipoIndicador",
						   "ModeloCliente", -- <TipoCliente>
						   "IdEmpresa",
						   "ValorIndicador",
						   "FechaIngreso",
						   "FechaCreacion",
						   "FechaModificacion",
						   "IdEstadoAutorizacion"
						   )  SELECT 
							"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL,
							TAB."IdPortafolio",
							IND."IdTipoEntidad",
							IND."IdTipoIndicador",
							IND."ModeloCliente",
							IND."IdEmpresa",
							IND."ValorIndicador",
							IND."FechaIngreso",
							IND."FechaCreacion",
							IND."FechaModificacion",
							IND."IdEstadoAutorizacion"				
						FROM :v_TablaPortafolio AS TAB	
						INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
						ON IND."IdEntidad" = :p_Portafolio
						AND TAB."IdPortafolio" != :p_Portafolio
						ORDER BY TAB."IdPortafolio";
					END IF;
						
						
					-- FIN INDICADORES.
													
					p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", 'Modificacion Portafolio' AS "Descripcion" FROM dummy WHERE 1=2;
					
				ELSEIF (:p_TipoEntidad = 'INDICADORES' AND :p_TipoOperacion = 'U') THEN	
							
					SELECT 
						COUNT(IND."IdIndicador") INTO v_countGrupoIndicadores
					FROM "BSCL.Plataforma.BD::IND.Indicador" AS IND
					INNER JOIN :v_TablaPortafolio AS TAB
					ON IND."IdEntidad" = TAB."IdPortafolio"
					WHERE TAB."IdPortafolio" != :p_Portafolio;

					IF(v_countGrupoIndicadores > 0) THEN
						DELETE FROM "BSCL.Plataforma.BD::IND.Indicador" 
						WHERE "IdIndicador" IN (							
							SELECT 
								IND."IdIndicador" --,TAB."IdPortafolio",IND."IdTipoIndicador" 
							FROM :v_TablaPortafolio AS TAB
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
							ON IND."IdEntidad" = TAB."IdPortafolio"
							LEFT JOIN (	SELECT 
											"IdEntidadOrigen"
											,"CampoModificado"
											,"IdSolicitudCambio"
										FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
										GROUP BY "IdEntidadOrigen","CampoModificado", "IdSolicitudCambio")  AS INDS
							ON IND."IdTipoIndicador" = INDS."CampoModificado"
							WHERE INDS."IdSolicitudCambio" = :p_IdEntidad
							AND "IdPortafolio" != :p_Portafolio
						);
					END IF;					
					
					INSERT INTO  "BSCL.Plataforma.BD::IND.Indicador" 
					  (
					   "IdIndicador",
					   "IdEntidad",
					   "IdTipoEntidad",
					   "IdTipoIndicador",
					   "ModeloCliente", -- <TipoCliente>
					   "IdEmpresa",
					   "ValorIndicador",
					   "FechaIngreso",
					   "FechaCreacion",
					   "FechaModificacion",
					   "IdEstadoAutorizacion"
					   )  SELECT 
						"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL,
						TAB."IdPortafolio",
						IND."IdTipoEntidad",
						IND."IdTipoIndicador",
						IND."ModeloCliente",
						IND."IdEmpresa",
						IND."ValorIndicador",
						IND."FechaIngreso",
						IND."FechaCreacion",
						IND."FechaModificacion",
						IND."IdEstadoAutorizacion"				
					FROM :v_TablaPortafolio AS TAB	
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
					ON IND."IdEntidad" = :p_Portafolio
					LEFT JOIN (	SELECT 
									"IdEntidadOrigen"
									,"CampoModificado"
									,"IdSolicitudCambio"
								FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
								GROUP BY "IdEntidadOrigen","CampoModificado", "IdSolicitudCambio")  AS INDS
					ON IND."IdTipoIndicador" = INDS."CampoModificado"
					WHERE INDS."IdSolicitudCambio" = :p_IdEntidad
					AND TAB."IdPortafolio" != :p_Portafolio
					ORDER BY TAB."IdPortafolio";
							
					/*	INDICADORES	*/				
				ELSE
					p_out_ErroresContador	:= 1;
					p_out_ErroresDetalle	= select '' as "Codigo",'El TipoEntidad y el TipoOperacion no coinciden con las opciones disponibles.' as "Descripcion" from dummy where 1=2;
					
				END IF;
			ELSE
				p_out_AdvertenciasContador	:= 1;
				p_out_AdvertenciasDetalle	= select 'NO_EXISTEN_PORTAFOLIOS_RELACIONADOS' as "Codigo",'' as "RequiereAutorizacion",'No se encontraron otros portafolios relacionados.' as "Descripcion" from dummy;
			
            END IF;
							
		--------------------
		--------------------
		--FIN SP 
	
		END;
	END;	
END;