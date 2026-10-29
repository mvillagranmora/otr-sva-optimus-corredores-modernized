SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.GrabarCargaMasivaOperacionesRF";

--
--  Proposito : Servicio para grabar Operaciones RF que fueron cargados desde archivo.
--  Autor ....: MSANDOVAL - BCS
--  Fecha ....: 31-12-2016+
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.GrabarCargaMasivaOperacionesRF" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada del servicio privado
	IN p_RegistrosGrabar	TABLE (	
								"FechaProceso"				NVARCHAR(10)	
								,"FolioOperacion"			BIGINT			
								,"IdTipoOperacion" 			NVARCHAR(6) 	
								,"RutCliente"				NVARCHAR(11)	
								,"SecuenciaRut"				BIGINT			
								,"IdTipoReajusteCabecera"	NVARCHAR(2)		
								,"IdTipoCustodia"			NVARCHAR(2)		
								,"IdFormaPago"				NVARCHAR(2)		
								,"IdIFRS"					NVARCHAR(2)		
								,"IdCondicionLiquidacion"	NVARCHAR(2)		
								,"IdIngreso"				NVARCHAR(2)		
								,"IdClasificacionTipoInstrumento" NVARCHAR(3)
								,"Nemo" 					NVARCHAR(40)	
								,"Emisor"					NVARCHAR(10)	
								,"IdTipoReajusteDetalle"	NVARCHAR(2)		
								,"FechaVencimiento"			DATE 			
								,"Cantidad" 				DECIMAL(26,8)	
								,"MontoNominal"				DECIMAL(26,8)	
								,"MontoValorizado"			DECIMAL(26,8)	
								,"FechaCarga"  				TIMESTAMP
								,"IdEstadoCarga"			NVARCHAR(3)		
								,"Glosa"					NVARCHAR(250)	
								,"ArchivoCarga"				NVARCHAR(250)	
								,"IdEmpresa"				BIGINT			
								),
								
	OUT p_out_CantidadRegistros		BIGINT,
	OUT p_out_RegistrosCargadosOK	BIGINT,
	OUT p_out_RegistrosCargadosNOK	BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	BIGINT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador		BIGINT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
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
		DECLARE	v_FechaSistemaCreacion		TIMESTAMP;
		DECLARE v_Count			 			BIGINT:=0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		p_out_CantidadRegistros		:=0;
		p_out_RegistrosCargadosOK	:=0;
		p_out_RegistrosCargadosNOK	:=0;
		
		--------------------
		--Validaciones
		--------------------

		-- Viene un folio de operacion repetido???
		-- => pa fuera!!!
		
		-- El registro que esta grabando YA esta Grabado ??? 
		v_Tbl_RegYaExiste = 
			SELECT 	DISTINCT RG."FolioOperacion" 
		    FROM 	:p_RegistrosGrabar RG
		   			INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaOperaciones" CAR 
		   			ON 	RG."FolioOperacion"= CAR."FolioOperacion" 
		   				AND RG."FechaProceso"= CAR."FechaProceso" 
			WHERE 	CAR."IdEmpresa" = p_IdEmpresa; 

		SELECT	COUNT(1)
		INTO	v_Count
		FROM	:v_Tbl_RegYaExiste;
		
		IF v_Count > 0 THEN
			p_out_ErroresDetalle = 
				SELECT 'UI_REGISTRO_YA_EXISTE' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM DUMMY;
		END IF;
		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		BEGIN
			-- Chequea clientes solo de los registros pendientes por grabar...
			v_ClienteOk =
				SELECT
						"FolioOperacion",
						CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'ERR' ELSE RG."IdEstadoCarga" END AS "IdEstadoCarga",
						CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'UI_CLIENTE_NO_REGISTRADO' ELSE RG."Glosa" END AS "Glosa",
						CLI."DocumentoIdentidad"
				FROM 	:p_RegistrosGrabar RG
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
						ON (RG."RutCliente"=CLI."DocumentoIdentidad" AND CLI."IdEmpresa"=p_IdEmpresa AND CLI."IdEstado"='ACT')
						LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
						ON (POR."IdCliente" = CLI."IdCliente" AND POR."CodigoPortafolio" = CAST(RG."SecuenciaRut" AS NVARCHAR(100)));
				
			v_RegistrosGrabar =
				SELECT
						RG."FolioOperacion"
						,CASE WHEN VC."IdEstadoCarga"='ERR' THEN 'ERR' 
							  WHEN COALESCE(RGEXST."FolioOperacion", 0) > 0 THEN 'ERR'
							  WHEN RG."IdEstadoCarga" <> 'ACP' THEN RG."IdEstadoCarga"
							  ELSE 'GRA' 
						END AS "IdEstadoCarga"
						,CASE WHEN VC."IdEstadoCarga"='ERR' THEN VC."Glosa"
							  WHEN COALESCE(RGEXST."FolioOperacion", 0) > 0 THEN 'UI_REGISTRO_YA_EXISTE'
							  WHEN RG."IdEstadoCarga" <> 'ACP' THEN RG."Glosa"						  
							  ELSE 'UI_REGISTRO_GRABADO_EN_SISTEMA'
						END AS "Glosa"
				FROM 	:p_RegistrosGrabar RG
						INNER JOIN :v_ClienteOk VC ON RG."FolioOperacion" = VC."FolioOperacion"
						LEFT JOIN :v_Tbl_RegYaExiste RGEXST ON RG."FolioOperacion" = RGEXST."FolioOperacion";

			
			INSERT INTO "BSCL.Mesa.BD::MES.CargaMasivaOperaciones"(
					"IdRegistroCarga"
					,"FechaProceso"
					,"FolioOperacion"
					,"IdTipoOperacion"
					,"RutCliente"
					,"SecuenciaRut"
					,"IdTipoReajusteCabecera"
					,"IdTipoCustodia"
					,"IdFormaPago"
					,"IdIFRS"
					,"IdCondicionLiquidacion"
					,"IdIngreso"
					,"IdClasificacionTipoInstrumento"
					,"Nemo"
					,"Emisor"
					,"IdTipoReajusteDetalle"
					,"FechaVencimiento"
					,"Cantidad"
					,"MontoNominal"
					,"MontoValorizado"
					,"FechaCarga"
					,"IdEstadoCarga"
					,"Glosa"
					,"ArchivoCarga"
					,"IdEmpresa" )
			SELECT
					"BSCL.Mesa.BD.Secuencias::MRF.IdCargaMasivaOperaciones".NEXTVAL
					,RG."FechaProceso"
					,RG."FolioOperacion"
					,RG."IdTipoOperacion"
					,RG."RutCliente"
					,RG."SecuenciaRut"
					,RG."IdTipoReajusteCabecera"
					,RG."IdTipoCustodia"
					,RG."IdFormaPago"
					,RG."IdIFRS"
					,RG."IdCondicionLiquidacion"
					,RG."IdIngreso"
					,RG."IdClasificacionTipoInstrumento"
					,RG."Nemo"
					,RG."Emisor"
					,RG."IdTipoReajusteDetalle"
					,RG."FechaVencimiento"
					,RG."Cantidad"
					,RG."MontoNominal"
					,RG."MontoValorizado"
					,RG."FechaCarga"
					,TMP."IdEstadoCarga"
					,TMP."Glosa"
					,RG."ArchivoCarga"
					,RG."IdEmpresa" 
			FROM 	:p_RegistrosGrabar RG
					INNER JOIN :v_RegistrosGrabar TMP ON RG."FolioOperacion" = TMP."FolioOperacion";
			
			SELECT COUNT(1) INTO p_out_CantidadRegistros FROM :v_RegistrosGrabar RG;
			SELECT COUNT(1) INTO p_out_RegistrosCargadosOK FROM :v_RegistrosGrabar RG WHERE "IdEstadoCarga"='GRA';
			SELECT COUNT(1) INTO p_out_RegistrosCargadosNOK FROM :v_RegistrosGrabar RG WHERE "IdEstadoCarga" <> 'ERR';
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;