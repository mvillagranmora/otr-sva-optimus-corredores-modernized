SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios.CAR::CrearCargaMasiva";
GO
CREATE PROCEDURE 
"BSCL.SocioNegocio.BD.Servicios.CAR::CrearCargaMasiva"(

    -- Parametros de Validacion                                                                                            
	IN p_IdEmpresa					BIGINT,
   	IN p_IdUsuario					BIGINT,
   	IN p_IdModulo					NVARCHAR(3),                                                                       
   	IN p_UsaReglasNegocio			NVARCHAR(1),


	IN p_IdTipoCargaMasiva 			BIGINT,
	IN p_CodigoArchivo				NVARCHAR(40),
	IN p_FechaCarga                 NVARCHAR(10),
	IN p_CargaMasiva                TABLE (
											"IdInterno"					BIGINT,
											"NombreArchivo"         	NVARCHAR(100)
										  ),

	IN p_DetalleCargaMasiva			TABLE (
											"IdInterno"					BIGINT,
											"DocumentoIdentidad" 		NVARCHAR(40),
											"CodigoPortafolio"		    NVARCHAR(100),
											"Entidad"					NVARCHAR(20),
											"CodigoIndicador"			NVARCHAR(100),
											"ValorIndicador"			NVARCHAR(255)
									      ),


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
	DECLARE v_CargaMasivaValidar TABLE ("IdCargaMasiva"     BIGINT, "CodigoArchivo"     NVARCHAR(40));



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
			RESIGNAL;
		END IF;		
	END;

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	v_CargaMasivaValidar            = SELECT 0 as "IdCargaMasiva",'' as "CodigoArchivo" FROM dummy DUMMY;

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
		v_SecuenciaCargaMasiva = 
		SELECT 	CM."IdInterno"
			   ,"BSCL.SocioNegocio.BD.Secuencias::CAR.CargaMasiva".NEXTVAL as "SecuenciaCargaMasiva"
		FROM :p_CargaMasiva AS CM;


		--------------------------------------------   
		-- INSERTAR ENCABEZADO CARGA MASIVA
		--------------------------------------------
		INSERT INTO "BSCL.SocioNegocio.BD::CAR.CargaMasiva"
		(
			"IdCargaMasiva",
			"IdTipoCargaMasiva",
			"IdEmpresa",
			"IdUsuario",
			"CantidadRegistros",
			"NombreArchivo",
		 	"FechaCarga",			
		 	"FechaProceso",
		  	"FechaIngresoRegistro",
			"IdEstado"
		)
		SELECT 
			MAX(SCM."SecuenciaCargaMasiva"),
			:p_IdTipoCargaMasiva,
			:p_IdEmpresa,
			:p_IdUsuario,
			COUNT(1),
			MAX(CM."NombreArchivo"),
			:p_FechaCarga,
			'0001-01-01',
			CURRENT_UTCTIMESTAMP,
			'ING'
		FROM :p_CargaMasiva AS CM
		INNER JOIN :v_SecuenciaCargaMasiva AS SCM ON (SCM."IdInterno" = CM."IdInterno")
		INNER JOIN :p_DetalleCargaMasiva AS DCM ON  (DCM."IdInterno" = CM."IdInterno");


		--------------------------------------------   
		-- INSERTAR DETALLE CARGA MASIVA
		--------------------------------------------

		INSERT INTO "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle",
			"IdCargaMasiva",
			"DocumentoIdentidad",
			"IdPortafolio",
			"CodigoPortafolio",
			"Entidad",
			"CodigoIndicador",
			"ValorIndicador",
			"ObservacionValidacion",			
			"IdEstado"
		)
		SELECT 
			"BSCL.SocioNegocio.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL,
			SCM."SecuenciaCargaMasiva",
			DCM."DocumentoIdentidad",
			COALESCE(POR."IdPortafolio",-1),
			DCM."CodigoPortafolio",			
			DCM."Entidad",
			DCM."CodigoIndicador",
			DCM."ValorIndicador",
			'OK',
			'ING'
		FROM :p_CargaMasiva AS CM
		INNER JOIN :v_SecuenciaCargaMasiva AS SCM ON (SCM."IdInterno" = CM."IdInterno")
		INNER JOIN :p_DetalleCargaMasiva AS DCM ON  (DCM."IdInterno" = CM."IdInterno")		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = DCM."DocumentoIdentidad")
		LEFT JOIN (
			SELECT * FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
			WHERE "IdEstadoAutorizacion"!='PEN'
		)POR ON (POR."IdPersona" = PER."IdPersona" AND RTRIM(LTRIM(UCASE(POR."CodigoPortafolio"))) = RTRIM(LTRIM(UCASE(DCM."CodigoPortafolio"))));
		
		
		
		--Salida controlada	
		--------------------------------------------------   
		-- VALIDA LA CM Y ACTUALIZA ESTADO DE INVALIDOS
		--------------------------------------------------
		
		v_CargaMasivaValidar = 
		SELECT 
			"SecuenciaCargaMasiva" AS "IdCargaMasiva",
			:p_CodigoArchivo AS "CodigoArchivo"
		FROM :v_SecuenciaCargaMasiva;


		CALL "BSCL.SocioNegocio.BD.Servicios.CAR::ValidarCargaMasiva"
		(
			 p_CargaMasivaValidar  		=> :v_CargaMasivaValidar			
			,p_out_CargaMasivaInvalida  => :v_CargaMasivaInvalida
			,p_out_ErroresValidacion    => :v_ErroresValidacion
		);


		-- Actualiza estado de CM que tienen registros invalidos
		UPDATE CM
		SET CM."IdEstado" = 'INV'
		FROM "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM, :v_CargaMasivaInvalida AS CMI
		WHERE CM."IdCargaMasiva" = CMI."IdCargaMasiva";


		-- Actualiza estado de los registros invalidos
		UPDATE CMD
		SET  CMD."IdEstado"              = 'INV',
			 CMD."ObservacionValidacion" = COALESCE(EV."ObservacionValidacion",'')
		FROM "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" AS CMD, :v_ErroresValidacion AS EV		
		WHERE CMD."IdCargaMasivaDetalle" = EV."IdCargaMasivaDetalle";
		
		
	END;
END;
