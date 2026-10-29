SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios.CAR::CrearCargaMasivaPlataforma";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios.CAR::CrearCargaMasivaPlataforma"(

    -- Parametros de Validacion                                                                                            
	IN  p_IdEmpresa					BIGINT,
   	IN  p_IdUsuario					BIGINT,
   	IN  p_IdModulo					NVARCHAR(3),                                                                       
   	IN  p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

	IN  p_FechaCarga                NVARCHAR(10),  -- Fecha de Sistema
	IN  p_CargaMasiva               TABLE (
											"IdInterno"				BIGINT,       -- Solo referencial
											"IdTipoCargaMasiva"     BIGINT,
											"NombreArchivo"         NVARCHAR(100)
										  ),	
	
	IN	p_DetalleCargaMasiva		TABLE (
											"IdInterno"							BIGINT,        -- Solo referencial
											"Nemotecnico"						NVARCHAR(40),
											"RF_PorcentajeDescuento"			DECIMAL(26,8),
											"RV_IdEntidadClasificadoraRiesgo"	NVARCHAR(10),
											"RV_Valor"							NVARCHAR(1)
									      ),
	

   	-- Parametros de Salida
   	OUT p_out_SecuenciaCargaMasiva  TABLE("IdInterno" BIGINT, "SecuenciaCargaMasiva" BIGINT),
   	
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
	DECLARE v_CargaMasivaValidar TABLE ("IdCargaMasiva"     BIGINT);



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
	v_CargaMasivaValidar            = SELECT 0 as "IdCargaMasiva" FROM dummy DUMMY;

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
		END; 
	END IF;

	--------------------
	--LOGICA
	--------------------
	BEGIN
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;


		-- Obtiene secuencias
		p_out_SecuenciaCargaMasiva = 
		SELECT 	CM."IdInterno"											AS "IdInterno"
			   ,"BSCL.Plataforma.BD.Secuencias::CargaMasiva".NEXTVAL	AS "SecuenciaCargaMasiva"
		FROM :p_CargaMasiva AS CM;
 
		--------------------------------------------   
		-- INSERTAR ENCABEZADO CARGA MASIVA
		--------------------------------------------
		INSERT INTO "BSCL.Plataforma.BD::PLA.CargaMasiva"
		(
			"IdCargaMasiva",
			"IdTipoCargaMasiva",
			"IdEmpresa",
			"IdUsuario",
			"CantidadRegistros",
			"NombreArchivo",
		 	"FechaProceso",
		 	"IdEstado",
		 	"FechaCarga",
		  	"FechaIngresoRegistro"
		)
		SELECT 
			MAX(SCM."SecuenciaCargaMasiva"),
			MAX(CM."IdTipoCargaMasiva"),
			:p_IdEmpresa,
			:p_IdUsuario,
			COUNT(1),
			MAX(CM."NombreArchivo"),
			'0001-01-01',
			'ING',
			:p_FechaCarga,
			CURRENT_UTCTIMESTAMP
		FROM :p_CargaMasiva AS CM
			INNER JOIN :p_out_SecuenciaCargaMasiva AS SCM 
				ON (SCM."IdInterno" = CM."IdInterno")
			INNER JOIN :p_DetalleCargaMasiva AS DCM 
				ON  (DCM."IdInterno" = CM."IdInterno");


		--------------------------------------------   
		-- INSERTAR DETALLE CARGA MASIVA
		--------------------------------------------
				
		INSERT INTO "BSCL.Plataforma.BD::PLA.CargaMasivaDetalle"
		(	"IdCargaMasivaDetalle" 				,
			"IdCargaMasiva" 					,
			"Nemotecnico"						,
			"IdEstado"							,
			"RF_PorcentajeDescuento"			,
			"RV_IdEntidadClasificadoraRiesgo"	,
			"RV_Valor"							
		)
		SELECT 
			"BSCL.Plataforma.BD.Secuencias::CargaMasiva".NEXTVAL,
			SCM."SecuenciaCargaMasiva"				,
			DCM."Nemotecnico"						,
			'ING'									,
			DCM."RF_PorcentajeDescuento"			,
			DCM."RV_IdEntidadClasificadoraRiesgo"	,
			DCM."RV_Valor"
		FROM :p_CargaMasiva AS CM
			INNER JOIN :p_out_SecuenciaCargaMasiva AS SCM 
				ON (SCM."IdInterno" = CM."IdInterno")
			INNER JOIN :p_DetalleCargaMasiva AS DCM 
				ON (DCM."IdInterno" = CM."IdInterno");

		--------------------------------------------------   
		-- VALIDA LA CM Y ACTUALIZA ESTADO DE INVALIDOS
		--------------------------------------------------

		-- Obtiene Id de las CM a validar
		v_CargaMasivaValidar = 
		SELECT "SecuenciaCargaMasiva" AS "IdCargaMasiva"
		FROM :p_out_SecuenciaCargaMasiva;
		
		
		CALL "BSCL.Plataforma.BD.Servicios.CAR::ValidarCargaMasivaPlataforma"
		(	 p_CargaMasivaValidar  		=> :v_CargaMasivaValidar
			,p_out_CargaMasivaInvalida  => :v_CargaMasivaInvalida
			,p_out_ErroresValidacion    => :v_ErroresValidacion
		);

		-- Actualiza estado de CM que tienen registros invalidos
		UPDATE CM
		SET CM."IdEstado" = 'INV'
		FROM "BSCL.Plataforma.BD::PLA.CargaMasiva" AS CM, 
			 :v_CargaMasivaInvalida AS CMI
		WHERE CM."IdCargaMasiva" = CMI."IdCargaMasiva";
		

		-- Actualiza estado de los registros invalidos
		UPDATE CMD
		SET  CMD."IdEstado"              = 'INV',
		     CMD."ObservacionValidacion" = EV."ObservacionValidacion"
		FROM "BSCL.Plataforma.BD::PLA.CargaMasivaDetalle" AS CMD, 
			 :v_ErroresValidacion AS EV		
		WHERE CMD."IdCargaMasivaDetalle" = EV."IdCargaMasivaDetalle";

  	END;    
END;