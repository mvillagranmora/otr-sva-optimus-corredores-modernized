SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado21";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado21"(
	-- ID empresa del login.
	IN p_IdEmpresa       BIGINT   ,
	-- ID usuario del login.
	IN p_IdUsuario       BIGINT   ,
	-- ID modulo invocador.
	IN p_IdModulo      NVARCHAR(3)  ,
	IN p_Version      NVARCHAR(10) ,
	IN p_Todos       NVARCHAR(1) DEFAULT 'S',
	--IN p_IdCliente                      BIGINT      ,
	--IN p_IdPortafolio                   BIGINT      DEFAULT 0,
	IN p_RutCliente      NVARCHAR(40) DEFAULT '',
	IN p_CodigoPortafolio     NVARCHAR(100) DEFAULT '',
	IN p_Folio       BIGINT
)
LANGUAGE sqlscript
SQL security invoker
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle    TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		BEGIN
		DECLARE v_resultadoRegla  NVARCHAR(40);
		DECLARE v_FechaSistema  NVARCHAR(10);
		DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    DECLARE v_FechaCertificado NVARCHAR(10);

		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','INICIO','INICIO');
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
		END IF;


		SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1894' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1894' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
        IF v_DefineFechaCertificado = 'S'
        THEN
            v_FechaSistema := v_FechaCertificado;
        ELSE
		    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
        END IF;

		v_meses =
			SELECT 1 "Mes",'ENERO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 2 "Mes",'FEBRERO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 3 "Mes",'MARZO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 4 "Mes",'ABRIL' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 5 "Mes",'MAYO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 6 "Mes",'JUNIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 7 "Mes",'JULIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 8 "Mes",'AGOSTO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 10 "Mes",'OCTUBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY;

		v_Resultado =
			SELECT
			c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			,d."IdFondo"
			--montos certificado
			,d."MontoRescate"
			,d."MontoHistorico"
			,d."MayorValor"
			,d."MenorValor"
			,d."Factor"
			,d."MayorValorActualizado"
			,d."MenorValorActualizado"
			-- fecha
			,m."DescripcionMes"
			,m."Mes" --,m."NUMERO" AS "Numero"
			--cliente
			,c."IdCliente"
			,c."IdPortafolio"
			,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"--nombre del contribuyente
			,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
			--empresa
			,e."RazonSocial" AS "RazonSocialEmpresa"
			,e."RazonSocial" AS "NombreEntidadRecaudadora"
			,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
			,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
			,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
			--emisor
			,COALESCE(em."NombreCompleto", '') AS "NombreCompletoEmisor"
			,(SELECT "NombreSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" fm WHERE fm."IdSerieFondo" = d."IdFondo") AS "NombreFondo"
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" d ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" em ON (em."IdEmisor" = d."IdEmisor")
			INNER JOIN :v_meses m ON (m."Mes" = d."DescMes")
			WHERE :p_Todos = 'S' OR (c."RutCliente" = :p_RutCliente AND c."CodigoPortafolio" = :p_CodigoPortafolio )
			AND c."Version" = :p_Version
			ORDER BY c."Folio";
		
		v_datos =
			SELECT DISTINCT
			"Folio",
			"IdEmpresa",
			"Esquema",
			"Version",
			"Anio",
			"Periodo",
			"Mes",
			--instituciòn autorizada
			"RazonSocialEmpresa",
			"DocumentoIdentidadEmpresa",
			"DireccionEmpresa",
			--La Administradora de Fondos de Pensiones...
			"NombreEntidadRecaudadora",
			--cliente
			"IdFondo",
			"IdCliente",
			"IdPortafolio",
			"NombreCompletoCliente",
			"DocumentoIdentidadCliente",
			--emisor
			"NombreCompletoEmisor",
			"CiudadFecha",
			"NombreFondo"
			FROM :v_Resultado;

		v_meses_sin_datos =
			SELECT DISTINCT 
			c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			,c."IdFondo"
			--valores
			,0 AS "Factor"
			,0 AS "MontoRescate"
			,0 AS "MontoHistorico"
			,0 AS "MayorValor"
			,0 AS "MenorValor"
			,0 AS "MayorValorActualizado"
			,0 AS "MenorValorActualizado"
			--fecha
			,m."DescripcionMes"
			,m."Mes"
			--cliente
			,c."IdCliente"
			,c."IdPortafolio"
			,c."NombreCompletoCliente"
			,c."DocumentoIdentidadCliente"
			--empresa
			,c."RazonSocialEmpresa"
			,c."NombreEntidadRecaudadora"
			,c."DocumentoIdentidadEmpresa"
			,c."DireccionEmpresa"
			,c."CiudadFecha"
			--emisor
			,c."NombreCompletoEmisor"
			-- nombre fondo
			,c."NombreFondo"
			FROM :v_datos AS c, :v_meses AS m
			WHERE NOT EXISTS (	SELECT 1
						FROM :v_Resultado r
						WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','FIN','FIN');

		v_Resultado1 =
			SELECT
			d."Folio"
			,d."IdEmpresa"
			,d."Esquema"
			,d."Version"
			,d."Anio"
			,d."Periodo"
			,d."IdFondo"
			,d."Factor"
			,d."MontoRescate"
			,d."MontoHistorico"
			,d."MayorValor"
			,d."MenorValor"
			,d."MayorValorActualizado"
			,d."MenorValorActualizado"
			,d."Mes"
			,d."DescripcionMes"
			,d."IdCliente"
			,d."IdPortafolio"
			,d."NombreCompletoCliente"
			,d."DocumentoIdentidadCliente"
			,d."RazonSocialEmpresa"
			,d."NombreEntidadRecaudadora"
			,d."DocumentoIdentidadEmpresa"
			,d."DireccionEmpresa"
			,d."CiudadFecha"
			,d."NombreCompletoEmisor"
			,COALESCE(c."Certificacion",'NGE') AS "Certificacion"
			,COALESCE(c."EstadoCertificado",'NGE') AS "EstadoCertificado"
			,d."NombreFondo"
			FROM
			(SELECT * FROM :v_Resultado UNION ALL
			SELECT * FROM :v_meses_sin_datos) AS d
			LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" c ON (c."Esquema" = d."Esquema"
			AND c."Version" = d."Version")
			ORDER BY d."Folio",d."Mes";

		v_logo =
			SELECT TOP 1
			"Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR'
			AND "IdEmpresa" = :p_IdEmpresa
			ORDER BY "Version" DESC;

		v_firma =
			SELECT TOP 1
			"Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA'
			ORDER BY "Version" DESC;

		v_datos_corredor=
			SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante");

		SELECT * FROM :v_Resultado1
		CROSS JOIN :v_logo
		CROSS JOIN :v_firma
		CROSS JOIN :v_datos_corredor
		ORDER BY "Folio","Mes";
		END;
	END;
END;