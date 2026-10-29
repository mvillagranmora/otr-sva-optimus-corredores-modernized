SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DepuracionMM";

CREATE PROCEDURE 
-- 
--  Proposito: Agregar un log de depuracion masivo
--     SET 'DEPURACION' = 'S';
--  Autor: OCT - Oscar Carreno - BCS
--  Fecha Creacion: 21/12/2016
"BSCL.Plataforma.BD.Servicios::DepuracionMM" (
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS	
BEGIN
	DECLARE v_Depuracion TABLE(
		"IdEmpresa" BIGINT,"Fecha" TIMESTAMP,"IdUsuario" BIGINT,"IdServicioAgrupador" BIGINT,"ServicioAgrupador" NVARCHAR(255),
		"Servicio" NVARCHAR(255),"Bandera" NVARCHAR(80),"Detalle" NVARCHAR(520),"IdEjecucion" BIGINT,"DEC" DECIMAL);
	
	v_Depuracion = 
		SELECT
			COALESCE("IdEmpresa",0) AS "IdEmpresa",COALESCE("Fecha",'0001-01-01') AS "Fecha",COALESCE("IdUsuario",0) AS "IdUsuario",
			COALESCE("IdServicioAgrupador",0) AS "IdServicioAgrupador",COALESCE("ServicioAgrupador",'') AS "ServicioAgrupador",
			COALESCE("Servicio",'') AS "Servicio",COALESCE("Bandera",'') AS "Bandera",COALESCE("Detalle",'') AS "Detalle",
			ROW_NUMBER() over (ORDER BY "Fecha") AS "IdEjecucion",
			COALESCE(SECONDS_BETWEEN (to_date('1970-01-01'),"Fecha")+to_int(TO_VARCHAR("Fecha",'FF3'))/1000,0) AS "DEC"
		FROM "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion";
		
	--IF SESSION_CONTEXT('DEPURACION')='S' THEN 
 			INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion" (
				"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","IdEjecucion","Servicio",
				"Bandera","Detalle","Diferencia"
			) 								
			SELECT
				"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","IdEjecucion","Servicio",
				"Bandera","Detalle",("DEC" - COALESCE(LAG("DEC") over (order by "IdEjecucion"),"DEC"))
			FROM :v_Depuracion;
					
	--END IF;
END;

