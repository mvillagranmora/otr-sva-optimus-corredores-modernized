SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.CrearTipoEventosAsociados"; 
CREATE PROCEDURE 
-- 
--  Proposito: Creación de Registro
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 23/10/2016
--  Id Servicio = 12035
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.CrearTipoEventosAsociados" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------
	IN p_IdTipoEvento				NVARCHAR(40),
	IN p_IdDefinicionAsiento		BIGINT,
	IN p_Operador					NVARCHAR(10),
	IN p_Estado						NVARCHAR(1),
	-- Parametros de Salida
	OUT p_out_InformacionContador	INT,
	OUT p_out_InformacionDetalle	TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,  
	OUT p_out_ErroresContador		INT,   
	OUT p_out_ErroresDetalle		TABLE ("IdDefinicionAsiento" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN	
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_InformacionContador FROM :p_out_InformacionDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			
			p_out_ErroresDetalle = 
			SELECT :p_IdDefinicionAsiento as "IdDefinicionAsiento",v_errorExt as "Codigo",::SQL_ERROR_MESSAGE as "Descripcion" FROM DUMMY;

			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;

	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;											
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_Mensajes = 
			SELECT :p_IdDefinicionAsiento as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
			  FROM :v_out_ErroresDetalle_Basicos;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			p_out_ErrorDetalle = SELECT EM."IdInterno" as "IdDefinicionAsiento",EM."Codigo",EM."Descripcion" FROM :v_erroroutmsg EM;			
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" (
					"IdTipoEvento",
					"IdDefinicionAsiento",
					"Operador",
					"Estado")
			VALUES (:p_IdTipoEvento,
					:p_IdDefinicionAsiento,
					:p_Operador,
					:p_Estado);

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
		   SET "TipoEvento" = CASE WHEN (SELECT COUNT(1)
		   								   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento"
		   								  WHERE "IdDefinicionAsiento" = :p_IdDefinicionAsiento) > 1 THEN
		   						  'C'
		   					  ELSE
		   					  	  'S'
		   					  END
		 WHERE "IdDefinicionAsiento" = :p_IdDefinicionAsiento;
	   
	   
	   v_Mensajes = 
		SELECT :p_IdDefinicionAsiento as "IdInterno",'EVENTO_ASOCIADO_AGREGADO' as "Codigo",'Evento' as "CodigoDato1",
				:p_IdTipoEvento as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
		p_out_InformacionDetalle = SELECT EM."IdInterno" as "IdDefinicionAsiento",EM."Codigo",EM."Descripcion" FROM :v_erroroutmsg EM;
	   
	   SIGNAL SALIDA_CONTROLADA;
	END;
END;