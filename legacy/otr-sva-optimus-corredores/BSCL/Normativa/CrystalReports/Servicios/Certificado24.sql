SET SCHEMA "BSCL";
--GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado24";
--GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado24" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_Version								NVARCHAR(10) ,
	IN p_Rut           							TABLE ( "Rut" NVARCHAR(40) ),
	
	OUT p_Salida   TABLE (
        "Folio"							            BIGINT,	
        "IdEmpresa"						            BIGINT,	
        "Esquema"						            NVARCHAR(10),
        "Version"						            NVARCHAR(10),
        "Anio"							            NVARCHAR(4),
        "Periodo"						            NVARCHAR(20),
        ----------------------------------------------------------
        "NombreEntidadRecaudadora"					NVARCHAR(200),
        "DocumentoIdentidadEntidad"					NVARCHAR(11),	  
        "DireccionEntidadRecaudadora"		        NVARCHAR(200),	  
        "IdCliente"				                    BIGINT,	  
        "NombreCompletoCliente"					    NVARCHAR(200),
        "DocumentoIdentidadCliente"					NVARCHAR(200),	
        "DireccionCliente"			                NVARCHAR(200),	
        "RazonSocialEmpresa"			            NVARCHAR(200),	
        "DocumentoIdentidadEmpresa"				    NVARCHAR(200),
        "DireccionEmpresa"				            NVARCHAR(200),
        "CiudadFecha"						        NVARCHAR(200),
        "Mes"					                    BIGINT,
        "DescripcionMes"			                NVARCHAR(400),	
        "MontoModalidadEmpleador"		            DECIMAL(26,8),	
        "MontoModalidadTrabajador"			        DECIMAL(26,8),
        "MontoCotizacionesObligatorias"		        DECIMAL(26,8),
        "MontoCotizacionesVoluntarias"		        DECIMAL(26,8),
        "MontoTrabajadoresActivos"				    DECIMAL(26,8),
        "MontoPensionados"					        DECIMAL(26,8),
        "MontoRetencion"			                DECIMAL(26,8),
        "Certificacion"					            NVARCHAR(100),
        "EstadoCertificado"				            NVARCHAR(300),
        "ActivoPensionado"					        BIGINT,	
        "AhorrosModalidadIndirectaEmpleador"        DECIMAL(26,8),
        "AhorrosModalidadDirectaTrabajador"         DECIMAL(26,8),
        "CotizacionesModalidadIndirectaEmpleador"   DECIMAL(26,8),	
        "CotizacionesModalidadDirectaTrabajador"    DECIMAL(26,8),
        "CotizacionesTrabajadorIndependiente"       DECIMAL(26,8),
        "MontoRetiroPorAhorrosIncisoPrimero"        DECIMAL(26,8),
        "RetencionImpuestoSobreRetiros"             DECIMAL(26,8),
        "MontoRetiroPorAhorrosIncisoSegundo"        DECIMAL(26,8),
        "RetiroEmpleadorActualizado"                DECIMAL(26,8),
         "ResumenTrabajadorActivo"					DECIMAL(26,8),
         "ResumenTrabajadorPensionado"				DECIMAL(26,8),
        -----------------------------------------	
        "RazonSocialCorredora"          NVARCHAR(100),
        "DireccionCorredora"            NVARCHAR(300),
        "DocumentoIdentidadCorredora"   NVARCHAR(10),
        "rut_representante"				NVARCHAR(10),
        "nombre_representante"			NVARCHAR(100),
        -------------------------------------------
        "RutCertificado"				NVARCHAR(40),
        
        "LogoCorredora"                 BLOB,
        "FirmaCorredora"                BLOB
	)
	
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_Logo  TABLE("LogoCorredora" BLOB);
		DECLARE v_Firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_count BIGINT:=0;DECLARE v_FechaSistema  NVARCHAR(10);
        DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    DECLARE v_FechaCertificado NVARCHAR(10);

		v_Logo =
			SELECT TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_Firma =
			SELECT TOP 1 "Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
	
		v_DatosCorredor = 
			SELECT 
			  PER."RazonSocial" 
			 ,EMP."IdEmpresa" 
			 ,CONCAT(DIR."Calle" ,CONCAT(' ',CONCAT(DIR."Numero",CONCAT(' , ',CIU."NombreCiudad")))) AS "Direccion"
			 ,DOC."DocumentoIdentidad" 
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona" 
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON DIR."IdPersona" = PER."IdPersona" AND DIR."Predeterminado" ='S'
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
			INNER JOIN  "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON CIU."IdCiudad" = DIR."IdCiudad" 
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;

			---zona de filtros
													
		v_cerificados = 	
				SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
			 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
			 INNER JOIN :p_Rut AS RUT ON RUT."Rut" = CER."RutCliente"
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT24'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT24'
					 AND EXISTS 
					 ( 
					 
					   SELECT 1 FROM 
						(SELECT COUNT(1) AS "CANT" FROM :p_Rut WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
			
					  );
										
			
		v_datos_representante=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);
		
			
		--zona especifica ceritificado
		
		SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1899' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1899' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
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
                c."Folio",--Certificado numero
                c."IdEmpresa",
                c."Esquema",
                c."Version",              
                c."Anio",
                c."Periodo", 
                -- Entidad
                e."RazonSocial" AS "NombreEntidadRecaudadora",
                e."DocumentoIdentidad" AS "DocumentoIdentidadEntidad",
                e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEntidadRecaudadora",
                --
                COALESCE(cli."IdCliente",0) AS "IdCliente",--contribuyente               
                COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
                TRIM(c."RutCliente") AS "DocumentoIdentidadCliente",--rut del contribuyente
                dfi."NombreLargo" AS "DireccionCliente",--direccion contribuyente
                e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
                e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
                e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
                e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha",  --ciudad y fecha
                m."Mes",
                m."DescripcionMes" AS "DescripcionMes",
                --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR DEPENDIENTE DEL ART.42 N°1 LIR, ACTUALIZADO
                --Cotizaciones   Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
                d."ModalidadIndirectaViaEmpleador" AS "MontoModalidadEmpleador",d."ModalidadDirectaViaTrabajador" AS "MontoModalidadTrabajador",
                --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR INDEPENDIENTE DEL ART.42 N°2 LIR, ACTUALIZADO
                d."CotizacionesObligatorias" AS "MontoCotizacionesObligatorias",--($)
                d."CotizacionesVoluntarias" AS "MontoCotizacionesVoluntarias",--Cotizaciones Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
                --MONTO DE LOS RETIROS EFECTUADOS CON CARGO A LOS AHORROS PREVISIONALES VOLUNTARIOS REALIZADOS, ACTUALIZADO
                d."TrabajadoresActivos" AS "MontoTrabajadoresActivos", --($)
                d."Pensionados" AS "MontoPensionados",--Pensionados o personas que cumplen con los requisitos para pensionarse que exigen los Arts.3° y 68 letra B) D.L. N°3.500/80 o D.L. N°2.448/7
                ---
                d."RetencionImpuesto" AS "MontoRetencion",--Retención de Impuesto de 15% practicada sobre los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados actualizada en $
                --
                d."ActivoPensionado", 		            --C3
                d."AhorrosModalidadIndirectaEmpleador",	--C11
                d."AhorrosModalidadDirectaTrabajador",	--C12
                d."CotizacionesModalidadIndirectaEmpleador",	--C13 	$ desde uf
                d."CotizacionesModalidadDirectaTrabajador",	--C14	$ desde uf
                d."CotizacionesTrabajadorIndependiente",  --C15	$ desde uf
                d."MontoRetiroPorAhorrosIncisoPrimero",	--C16
                d."RetencionImpuestoSobreRetiros",        --C17
                d."MontoRetiroPorAhorrosIncisoSegundo",   --C18
                d."RetiroEmpleadorActualizado"            --C9
            FROM :v_cerificados c
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" dfi ON (dfi."IdDireccionFisica" = cli."IdDireccionFisicaPrincipal")
            INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
            INNER JOIN :v_meses AS m ON (m."Mes" = d."Mes")
            WHERE c."Version" = :p_Version;
			
			
        v_datos =
            SELECT
                DISTINCT
                    "Folio","IdEmpresa","Esquema","Version","Anio","Periodo", 
                    -- Entidad
                    "NombreEntidadRecaudadora","DocumentoIdentidadEntidad","DireccionEntidadRecaudadora",
                    --
                    "IdCliente","NombreCompletoCliente","DocumentoIdentidadCliente","DireccionCliente",
                    "RazonSocialEmpresa","DocumentoIdentidadEmpresa","DireccionEmpresa","CiudadFecha"
            FROM :v_Resultado;

        v_meses_sin_datos =
            SELECT
                c."Folio",c."IdEmpresa",c."Esquema",c."Version",c."Anio",c."Periodo", 
                -- Entidad
                c."NombreEntidadRecaudadora",c."DocumentoIdentidadEntidad",c."DireccionEntidadRecaudadora",
                --
                c."IdCliente",--contribuyente               
                c."NombreCompletoCliente",--nombre del contribuyente
                c."DocumentoIdentidadCliente",--rut del contribuyente
                c."DireccionCliente",--direccion contribuyente
                c."RazonSocialEmpresa", --razon social de la administradora
                c."DocumentoIdentidadEmpresa", --rut de la administradora
                c."DireccionEmpresa", --diraccion de la administradora
                c."CiudadFecha",  --ciudad y fecha
                m."Mes",
                m."DescripcionMes" AS "DescripcionMes",
                --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR DEPENDIENTE DEL ART.42 N°1 LIR, ACTUALIZADO
                --Cotizaciones   Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
                0 AS "MontoModalidadEmpleador",0 AS "MontoModalidadTrabajador",
                --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR INDEPENDIENTE DEL ART.42 N°2 LIR, ACTUALIZADO
                0 AS "MontoCotizacionesObligatorias",--($)
                0 AS "MontoCotizacionesVoluntarias",--Cotizaciones Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
                --MONTO DE LOS RETIROS EFECTUADOS CON CARGO A LOS AHORROS PREVISIONALES VOLUNTARIOS REALIZADOS, ACTUALIZADO
                0 AS "MontoTrabajadoresActivos", --($)
                0 AS "MontoPensionados",--Pensionados o personas que cumplen con los requisitos para pensionarse que exigen los Arts.3° y 68 letra B) D.L. N°3.500/80 o D.L. N°2.448/7
                ---
                0 AS "MontoRetencion",--Retención de Impuesto de 15% practicada sobre los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados actualizada en $
                --
                0 AS "ActivoPensionado", 		            --C3
                0 AS "AhorrosModalidadIndirectaEmpleador",	--C11
                0 AS "AhorrosModalidadDirectaTrabajador",	--C12
                0 AS "CotizacionesModalidadIndirectaEmpleador",	--C13 	$ desde uf
                0 AS "CotizacionesModalidadDirectaTrabajador",	--C14	$ desde uf
                0 AS "CotizacionesTrabajadorIndependiente",  --C15	$ desde uf
                0 AS "MontoRetiroPorAhorrosIncisoPrimero",	--C16
                0 AS "RetencionImpuestoSobreRetiros",        --C17
                0 AS "MontoRetiroPorAhorrosIncisoSegundo",   --C18
                0 AS "RetiroEmpleadorActualizado"            --C9
            FROM :v_datos AS c,:v_meses AS m
            WHERE NOT EXISTS (SELECT 1 FROM :v_Resultado r WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");

			
			
		--Zona comun	
						
		v_DatosDetalle =
            SELECT
                d."Folio",d."IdEmpresa",d."Esquema",d."Version",d."Anio",d."Periodo", 
                -- Entidad
                d."NombreEntidadRecaudadora",d."DocumentoIdentidadEntidad",d."DireccionEntidadRecaudadora",
                --
                d."IdCliente",d."NombreCompletoCliente",d."DocumentoIdentidadCliente",d."DireccionCliente",
                d."RazonSocialEmpresa",d."DocumentoIdentidadEmpresa",d."DireccionEmpresa",d."CiudadFecha",
                d."Mes",d."DescripcionMes",d."MontoModalidadEmpleador",d."MontoModalidadTrabajador",
                d."MontoCotizacionesObligatorias",d."MontoCotizacionesVoluntarias",d."MontoTrabajadoresActivos",
                d."MontoPensionados",d."MontoRetencion",
                COALESCE(c."Certificacion",'NGE') AS "Certificacion",
                COALESCE(c."EstadoCertificado",'NGE') AS "EstadoCertificado",
                --
                d."ActivoPensionado", 		            --C3
                d."AhorrosModalidadIndirectaEmpleador",	--C11
                d."AhorrosModalidadDirectaTrabajador",	--C12
                d."CotizacionesModalidadIndirectaEmpleador",	--C13 	$ desde uf
                d."CotizacionesModalidadDirectaTrabajador",	--C14	$ desde uf
                d."CotizacionesTrabajadorIndependiente",  --C15	$ desde uf
                d."MontoRetiroPorAhorrosIncisoPrimero",	--C16
                d."RetencionImpuestoSobreRetiros",        --C17
                d."MontoRetiroPorAhorrosIncisoSegundo",   --C18
                d."RetiroEmpleadorActualizado",            --C9
                --
                CASE 
                    WHEN d."ActivoPensionado" = 1 THEN SUM(d."MontoRetiroPorAhorrosIncisoPrimero"+ d."MontoRetiroPorAhorrosIncisoSegundo"  ) 
                ELSE 0 END AS "ResumenTrabajadorActivo",
                --
                CASE 
                    WHEN d."ActivoPensionado" = 2 THEN SUM(d."MontoRetiroPorAhorrosIncisoPrimero"+ d."MontoRetiroPorAhorrosIncisoSegundo" ) 
                ELSE 0 END AS "ResumenTrabajadorPensionado"
            FROM
            (SELECT * FROM :v_Resultado UNION ALL
                SELECT * FROM :v_meses_sin_datos) AS d
            LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" c ON (c."Esquema" = d."Esquema"
                                                                AND c."Version" = d."Version")
            GROUP BY 	d."ActivoPensionado",d."Folio",d."IdEmpresa",d."Esquema",d."Version",d."Anio",d."Periodo",
                        d."NombreEntidadRecaudadora",d."DocumentoIdentidadEntidad",d."DireccionEntidadRecaudadora",
                        d."IdCliente",d."NombreCompletoCliente",d."DocumentoIdentidadCliente",d."DireccionCliente",
                        d."RazonSocialEmpresa",d."DocumentoIdentidadEmpresa",d."DireccionEmpresa",d."CiudadFecha",
                        d."Mes",d."DescripcionMes",d."MontoModalidadEmpleador",d."MontoModalidadTrabajador",
                        d."MontoCotizacionesObligatorias",d."MontoCotizacionesVoluntarias",d."MontoTrabajadoresActivos",
                        d."MontoPensionados",d."MontoRetencion",
                        "Certificacion",
                        "EstadoCertificado",
                        d."AhorrosModalidadIndirectaEmpleador",
                        d."AhorrosModalidadDirectaTrabajador",
                        d."CotizacionesModalidadIndirectaEmpleador",
                        d."CotizacionesModalidadDirectaTrabajador",
                        d."CotizacionesTrabajadorIndependiente",
                        d."MontoRetiroPorAhorrosIncisoPrimero",
                        d."RetencionImpuestoSobreRetiros",
                        d."MontoRetiroPorAhorrosIncisoSegundo",
                        d."RetiroEmpleadorActualizado"
            ORDER BY d."Folio",d."Mes";
			
			
						
		p_Salida = SELECT	 
				 "Detalle"."Folio"
				,"Detalle"."IdEmpresa"
				,"Detalle"."Esquema"
				,"Detalle"."Version"
				,"Detalle"."Anio"
				,"Detalle"."Periodo"
                ---
				,"Detalle"."NombreEntidadRecaudadora"
				,"Detalle"."DocumentoIdentidadEntidad"
				,"Detalle"."DireccionEntidadRecaudadora"
				,"Detalle"."IdCliente"
				,"Detalle"."NombreCompletoCliente"
				,"Detalle"."DocumentoIdentidadCliente"
				,"Detalle"."DireccionCliente"
				,"Detalle"."RazonSocialEmpresa"
				,"Detalle"."DocumentoIdentidadEmpresa"
				,"Detalle"."DireccionEmpresa"
				,"Detalle"."CiudadFecha"
				,"Detalle"."Mes"
				,"Detalle"."DescripcionMes"
				,"Detalle"."MontoModalidadEmpleador"
				,"Detalle"."MontoModalidadTrabajador"
				,"Detalle"."MontoCotizacionesObligatorias"
				,"Detalle"."MontoCotizacionesVoluntarias"
				,"Detalle"."MontoTrabajadoresActivos"
				,"Detalle"."MontoPensionados"
				,"Detalle"."MontoRetencion"
				,"Detalle"."Certificacion"
                ,"Detalle"."EstadoCertificado"
                ,"Detalle"."ActivoPensionado"
                ,"Detalle"."AhorrosModalidadIndirectaEmpleador"
                ,"Detalle"."AhorrosModalidadDirectaTrabajador"
                ,"Detalle"."CotizacionesModalidadIndirectaEmpleador"
                ,"Detalle"."CotizacionesModalidadDirectaTrabajador"
                ,"Detalle"."CotizacionesTrabajadorIndependiente"
                ,"Detalle"."MontoRetiroPorAhorrosIncisoPrimero"
                ,"Detalle"."RetencionImpuestoSobreRetiros"
                ,"Detalle"."MontoRetiroPorAhorrosIncisoSegundo"
                ,"Detalle"."RetiroEmpleadorActualizado"
                ,"Detalle"."ResumenTrabajadorActivo"
                ,"Detalle"."ResumenTrabajadorPensionado"
				-------------------------
				,"Corredor"."RazonSocial" AS "RazonSocialCorredora"
				,"Corredor"."Direccion" AS "DireccionCorredora"
				,"Corredor"."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
				,"Rep"."rut_representante"
				,"Rep"."nombre_representante"
				,"Detalle"."DocumentoIdentidadCliente" AS "RutCertificado"
				,"Logo"."LogoCorredora"
				,"Firma"."FirmaCorredora"
					FROM :v_DatosDetalle AS "Detalle"
					LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
			        LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
					LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
                    LEFT JOIN :v_datos_representante AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
				ORDER BY "Detalle"."Folio","Detalle"."Mes";
					 

END;
