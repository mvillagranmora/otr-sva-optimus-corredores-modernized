SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarCartaLiquidacion";

CREATE PROCEDURE
--  Proposito: Crear Carta Liquidacion Aporte Rescate .
--  Autor: Gat-Blac 
--  Fecha Creacion: 05/09/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarCartaLiquidacion" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
	IN p_IdEmisor								NVARCHAR(40),
	IN p_FechaMovimiento						NVARCHAR(10),
	IN p_FechaPago								NVARCHAR(10),
	IN p_SerieFondo								NVARCHAR(40),
	IN p_Estado									NVARCHAR(3),
	IN p_TipoSolicitud							NVARCHAR(3)
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	BEGIN
		
		BEGIN
	    DECLARE v_MensajeError NVARCHAR(100) := '';
	    DECLARE v_countOperaciones 	BIGINT := 0;
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_DatosCorredora TABLE( "IdPersona" 			BIGINT,
										"DocumentoIdentidad" 	NVARCHAR(20),
										"RazonSocial" 			NVARCHAR(100),
										"DireccionFisica" 		NVARCHAR(200),
										"Telefono" 				NVARCHAR(40)
										); 
		
		v_logo =SELECT TOP 1 "Binario" AS "LogoCorredora"
				FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
				WHERE "NombreImagen" = 'LOGO_CORREDOR' 
				AND "IdEmpresa" = p_IdEmpresa 
				ORDER BY "Version" DESC;
		
				
		v_DatosCorredora = SELECT Persona."IdPersona"
							     , Persona."DocumentoIdentidad"
							     , Persona."RazonSocial"
							     , COALESCE(DireccionFisica."Calle",'')	|| ' ' || COALESCE(DireccionFisica."Numero",'')				AS "DireccionFisica"
							     , COALESCE(Telefono."Numero",'') 						AS "Telefono"
							FROM "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa
							INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocioNegocio
									ON SocioNegocio."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Persona
									ON Persona."IdPersona" = SocioNegocio."IdPersona"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DireccionFisica 
								   ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS Telefono 
							       ON Telefono."IdTelefono" = Persona."IdTelefonoPrincipal";  		
				
		v_resultado = 	SELECT CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoAporte"
							   	, CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoRescate"
							   	, "SolicitudAporteRescate"."IdSerieFondo"
							   	, "Fondo"."IdMoneda"
							   	, "SolicitudAporteRescate"."FolioCartaLiquidacion"
							   	, "Cliente"."DocumentoIdentidad"
							   	, "Portafolio"."CodigoPortafolio"
							   	, "ClienteSolicitud"."DocumentoIdentidad" AS "DocumentoIdentidadClienteSolicitud"
							   	, "Fondo"."IdEmisor"
							   	, TO_CHAR("SolicitudAporteRescate"."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
							   	, "SolicitudAporteRescate"."IdEmpresa"
							   	, CASE WHEN "TipoSolicitud" = 'APO' THEN 'aportes' ELSE 'rescates' END AS "TipoOperacion"
								FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
								INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ClienteSolicitud" ON ("ClienteSolicitud"."IdCliente" = "SolicitudAporteRescate"."IdCliente")
								WHERE "EstadoLiquidacion" = 'LIQ'
									   AND (:p_IdEmisor = '' 		OR "Fondo"."IdEmisor" = :p_IdEmisor)
									   AND "SolicitudAporteRescate"."FechaCertificacion" = :p_FechaMovimiento
									   AND "SolicitudAporteRescate"."FechaLiquidacion" = :p_FechaPago
									   AND (:p_SerieFondo = '' 		OR "SolicitudAporteRescate"."IdSerieFondo" = :p_SerieFondo)
									   AND "SolicitudAporteRescate"."Estado" = :p_Estado
									   AND "SolicitudAporteRescate"."TipoSolicitud" = :p_TipoSolicitud
									   AND "SerieFondo"."Omnibus" = 'N'
									   AND "SolicitudAporteRescate"."IdEmpresa" = :p_IdEmpresa
									   AND "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER')
						UNION ALL
						SELECT 
								CAST(SUM(CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS DECIMAL(26,8)) AS "MontoAporte"
								,CAST(SUM(CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS DECIMAL(26,8)) AS "MontoRescate"
								, "SolicitudAporteRescate"."IdSerieFondo"
								, "Fondo"."IdMoneda"
								, "SolicitudAporteRescate"."FolioCartaLiquidacion"
								, "Cliente"."DocumentoIdentidad"
								, "Portafolio"."CodigoPortafolio"
								, '' AS "DocumentoIdentidadClienteSolicitud"
								, "Fondo"."IdEmisor"
								, TO_CHAR("SolicitudAporteRescate"."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
								, "SolicitudAporteRescate"."IdEmpresa"
								, CASE WHEN "TipoSolicitud" = 'APO' THEN 'aportes' ELSE 'rescates' END AS "TipoOperacion"
								FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
								INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
								WHERE "EstadoLiquidacion" = 'LIQ'
									   AND (:p_IdEmisor = '' 		OR "Fondo"."IdEmisor" = :p_IdEmisor)
									   AND "SolicitudAporteRescate"."FechaCertificacion" = :p_FechaMovimiento
									   AND "SolicitudAporteRescate"."FechaLiquidacion" = :p_FechaPago
									   AND (:p_SerieFondo = '' 		OR "SolicitudAporteRescate"."IdSerieFondo" = :p_SerieFondo)
									   AND "SolicitudAporteRescate"."Estado" = :p_Estado
									   AND "SolicitudAporteRescate"."TipoSolicitud" = :p_TipoSolicitud
									   AND "SerieFondo"."Omnibus" = 'S'
									   AND "SolicitudAporteRescate"."IdEmpresa" = :p_IdEmpresa
									   AND "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER')
								GROUP BY "SolicitudAporteRescate"."IdSerieFondo","Fondo"."IdMoneda","SolicitudAporteRescate"."FolioCartaLiquidacion","Cliente"."DocumentoIdentidad","Portafolio"."CodigoPortafolio","Fondo"."IdEmisor","SolicitudAporteRescate"."FechaLiquidacion","SolicitudAporteRescate"."IdEmpresa","SolicitudAporteRescate"."TipoSolicitud"
								ORDER BY "FolioCartaLiquidacion";
								
	   SELECT COUNT(1) INTO v_countOperaciones
	   FROM :v_resultado;
	   
	   IF (v_countOperaciones = 0) THEN
	   v_MensajeError := 'NO_EXISTEN_OPERACIONES';
	   END IF;
	   
	   IF (v_countOperaciones > 0) THEN
	   SELECT DAT."MontoAporte",
			  DAT."MontoRescate",
			  DAT."IdSerieFondo",
			  DAT."IdMoneda",
			  DAT."FolioCartaLiquidacion",
			  DAT."DocumentoIdentidad",
			  DAT."CodigoPortafolio",
			  DAT."IdEmisor",
			  DAT."FechaLiquidacion",
			  DAT."IdEmpresa",
			  DAT."TipoOperacion",
			  IMG."LogoCorredora",
			  TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY') AS "FechaSistema",
			  CORR."IdPersona",
			  CORR."DocumentoIdentidad" AS "DocumentoIdentidadCorredora",
			  CORR."RazonSocial",
			  CORR."DireccionFisica",
			  CORR."Telefono",
			  '' AS "MensajeError"
		FROM :v_resultado AS DAT
		CROSS JOIN :v_logo AS IMG
		CROSS JOIN :v_DatosCorredora AS CORR;
		
		ELSE
		
		SELECT 
			  '' AS "MontoAporte",
			  '' AS "MontoRescate",
			  '' AS "IdSerieFondo",
			  '' AS "IdMoneda",
			  '' AS "FolioCartaLiquidacion",
			  '' AS "DocumentoIdentidad",
			  '' AS "CodigoPortafolio",
			  '' AS "IdEmisor",
			  '' AS "FechaLiquidacion",
			  '' AS "IdEmpresa",
			  '' AS "TipoOperacion",
			  IMG."LogoCorredora",
			  TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY') AS "FechaSistema",
			  CORR."IdPersona",
			  CORR."DocumentoIdentidad" AS "DocumentoIdentidadCorredora",
			  CORR."RazonSocial",
			  CORR."DireccionFisica",
			  CORR."Telefono",
			  v_MensajeError AS "MensajeError"
		FROM :v_logo AS IMG
		CROSS JOIN :v_DatosCorredora AS CORR;
		
		END IF;
		
		END; 
	END;
END;
		
