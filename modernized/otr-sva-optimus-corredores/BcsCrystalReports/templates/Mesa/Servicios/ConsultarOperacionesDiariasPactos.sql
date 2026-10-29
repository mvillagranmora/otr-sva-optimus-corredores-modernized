SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactos"; 
GO
--
--  Proposito: servicio para consultar operaciones Diarias de Pactos
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 28/11/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactos" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_FechaDesde		NVARCHAR(10),
	IN p_FechaHasta		NVARCHAR(10),
	--IN p_Corredora	BIGINT,
	IN p_IdMesa			BIGINT,
	IN p_IdTipoProducto	NVARCHAR(4),
	IN p_OrdenarPor		NVARCHAR(40),
	IN p_IdEstado		NVARCHAR(4)
	
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
	
	-- se obtienen operaciones
	v_Operaciones = 
		SELECT
			--------------- DATOS OPERACIONES ---------------
			 OPE."IdOperacion"
			,OPE."FolioOperacion"
			,TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion"
			,OPE."IdTipoOperacion"
			,OPE."FechaVencimiento"
			,OPE."NombreOperacion"
			,OPE."IdCliente"
			,OPE."DocumentoIdentidad"
			,OPE."RazonSocial"
			,OPE."IdPortafolio"
			,OPE."CodigoPortafolio"
			,OPE."IdClienteCorredor"
			,OPE."IdContraparte"
			,OPE."RazonSocialCorredora"
			,OPE."Tasa"
			,OPE."CatIFRS"
			,OPE."IdTipoReajuste"
			,OPE."FechaLiquidacion"
			,OPE."IdAgente"
			,OPE."IdPersonaEjecutivo"
			,OPE."Agente"
			,OPE."IdSucursalEjecutivo"
			,OPE."Sucursal"
			,OPE."MontoOperacion"
			,OPE."MontoCompromiso"
			,OPE."IdEstado"
			,OPE."IdEmpresa"
			,OPE."IdMonedaLiquidacion"
			
			--------------- DATOS FILTROS ---------------
			,CASE 
    			WHEN TO_VARCHAR(:p_FechaDesde,'DD-MM-YYYY') = '01-01-0001' THEN ''
    			ELSE CONCAT('del ',TO_VARCHAR(:p_FechaDesde,'DD-MM-YYYY'))
			END AS "FiltroFechaOperacionDesde"
			,TO_VARCHAR(:p_FechaHasta,'DD-MM-YYYY') AS "FiltroFechaOperacionHasta"
			--,CASE WHEN p_Corredora = 0 THEN 'TODOS' ELSE OPE."RazonSocialCorredora" END AS "FiltroCorredora"
			,'' AS "FiltroCorredora"
			,CASE WHEN p_IdMesa = 0 THEN 'TODOS' ELSE OPE."NombreMesaOrigen" END AS "Mesa" 
			,CASE WHEN p_OrdenarPor = 'CLIENTE' THEN 'NOMBRE CLIENTE' ELSE 'NOMBRE AGENTE' END AS "OrdenarPor"
			
			--------------- DATOS GENERALES ---------------
			,v_CodigoUsuario AS "Usuario"
			,TO_NVARCHAR(v_FechaHoraSistemaLOCAL,'DD/MM/YYYY HH24:MI:SS') AS "FechaHoraSistema"
			,OPE."RelacionCorredora"
			,OPE."PlazoEnDias"
			,MF."TasaDecimales"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactos" OPE
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = OPE."IdTipoInstrumento"
			AND MF."IdMoneda" = OPE."IdMonedaLiquidacion"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdTipoOperacion" = ''
			LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarTablaCartera" 			CARHIS	
				ON (OPE."IdOperacion" = CARHIS."FolioComprobante" AND CARHIS."IdTipoInstrumento" = 'PAC')
		WHERE ((p_IdEstado = 'ANT' AND OPE."FechaAnticipo" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux AND OPE."Liquidado" = 'N')
	        OR (p_IdEstado = 'LIQ' AND OPE."FechaAnticipo" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux AND OPE."DateVencimiento" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux AND OPE."Liquidado" = 'S')
	        OR (p_IdEstado NOT IN ('ANT', 'LIQ') AND OPE."FechaOperacion" BETWEEN v_FechaDesdeAux AND v_FechaHastaAux))
		  AND (OPE."IdMesaOrigen" = p_IdMesa OR p_IdMesa = 0)
		  AND (OPE."IdTipoProducto" = p_IdTipoProducto OR p_IdTipoProducto = '')
		  AND (p_IdEstado = '0' OR OPE."IdEstado" = p_IdEstado)
		ORDER BY (CASE WHEN p_OrdenarPor = 'CLIENTE' THEN OPE."RazonSocial" ELSE OPE."Agente" END) ASC;
	
	
	v_saldos_tmp = SELECT 
		 "IdMonedaLiquidacion"
		,SUM(CASE WHEN "IdTipoOperacion" = 'COMPAC' THEN "MontoOperacion" ELSE 0 END) AS "TotalCOMPAC"
		,SUM(CASE WHEN "IdTipoOperacion" = 'COMTER' THEN "MontoOperacion" ELSE 0 END) AS "TotalCOMTER"
		,SUM(CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN "MontoOperacion" ELSE 0 END) AS "TotalVENPAC"
		,SUM(CASE WHEN "IdTipoOperacion" = 'VENTER' THEN "MontoOperacion" ELSE 0 END) AS "TotalVENTER"
		FROM :v_Operaciones
	GROUP BY "IdMonedaLiquidacion";
		
	SELECT COUNT(*) INTO v_count FROM :v_Operaciones;
	IF (v_count = 0) THEN
	v_Operacion_Fin=
	SELECT 
		 0 			  AS "IdOperacion"
		,0 			  AS "FolioOperacion"
		,'0001-01-01' AS "FechaOperacion"
		,''			  AS "IdTipoOperacion"
		,'0001-01-01' AS "FechaVencimiento"
		,''			  AS "NombreOperacion"
		,0 			  AS "IdCliente"
		,'' 		  AS "DocumentoIdentidad"
		,''           AS "RazonSocial"
		,0 			  AS "IdPortafolio"
		,'' 			  AS "CodigoPortafolio"
		,0 			  AS "IdClienteCorredor"
		,0 			  AS "IdContraparte"
		,''			  AS "RazonSocialCorredora"
		,0 			  AS "Tasa"
		,'' 	      AS "CatIFRS"
		,'' 		  AS "IdTipoReajuste"
		,'0001-01-01' AS "FechaLiquidacion"
		,0 			  AS "IdAgente"
		,0 			  AS "IdPersonaEjecutivo"
		,'' 		  AS "Agente"
		,0 			  AS "IdSucursalEjecutivo"
		,''  	      AS "Sucursal"
		,0 			  AS "MontoOperacion"
		,0 			  AS "MontoCompromiso"
		,'' 		  AS "IdEstado"
		,0 			  AS "IdEmpresa"
		,''			  AS "IdMonedaLiquidacion"
		
		--------------- DATOS FILTROS ---------------
		,'0001-01-01' AS "FiltroFechaOperacionDesde"
		,'0001-01-01' AS "FiltroFechaOperacionHasta"
		,''			  AS "FiltroCorredora"
		,'' 		  AS "Mesa" 
		,'' 		  AS "OrdenarPor"
			
		--------------- DATOS GENERALES ---------------
		,''			  AS "Usuario"
		,'0001-01-01' AS "FechaHoraSistema"
		,'' AS "RelacionCorredora"
		,0 AS "PlazoEnDias"
		,0 AS "TasaDecimales"
		--------------- TOTALES ---------------
		,0 AS "v_TotalCOMPAC"
		,0 AS "v_TotalCOMTER"
		,0 AS "v_TotalVENPAC"
		,0 AS "v_TotalVENTER"
		,0 AS "Saldo"
	FROM DUMMY;
	
	ELSE
	v_Operacion_Fin=
	SELECT DISTINCT
		  OPE.*
		 ,SL."TotalCOMPAC" AS "v_TotalCOMPAC"
		 ,SL."TotalCOMTER" AS "v_TotalCOMTER"
		 ,SL."TotalVENPAC" AS "v_TotalVENPAC"
		 ,SL."TotalVENTER" AS "v_TotalVENTER"
		 ,COALESCE(SL."TotalVENPAC",0) + COALESCE(SL."TotalVENTER",0) - COALESCE(SL."TotalCOMPAC",0) - COALESCE(SL."TotalCOMTER",0) AS "Saldo"
	FROM :v_Operaciones AS OPE
	INNER JOIN :v_saldos_tmp AS SL ON SL."IdMonedaLiquidacion" = OPE."IdMonedaLiquidacion";
	
	
	END IF;
	
	SELECT * FROM :v_Operacion_Fin
			CROSS JOIN :v_logo;
	
	
	
	--------------------
	--FIN SP 
	--------------------
END;
GO