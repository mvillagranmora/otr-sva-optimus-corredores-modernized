SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactosDetalles"; 
GO
--
--  Proposito: servicio para consultar operaciones Diarias de Pactos Detalles
--  Autor: FGUZMAN - BCS
--  Fecha Creacion: 14/05/2019
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactosDetalles" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_FechaDesde		NVARCHAR(10),
	IN p_FechaHasta		NVARCHAR(10)
	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--Variables
	--------------------
	
	DECLARE v_CodigoUsuario		NVARCHAR(100);
	DECLARE v_FechaHoraSistema	SECONDDATE;
	DECLARE v_count INT;
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	
	DECLARE v_FechaDesdeAux TIMESTAMP := TO_TIMESTAMP(p_FechaDesde||' 00:00:00','YYYY-MM-DD HH24:MI:SS');
	DECLARE v_FechaHastaAux TIMESTAMP := TO_TIMESTAMP(p_FechaHasta||' 23:59:59','YYYY-MM-DD HH24:MI:SS');
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	--------------------
	--Lógica de Negocio 
	--------------------
	/** Obtener Logo  **/
		v_logo =	SELECT TOP 1
						"Binario" AS "LogoCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;	
	-- se obtiene Usuario
	SELECT "CodigoUsuario" INTO v_CodigoUsuario
	FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"
	WHERE "IdUsuario" = p_IdUsuario;
	
	-- se obtiene fecha y hora de sistema
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaHoraSistema);
	
	-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	
	v_Operaciones = 
	SELECT
		--------------- DATOS OPERACIONES ---------------
		 CODPD."FolioOperacion"
		,TO_VARCHAR(CODPD."FolioCartera") AS "FolioCartera"
		,CODPD."Nemotecnico"
		,CODPD."IdEmisor"
		,CODPD."Mercado" AS "Mercado"
		,CODPD."IndicadorCobertura"
		,CODPD."IdTipoReajuste"
		,CODPD."Tasa" AS "Tasa"
		,TO_NVARCHAR(CODPD."FechaVencimientoInstrumento",'DD-MM-YYYY') AS "FechaVencimientoInstrumentoCadena"
		,CODPD."Cantidad"
		,CASE 
        	WHEN CODPD."IdTipoReajuste" = 'CLP' THEN ROUND(CODPD."MontoSubyacente", 0)
        	ELSE CODPD."MontoSubyacente"
    	END AS "MontoSubyacente"
	    ,CASE 
	        WHEN CODPD."IdTipoReajuste" = 'CLP' THEN ROUND(CODPD."MontoCompromisoDetalle", 0)
	        ELSE CODPD."MontoCompromisoDetalle"
    	END AS "MontoCompromisoDetalle"				
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactosDetalle" AS CODPD
	WHERE CODPD."FechaOperacion" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux;
	
		 
		
	SELECT COUNT(*) INTO v_count FROM :v_Operaciones;
	IF (v_count = 0) THEN
		v_Operacion_Fin=
			SELECT 
				0  			 	AS "FolioOperacion"
				,''				AS "FolioCartera"
				,'' 			AS "Nemotecnico"
				,'' 			AS "IdEmisor"
				,'' 			AS "Mercado"
				,0 				AS "IndicadorCobertura"
				,'' 			AS "IdTipoReajuste"
				,0				AS "Tasa"
				,'01-01-0001' 	AS "FechaVencimientoInstrumentoCadena"
				,0				AS "Cantidad"
				,0				AS "MontoSubyacente"
				,0				AS "MontoCompromisoDetalle"
			FROM DUMMY;
	
	ELSE
		v_Operacion_Fin=
			SELECT
		 	 	OPE.*
			FROM :v_Operaciones AS OPE;

	END IF;
	
	SELECT * FROM :v_Operacion_Fin;

	
	
	
	--------------------
	--FIN SP 
	--------------------
END;
GO
