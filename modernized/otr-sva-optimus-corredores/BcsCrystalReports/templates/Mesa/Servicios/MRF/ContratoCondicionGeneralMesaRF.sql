SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.ContratoCondicionGeneralMesaRF";
GO
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.ContratoCondicionGeneralMesaRF" ( 
    --VARIABLES OBLIGATORIAS
   	 IN p_IdEmpresa          	          	BIGINT       -- identificador de la empresa del contrato
    ,IN p_IdModulo                          NVARCHAR(3)  -- modulo del contrato
	,IN p_Fecha								DATE		 --Fecha Contrato
	,IN p_IdCliente							BIGINT
	,IN p_IdPersonaCorredora				BIGINT				/** Se usan los Id Persona para los apoderados! (Guz Here) **/
    ,IN p_IdApoderadoCorredoraPrimario      BIGINT
    ,IN p_IdApoderadoCorredoraSecundario    BIGINT
    ,IN p_IdApoderadoClientePrimario        BIGINT
    ,IN p_IdApoderadoClienteSecundario      BIGINT
    ,IN p_IdPais							BIGINT
    ,IN p_esFormaIndividual					NVARCHAR(3)
    ,IN p_esFormaConjunta					NVARCHAR(3)
    --Salidas
/*	,OUT p_out_AdvertenciasContador			INT
	,OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520))	
	,OUT p_out_ErroresContador 				INT
	,OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))*/
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
		
		/** Comprobar Que existe contrato **/
		--SELECT 'Contador Cliente', :v_Cliente FROM DUMMY;
		IF :v_Cliente > 0 THEN
			SELECT count(1) INTO v_Contrato FROM "BSCL.Mesa.BD::ContratoCondicionGeneral" WHERE "IdCliente" = :p_IdCliente AND "IdModulo" = :p_IdModulo;	
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
					"FechaIngreso"  AS "FechaSistema",
					TO_CHAR("FechaIngreso",'DD')||' de '||TO_CHAR("FechaIngreso",'MONTH')||' de '||TO_CHAR("FechaIngreso",'YYYY') AS "FechaSistemaTxt",
					:p_IdCliente AS "IdCliente",
					:p_esFormaIndividual AS "Individual",
					:p_esFormaConjunta	AS	"Conjunta"
				FROM "BSCL.Mesa.BD::ContratoCondicionGeneral"
				WHERE "IdCliente" = :p_IdCliente AND "IdModulo" = :p_IdModulo; 
					
		ELSE
			v_datoBaseContrato =
				SELECT 
					"FolioContrato" AS "IdOperacion",
					"FechaIngreso"  AS "FechaSistema",
					TO_CHAR("FechaIngreso",'DD')||' de '||TO_CHAR("FechaIngreso",'MONTH')||' de '||TO_CHAR("FechaIngreso",'YYYY') AS "FechaSistemaTxt",
					:p_IdCliente AS "IdCliente",
					:p_esFormaIndividual AS "Individual",
					:p_esFormaConjunta	AS	"Conjunta"
				FROM "BSCL.Mesa.BD::ContratoCondicionGeneral"
				WHERE "IdCliente" = :p_IdCliente  AND "IdModulo" = :p_IdModulo;
		END IF;
		




		/** VALIDAMOS QUE SE INSERTO LA INFORMACIÓN EN LA TABLA DE CONTRATOS **/
		--SELECT 'CONTRATOS',*  FROM "BSCL.Mesa.BD::ContratoCondicionGeneral";
		/** FIN VALIDACIÓN DE CONTRATOS NUEVOS EN LA TABLA DE CONTRATOS **/
	
	
		
		/** Vamos por los datos del cliente **/
		IF :v_Cliente > 0 THEN
			v_DatosClienteContrato =
				SELECT
					CASE WHEN CLIENTE."Tipo" = 'J' THEN CLIENTE."RazonSocial"
						 ELSE CLIENTE."NombreCompleto" END AS "RazonSocial",
					CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidad",
					CASE WHEN CLIENTE."Tipo" = 'J' THEN 
						 CASE WHEN CLIENTE."NombreValorIndicador" = 'BANCO' THEN 'S'
						 	  ELSE 'N' END
						 ELSE 'N' END AS "TipoClienteEsBanco",
					CLIENTE."NombreLargo"		 AS "DireccionCliente",
					CLIENTE."Direccion"			 AS "EmailCliente",
					CLIENTE."Numero"			AS "TelefonoCliente"
				FROM (
					SELECT 
							-- Datos de Personas
							"PER"."NombreCompleto",
							"PER"."Tipo",
							"PER"."RazonSocial",
							"PER"."DocumentoIdentidad",
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
							"PER"."DocumentoIdentidad",
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
								"PER"."DocumentoIdentidad",
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
								"PER"."DocumentoIdentidad",
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
								"PER"."DocumentoIdentidad",
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
								"PER"."DocumentoIdentidad",
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
		
		
		
		
		/*
		Select 'Base Contrato',* FROM :v_datoBaseContrato;
		Select 'Cliente',* FROM :v_DatosClienteContrato;
		Select 'Corredora',* FROM :v_DatosCorredoraContrato;
		Select 'APO 1 CORRE',* FROM :v_DatosApoderadoPrimarioCorredoraContrato;
		Select 'APO 2 CORRE',* FROM :v_DatosApoderadoSecundarioCorredoraContrato;
		Select 'APO 1 CLIENT',* FROM :v_DatosApoderadoPrimarioClienteContrato;
		Select 'APO 2 CLIENT',* FROM :v_DatosApoderadoSecundarioClienteContrato;*/
				 	
		Select		 	
			/*BC."IdOperacion" 				
			,CC."RazonSocial" 					
			,BC."IdCliente"					
			,CC."DocumentoIdentidad" 			
			,CCC."Corredora"					
			,CCC."DocumentoIdentidadCorredora" 	
			,CC."TipoClienteEsBanco"			
			,BC."FechaSistema"					
			,BC."FechaSistemaTxt"				
			,APCCC."ApoderadoCorredora1" 			
			,APCCC."DocIdenApoderadoCorredora1" 	
			,ASCCC."ApoderadoCorredora2" 			
			,ASCCC."DocIdenApoderadoCorredora2" 	
			,APCC."ApoderadoCliente1" 			
			,APCC."DocIdenApoderadoCliente1"		
			,ASCC."ApoderadoCliente2"			
			,ASCC."DocIdenApoderadoCliente2"*/
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
	
