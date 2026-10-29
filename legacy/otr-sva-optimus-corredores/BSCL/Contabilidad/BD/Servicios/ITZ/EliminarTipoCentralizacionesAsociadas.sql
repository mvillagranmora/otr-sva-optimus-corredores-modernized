SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.EliminarTipoCentralizacionesAsociadas";

CREATE PROCEDURE 
-- 
--  Proposito: Eliminar 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 21/09/2016
--  Id Servicio = 12030
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.EliminarTipoCentralizacionesAsociadas" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	-- Parametros propos del Proceso
	IN p_IdTipoCentralizacion		SMALLINT		DEFAULT 0,
	IN p_IdTipoContabilidad			BIGINT			DEFAULT 0,
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           INT,   
	OUT p_out_ErroresDetalle            TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	-- FIN Sección Fija
	)
      LANGUAGE SQLSCRIPT
      SQL SECURITY INVOKER
      DEFAULT SCHEMA "BSCL"
      --READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo",'' AS "Descripcion" FROM DUMMY WHERE 1=2; 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
												SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 

		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		DECLARE v_Quantity	INT;
		DECLARE v_sysDate 	NVARCHAR(20);
		DECLARE v_DetalleLog TABLE ("SubRegistro" 	NVARCHAR(40),
									"CampoRegistro" NVARCHAR(40),
									"ValorAntiguo" 	NVARCHAR(250),
									"ValorNuevo" 	NVARCHAR(250)) DEFAULT 
											SELECT '' AS "SubRegistro", '' AS "CampoRegistro", '' AS "ValorAntiguo", '' AS "ValorNuevo" FROM DUMMY WHERE 1=2;

		v_Quantity :=0;

		SELECT COUNT(1)
		  INTO v_Quantity
	      FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo"
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad
		   AND "IdTipoCentralizacion" = :p_IdTipoCentralizacion;

		IF (:v_Quantity > 0) THEN
			SELECT CURRENT_DATE INTO v_sysDate FROM DUMMY;
		
			p_DetalleLog =  (SELECT :p_IdUsuario 			AS "SubRegistro",
									'IdTipoContabilidad'	AS "CampoRegistro",
									:p_IdTipoContabilidad	AS "ValorAntiguo",
									'' 						AS "ValorNuevo"
							   FROM DUMMY
							 UNION ALL
							 SELECT :p_IdUsuario 			AS "SubRegistro",
									'IdTipoCentralizacion'	AS "CampoRegistro",
									:p_IdTipoCentralizacion	AS "ValorAntiguo",
									'' 						AS "ValorNuevo"
							   FROM DUMMY);
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
																p_IdTipoLog 		=> 'ELIMINACION',
																p_IdModulo 			=> :p_IdModulo,
																p_IdUsuario 		=> :p_IdUsuario,
																p_IdEmpresa 		=> :p_IdEmpresa,
																p_FechaSistemaLog 	=> TO_VARCHAR(:v_sysDate, 'YYYY-MM-DD'),
																p_IdUsuarioAtendido => :p_IdUsuario,
																p_IdTipoRegistro	=> 'ITZ',
																p_DetalleLog 		=> :p_DetalleLog
															 );

			DELETE 
			  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo"
			 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad
			   AND "IdTipoCentralizacion" = :p_IdTipoCentralizacion;
		ELSE
			SIGNAL SALIDA_CONTROLADA_NO_DATA; --validar
		END IF;
	END;
	
	p_out_ResultadoProceso = 
		 SELECT TRIM(CAST(:p_IdTipoContabilidad AS NVARCHAR)) || ';' || TRIM(CAST(:p_IdTipoCentralizacion AS NVARCHAR)) AS "Campo",
				1 																										AS "Id",
				'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarTipoCentralizacionesAsociadas' 							AS "Servicio",
				'ELIMINADO' 																							AS "Resultado"
		   FROM DUMMY;
END;