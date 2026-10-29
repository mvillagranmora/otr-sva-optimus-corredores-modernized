SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.CanalCliente.BD.Servicios::ConsultarAgenteDelPortafolio";
-- 
--  Proposito: Servicio para obtener la informacion del agente al cual esta asignado el cliente.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 20/12/2016
--  
CREATE PROCEDURE "BSCL.CanalCliente.BD.Servicios::ConsultarAgenteDelPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_Token							NVARCHAR(520),
	IN p_IdPortafolio					BIGINT,

	--SALIDAS
	OUT p_out_IdEmpleado				BIGINT,	
	OUT p_out_CodigoEmpleado			NVARCHAR(10),
	OUT p_out_NombreCompleto			NVARCHAR(500),
	OUT p_out_IdDireccionElectronica	BIGINT,
	OUT p_out_DireccionElectronica		NVARCHAR(100),
	OUT p_out_IdTelefono				BIGINT,
	OUT p_out_Telefono					NVARCHAR(20),
	
	OUT p_out_StatusCode				INT,
	OUT p_out_StatusMessage				NVARCHAR(2000)
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------		
	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_IdEmpresa BIGINT;
		DECLARE v_IdUsuario BIGINT;
		DECLARE v_NombreUsuario NVARCHAR(100);
		DECLARE v_TipoEntidad NVARCHAR(3);
		DECLARE v_IdEntidad BIGINT;
		DECLARE v_TokenValido NVARCHAR(1);
		DECLARE v_Mensaje NVARCHAR(80);
		
		DECLARE v_IdPortafolio BIGINT;
		DECLARE v_IdEmpleadoReemplazo BIGINT;
		DECLARE v_IdEmpleado BIGINT;
		DECLARE v_Existe BIGINT;
		
		-----------------------------------------------------
		--Logica de negocio
		-----------------------------------------------------
		CALL "BSCL.CanalCliente.BD.Servicios::ValidarTokenUsuarioCliente"(
			p_Token => p_Token,
			-- Salida Info sesion
			p_out_IdEmpresa => v_IdEmpresa,
			p_out_IdUsuario => v_IdUsuario,
			p_out_NombreUsuario => v_NombreUsuario,
			p_out_TipoEntidad => v_TipoEntidad,
			p_out_IdEntidad => v_IdEntidad,			
			p_out_TokenValido => v_TokenValido,
			p_out_Mensaje => v_Mensaje
		);
		IF v_TokenValido = 'S' AND v_Mensaje = 'OK++' THEN
		
			SELECT COUNT("IdCliente") INTO v_Existe
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
			WHERE "IdCliente" = :v_IdEntidad;
			
			IF :v_Existe > 0 THEN
				
				SELECT COUNT("IdPortafolio") INTO v_Existe 
				FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
				WHERE "IdCliente" = :v_IdEntidad and "IdPortafolio" = :p_IdPortafolio;
				
				IF v_Existe > 0 THEN
					SELECT "IdPortafolio", "IdEmpleadoEjecutivo" INTO v_IdPortafolio, v_IdEmpleado
					FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
					WHERE "IdCliente" = :v_IdEntidad and "IdPortafolio" = :p_IdPortafolio;
					
					IF v_IdEmpleado > 0 THEN
						
						--Verificar si el empleado esta siendo reemplazado.
						SELECT COUNT("IdEmpleadoEjecutivo") INTO v_Existe
						FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" CEP
						WHERE CEP."IdEmpleadoEjecutivo" = :v_IdEmpleado AND "IdCliente" = :v_IdEntidad
						AND TO_DATE(CURRENT_UTCTIMESTAMP) BETWEEN CEP."FechaInicioReemplazo" AND CEP."FechaFinReemplazo" AND CEP."IdEstado" = 'ACT';

						IF v_Existe > 0 THEN
							SELECT "IdEmpleadoReemplaza" INTO v_IdEmpleado
							FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" CEP
							WHERE CEP."IdEmpleadoEjecutivo" = :v_IdEmpleado AND "IdCliente" = :v_IdEntidad
							AND TO_DATE(CURRENT_UTCTIMESTAMP) BETWEEN CEP."FechaInicioReemplazo" AND CEP."FechaFinReemplazo" AND CEP."IdEstado" = 'ACT';
						END IF;
										
						SELECT 
							"IdEmpleado", "CodigoEmpleado", "NombreCompleto", "IdDireccionElectronicaPrincipal", "Direccion", "IdTelefonoPrincipal", "Numero" INTO p_out_IdEmpleado, p_out_CodigoEmpleado, p_out_NombreCompleto, p_out_IdDireccionElectronica, p_out_DireccionElectronica, p_out_IdTelefono, p_out_Telefono
						FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CE."IdDireccionElectronicaPrincipal"
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono" = CE."IdTelefonoPrincipal"
						WHERE "IdEmpleado" = :v_IdEmpleado;

						p_out_StatusCode :=0;
					ELSE
						p_out_StatusCode :=2;
						p_out_StatusMessage := 'El portafolio no tiene un agente';
					END IF;
					
					--select * FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolioSimple" ORDER BY "FechaInicioReemplazo" DESC
					
				END IF;

			END IF;
		ELSE
			p_out_StatusCode :=1;
			p_out_StatusMessage := v_Mensaje;
		END IF;
	END;
END;
