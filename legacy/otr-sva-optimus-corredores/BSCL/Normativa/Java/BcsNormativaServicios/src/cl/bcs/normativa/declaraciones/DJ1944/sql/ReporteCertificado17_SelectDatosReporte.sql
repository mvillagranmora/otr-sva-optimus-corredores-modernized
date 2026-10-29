   SELECT
			
	 c."Folio"
	,c."IdEmpresa"
	,c."Esquema"
	,c."Version"
	,c."Anio"
	,c."Periodo"
	
	,e."RazonSocial" AS "RazonSocialEmpresa"
	,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
	,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
	,e."RazonSocial" AS "NombreEntidadRecaudadora"
	
	,c."IdCliente"
	,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
	,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
	,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
	

	,G."Nombre" AS "NombreDelGiro"
	,d."SalAhorroNetoEjerNEGT1"
	,d."SalAhorroNetoEjerT2"
	,d."SalAhorroNetoEjeNegT3"
	,d."MtoRenRetFFMMPOS"
	,d."MtoRenRetFFMMNEG"
	
	FROM  -- AQUI la tabla que filtra los ruts
	
	(
	
		SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
		 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
		 WHERE CER."IdEmpresa" = :IdEmpresa AND CER."Version" = :Version AND CER."IdTipoCertificado"='CERT17'
		 AND CER."IdCertificadoCliente" IN (:IdCertificadoCliente)
		
	) AS c
	INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"					s	ON (e."IdSocioDeNegocio" = s."IdSocioDeNegocio")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 							p	ON (s."IdPersona"= p."IdPersona")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Giro"								g 	ON (p."IdGiroPrincipal" = g."IdGiro")
WHERE c."Version" = :Version
