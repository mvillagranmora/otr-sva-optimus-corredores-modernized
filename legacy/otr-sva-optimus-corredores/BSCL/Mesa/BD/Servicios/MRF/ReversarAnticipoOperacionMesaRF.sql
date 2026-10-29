SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ReversarAnticipoOperacionMesaRF";

CREATE PROCEDURE
--  Proposito: Anular operacion mesa RF
--  Autor: projo
--  Fecha Creacion: 04/07/2016
"BSCL.Mesa.BD.Servicios::MRF.ReversarAnticipoOperacionMesaRF" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la operacion
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la operacion
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la operacion 
    IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
	
	IN p_Operaciones				TABLE(
											"IdOperacion"					BIGINT
											,"IdEstadoSiguiente"			NVARCHAR(3)
									),

    OUT p_out_Operaciones			TABLE(
    									"IdOperacion"			BIGINT
    									,"IdEstado"				NVARCHAR(3)
									),
	  									
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
							DEFAULT SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
							DEFAULT SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	DECLARE v_AdvertenciasDetalleCus  TABLE ("IdGenerarMovimientoCustodia"			BIGINT,
										"IdGrupoOperacion"						BIGINT,
										"Codigo"								NVARCHAR(100),
										"RequiereAutorizacion"					NVARCHAR(40),
										"Descripcion"							NVARCHAR(5000))
							DEFAULT SELECT 0 as "IdGenerarMovimientoCustodia",0 as "IdGrupoOperacion",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	DECLARE v_ErroresDetalleCus  		TABLE ("IdGenerarMovimientoCustodia"			BIGINT,
										"IdGrupoOperacion"						BIGINT,
										"Codigo"								NVARCHAR(100),
										"Descripcion"							NVARCHAR(5000))
							DEFAULT SELECT 0 as "IdGenerarMovimientoCustodia",0 as "IdGrupoOperacion",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
		
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
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo" UNION ALL
								SELECT "IdGrupoOperacion" as "IdInterno",
									"Codigo",
									"Descripcion"
								FROM :v_ErroresDetalleCus;
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo" UNION ALL
								SELECT "IdGrupoOperacion" as "IdInterno",
									"Codigo",
									"RequiereAutorizacion",
									"Descripcion"
								FROM :v_AdvertenciasDetalleCus;
				 					 	
	END;
	
	/*DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select 0,v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)	
			RESIGNAL; 
		END IF;		
	END;*/
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	BEGIN
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count_oper	BIGINT;
		DECLARE v_countVig 					BIGINT :=0;
		DECLARE v_IdPaisSistema				BIGINT;
		DECLARE v_TABLE_OID					BIGINT :=0;
		DECLARE v_FechaOperacionSecondate 	SECONDDATE;
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_IdMonedaSistema			NVARCHAR(3);
		DECLARE v_IdSucursal		BIGINT;
		DECLARE v_FechaOperacionVarchar 	NVARCHAR(10);
		/*
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
		v_AdvertenciasDetalleCus = SELECT 0 as "IdGenerarMovimientoCustodia",0 as "IdGrupoOperacion",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_ErroresDetalleCus = SELECT 0 as "IdGenerarMovimientoCustodia",0 as "IdGrupoOperacion",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		*/			
		
		v_OperacionesCustoria = SELECT ROW_NUMBER() over (PARTITION BY det."IdOperacion"	ORDER BY det."IdOperacion" DESC) as "IdInternoDetalle"
									,det.*
									,ope."IdEstadoSiguiente" 
								FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" det
									INNER JOIN :p_Operaciones ope ON (ope."IdOperacion" = det."IdOperacion");
		
		select 'ConsultarDetalleOperacionesMesaRF' as "ConsultarDetalleOperacionesMesaRF",* from :v_OperacionesCustoria;
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(*) as "count" into v_count_oper
		FROM :p_Operaciones;
		if (v_count_oper = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESO_OPERACION_MESA_RF' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'PLA.MonedaSistema',v_IdMonedaSistema);
		
		v_FechaOperacionVarchar := TO_VARCHAR(:v_FechaOperacionSecondate,'YYYY-MM-DD');
		
		SELECT "IdSucursal" 
		INTO v_IdSucursal
		FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" em
			INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" us ON (em."IdEmpleado"= us."IdEntidad" and us."TipoEntidad" = 'EMP' and us."IdUsuario"= p_IdUsuario);--where "IdEmpleado"=7305

		SELECT "IdPais"
		INTO v_IdPaisSistema
		FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = :p_IdEmpresa;	
		/*
		if (p_UsaReglasNegocio = 'S') then
			CALL "BSCL.Mesa.BD.Reglas.MRF::ServicioCumpleRequisitosAnularOperacionRF"(p_IdEmpresa => p_IdEmpresa
																					,p_DatosOperacion => :v_reglaOperacion
																					,p_DatosDetalle => :v_reglaDetalleOperacion
																					,p_out_SalidaDetalle => v_out_SalidaDetalle
																					,p_out_Salida => v_out_Salida 	-- OK 	= Cumple todos los requisitos
																													-- ADV	= Advertencia
																													-- AUT	= Autorizacion							  	   
																		                                            -- ERR	= Error
																					);
		
			v_out_AdvertenciasDetalle = SELECT "IdRegistroOpe" as "IdInterno" ,"Codigo",'N' as "RequiereAutorizacion","ValorParametro","NombreParametro"
											FROM :v_out_SalidaDetalle
											WHERE "Resultado" = 'ADV'
											UNION ALL
											SELECT "IdRegistroOpe" as "IdInterno" ,"Codigo",'S' as "RequiereAutorizacion","ValorParametro","NombreParametro"
											FROM :v_out_SalidaDetalle
											WHERE "Resultado" = 'AUT';
			v_out_ErroresDetalle = 		SELECT "IdRegistroOpe" as "IdInterno" ,"Codigo","ValorParametro","NombreParametro"
											FROM :v_out_SalidaDetalle
											WHERE "Resultado" = 'ERR';
										
			select count("IdInterno") 
				into v_countErroresRegla 
			from :v_out_ErroresDetalle;
			
			if (:v_countErroresRegla > 0) then
				SIGNAL SALIDA_CONTROLADA;
			end if;
		end if;		
		*/
		
		
		--select 'v_EstadoOperacion' as "v_EstadoOperacion",* from :v_EstadoOperacion;
		--cambiar estado de la operacion y de la reserva
		v_TblNuevoEstado = 	SELECT ope."IdOperacion"
								,ope."IdInternoDetalle"
								,estado."IdTipoProducto" 	as "IdTipoProducto"
								,ope."IdEstado"				as "IdEstado"
								,estado."IdEstadoSiguiente" as "IdNuevoEstado"
								,ope."IdReservaDetalle" 	as "IdReserva"
								,ope."FolioCarteraCustodiaDetalle"  as "FolioCarteraCustodia"
								--,ope.*
							FROM :v_OperacionesCustoria ope
								LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" estado
									ON (ope."IdEstado" = estado."IdEstado" and estado."IdEstadoSiguiente" = ope."IdEstadoSiguiente" and estado."NombreEntidad"='CAMBIO_ESTADO' 
										and estado."IdTipoProducto" = ope."IdTipoProducto");
		--si hay alguna que quede vigente
		select count(*)
		into v_countVig
		from :v_TblNuevoEstado
		where "IdEstado" = 'ANT'; --si estan antocipada hay que ir a custodia a anular
		
		select 'v_TblNuevoEstado' as "v_TblNuevoEstado",v_countVig as "v_countVig",* from :v_TblNuevoEstado;
		
		IF (v_countVig > 0) THEN
			--select * from :v_TblNuevoEstado where "IdEstado" = 'VIG'
			--llamo a custodia
			/*v_DatosCustodia = 	SELECT
							ope."IdOperacion" 	as "IdOperacion"
							,'VIG' 				as "IdEstadoSiguiente"
						FROM :p_Operacion ope
						WHERE ope."Accion" = 'CERRAR';
			CALL "BSCL.Mesa.BD.Servicios::MRF.IngresarOperacionCustodiaMesaRF" (
							p_IdEmpresa => p_IdEmpresa
							,p_IdUsuario => p_IdUsuario
							,p_IdModulo => p_IdModulo
							,p_UsaReglasNegocio => p_UsaReglasNegocio
							,p_Operaciones => :v_DatosCustodia
							,p_out_Operaciones => v_out_Operaciones
							,p_out_AdvertenciasContador => p_out_AdvertenciasContador
							,p_out_AdvertenciasDetalle => p_out_AdvertenciasDetalle
							,p_out_ErroresContador => p_out_ErroresContador
							,p_out_ErroresDetalle => p_out_ErroresDetalle);*/
			
		END IF;
		
		select * from :v_ErroresDetalleCus;
		--	select * from "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" where "NombreEntidad"='CAMBIO_ESTADO' and "IdTipoProducto"='MERF'					
		--	select 'v_TblNuevoEstado' as "v_TblNuevoEstado",* from :v_TblNuevoEstado;
		
		--v_TblNuevoEstadoFinal = 	
		INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"
		SELECT est."IdOperacion"
			--,est."IdInternoDetalle"
			,est."IdTipoProducto"
			--,est."IdEstado"
			,est."IdNuevoEstado"
			,est."IdReserva"
			,est."FolioCarteraCustodia"
			,0 AS "MontoOperacionAnticipo"
			,0 AS "MontoOperacionInicialAnticipo"
			,0 AS "MontoOperacionFinalAnticipo"
			,0 AS "TasaAnticipo"
			,0 AS "MontoOperacionMonedaAnticipo"
			,0 AS "MontoOperacionInicialMonedaAnticipo"
			,0 AS "MontoOperacionFinalMonedaAnticipo"
			,'' AS "IdMonedaLiquidacionAnticipo"
			,'' AS "IdCondicionLiquidacionAnticipo"
			,'0001-01-01' AS "FechaCondicionLiquidacionAnticipo"
		FROM :v_TblNuevoEstado est
			LEFT JOIN :v_ErroresDetalleCus errCus on (errCus."IdGrupoOperacion" = est."IdOperacion" and errCus."IdGenerarMovimientoCustodia" = est."IdInternoDetalle")
		WHERE errCus."IdGrupoOperacion" IS NULL;
		--select 'v_TblNuevoEstadoFinal' as "v_TblNuevoEstadoFinal", * from :v_TblNuevoEstadoFinal;
		SELECT 'BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN' as "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN",* FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN";
		
		CALL "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion" (
														p_IdEmpresa => p_IdEmpresa
	    												,p_IdUsuario => p_IdUsuario
	    												,p_IdModulo 	=> p_IdModulo
	    												,p_simularOperacion => 'N'
														,p_UsaReglasNegocio => p_UsaReglasNegocio
	  													,p_IdPais => v_IdPaisSistema
	    												--,p_TblNuevoEstado  => :v_TblNuevoEstadoFinal
	  													--,p_out_TblResultado => v_out_TblResultado --OK, ADV, ERR
														);
				
			select 'BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT' as "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT",* from "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT";
		
		v_out_AdvertenciasDetalle = SELECT * FROM :v_out_AdvertenciasDetalle UNION ALL
									SELECT "IdOperacion" as "IdInterno" ,"MsgResultado" as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro"
									FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT"--:v_out_TblResultado
									WHERE "CodResultado" = 'ADV';
		v_out_ErroresDetalle = 		SELECT * FROM :v_out_ErroresDetalle UNION ALL
									SELECT "IdOperacion" as "IdInterno" ,"MsgResultado" as "Codigo",'' as "ValorParametro",'' as "NombreParametro"
										FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT"--:v_out_TblResultado
										WHERE "CodResultado" = 'ERR';
		
		
		p_out_Operaciones = SELECT conOp."IdOperacion"
								,conOp."IdEstado" 
							FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" conOp
								INNER JOIN :p_Operaciones op ON (conOp."IdOperacion"= op."IdOperacion");
							
	
		SIGNAL SALIDA_CONTROLADA;	
	END;
	
END;

