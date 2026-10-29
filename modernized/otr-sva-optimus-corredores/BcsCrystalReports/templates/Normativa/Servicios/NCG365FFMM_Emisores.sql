SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM_Emisores";

CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM_Emisores" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa				BIGINT

    --VARIABLES DE SALIDA OBLIGATORIAS
	--OUT p_out_TablaPEP	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	v_tablaSalida = 
		SELECT 
			ROW_NUMBER() OVER (ORDER BY x."IdEmisor") AS "NumeroRegistro"
			, x."IdEmisor"
			, x."NombreCompleto"
			, x."IndicadorAGF"
			, x."IdEstado" 
		FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" x
		WHERE x."IndicadorAGF" = 'S' AND x."IdEstado" = 'ACT'
		ORDER BY x."IdEmisor";
	
	SELECT * FROM :v_tablaSalida;
END;