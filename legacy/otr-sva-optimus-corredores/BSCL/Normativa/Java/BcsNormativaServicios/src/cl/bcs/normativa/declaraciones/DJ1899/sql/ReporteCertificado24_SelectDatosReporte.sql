
WITH Meses AS (	
			SELECT 1 "Mes",'ENERO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 2 "Mes",'FEBRERO' 	AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 3 "Mes",'MARZO'		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 4 "Mes",'ABRIL' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 5 "Mes",'MAYO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 6 "Mes",'JUNIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 7 "Mes",'JULIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 8 "Mes",'AGOSTO'		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 10 "Mes",'OCTUBRE' 	AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY
)
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
			 e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha",  --ciudad y fecha
			 m."Mes",
			 m."DescripcionMes" AS "DescripcionMes",
			 --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR DEPENDIENTE DEL ART.42 N°1 LIR, ACTUALIZADO
			 --Cotizaciones   Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
			 d."ModalidadIndirectaViaEmpleador" AS "MontoModalidadEmpleador",
			 d."ModalidadDirectaViaTrabajador" AS "MontoModalidadTrabajador",
			 --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR INDEPENDIENTE DEL ART.42 N°2 LIR, ACTUALIZADO
			 d."CotizacionesObligatorias" AS "MontoCotizacionesObligatorias",--($)
			 d."CotizacionesVoluntarias" AS "MontoCotizacionesVoluntarias",--Cotizaciones Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
			 --MONTO DE LOS RETIROS EFECTUADOS CON CARGO A LOS AHORROS PREVISIONALES VOLUNTARIOS REALIZADOS, ACTUALIZADO
			 d."TrabajadoresActivos" AS "MontoTrabajadoresActivos", --($)
			 d."Pensionados" AS "MontoPensionados",--Pensionados o personas que cumplen con los requisitos para pensionarse que exigen los Arts.3° y 68 letra B) D.L. N°3.500/80 o D.L. N°2.448/7
			 ---
			 d."RetencionImpuesto" AS "MontoRetencion",--Retención de Impuesto de 15% practicada sobre los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados actualizada en $
			 COALESCE(CP."Certificacion",'NGE') AS "Certificacion",
 			 COALESCE(CP."EstadoCertificado",'NGE') AS "EstadoCertificado",
			 
			 --
			 CAST(d."ActivoPensionado" AS BIGINT) AS "ActivoPensionado", --C3
			 d."AhorrosModalidadIndirectaEmpleador", --C11
			 d."AhorrosModalidadDirectaTrabajador", --C12
			 d."CotizacionesModalidadIndirectaEmpleador", --C13  $ desde uf
			 d."CotizacionesModalidadDirectaTrabajador", --C14 $ desde uf
			 d."CotizacionesTrabajadorIndependiente",  --C15 $ desde uf
			 d."MontoRetiroPorAhorrosIncisoPrimero", --C16
			 d."RetencionImpuestoSobreRetiros",        --C17
			 d."MontoRetiroPorAhorrosIncisoSegundo",   --C18
			 d."RetiroEmpleadorActualizado",            --C9
			 SUMA."ResumenTrabajadorActivo",
			 SUMA."ResumenTrabajadorPensionado"
			FROM  --AQUI la tabla que filtra los ruts
			
			(
			
				SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
				 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
				 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT24'
				 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
				
			) AS c
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
			INNER JOIN 	(SELECT * FROM Meses) m ON (m."Mes" = d."Mes")
			INNER JOIN 
			(
				SELECT 
					d."ActivoPensionado",	
					CASE WHEN d."ActivoPensionado" = 1 THEN SUM(d."MontoRetiroPorAhorrosIncisoPrimero"+ d."MontoRetiroPorAhorrosIncisoSegundo"  )
			 		   ELSE 0 END AS "ResumenTrabajadorActivo",
			 		   
			 		CASE WHEN d."ActivoPensionado" = 2 THEN SUM(d."MontoRetiroPorAhorrosIncisoPrimero"+ d."MontoRetiroPorAhorrosIncisoSegundo" )
			 		   ELSE 0 END AS "ResumenTrabajadorPensionado",
			 		   "IdCertificadoCliente"
				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" AS d
				WHERE d."IdCertificadoCliente" IN (:IdCertificadoCliente)
				GROUP BY "ActivoPensionado","IdCertificadoCliente"
			) AS SUMA ON c."IdCertificadoCliente"=SUMA."IdCertificadoCliente" AND SUMA."ActivoPensionado" = d."ActivoPensionado"
			LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1899' AND CP."Version" = :Version)
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" dfi ON (dfi."IdDireccionFisica" = cli."IdDireccionFisicaPrincipal")
			
			WHERE c."Version" = :Version 
			ORDER BY m."Mes" ASC