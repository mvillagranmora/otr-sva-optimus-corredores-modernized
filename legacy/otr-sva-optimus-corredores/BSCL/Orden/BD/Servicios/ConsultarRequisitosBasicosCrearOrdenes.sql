SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Orden.BD.Servicios::ConsultarRequisitosBasicosCrearOrdenes";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la orden.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: TEAM ORDENES
--  Fecha Creacion: 13-06-2016
--  
"BSCL.Orden.BD.Servicios::ConsultarRequisitosBasicosCrearOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
		  
	IN	p_IdEmpleado					BIGINT,
	IN	p_IdTipoOperacion				NVARCHAR(6),
	IN	P_IdPortafolio					BIGINT,
	
	OUT p_out_InformacionContador			INT,
    OUT p_out_InformacionDetalle 			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE SALIDA_CONTROLADA CONDITION;      	
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT  SELECT 0 as "IdInterno",0 as "IdOperacion", '' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
																	FROM DUMMY WHERE 1=2;
	DECLARE v_EmpleadoTienePlantilla NVARCHAR(1);
		
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);


	END;
	
	
	SELECT CASE WHEN det1."IdTipoOperacion" IS NOT NULL THEN 'S' WHEN det2."IdTipoOperacion" IS NOT NULL THEN 'S' ELSE 'N' END INTO v_EmpleadoTienePlantilla
	FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = :p_IdTipoOperacion
	LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" pnt on emp."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado"
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det1 on det1."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det1."IdEstado" = 'ACT' and det1."IdTipoProducto" = tipoope."IdTipoProducto" and det1."IdTipoOperacion" = :p_IdTipoOperacion
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det2 on det2."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det2."IdEstado" = 'ACT' and det2."IdTipoProducto" = tipoope."IdTipoProducto" and det2."IdTipoOperacion" = '0'
	WHERE emp."IdEmpleado" = :p_IdEmpleado;
	
	IF (:v_EmpleadoTienePlantilla = 'N') THEN
		v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion", 'RECHAZO' as "Resultado",'PRODUCTO_NO_HABILITADO_EMPLEADO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY; 

	END IF;
	
	
	SIGNAL SALIDA_CONTROLADA;				
	
	
END;
