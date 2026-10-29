SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CAR.EliminarCargaMasiva";
GO 
CREATE PROCEDURE 

-- Contador para recompilacion sin modificaciones : 1
"BSCL.Orden.BD.Servicios::CAR.EliminarCargaMasiva"(

    -- Parametros de Validacion                                                                                            
	IN  p_IdEmpresa					BIGINT,
   	IN  p_IdUsuario					BIGINT,
   	IN  p_IdModulo					NVARCHAR(3),                                                                       
   	IN  p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

	-- Id Carga Masiva a borrar 
	IN  p_CargaMasivaEliminar       TABLE ("IdCargaMasiva"             BIGINT),
										
   	-- Parametros de Salida
   	OUT p_out_AdvertenciasContador	INT,
   	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   	OUT p_out_ErroresContador		INT,   
   	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);	
	DECLARE v_ErroresDetalle         	TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520));
	DECLARE v_AdvertenciasDetalle	 	TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520));
	DECLARE v_RegistrosRech	         	TABLE ("IdCargaMasiva"   BIGINT);
	        	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 	
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;
	

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle       			= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
	v_AdvertenciasDetalle  			= SELECT '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" FROM DUMMY WHERE 1=2;
    v_RegistrosRech	       			= SELECT 0 as "IdCargaMasiva"  FROM DUMMY WHERE 1=2;


	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		-- Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		

		CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosEliminarCargaMasiva"(:p_CargaMasivaEliminar, :v_RegistrosRech ,:v_ErroresDetalle, :v_AdvertenciasDetalle, :v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_AdvertenciasDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;	
		END IF;

	END; 
	END IF;



	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_SecuenciaCargaMasiva BIGINT;

		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;


		-- Descarta los registros que no pasaron la validación
		v_CargaMasivaEliminar =  
		SELECT *
		FROM :p_CargaMasivaEliminar
		WHERE "IdCargaMasiva" NOT IN (SELECT "IdCargaMasiva" FROM :v_RegistrosRech);
		

		--------------------------------------------   
		-- ELIMINA DETALLES DE CARGAS MASIVAS
		--------------------------------------------
		DELETE
		FROM "BSCL.Orden.BD::CAR.CargaMasivaDetalle"
		WHERE "IdCargaMasiva" IN (SELECT "IdCargaMasiva" FROM :v_CargaMasivaEliminar);
		

		--------------------------------------------   
		-- ELIMINA ENCABEZADOS DE CARGAS MASIVAS
		--------------------------------------------
		DELETE
		FROM "BSCL.Orden.BD::CAR.CargaMasiva"		
		WHERE "IdCargaMasiva" IN (SELECT "IdCargaMasiva" FROM :v_CargaMasivaEliminar);
				
		

  	END;    
END;