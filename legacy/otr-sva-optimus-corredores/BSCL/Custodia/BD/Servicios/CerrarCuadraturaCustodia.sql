SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::CerrarCuadraturaCustodia";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::CerrarCuadraturaCustodia" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_Fecha					DATE,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_ErroresContador  	 	 	 	int
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
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador
		from "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_AdvertenciasDetalle";
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_ErroresDetalle"
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_ErroresDetalle";									

		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_ErroresContadorCustodia	BIGINT;	
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		--------------------

		BEGIN -- VALIDA
			SELECT COUNT(1) INTO v_ErroresContadorCustodia
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"	AS Custodia
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdEstado" <> 'ANU';
			
			IF(:v_ErroresContadorCustodia < 1) THEN
				
	           	v_TablaCodigosMensajes =	Select 	0				 						As "IdInterno", 
												'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA'	As "Codigo",
												'' 											As "CodigoDato1", 
												'' 											as "ValorDato1",
												''			 								As "CodigoDato2", 
												''											As "ValorDato2" 
										From dummy;
			                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_ErroresDetalle" ("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM :v_out_hash;

			END IF;
			
			SELECT count(1) into p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.CerrarCuadraturaCustodia_Salida_ErroresDetalle"; 
			
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;		
						
		END;

		-----------------------------------------------------------------
		-- ACTUALIZACIÓN ESTADO EN TABLA DE CUADRATURA DE CUSTODIA.
		-----------------------------------------------------------------
		  
		UPDATE	Custodia 
		SET		Custodia."IdEstado"				= 'CER',
				Custodia."FechaModificacion"	= Current_UTCTIMESTAMP
		FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
		WHERE Custodia."FechaCuadraturaCustodia" = p_Fecha;



		-----------------------------------------------------------------
		-- AGENDA PROCESO DE NORMATIVA.
		-----------------------------------------------------------------
	 
		v_Tab_Param = SELECT 'IdEmpresa' AS "NombreParametro" ,to_varchar(p_IdEmpresa) AS "Valor" FROM DUMMY
  		    UNION ALL SELECT 'IdTipoTarea' AS "NombreParametro" ,'PROCESOCUADRATURACUSTODIA_NOR' AS "Valor" FROM DUMMY
			UNION ALL SELECT 'IdSolicitudCircular' AS "NombreParametro" ,'0' AS "Valor" FROM DUMMY
   			UNION ALL SELECT 'FechaDesde' AS "NombreParametro" ,TO_VARCHAR (p_Fecha, 'YYYY-MM-DD') AS "Valor" FROM DUMMY;

		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
		 (
			 p_IdEmpresa  => :p_IdEmpresa,
			 p_IdUsuario  => :p_IdUsuario,
			 p_IdModulo   =>'NOR',
			 p_IdAgenda	  =>0, 
			 p_IdTipoTarea =>'PROCESOCUADRATURACUSTODIA_NOR',  
			 p_Segundos =>0, 
			 p_TablaParametros  =>:v_Tab_Param,
			 p_CreaParam		 =>'S',
			 p_out_IdPrograma	=>v_out_IdPrograma,
			 p_out_AdvertenciasContador	=>v_out_AdvertenciasContador,
			 p_out_AdvertenciasDetalle 	=>v_out_AdvertenciasDetalle,
			 p_out_ErroresContador 	=>v_out_ErroresContador,
			 p_out_ErroresDetalle 	=>v_out_ErroresDetalle
			 );	 

	END;
END;
