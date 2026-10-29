SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TRA.CrearOperadorCarteraEjecutivo";

CREATE PROCEDURE
--  Proposito: crear operador cartera Ejecutivo
--  Autor: -
--  Fecha Creacion: 09/04/2019 
 
"BSCL.Plataforma.BD.Servicios::TRA.CrearOperadorCarteraEjecutivo" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	    BIGINT, 
    IN p_IdUsuario	                BIGINT,
    IN p_IdModulo                   NVARCHAR(3),
    IN p_NombreCarteraEjecutivo		NVARCHAR(40),
	IN p_CodigoOperador				NVARCHAR(40),
	IN p_IdEstado					NVARCHAR(3) default 'ACT',					
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN	

	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
	p_out_AdvertenciasDetalle = select 0 as "IdInterno",'' as "Codigo", '' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle = select 0 as "IdInterno",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	---------------------------------------------------------------------------------------
	insert into "BSCL.Plataforma.BD::TRA.OperadorCarteraEjecutivo" (
		"CodigoOperador",
		"NombreCarteraEjecutivo",
		"IdEmpresa",
		"IdEstado"
	)select 
		p_CodigoOperador,
		p_NombreCarteraEjecutivo,
		p_IdEmpresa,
		p_IdEstado 
	from dummy;
	
END;
