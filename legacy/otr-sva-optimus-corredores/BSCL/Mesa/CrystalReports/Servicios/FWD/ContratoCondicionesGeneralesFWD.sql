SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FWD.ContratoCondicionesGeneralesFWD";

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FWD.ContratoCondicionesGeneralesFWD" ( 
    --VARIABLES OBLIGATORIAS
   	 IN p_IdEmpresa          	          	BIGINT       -- identificador de la empresa del contrato
    ,IN p_IdModulo                          NVARCHAR(3)  -- modulo del contrato
	,IN p_Fecha								DATE		 --Fecha Contrato
	,IN p_IdCliente							BIGINT
	,IN p_IdPortafolio						BIGINT
	,IN p_IdPersonaCorredora				BIGINT				/** Se usan los Id Persona para los apoderados! (Guz Here) **/
    ,IN p_IdApoderadoCorredoraPrimario      BIGINT
    ,IN p_IdApoderadoCorredoraSecundario    BIGINT
    ,IN p_IdApoderadoClientePrimario        BIGINT
    ,IN p_IdApoderadoClienteSecundario      BIGINT
    ,IN p_IdPais							BIGINT
    --Salidas
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	BEGIN

	
	BEGIN
		DECLARE v_Cliente   					INT;
		DECLARE v_Corredora 					INT;
		DECLARE v_ApoderadoPrimarioCliente 		INT;
		DECLARE v_ApoderadoSecundarioCliente 	INT;
		DECLARE v_ApoderadoPrimarioCorredora 	INT;
		DECLARE v_ApoderadoSecundarioCorredora 	INT;
		DECLARE v_Contrato 						INT;
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_DireccionPrimariaCLI NVARCHAR(1);
		DECLARE v_DireccionCliente     NVARCHAR(100);
		DECLARE v_DireccionPredefinida			INT;
		DECLARE v_EsFechaFirma			NVARCHAR(1);
		DECLARE v_ExisteParametro		INT;
		DECLARE v_FechaFirmaFWD			DATE;
		DECLARE v_ExisteFechaFirma		INT;
		DECLARE v_Dummy					INT;
		
	
		/** Validar Que datos vienen en null **/
			
		SELECT COALESCE(:p_IdCliente,0) INTO v_Cliente
		FROM DUMMY;
		SELECT COALESCE(:p_IdPersonaCorredora,0) INTO v_Corredora
		FROM DUMMY;
		SELECT COALESCE(:p_IdApoderadoClientePrimario,0) INTO v_ApoderadoPrimarioCliente
		FROM DUMMY;
		SELECT COALESCE(:p_IdApoderadoClienteSecundario,0) INTO v_ApoderadoSecundarioCliente
		FROM DUMMY;
		SELECT COALESCE(:p_IdApoderadoCorredoraPrimario,0) INTO v_ApoderadoPrimarioCorredora
		FROM DUMMY;
		SELECT COALESCE(:p_IdApoderadoCorredoraSecundario,0) INTO v_ApoderadoSecundarioCorredora
		FROM DUMMY;
		
		-- Valida que exista el parametro
		SELECT count(1)
			INTO v_ExisteParametro
			FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
			WHERE "CodigoParametro" = 'FWD.FechaCCGEsFechaFirma';
		
		-- Se obtiene parametro FechaCCGEsFechaFirma
		IF :v_ExisteParametro > 0 THEN
		SELECT "Valor"
			INTO v_EsFechaFirma
			FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
			WHERE "CodigoParametro" = 'FWD.FechaCCGEsFechaFirma';
		ELSE v_EsFechaFirma = 'N';
		END IF;
	
		-- Valida que existan fechas de firma para Contrato Marco FWD
		SELECT count(1)
			INTO v_ExisteFechaFirma
			FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
			LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdTipoDocumento"='CONTMARFWD' AND DOC."IdCliente"=:p_IdCliente
			LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
			LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
			WHERE IFNULL(ANX."IdEstado",'PEN') = 'ACT';
			
		-- Almacena fecha firma contrato marco FWD
		IF :v_EsFechaFirma = 'S' THEN
			IF :v_ExisteFechaFirma > 0 THEN
				SELECT TOP 1 ANE."FechaFirma" AS "FechaFirma", ANX."IdAnexo" AS "IdAnexo"
					INTO v_FechaFirmaFWD, v_Dummy
					FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdTipoDocumento"='CONTMARFWD' AND DOC."IdCliente"=:p_IdCliente
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
					WHERE IFNULL(ANX."IdEstado",'PEN') = 'ACT'
					ORDER BY "IdAnexo" DESC;
			ELSE 
				SELECT "Valor"
				INTO v_FechaFirmaFWD
				FROM  "BSCL.Plataforma.BD::PLA.Configuracion"
				WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
			END IF;
		END IF;
		
		/** Comprobar Que existe contrato **/
		--SELECT 'Contador Cliente', :v_Cliente FROM DUMMY;
		IF :v_Cliente > 0 THEN
			SELECT count(1) INTO v_Contrato FROM "BSCL.Mesa.BD::ContratoCondicionGeneral" WHERE "IdCliente" = :p_IdCliente  AND "IdModulo" = :p_IdModulo;	
		END IF;
		
		/** Obtener Logo  **/
		v_logo =	SELECT TOP 1
						"Binario" AS "LogoCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		/** Si no existe contrato, insertamos la información del nuevo contrato **/
		IF :v_Contrato = 0 THEN
			INSERT  INTO "BSCL.Mesa.BD::ContratoCondicionGeneral"
				  ("FolioContrato","IdCliente","FechaIngreso","IdEmpresa","IdModulo")
			VALUES("BSCL.Mesa.BD.Secuencias::FolioContrato".NEXTVAL,p_IdCliente,p_Fecha,p_IdEmpresa,p_IdModulo);
			
			/** Ahora a nuestra tabla resultado **/
			v_datoBaseContrato =
				SELECT 
					"FolioContrato" AS "IdOperacion",
					CASE
						WHEN :v_EsFechaFirma = 'S' THEN TO_VARCHAR(:v_FechaFirmaFWD,'DD-MM-YYYY')
						ELSE TO_VARCHAR(IFNULL("FechaIngreso", '0001-01-01'),'DD-MM-YYYY')
						END AS "FechaSistema",
					CASE
						WHEN :v_EsFechaFirma = 'S' THEN TO_CHAR(:v_FechaFirmaFWD,'DD')||' de '||TO_CHAR(:v_FechaFirmaFWD,'MONTH')||' de '||TO_CHAR(:v_FechaFirmaFWD,'YYYY')
						ELSE TO_CHAR(ConfFechSistema."Valor",'DD')||' de '||TO_CHAR(ConfFechSistema."Valor",'MONTH')||' de '||TO_CHAR(ConfFechSistema."Valor",'YYYY')
						END AS "FechaSistemaTxt",
					:p_IdCliente AS "IdCliente",
					'' AS "Individual",
					''	AS	"Conjunta"
				FROM "BSCL.Mesa.BD::ContratoCondicionGeneral"
				  INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" ConfFechSistema 
				   ON ConfFechSistema."IdEmpresa" = :p_IdEmpresa
				  AND ConfFechSistema."CodigoParametro" = 'PLA.FechaSistema'
				WHERE "IdCliente" = :p_IdCliente AND "IdModulo" = :p_IdModulo;
					
		ELSE
			v_datoBaseContrato =
				SELECT 
					"FolioContrato" AS "IdOperacion",
					CASE
						WHEN :v_EsFechaFirma = 'S' THEN TO_VARCHAR(:v_FechaFirmaFWD,'DD-MM-YYYY')
						ELSE TO_VARCHAR(IFNULL("FechaIngreso", '0001-01-01'),'DD-MM-YYYY')
						END AS "FechaSistema",
					CASE
						WHEN :v_EsFechaFirma = 'S' THEN TO_CHAR(:v_FechaFirmaFWD,'DD')||' de '||TO_CHAR(:v_FechaFirmaFWD,'MONTH')||' de '||TO_CHAR(:v_FechaFirmaFWD,'YYYY')
						ELSE TO_CHAR(ConfFechSistema."Valor",'DD')||' de '||TO_CHAR(ConfFechSistema."Valor",'MONTH')||' de '||TO_CHAR(ConfFechSistema."Valor",'YYYY')
						END AS "FechaSistemaTxt",
					:p_IdCliente AS "IdCliente",
					'' AS "Individual",
					''	AS	"Conjunta"
				FROM "BSCL.Mesa.BD::ContratoCondicionGeneral"
				  INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" ConfFechSistema 
				   ON ConfFechSistema."IdEmpresa" = :p_IdEmpresa
				  AND ConfFechSistema."CodigoParametro" = 'PLA.FechaSistema'
				WHERE "IdCliente" = :p_IdCliente  AND "IdModulo" = :p_IdModulo;
		END IF;
		




		/** VALIDAMOS QUE SE INSERTO LA INFORMACIÓN EN LA TABLA DE CONTRATOS **/
		--SELECT 'CONTRATOS',*  FROM "BSCL.Mesa.BD::ContratoCondicionGeneral";
		/** FIN VALIDACIÓN DE CONTRATOS NUEVOS EN LA TABLA DE CONTRATOS **/
	
	
		
		/** Vamos por los datos del cliente **/
		IF :v_Cliente > 0 THEN
		
			SELECT count(1) INTO v_DireccionPredefinida FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND WHERE IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA') AND "IdPortafolio"= :p_IdPortafolio;	
			
			IF :v_DireccionPredefinida > 0 THEN
				SELECT  IND."ValorIndicador"  INTO v_DireccionPrimariaCLI 
				FROM  "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
				WHERE IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA')
				AND "IdPortafolio"= :p_IdPortafolio;
			ELSE
				v_DireccionPrimariaCLI = 'N';
			END IF;
			
			
		
			if(v_DireccionPrimariaCLI='S') THEN 
			
					SELECT count(1) INTO v_DireccionPredefinida FROM 
					"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
					WHERE IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
					AND "IdPortafolio"= :p_IdPortafolio;
					
					IF :v_DireccionPredefinida > 0 THEN
			
						SELECT DIR."NombreLargo" INTO v_DireccionCliente
						FROM  "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
						WHERE IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
						AND "IdPortafolio"= :p_IdPortafolio;
					ELSE
						v_DireccionCliente = '';
					END IF;
			
			ELSE 
				
				SELECT count(1) INTO v_DireccionPredefinida FROM 
				"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
				INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
				LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdPersona" = REL."IdPersonaDestino"
				WHERE IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
				AND "IdPortafolio"= :p_IdPortafolio;
				
				IF :v_DireccionPredefinida > 0 THEN
						
					SELECT REL."Calle"||' '||REL."Numero"||'-'||REL."Ciudad" INTO v_DireccionCliente
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
					INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
					LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdPersona" = REL."IdPersonaDestino"
					WHERE IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
					AND "IdPortafolio"= :p_IdPortafolio;
				
				ELSE
					v_DireccionCliente = '';
				END IF;
				
			
			END IF;
		
			v_DatosClienteContrato =
				SELECT
					CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
						 ELSE CLIENTE."NombreCompleto" END AS "RazonSocial",
					CLIENTE."DocumentoIdentidad" 		   AS "DocumentoIdentidad",
					CASE WHEN CLIENTE."Tipo" = 'J' THEN 
						 CASE WHEN CLIENTE."NombreValorIndicador" = 'BANCO' THEN 'S'
						 	  ELSE 'N' END
						 ELSE 'N' END 			AS "TipoClienteEsBanco",
					CASE WHEN :v_DireccionCliente = '' THEN 
						"NombreLargo"
						ELSE :v_DireccionCliente END  AS "DireccionCliente",
					CLIENTE."Direccion"			AS "EmailCliente",
					CLIENTE."Numero"			AS "TelefonoCliente"
				FROM (
					SELECT 
							-- Datos de Personas
							"PER"."NombreCompleto",
							"PER"."Tipo",
							"PER"."RazonSocial",
							CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
							'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
							ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
							"CLI"."IdCliente",
							"PER"."IdPersona",
							 DF."NombreLargo",
							 DE."Direccion",
							 TEL."Numero",
							 IND."NombreValorIndicador"
					FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
					INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"   ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as "SOC" ON "CLI" ."IdCliente" = "SOC"."IdCliente"
				  	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS  "SEG" ON ( "SEG"."IdSegmentoCliente" = "CLI"."IdSegmentoCliente" )
					INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" as "CAT" on ("CAT"."IdCategoriaInversionista" ="CLI"."IdCategoriaInversionista" AND "CAT"."IdEmpresa" = "CLI"."IdEmpresa")  
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EMP"  ON("EMP"."IdPersona" = "CLI"."IdPersonaEjecutivo")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "TIPPER" ON ("TIPPER"."IdTipoPersona" = "PER"."Tipo")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "POR" ON ("POR"."IdPortafolio" = "CLI"."IdPortafolioDefecto")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERSO ON (PERSO."IdPersona" = "PER"."IdPersona")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DF ON (DF."IdPersona" = PERSO."IdPersona" AND DF."IdDireccionFisica" = PERSO."IdDireccionFisicaPrincipal")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DE ON (DE."IdPersona" = PERSO."IdPersona"  AND DE."IdDireccionElectronica"=PERSO."IdDireccionElectronicaPrincipal")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL ON (TEL."IdPersona" = PERSO."IdPersona" AND TEL."IdTelefono" =PERSO."IdTelefonoPrincipal")
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IND ON (IND."IdPersona" = PERSO."IdPersona" AND IND."IdTipoIndicador" = 5 )
					WHERE "CLI"."Borrado"='N' AND "CLI"."IdCliente" = :p_IdCliente) CLIENTE;
			
		END IF;
		
		/** Datos Corredora **/
		IF :v_Corredora > 0 THEN
			v_DatosCorredoraContrato =
				SELECT
					CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
						 ELSE CLIENTE."NombreCompleto" END AS "Corredora",
					CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidadCorredora",
					CLIENTE."NombreLargo"		 AS "DireccionCorredora",
					CLIENTE."Direccion"			 AS "EmailCorredora",
					CLIENTE."Numero"			AS "TelefonoCorredora"
				FROM (
					SELECT 
							-- Datos de Personas
							"PER"."NombreCompleto",
							"PER"."Tipo",
							"PER"."RazonSocial",
							CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
							'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
							ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
							"PER"."IdPersona",
							 DF."NombreLargo",
							 DE."Direccion",
							 TEL."Numero"
					FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERSO ON (PERSO."IdPersona" = "PER"."IdPersona")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DF ON (DF."IdPersona" = PERSO."IdPersona" AND DF."IdDireccionFisica" = PERSO."IdDireccionFisicaPrincipal")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DE ON (DE."IdPersona" = PERSO."IdPersona"  AND DE."IdDireccionElectronica"=PERSO."IdDireccionElectronicaPrincipal")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL ON (TEL."IdPersona" = PERSO."IdPersona" AND TEL."IdTelefono" =PERSO."IdTelefonoPrincipal")
					WHERE  "PER"."IdPersona" = :p_IdPersonaCorredora) CLIENTE;
	
		END IF;
		
		--SELECT 'DatosCorredora',* FROM :v_DatosCorredoraContrato;
		/**Apoderado Corredora 1 **/
		IF :v_ApoderadoPrimarioCorredora > 0 THEN
				v_DatosApoderadoPrimarioCorredoraContrato =
					SELECT
						CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
							 ELSE CLIENTE."NombreCompleto" END AS "ApoderadoCorredora1",
						CLIENTE."DocumentoIdentidad" AS "DocIdenApoderadoCorredora1"
					FROM (
						SELECT 
								-- Datos de Personas
								"PER"."NombreCompleto",
								"PER"."Tipo",
								"PER"."RazonSocial",
								CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
								"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
								'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
								ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
								"PER"."IdPersona"
						FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
						WHERE  "PER"."IdPersona" = :p_IdApoderadoCorredoraPrimario) CLIENTE;
		END IF;
	
	
		/** Apoderado Corredora 2 **/
		IF :v_ApoderadoSecundarioCorredora > 0 THEN
				v_DatosApoderadoSecundarioCorredoraContrato =
					SELECT
						CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
							 ELSE CLIENTE."NombreCompleto" END AS "ApoderadoCorredora2",
						CLIENTE."DocumentoIdentidad" AS "DocIdenApoderadoCorredora2"
					FROM (
						SELECT 
								-- Datos de Personas
								"PER"."NombreCompleto",
								"PER"."Tipo",
								"PER"."RazonSocial",
								CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
								"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
								'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
								ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
								"PER"."IdPersona"
						FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
						WHERE "PER"."IdPersona" = :p_IdApoderadoCorredoraSecundario) CLIENTE;
			
		 	ELSE
		 		v_DatosApoderadoSecundarioCorredoraContrato = 
		 			SELECT
		 				'' AS "ApoderadoCorredora2",
		 				'' AS "DocIdenApoderadoCorredora2"
		 			FROM DUMMY;
			
		END IF;
	
		/** Apoderado Cliente 1 **/
		IF :v_ApoderadoPrimarioCliente > 0 THEN
				v_DatosApoderadoPrimarioClienteContrato =
					SELECT
						CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
							 ELSE CLIENTE."NombreCompleto" END AS "ApoderadoCliente1",
						CLIENTE."DocumentoIdentidad" AS "DocIdenApoderadoCliente1"
					FROM (
						SELECT 
								-- Datos de Personas
								"PER"."NombreCompleto",
								"PER"."Tipo",
								"PER"."RazonSocial",
								CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
								"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
								'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
								ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
								"PER"."IdPersona"
						FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
						WHERE  "PER"."IdPersona" = :p_IdApoderadoClientePrimario) CLIENTE;
		ELSE
		 		v_DatosApoderadoPrimarioClienteContrato = 
		 			SELECT
		 				'' AS "ApoderadoCliente1",
		 				'' AS "DocIdenApoderadoCliente1"
		 			FROM DUMMY;
			

			
		END IF;
		
		/** Apoderado Cliente 2 **/
		IF :v_ApoderadoSecundarioCliente > 0 THEN
			
				v_DatosApoderadoSecundarioClienteContrato =
					SELECT
						CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
							 ELSE CLIENTE."NombreCompleto" END AS "ApoderadoCliente2",
						CLIENTE."DocumentoIdentidad" AS "DocIdenApoderadoCliente2"
					FROM (
						SELECT 
								-- Datos de Personas
								"PER"."NombreCompleto",
								"PER"."Tipo",
								"PER"."RazonSocial",
								CASE WHEN COALESCE("PER"."DocumentoIdentidad",'') <> '' AND "PER"."TipoDocumentoIdentidad" = 'R' THEN
								"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE("PER"."DocumentoIdentidad",'-'),'i',0)||
								'-'||SUBSTR_AFTER("PER"."DocumentoIdentidad",'-')
								ELSE COALESCE("PER"."DocumentoIdentidad",'-') END AS "DocumentoIdentidad",
								"PER"."IdPersona"
						FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
						WHERE "PER"."IdPersona" = :p_IdApoderadoClienteSecundario) CLIENTE;
		ELSE
		 		v_DatosApoderadoSecundarioClienteContrato = 
		 			SELECT
		 				'' AS "ApoderadoCliente2",
		 				'' AS "DocIdenApoderadoCliente2"
		 			FROM DUMMY;
			
		END IF;
		
				 	
		Select		 	
			*
		FROM  :v_datoBaseContrato BC		  
		LEFT JOIN :v_DatosClienteContrato AS CC ON (0=0)
		LEFT JOIN :v_DatosCorredoraContrato	AS CCC ON (0=0)
		LEFT JOIN :v_DatosApoderadoPrimarioCorredoraContrato AS APCCC ON (0=0)
		LEFT JOIN :v_DatosApoderadoSecundarioCorredoraContrato AS ASCCC ON (0=0)
		LEFT JOIN :v_DatosApoderadoPrimarioClienteContrato AS APCC ON (0=0)
		LEFT JOIN :v_DatosApoderadoSecundarioClienteContrato AS ASCC ON (0=0)
		CROSS JOIN :v_logo;				 
			          
			         
	END;
END;