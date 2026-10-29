SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.CorregirPrimeraEjecucion";

CREATE PROCEDURE
-- 
--  Proposito: Corrige fecha de ejecucion en caso de cambio de hora
--  Autor: - BCS 
--  Fecha Creacion: 20/05/2016
--   
"BSCL.Plataforma.BD.Servicios::BAT.CorregirPrimeraEjecucion" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
 	IN p_IdModulo                       NVARCHAR(3),
 	
 	OUT SALIDA							NVARCHAR(520)
	
	

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
 	
 	DECLARE contador BIGINT;
 	DECLARE cantidadAgendas BIGINT;
 	DECLARE IdAgenda BIGINT;
 	DECLARE FechaSegundos BIGINT;
 	DECLARE IdTipoPrograma NVARCHAR(520);
 	DECLARE parametros TABLE("NombreParametro" NVARCHAR(520), "Valor" NVARCHAR(520));
  	DECLARE p_out_AdvertenciasContador BIGINT;
 	DECLARE p_out_ErroresContador BIGINT;
 	contador := 0;
 	
 	
	  	tabletemporary 
	  	= SELECT DISTINCT agd."IdAgenda", agd."IdTipoPrograma" FROM "BSCL.Plataforma.BD::BAT.Agenda" agd
				   WHERE "habilitado" = 'S';
				   
		SELECT COUNT(1) INTO cantidadAgendas FROM :tabletemporary;
	
		
		
		UPDATE	AGD
			 SET "SiguienteFechaDeEjecucion" = CURRENT_UTCTIMESTAMP,"UltimaFechaDeEjecucion" = CURRENT_UTCTIMESTAMP  
		FROM :tabletemporary tbt 
		INNER JOIN "BSCL.Plataforma.BD::BAT.Agenda" AGD
		ON AGD."IdAgenda" = tbt."IdAgenda" and "habilitado" = 'S'
		WHERE "SiguienteFechaDeEjecucion" < CURRENT_UTCTIMESTAMP ;
		
		
		UPDATE	PGD
			 SET "idEstado" = 'ERR',"Resultado" = 'Programa no ejecutado anteriormente'
		FROM :tabletemporary tbt
		INNER JOIN  "BSCL.Plataforma.BD::BAT.Programa" PGD
		ON PGD."IdAgenda" = tbt."IdAgenda" and pgd."IdTipoPrograma" = tbt."IdTipoPrograma"
		WHERE "fechaEjecucion" < CURRENT_UTCTIMESTAMP and "idEstado" != 'TER';


 	
 	SALIDA := 'Cantidad de registros actualizados ' || cantidadAgendas;
	
	
END;
