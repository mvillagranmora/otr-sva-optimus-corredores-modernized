SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DemoServicioPrivado";

CREATE PROCEDURE "BSCL"."BSCL.Plataforma.BD.Servicios::DemoServicioPrivado" (
	-- Parametros entrada comunes en todos los servicios privados
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	
	-- Parametros entrada del servicio privado
	IN  p_GeneraErrorRut			nvarchar(1),
	IN  p_GeneraErrorNoControlado	nvarchar(1),
	
	-- Parametros salida del servicio privado
	OUT p_out_Mensaje 				nvarchar(520)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--declara errores  (debe estar registrado en tabla CodigoError y Diccionario) 
	DECLARE ERROR_RUT_INVALIDO CONDITION FOR SQL_ERROR_CODE 10001;
	
	if p_GeneraErrorRut = 'S' then
		SIGNAL ERROR_RUT_INVALIDO;
	end if;	
	

	if p_GeneraErrorNoControlado = 'S' then
		declare v_sum int;
		v_sum := 1 / (1-1);
	end if;	
				
	p_out_Mensaje := 'DEMO OK+';
END;
