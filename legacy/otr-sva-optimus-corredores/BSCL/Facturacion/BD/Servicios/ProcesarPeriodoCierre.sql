SET SCHEMA "BSCL"; 
GO

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ProcesarPeriodoCierre";
GO

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ProcesarPeriodoCierre"
  (
       -- Parametros de Validacion
       IN p_IdEmpresa                     BIGINT,
       IN p_IdUsuario                     BIGINT,
       IN p_IdModulo                      NVARCHAR(3),
       
       IN p_simularOperacion              NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
       IN p_UsaReglasNegocio              NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
       IN p_EsIngresoAutomatico           NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente                                    
                                                                                                                          
       -- Parametros de Salida
       OUT p_out_InformacionContador  	   INT,
       OUT p_out_AdvertenciasContador      INT,
       OUT p_out_ErroresContador           INT
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA
AS
BEGIN

	------------------------------
	--DECLARE Manejo de Errores
	------------------------------	
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
    
    BEGIN --Logica    
	    
	    --------------------
		--Negocio Aqui
		--------------------
		--Periodo abierto y a cerrar
		INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
		 ("IdPeriodoCierre", "Periodo", "IdEstadoPeriodo", "IdEstado", "IdUsuario", "FechaIngresoRegistro")
		SELECT "BSCL.Facturacion.BD.Secuencias::PeriodoCierre".Nextval, "Periodo", 'CER', 'VIG', :p_IdUsuario, CURRENT_UTCTIMESTAMP
		FROM "BSCL.Facturacion.BD::GBL.ProcesarPeriodoCierre_IN_PeriodoCierre"
		WHERE "IdEstadoPeriodo" = 'ABT';
		
		
		--Periodo cerrado y a abrir
		INSERT INTO "BSCL.Facturacion.BD::FAC.PeriodoCierre"
		 ("IdPeriodoCierre", "Periodo", "IdEstadoPeriodo", "IdEstado", "IdUsuario", "FechaIngresoRegistro")
		SELECT "BSCL.Facturacion.BD.Secuencias::PeriodoCierre".Nextval, "Periodo", 'ABT', 'VIG', :p_IdUsuario, CURRENT_UTCTIMESTAMP
		FROM "BSCL.Facturacion.BD::GBL.ProcesarPeriodoCierre_IN_PeriodoCierre"
		WHERE "IdEstadoPeriodo" = 'CER';
		
		
		Update PeriodoCierre
		Set PeriodoCierre."IdEstado" = 'ANU'
		From "BSCL.Facturacion.BD::FAC.PeriodoCierre" As PeriodoCierre
		  Inner Join "BSCL.Facturacion.BD::GBL.ProcesarPeriodoCierre_IN_PeriodoCierre" As GblPerCierre
		     On GblPerCierre."IdPeriodoCierre" =  PeriodoCierre."IdPeriodoCierre";
		
    END; --Logica    
END;
