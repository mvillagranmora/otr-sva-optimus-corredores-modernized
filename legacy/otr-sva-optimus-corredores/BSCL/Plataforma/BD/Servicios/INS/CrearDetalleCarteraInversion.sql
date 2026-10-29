SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.CrearDetalleCarteraInversion";

CREATE PROCEDURE
--  Proposito: crear detalle cartera inversion
--  Autor: - JGOL
--  Fecha Creacion: 17/11/2017 
 
"BSCL.Plataforma.BD.Servicios::INS.CrearDetalleCarteraInversion" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	    BIGINT, 
    IN p_IdUsuario	                BIGINT,
    IN p_IdModulo                   NVARCHAR(3),
    IN p_NombreCarteraInversion		NVARCHAR(40),
	IN p_IdInstrumento				BIGINT,
	IN p_IdEstado				    NVARCHAR(3) default 'ACT',					
	OUT p_out_AdvertenciasContador		INT,
	OUT p_out_AdvertenciasDetalle		TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
	insert into "BSCL.Plataforma.BD::INS.DetalleCarteraInversion" (
		"NombreCarteraInversion",
		"IdEmpresa",
		"IdInstrumento"
	)select 
		p_NombreCarteraInversion,
		p_IdEmpresa,
		p_IdInstrumento 
	from dummy;
	
END;
