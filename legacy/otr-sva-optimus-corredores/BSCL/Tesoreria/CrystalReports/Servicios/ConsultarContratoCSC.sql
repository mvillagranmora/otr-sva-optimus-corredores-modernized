SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::ConsultarContratoCSC";
GO

 
CREATE PROCEDURE

"BSCL.Tesoreria.CrystalReports.Servicios::ConsultarContratoCSC" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,
	in p_IdContratoCSC		BIGINT
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
	

		--Se recupera la ultima version del logo cargado en el sistema
		v_logo = 
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' 
		AND "IdEmpresa" = p_IdEmpresa 
		ORDER BY "Version" DESC;


	
	--Calcula la paridad para las operaciones que no son en CLP								
	v_tablaSalida=	SELECT 	"CONTRATO"."IdFolioCSC", 
							"CONTRATO"."IdCliente", 
							"CONTRATO"."MontoPesos", 
							"DETALLE"."IdCSCDetalle", 
							"DETALLE"."Serie", 
							"DETALLE"."Monto", 
							"DETALLE"."Porcentaje", 
							"DETALLE"."Regimen", 
							"DETALLE"."IdCarga"  
					FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" AS "DETALLE" 
					INNER JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS "CONTRATO"  ON ("CONTRATO"."IdFolioCSC" = "DETALLE"."IdFolioCSC")
					WHERE "CONTRATO"."IdFolioCSC" =  :p_IdContratoCSC;
				 
		
	SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;

	IF v_Count = 0 THEN
		
        SELECT  ''  AS  "IdFolioCSC", 
		        ''  AS  "IdCliente",
                ''  AS  "MontoPesos",
		        ''  AS  "IdCSCDetalle", 
		        ''  AS  "Serie",
		        ''  AS  "Porcentaje", 
		        ''  AS  "Regimen",
		        ''  AS  "Direccion",
		        ''  AS  "IdCarga"
        FROM 	DUMMY;
	ELSE 

		SELECT * FROM :v_tablaSalida
		CROSS JOIN :v_logo;
	END IF;

END;


