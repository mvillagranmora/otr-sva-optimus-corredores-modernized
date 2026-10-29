SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearCanasta";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearCanasta" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
	IN p_tablaCanastaMoneda			TABLE(
											 "IdCanasta"		BIGINT
			                                ,"IdMoneda"			NVARCHAR(3)
										  ),
	-- Parametros de Salida
	OUT p_out_TblCanastaMoneda table ("Ids" BIGINT),
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador int,   
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN
	DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" 				BIGINT
											 ,"Codigo" 					NVARCHAR(100)
											 ,"RequiereAutorizacion" 	NVARCHAR(1)
											 ,"ValorParametro" 			NVARCHAR(100)
											 ,"NombreParametro" 		NVARCHAR(40)
									   )DEFAULT SELECT 0  AS "IdInterno"
									                 , '' AS "Codigo"
									                 , '' AS "RequiereAutorizacion"
									                 , '' AS "ValorParametro"
									                 , '' AS "NombreParametro"
									            FROM DUMMY
									            WHERE 1=2;
	DECLARE v_out_ErroresDetalle  	   TABLE ("IdInterno" 		BIGINT
											 ,"Codigo" 			NVARCHAR(100)
											 ,"ValorParametro" 	NVARCHAR(100)
											 ,"NombreParametro" NVARCHAR(40)
									   )
									   DEFAULT SELECT 0  AS "IdInterno"
									                , '' AS "Codigo"
									                , '' AS "ValorParametro"
									                , '' AS "NombreParametro"
									           FROM DUMMY
									           WHERE 1=2;
	
	DECLARE v_count_operaciones		BIGINT;
	DECLARE v_fechaHoraSistema		NVARCHAR(40);
	
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
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv
								    INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	p_out_TblCanastaMoneda 		= select 0 as "Ids" from dummy where 1=2;
		
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa => :p_IdEmpresa,
			p_out_FechaHoraSistema => v_fechaHoraSistema
		);
		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		/*
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno"
			                            , "Codigo"
			                            , '' as "ValorParametro"
			                            , '' as "NombreParametro" 
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;	
		*/
		
		--DATOS DETALLE LOG
		v_DetalleLog = 
		(Select 'CANASTA'	    AS "SubRegistro",
				'IdCanasta'  AS "CampoRegistro",--MOD."IdLimite"			 		 as "CampoRegistro",
				"IdCanasta"  AS "ValorAntiguo",--MOD."IdEstadoAutorizacion"  	 as "ValorAntiguo",
				"IdMoneda"	AS "ValorNuevo" --:p_IdEstadoAutorizacion 		 as "ValorNuevo" 
		 FROM :p_tablaCanastaMoneda
		);

		--GRABAR LOG
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			 p_IdTipoLog		 	=> 'INGRESOCANASTA' 
			,p_IdModulo 			=> :p_IdModulo 
			,p_IdUsuario 			=> :p_IdUsuario 
			,p_IdEmpresa 			=> :p_IdEmpresa 
			,p_FechaSistemaLog 		=> TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido 	=> :p_IdUsuario
			,p_IdTipoRegistro 		=> 'FORWARD' --v_TipoEntidad--'CLIENTE'
			,p_DetalleLog 			=> :v_DetalleLog	-- hacia arriba obligatorios
			,p_NombreProceso 		=> 'Forward'-- hacia abajo opcionales
			,p_EstadoProceso 		=> 'VIG'
			);
		--LOG DE AUDITORIA (Fin)

		
		INSERT INTO "BSCL.Mesa.BD::FWD.CanastaMoneda"
				 ( "IdCanastaMoneda"
				 , "IdEmpresa"
				 , "IdCanasta" 
				 , "IdMoneda")		
		SELECT "BSCL.Mesa.BD.Secuencias::FWD.IdCanastaMoneda".NEXTVAL AS "IdCanastaMoneda"
		     , :p_IdEmpresa As "IdEmpresa"
			 , "IdCanasta"
			 , "IdMoneda"		
        FROM :p_tablaCanastaMoneda;

		p_out_TblCanastaMoneda = SELECT TOP 1 "IdCanastaMoneda" AS "Ids" 
								 FROM "BSCL.Mesa.BD::FWD.CanastaMoneda"
								 ORDER BY "IdCanastaMoneda" Desc;
				 
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'CREAR INSTRUMENTO CANASTA','FIN','FIN');	

	END;
END; 