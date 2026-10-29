SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::UAFDOF";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar información para emitir Transacciones con declaración origen de fondos
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/11/2016

 "BSCL.Normativa.CrystalReports.Servicios::UAFDOF" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    --VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_Monto		 							DECIMAL(27,8)	DEFAULT 0,
	IN p_IdSucursal 							BIGINT			DEFAULT 0,
	IN p_IdEmpleado								BIGINT			DEFAULT 0,
	IN p_IdCliente								BIGINT			DEFAULT 0,
	IN p_Declaracion							NVARCHAR(1)	DEFAULT ''--'S o N'

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_Count								 		 BIGINT:=0;
	
    	DECLARE v_logo TABLE("LogoCorredora" BLOB);
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora" 
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
	
	
		
		CALL "BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF"
		(	p_IdEmpresa 						=>p_IdEmpresa,
			p_FechaOperacionDesde 				=>p_FechaOperacionDesde,
			p_FechaOperacionHasta 				=>p_FechaOperacionHasta,
			p_Monto 							=>p_Monto,
			p_IdSucursal 						=>p_IdSucursal,
			p_IdEmpleado						=>p_IdEmpleado,
			p_out_TablaDOF 						=>v_tablaOperaciones
		); 
		
		
		
			 
		--Verificar si existen operaciones 
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones WHERE "Declaracion"='S';
 				 
		IF v_Count > 0 THEN
				SELECT * FROM :v_tablaOperaciones  
				CROSS JOIN :v_datosGenerales
				CROSS JOIN :v_logo WHERE "Declaracion"='S';
		ELSE

				SELECT
						 '' AS "p_FechaOperacionDesde"
						,'' AS "p_FechaOperacionHasta"
						,'' AS "LogoCorredora"
						,'' AS "RazonCorredora" 
						,'' AS "DireccionCorredora" 
						,'' AS "RutCorredora" 
						,'' AS "TelefonoCorredora" 
						,  0 AS "IdComprobanteFormaDePago"
						,'' AS "FechaLiquidacion"
						, 0 AS "IdComprobante"
						,'' AS "IdTipoOperacion"
						,'' AS "TipoOperacion"
						, 0 AS "IdPersona"
						,'' AS "Identificacion"
						,'' AS "TipoDocumento"
						,'' AS "TipoPersona"
						,'' AS "NombreCompleto"
						,'' AS "RazonSocial"
						,'' AS "ApellidoPaterno"
						,'' AS "ApellidoMaterno"
						,'' AS "Telefono"
						, 0 AS "IdPersonaTitularPago"
						,'' AS "NombreCompletoPagador"
						,'' AS "IdentificacionPagador"
						,'' AS "TipoDocumentoPagador"
						, 0 AS "MontoOperacion"
						,'' AS "IdMonedaOperacion"
						, 0 AS "MontoComprobanteFormaDePago"
						,'' AS "IdMonedaFormaDePago"
						,'' AS "MonedaFormaDePago"				
						,'' AS "IdMedioDePago"
						,'' AS "NombreMedioDePago" 
						,'' AS "IdModalidadDePago"
						,'' AS "NombreModalidadDePago"
						, 0 AS "IdEmpleadoDigitador"
						,'' AS "NombreAgente"
						, 0 AS "IdSucursal"
						,'' AS "CodigoSucursal"
						,'' AS "NombreSucursal"
						, 0 AS "IdCliente"
						, 0 AS "IdDeclaracionOrigenFondos"
						,'' AS "Comentario"
						,'' AS "Declaracion"
						,'' AS "DeclaracionUI"
						FROM DUMMY WHERE 1=2;	
		
		
		END IF;
		
		
END; 