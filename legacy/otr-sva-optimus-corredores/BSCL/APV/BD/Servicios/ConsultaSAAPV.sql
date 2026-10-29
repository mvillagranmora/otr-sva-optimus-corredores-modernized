SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.APV.BD.Servicios::ConsultaSAAPV";
GO

CREATE PROCEDURE "BSCL.APV.BD.Servicios::ConsultaSAAPV" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	
	IN p_IdPersona					BIGINT	DEFAULT NULL,
	IN p_IdCliente					BIGINT	DEFAULT NULL,
    
    OUT p_out_tbl					TABLE (
										"IdContrato"		BIGINT
									   ,"TipoRecaudacion"	NVARCHAR(50)
									   ,"Regimen"			NVARCHAR(5)
									   ,"Cuenta"			NVARCHAR(5)
									   ,"MontoTotal"		BIGINT
									   ,"Moneda"			NVARCHAR(5)
									   ,"CodigoFondo"		BIGINT
									   ,"Serie"				NVARCHAR(5)
									   ,"MontoParcial"		BIGINT
									   ,"PorcentajeParcial"	BIGINT
									   ,"NombreFondo"		NVARCHAR(50)
									   ,"Nombre"			NVARCHAR(100)
									   ,"NumeroCuenta"		BIGINT
									   ,"Rut"				NVARCHAR(15)
									   ,"RazonSocial"		NVARCHAR(100)
									   ,"NombreFantasia"	NVARCHAR(100)
									   ,"Direccion"			NVARCHAR(200)
									   ,"Telefono"			NVARCHAR(15)
									   ,"Region"			NVARCHAR(50)
									   ,"Comuna"			NVARCHAR(50)
									   ,"Ciudad"			NVARCHAR(50)
									),
	
   	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	-------------------------------
	-- DECLARE Manejo de Errores --
	-------------------------------
	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 AS "IdInterno" ,'' AS "Codigo",'N' AS "RequiereAutorizacion",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1 = 2;
		v_out_ErroresDetalle 	  = SELECT 0 AS "IdInterno" ,'' AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1 = 2;
	END;
	
	BEGIN
	
		DECLARE SALIDA_CONTROLADA CONDITION;
		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			RESIGNAL;
		END;
		
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
		BEGIN
		
			SELECT COUNT(1)
			INTO p_out_ErroresContador
			FROM :v_out_ErroresDetalle;
			
			SELECT COUNT(1)
			INTO p_out_AdvertenciasContador
			FROM :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes =
				SELECT
					"Codigo" AS "CodigoMensaje"
				   ,"NombreParametro" AS "CodigoDato"
				   ,"ValorParametro" AS "ValorDato"
				FROM :v_out_ErroresDetalle
				UNION ALL
				SELECT
					"Codigo" AS "CodigoMensaje"
				   ,"NombreParametro" AS "CodigoDato"
				   ,"ValorParametro" AS "ValorDato"
				FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle =
				SELECT
					"errores"."IdInterno"
				   ,"errores"."Codigo"
				   ,"msj"."Descripcion"
				FROM :v_out_ErroresDetalle AS "errores"
				INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle =
				SELECT
					"adv"."IdInterno"
				   ,"adv"."Codigo"
				   ,"adv"."RequiereAutorizacion"
				   ,"msj"."Descripcion"
				FROM :v_out_AdvertenciasDetalle AS "adv"
				INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo";
			
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		-------------
		-- NEGOCIO --
		-------------
		BEGIN
		
			DECLARE v_IdCliente			BIGINT DEFAULT NULL;
			DECLARE v_IdPersona			BIGINT DEFAULT NULL;
			DECLARE v_resultadoRegla	NVARCHAR(40);
			
			---------------------------------------
			-- VALIDACION VARIABLES OBLIGATORIAS --
			---------------------------------------
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
			
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", "Codigo", '' AS "ValorParametro",'' AS "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
			---------------------------------------
			---------------------------------------
			
			IF :p_IdCliente > 0 THEN
			
				v_IdCliente = :p_IdCliente;
			
			ELSEIF :p_IdPersona > 0 THEN
			
				v_IdPersona = :p_IdPersona;
			
			END IF;
			
			IF :v_IdCliente IS NULL AND :v_IdPersona IS NULL THEN
			
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'DEBES_INGRESAR_UN_ID_PERSONA_O_UN_ID_CLIENTE' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
			
			v_contrato =
				SELECT
					ContratoSAAPV."IdContratoSAAPV"					AS "IdContrato"
				   ,MedioRecaudacion."NombreMedioRecaudacion"		AS "TipoRecaudacion"
				   ,SUBSTR_AFTER(RegimenTributario."Codigo",'-')	AS "Regimen"
				   ,SUBSTR_BEFORE(RegimenTributario."Codigo",'-')	AS "Cuenta"
				   ,CASE
				   		WHEN PortafolioContratoSAAPV."MontoPesos" > 0 THEN PortafolioContratoSAAPV."MontoPesos"
				   		WHEN PortafolioContratoSAAPV."MontoUF" > 0 THEN PortafolioContratoSAAPV."MontoUF"
				   		ELSE NULL
				   	END												AS "MontoTotal"
				   ,'CLP'											AS "Moneda"
				   ,Instrumento."IdInstrumento"						AS "CodigoFondo"
				   ,SerieDeFondo."CodigoSerie"						AS "Serie"
				   ,DistribucionInversion."MontoPesos"				AS "MontoParcial"
				   ,DistribucionInversion."Porcentaje"				AS "PorcentajeParcial"
				   ,Instrumento."RazonSocial"						AS "NombreFondo"
				   ,Banco."NombreBanco"								AS "Nombre"
				   ,CuentaBancaria."NumeroCuentaBancaria"			AS "NumeroCuenta"
				FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdPersona"
				LEFT JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MedioRecaudacion ON MedioRecaudacion."IdMedioRecaudacion" = ContratoSAAPV."IdMedioRecaudacionTrabajador"
				LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContratoSAAPV ON PortafolioContratoSAAPV."IdContratoSAAPV" = ContratoSAAPV."IdContratoSAAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PortafolioContratoSAAPV."IdPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupador."IdRegimenTributario"
				LEFT JOIN "BSCL.APV.BD::SCS.DistribucionInversion" AS DistribucionInversion ON DistribucionInversion."IdContratoSAAPV" = ContratoSAAPV."IdContratoSAAPV"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento ON Instrumento."IdInstrumento" = DistribucionInversion."IdInstrumento"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = Instrumento."IdInstrumento"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancaria ON CuentaBancaria."IdPersona" = Persona."IdPersona"
				LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS Banco ON Banco."IdBanco" = CuentaBancaria."IdBanco"
				WHERE
					ContratoSAAPV."IdCliente" = :v_IdCliente
				OR  ContratoSAAPV."IdPersona" = :v_IdPersona
				AND Portafolio."IdEstado" <> 'INA';
			
			IF IS_EMPTY(:v_contrato) THEN
			
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_EXISTE_CONTRATO_PARA_ESTA_PERSONA' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
			
			v_empleador =
				SELECT
					ContratoSAAPV."IdContratoSAAPV"								AS "IdContrato"
				   ,DocumentoIdentidad."DocumentoIdentidad"						AS "Rut"
				   ,Persona."RazonSocial"										AS "RazonSocial"
				   ,''															AS "NombreFantasia"
				   ,DireccionFisica."Calle" || ' ' || DireccionFisica."Numero"	AS "Direccion"
				   ,Telefono."Numero"											AS "Telefono"
				   ,ClasificacionDivisionPais."NombreClasificacionDivisionPais"	AS "Region"
				   ,DivisionPais."NombreDivisionPais"							AS "Comuna"
				   ,Ciudad."NombreCiudad"										AS "Ciudad"
				FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdEmpleador"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DireccionFisica ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS Telefono ON Telefono."IdTelefono" = Persona."IdTelefonoPrincipal"
				LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DivisionPais ON DivisionPais."IdDivisionPais" = DireccionFisica."IdDivisionPais"
				LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS ClasificacionDivisionPais ON ClasificacionDivisionPais."IdClasificacionDivisionPais" = DivisionPais."IdClasificacion1"
				LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad ON Ciudad."IdCiudad" = DireccionFisica."IdCiudad"
				WHERE
					ContratoSAAPV."IdEmpleador" IS NOT NULL
				AND ContratoSAAPV."IdEmpleador" != 0;
			
			p_out_tbl =
				SELECT
					CON."IdContrato"
				   ,CON."TipoRecaudacion"
				   ,CON."Regimen"
				   ,CON."Cuenta"
				   ,CON."MontoTotal"
				   ,CON."Moneda"
				   ,CON."CodigoFondo"
				   ,CON."Serie"
				   ,CON."MontoParcial"
				   ,CON."PorcentajeParcial"
				   ,CON."NombreFondo"
				   ,CON."Nombre"
				   ,CON."NumeroCuenta"
				   ,EMP."Rut"
				   ,EMP."RazonSocial"
				   ,EMP."NombreFantasia"
				   ,EMP."Direccion"
				   ,EMP."Telefono"
				   ,EMP."Region"
				   ,EMP."Comuna"
				   ,EMP."Ciudad"
				FROM :v_contrato AS CON
				LEFT JOIN :v_empleador AS EMP ON EMP."IdContrato" = CON."IdContrato"
				ORDER BY
					CON."IdContrato";
			
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;		
END;