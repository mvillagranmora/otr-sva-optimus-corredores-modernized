SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::TraspasoEntreCorredoras"
GO
CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::TraspasoEntreCorredoras"(
	IN p_IdEmpresa		BIGINT,
	IN p_Movimientos 	NVARCHAR(5000)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"

AS
BEGIN	
	
	DECLARE Movimientos BIGINT ARRAY;
	DECLARE Contador   INTEGER;
	DECLARE Cadena     NVARCHAR(5000);
	
	Contador := 1;
	Cadena := p_Movimientos;
		
	WHILE LOCATE(:Cadena,',')>0 DO
		Movimientos[:Contador]:=SUBSTR_BEFORE(:Cadena,',');
		Cadena := SUBSTR_AFTER(:Cadena,',');
		Contador:=Contador +1;
	END WHILE;
		Movimientos[:Contador]:=:Cadena;
		
		v_Movimientos = UNNEST(:Movimientos)as("IdMovimiento");

	v_MovimientosEnTransito = 

            SELECT
                PER."RazonSocial" AS "CorredoraOrigen",
                EMP."RazonSocial" AS "CorredoraDestino",
                M."Nemotecnico",
                M."NombrePersona",
                M."Cantidad",
                M."DocumentoIdentidad",
                M."FechaOperacion" AS "FechaOperacion",
                M."IdTipoOperacion" AS "TipoOperacion"

            FROM :v_Movimientos MV
            JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" M 
            ON M."IdMovimientoCarteraEnTransito" = MV."IdMovimiento"
            LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" T ON M."IdMovimientoCarteraEnTransito" = T."IdMovimientoCarteraEnTransito"
            LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" C ON T."IdCorredora" = C."IdCorredora"

            LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."IdEmpresa" = M."IdEmpresa" 
            LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "PER" ON "PER"."IdPersona"= C."IdPersona";


	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
	SELECT * FROM :v_MovimientosEnTransito
	CROSS JOIN :v_logo;

END;