SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::AgregarProgramaActualizarSaldosReporteAntiguedad";
GO
CREATE PROCEDURE "BSCL.CuentaInversion.BD.Servicios::AgregarProgramaActualizarSaldosReporteAntiguedad" (
	-- Parametros de Entrada
	IN  p_IdEmpresa         BIGINT,
    IN  p_IdUsuario         BIGINT,
    
	IN  p_FechaMovimiento	NVARCHAR(10),
	IN  p_Rut				NVARCHAR(40),
	IN	p_CodigoPortafolio  NVARCHAR(40),
	IN	p_Moneda			NVARCHAR(3),
	
	--Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle  	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador      	INT,
	OUT p_out_ErroresDetalle       	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;
	DECLARE v_out_IdPrograma			TABLE("IdPrograma" BIGINT);
	DECLARE v_Count						BIGINT := 0;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado	
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	
	v_AdvertenciasContador		:= 0;
	v_ErroresContador			:= 0;
	v_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	
	v_out_IdPrograma			= SELECT 0 as "IdPrograma" FROM dummy where 1=2;

	BEGIN
		--------------------
		--VALIDACIONES
		--------------------
		SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
				ON DOC."IdPersona" = CLI."IdPersona"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
				ON POR."IdCliente" = CLI."IdCliente"
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
			ON CTA."IdCliente" = CLI."IdCliente" AND CTA."IdPortafolio" = POR."IdPortafolio" 
				WHERE DOC."DocumentoIdentidad" = TRIM(:p_Rut) AND POR."CodigoPortafolio" = UPPER(TRIM(:p_CodigoPortafolio)) AND CTA."IdMoneda" = UPPER(TRIM(:p_Moneda));
		
		IF(v_Count = 0) THEN
			p_out_ErroresDetalle = select 'ERROR_NO_SE_ENCONTRO_LA_CUENTA_DEL_CLIENTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--------------------
		--LOGICA
		--------------------
		
		--Parametros de entrada para el Job
 	 	v_TablaParametros = 
 	 		SELECT 'IdEmpresa'			AS "NombreParametro", TO_NVARCHAR(p_IdEmpresa)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'IdUsuario'			AS "NombreParametro", TO_NVARCHAR(p_IdUsuario)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'FechaMovimiento'	AS "NombreParametro", TO_NVARCHAR(p_FechaMovimiento)	AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'Rut'				AS "NombreParametro", TO_NVARCHAR(p_Rut)				AS "Valor" FROM DUMMY
			UNION ALL
			SELECT 'CodigoPortafolio'	AS "NombreParametro", TO_NVARCHAR(p_CodigoPortafolio)	AS "Valor" FROM DUMMY
			UNION ALL
 	 		SELECT 'IdMoneda'			AS "NombreParametro", TO_NVARCHAR(p_Moneda)				AS "Valor" FROM DUMMY
 	 		;
 	 	
 	 	CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"    
				(   
				     p_IdEmpresa     			=> :p_IdEmpresa
					,p_IdUsuario    			=> :p_IdUsuario
					,p_IdModulo    				=> 'CTA'					
				 	,p_IdAgenda	    			=> 0
					,p_IdTipoTarea				=> 'ACTUALIZAR_SALDOS_REPORTE_ANTIGUEDAD'  -- Job a ejecutar
					,p_Segundos	    			=> 0     -- En cuantos segundos mas se ejecuta
				    ,p_TablaParametros 			=> :v_TablaParametros
				 	,p_CreaParam				=> 'S'		       -- S/N indica si vienen parametros
					,p_out_IdPrograma 			=> :v_out_IdPrograma    -- Id el job creado
					,p_out_AdvertenciasContador => v_AdvertenciasContador
					,p_out_AdvertenciasDetalle 	=> v_AdvertenciasDetalle
					,p_out_ErroresContador 		=> v_ErroresContador
					,p_out_ErroresDetalle 		=> v_ErroresDetalle
				);

		p_out_AdvertenciasDetalle 	= SELECT * FROM :v_AdvertenciasDetalle UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 		= SELECT * FROM :v_ErroresDetalle UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		
		IF (:v_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
  	END;
END;