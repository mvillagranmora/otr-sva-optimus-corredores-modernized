SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProductoMM";
CREATE PROCEDURE 
-- 
--  Proposito: Consulta los cobros asociados a un producto
--  Autor: csi
--  Fecha Creacion: 31/08/2015
--  
"BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProductoMM"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa            BIGINT,
    
    IN p_Portafolios_2		TABLE(
    							"IdInterno"			BIGINT,
    							"IdPerfilCobros"	NVARCHAR(40),
    							"IdBolsa"			NVARCHAR(3),
    							"IdTipoProducto"	NVARCHAR(4),
    							"IdTipoOperacion"	NVARCHAR(6),
    							"IdTipoCanalIngreso"	NVARCHAR(3),
    							"DiasPlazo"				BIGINT
    							),
	OUT p_out_PerfilCobros TABLE(
							"IdInterno"					BIGINT,
							"IdCobro" 					NVARCHAR(10),
							"ValorCobroPerfil"			DECIMAL(26,8),
							"ValorCobroAplicado"		DECIMAL(26,8),
							"MontoMinimo" 				DECIMAL(26,8),
							"IdMoneda" 					NVARCHAR(3),
							"NombreCobro" 				NVARCHAR(100),
							"IdCobroClasificacion" 		BIGINT,
							"FormaCobro" 				NVARCHAR(1),
							"CodigoCobroClasificacion" 	NVARCHAR(40)
							)
  
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	
	v_cobros = select port."IdInterno", per.*
				from :p_Portafolios_2 port
				INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" per
				ON port."IdPerfilCobros" = per."IdPerfilCobros"
				where "EstadoPerfilCobros"='ACT' AND   
					  "EstadoDetalle"='ACT' AND   
					  "IdEmpresa"=:p_IdEmpresa AND 
					  (per."IdBolsa"= port."IdBolsa" or per."IdBolsa"='0') AND 
					  (per."IdTipoProducto"= port."IdTipoProducto" or per."IdTipoProducto"='0') AND  
					  (per."IdTipoOperacion"= port."IdTipoOperacion" or per."IdTipoOperacion"='0') AND   
					  (per."IdCanalIngreso"= port."IdTipoCanalIngreso" or per."IdCanalIngreso"='0') AND  
					  per."DiasPlazo" <= port."DiasPlazo";  
		   
	v_cobros_pre = select *,ROW_NUMBER() over (partition by "IdCobro" order by "IdCobro","Prioridad","DiasPlazo") as "GRUPO"
	                 from  :v_cobros;					   
					  			  					   
	p_out_PerfilCobros = select vc."IdInterno", vc."IdCobro", vc."ValorCobro" as "ValorCobroPerfil", vc."ValorCobro" as "ValorCobroAplicado", vc."ValorTopeMinimoOrden" as "MontoMinimo",
							vc."IdMoneda",vc."NombreCobro",vc."IdCobroClasificacion",vc."FormaCobro",vc."CodigoCobroClasificacion"
						from :v_cobros as vc, :v_cobros_pre as vp
						where vc."IdInternal" = vp."IdInternal" and  vp."GRUPO"=1;
		
END;
