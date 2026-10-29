SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionDeContrato";

--
--  Proposito: servicio para consultar Emision de Papeleta FWD
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--
--SET SCHEMA "BSCL";


CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionDeContrato"(
	-- Parametros entrada 
	IN p_IdEmpresa			BIGINT,
	IN p_FolioOperacion		BIGINT,
	IN p_Rut_ApoderadoClientePrimero NVARCHAR(20),
	IN p_Rut_ApoderadoCorredoraPrimero NVARCHAR(20),
	IN p_Rut_ApoderadoClienteSegundo NVARCHAR(20),
	IN p_Rut_ApoderadoCorredoraSegundo NVARCHAR(20)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	BEGIN
		--------------------
		--   Variables    --
		--------------------
		DECLARE varFECHA 		DATE;
		DECLARE varUF 			DECIMAL(26,8);	
		DECLARE varDOLAR 		DECIMAL(26,8);
		DECLARE varExisteOp		INT;
		DECLARE varIdCliente   	INT;
		DECLARE varExisteDolar 	INT;
		DECLARE varExisteUf		INT;
		DECLARE	varNomApodCor1				NVARCHAR(100);
		DECLARE varRutApodCor1				NVARCHAR(40);
		DECLARE varDirApodCor1				NVARCHAR(100);
		DECLARE varTelApodCor1				NVARCHAR(100);
		DECLARE varNombreCiudadApodCor1 	NVARCHAR(100);
		DECLARE	varNomApodCor2				NVARCHAR(100);
		DECLARE varRutApodCor2				NVARCHAR(40);
		DECLARE varDirApodCor2				NVARCHAR(100);
		DECLARE varTelApodCor2				NVARCHAR(100);
		DECLARE varNombreCiudadApodCor2 	NVARCHAR(100);
		DECLARE	varNombreReprEmpresa		NVARCHAR(100);
		DECLARE varRutReprEmpresa			NVARCHAR(40);
		DECLARE varCiudadReprEmpresa		NVARCHAR(100);
		DECLARE varDireccionReprEmpresa		NVARCHAR(100);
		DECLARE varTelefonoReprEmpresa		NVARCHAR(100);
		DECLARE varTipoPersona				NVARCHAR(1);
		DECLARE	varNombreReprCliente 		NVARCHAR(100);
		DECLARE varRutReprCliente 			NVARCHAR(40);
		DECLARE varCiudadReprCliente    	NVARCHAR(100);
		DECLARE varDireccionReprCliente	    NVARCHAR(100);
		DECLARE varTelefonoReprCliente		NVARCHAR(100);
		DECLARE	varNombreApoCliente1		NVARCHAR(100);
		DECLARE varRutApoCliente1			NVARCHAR(40);
		DECLARE varCiudadApoCliente1		NVARCHAR(100);
		DECLARE varDireccionApoCliente1 	NVARCHAR(100);
		DECLARE varTelefonoApoCliente1		NVARCHAR(100);
		DECLARE	varNombreApoCliente2		NVARCHAR(100);--
		DECLARE varRutApoCliente2			NVARCHAR(40);
		DECLARE varCiudadApoCliente2		NVARCHAR(100);
		DECLARE varDireccionApoCliente2 	NVARCHAR(100);
		DECLARE varTelefonoApoCliente2		NVARCHAR(100);
		DECLARE	varNombreCliente			NVARCHAR(100);--
		DECLARE varRutCliente				NVARCHAR(40);
		DECLARE varTipoPersonaCliente		NVARCHAR(1);
		DECLARE varCiudadCliente			NVARCHAR(100);
		DECLARE varDireccionCliente 		NVARCHAR(100);
		DECLARE varTelefonoCliente			NVARCHAR(100);
	    DECLARE varBancosReferencia       	NVARCHAR(100);
		DECLARE varExisteNomApodCor1 INT;
		DECLARE varExisteNomApodCli1 INT;
		DECLARE varExisteNomApodCli2 INT;
		DECLARE varExisteReprCliente INT;
		DECLARE varEsFechaFirma				NVARCHAR(1);
		DECLARE varExisteParametro			INT;
		DECLARE varFechaFirmaFWD			DATE;
		DECLARE varExisteFechaFirma			INT;
		DECLARE varDummy					INT;
		
	
		--Se obtienen los bancos de referencia forwards
		SELECT
			STRING_AGG(BR.BancosReferenciaForward,
		', ') INTO varBancosReferencia
		FROM
		(
		SELECT
			DISTINCT "NombreBanco" AS BancosReferenciaForward
		FROM
			"BSCL.Plataforma.BD::BAN.Banco" CB
		WHERE
			CB."BancoReferenciaForward" = 'S') BR;
			
	
		
		--Se obtiene FechaOperacion
		SELECT coalesce(CC."FechaOperacion",'1900-01-01'), 
		       "IdCliente",
		       1
		        INTO varFECHA, varIdCliente, varExisteOp
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
		WHERE CC."IdTipoProducto"	= 'MEFW' 
		  --AND CC."FolioCartera" 	= :p_FolioOperacion
		  AND CC."IdCartera"		= :p_FolioOperacion;
		
		--Validar SI Existe Dolar
		SELECT count(1) INTO varExisteDolar
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
		WHERE PTC."Origen"	= 'OBSERVADO' 
		  AND PTC."Fecha"	= :varFECHA 
		  AND PTC."IdMoneda"= 'DO';
		
		--Validar SI Existe UF
		SELECT count(1) INTO varExisteUf
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
		WHERE PTC."Origen"	= 'OBSERVADO' 
		  AND PTC."Fecha"	= :varFECHA
		  AND PTC."IdMoneda"= 'UF';
		
		--Validar SI Existe Apoderado Corredora
		SELECT count(1) INTO varExisteNomApodCor1
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
			ON (CLI."IdPersona" = SOC."IdPersona")
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio" AND EMP."IdEmpresa" = :p_IdEmpresa)
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS CR
			ON (CR."IdPersonaOrigen" = SOC."IdPersona" AND CR."IdTipoRelacion" = 'APO' AND CR."IdEstado" = 'ACT')
		WHERE CLI."IdEstado" = 'ACT';
		
		
		IF (varExisteOp != 0 AND varExisteDolar != 0 AND varExisteUf != 0 AND varExisteNomApodCor1 != 0) THEN
			
			--Se obtiene el valor del dolar
			SELECT coalesce(PTC."TipoCambioMercado",0) INTO varDOLAR
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			WHERE PTC."Origen"	= 'OBSERVADO'
			  AND PTC."Fecha"	= :varFECHA
			  AND PTC."IdMoneda"= 'DO';
	
			--Se obtiene el valor de la UF
			SELECT PTC."TipoCambioMercado" INTO varUF
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			WHERE PTC."Origen"	= 'OBSERVADO'
			  AND PTC."Fecha"	= :varFECHA
			  AND PTC."IdMoneda"= 'UF';
			
			--Se obtienen datos Apoderado de Corredora
			IF (varExisteNomApodCor1 >= 1 AND p_Rut_ApoderadoCorredoraPrimero <> '334-4') THEN
			SELECT
				COALESCE(CR."NombreCompletoDestino",'-'),
				COALESCE(CR."DocumentoIdentidadDestino",'-'),
				COALESCE(DIR."Calle",'-')||' '||COALESCE(DIR."Numero",'-')||' '||COALESCE(DIR."Departamento",'-'),
				COALESCE(TEL."Numero",'-')||' '||COALESCE(TEL."Anexo",'-'),
				COALESCE(DIR."NombreCiudad",'-')
			  INTO varNomApodCor1, varRutApodCor1, varDirApodCor1, varTelApodCor1, varNombreCiudadApodCor1
			FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
				ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" CR
				ON SOC."IdPersona" = CR."IdPersonaOrigen" AND CR."IdTipoRelacion" = 'APO' AND CR."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
				ON SOC."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
			WHERE EMP."IdEmpresa" = :p_IdEmpresa AND TRIM(COALESCE(CR."DocumentoIdentidadDestino",'-')) = :p_Rut_ApoderadoCorredoraPrimero;
			ELSE 
			    SELECT '','','','',''
				INTO varNomApodCor1, varRutApodCor1, varDirApodCor1, varTelApodCor1, varNombreCiudadApodCor1
				FROM DUMMY;
		   END IF;	
			IF (varExisteNomApodCor1 > 1 AND p_Rut_ApoderadoCorredoraSegundo <> '334-4') THEN
				SELECT
					COALESCE(CR."NombreCompletoDestino",'-') AS "NombreCompleto",
					COALESCE(CR."DocumentoIdentidadDestino",'-') AS "DocumentoIdentidad",
					COALESCE(DIR."Calle",'-')||' '||COALESCE(DIR."Numero",'-')||' '||COALESCE(DIR."Departamento",'-') AS "Direccion",
					COALESCE(TEL."Numero",'-')||' '||COALESCE(TEL."Anexo",'-') AS "Telefono", 
					COALESCE(DIR."NombreCiudad",'-') AS "Ciudad"
				INTO varNomApodCor2, varRutApodCor2, varDirApodCor2, varTelApodCor2, varNombreCiudadApodCor2
				FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
					ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" CR
					ON SOC."IdPersona" = CR."IdPersonaOrigen" AND CR."IdTipoRelacion" = 'APO' AND CR."IdEstado" = 'ACT'
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
					ON SOC."IdPersona" = PER."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
					ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
					ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
				WHERE EMP."IdEmpresa" = :p_IdEmpresa AND TRIM(COALESCE(CR."DocumentoIdentidadDestino",'-')) = :p_Rut_ApoderadoCorredoraSegundo;
		   ELSE 
			    SELECT '','','','',''
				INTO varNomApodCor2, varRutApodCor2, varDirApodCor2, varTelApodCor2, varNombreCiudadApodCor2
				FROM DUMMY;
		   END IF;		
		  
		  
			--Se obtienen datos Representante de Corredora
			SELECT
				TOP 1 COALESCE(REPR."NombreCompleto",'-'),
				--COALESCE(REPR."DocumentoIdentidad",'-'),
				CASE WHEN COALESCE(REPR."DocumentoIdentidad",'') <> '' AND REPR."TipoDocumentoIdentidad" = 'R' THEN
						"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(REPR."DocumentoIdentidad",'-'),'i',0)||
						'-'||SUBSTR_AFTER(REPR."DocumentoIdentidad",'-')
					 ELSE COALESCE(REPR."DocumentoIdentidad",'-') END,
				COALESCE(DIR."NombreCiudad",'-'),
				COALESCE(DIR."Calle",'-')||' '||COALESCE(DIR."Numero",'-')||' '||COALESCE(DIR."Departamento",'-'),
				COALESCE(TEL."Numero",'-')||' '||COALESCE(TEL."Anexo",'-')
			  INTO varNombreReprEmpresa, varRutReprEmpresa, varCiudadReprEmpresa, varDireccionReprEmpresa, varTelefonoReprEmpresa
			FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
				ON CC."IdEmpresa" = EMP."IdEmpresa"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
				ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
				ON SOC."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
				ON PER."IdPersonaRepresentante" = REPR."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON REPR."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON REPR."IdTelefonoPrincipal" = TEL."IdTelefono" AND REPR."IdPersona" = TEL."IdPersona"
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;

			--datos cliente
			SELECT
				CLI."NombreCompleto",
			    CLI."DocumentoIdentidad",
			    DIRCLI."NombreCiudad",
			    COALESCE(DIRCLI."Calle", '-') || ' ' || COALESCE(DIRCLI."Numero", '-') || ' ' || COALESCE(DIRCLI."Departamento", '-') ,
			    COALESCE(TELCLI."Numero", '-') || ' ' || COALESCE(TELCLI."Anexo", '-') ,
			    CLI."IdTipoPersona"
			INTO varNombreCliente, varRutCliente, varCiudadCliente, varDireccionCliente, varTelefonoCliente, varTipoPersonaCliente
			FROM
			    "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERCLI ON PERCLI."IdPersona" = CLI."IdPersona"
			    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRCLI ON PERCLI."IdDireccionFisicaPrincipal" = DIRCLI."IdDireccionFisica"
			    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELCLI ON TELCLI."IdTelefono" = PERCLI."IdTelefonoPrincipal"
			    AND TELCLI."IdPersona" = PERCLI."IdPersona"
			WHERE
			    "IdCliente" = :varIdCliente;						
		
			SELECT count(1) INTO varExisteNomApodCli1
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				ON CLI."IdPersona" = PER."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
				ON REL."IdPersonaOrigen" = PER."IdPersona"
				AND "IdTipoRelacion" = 'APO'
				AND REL."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
				ON REL."IdPersonaDestino" = REPR."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
				ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
				ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
		    WHERE CLI."IdCliente" = :varIdCliente AND REPR."DocumentoIdentidad" = :p_Rut_ApoderadoClientePrimero;
		   
			SELECT count(1) INTO varExisteNomApodCli2
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				ON CLI."IdPersona" = PER."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
				ON REL."IdPersonaOrigen" = PER."IdPersona"
				AND "IdTipoRelacion" = 'APO'
				AND REL."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
				ON REL."IdPersonaDestino" = REPR."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
				ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
				ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
		    WHERE CLI."IdCliente" = :varIdCliente AND REPR."DocumentoIdentidad" = :p_Rut_ApoderadoClienteSegundo;
				
		
		if(varExisteNomApodCli1>0) THEN 
		 
			--Se obtienen datos Representante de Cliente (apoderado)
			SELECT TOP 1
				"NombreCompleto",
				 "DocumentoIdentidad",
				 "Ciudad",
				 "Direccion",
				 "Telefono"
				INTO varNombreApoCliente1, varRutApoCliente1, varCiudadApoCliente1, varDireccionApoCliente1, varTelefonoApoCliente1
			FROM (
				SELECT 
						PER."Tipo",
						--Representante
						CASE WHEN COALESCE(REPR."DocumentoIdentidad",'') <> '' AND REPR."TipoDocumentoIdentidad" = 'R' THEN
								"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(REPR."DocumentoIdentidad",'-'),'i',0)||
								'-'||SUBSTR_AFTER(REPR."DocumentoIdentidad",'-')
							 ELSE COALESCE(REPR."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
						COALESCE(REPR."NombreCompleto",'-') AS "NombreCompleto",
					    COALESCE(DIR."NombreCiudad",'-') AS "Ciudad",
						COALESCE(REPR_DIR."Calle",'-')||' '||COALESCE(REPR_DIR."Numero",'-')||' '||COALESCE(REPR_DIR."Departamento",'-') AS "Direccion",
						COALESCE(REPR_TEL."Numero",'-')||' '||COALESCE(REPR_TEL."Anexo",'-') AS "Telefono"
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
					ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
					ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
					ON CLI."IdPersona" = PER."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
					ON REL."IdPersonaOrigen" = PER."IdPersona"
					AND "IdTipoRelacion" = 'APO'
					AND REL."IdEstado" = 'ACT'
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
					ON REL."IdPersonaDestino" = REPR."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
					ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
					ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
			    WHERE REPR."DocumentoIdentidad" = :p_Rut_ApoderadoClientePrimero
			);
		
		ELSE 
			    SELECT '','','','',''
				INTO varNombreApoCliente1, varRutApoCliente1, varCiudadApoCliente1, varDireccionApoCliente1, varTelefonoApoCliente1
				FROM DUMMY;
		END IF;
		
	
	
		IF (varExisteNomApodCli2 > 0) THEN
		
			--Se obtienen datos del segundo apoderado
			SELECT 
				COALESCE(REPR."NombreCompleto",'-') AS "NombreCompleto",
				CASE WHEN COALESCE(REPR."DocumentoIdentidad",'') <> '' AND REPR."TipoDocumentoIdentidad" = 'R' THEN
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(REPR."DocumentoIdentidad",'-'),'i',0)||
					'-'||SUBSTR_AFTER(REPR."DocumentoIdentidad",'-')
					 ELSE COALESCE(REPR."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",						
					    COALESCE(DIR."NombreCiudad",'-') AS "Ciudad",
						COALESCE(REPR_DIR."Calle",'-')||' '||COALESCE(REPR_DIR."Numero",'-')||' '||COALESCE(REPR_DIR."Departamento",'-') AS "Direccion",
						COALESCE(REPR_TEL."Numero",'-')||' '||COALESCE(REPR_TEL."Anexo",'-') AS "Telefono"
			INTO varNombreApoCliente2, varRutApoCliente2, varCiudadApoCliente2, varDireccionApoCliente2, varTelefonoApoCliente2
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				ON CLI."IdPersona" = PER."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
				ON REL."IdPersonaOrigen" = PER."IdPersona"
				AND "IdTipoRelacion" = 'APO'
				AND REL."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
				ON REL."IdPersonaDestino" = REPR."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
				ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
				ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
		    WHERE CLI."IdCliente" = :varIdCliente AND REPR."DocumentoIdentidad" =  :p_Rut_ApoderadoClienteSegundo;
		
		ELSE 
			
			    SELECT '','','','',''
				INTO varNombreApoCliente2, varRutApoCliente2, varCiudadApoCliente2, varDireccionApoCliente2, varTelefonoApoCliente2
				FROM DUMMY;
		END IF;
	
		SELECT 
			count(1)
		INTO varExisteReprCliente
		FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
			ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
			ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
			ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
			ON REL."IdPersonaOrigen" = PER."IdPersona"
			AND "IdTipoRelacion" = 'REP'
			AND REL."IdEstado" = 'ACT'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
			ON REL."IdPersonaDestino" = REPR."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
			ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
			ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
		WHERE CLI."IdCliente" = :varIdCliente ;

		IF(varExisteReprCliente > 0) THEN
			SELECT 
				TOP 1 CASE WHEN COALESCE(REPR."DocumentoIdentidad",'') <> '' AND REPR."TipoDocumentoIdentidad" = 'R' THEN
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(REPR."DocumentoIdentidad",'-'),'i',0)||
						'-'||SUBSTR_AFTER(REPR."DocumentoIdentidad",'-')
						 ELSE COALESCE(REPR."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
							COALESCE(REPR."NombreCompleto",'-') AS "NombreCompleto",
						    COALESCE(DIR."NombreCiudad",'-') AS "Ciudad",
							COALESCE(REPR_DIR."Calle",'-')||' '||COALESCE(REPR_DIR."Numero",'-')||' '||COALESCE(REPR_DIR."Departamento",'-') AS "Direccion",
							COALESCE(REPR_TEL."Numero",'-')||' '||COALESCE(REPR_TEL."Anexo",'-') AS "Telefono",
							COALESCE(REPR."Tipo" ,'-')
				INTO varRutReprCliente,varNombreReprCliente, varCiudadReprCliente, varDireccionReprCliente, varTelefonoReprCliente, varTipoPersona
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
					ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
					ON PER."IdTelefonoPrincipal" = TEL."IdTelefono" AND PER."IdPersona" = TEL."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
					ON CLI."IdPersona" = PER."IdPersona"
				INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL
					ON REL."IdPersonaOrigen" = PER."IdPersona"
					AND "IdTipoRelacion" = 'REP'
					AND REL."IdEstado" = 'ACT'
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" REPR
					ON REL."IdPersonaDestino" = REPR."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" REPR_DIR
					ON REPR."IdDireccionFisicaPrincipal" = REPR_DIR."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" REPR_TEL
					ON REPR."IdTelefonoPrincipal" = REPR_TEL."IdTelefono" AND REPR."IdPersona" = REPR_TEL."IdPersona"
			    WHERE CLI."IdCliente" = :varIdCliente ORDER BY REPR."FechaInicioRepresentante" ;
		ELSE
				SELECT varNombreCliente, varRutCliente, varCiudadCliente, varDireccionCliente, varTelefonoCliente, varTipoPersonaCliente
				INTO varNombreReprCliente, varRutReprCliente, varCiudadReprCliente, varDireccionReprCliente, varTelefonoReprCliente, varTipoPersona
				FROM DUMMY;
		END IF;
		
		-- Valida que exista el parametro
		SELECT count(1)
			INTO varExisteParametro
			FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
			WHERE "CodigoParametro" = 'FWD.FechaFWDAdendumEsFechaFirma';
		
		-- Se obtiene parametro FechaFWDAdendumEsFechaFirma
		IF :varExisteParametro > 0 THEN
			SELECT "Valor"
				INTO varEsFechaFirma
				FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
				WHERE "CodigoParametro" = 'FWD.FechaFWDAdendumEsFechaFirma';
		ELSE varEsFechaFirma = 'N';
		END IF;
			
		-- Valida que existan fechas de firma para Contrato Marco FWD
		SELECT count(1)
			INTO varExisteFechaFirma
			FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
			INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdTipoDocumento"='CONTMARFWD' AND DOC."IdCliente"=:varIdCliente
			LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
			LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
			WHERE IFNULL(ANX."IdEstado",'PEN') = CASE 
													WHEN COALESCE(ANE."FechaFirma", '') = '' AND COALESCE(ANX."IdEstado", '') NOT IN ('', 'ACT') 
														AND COALESCE(ANX."FechaIngreso", '') <> '' 
														THEN ANX."IdEstado"
													ELSE 'ACT' 
												 END;
			
		-- Almacena fecha firma contrato marco FWD
		IF (:varEsFechaFirma = 'S') THEN
			IF (:varExisteFechaFirma > 0) THEN
				SELECT TOP 1 COALESCE(ANE."FechaFirma", UTCTOLOCAL(ANX."FechaIngreso", 'America/Santiago','platform') ) AS "FechaFirma", ANX."IdAnexo" AS "IdAnexo"
					INTO varFechaFirmaFWD, varDummy
					FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
					INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdTipoDocumento"='CONTMARFWD' AND DOC."IdCliente"=:varIdCliente
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
					WHERE IFNULL(ANX."IdEstado",'PEN') = CASE 
															WHEN COALESCE(ANE."FechaFirma", '') = '' AND COALESCE(ANX."IdEstado", '') NOT IN ('', 'ACT') 
																AND COALESCE(ANX."FechaIngreso", '') <> '' 
																THEN ANX."IdEstado"
															ELSE 'ACT' 
														 END
					ORDER BY "IdAnexo" DESC;
			ELSE 
				SELECT "FechaModificacion" AS "FechaContratoCondicionesForward"
				INTO varFechaFirmaFWD
				FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TI
				LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador" AND I."IdTipoEntidad" = 'CLIENTE' 
																	AND I."IdEntidad" = :varIdCliente AND I."ValorIndicador" = 'S'
				WHERE TI."CodigoTipoIndicador" = 'CCF';
			END IF;
		END IF;
	
		v_ContratoDatos =
				SELECT
					 CC."IdOperacion"					AS "IdOperacion"
					,CC."FolioCartera" 					AS "NroOperacion"
					,EMP."RazonSocial"					AS "NombreEmpresa"
					-- Calle Numero Departamento
					,COALESCE(EMP."DireccionFisicaPrincipalCalle", '-') || ' ' || COALESCE(EMP."DireccionFisicaPrincipalNumero", '-') || ' ' || COALESCE(EMP."DireccionFisicaPrincipalDepartamento", '-') AS "DireccionEmpresa"
					,EMP."DireccionFisicaPrincipalNombreCiudad" AS "CiudadEmpresa"
					,EMP."DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaEmpresa"
					,EMP."TelefonoPrincipal" AS "TelefonoEmpresa"
					--,EMP."DocumentoIdentidad"			AS "RutEmpresa"
					,CASE WHEN COALESCE(EMP."DocumentoIdentidad",'') <> '' AND EMP."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(EMP."DocumentoIdentidad",'-'),'i',0)||
							'-'||SUBSTR_AFTER(EMP."DocumentoIdentidad",'-')
						  ELSE COALESCE(EMP."DocumentoIdentidad",'-') END	AS "RutEmpresa"
					--,REPREMP."NombreCompleto"			AS "NombreReprEmpresa"
					,:varNombreReprEmpresa				AS "NombreReprEmpresa"
					--,REPREMP."DocumentoIdentidad"		AS "RutReprEmpresa"
					,:varRutReprEmpresa					AS "RutReprEmpresa"
					--,DIRREPEMP."NombreCiudad"			AS "CiudadReprEmpresa"
					,:varCiudadReprEmpresa				AS "CiudadReprEmpresa"
					--,DIRREPEMP."Calle"||' '||DIRREPEMP."Numero"||' '||DIRREPEMP."Departamento"	AS "DireccionReprEmpresa"
					,:varDireccionReprEmpresa			AS "DireccionReprEmpresa"
					,:varTelefonoReprEmpresa			AS "TelefonoReprEmpresa"
					,CLI."NombreCompleto" 				AS "NombreCliente"
					,DIRCLI."NombreCiudad"				AS "CiudadCliente"
					--,CLI."DocumentoIdentidad" 			AS "RutCliente"
					,CASE WHEN COALESCE(CLI."DocumentoIdentidad",'') <> '' AND CLI."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(CLI."DocumentoIdentidad",'-'),'i',0)||
							'-'||SUBSTR_AFTER(CLI."DocumentoIdentidad",'-')
						  ELSE COALESCE(CLI."DocumentoIdentidad",'-') END	AS "RutCliente"
					,COALESCE(DIRCLI."Calle",'-')||' '||COALESCE(DIRCLI."Numero",'-')||' '||COALESCE(DIRCLI."Departamento",'-') AS "DireccionCliente"
					,COALESCE(TELCLI."Numero",'-')||' '||COALESCE(TELCLI."Anexo",'-') AS "TelefonoCliente"
					,CASE WHEN :varTipoPersonaCliente = 'N' OR :varTipoPersona = '' THEN CLI."NombreCompleto" ELSE  :varNombreReprCliente END				AS "NombreReprCliente"
					,CASE WHEN :varTipoPersonaCliente = 'N' OR :varTipoPersona = '' THEN CASE WHEN COALESCE(CLI."DocumentoIdentidad",'') <> '' AND CLI."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(CLI."DocumentoIdentidad",'-'),'i',0)||
							'-'||SUBSTR_AFTER(CLI."DocumentoIdentidad",'-')
						  ELSE COALESCE(CLI."DocumentoIdentidad",'-') END ELSE  :varRutReprCliente END					AS "RutReprCliente"
					,:varCiudadReprCliente AS "CiudadReprCliente"
					,CASE WHEN :varTipoPersonaCliente = 'N' OR :varTipoPersona = ''
							THEN COALESCE(DIRCLI."Calle",'-')||' '||COALESCE(DIRCLI."Numero",'-')||' '||COALESCE(DIRCLI."Departamento",'-') 
							ELSE :varDireccionReprCliente END AS "DireccionReprCliente"
					,CASE WHEN :varTipoPersonaCliente = 'N' OR :varTipoPersona = ''
					      THEN COALESCE(TELCLI."Numero",'-')||' '||COALESCE(TELCLI."Anexo",'-')  
					      ELSE  :varTelefonoReprCliente	END		AS "TelefonoReprCliente"
					,:varNombreApoCliente1 AS "NombreApoCliente1"
					,:varRutApoCliente1 AS "RutApoCliente1"
					,:varCiudadApoCliente1 AS "CiudadApoCliente1"
					,:varDireccionApoCliente1 AS "DireccionApoCliente1"
					,:varTelefonoApoCliente1 AS "TelefonoApoCliente1"
					,:varNombreApoCliente2 AS "NombreApoCliente2"
					,:varRutApoCliente2  AS "RutApoCliente2"
					,:varCiudadApoCliente2  AS "CiudadApoCliente2"
					,:varDireccionApoCliente2 AS "DireccionApoCliente2"
					,:varTelefonoApoCliente2 AS "TelefonoApoCliente2"
					,CASE WHEN CC."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' 
						  WHEN CC."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END AS "TipoOperacion"
					,CC."IdTipoProducto" 				AS "Producto"
					,CC."IdInstrumento" 				AS "IdInstrumento"--"SubProducto"
					,CC."TipoInstrumento"				AS "TipoInstrumento"--"SubProducto"
					,CC."Nemotecnico"					AS "Nemotecnico"
					,CI."RazonSocial"					AS "NombreInstrumento"
					,CC."FechaOperacion"				AS "FechaInicio"
					,TO_VARCHAR(CC."FechaOperacion",'DD-MM-YYYY')	AS "FechaOperacionFormato"
					,CC."FechaVencimientoOperacion" 	AS "FechaVcto"
					,TO_VARCHAR(CC."FechaVencimientoOperacion",'DD-MM-YYYY')	AS "FechaVctoFormato"
					,CC."PlazoEnDiasOperacion"			AS "Plazo"
					,CC."IdMonedaPrincipal" 			AS "MonedaOperacion"
					,CC."IdMonedaSecundaria"			AS "MonedaConversion"
					,CC."MontoMonedaPrincipal" 			AS "MontoOperacion"
					,CASE WHEN CC."Nemotecnico" = 'ARBITR' THEN CC."ValorParidadCierre" ELSE CC."ValorTipoCambioCierre" END	AS "PrecioFDW"
					,CC."ValorParidadCosto"             AS "ValorParidadReferencial"
					,CC."ValorTipoCambioCosto"          AS "TipoCambioReferencial"
					,CC."MontoMonedaSecundaria"			AS "MontoFinal"
					,PA."NombreParametro"				AS "Modalidad"
					,CC."NombreEstado"					AS "Estado"
					,CC."FechaLiquidacion"				AS "FechaLiquidacion"
					,TO_VARCHAR(CC."FechaEfectiva", 'DD-MM-YYYY')			    AS "FechaFixing1"
					,TO_VARCHAR(CC."FechaFixing", 'DD-MM-YYYY')    AS "FechaFixingConfirmacion"
					,TO_VARCHAR(FLIQ."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacionFormato"
					,CC."IdTipoCanalIngreso" 			AS "Origen"	
					,:varDOLAR  						AS "Obs_Inicio" -- DolarObservado
					,:varUF 							AS "UF_Inicio"  -- ValorUF dia operacion
					,US."CodigoUsuario" 				AS "UsuarioCreacion"			
					,CC."EGRValorTipoCambioSpot" 		AS "TasaCambioSpot"
					,CC."INGMontoMonedaSecundaria" 		AS "MontoEquivalente"
					,PA1."NombreParametro"				AS "Fixing"
					,''									AS "Sistema" --Mercado
					,CC."NombreBolsa" 		 			AS "Bolsa"
					,:varNomApodCor1					AS "NombreApodradoCorredor1"
					,:varRutApodCor1					AS "RutApodradoCorredor1"
					,:varDirApodCor1					AS "DirApodradoCorredor1"
					,:varTelApodCor1					AS "TelApodradoCorredor1"
					,:varNombreCiudadApodCor1           AS "CiuApodradoCorredor1"
					,:varNomApodCor2					AS "NombreApodradoCorredor2"
					,:varRutApodCor2					AS "RutApodradoCorredor2"
					,:varDirApodCor2					AS "DirApodradoCorredor2"
					,:varTelApodCor2					AS "TelApodradoCorredor2"
					,:varNombreCiudadApodCor2           AS "CiuApodradoCorredor2"
                	,COALESCE(MONPRI."MontoDecimales",4)    AS "MontoDecimalesPrincipal"
                	,COALESCE(MONSEC."MontoDecimales",4)    AS "MontoDecimalesSecundaria"
                	,COALESCE(MONPRI."TipoCambioDecimales",4)    AS "TipoCambioDecimalesPrincipal"
                	,COALESCE(MONSEC."TipoCambioDecimales",4)    AS "TipoCambioDecimalesSecundaria"
					,COALESCE(MONPRI."CantidadDecimales",4)    AS "CantidadDecimalesPrincipal"
                	,COALESCE(MONSEC."CantidadDecimales",4)    AS "CantidadDecimalesSecundaria"
                	,COALESCE(MONSEC."ParidadDecimales",4) AS "ParidadDecimalesSecundaria"
					,COALESCE(MONDO."MontoDecimales",4)		AS "MontoDecimalesDO"
					,''									AS "Observaciones"
					,FPR."NombreFormaDePago" AS "NombreMedioDePagoRecibe"
					,FPE."NombreFormaDePago" AS "NombreMedioDePagoEntrega"
					,CASE
						WHEN :varEsFechaFirma = 'S' THEN TO_VARCHAR(:varFechaFirmaFWD,'DD-MM-YYYY')
						ELSE TO_VARCHAR(IFNULL(:varFECHA, '0001-01-01'),'DD-MM-YYYY')
						END AS "FechaContrato"
					,TO_VARCHAR(ConfFechSistema."Valor",'DD-MM-YYYY') AS "FechaSistema"
					,:varBancosReferencia               AS "BancosReferencia"
					,COALESCE(OPE."IdMonedaCompensacion",'') AS "MonedaCompensacion"
				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
					ON CC."IdEmpresa" = EMP."IdEmpresa"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
					ON CLI."IdCliente" = CC."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
					ON CC."IdUsuario" = US."IdUsuario"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" CI
					ON CC."IdInstrumento" = CI."IdInstrumento"
				LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA
					ON PA."IdParametro" = CC."IdModalidadCumplimiento"
				LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA1
					ON PA1."IdParametro" = CC."IdFixing"		
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
					ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = CC."IdMonedaPrincipal" AND MONPRI."IdTipoOperacion" = CI."Nemotecnico")
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
					ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = CC."IdMonedaSecundaria" AND MONSEC."IdTipoOperacion" = CI."Nemotecnico")
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
					ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO')
				LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPR
				    ON (FPR."IdOperacion" = CC."IdCartera" AND FPR."RecibePaga" = 'R')
				LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPE
				    ON (FPE."IdOperacion" = CC."IdCartera" AND FPE."RecibePaga" = 'P')
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERCLI
				    ON PERCLI."IdPersona" = CLI."IdPersona"    
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRCLI
				    ON PERCLI."IdDireccionFisicaPrincipal" = DIRCLI."IdDireccionFisica"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELCLI
				    ON TELCLI."IdTelefono" = PERCLI."IdTelefonoPrincipal"
				   AND TELCLI."IdPersona" = PERCLI."IdPersona"    
				LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" AS TipDocumento
				   ON TipDocumento."IdTipoDocumento" = 'CONTRATOCONDICIONESGRALES'
				 LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" AS Documento
				   ON Documento."IdTipoDocumento" = TipDocumento."IdTipoDocumento" 
				  AND Documento."IdCliente" = CC."IdCliente"
				 LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" AS Anexo
				   ON Anexo."IdDocumento" = Documento."IdDocumento" 
				  AND Anexo."IdAnexo" = Documento."IdUltimoAnexo" 
				  AND Anexo."IdEstado" != 'VCD'
				INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" ConfFechSistema 
				   ON ConfFechSistema."IdEmpresa" = EMP."IdEmpresa" 
				  and ConfFechSistema."CodigoParametro" = 'PLA.FechaSistema'  
				INNER JOIN (
					SELECT
		                 CC."IdCartera"
		                ,CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
		                      WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
		                      ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion") END AS "FechaLiquidacion"
		            FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CC
		            LEFT JOIN (
		                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
		                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
		                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
		                ) AS FPE
		                ON (FPE."IdOperacion" = CC."IdCartera" AND FPE."IdEmpresa" = CC."IdEmpresa")
		            LEFT JOIN (
		                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
		                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
		                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
		                ) AS FPR
		               ON (FPR."IdOperacion" = CC."IdCartera" AND FPR."IdEmpresa" = CC."IdEmpresa")
		            WHERE CC."IdTipoProducto"   = 'MEFW'
		              AND CC."IdCartera"        = :p_FolioOperacion
		              AND CC."IdEmpresa"        = :p_IdEmpresa
				) FLIQ ON (CC."IdCartera" = FLIQ."IdCartera")
				LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS OPE ON CC."IdCartera" = OPE."IdCartera"
				WHERE CC."IdTipoProducto" = 'MEFW'
				  AND CC."IdCartera"= :p_FolioOperacion
				  AND CC."IdEmpresa"= :p_IdEmpresa;
			
			v_ContratoSalida =
				SELECT 
					 *
					,(SELECT TOP 1 "Binario" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" 
					  WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC) AS "LogoCorredora"
				FROM :v_ContratoDatos
				;
		
		ELSE
			v_ContratoSalida =
				SELECT
					 0			AS "IdOperacion"
					,0 			AS "NroOperacion"
					,''			AS "NombreEmpresa"
					,''			AS "DireccionEmpresa"
					,''			AS "CiudadEmpresa"
					,''			AS "ComunaEmpresa"
					,''			as "TelefonoEmpresa"
					,''			AS "RutEmpresa"
					,''			AS "NombreReprEmpresa"
					,''			AS "RutReprEmpresa"
					,''			AS "CiudadReprEmpresa"
					,''			AS "DireccionReprEmpresa"
					,''			AS "TelefonoReprEmpresa"
					,''			AS "NombreCliente"
					,''			AS "CiudadCliente"
					,''			AS "RutCliente"
					,''         AS "DireccionCliente"
					,''         AS "TelefonoCliente"
					,''			AS "NombreReprCliente"					
					,''			AS "RutReprCliente"
					,''			AS "CiudadReprCliente"
					,''			AS "DireccionReprCliente"
					,''			AS "TelefonoReprCliente"
					,''			AS "NombreApoCliente1"					
					,''			AS "RutApoCliente1"
					,''			AS "CiudadApoCliente1"
					,''			AS "DireccionApoCliente1"
					,''			AS "TelefonoApoCliente1"
					,''			AS "NombreApoCliente2"					
					,''			AS "RutApoCliente2"
					,''			AS "CiudadApoCliente2"
					,''			AS "DireccionApoCliente2"
					,''			AS "TelefonoApoCliente2"
					,'' 		AS "TipoOperacion"--formula
					,''			AS "Producto"
					,0			AS "IdInstrumento"--"SubProducto"
					,''			AS "TipoInstrumento"
					,''			AS "Nemotecnico"
					,'' 		AS "NombreInstrumento"
					,''			AS "FechaInicio"
					,''			AS "FechaOperacionFormato"
					--,''			AS "HoraCreacion"
					,'' 		AS "FechaVcto"
					,''			AS "FechaVctoFormato"
					,0			AS "Plazo"
					,''			AS "MonedaOperacion"
					,''			AS "MonedaConversion"
					,0 			AS "MontoOperacion"
					,0			AS "PrecioFDW"	--depende del instrumento
					,0          AS "ValorParidadReferencial"
					,0          AS "TipoCambioReferencial"
					,0			AS "MontoFinal"
					,0			AS "Modalidad"
					,''			AS "Estado"
					,''			AS "FechaLiquidacion"
					,''			AS "FechaFixing1"
					,''    		AS "FechaFixingConfirmacion"
					,''			AS "FechaLiquidacionFormato"
					,'' 		AS "Origen"	
					,0 			AS "Obs_Inicio" -- DolarObservado
					,0			AS "UF_Inicio"  -- ValorUF dia operacion
					,'' 		AS "UsuarioCreacion"			
					,0 			AS "TasaCambioSpot"
					,0 			AS "MontoEquivalente"
					,0			AS "Fixing"
					,'' 		AS "Sistema" --Mercado
					,'' 		AS "Bolsa"
					,''			AS "NombreApodradoCorredor1"
					,''			AS "RutApodradoCorredor1"
					,''			AS "DirApodradoCorredor1"
					,''			AS "TelApodradoCorredor1"
					,''         AS "CiuApodradoCorredor1"
					,''			AS "NombreApodradoCorredor2"
					,''			AS "RutApodradoCorredor2"
					,''			AS "DirApodradoCorredor2"
					,''			AS "TelApodradoCorredor2"
					,''         AS "CiuApodradoCorredor2"
					,4			AS "MontoDecimalesPrincipal"
					,4			AS "MontoDecimalesSecundaria"
					,4 			AS "TipoCambioDecimalesPrincipal"
					,4			AS "TipoCambioDecimalesSecundaria"
					,4			AS "CantidadDecimalesPrincipal"
					,4			AS "CantidadDecimalesSecundaria"
					,4			AS "ParidadDecimalesSecundaria"
					,4			AS "MontoDecimalesDO"
					,CASE
						WHEN varExisteOp = 0 	THEN 'NO EXISTE OPERACIÓN'
						WHEN varExisteDolar = 0 THEN 'NO EXISTE DOLAR PARA LA FECHA DE OPERACION'
						WHEN varExisteUf = 0	THEN 'NO EXISTE UF PARA LA FECHA DE OPERACION'	--depende del instrumento
						WHEN varExisteNomApodCor1 = 0 THEN 'NO EXISTE NOMBRE DE APODERADO CONFIGURADO'
						ELSE 'ERROR AL OBTENER DATOS DE REPORTE CONTRATO' END AS "Observaciones"
					,'' 		AS "NombreMedioDePagoRecibe"
					,'' 		AS "NombreMedioDePagoEntrega"
					,''         AS "FechaContrato"
					,''         AS "FechaSistema"
					,''         AS "BancosReferencia"
					,''			AS "MonedaCompensacion"
					,(SELECT TOP 1 "Binario" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
					  WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC) AS "LogoCorredora"
				FROM DUMMY
				;
		END IF;
		
		--Salida del SP
		SELECT * FROM :v_ContratoSalida;
		
	END;
END;