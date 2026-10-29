SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FormasDePagoOperacion";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::FormasDePagoOperacion" ( 
	--Entrada
	IN p_IdEmpresa 		BIGINT,	 
	IN p_IdOperacion BIGINT	
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

				

		v_Formas_De_Pago = SELECT 
								 *
							 FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
							 WHERE "FolioOperacion" = p_IdOperacion
							 AND "IdTipoMovimiento" = 'MOVING';
							 
			BEGIN
			
				SELECT * FROM :v_Formas_De_Pago;
				
			END;	
					
END;

