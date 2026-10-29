SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EliminarTipoCriterioNeteo";

CREATE PROCEDURE
-- 
--  Proposito: 
--   
 
"BSCL.Plataforma.BD.Servicios::EliminarTipoCriterioNeteo" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdTipoCriterioNeteo BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
	p_out_AdvertenciasContador	:= 1;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

	BEGIN
		

		DECLARE ID_CRITERIO_NETEO INT;
		declare v_pcli_out_AdvertenciasContador int;
		declare v_pcli_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		declare v_pcli_out_ErroresContador int;
		declare v_pcli_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		
		--quitar referencia
		update "BSCL.Plataforma.BD::PLA.Concepto" set "IdTipoCriterioNeteo" = null 
		where "IdTipoCriterioNeteo" = :p_IdTipoCriterioNeteo;
		
		--eliminar registro
		delete from "BSCL.Plataforma.BD::PLA.TipoCriterioNeteo"
		where "IdTipoCriterioNeteo" = :p_IdTipoCriterioNeteo;
		
		--recalcular matriz
		call "BSCL.Plataforma.BD.Servicios::CalculaPreferenciaCalceLibroInventario"(
			p_IdEmpresa => :p_IdEmpresa,
		    p_IdUsuario => :p_IdUsuario,
		    p_IdModulo  => :p_IdModulo,
		    p_out_AdvertenciasContador => :v_pcli_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle => :v_pcli_out_AdvertenciasDetalle,
			p_out_ErroresContador => :v_pcli_out_ErroresContador,
			p_out_ErroresDetalle => :v_pcli_out_ErroresDetalle
		);

		p_out_ErroresContador := 0;

	END;
	

END;
