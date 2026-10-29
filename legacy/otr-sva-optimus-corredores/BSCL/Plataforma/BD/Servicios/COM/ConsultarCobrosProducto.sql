SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProducto";
CREATE PROCEDURE 
-- 
--  Proposito: Consulta los cobros asociados a un producto
--  Autor: csi
--  Fecha Creacion: 31/08/2015
--  
"BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProducto"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa            BIGINT,
    IN p_IdUsuario            BIGINT,
    IN p_IdModulo             NVARCHAR(3),
    IN p_IdPerfilCobros		  NVARCHAR(40),
	IN p_IdBolsa			  NVARCHAR(10),
	IN p_IdTipoProducto		  NVARCHAR(4),
	IN p_IdTipoOperacion	  NVARCHAR(6),
	IN p_IdCanalIngreso		  VARCHAR(3),
	IN p_DiasPlazo			  BIGINT,    
    -- Salida Producto
	OUT p_out_ConsultarPerfilCobros TABLE (
							"IdCobro" 					NVARCHAR(10),
							"ValorCobroPerfil"			DECIMAL(26,8),
							"ValorCobroAplicado"		DECIMAL(26,8),
							"MontoMinimo" 				DECIMAL(26,8),
							"IdMoneda" 					NVARCHAR(3),
							"NombreCobro" 				NVARCHAR(100),
							"IdCobroClasificacion" 		BIGINT,
							"FormaCobro" 				NVARCHAR(1),
							"CodigoCobroClasificacion" 	NVARCHAR(40),
							"PermiteCambiarValor" 		NVARCHAR(1)
							)	 
  
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	
	v_cobros = select  "IdCobro",
						"ValorCobro" AS "ValorCobroPerfil",
						"ValorCobro" AS "ValorCobroAplicado",
						"ValorTopeMinimoOrden" as "MontoMinimo",
						"IdMoneda",
						"NombreCobro",
						"IdCobroClasificacion",
						"FormaCobro",
						"Prioridad",
						"DiasPlazo",
						"IdInternal",
						"CodigoCobroClasificacion",
						"PermiteCambiarValor"
				from "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" 
				where  ("IdPerfilCobros"=:p_IdPerfilCobros)  AND   
					   ("EstadoPerfilCobros"='ACT') AND   
					   ("EstadoDetalle"='ACT') AND   
					  -- ("EstadoGrupoCobroNormal"='ACT') AND  
					  -- ("EstadoDetalleGrupoCobroNormal"='ACT') AND   
					   ("IdEmpresa"=:p_IdEmpresa)  AND 
					   ("IdBolsa"=:p_IdBolsa or "IdBolsa"='0')  AND 
					   ("IdTipoProducto"=:p_IdTipoProducto or "IdTipoProducto"='0') AND  
					   ("IdTipoOperacion"=:p_IdTipoOperacion or "IdTipoOperacion"='0') AND   
					   ("IdCanalIngreso"=:p_IdCanalIngreso or "IdCanalIngreso"='0')  AND  
					   ("DiasPlazo" <= :p_DiasPlazo );  
		   
	v_cobros_pre = select *,ROW_NUMBER() over (partition by "IdCobro" order by "IdCobro","Prioridad","DiasPlazo") as "GRUPO"
	                 from  :v_cobros;					   
					  			  					   
	p_out_ConsultarPerfilCobros = select 	vc."IdCobro",
										  	vc."ValorCobroPerfil",
											vc."ValorCobroAplicado",
											vc."MontoMinimo",
											vc."IdMoneda",
											vc."NombreCobro",
											vc."IdCobroClasificacion",
											vc."FormaCobro",
											vc."CodigoCobroClasificacion",
											vc."PermiteCambiarValor"
									from :v_cobros as vc, :v_cobros_pre as vp
									where vc."IdInternal" = vp."IdInternal" and  vp."GRUPO"=1;
		
END;
