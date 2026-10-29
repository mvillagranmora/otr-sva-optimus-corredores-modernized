SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito";

CREATE PROCEDURE
--
--  Proposito: Servicio para crear relacion laboral empleado y empleador(es) 
--  Autor: edelapeña - BCS
--  Fecha Creacion: 28/11/2015
--
"BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),  	--ID del Modulo que lo invoca
    -- Origen de datos del servicio
	IN  p_IdRecaudacion						BIGINT,

	OUT p_out_TotalesOrigen					TABLE ("Origen" NVARCHAR(100), "Total" DECIMAL(26,8)),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;

	/*DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador
			from :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;*/
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		
		p_out_TotalesOrigen =	SELECT 
									 ORI."Codigo" AS "Origen"
									,CASE WHEN COALESCE(SUM(CLI."MontoDepositoConvenido") + SUM(CLI."MontoDepositoAPV"), 0) = 0 THEN 0 ELSE SUM(CLI."MontoDepositoConvenido") + SUM(CLI."MontoDepositoAPV") END AS "Total" 
								FROM (SELECT * FROM "BSCL.APV.BD::REC.Cliente" WHERE "IdRecaudacion" = :p_IdRecaudacion) AS CLI
								INNER JOIN "BSCL.APV.BD::REC.Recaudacion" AS REC
								ON REC."IdRecaudacion" = CLI."IdRecaudacion"
								RIGHT JOIN "BSCL.APV.BD::REC.Origen" AS ORI
								ON ORI."Codigo" = REC."Origen"
								GROUP BY ORI."Codigo";	
	END;
END;