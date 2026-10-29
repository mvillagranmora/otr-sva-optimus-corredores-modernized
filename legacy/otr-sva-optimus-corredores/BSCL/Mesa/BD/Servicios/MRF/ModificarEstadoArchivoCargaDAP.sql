SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.ModificarEstadoArchivoCargaDAP";

--
--  Proposito: Servicio para modificar el estado de Depositos.
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 25/07/2016
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ModificarEstadoArchivoCargaDAP" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	 		NVARCHAR(1)	,	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada del servicio privado
	IN p_IdEstado					NVARCHAR(3),
	IN p_Glosa						NVARCHAR(250),
	IN p_tablaDeposito				TABLE (	"IdCargaMasivaDepositos"	BIGINT,
											"FechaProceso"				NVARCHAR(10),
											"IdSecuencia"				BIGINT),
	
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
		DECLARE	v_countDepositos			BIGINT;
		DECLARE v_TipoDocumentoIdentidad	NVARCHAR(1);
		DECLARE v_Count			 			BIGINT:=0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- validacion existan registros
		SELECT COUNT(1) into v_Count
	    FROM :p_tablaDeposito TD
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR
	   		ON (TD."IdCargaMasivaDepositos"=CAR."IdCargaMasivaDepositos" AND TD."IdSecuencia"=CAR."IdSecuencia" AND TD."FechaProceso"=CAR."FechaProceso")
		WHERE CAR."IdEmpresa" = p_IdEmpresa;
		
		IF v_Count = 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTROS_NO_EXISTEN' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- validacion registros ya anulados
		SELECT COUNT(1) into v_Count
	    FROM :p_tablaDeposito TD
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR
	   		ON (TD."IdCargaMasivaDepositos"=CAR."IdCargaMasivaDepositos" AND TD."IdSecuencia"=CAR."IdSecuencia" AND TD."FechaProceso"=CAR."FechaProceso")
		WHERE CAR."IdEstado"	= p_IdEstado
		  AND CAR."IdEmpresa"	= p_IdEmpresa;
		
		IF v_Count > 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTROS_YA_MODIFICADOS' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- validacion cantidad registros con errores
		SELECT COUNT(1) into v_countDepositos
	    FROM :p_tablaDeposito TD;
		
		SELECT COUNT(1) into v_Count
	    FROM :p_tablaDeposito TD
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR
	   		ON (TD."IdCargaMasivaDepositos"=CAR."IdCargaMasivaDepositos" AND TD."IdSecuencia"=CAR."IdSecuencia" AND TD."FechaProceso"=CAR."FechaProceso")
		WHERE CAR."IdEstado"	= 'ERR'
		  AND CAR."IdEmpresa"	= p_IdEmpresa;
		
		IF v_countDepositos = v_Count THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTROS_CON_ERRORES' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		BEGIN
			UPDATE CAR
			SET CAR."IdEstado"	= p_IdEstado,
				CAR."Glosa"		= p_Glosa
			FROM "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR
			INNER JOIN :p_tablaDeposito TD
				ON (TD."IdCargaMasivaDepositos"=CAR."IdCargaMasivaDepositos" AND TD."IdSecuencia"=CAR."IdSecuencia" AND TD."FechaProceso"=CAR."FechaProceso")
			WHERE CAR."IdEstado"	= 'GRA'
			  AND CAR."IdEmpresa"	= p_IdEmpresa;
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;