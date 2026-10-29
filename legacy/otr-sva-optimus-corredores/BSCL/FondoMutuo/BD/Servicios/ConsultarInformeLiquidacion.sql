SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarInformeLiquidacion";

CREATE PROCEDURE
--  Proposito: Crear Informe Liquidacion Fondos Mutuos.
--  Autor: Gat-Blac 
--  Fecha Creacion: 05/09/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarInformeLiquidacion" (
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
	    
		v_resultado = 	SELECT DISTINCT
						CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoAporte"
						, CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoRescate"
						, "SolicitudAporteRescate"."IdSerieFondo"
						, "Fondo"."IdMoneda"
						, "ClienteSolicitud"."DocumentoIdentidad" AS "DocumentoIdentidadClienteSolicitud"
						, "Fondo"."IdEmisor"
						, "Cliente"."DocumentoIdentidad"
						, "Portafolio"."CodigoPortafolio"
						, "SolicitudAporteRescate"."IdEmpresa"
						, "Estado"."Nombre" AS "NombreEstado"
						, CASE WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'CLP' THEN 'SOAP'
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'USD' THEN 'SOAD' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'EUR' THEN 'SOAX' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'CLP' THEN 'SORE' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'USD' THEN 'SORD' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'EUR' THEN 'SORX' ELSE '' END AS "TipoMovimiento" 
						FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ClienteSolicitud" ON ("ClienteSolicitud"."IdCliente" = "SolicitudAporteRescate"."IdCliente")
						INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor" ON ("Cliente"."IdPersona" = "Emisor"."IdPersona")
						LEFT  JOIN "BSCL.Plataforma.BD::PLA.Estado" "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
						WHERE "EstadoLiquidacion" = 'LIQ' 
						AND (COALESCE(:p_FechaMovimiento,'') = '' OR "SolicitudAporteRescate"."FechaCertificacion" = :p_FechaMovimiento)
						AND (COALESCE(:p_FechaPago,'') = '' OR "SolicitudAporteRescate"."FechaLiquidacion" = :p_FechaPago)
						AND (COALESCE(:p_SerieFondo,'') = '' OR "SolicitudAporteRescate"."IdSerieFondo" = :p_SerieFondo)
						AND (COALESCE(:p_Estado,'') = '' OR "SolicitudAporteRescate"."Estado" = :p_Estado)
						AND (COALESCE(:p_IdEmisor,'') = '' OR "Fondo"."IdEmisor" = :p_IdEmisor)
						AND "SerieFondo"."Omnibus" = 'N'
						AND "SolicitudAporteRescate"."TipoSolicitud" = :p_TipoSolicitud
						AND "Emisor"."IndicadorAGF" = 'S'
						AND "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER')
						UNION ALL
						SELECT 
						SUM(CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS "MontoAporte"
						,SUM(CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS "MontoRescate"
						, "SolicitudAporteRescate"."IdSerieFondo"
						, "Fondo"."IdMoneda"
						, '' AS "DocumentoIdentidadClienteSolicitud"
						, "Fondo"."IdEmisor"
						, "Cliente"."DocumentoIdentidad"
						, "Portafolio"."CodigoPortafolio"
						, "SolicitudAporteRescate"."IdEmpresa"
						, "Estado"."Nombre" AS "NombreEstado"
						, CASE WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'CLP' THEN 'SOAP'
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'USD' THEN 'SOAD' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO' AND "SolicitudAporteRescate"."IdMoneda" = 'EUR' THEN 'SOAX' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'CLP' THEN 'SORE' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'USD' THEN 'SORD' 
							   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES' AND "SolicitudAporteRescate"."IdMoneda" = 'EUR' THEN 'SORX' ELSE '' END AS "TipoMovimiento" 
						FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
						INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor" ON ("Cliente"."IdPersona" = "Emisor"."IdPersona") 
						LEFT  JOIN "BSCL.Plataforma.BD::PLA.Estado" "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
						WHERE "SolicitudAporteRescate"."EstadoLiquidacion" = 'LIQ' 
						AND "SerieFondo"."Omnibus" = 'S'
						AND "SolicitudAporteRescate"."TipoSolicitud" = :p_TipoSolicitud
						AND "Emisor"."IndicadorAGF" = 'S'
						AND (COALESCE(:p_FechaMovimiento,'') = '' OR "SolicitudAporteRescate"."FechaCertificacion" = :p_FechaMovimiento)
						AND (COALESCE(:p_FechaPago,'') = '' OR "SolicitudAporteRescate"."FechaLiquidacion" = :p_FechaPago)
						AND (COALESCE(:p_SerieFondo,'') = '' OR "SolicitudAporteRescate"."IdSerieFondo" = :p_SerieFondo)
						AND (COALESCE(:p_Estado,'') = '' OR "SolicitudAporteRescate"."Estado" = :p_Estado)
						AND (COALESCE(:p_IdEmisor,'') = '' OR "Fondo"."IdEmisor" = :p_IdEmisor)
						AND "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER')
						GROUP BY "SolicitudAporteRescate"."IdSerieFondo","Fondo"."IdMoneda","Fondo"."IdEmisor", "Cliente"."DocumentoIdentidad",
						         "SolicitudAporteRescate"."IdEmpresa", "SolicitudAporteRescate"."TipoSolicitud", "SolicitudAporteRescate"."IdMoneda",
						         "Portafolio"."CodigoPortafolio","Estado"."Nombre"
						ORDER BY "SolicitudAporteRescate"."IdSerieFondo" asc;
						
		SELECT COUNT(1) INTO v_countOperaciones
		FROM :v_resultado;
		   
		IF (v_countOperaciones = 0) THEN
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
		END IF;
		
		v_datosGenerales = select 
        CCDG."RazonSocial" AS "RazonCorredora", 
        CCDG."Direccion" AS "DireccionCorredora", 
        CCDG."RUT" AS "RutCorredora", 
        CCDG."Telefono" AS "TelefonoCorredora",
        TO_CHAR(TO_DATE(CONF."Valor",'YYYY-MM-DD'),'DD-MM-YYYY') AS "FechaSistema",
        CCDG."IdEmpresa"
        from "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG
        JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON (CONF."IdEmpresa" = CCDG."IdEmpresa" AND
													 	      CONF."CodigoParametro" = 'PLA.FechaSistema')
        WHERE CCDG."IdEmpresa" = p_IdEmpresa;
        
	    v_logo = select TOP 1
        "Binario" AS "LogoCorredora",
        "IdEmpresa"
        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		
        IF (v_countOperaciones > 0) THEN
	    SELECT "Resultado".*, "Logo".*, "DatosGenerales".*,
	    '' AS "MensajeError"
		FROM :v_resultado "Resultado"
		INNER JOIN :v_logo "Logo" ON ("Resultado"."IdEmpresa" = "Logo"."IdEmpresa")
		INNER JOIN :v_datosGenerales "DatosGenerales" ON ("DatosGenerales"."IdEmpresa" = "Resultado"."IdEmpresa");
		
		ELSE
		
		SELECT 
		'' AS "MontoAporte",
		'' AS "MontoRescate",
		'' AS "IdSerieFondo",
		'' AS "IdMoneda",
		'' AS "DocumentoIdentidadClienteSolicitud",
		'' AS "IdEmisor",
		'' AS "DocumentoIdentidad",
		'' AS "CodigoPortafolio",
		'' AS "IdEmpresa",
		'' AS "NombreEstado",
		'' AS "TipoMovimiento",
		"Logo".*, "DatosGenerales".*,
		v_MensajeError AS "MensajeError"
		FROM :v_logo "Logo"
		INNER JOIN :v_datosGenerales "DatosGenerales" ON ("DatosGenerales"."IdEmpresa" = 1);

		END IF;
		
		END; 
	END;
END;
		
