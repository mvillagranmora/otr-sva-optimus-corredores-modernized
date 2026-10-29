SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ConsultarCarterasParaPrestamos";

CREATE PROCEDURE
-- 
--  Proposito: Identificar las carteras que pueden ser usadas para cubrir prestamos
--  Autor: ACC - BCS 
--   
"BSCL.Custodia.BD.Servicios::PRT.ConsultarCarterasParaPrestamos" (
-- Parametros entrada comunes en todos los servicios publicos
--p_tblIdInstrumento=BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Entrada_tblIdInstrumento
	 IN p_IdEmpresa					BIGINT
	,IN p_IdUsuario					BIGINT
	,IN p_IdModulo						NVARCHAR(3)
	,IN p_idCliente						BIGINT default 0		
	,IN p_idPortafolio					BIGINT default 0

--SALIDAS
-- p_out_AdvertenciasDetalle=BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_AdvertenciasDetalle;
-- p_out_ErroresDetalle=BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_ErroresDetalle;
-- p_out_DisponiblePrestamos=BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_DisponiblePrestamos;
	,OUT p_out_AdvertenciasContador INT
	,OUT p_out_ErroresContador 		INT
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	 BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) into p_out_ErroresContador 		FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_ErroresDetalle";
		SELECT COUNT(1) into p_out_AdvertenciasContador FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_AdvertenciasDetalle";
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			
			INSERT INTO "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_ErroresDetalle"("Codigo","Descripcion")
			SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;
			
			SELECT COUNT(1) INTO p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_ErroresDetalle";					
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_RutCliente 	NVARCHAR(100)	:= 'RUT';
		DECLARE v_NombreCliente NVARCHAR(100)	:= 'NOMBRECLIENTE';
		DECLARE	v_fechaCondLiq	NVARCHAR(10);
		DECLARE v_FechaLiquidacion	DATE;
		DECLARE v_fechaConsulta NVARCHAR(10);
		
		DECLARE v_tblIdClientePrestamistas table ("IdCliente" BIGINT, "IdPortafolio" BIGINT, "RazonSocial" NVARCHAR(100))
											DEFAULT SELECT 0 AS "IdCliente", 0 AS "IdPortafolio", '' AS "RazonSocial" FROM dummy WHERE 1=2;
		DECLARE v_cantInstrumentos BIGINT := 0;
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
/*		BEGIN
			DECLARE v_resultadoRegla nvarchar(40);
			--validacion invalidante configuración básica del sistema
			--CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_out_ErroresDetalle,:v_resultadoRegla);
			--if :v_resultadoRegla = 'RECH' THEN
			--	SIGNAL SALIDA_CONTROLADA; 	
			--END IF;
			--validacion invalidante (No continua, lleva signal)
			IF length(v_RutCliente)<2 THEN
				v_hash= SELECT 'CLIENTE_RUT' AS "Codigo",:v_RutCliente AS "Valor" FROM DUMMY UNION ALL
				        SELECT 'CLIENTE_NOMBRE',:v_NombreCliente FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RUT_INVALIDO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  SELECT 'RUT_INVALIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
				SIGNAL SALIDA_CONTROLADA; 		  
			END IF;		
			--validacion sin signal
			IF length(v_RutCliente)<2 THEN
				v_hash= SELECT 'CLIENTE_RUT' AS "Codigo",:v_RutCliente AS "Valor" FROM DUMMY UNION ALL
				        SELECT 'CLIENTE_NOMBRE',:v_NombreCliente FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RUT_INVALIDO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  SELECT 'RUT_INVALIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
			END IF;				
			--Si existen errores en las validaciones debe salir
			SELECT COUNT(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
*/
		-----------------------------------------------------
		--Validaciones via Motor (solo complejas)
		--(Normalmente no deberia usarse este bloque de codigo ) 
		-----------------------------------------------------	

		--------------------
		--Reglas Negocio 
		--------------------	
			
		--------------------
		--Negocio Aqui
		--------------------

		--fecha del sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaLiquidacion);
		
									 
		/****** Implementar el filtro de clientes prestamistas de titulos
		------- cambiar esto cuando se defina el modelo de atributos del cliente
		******/
		v_tblIdClientePrestamistas = SELECT por."IdCliente", por."IdPortafolio", cli."RazonSocial"
									FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS por
									INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cli ON cli."IdCliente" = por."IdCliente";
									
		--SELECT 'clipresta' AS "clipresta", * FROM :v_tblIdClientePrestamistas;
																														
								
		INSERT INTO "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_DisponiblePrestamos" ("IdCliente","IdPortafolio","IdInstrumento",
			"Nemotecnico","CantidadDisponible","MontoDisponible")
		SELECT  cli."IdCliente" AS "IdCliente"
				, cli."IdPortafolio"  AS "IdPortafolio"
				, car."IdInstrumento"  AS "IdInstrumento"
				, car."Nemotecnico"
				, CAST(SUM(car."INGCantidad") AS DECIMAL(26,8))  AS "CantidadDisponible"
				, CAST(SUM(car."INGMontoATasaCompraActual") AS DECIMAL(26,8))  AS "MontoDisponible"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" AS car
		INNER JOIN :v_tblIdClientePrestamistas AS cli ON cli."IdCliente" = car."IdCliente" and cli."IdPortafolio" = car."IdPortafolio"
		
		WHERE 
			car."IdEmpresa" = p_IdEmpresa AND
			car."IdCliente" = CASE  WHEN p_IdCliente > 0 THEN p_IdCliente else car."IdCliente" END AND
			car."IdPortafolio" = CASE  WHEN p_IdPortafolio > 0 THEN p_IdPortafolio else car."IdPortafolio" END AND
			car."INGFechaLiquidacion" <= :v_FechaLiquidacion
		GROUP BY cli."IdCliente", cli."IdPortafolio", car."IdInstrumento", car."Nemotecnico";
			
		
		
		
		SELECT COUNT(1) INTO v_cantInstrumentos FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Entrada_tblIdInstrumento";
		IF v_cantInstrumentos > 0 THEN
			-- si viene algun(os) instrumento(s) especificado(s) en la tabla, acorta la salida al/los instrumento(s) solicitado(s)
			DELETE FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Salida_DisponiblePrestamos"
			WHERE "IdInstrumento" NOT IN (
				SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::GBL.ConsultarCarterasParaPrestamos_Entrada_tblIdInstrumento"
			);
		END IF;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;









