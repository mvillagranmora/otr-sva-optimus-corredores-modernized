SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientoTipo";

CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 21/09/2016
--  Id Servicio = 12020
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientoTipo" (
	IN p_IdTipoCentralizacion			SMALLINT		DEFAULT 0,	                          
	IN p_Nombre							NVARCHAR(60)	DEFAULT '',
	IN p_IdTipoMovimiento				NVARCHAR(10)	DEFAULT '',
	IN p_IdEmpresa						INT				DEFAULT 0,
	IN p_Estado							NVARCHAR(1)		DEFAULT 'V',
	-- Parametros salida del servicio publico
	OUT p_out_TabEventoAsientoAsociado TABLE 	(
												"Nombre"					NVARCHAR(60),
												"IdTipoMovimiento"			NVARCHAR(10)
												),
	-- Sección Fija ----
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           int,   
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2; 	
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
		p_out_TabEventoAsientoAsociado = 
			 SELECT ctb_itzMovimientoTipo."Nombre" 				AS "Nombre", 
					ctb_itzMovimientoTipo."IdTipoMovimiento" 	AS "IdTipoMovimiento" 
			   FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS ctb_itzCentralizacionTipo
			   			INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo" AS ctb_itzMovimientoTipoCentralizacionTipo ON 
			   					ctb_itzMovimientoTipoCentralizacionTipo."IdTipoCentralizacion" = ctb_itzCentralizacionTipo."IdTipoCentralizacion" 
			   			INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS ctb_itzMovimientoTipo ON 
			   					ctb_itzMovimientoTipo."IdTipoMovimiento" = ctb_itzMovimientoTipoCentralizacionTipo."IdTipoMovimiento" 
			   				AND ctb_itzMovimientoTipo."Estado" = 'V' 
			  WHERE (ctb_itzCentralizacionTipo."IdTipoCentralizacion" 	= :p_IdTipoCentralizacion 	OR :p_IdTipoCentralizacion 	= 0) 
			    AND (ctb_itzMovimientoTipo."Nombre" 					= :p_Nombre 				OR :p_Nombre 				= '')
			    AND (ctb_itzMovimientoTipo."IdTipoMovimiento" 			= :p_IdTipoMovimiento 		OR :p_IdTipoMovimiento 		= '') 
			    AND (ctb_itzCentralizacionTipo."IdEmpresa" 				= :p_IdEmpresa 				OR :p_IdEmpresa 			= 0) 
			    AND (ctb_itzMovimientoTipo."Estado" 					= :p_Estado 				OR :p_Estado 				= '');
	END;
END;