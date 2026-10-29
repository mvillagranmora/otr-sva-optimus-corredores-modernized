SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::MandatoPAC";
GO

 
CREATE PROCEDURE

"BSCL.Tesoreria.CrystalReports.Servicios::MandatoPAC" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,
	in p_IdMandatoPAC		BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_Count	 BIGINT:=0;
        DECLARE v_logo TABLE("LogoCorredora" BLOB);
	

		--Se recupera la ultima versi√≥n del logo cargado en el sistema
		v_logo = 
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' 
		AND "IdEmpresa" = p_IdEmpresa 
		ORDER BY "Version" DESC;


	
	--Calcula la paridad para las operaciones que no son en CLP								
	v_tablaSalida=SELECT    "PAC"."NombreCompleto", 
		                    "PAC"."DocumentoIdentidad", 
		                    "PAC"."NumeroCuentaBancaria", 
		                    "PAC"."Receptor", 
		                    "PAC"."Fecha", 
		                    "PAC"."NumeroMandato",
		                    "PAC"."NombreBanco", 
		                    "PAC"."IdCliente",
		                    "DIR"."NombreLargo" AS "Direccion",
		                    "TEL"."Numero" AS "Telefono",
		                    "PAC"."NombrePlaza",
		                    "PAC"."FolioPacMandatoAgrupador"
							
                FROM 	"BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC" AS "PAC"
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "DIR" ON ("DIR"."IdDireccionFisica" = "PAC"."IdDireccionFisicaPrincipal")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS "TEL" ON ("TEL"."IdTelefono" = "PAC"."IdTelefonoPrincipal")
                WHERE   "PAC"."IdMandatoPAC" = :p_IdMandatoPAC;
				 
		
	SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;

	IF v_Count = 0 THEN
		
        SELECT  ''  AS  "DocumentoIdentidad", 
		        ''  AS  "NumeroCuentaBancaria",
                ''  AS  "Receptor",
		        ''  AS  "Fecha", 
		        ''  AS  "NumeroMandato",
		        ''  AS  "NombreBanco", 
		        ''  AS  "IdCliente",
		        ''  AS  "Direccion",
		        ''  AS  "Telefono",
		        ''  AS  "NombrePlaza"
        FROM 	DUMMY;
	ELSE 
		v_asdf = SELECT 'ORIGINAL BANCO' AS "EntidadDestino", * FROM :v_tablaSalida
		         UNION 
				 SELECT 'COPIA 1: INSTITUCI”N AUTORIZADA' AS "EntidadDestino", * FROM :v_tablaSalida
		         UNION 
		         SELECT 'COPIA 2: SUCURSAL' AS "EntidadDestino", * FROM :v_tablaSalida
		         UNION
		         SELECT 'COPIA 3: CLIENTE' AS "EntidadDestino", * FROM :v_tablaSalida;
		SELECT * FROM :v_asdf
		CROSS JOIN :v_logo;
	END IF;

END;


