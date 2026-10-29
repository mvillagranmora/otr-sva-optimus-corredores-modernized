SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarPlazosFechas";

-- 
--  Proposito: Consulta de Plazos y Fechas para UI
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 05/12/2016  
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarPlazosFechas" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
		  
	-- Parametros entrada
	IN p_IdPais				BIGINT,
	IN p_CantDias			BIGINT	DEFAULT 365,
	
	-- Parametro salida para grilla de PlazosFechas
	OUT p_out_PlazosFechas	TABLE (	"Plazo"		BIGINT,
									"Fecha"		NVARCHAR(10),
									"DiaSemana"	NVARCHAR(40))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_out_PlazosFechas	TABLE (	"Plazo"		BIGINT,
										"Fecha"		DATE,
										"DiaSemana"	NVARCHAR(40));
	
	DECLARE v_FechaConsulta	DATE;
	DECLARE v_i 			INTEGER;
	DECLARE v_DiaEsHabil	NVARCHAR(1);
	
	v_out_PlazosFechas = SELECT 0 AS "Plazo", '' AS "Fecha", '' AS "DiaSemana" FROM DUMMY WHERE 1=2;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(p_IdEmpresa,:v_FechaConsulta);
	
	FOR v_i IN 0..p_CantDias DO
		CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(p_Fecha=> :v_FechaConsulta, p_IdPais=> :p_IdPais, p_out_Habil=> :v_DiaEsHabil);
		
		IF :v_DiaEsHabil = 'S' THEN
			v_out_PlazosFechas =
				SELECT "Plazo", "Fecha", "DiaSemana" FROM :v_out_PlazosFechas
				UNION ALL
				SELECT
					v_i AS "Plazo",
					:v_FechaConsulta AS "Fecha",
					COALESCE(PAR."NombreParametro",' -') AS "DiaSemana"
				FROM DUMMY
				LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" PAR
					ON (PAR."IdEmpresa"				= p_IdEmpresa AND
						PAR."NombreTipoParametro"	= 'DIASEMANAESPANOL' AND
						TRIM(WEEKDAY(:v_FechaConsulta))	= TRIM(PAR."ValorParametro"));
		END IF;
		
		v_FechaConsulta	:= ADD_DAYS(:v_FechaConsulta,1);
	END FOR;
	
	p_out_PlazosFechas =
		SELECT "Plazo", "Fecha", "DiaSemana" FROM :v_out_PlazosFechas;

END; 
