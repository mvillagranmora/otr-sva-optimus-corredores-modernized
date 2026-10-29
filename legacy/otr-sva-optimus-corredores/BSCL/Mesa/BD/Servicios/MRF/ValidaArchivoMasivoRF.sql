SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ValidaArchivoMasivoRF";

CREATE PROCEDURE
--  Proposito: crear operacion mesa RF
--  Autor: msandoval
--  Fecha Creacion: 04/07/2016
"BSCL.Mesa.BD.Servicios::MRF.ValidaArchivoMasivoRF" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca
	
	-- Parametros entrada del servicio privado
	IN p_NombreArchivo	NVARCHAR(100),
	IN p_DataArchivo	TABLE (	
								"FOLIO_OPERACION_CMO_RF" BIGINT,
								"COD_OPERACION_CMO_RF" NVARCHAR(6), 
								"RUT_CLIENTE_CMO_RF" NVARCHAR(11), 
								"SEC_CLIENTE_CMO_RF" BIGINT, 
								"TIP_REAJUSTE_CAB_CMO_RF" NVARCHAR(2),
								"IND_CUSTODIA_CMO_RF" NVARCHAR(2), 
								"IND_VIENEN_CMO_RF" NVARCHAR(2), 
								"COD_IFRS_CMO_RF" NVARCHAR(2), 
								"IND_LIQUIDACION_CMO_RF" NVARCHAR(2),
								"TIP_INGRESO_CMO_RF" NVARCHAR(2), 
								"COD_MERCADO_CMO_RF" NVARCHAR(3), 
								"NEMO_CMO_RF" NVARCHAR(40), 
								"EMISOR_CMO_RF" NVARCHAR(10), 
								"TIP_REAJUSTE_DET_CMO_RF" NVARCHAR(2), 
								"NRO_VECES_CMO_RF" DECIMAL(26,8),
								"CANTIDAD_CMO_RF" DECIMAL(26,8), 
								"FEC_VENCIMIENTO_CMO_RF" NVARCHAR(10),
								"MONTO_CMO_RF" DECIMAL(26,8),
								"MENSAJEVALIDACION" NVARCHAR(200)
							),		
							
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	int,
	OUT p_out_AdvertenciasDetalle	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador		int,
	OUT p_out_ErroresDetalle		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador	from :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE	v_FechaSistema		TIMESTAMP;
		DECLARE	v_countDepositos	BIGINT;
		DECLARE v_Count			 	BIGINT:=0;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
			(:p_IdEmpresa,v_FechaSistema);

			
		p_out_Registros = 
			SELECT	TO_VARCHAR(v_FechaSistema, 'DD-MM-YYYY') AS "FechaProceso"
					,0 AS "IdRegistroCarga"			
					,"FOLIO_OPERACION_CMO_RF" AS "FolioOperacion"
					,"IdOperacion" AS "IdSecuencia"
					,"COD_OPERACION_CMO_RF" AS "IdTipoOperacion"
					,"RUT_CLIENTE_CMO_RF" AS "RutCliente"
					,"SEC_CLIENTE_CMO_RF" AS "SecuenciaRut"

					,"TIP_REAJUSTE_CAB_CMO_RF" AS "IdTipoReajusteCabecera"
					,"IND_CUSTODIA_CMO_RF" AS "IdTipoCustodia"
					,"IND_VIENEN_CMO_RF" AS "IdFormaPago"
					,"COD_IFRS_CMO_RF" AS "IdIFRS"
					,"IND_LIQUIDACION_CMO_RF" AS "IdCondicionLiquidacion"
					,"TIP_INGRESO_CMO_RF" AS "IdIngreso"
					,"COD_MERCADO_CMO_RF" AS "IdClasificacionTipoInstrumento"
					,"NEMO_CMO_RF"  AS "Nemo"
					,"EMISOR_CMO_RF" AS "Emisor"
					,"TIP_REAJUSTE_DET_CMO_RF" AS "IdTipoReajusteDetalle"
					,"FEC_VENCIMIENTO_CMO_RF" AS "FechaVencimiento"
					,"NRO_VECES_CMO_RF" AS "Cantidad"
					,"CANTIDAD_CMO_RF" AS "MontoNominal"
					,"MONTO_CMO_RF" AS "MontoValorizado"					

					,CURRENT_DATE AS "FechaCarga"
					,p_NombreArchivo AS "ArchivoCarga"					
					,p_IdEmpresa AS "IdEmpresa"					
					
					,CASE WHEN TRIM("MENSAJEVALIDACION") <> 'OK' 
					      THEN 'ERR'
						  ELSE CASE WHEN COALESCE(OPE."FolioOperacion", 0) = 0 
						  	        THEN 'ACP' 
						  	   		ELSE 'ERR' END 
						  END AS "EstadoRegistro"
					,CASE WHEN TRIM("MENSAJEVALIDACION") <> 'OK' 
						  THEN "MENSAJEVALIDACION"
						  ELSE CASE WHEN COALESCE(OPE."FolioOperacion", 0) = 0 
							        THEN '' 
									ELSE 'UI_OPERACION_YA_EXISTE' 
									END 
						  END AS "Glosa"					
			FROM	:p_DataArchivo AS TMP
					LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
					ON "FOLIO_OPERACION_CMO_RF" = "FolioOperacion" AND OPE."IdTipoProducto" = 'MERF';
			

		---------- Consulta Depositos de Archivo ----------
		SELECT	*
		FROM	:p_out_Registros;
			
		--------------------
		--FIN SP 
		--------------------

	END;
END;