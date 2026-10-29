		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::Operaciones500000USD";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::Operaciones500000USD" ( 
	--Entrada
	IN p_FechaSistema 	DATE,
	IN p_IdEmpresa 		BIGINT	   
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

   	DECLARE v_countOperaciones 	BIGINT;
   		
	v_OperacionesSobre500000 = SELECT 
									MSP."IdMonedaPrincipal" AS "Moneda"
									,MSP."MontoMonedaPrincipal" AS "Monto"
									,MSP."CodigoComercio"
									,CC."NombreCompleto" AS "Cliente"
									,MSP."TipoCambioCierre"
									,:p_FechaSistema AS "Fecha"
									,MSP."TipoOperacion"
									,'' AS "MensajeError"
									FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" MSP
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
								ON MSP."IdCliente" = CC."IdCliente"
								WHERE ("Estado" = 'VIG'
								OR "Estado" = 'PEL'
								OR "Estado" = 'PDA')
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaSistema) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaSistema), 86399)
								AND (CASE WHEN "IdMonedaPrincipal" = 'USD' AND "IdMonedaSecundaria" != 'USD' THEN "MontoMonedaPrincipal" WHEN "IdMonedaPrincipal" != 'USD' AND "IdMonedaSecundaria" = 'USD' THEN "MontoMonedaPrincipalEquivalente" END) >= 500000;			     
	
	SELECT COUNT(*) INTO v_countOperaciones  FROM :v_OperacionesSobre500000;
	
	IF (v_countOperaciones > 0) THEN
		BEGIN	
			SELECT * FROM :v_OperacionesSobre500000;				
		END;	
	ELSE 
		v_OperacionesSobre500000vacio = SELECT 
											'' AS "Moneda"
											,0 AS "Monto"
											,0 AS "CodigoComercio"
											,'' AS "Cliente"
											,0 AS "TipoCambioCierre"
											,:p_FechaSistema AS "Fecha"
											,'' AS "TipoOperacion"
											,'No se encontraron datos.' AS "MensajeError"
										FROM DUMMY;
		BEGIN	
			SELECT * FROM :v_OperacionesSobre500000vacio;				
		END;
	END IF;				
END;
