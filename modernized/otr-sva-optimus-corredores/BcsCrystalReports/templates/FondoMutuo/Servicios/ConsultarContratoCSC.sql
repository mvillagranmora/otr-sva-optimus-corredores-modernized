SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.FondoMutuo.CrystalReports.Servicios::ConsultarContratoCSC";
GO

 
CREATE PROCEDURE "BSCL.FondoMutuo.CrystalReports.Servicios::ConsultarContratoCSC" (
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

		
	v_tablaSalida=	SELECT 	"CONTRATO"."IdFolioCSC", 
							"CONTRATO"."Fecha", 
							"CONTRATO"."Hora",
							"CONTRATO"."MontoPesos",
							"CONTRATO"."FolioCSC",
							"CC"."DocumentoIdentidad",
							"TEL"."Numero" AS "Telefono",
							"CC"."NombreCompleto",
							"DIR"."Direccion",
							"CONTRATO"."NombreFuncionario",
							"CONTRATO"."DocumentoIdentificacionFuncionario",
							"CONTRATO"."DomicilioSucursal",
							"CONTRATO"."CodigoSucursal",
							"CF"."NombreFondo",
							"CF"."CodigoSerie",
							"DETALLE"."Monto",
							CASE WHEN ("CONTRATO"."TipoPago" = 'PAC') THEN 'x' ELSE '' END AS "esPAC",
							CASE WHEN ("CONTRATO"."TipoPago" = 'CHE') THEN 'x' ELSE '' END AS "esCHE",
							CASE WHEN ("CONTRATO"."TipoPago" = 'VVI') THEN 'x' ELSE '' END AS "esVVI",
							"CONTRATO"."DiaDescuento",
							"BANCO"."NombreBanco"
					FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" AS "DETALLE" 
					INNER JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS "CONTRATO"  ON ("CONTRATO"."IdFolioCSC" = "DETALLE"."IdFolioCSC")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON (CC."IdCliente" = "CONTRATO"."IdCliente")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS "DIR" ON ("DIR"."IdDireccionElectronica" = "CC"."IdDireccionElectronicaPrincipal")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS "TEL" ON ("TEL"."IdTelefono" = "CC"."IdTelefonoPrincipal")
					INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS "CF" ON ("CF"."IdSerieFondo" = "DETALLE"."Serie")
					LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BANCO" ON ("BANCO"."IdBanco" = "CONTRATO"."IdBanco") 
					WHERE "CONTRATO"."IdFolioCSC" =  :p_IdContratoCSC;
				 
		
	SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;

	IF v_Count = 0 THEN
		
        SELECT  ''  AS  "IdFolioCSC", 
				''  AS  "Fecha", 
				''  AS  "Hora",
				''  AS  "MontoPesos",
				''  AS  "FolioCSC",
				''  AS  "DocumentoIdentidad",
				''  AS  "Telefono",
				''  AS  "NombreCompleto",
				''  AS  "Direccion",
				''  AS  "NombreFuncionario",
				''  AS  "DocumentoIdentificacionFuncionario",
				''  AS  "DomicilioSucursal",
				''  AS  "CodigoSucursal",
				''  AS  "NombreFondo",
				''  AS  "CodigoSerie",
				''	AS  "Monto",
				''  AS  "esPAC",
				''  AS  "esCHE",
				''  AS  "esVVI",
				''  AS  "esVVI",
				''  AS  "DiaDescuento",
				''  AS  "NombreBanco"
        FROM 	DUMMY;
	ELSE 

		SELECT * FROM :v_tablaSalida
		CROSS JOIN :v_logo;
	END IF;

END;