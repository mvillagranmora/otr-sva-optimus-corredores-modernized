SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Deudores";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Deudores"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380Deudores" --Se debe llamar siemte p_out_TablaSalida
      
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	  
	DECLARE v_Count BIGINT := 0;
    DECLARE v_FechaInicio DATE DEFAULT '0001-01-01';
    DECLARE v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_TodoCliente CHAR(1) DEFAULT 'S';
	DECLARE v_Solo1Cliente BIGINT := -1;    
    DECLARE v_IdEmpresa INT := 0;
    DECLARE v_SecuenciaTablaGlobal BIGINT DEFAULT 0;
	DECLARE v_FecSistema DATE;	
	declare v_DiasAntiguedadDeuda BIGINT;
	--* ------------------------------------------------------------------------------	
    --* PARAMETROS DESDE LA UI EN VARIABLES
	--* ------------------------------------------------------------------------------
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX("FechaInicio"),'0001-01-01') AS DATE),
        CAST(MAX("FechaTermino") AS DATE)
        INTO v_IdEmpresa, v_FechaInicio, v_FechaTermino
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino"
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER'))
    );
	
	select COUNT(1) INTO v_Count from "BSCL.Normativa.BD::NOR.Configuracion" where "CodigoParametro" = 'DiasAntiguedadDeuda' AND "IdEmpresa" = :v_IdEmpresa;
	IF v_Count > 0 THEN
    		select "Valor" into v_DiasAntiguedadDeuda from "BSCL.Normativa.BD::NOR.Configuracion" where "CodigoParametro" = 'DiasAntiguedadDeuda' AND "IdEmpresa" = :v_IdEmpresa;
    		ELSE
    		v_DiasAntiguedadDeuda:=3;
    END IF;
    
	--------------------
	
    --LOGICA
	--------------------
	SELECT 	Top 1 "Valor" 
    INTO	v_FecSistema 
    FROM	"BSCL.Plataforma.BD::PLA.Configuracion" 
    WHERE	"CodigoParametro" = 'PLA.FechaSistema';	
		
    
    v_deudas = select  
			"CUE"."IdCliente" AS "IdCliente",
			"CUE"."FechaEfectivaMovimiento" AS "Fecha",
			"CUE"."Monto",
			"CUE"."IdMoneda"
			from "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" "CUE" 
	        INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'RUT') AS r ON (r."Valor" IS NULL OR r."IdSistema" = "CUE"."IdCliente")
			where 
			"CUE"."IdTipoMovimiento" = 'MOVCAR'
			and "CUE"."AntiguedadDeuda" >= :v_DiasAntiguedadDeuda
			and "CUE"."FechaEfectivaMovimiento" >= v_FechaInicio
			and "CUE"."FechaEfectivaMovimiento" <= IFNULL (v_FechaTermino,"CUE"."FechaEfectivaMovimiento" );
			
			

	
	
	
	select count(1) into v_Count from :v_deudas;
	IF (v_Count > 0) THEN 
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
			,"CFF"."Fecha"
			,"CFF"."Monto" AS "Monto"
			,"CFF"."IdMoneda" AS "Moneda"
			,'0001-01-01' AS "FechaRepactacion"
			,'0001-01-01' AS "FechaAbono"
			,0 AS "MontoAbono"
			,'' AS "MonedaAbono"
			FROM "BSCL.Normativa.BD::NOR.HistoricoCliente" "CLI"
			INNER JOIN :v_deudas "CFF" ON ("CLI"."IdCliente"  = "CFF"."IdCliente" AND "CFF"."Fecha"="CLI"."FechaRegistro" AND "CFF"."Fecha" < v_FecSistema)
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" AS CONSULTAR_CLIENTE ON (TRIM("DocumentoIdentidadRut")||'-'||TRIM("DocumentoIdentidadDig") = CONSULTAR_CLIENTE."DocumentoIdentidad")
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL';
	END IF;	
	
	
	
	
	
	IF v_FechaTermino >= v_FecSistema THEN
	
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline"( IdSecuenciaGlobal => :v_SecuenciaTablaGlobal );
		
		
		v_Tabla_ONLINE = 
			SELECT DISTINCT * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" WHERE "IdClienteOnline" = v_SecuenciaTablaGlobal; 
			
			
			
	p_out_TablaSalida =SELECT * FROM :p_out_TablaSalida
					 UNION ALL select 
			CASE 
                WHEN "TipoDoc"."TipoDocumentoIdentidad" = 'R' THEN "CLI"."Rut" 
				ELSE ''
			END AS "DocumentoIdentidad"		 
			,CASE 
			 	WHEN "TipoDoc"."TipoDocumentoIdentidad" = 'R' THEN "CLI"."Dig"
				ELSE ''
			END AS "DigitoVerificador"	 
			,CASE 
				WHEN "TipoDoc"."TipoDocumentoIdentidad" = 'P' THEN "CLI"."Pasaporte"
				ELSE ''
			END AS "Pasaporte"
			,"CLI"."Glei" as "GlobalLegalEntity"
			,"CLI"."ApellidoPaterno"
			,"CLI"."ApellidoMaterno"
			,"CLI"."PrimerNombre" AS "PrimerNombre"
			,"CLI"."SegundoNombre" AS "SegundoNombre"
			,"CLI"."RazonSocial"
			,"CLI"."Telefono" AS "Telefono"
			,"CLI"."Email" AS "DireccionElectronica"
			,"CLI"."DireccionTipo" AS "TipoDireccion"
			,"CLI"."DireccionNombre" AS "Calle"
			,"CLI"."DireccionNumero" AS "Numero"
			,"CLI"."DireccionDepto" AS "Departamento"
			,"CLI"."DireccionComuna" AS "Comuna"
			,"CLI"."DireccionCiudad" AS "Ciudad"
			,COALESCE("REG"."NombreClasificacionDivisionPais1",'') AS "Provincia"
			,"CLI"."DireccionPais" AS "Pais"
			,"CLI"."CodigoPostal"
			,"CLI"."Nacionalidad" AS "PaisNacimiento"
			,"CFF"."Fecha"
			,"CFF"."Monto" AS "Monto"
			,CASE
                WHEN SUBSTRING("CFF"."IdMoneda", 1, 3) = 'CLP' THEN '$$' || COALESCE(SUBSTRING("CFF"."IdMoneda", 4), '')
                WHEN SUBSTRING("CFF"."IdMoneda", 1, 3) = 'AUD' THEN 'DA' || COALESCE(SUBSTRING("CFF"."IdMoneda", 4), '')
                ELSE COALESCE("CFF"."IdMoneda",'')
            END AS "Moneda"
			,'0001-01-01' AS "FechaRepactacion"
			,'0001-01-01' AS "FechaAbono"
			,0 AS "MontoAbono"
			,'' AS "MonedaAbono"
			FROM :v_Tabla_ONLINE AS "CLI"
			INNER JOIN :v_deudas "CFF" ON ("CLI"."NumeroCliente"  = "CFF"."IdCliente" AND "CFF"."Fecha" >= v_FecSistema)
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" AS CONSULTAR_CLIENTE ON ("CLI"."Pasaporte" = CONSULTAR_CLIENTE."DocumentoIdentidad")
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad" AS "REG" ON ("REG"."NombreCiudad" = "CLI"."DireccionCiudad") AND ("REG"."NombreDivisionPais" = "CLI"."DireccionComuna")
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "TipoDoc" ON ("TipoDoc"."IdCliente" = "CLI"."NumeroCliente")
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL';

	END IF;
			
	
	
	
	
	
	
	
	

END;

