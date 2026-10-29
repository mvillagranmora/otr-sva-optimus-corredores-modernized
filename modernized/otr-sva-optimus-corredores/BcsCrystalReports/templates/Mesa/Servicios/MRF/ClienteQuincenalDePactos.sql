SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::MRF.ClienteQuincenalDePactos";
GO
CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::MRF.ClienteQuincenalDePactos" ( 
	--Entrada
	IN p_Fecha 			DATE,
 	IN p_IdEmpresa		BIGINT,
	IN p_IdUsuario		BIGINT	   
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

	DECLARE v_IdCliente BIGINT;
	DECLARE v_countDatos BIGINT;

	
	SELECT 
    	"IdCliente" INTO v_IdCliente
    FROM "BSCL.Mesa.BD::MES.Mesa" 
    WHERE "PorDefecto" = 'S' 
    AND "IdTipoProducto"='MERF';
           			
  	v_datosCliente = 	SELECT 
       						 CC."RazonSocial" 			AS "RazonSocial"
       						,CPD."Calle"				AS "Direccion"
       						,CC."DocumentoIdentidad" 	AS "Rut"
       						,CP."CodigoInscripcion"		AS "INSC"
       						,:p_Fecha					AS "FechaParametro"
       						,'' 						AS "MensajeError"
       					FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
       					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPD
       					ON CC."IdDireccionFisicaPrincipal" = CPD."IdDireccionFisica"
       					LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" CP
       					ON CC."IdPersona" = CP."IdPersona"
       					WHERE "IdCliente" = :v_IdCliente;
		
	SELECT COUNT(*) INTO v_countDatos  FROM :v_datosCliente;

	IF (v_countDatos > 0) THEN
		BEGIN	
			SELECT * FROM :v_datosCliente;
		END;	
	ELSE 
		v_datosClienteVacio = 	SELECT 
			 						'' 							AS "RazonSocial"  
									,'' 						AS "Direccion"
									,'' 						AS "Rut"
									,'' 						AS "INSC"
									,:p_Fecha					AS "FechaParametro"
									,'No se encontraron datos.' AS "MensajeError" 
								FROM DUMMY;
		BEGIN	
			SELECT * FROM :v_datosCliente;				
		END;
	END IF;				
					
END;


				
			