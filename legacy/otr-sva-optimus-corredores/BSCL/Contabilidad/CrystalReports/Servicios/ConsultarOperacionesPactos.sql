--
--  Proposito: servicio para consultar operaciones de pactos
--  Autor: NLOPEZ - Chainlabs
--  Fecha Creacion: 01/07/2025
--
SET SCHEMA "BSCL";
go
DROP PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::ConsultarOperacionesPactos";
go
CREATE PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::ConsultarOperacionesPactos" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_Fecha		NVARCHAR(10),
	IN p_IdMesa			BIGINT	
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
	DECLARE v_FechaSistemaUTC    NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL       NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaSistema       SECONDDATE;
	DECLARE v_FechaHoraUTC     NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL     NVARCHAR(40);
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	
	-- recupero fecha sistema
  	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> 1,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaSistema,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	
	--------------------
	--Lógica de Negocio 
	--------------------
	/** Obtener Logo  **/
	v_logo =	
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;	

	-- se obtiene datos corredora
	v_DatosCorredora =
		SELECT COR1."RazonSocial" 
		FROM  "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" COR1 
	    WHERE COR1."IdEmpresa" = :p_IdEmpresa;

	-- se obtienen operaciones
	v_Operaciones = 
	SELECT
    --------------- DATOS OPERACIONES ---------------
     OPE."IdOperacion"
	,PORT."CodigoPortafolio" AS "Portafolio"
    ,OPE."FolioOperacion" AS "Folio"
    ,TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion"
    ,OPE."FechaVencimiento"
    ,OPE."DocumentoIdentidad"
    ,OPE."RazonSocial" AS "Nombre"
    ,RES."Tasa" as "TasaTitulo"
    ,OPE."Tasa" as "TasaPacto"
	,((CASE WHEN OPE."IdMoneda" <> 'CLP' THEN RES."INGMontoAPlazo" ELSE RES."INGMontoAPlazoMonedaInstrumento" END ) - RES."MontoValorizado") AS "Diferencia"
    ,SUB."Cantidad"
    ,RES."MontoValorizado"  as "MontoTitulo"
    ,CASE WHEN OPE."IdMoneda" <> 'CLP' THEN RES."INGMontoAPlazo" ELSE RES."INGMontoAPlazoMonedaInstrumento" END AS "MontoPacto"
    ,RES."IdTipoReajuste"
    ,TIPOPE."Nombre" AS "IdTipoOperacion"
    ,SUB."NemoSVS" AS "Nemotecnico"
    ,SUB."IdEmisor"
	, CASE WHEN TES."Nombre" IS NOT NULL THEN  REPLACE(TES."Nombre", '(Centralizacion) ', '')
          ELSE  'Otras Empresas'
      END AS "Familia"
    ,RES."Nemotecnico" AS "Titulo"
    ,RES."Nemotecnico" AS "NemoSVS" 
    --------------- DATOS FILTROS ---------------
    ,TO_VARCHAR(:p_Fecha,'DD-MM-YYYY') as "FechaConsulta"
    ,TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI')	AS "HoraImpresion"
    ,TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
    ,CORD."RazonSocial" AS "Corredora"    
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES 	ON OPE."IdOperacion" = RES."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB 	ON RES."FolioSubyacente" = SUB."FolioSubyacente"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPOPE	ON TIPOPE."IdTipoOperacion" = OPE."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT	ON PORT."IdPortafolio" = OPE."IdPortafolio"
LEFT JOIN (SELECT "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico" 
		   FROM "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" GROUP BY "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico"   ) as TEM 
		   ON TEM."IdTipoInstrumento" = RES."IdTipoInstrumento"
		   AND TEM."Nemotecnico"=CASE  WHEN RES."IdClasificacionTipoInstrumento"  = 'IF' AND RES."Nemotecnico" Is NOT null 
		                               THEN RES."Nemotecnico" ELSE '' END
LEFT JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" AS TES ON TES."Codigo" = TEM."CodigoTipoEmisor"
CROSS JOIN :v_DatosCorredora AS CORD
WHERE 
    OPE."IdEstado" IN ('ANT','LIQ','VIG') 
    AND OPE."FechaOperacion" = :p_Fecha
    AND (
        EXISTS (
            SELECT 1 FROM "BSCL.Custodia.BD::CUS.Cartera" CAR2
            WHERE CAR2."IdOperacion" = OPE."IdOperacion" AND CAR2."IdTipoInstrumento" = 'PAC')
        OR
        EXISTS (
            SELECT 1 FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CARHIS2
            WHERE CARHIS2."IdOperacion" = OPE."IdOperacion" AND CARHIS2."IdTipoInstrumento" IN ('PAC'))
    )
    AND (OPE."IdMesaOrigen" = :p_IdMesa)
ORDER BY OPE."FolioOperacion" ASC;

SELECT COUNT(*) INTO v_count FROM :v_Operaciones;
	IF (v_count = 0) THEN
	v_Operaciones=
		SELECT 
		 0 AS "IdOperacion"
		,'' AS "Portafolio"
    	,0 AS "Folio"
    	,'0001-01-01' AS "FechaOperacion"
    	,'0001-01-01' AS "FechaVencimiento"
    	,'' AS  "DocumentoIdentidad"
    	,'' AS "Nombre"
    	,0 as "TasaTitulo"
    	,0 as "TasaPacto"
    	,0 AS "Diferencia"
    	,0 AS "Cantidad"
    	,0 as "MontoTitulo"
    	,0 AS "MontoPacto"
    	,'' AS "IdTipoReajuste"
    	,'' AS "IdTipoOperacion"
    	,'' AS "Nemotecnico"
    	,'' AS "IdEmisor"
    	,'' AS "Familia"
    	,'' AS "Titulo"
        ,'' AS "NemoSVS"

    --------------- DATOS FILTROS ---------------
    	,TO_VARCHAR(:p_Fecha,'DD-MM-YYYY') as "FechaConsulta"
    	,TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI')	AS "HoraImpresion"
        ,TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
        ,CORD."RazonSocial" AS "Corredora"
	FROM DUMMY
    CROSS JOIN :v_DatosCorredora AS CORD;
	END IF;
	
SELECT * FROM :v_Operaciones
	CROSS JOIN :v_logo;
	
	
	
	--------------------
	--FIN SP 
	--------------------
END;
