SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::SOC.ObtieneDatosCliente";
GO

CREATE PROCEDURE "BSCL.SocioNegocio.BD.Servicios::SOC.ObtieneDatosCliente" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	
	IN p_Rut						BIGINT,
	IN p_Dv							NVARCHAR(1),
    
    OUT p_out_tbl					TABLE (
										"Rut"					NVARCHAR(40)
									   ,"IdPersona"				BIGINT
									   ,"IdCliente"				BIGINT
									   ,"IdProductoAgrupador"	BIGINT
									   ,"IdPortafolio"			BIGINT
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
		
			DECLARE v_Rut				BIGINT		DEFAULT NULL;
			DECLARE v_Dv				NVARCHAR(1)	DEFAULT NULL;
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
			
			IF :p_Rut > 0 THEN
			
				IF :p_Dv >= '0' AND :p_Dv <= '9' OR UPPER(:p_Dv) = 'K' THEN
				
					v_Rut = :p_Rut;
					v_Dv = :p_Dv;
				
				END IF;
			
			END IF;
			
			IF :v_Rut IS NULL AND :v_Dv IS NULL THEN
			
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'DEBES_INGRESAR_UN_RUT_Y_SU_DV' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
			
			v_datos =
				SELECT
					DocumentoIdentidad."DocumentoIdentidad"	AS "Rut"
				   ,Persona."IdPersona"						AS "IdPersona"
				   ,Cliente."IdCliente"						AS "IdCliente"
				   ,MatrizAgrupador."IdProductoAgrupador"	AS "IdProductoAgrupador"
				   ,Portafolio."IdPortafolio"				AS "IdPortafolio"
				FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdCliente" = Cliente."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				WHERE
					UPPER(DocumentoIdentidad."DocumentoIdentidad") = UPPER(v_Rut || '-' || v_Dv)
				ORDER BY
					MatrizAgrupador."IdProductoAgrupador";
			
			IF IS_EMPTY(:v_datos) THEN
			
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_EXISTEN_DATOS_PARA_ESTA_PERSONA' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
			
			p_out_tbl =
				SELECT
					"Rut"
				   ,"IdPersona"
				   ,"IdCliente"
				   ,"IdProductoAgrupador"
				   ,"IdPortafolio"
				FROM :v_datos;
			
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;		
END;