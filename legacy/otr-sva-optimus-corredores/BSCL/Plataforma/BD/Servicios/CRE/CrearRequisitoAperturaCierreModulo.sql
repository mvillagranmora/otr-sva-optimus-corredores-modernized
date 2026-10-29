SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CRE.CrearRequisitoAperturaCierreModulo";

-- 
--  Proposito: 
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 11/03/2016  
-- 
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::CRE.CrearRequisitoAperturaCierreModulo"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa          		BIGINT,                                                                            
   IN  p_IdUsuario              	BIGINT,                                                                            
   IN  p_IdModulo               	NVARCHAR(3),                                                                       
   IN  p_simularOperacion       	NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio       	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   -- Parámetros para lógica
   IN  p_IdCREModuloEmpresa			BIGINT DEFAULT NULL,
   IN  p_IdTipoRequisito  			NVARCHAR(3) ,
   IN  p_IdUsuarioRequisito 		BIGINT DEFAULT NULL,
   IN  p_IdPerfil  					BIGINT DEFAULT NULL,
   IN  p_IdModuloFuncionProceso  	BIGINT DEFAULT NULL,
   IN  p_Orden   					INTEGER DEFAULT NULL,
   IN  p_Tipo						CHAR(1) ,
   IN  p_HoraMinApertura			NVARCHAR(8) DEFAULT NULL,
   IN  p_HoraMinCierre				NVARCHAR(8) DEFAULT NULL,
   IN  p_Nombre			 			NVARCHAR(40) DEFAULT NULL,
   IN  p_AbreModulo				 	CHAR(1) ,
   IN  p_CierraModulo				CHAR(1) ,
   IN  p_IdEstado					NVARCHAR(3) ,
   
   
   -- Parametros de Salida
   OUT p_out_REF_ID                    BIGINT,  
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520)) DEFAULT EMPTY
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				table ("Codigo" nvarchar(100),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE, '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" from dummy;
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	
	p_out_REF_ID				:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" from dummy where 1=2;
	
	BEGIN --LOGICA

	    --------------------
		-- Declare Variables
		--------------------
		DECLARE v_Obligatorios					NVARCHAR(250);
		DECLARE v_resultadoRegla 				NVARCHAR(40);
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
    	
    	
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;		
			 	
		--------------------------------------------
		-- VALIDACIONES BASICAS
		-------------------------------------------- 

		BEGIN
			-- Validación básica de Empresa, Usuario, Módulo
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_out_ErroresDetalle,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;   

			-- Validación Datos Obligatorios
			
		END;

		--------------------------------------------------------
		--- VALIDACIONES DE NEGOCIO
		--------------------------------------------------------
		BEGIN
			--Posible validacion de Modulos - Empresa
			DECLARE v_countReq		INT;
			
			IF (:p_IdTipoRequisito = 'HOR') THEN
				p_TablaCodigosMensajes = SELECT DISTINCT 'REQUISITO_HORA_EXISTE' as "CodigoMensaje", 'HoraMin' as "CodigoDato" , COALESCE(:p_HoraMinApertura, :p_HoraMinCierre, '')  as "ValorDato"
										FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
										WHERE 	"IdTipoRequisito" = :p_IdTipoRequisito AND
												"Tipo" = :p_Tipo AND
			 									COALESCE("HoraMinApertura", '') = COALESCE(:p_HoraMinApertura, '') AND
			 									COALESCE("HoraMinCierre", '') = COALESCE(:p_HoraMinCierre, '') AND
			 									"Nombre" = :p_Nombre
			 							UNION ALL 
			 							Select "CodigoMensaje", "CodigoDato" , "ValorDato"
		                        		FROM :p_TablaCodigosMensajes;
		                        		
			ELSEIF (:p_IdTipoRequisito = 'USU') THEN 
				p_TablaCodigosMensajes = SELECT DISTINCT  'REQUISITO_USUARIO_EXISTE' as "CodigoMensaje", 'IdUsuario' as "CodigoDato" , :p_IdUsuarioRequisito  as "ValorDato"
										FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
										WHERE 	"IdTipoRequisito" = :p_IdTipoRequisito AND
												"Tipo" = :p_Tipo AND
												"IdUsuario" = :p_IdUsuarioRequisito AND
												"Nombre" = :p_Nombre
										UNION ALL 
			 							Select "CodigoMensaje", "CodigoDato" , "ValorDato"
		                        		FROM :p_TablaCodigosMensajes;
								
			ELSEIF (:p_IdTipoRequisito = 'PRF') THEN
				p_TablaCodigosMensajes = SELECT DISTINCT  'REQUISITO_PERFIL_EXISTE' as "CodigoMensaje", 'IdPerfil' as "CodigoDato" , :p_IdPerfil  as "ValorDato"
										FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
										WHERE 	"IdTipoRequisito" = :p_IdTipoRequisito AND
												"Tipo" = :p_Tipo AND
												"IdPerfil" = :p_IdPerfil AND
												"Nombre" = :p_Nombre
										UNION ALL 
			 							Select "CodigoMensaje", "CodigoDato" , "ValorDato"
		                        		FROM :p_TablaCodigosMensajes;
												
			ELSEIF (:p_IdTipoRequisito = 'PRO') THEN
				p_TablaCodigosMensajes = SELECT DISTINCT  'REQUISITO_HORA_EXISTE' as "CodigoMensaje", 'IdModuloFuncionProceso' as "CodigoDato" , :p_IdModuloFuncionProceso as "ValorDato"
										FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
										WHERE 	"IdTipoRequisito" = :p_IdTipoRequisito AND
												"Tipo" = :p_Tipo AND
												"IdModuloFuncionProceso" = :p_IdModuloFuncionProceso AND
												"Nombre" = :p_Nombre
										UNION ALL 
			 							Select "CodigoMensaje", "CodigoDato" , "ValorDato"
		                        		FROM :p_TablaCodigosMensajes;
			END IF;
			SELECT * FROM :p_TablaCodigosMensajes;

		END;

		--------------------------------------------   
		-- DICCIONARIO
		--------------------------------------------
        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

		p_out_ErroresDetalle =  Select "Codigo", "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2"  
		                        From :p_out_TablaMensajes Union All
								Select "Codigo", "Descripcion", "CodigoDato1", "ValorDato1", "CodigoDato2", "ValorDato2"
								From :p_out_ErroresDetalle;		


		--Si existen errores en las validaciones debe salir
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;					

		
		--Solo fue una simulacion del servicio... move bitch,get out the way
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;

   		 
   		------------------------------------------------------
		-- OBTENER NÚMERO DE SECUENCIA
		------------------------------------------------------
        SELECT "BSCL.Plataforma.BD.Secuencias::CRE.ModuloRequisitoAperturaCierre".NEXTVAL INTO p_out_REF_ID FROM DUMMY;


		------------------------------------------------------
		-- INSERTAR DATOS 
		------------------------------------------------------

		INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
		(
			 "IdModuloRequisitoAperturaCierre",
			 "IdCREModuloEmpresa",
			 "IdTipoRequisito",
			 "IdUsuario", 
			 "IdPerfil", 
			 "IdModuloFuncionProceso",
			 "Orden",
			 "Tipo",  
			 "HoraMinApertura",
			 "HoraMinCierre",
			 "Nombre",
			 "AbreModulo",
			 "CierraModulo",
			 "IdEstado"
		)
		VALUES (
		    :p_out_REF_ID,
		    :p_IdCREModuloEmpresa,
		   	UPPER(:p_IdTipoRequisito),
   			:p_IdUsuarioRequisito,
   			:p_IdPerfil,
   			:p_IdModuloFuncionProceso,
   			:p_out_REF_ID,
   			UPPER(:p_Tipo),
   			:p_HoraMinApertura,
   			:p_HoraMinCierre,
   			UPPER(:p_Nombre),
   			UPPER(:p_AbreModulo),
   			UPPER(:p_CierraModulo),
   			UPPER(:p_IdEstado)
		);
    End; 
END;