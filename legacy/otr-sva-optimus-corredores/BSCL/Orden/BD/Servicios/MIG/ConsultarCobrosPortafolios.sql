SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Orden.BD.Servicios::MIG.ConsultarCobrosPortafolios";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::MIG.ConsultarCobrosPortafolios" ( 

	IN  p_IdEmpresa					BIGINT,
	IN  p_IdUsuario					BIGINT,
	IN  p_IdModulo					NVARCHAR(3),
	
	OUT p_out_MatrizOperacional TABLE (
									"IdInterno"					BIGINT,
									"IdPortafolio"				BIGINT,
									"IdTipoProducto"			NVARCHAR(4),
									"IdTipoOperacion" 			NVARCHAR(6), 
									"IdCanalIngreso"			NVARCHAR(4),
									"IdBolsa" 					NVARCHAR(10),
									"IdCorredorCorresponsal" 	BIGINT,
									
									"IdCobro"					NVARCHAR(40),
									"IdOrigenCobro"				NVARCHAR(40),
									"ValorCobro"				DECIMAL(26,8),
									"FormaCobro"				NVARCHAR(1),
									"ValorTopeMinimoOrden"		DECIMAL(26,8),
									"IdMoneda"					NVARCHAR(3),
									"Prioridad" 				BIGINT,
									"DiasPlazo"					BIGINT,
									"NombreCobro"				NVARCHAR(100),
									"IdCobroClasificacion"		BIGINT,
									"CodigoCobroClasificacion"	NVARCHAR(40),
									"PermiteCambiarValor"		NVARCHAR(1),
									"IdEmpresa" 				BIGINT
		),
		
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
							    
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN

	
	v_Portafolios = SELECT distinct "IdPortafolio","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdBolsa" 
					FROM "BSCL.Orden.BD::MIG.ConsultarCobrosPortafolios_Entrada";
	
	--select 'v_Portafolios',* from :v_Portafolios;
							
	
	CALL "BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalPortafolios" (
		p_IdEmpresa		=> :p_IdEmpresa,
		p_IdUsuario		=> :p_IdUsuario,
		p_IdModulo		=> :p_IdModulo,
		p_Portafolios	=> :v_Portafolios,
	
		p_out_MatrizOperacional		=> :v_MatrizOperacional,
		p_out_AdvertenciasContador	=> :p_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle	=> :p_out_AdvertenciasDetalle,
		p_out_ErroresContador		=> :p_out_ErroresContador,
		p_out_ErroresDetalle		=> :p_out_ErroresDetalle);
	
	
	v_C1 = select port."IdInterno",mat.*
			from "BSCL.Orden.BD::MIG.ConsultarCobrosPortafolios_Entrada" port
			inner join :v_MatrizOperacional mat on 
				port."IdPortafolio" = mat."IdPortafolio" 
				and port."IdTipoOperacion" = mat."IdTipoOperacion"
				and port."IdTipoProducto" = mat."IdTipoProducto"
				and port."IdCanalIngreso" = mat."IdCanalIngreso"
				and port."IdBolsa" = mat."IdBolsa" and "DiasPlazo" = 0;
	
	select 'v_C1',* from :v_C1;
						
	v_cant_cobros = select c1."IdInterno",count(c1."IdCobro") as "CantCobros"
						from :v_C1 c1
						group by c1."IdInterno";
	
	select 'v_cant_cobros',* from :v_cant_cobros;
	
	
	v_CobrosCorresponsal = SELECT cc."IdInterno",port."IdPortafolio",port."IdTipoProducto",port."IdTipoOperacion",port."IdCanalIngreso",port."IdBolsa",port."IdCorredorCorresponsal",
							corr."IdCobro",'CORRESPONSAL' as "IdOrigenCobro",corr."ValorCobro",
							 
							
							cob."FormaCobro",cob."ValorTopeMinimoOrden",cob."IdMoneda",0 as "Prioridad",0 as "DiasPlazo",cob."NombreCobro",cob."IdCobroClasificacion",
							cob."CodigoCobroClasificacion",cob."PermiteCambiarValor",cob."IdEmpresa"
							
							FROM :v_cant_cobros cc
							INNER JOIN "BSCL.Orden.BD::MIG.ConsultarCobrosPortafolios_Entrada" port on cc."IdInterno" = port."IdInterno" and cc."CantCobros" > 0
							INNER JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr on port."IdCorredorCorresponsal" IS NOT NULL AND port."IdCorredorCorresponsal" = corr."IdCorredorCorresponsal"
							INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" cob on cob."IdCobro" = corr."IdCobro";
	
	select 'v_CobrosCorresponsal',* from :v_CobrosCorresponsal;
	
	p_out_MatrizOperacional = SELECT port."IdInterno",outt."IdPortafolio",outt."IdTipoProducto",outt."IdTipoOperacion",outt."IdCanalIngreso",outt."IdBolsa",port."IdCorredorCorresponsal",
									
									"IdCobro",'PORTAFOLIO' AS "IdOrigenCobro","ValorCobro","FormaCobro","ValorTopeMinimoOrden","IdMoneda","Prioridad","DiasPlazo","NombreCobro","IdCobroClasificacion",
									"CodigoCobroClasificacion","PermiteCambiarValor","IdEmpresa"
									FROM :v_C1 outt
									LEFT JOIN "BSCL.Orden.BD::MIG.ConsultarCobrosPortafolios_Entrada" port on 
									port."IdInterno" 	= outt."IdInterno"
							UNION ALL
							SELECT * FROM :v_CobrosCorresponsal;
							
						
 

END; 
