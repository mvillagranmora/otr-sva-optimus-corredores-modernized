SET SCHEMA "BSCL";  
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline" ( 
	OUT IdSecuenciaGlobal BIGINT
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
  

	--------------------------
	-- Declaraci?3n de variables
	--------------------------  
	DECLARE v_Cond_CliConyRep, v_Cond_CliConyRepCony BIGINT DEFAULT 0;
	DECLARE v_CondRelaciones,v_CondConyuge,v_CondRepresentante,v_CondConyRepresentante BIGINT DEFAULT 0;
	DECLARE	v_CountRep BIGINT;  
	DECLARE v_CuentasPredeterminadas TABLE ("IdPersona" BIGINT, "IdCuentaBancaria" BIGINT, "IdEstado" NVARCHAR(3), "Borrado" NVARCHAR(1), "Predeterminado" NVARCHAR(1), "row_num" INT);
	DECLARE v_CuentasNoPredeterminadas TABLE ("IdPersona" BIGINT, "IdCuentaBancaria" BIGINT, "IdEstado" NVARCHAR(3), "Borrado" NVARCHAR(1), "Predeterminado" NVARCHAR(1), "row_num" INT);
	
	--------------------
	-- LOGICA
	--------------------

	CREATE LOCAL TEMPORARY TABLE #ClaseOrdenesPortafoliosPorIdCliente (
		"IdCliente" BIGINT,
		"ClaseOrden" NVARCHAR(100)
	);

	--Se buscan las clases de orden (Modalidad captaci¨®n) por portafolio y se incertan en tabla temporal 
	INSERT INTO #ClaseOrdenesPortafoliosPorIdCliente
	SELECT C."IdCliente",
			CASE 
				WHEN JSON_VALUE(I."ValorIndicador", '$.OE') = 'true' THEN 'OE'
				WHEN JSON_VALUE(I."ValorIndicador", '$.OV') = 'true' THEN 'OV'
				WHEN JSON_VALUE(I."ValorIndicador", '$.OD') = 'true' THEN 'OD'
				WHEN JSON_VALUE(I."ValorIndicador", '$.OR') = 'true' THEN 'OR'
				WHEN JSON_VALUE(I."ValorIndicador", '$.OM') = 'true' THEN 'OM'
				WHEN JSON_VALUE(I."ValorIndicador", '$.AC') = 'true' THEN 'TM'	
			   	ELSE '' 
			END AS "ClaseOrden"
	FROM "BSCL.SocioNegocio.BD::CLI.Cliente" C
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON C."IdCliente" = P."IdCliente"
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TP ON TP."IdTipoIndicador" = I."IdTipoIndicador" AND TP."IdEstado" = 'ACT'
	WHERE TP."CodigoTipoIndicador" IN ('ORDMODALIDAD') AND P."CodigoPortafolio" = '0'
	AND (I."ModeloCliente" IN (NULL, '', '0') OR C."IdTipoCliente" = I."ModeloCliente");
	-- fin de busquedas e inserciones

	--* almaceno en tabla global s?3lo la informaci?3n basica de clientes, luego se a?¡Àadir?? la info de sus relaciones (conyuge ,rep legal y conyuge rep legal)
	SELECT "BSCL.Normativa.BD.Secuencias::GL.SALIDA_CLIENTE_ONLINE".NEXTVAL INTO IdSecuenciaGlobal FROM dummy;
	
	v_propositosAdministracionCartera = SELECT "IdCliente",
									       CASE
									           WHEN MAX("ValorIndicador") = 'S' THEN 'S'
									           ELSE 'N'
									       END AS "ValorIndicador"
										FROM "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio"
										WHERE "NombreTipoIndicador" = 'Administración de Cartera'
										GROUP BY "IdCliente";
										
	INSERT INTO :v_CuentasPredeterminadas
		SELECT
			"CTA"."IdPersona",
			"CTA"."IdCuentaBancaria",
			"CTA"."IdEstado",
			"CTA"."Borrado",
			"CTA"."Predeterminado",
			ROW_NUMBER() OVER (PARTITION BY "CTA"."IdPersona" ORDER BY "CTA"."IdCuentaBancaria" DESC) AS "row_num"
		FROM
			"BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "CTA"
		WHERE
			"CTA"."IdEstado" = 'ACT' AND "CTA"."Borrado" = 'N' AND "CTA"."Predeterminado" = 'S';

	INSERT INTO :v_CuentasNoPredeterminadas
		SELECT
			"CTA"."IdPersona",
			"CTA"."IdCuentaBancaria",
			"CTA"."IdEstado",
			"CTA"."Borrado",
			"CTA"."Predeterminado",
			ROW_NUMBER() OVER (PARTITION BY "CTA"."IdPersona" ORDER BY "CTA"."IdCuentaBancaria" DESC) AS "row_num"
		FROM
			"BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "CTA"
		WHERE
			"CTA"."IdEstado" = 'ACT' AND "CTA"."Borrado" = 'N' AND "CTA"."Predeterminado" = 'N';

	v_ClaseOrden = SELECT "IdCliente", "ClaseOrden"
						FROM #ClaseOrdenesPortafoliosPorIdCliente
						GROUP BY "IdCliente", "ClaseOrden";
						
	v_CategoriaInversionista = SELECT "ValorAntiguo", "IdCliente", "FechaModificacion"
							FROM	(SELECT "LOGCATCLI"."ValorAntiguo","LOGCATCLI"."IdCliente",TO_NVARCHAR("LOGCATCLI"."FechaRegistro",'DD-MM-YYYY') AS "FechaModificacion",
										ROW_NUMBER () OVER (PARTITION BY "LOGCATCLI"."IdCliente" ORDER BY "LOGCATCLI"."FechaRegistro" DESC) AS "RN" 
							  			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCategoriaInversionista" AS "LOGCATCLI")
							WHERE "RN" = 1;

	v_clientes = SELECT	IdSecuenciaGlobal AS "IdClienteOnline",
			"CLIENTE"."IdCliente" 			AS "NumeroCliente",
			"CLIENTE"."IdPersona" 			AS "IdPersonaCli",
			COALESCE((LTRIM(RTRIM(SUBSTRING("PER"."DocumentoIdentidad",1,LENGTH("PER"."DocumentoIdentidad")-2)))),'') 
											AS "Rut",
			(LTRIM(RTRIM(SUBSTRING("PER"."DocumentoIdentidad",LENGTH("PER"."DocumentoIdentidad"),LENGTH("PER"."DocumentoIdentidad"))))) 
											AS "Dig",
			"PER"."DocumentoIdentidad" 		AS "Pasaporte",  
			"PER"."GlobalLegalEntity" 		AS "Glei",
			CASE 
				WHEN "PER"."Tipo" = 'J'	THEN ''
				ELSE "PER"."ApellidoPaterno"
			END	                            AS "ApellidoPaterno",
			CASE 
				WHEN "PER"."Tipo" = 'J'	THEN ''
				ELSE "PER"."ApellidoMaterno"
			END	                            AS "ApellidoMaterno",
			CASE 
				WHEN (SUBSTR_BEFORE(LTRIM(RTRIM("PER"."Nombres")),' ')) = '' THEN (LTRIM(RTRIM("PER"."Nombres"))) 
				ELSE SUBSTR_BEFORE(LTRIM(RTRIM("PER"."Nombres")),' ') 
			END							    AS "PrimerNombre",
	 		SUBSTR_AFTER((LTRIM(RTRIM("PER"."Nombres"))),' ')
		 									AS "SegundoNombre",
			"PER"."RazonSocial" 			AS "RazonSocial",		
			"TELCLI"."Numero" 				AS "Telefono",
			"MAILCLI"."Direccion"			AS "Email",
			COALESCE("DIRECLI"."DescripcionProposito", '') AS "DireccionTipo",
			"DIRECLI"."Calle" 				AS "DireccionNombre",
			"DIRECLI"."Numero" 				AS "DireccionNumero",
			"DIRECLI"."Departamento"		AS "DireccionDepto",
			"DIRECLI"."NombreDivisionPais"	AS "DireccionComuna",
			"DIRECLI"."NombreCiudad" 		AS "DireccionCiudad",
			COALESCE("CLASDIVPAIS"."NombreClasificacionDivisionPais", '') AS "DireccionEstado",
			"DIRECLI"."NombrePais"			AS "DireccionPais",
			"DIRECLI"."CodigoPostal"		AS "CodigoPostal",
			COALESCE("PAISCLI"."NombrePais",'') AS "Nacionalidad",
			CASE 
				WHEN "RELCORRCLI"."CodigoValorIndicador" = 'SOCIO' 		THEN 'SO'
				WHEN "RELCORRCLI"."CodigoValorIndicador" = 'MIEMBRO' 	THEN 'MG'
				WHEN "RELCORRCLI"."CodigoValorIndicador" = 'OTRA' 		THEN 'XX'
				WHEN "RELCORRCLI"."CodigoValorIndicador" = 'DIRECTOR' 	THEN 'DI'
				WHEN "RELCORRCLI"."CodigoValorIndicador" = 'TRABAJADOR' THEN 'TR'
				ELSE 'SR'  
			END	AS "RelacionIntermediario",
			CASE 
				WHEN "PROPOSITOPORTAFOLIOADMCARTERA"."ValorIndicador" = 'S'	THEN 'S'  
				ELSE 'N' 
			END								AS "AdministracionCartera",
			CASE 
				WHEN "PER"."Tipo" = 'J'	THEN ''
				ELSE COALESCE((LTRIM(RTRIM(SUBSTRING("DOCPEREMPL"."DocumentoIdentidad",1,LENGTH("DOCPEREMPL"."DocumentoIdentidad")-2)))),'') 
			END	AS "RutEntidadEmpleadora",
			CASE 
				WHEN "PER"."Tipo" = 'J'	THEN ''				
			    ELSE COALESCE((LTRIM(RTRIM(SUBSTRING("DOCPEREMPL"."DocumentoIdentidad",LENGTH("DOCPEREMPL"."DocumentoIdentidad"),LENGTH("DOCPEREMPL"."DocumentoIdentidad"))))),'') 
			END	AS "DigEntidadEmpleadora",
			CASE 
				WHEN "PER"."Tipo" = 'J'	THEN ''	
			    ELSE COALESCE("PEREMPL"."RazonSocial",'') 
			END	AS "RazonSocialEntidadEmpleadora",						
			CASE
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'CA' THEN 'C'
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'SO' THEN 'S'
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'DI' THEN 'D'
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'SE' THEN 'D'
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'VI' THEN 'V'
				WHEN "PER"."Tipo" = 'N' AND "PER"."EstadoCivil" = 'CC' THEN 'U'
				WHEN "PER"."Tipo" = 'J' THEN ''
				ELSE ''	
			END AS "EstadoCivil",
			CASE
				WHEN COALESCE("NumeroCuentaPredeterminada1"."NumeroCuentaBancaria",'') = '' THEN
				COALESCE("NumeroCuentaNoPredeterminada1"."NumeroCuentaBancaria",'')
				ELSE COALESCE("NumeroCuentaPredeterminada1"."NumeroCuentaBancaria",'')
			END AS 	"CuentaBancariaUno",
			CASE
				WHEN COALESCE("NumeroCuentaPredeterminada1"."NumeroCuentaBancaria",'') = '' THEN
				COALESCE("NumeroCuentaNoPredeterminada2"."NumeroCuentaBancaria",'')
				ELSE
					CASE
						WHEN COALESCE("NumeroCuentaPredeterminada2"."NumeroCuentaBancaria",'') = '' THEN
						COALESCE("NumeroCuentaNoPredeterminada1"."NumeroCuentaBancaria",'')
						ELSE COALESCE("NumeroCuentaPredeterminada2"."NumeroCuentaBancaria",'')
					END
			END AS "CuentaBancariaDos",
			CASE
				WHEN COALESCE("NumeroCuentaPredeterminada1"."NumeroCuentaBancaria",'') = '' THEN
				COALESCE("NumeroCuentaNoPredeterminada3"."NumeroCuentaBancaria",'')
				ELSE
					CASE
						WHEN COALESCE("NumeroCuentaPredeterminada2"."NumeroCuentaBancaria",'') = '' THEN
						COALESCE("NumeroCuentaNoPredeterminada2"."NumeroCuentaBancaria",'')
						ELSE
							CASE
								WHEN COALESCE("NumeroCuentaPredeterminada3"."NumeroCuentaBancaria",'') = '' THEN
								COALESCE("NumeroCuentaNoPredeterminada1"."NumeroCuentaBancaria",'')
								ELSE COALESCE("NumeroCuentaPredeterminada3"."NumeroCuentaBancaria",'')
							END
					END
			END AS "CuentaBancariaTres",	
			COALESCE("CLASEORDEN"."ClaseOrden", '') AS "ClaseOrden",
			"CATCLI"."NombreCategoriaInversionista" AS "Perfil",
			COALESCE("CATEGORIAINVERSIONISTALOG"."ValorAntiguo",'')	AS "PerfilAnterior",
			COALESCE("CATEGORIAINVERSIONISTALOG"."FechaModificacion",  TO_NVARCHAR("CLIENTE"."FechaCreacion",'DD-MM-YYYY')) AS "FechaInicioPerfilActual"
	FROM 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" 
			 INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIENTE" ON ( "PER"."IdPersona" = "CLIENTE"."IdPersona" )
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS "TELCLI" ON ("TELCLI"."IdTelefono" = "PER"."IdTelefonoPrincipal")
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "MAILCLI" ON ("MAILCLI"."IdDireccionElectronica" = "PER"."IdDireccionElectronicaPrincipal")
			 LEFT JOIN	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "DIRECLI" ON ("DIRECLI"."IdDireccionFisica" = "PER"."IdDireccionFisicaPrincipal")
			 LEFT JOIN 	"BSCL.Plataforma.BD::REG.Pais" AS "PAISCLI" ON ("PAISCLI"."IdPais" = "PER"."IdPaisNacionalidadPrincipal")
		     LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS "RELCORRCLI" ON ("RELCORRCLI"."IdCliente" = "CLIENTE"."IdCliente" AND "RELCORRCLI"."CodigoTipoIndicador" = 'NCG380')
			 LEFT JOIN  "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS "CATCLI" ON ("CATCLI"."IdCategoriaInversionista" ="CLIENTE"."IdCategoriaInversionista" AND "CLIENTE"."IdEmpresa" = "CATCLI"."IdEmpresa")
			 LEFT JOIN 	"BSCL.SocioNegocio.BD::PER.Persona" AS "PEREMPL" ON ("PEREMPL"."IdPersona" = "PER"."IdPersonaEmpleador")
			 LEFT JOIN 	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOCPEREMPL"  ON ("DOCPEREMPL"."IdDocumentoIdentidad" = "PEREMPL"."IdDocumentoIdentidad")
			 LEFT JOIN	:v_propositosAdministracionCartera AS "PROPOSITOPORTAFOLIOADMCARTERA" ON ("PROPOSITOPORTAFOLIOADMCARTERA"."IdCliente" = "CLIENTE"."IdCliente")
			 LEFT JOIN :v_CuentasPredeterminadas AS "CuentaPredeterminada1" ON ("CuentaPredeterminada1"."IdPersona" = "PER"."IdPersona" AND "CuentaPredeterminada1"."row_num" = 1)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaPredeterminada1" ON ("NumeroCuentaPredeterminada1"."IdCuentaBancaria" =  "CuentaPredeterminada1"."IdCuentaBancaria")
			 LEFT JOIN :v_CuentasPredeterminadas AS "CuentaPredeterminada2" ON ("CuentaPredeterminada2"."IdPersona" = "PER"."IdPersona" AND "CuentaPredeterminada2"."row_num" = 2)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaPredeterminada2" ON ("NumeroCuentaPredeterminada2"."IdCuentaBancaria" =  "CuentaPredeterminada2"."IdCuentaBancaria")
			 LEFT JOIN :v_CuentasPredeterminadas AS "CuentaPredeterminada3" ON ("CuentaPredeterminada3"."IdPersona" = "PER"."IdPersona" AND "CuentaPredeterminada3"."row_num" = 3)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaPredeterminada3" ON ("NumeroCuentaPredeterminada3"."IdCuentaBancaria" =  "CuentaPredeterminada3"."IdCuentaBancaria")
			 LEFT JOIN :v_CuentasNoPredeterminadas AS "CuentaNoPredeterminada1" ON ("CuentaNoPredeterminada1"."IdPersona" = "PER"."IdPersona" AND "CuentaNoPredeterminada1"."row_num" = 1)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaNoPredeterminada1" ON ("NumeroCuentaNoPredeterminada1"."IdCuentaBancaria" =  "CuentaNoPredeterminada1"."IdCuentaBancaria")
			 LEFT JOIN :v_CuentasNoPredeterminadas AS "CuentaNoPredeterminada2" ON ("CuentaNoPredeterminada2"."IdPersona" = "PER"."IdPersona" AND "CuentaNoPredeterminada2"."row_num" = 2)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaNoPredeterminada2" ON ("NumeroCuentaNoPredeterminada2"."IdCuentaBancaria" =  "CuentaNoPredeterminada2"."IdCuentaBancaria")
			 LEFT JOIN :v_CuentasNoPredeterminadas AS "CuentaNoPredeterminada3" ON ("CuentaNoPredeterminada3"."IdPersona" = "PER"."IdPersona" AND "CuentaNoPredeterminada3"."row_num" = 3)
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS "NumeroCuentaNoPredeterminada3" ON ("NumeroCuentaNoPredeterminada3"."IdCuentaBancaria" =  "CuentaNoPredeterminada3"."IdCuentaBancaria")		 
			 LEFT JOIN :v_ClaseOrden AS "CLASEORDEN" ON ("CLASEORDEN"."IdCliente" = "CLIENTE"."IdCliente")
			 LEFT JOIN :v_CategoriaInversionista AS "CATEGORIAINVERSIONISTALOG" ON ("CATEGORIAINVERSIONISTALOG"."IdCliente" = "CLIENTE"."IdCliente")
			 LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS "DIVPAIS" ON "DIVPAIS"."NombreDivisionPais" = "DIRECLI"."NombreDivisionPais" AND "DIVPAIS"."IdPais" = "DIRECLI"."IdPais"
			 LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS "CLASDIVPAIS" ON "CLASDIVPAIS"."IdClasificacionDivisionPais" = "DIVPAIS"."IdClasificacion1";
			
	DROP TABLE #ClaseOrdenesPortafoliosPorIdCliente;
	--SELECT * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE";
	--SELECT * FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
	
	--* Conyuge
	SELECT 	COUNT(1) INTO v_Cond_CliConyRep 
	FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
	WHERE PER."IdPersonaConyuge" <> 0 AND PER."Tipo" = 'N' AND PER."EstadoCivil" <> 'SO';
	
	--* Representante Legal
	
	SELECT 	COUNT(1) INTO v_CountRep
	FROM 	"BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
	WHERE 	"Administracion" = 'PERSONA' AND "IdTipoRelacion" ='REP';
	
	
	
	IF v_Cond_CliConyRep > 0 OR v_CountRep > 0  THEN
		v_cliente_conyuge_rep = 
			SELECT	CLI."IdCliente", PER."IdPersonaConyuge", PER."IdPersonaRepresentante" AS "IdPersonaRepresentante" 
			FROM	"BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
			WHERE	PER."IdPersonaConyuge" <> 0 AND PER."Tipo" = 'N' AND PER."EstadoCivil" <> 'SO'
			--------------------------------------------------------------------------
			UNION ALL
			--------------------------------------------------------------------------		
			
			SELECT 	CLI."IdCliente" AS "IdCliente", 0 AS "IdPersonaConyuge", REL."IdPersonaDestino" AS "IdPersonaRepresentante" 
			FROM 	"BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" REL
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON REL."IdPersonaOrigen" = CLI."IdPersona"
			WHERE 	"Administracion" = 'PERSONA' AND "IdTipoRelacion" ='REP';
			
	ELSE
		v_cliente_conyuge_rep = 
			SELECT 	0 AS "IdCliente", 0 AS "IdPersonaConyuge", 0 AS "IdPersonaRepresentante" 
			FROM 	DUMMY
			WHERE 	1 = 2;
	END IF;


	--busco si existen representantes con conyuge
	SELECT	COUNT(1) INTO v_Cond_CliConyRepCony  
	FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "CP" 
	INNER JOIN :v_cliente_conyuge_rep AS "REP" ON ("REP"."IdPersonaRepresentante" = "CP"."IdPersona")
	WHERE 	"CP"."IdPersonaConyuge" <> 0;
	
	IF(v_Cond_CliConyRepCony>0)	THEN
	
		v_cliente_conyuge_repcony =
			SELECT	"REP"."IdCliente" 				AS "IdCliente"
					,"REP"."IdPersonaConyuge" 		AS "IdPersonaConyuge"
					,"REP"."IdPersonaRepresentante" AS "IdPersonaRepresentante"
					,"CP"."IdPersonaConyuge"		AS "IdConyugeRepresentante" 
			FROM 	"BSCL.SocioNegocio.BD::PER.Persona" AS "CP" 
					INNER JOIN :v_cliente_conyuge_rep AS "REP" ON ("REP"."IdPersonaRepresentante" = "CP"."IdPersona")
			WHERE 	"CP"."IdPersonaConyuge" <> 0;
	
	ELSE
	
		v_cliente_conyuge_repcony =
			SELECT	0 AS "IdCliente"
					,0 AS "IdPersonaConyuge"
					,0 AS "IdPersonaRepresentante"
					,0 AS "IdConyugeRepresentante" 
			FROM 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "CP" 
					INNER JOIN :v_cliente_conyuge_rep AS "REP" ON  "REP"."IdPersonaRepresentante" = "CP"."IdPersona";
	
	END IF;
	 
	SELECT	COUNT(1) INTO v_CondRelaciones 
	FROM 	:v_cliente_conyuge_rep AS "CR"
			LEFT JOIN :v_cliente_conyuge_repcony "CRC" ON ("CRC"."IdCliente" = "CR"."IdCliente");
	 
	IF	(v_CondRelaciones>0) THEN
	 
		v_TablaRelaciones = 
			SELECT	"CR"."IdCliente" AS "IdCliente"
					,"CR"."IdPersonaConyuge" AS "IdPersonaConyuge"
					,"CR"."IdPersonaRepresentante" AS "IdPersonaRepresentante"
					,COALESCE("CRC"."IdConyugeRepresentante",0) AS "IdConyugeRepresentante"
			FROM 	:v_cliente_conyuge_rep AS "CR"
					LEFT JOIN :v_cliente_conyuge_repcony "CRC" ON ("CRC"."IdCliente" = "CR"."IdCliente");

	ELSE
	
		v_TablaRelaciones = 
			SELECT	"CR"."IdCliente" AS "IdCliente"
					,"CR"."IdPersonaConyuge" AS "IdPersonaConyuge"
					,"CR"."IdPersonaRepresentante" AS "IdPersonaRepresentante"
					,0 AS "IdConyugeRepresentante"
			FROM 	:v_cliente_conyuge_rep AS "CR";
		
	END IF;
	
	
	
	--pregunto si en tabla relaciones existe info de conyuge
	SELECT	COUNT(1) INTO v_CondConyuge
	FROM 	:v_TablaRelaciones 
	WHERE 	"IdPersonaConyuge" <> 0;
	
	IF (v_CondConyuge>0) THEN
		v_TablaConyuge =
			SELECT	"PERCONY"."IdPersona" AS "ConyIdPersona",
				   	CASE WHEN "DOCCONY"."IdTipoDocumentoIdentidad" = 'R' 
	   			 		THEN LTRIM(RTRIM(SUBSTRING("DOCCONY"."DocumentoIdentidad",1,LENGTH("DOCCONY"."DocumentoIdentidad")-2)))
						ELSE '' 
					END  AS "ConyRut",
					CASE WHEN "DOCCONY"."IdTipoDocumentoIdentidad" = 'R' 
						THEN LTRIM(RTRIM(SUBSTRING("DOCCONY"."DocumentoIdentidad",LENGTH("DOCCONY"."DocumentoIdentidad"),LENGTH("DOCCONY"."DocumentoIdentidad"))))
						ELSE '' 
					END AS "ConyDig",
					CASE WHEN "DOCCONY"."IdTipoDocumentoIdentidad" = 'P' 
						THEN "DOCCONY"."DocumentoIdentidad"  
						ELSE '' 
					END AS "ConyPasaporte",
					"PERCONY"."ApellidoPaterno" AS "ConyApellidoPaterno",
					"PERCONY"."ApellidoMaterno" AS "ConyApellidoMaterno",
					CASE 
						WHEN (SUBSTR_BEFORE(LTRIM(RTRIM("PERCONY"."Nombres")),' ')) = '' THEN (LTRIM(RTRIM("PERCONY"."Nombres"))) 
						ELSE SUBSTR_BEFORE(LTRIM(RTRIM("PERCONY"."Nombres")),	' ') 
						END AS "ConyPrimerNombre",
				    SUBSTR_AFTER((LTRIM(RTRIM("PERCONY"."Nombres"))),' ') AS "ConySegundoNombre",
					CASE 
						WHEN "PERCONY"."Tipo" = 'J' 
						THEN "PERCONY"."RazonSocial" 
						ELSE '' 
					END AS "ConyRazonSocial",
					"TEL"."Numero" AS "ConyTelefono" ,
					"DIRE"."Direccion" AS "ConyEmail",
					COALESCE("DIRECONY"."DescripcionProposito", '') AS "ConyTipoDire",
					"DIRECONY"."Calle" 					AS "ConyNombre",
					"DIRECONY"."Numero" 				AS "ConyNumero",
					"DIRECONY"."Departamento" 			AS "ConyDepto",
					"DIRECONY"."NombreDivisionPais" 	AS "ConyComuna",
					"DIRECONY"."NombreCiudad" 			AS "ConyCiudad",
					COALESCE("CLASDIVPAIS"."NombreClasificacionDivisionPais", '') AS "ConyEstadoProvincia",
					"DIRECONY"."NombrePais" 			AS "ConyPais",
					"DIRECONY"."CodigoPostal" 			AS "ConyCodigoPostal",
					"PAISNAC"."NombrePais"	AS "ConyNacionalidad",
					 CASE 
						WHEN "RELCORRCONY"."CodigoValorIndicador" = 'SOCIO' 		THEN 'SO'
						WHEN "RELCORRCONY"."CodigoValorIndicador" = 'MIEMBRO' 		THEN 'MG'
						WHEN "RELCORRCONY"."CodigoValorIndicador" = 'OTRA' 			THEN 'XX'
						WHEN "RELCORRCONY"."CodigoValorIndicador" = 'DIRECTOR' 		THEN 'DI'
						WHEN "RELCORRCONY"."CodigoValorIndicador" = 'TRABAJADOR' 	THEN 'TR'
						ELSE 'SR'  
					END	 								AS "ConyRelacionIntermediario",
					1									AS "Condicion",
					"TR"."IdCliente"	
			FROM 	:v_TablaRelaciones AS "TR"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERCONY"  ON ("PERCONY"."IdPersona" = "TR"."IdPersonaConyuge")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "DIRECONY" ON ("DIRECONY"."IdDireccionFisica" = "PERCONY"."IdDireccionFisicaPrincipal")
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS "RELCORRCONY" ON ("RELCORRCONY"."IdPersona" = "PERCONY"."IdPersona" AND "RELCORRCONY"."CodigoTipoIndicador" = 'NCG380PER')
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOCCONY" ON "DOCCONY"."IdDocumentoIdentidad" = "PERCONY"."IdDocumentoIdentidad"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS "TEL" ON "TEL"."IdTelefono" = "PERCONY"."IdTelefonoPrincipal"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "DIRE" ON "DIRE"."IdDireccionElectronica" = "PERCONY"."IdDireccionElectronicaPrincipal"
					LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAISNAC" ON "PAISNAC"."IdPais" = "PERCONY"."IdPaisNacimiento"
					LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS "DIVPAIS" ON "DIVPAIS"."NombreDivisionPais" = "DIRECONY"."NombreDivisionPais" AND "DIVPAIS"."IdPais" = "DIRECONY"."IdPais"
					LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS "CLASDIVPAIS" ON "CLASDIVPAIS"."IdClasificacionDivisionPais" = "DIVPAIS"."IdClasificacion1";
	ELSE 

		v_TablaConyuge =
			SELECT	0 AS "ConyIdPersona",
					'' AS "ConyRut",	
					'' AS "ConyDig",
					'' AS "ConyPasaporte",
					'' AS "ConyApellidoPaterno",
					'' AS "ConyApellidoMaterno",
					'' AS "ConyPrimerNombre",
				    '' AS "ConySegundoNombre",
					'' AS "ConyRazonSocial",
					'' AS "ConyTelefono" ,
					'' AS "ConyEmail",
					'' AS "ConyTipoDire",
					'' AS "ConyNombre",
					'' AS "ConyNumero",
					'' AS "ConyDepto",
					'' AS "ConyComuna",
					'' AS "ConyCiudad",
					'' AS "ConyEstadoProvincia",
					'' AS "ConyPais",
					'' AS "ConyCodigoPostal",
					'' AS "ConyNacionalidad",
					'' AS "ConyRelacionIntermediario",
					0  AS "Condicion",
					0 AS "IdCliente"
			FROM 	DUMMY;

	END IF;

	
	--pregunto si en tabla relaciones existe info de representante legal
	SELECT	COUNT(1) INTO v_CondRepresentante
	FROM 	:v_TablaRelaciones 
	WHERE 	"IdPersonaRepresentante" <> 0;

	IF (v_CondRepresentante>0) THEN

		v_TablaRepresentante = 
			SELECT 	"TR"."IdPersonaRepresentante"											AS "IdPersonaRepresentante",
					CASE 
						WHEN "DOC"."IdTipoDocumentoIdentidad" = 'R' THEN LTRIM(RTRIM(SUBSTRING("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2)))
						ELSE '' 
					END AS "RepRut",
					CASE 
						WHEN "DOC"."IdTipoDocumentoIdentidad" = 'R' THEN LTRIM(RTRIM(SUBSTRING("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),	LENGTH("DOC"."DocumentoIdentidad"))))
						ELSE '' 
					END AS "RepDig",
					CASE 
						WHEN "DOC"."IdTipoDocumentoIdentidad" = 'P' THEN "DOC"."DocumentoIdentidad"
						ELSE '' 
					END	AS "RepPasaporte",
					"REP"."GlobalLegalEntity" 												AS "RepGlobalLegalEntity",
					 "REP"."ApellidoPaterno" 												AS "RepApellidoPaterno",
					 "REP"."ApellidoMaterno" 												AS "RepApellidoMaterno",
					 CASE WHEN (SUBSTR_BEFORE(LTRIM(RTRIM("REP"."Nombres")),
					' ')) = '' 
					THEN (LTRIM(RTRIM("REP"."Nombres"))) 
					ELSE SUBSTR_BEFORE(LTRIM(RTRIM("REP"."Nombres")),
					' ') 
					END 																	AS "RepPrimerNombre",
				    SUBSTR_AFTER((LTRIM(RTRIM("REP"."Nombres"))),' ') 						AS "RepSegundoNombre",
					CASE WHEN "REP"."Tipo" = 'J' 
					THEN "REP"."RazonSocial" 
					ELSE '' 
					END 																	AS "RepRazonSocial",
					"TEL"."Numero"															AS "RepTelefono",
					COALESCE("DIRELEC"."Direccion", '')										AS "RepEmail",
					COALESCE("DIREREP"."DescripcionProposito", '')							AS "RepTipoDire",
					"DIREREP"."Calle" 														AS "RepNombre",
					"DIREREP"."Numero" 														AS "RepNumero",
					"DIREREP"."Departamento" 												AS "RepDepto",
					"DIREREP"."NombreDivisionPais" 											AS "RepComuna",
					"DIREREP"."NombreCiudad" 												AS "RepCiudad",
					COALESCE("CLASDIVPAIS"."NombreClasificacionDivisionPais", '')			AS "RepEstadoProvincia",
					 "DIREREP"."NombrePais" 												AS "RepPais",
					"DIREREP"."CodigoPostal" 												AS "RepCodigoPostal",
					"PAISNAC"."NombrePais"													AS "RepNacionalidad",
					 "REP"."CodigoTipoCargoEmpleado" 										AS "RepRelacionIntermediario",
					CASE
					WHEN "REP"."EstadoCivil" = 'CA' THEN 'C'
					WHEN "REP"."EstadoCivil" = 'SO' THEN 'S'
					WHEN "REP"."EstadoCivil" = 'DI' THEN 'D'
					WHEN "REP"."EstadoCivil" = 'SE' THEN 'D'
					WHEN "REP"."EstadoCivil" = 'VI' THEN 'V'
					WHEN "REP"."EstadoCivil" = 'CC' THEN 'U'
					ELSE ''	
					END
																							AS "RepEstadoCivil",
					1																		AS "Condicion",
					"TR"."IdCliente"
			FROM 	:v_TablaRelaciones AS "TR"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "REP" ON ("REP"."IdPersona" = "TR"."IdPersonaRepresentante") 
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "DIREREP" ON ("DIREREP"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal")
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS "RELCORRREP" ON ("RELCORRREP"."IdPersona" = "REP"."IdPersona" AND "RELCORRREP"."CodigoTipoIndicador" = 'NCG380PER')
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC" ON "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS "TEL" ON "TEL"."IdTelefono" = "REP"."IdTelefonoPrincipal"
					LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAISNAC" ON "PAISNAC"."IdPais" = "REP"."IdPaisNacionalidadPrincipal"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "DIRELEC" ON "DIRELEC"."IdDireccionElectronica" = "REP"."IdDireccionElectronicaPrincipal"					
					LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS "DIVPAIS" ON "DIVPAIS"."NombreDivisionPais" = "DIREREP"."NombreDivisionPais" AND "DIVPAIS"."IdPais" = "DIREREP"."IdPais"
					LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS "CLASDIVPAIS" ON "CLASDIVPAIS"."IdClasificacionDivisionPais" = "DIVPAIS"."IdClasificacion1";
	
	ELSE
	
		v_TablaRepresentante = 
			SELECT	0  AS "IdPersonaRepresentante",
					'' AS "RepRut",
					'' AS "RepDig",
					'' AS "RepPasaporte",
					'' AS "RepGlobalLegalEntity",
					'' AS "RepApellidoPaterno",
					'' AS "RepApellidoMaterno",
					'' AS "RepPrimerNombre",
					'' AS "RepSegundoNombre",
					'' AS "RepRazonSocial",
					'' AS "RepTelefono" ,
					'' AS "RepEmail",
					'' AS "RepTipoDire",
					'' AS "RepNombre",
					'' AS "RepNumero",
					'' AS "RepDepto",
					'' AS "RepComuna",
					'' AS "RepCiudad",
					'' AS "RepEstadoProvincia",
					'' AS "RepPais",
					'' AS "RepCodigoPostal",
					'' AS "RepNacionalidad",
					'' AS "RepRelacionIntermediario",
					'' AS "RepEstadoCivil",
					0  AS "Condicion",
					0 AS "IdCliente"
			FROM 	DUMMY;

	END IF;

	
	--pregunto si en tabla relaciones existe info de representante legal
	SELECT 	COUNT(1) INTO v_CondConyRepresentante
	FROM 	:v_TablaRelaciones 
	WHERE 	"IdConyugeRepresentante" <> 0;


	IF ( v_CondConyRepresentante > 0 ) THEN

		v_TablaConyRepresentante = 
		SELECT 	"TR"."IdConyugeRepresentante" 											AS "IdConyugeRepresentante",
				CASE WHEN "DOC"."IdTipoDocumentoIdentidad" = 'R' 
					THEN LTRIM(RTRIM(SUBSTRING("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2)))
				 	ELSE '' 
				 END																	AS "ConyRepRut",
				CASE WHEN "DOC"."IdTipoDocumentoIdentidad" = 'R' 
					THEN LTRIM(RTRIM(SUBSTRING("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"), LENGTH("DOC"."DocumentoIdentidad")))) 
					ELSE '' 
				END																		AS "ConyRepDig",
				CASE WHEN "DOC"."IdTipoDocumentoIdentidad" = 'P' 
					THEN "DOC"."DocumentoIdentidad" 		
					ELSE '' 
				END																		AS "ConyRepPasaporte",
				"CONYREP"."ApellidoPaterno" 											AS "ConyRepApellidoPaterno",
				"CONYREP"."ApellidoMaterno" 											AS "ConyRepApellidoMaterno",
				CASE WHEN (SUBSTR_BEFORE(LTRIM(RTRIM("CONYREP"."Nombres")),' ')) = ''
					THEN (LTRIM(RTRIM("CONYREP"."Nombres"))) 
					ELSE SUBSTR_BEFORE(LTRIM(RTRIM("CONYREP"."Nombres")),' ') 
				END 																	AS "ConyRepPrimerNombre",
				SUBSTR_AFTER((LTRIM(RTRIM("CONYREP"."Nombres"))),' ') 					AS "ConyRepSegundoNombre",
				CASE WHEN "CONYREP"."Tipo" = 'J' 
					THEN "CONYREP"."RazonSocial" 
					ELSE '' 
				END 																	AS "ConyRepRazonSocial",
				"TEL"."Numero" 															AS "ConyRepTelefono",
				"DIRE"."Direccion"														AS "ConyRepEmail",
				 -- Comienzo de Campos Asociados a la Direcci?3n Fisica del Cliente
				COALESCE("DIRECONYREP"."DescripcionProposito", '')						AS "ConyRepTipoDire",
				"DIRECONYREP"."Calle" 													AS "ConyRepNombre",
				"DIRECONYREP"."Numero" 													AS "ConyRepNumero",
				"DIRECONYREP"."Departamento" 											AS "ConyRepDepto",
				"DIRECONYREP"."NombreDivisionPais" 										AS "ConyRepComuna",	
				"DIRECONYREP"."NombreCiudad" 											AS "ConyRepCiudad",
				COALESCE("CLASDIVPAIS"."NombreClasificacionDivisionPais", '')			AS "ConyRepEstadoProvincia",
				"DIRECONYREP"."NombrePais" 												AS "ConyRepPais",
				"DIRECONYREP"."CodigoPostal" 											AS "ConyRepCodigoPostal",
				"PAISNAC"."NombrePais"													AS "ConyRepNacionalidad",
				CASE 
					WHEN "RELCORRCONYREP"."CodigoValorIndicador" = 'SOCIO' 	 	THEN 'SO'
					WHEN "RELCORRCONYREP"."CodigoValorIndicador" = 'MIEMBRO' 	THEN 'MG'
					WHEN "RELCORRCONYREP"."CodigoValorIndicador" = 'OTRA' 		THEN 'XX'
					WHEN "RELCORRCONYREP"."CodigoValorIndicador" = 'DIRECTOR' 	THEN 'DI'
					WHEN "RELCORRCONYREP"."CodigoValorIndicador" = 'TRABAJADOR' THEN 'TR'
					ELSE 'SR'  
				END																		AS "ConyRepRelacionIntermediario",
				1																		AS "Condicion",
				"TR"."IdCliente"
		FROM 	:v_TablaRelaciones AS "TR"
				INNER JOIN	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "CONYREP" ON ("CONYREP"."IdPersona"="TR"."IdConyugeRepresentante")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "DIRECONYREP" ON ("DIRECONYREP"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS "RELCORRCONYREP" ON ("RELCORRCONYREP"."IdPersona" = "CONYREP"."IdPersona" AND "RELCORRCONYREP"."CodigoTipoIndicador" = 'NCG380PER')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC" ON "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS "TEL" ON "TEL"."IdTelefono" = "CONYREP"."IdTelefonoPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "DIRE" ON "DIRE"."IdDireccionElectronica" = "CONYREP"."IdDireccionElectronicaPrincipal"
				LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAISNAC" ON "PAISNAC"."IdPais" = "CONYREP"."IdPaisNacionalidadPrincipal"
				LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS "DIVPAIS" ON "DIVPAIS"."NombreDivisionPais" = "DIRECONYREP"."NombreDivisionPais" AND "DIVPAIS"."IdPais" = "DIRECONYREP"."IdPais"
				LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS "CLASDIVPAIS" ON "CLASDIVPAIS"."IdClasificacionDivisionPais" = "DIVPAIS"."IdClasificacion1";
	ELSE
	
		v_TablaConyRepresentante = 
			SELECT	0  AS "IdConyugeRepresentante",
					'' AS "ConyRepRut",
					'' AS "ConyRepDig",
					'' AS "ConyRepPasaporte",
					--'' AS "ConyRepGlobalLegalEntity",
					'' AS "ConyRepApellidoPaterno",
					'' AS "ConyRepApellidoMaterno",
					'' AS "ConyRepPrimerNombre",
					'' AS "ConyRepSegundoNombre",
					'' AS "ConyRepRazonSocial",
					'' AS "ConyRepTelefono",
					'' AS "ConyRepEmail",
					'' AS "ConyRepTipoDire",
					'' AS "ConyRepNombre",
					'' AS "ConyRepNumero",
					'' AS "ConyRepDepto",
					'' AS "ConyRepComuna",	
					'' AS "ConyRepCiudad",
					'' AS "ConyRepEstadoProvincia",
					'' AS "ConyRepPais",
					'' AS "ConyRepCodigoPostal",
					'' AS "ConyRepNacionalidad",
					'' AS "ConyRepRelacionIntermediario",
					0  AS "Condicion",
					0 AS "IdCliente"
			FROM 	DUMMY;

	END IF;
	
	INSERT INTO "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE"
		(	"IdClienteOnline","NumeroCliente","IdPersonaCli","Rut","Dig","Pasaporte","Glei","ApellidoPaterno","ApellidoMaterno","PrimerNombre","SegundoNombre",
			"RazonSocial","Telefono","Email","DireccionTipo", "DireccionNombre","DireccionNumero","DireccionDepto","DireccionComuna","DireccionCiudad",
			"DireccionEstado","DireccionPais","CodigoPostal","Nacionalidad","RelacionIntermediario","AdministracionCartera", "RutEntidadEmpleadora",
			"DigEntidadEmpleadora","RazonSocialEntidadEmpleadora","EstadoCivil","CuentaBancariaUno","CuentaBancariaDos","CuentaBancariaTres","ClaseOrden","Perfil","PerfilAnterior",
			"FechaInicioPerfilActual","ConyRut","ConyDig","ConyPasaporte","ConyApellidoPaterno","ConyApellidoMaterno","ConyPrimerNombre","ConySegundoNombre","ConyRazonSocial"
			,"ConyTelefono","ConyEmail","ConyDireccionTipo","ConyDireccionNombre","ConyDireccionNumero","ConyDireccionDepto","ConyDireccionComuna",
			"ConyDireccionCiudad","ConyDireccionEstado","ConyDireccionPais","ConyCodigoPostal","ConyNacionalidad","ConyRelacionIntermediario",
			"RepRut","RepDig","RepPasaporte","RepGlei","RepApellidoPaterno","RepApellidoMaterno","RepPrimerNombre","RepSegundoNombre","RepRazonSocial","RepTelefono",
			"RepEmail","RepDireccionTipo","RepDireccionNombre","RepDireccionNumero","RepDireccionDepto","RepDireccionComuna","RepDireccionCiudad",
			"RepDireccionProvincia","RepDireccionPais","RepCodigoPostal","RepNacionalidad","RepRelacionIntermediario","RepEstadoCivil",
			"ConyRepRut","ConyRepDig","ConyRepPasaporte","ConyRepApellidoPaterno","ConyRepApellidoMaterno","ConyRepPrimerNombre",
			"ConyRepSegundoNombre","ConyRepRazonSocial","ConyRepTelefono","ConyRepEmail","ConyRepDireccionTipo","ConyRepDireccionNombre",
			"ConyRepDireccionNumero","ConyRepDireccionDepto","ConyRepDireccionComuna","ConyRepDireccionCiudad","ConyRepDireccionEstado",
			"ConyRepDireccionPais","ConyRepCodigoPostal","ConyRepNacionalidad","ConyRepRelacionIntermediario","Concatenados"
		)
	SELECT "IdClienteOnline","NumeroCliente","IdPersonaCli","Rut","Dig","Pasaporte","Glei","ApellidoPaterno","ApellidoMaterno","PrimerNombre","SegundoNombre",
			"RazonSocial","Telefono","Email","DireccionTipo", "DireccionNombre","DireccionNumero","DireccionDepto","DireccionComuna","DireccionCiudad",
			"DireccionEstado","DireccionPais","CodigoPostal","Nacionalidad","RelacionIntermediario","AdministracionCartera", "RutEntidadEmpleadora",
			"DigEntidadEmpleadora","RazonSocialEntidadEmpleadora","EstadoCivil","CuentaBancariaUno","CuentaBancariaDos","CuentaBancariaTres","ClaseOrden","Perfil","PerfilAnterior",
			"FechaInicioPerfilActual","ConyRut","ConyDig","ConyPasaporte","ConyApellidoPaterno","ConyApellidoMaterno","ConyPrimerNombre","ConySegundoNombre","ConyRazonSocial"
			,"ConyTelefono","ConyEmail","ConyTipoDire","ConyNombre","ConyNumero","ConyDepto","ConyComuna"
			,"ConyCiudad","ConyEstadoProvincia","ConyPais","ConyCodigoPostal","ConyNacionalidad","ConyRelacionIntermediario",
			"RepRut","RepDig","RepPasaporte","RepGlobalLegalEntity","RepApellidoPaterno","RepApellidoMaterno","RepPrimerNombre","RepSegundoNombre","RepRazonSocial","RepTelefono",
			"RepEmail","RepTipoDire","RepNombre","RepNumero","RepDepto","RepComuna","RepCiudad",
			"RepEstadoProvincia","RepPais","RepCodigoPostal","RepNacionalidad","RepRelacionIntermediario","RepEstadoCivil",
			"ConyRepRut","ConyRepDig","ConyRepPasaporte","ConyRepApellidoPaterno","ConyRepApellidoMaterno","ConyRepPrimerNombre",
			"ConyRepSegundoNombre","ConyRepRazonSocial","ConyRepTelefono","ConyRepEmail","ConyRepTipoDire","ConyRepNombre",
			"ConyRepNumero","ConyRepDepto","ConyRepComuna","ConyRepCiudad","ConyRepEstadoProvincia",
			"ConyRepPais","ConyRepCodigoPostal","ConyRepNacionalidad","ConyRepRelacionIntermediario",
			CAST(COALESCE("NumeroCliente",0)as NVARCHAR(20))||TRIM(COALESCE("Rut",''))||TRIM(COALESCE("Pasaporte",''))
			||TRIM(COALESCE("ApellidoPaterno",''))||TRIM(COALESCE("ApellidoMaterno",''))||TRIM(COALESCE("PrimerNombre",''))
			||TRIM(COALESCE("SegundoNombre",''))||TRIM(COALESCE("RazonSocial",''))||TRIM(COALESCE("Telefono",''))||TRIM(COALESCE("Email",''))
			||TRIM(COALESCE("DireccionTipo",''))||TRIM(COALESCE("DireccionNombre",''))||TRIM(COALESCE("DireccionNumero",''))||TRIM(COALESCE("DireccionDepto",''))
			||TRIM(COALESCE("DireccionComuna",''))||TRIM(COALESCE("DireccionCiudad",''))||TRIM(COALESCE("DireccionEstado",''))||TRIM(COALESCE("DireccionPais",''))
			||TRIM(COALESCE("CodigoPostal",''))||TRIM(COALESCE("Nacionalidad",''))||TRIM(COALESCE("RelacionIntermediario",''))
			||TRIM(COALESCE("AdministracionCartera",''))||TRIM(COALESCE("RutEntidadEmpleadora",''))||TRIM(COALESCE("DigEntidadEmpleadora",''))
			||TRIM(COALESCE("RazonSocialEntidadEmpleadora",''))||TRIM(COALESCE("EstadoCivil",''))||TRIM(COALESCE("ConyRut",''))
			||TRIM(COALESCE("ConyPasaporte",''))||TRIM(COALESCE("ConyApellidoPaterno",''))||TRIM(COALESCE("ConyApellidoMaterno",''))
			||TRIM(COALESCE("ConyPrimerNombre",''))||TRIM(COALESCE("ConySegundoNombre",''))||TRIM(COALESCE("ConyRazonSocial",''))||TRIM(COALESCE("ConyTelefono",''))
			||TRIM(COALESCE("ConyEmail",''))||TRIM(COALESCE("ConyTipoDire",''))||TRIM(COALESCE("ConyNombre",''))
			||TRIM(COALESCE("ConyNumero",''))||TRIM(COALESCE("ConyDepto",''))||TRIM(COALESCE("ConyComuna",''))
			||TRIM(COALESCE("ConyCiudad",''))||TRIM(COALESCE("ConyEstadoProvincia",''))||TRIM(COALESCE("ConyPais",''))
			||TRIM(COALESCE("ConyCodigoPostal",''))||TRIM(COALESCE("ConyNacionalidad",''))||TRIM(COALESCE("ConyRelacionIntermediario",''))
			||TRIM(COALESCE("RepRut",''))||TRIM(COALESCE("RepPasaporte",''))||TRIM(COALESCE("RepGlobalLegalEntity",''))
			||TRIM(COALESCE("RepApellidoPaterno",''))||TRIM(COALESCE("RepApellidoMaterno",''))||TRIM(COALESCE("RepPrimerNombre",''))
			||TRIM(COALESCE("RepSegundoNombre",''))||TRIM(COALESCE("RepRazonSocial",''))||TRIM(COALESCE("RepTelefono",''))||TRIM(COALESCE("RepEmail",''))
			||TRIM(COALESCE("RepTipoDire",''))||TRIM(COALESCE("RepNombre",''))||TRIM(COALESCE("RepNumero",''))
			||TRIM(COALESCE("RepDepto",''))||TRIM(COALESCE("RepComuna",''))||TRIM(COALESCE("RepCiudad",''))
			||TRIM(COALESCE("RepEstadoProvincia",''))||TRIM(COALESCE("RepPais",''))||TRIM(COALESCE("RepCodigoPostal",''))
			||TRIM(COALESCE("RepNacionalidad",''))||TRIM(COALESCE("RepRelacionIntermediario",''))||TRIM(COALESCE("RepEstadoCivil",''))
			||TRIM(COALESCE("ConyRepRut",''))||TRIM(COALESCE("ConyRepPasaporte",''))||TRIM(COALESCE("ConyRepApellidoPaterno",''))
			||TRIM(COALESCE("ConyRepApellidoMaterno",''))||TRIM(COALESCE("ConyRepPrimerNombre",''))||TRIM(COALESCE("ConyRepSegundoNombre",''))
			||TRIM(COALESCE("ConyRepRazonSocial",''))||TRIM(COALESCE("ConyRepTelefono",''))||TRIM(COALESCE("ConyRepEmail",''))
			||TRIM(COALESCE("ConyRepTipoDire",''))||TRIM(COALESCE("ConyRepNombre",''))||TRIM(COALESCE("ConyRepNumero",''))
			||TRIM(COALESCE("ConyRepDepto",''))||TRIM(COALESCE("ConyRepComuna",''))||TRIM(COALESCE("ConyRepCiudad",''))
			||TRIM(COALESCE("ConyRepEstadoProvincia",''))||TRIM(COALESCE("ConyRepPais",''))||TRIM(COALESCE("ConyRepCodigoPostal",''))
			||TRIM(COALESCE("ConyRepNacionalidad",''))||TRIM(COALESCE("ConyRepRelacionIntermediario",''))||TRIM(COALESCE("CuentaBancariaUno",''))
			||TRIM(COALESCE("CuentaBancariaDos",''))||TRIM(COALESCE("CuentaBancariaTres",''))||TRIM(COALESCE("ClaseOrden",''))||TRIM(COALESCE("Perfil",''))
			||TRIM(COALESCE("PerfilAnterior",''))||TRIM(COALESCE("FechaInicioPerfilActual",''))
		FROM :v_clientes AS "CO"
		LEFT JOIN :v_TablaConyuge AS "TC"  ON "TC"."IdCliente" = "CO"."NumeroCliente"
		LEFT JOIN :v_TablaRepresentante AS "TREP"  ON "TREP"."IdCliente" = "CO"."NumeroCliente"
		LEFT JOIN :v_TablaConyRepresentante AS "TCR"  ON "TCR"."IdCliente" = "CO"."NumeroCliente"; 

--SELECT * FROM :v_TablaConyRepresentante;
--SELECT * FROM :v_TablaRelaciones;

--SELECT 'FINAL' AS "TABLA", * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" WHERE "ApellidoPaterno" = 'OSSANDON';
END;