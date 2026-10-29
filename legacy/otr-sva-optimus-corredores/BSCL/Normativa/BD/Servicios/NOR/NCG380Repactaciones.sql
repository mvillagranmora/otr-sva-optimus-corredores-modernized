SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Repactaciones";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Repactaciones"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380Repactaciones" --Se debe llamar siemte p_out_TablaSalida
      
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	  
	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	declare v_Count				 int DEFAULT 0;
	

	declare v_IdEmpresa			 BIGINT DEFAULT 0;
	
    --------------------
	--DECLARE Manejo de Errores
	-------------------- 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL;
	END;

	--------------------
	--LOGICA
	--------------------
	
	select top 1 "Valor" into v_IdEmpresa from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = :p_IdSolicitudCircular and "SP"."TipoParametro" = 'EMP';
	select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = :p_IdSolicitudCircular and "SP"."TipoParametro" ='FTER';
	select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = :p_IdSolicitudCircular and "SP"."TipoParametro" ='FINI';

	--* FILTRO POR CLIENTES (Asume que la info viene como 338-4)
	--* ------------------------------------------------------------------------------	
	v_TablaClientes = 
		SELECT 	"Valor", "IdSistema" 
		FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
		WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'RUT';

	SELECT COUNT(1) INTO v_Count FROM :v_TablaClientes;
	IF v_Count = 0 THEN
		v_TablaClientes = 
			SELECT	NULL AS "Valor", NULL AS "IdSistema"
			FROM	DUMMY;
	END IF;
		
		
	p_out_TablaSalida = select 
			 "CLI"."DocumentoIdentidadRut" AS "DocumentoIdentidad"
			,"CLI"."DocumentoIdentidadDig" AS "DigitoVerificador"
			,"CLI"."DocumentoIdentidadPasaporte" AS "Pasaporte"
			,"CLI"."Glei" as "GlobalLegalEntity"
			,"CLI"."ApellidoPaterno"
			,"CLI"."ApellidoMaterno"
			,"CLI"."PrimerNombre" AS "PrimerNombre"
			,"CLI"."SegundoNombre" AS "SegundoNombre"
			,"CLI"."RazonSocial"
			,"CLI"."Numero" AS "Telefono"
			,"CLI"."DireccionElectronica" AS "DireccionElectronica"
			,"CLI"."TipoDireccion" AS "TipoDireccion"
			,"CLI"."Calle"
			,"CLI"."Numero"
			,"CLI"."Departamento"
			,"CLI"."Comuna" AS "Comuna"
			,"CLI"."Ciudad" AS "Ciudad"
			,"CLI"."Provincia" AS "Provincia"
			,"CLI"."Pais" AS "Pais"
			,"CLI"."CodigoPostal"
			,"CLI"."Pais" AS "PaisNacimiento"
			,CASE WHEN "CTA"."IdConcepto" = 'DEURPC' AND "CTA"."IdTipoMovimiento" = 'MOVCAR' THEN "FechaSistema" ELSE '0001-01-01' END AS "Fecha"
			,CASE WHEN "CTA"."IdConcepto" = 'DEURPC' AND "CTA"."IdTipoMovimiento" = 'MOVCAR' THEN "Monto" ELSE 0 END AS "Monto"
			,CASE WHEN "CTA"."IdConcepto" = 'DEURPC' AND "CTA"."IdTipoMovimiento" = 'MOVCAR' THEN "IdMoneda" ELSE '' END AS "Moneda"
			,CASE WHEN "CTA"."IdConcepto" = 'REPCDE' THEN "FechaSistema" ELSE '0001-01-01' END AS "FechaRepactacion"
			,CASE WHEN  "CTA"."IdTipoMovimiento" = 'MOVABN' THEN "FechaSistema" ELSE '0001-01-01' END AS "FechaAbono"
			,CASE WHEN  "CTA"."IdTipoMovimiento" = 'MOVABN' THEN "Monto" ELSE 0 END AS "MontoAbono"
			,CASE WHEN  "CTA"."IdTipoMovimiento" = 'MOVABN' THEN "IdMoneda" ELSE '' END AS "MonedaAbono"
			FROM "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" "CLI"
			INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" "CTA" ON ("CLI"."IdCliente"  = "CTA"."IdCliente")
			INNER JOIN :v_TablaClientes AS PrmCli ON (PrmCli."Valor" IS NULL OR PrmCli."Valor" ="CLI"."DocumentoIdentidadRut")	
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" CLIENTE ON (TRIM("CLI"."DocumentoIdentidadRut" || '-' || TRIM("CLI"."DocumentoIdentidadDig")) = CLIENTE."DocumentoIdentidad")
			WHERE
			"CLI"."FechaRegistro" <= v_ParametrosSolicitudFter 
			AND "CTA"."IdConcepto" in ('REPCDE','DEURPC')
			AND "CTA"."FechaSistema" >= v_ParametrosSolicitudFini
			AND "CTA"."FechaSistema" <= IFNULL (v_ParametrosSolicitudFter,"CTA"."FechaSistema" )
			AND CLIENTE."TipoEnrolamiento" = 'FULL';
	
	
			SELECT COUNT(1) INTO v_Count FROM :p_out_TablaSalida;
		
	IF v_Count=0 THEN
			p_out_TablaSalida = SELECT
				 '' AS "DocumentoIdentidad" 
				,'' AS "DigitoVerificador" 
				,'' AS "Pasaporte" 
				,'' AS "GlobalLegalEntity" 
				,'' AS "ApellidoPaterno"
				,'' AS "ApellidoMaterno"
				,'' AS "PrimerNombre"
				,'' AS "SegundoNombre"
				,'' AS "RazonSocial"
				,'' AS "Telefono"
				,'' AS "DireccionElectronica"	
				,'' AS "TipoDireccion"
				,'' AS "Calle"
				,'' AS "Numero"
				,'' AS "Departamento"
				,'' AS "Comuna"
				,'' AS "Ciudad"
				,'' AS "Provincia"
				,'' AS "Pais"
				,'' AS "CodigoPostal"
				,'' AS "PaisNacimiento"
				,'0001-01-01' AS "Fecha"
				, 0 AS "Monto"
				,'' AS "Moneda"
				,'0001-01-01' AS "FechaRepactacion"
				,'0001-01-01' AS "FechaAbono"
				, 0 AS "MontoAbono"	
				,'' AS "MonedaAbono"
				from DUMMY WHERE	1=2;
	END IF;
			
	
	

	
	

END;