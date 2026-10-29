SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarAtributos";

CREATE PROCEDURE
-- 
--  Proposito: Devuelve atributos de la entidad especificada
--  Autor: - BCS
--  Fecha Creacion: 02/12/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarAtributos" (

IN p_IdEmpresa		BIGINT,
IN p_IdEntidad		BIGINT,
IN p_IdTipoEntidad	NVARCHAR(40),
IN p_EsPreferencia		NVARCHAR(1),

OUT p_out_AtributosContador BIGINT,
OUT p_out_Atributos table (	"IdAtributo"    		BIGINT
							,"IdTipoAtributo" 		BIGINT
							,"NombreTipoAtributo" 	NVARCHAR(255)
							,"ClaseDato"			NVARCHAR(40)
							,"ValorAtributo"		NVARCHAR(255)
							,"IdEstadoAutorizado"	NVARCHAR(3)
							,"GrupoAtributo"		NVARCHAR(40)
							,"EsPreferencia"			NVARCHAR(1)
						   ),
OUT	p_out_GrupoAtributosContador BIGINT,					   
OUT p_out_GrupoAtributos TABLE ("GrupoAtributo" NVARCHAR(40)),
						   
OUT p_out_AdvertenciasContador int,
OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
OUT p_out_ErroresContador int,
OUT p_out_ErroresDetalle table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))    
						   
						   )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

		
	p_out_GrupoAtributos = SELECT TPO."GrupoAtributo"
	FROM "BSCL.Plataforma.BD::PLA.TipoAtributo" TPO
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Atributo" ATR ON 
	ATR."IdAtributo" = TPO."IdTipoAtributo" AND ATR."IdEntidad" = p_IdEntidad 
	WHERE TPO."IdTipoEntidad" = p_IdTipoEntidad AND TPO."EsPreferencia" = p_EsPreferencia AND TPO."IdEstado" = 'ACT'
	AND TPO."IdEmpresa" = p_IdEmpresa
	GROUP BY TPO."GrupoAtributo";
	
	select count(1) into p_out_GrupoAtributosContador from :p_out_GrupoAtributos;
	
	p_out_Atributos =
	SELECT IFNULL(ATR."IdAtributo",0) AS "IdAtributo",
	TPO."IdTipoAtributo",
	TPO."NombreTipoAtributo", 
	TPO."ClaseDato", 
	IFNULL(ATR."ValorAtributo",' ') as "ValorAtributo", 
	IFNULL(ATR."IdEstadoAutorizacion",' ') as "IdEstadoAutorizado",
	TPO."GrupoAtributo",
	TPO."EsPreferencia"
	FROM "BSCL.Plataforma.BD::PLA.TipoAtributo" TPO
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Atributo" ATR ON 
	ATR."IdTipoAtributo" = TPO."IdTipoAtributo" AND ATR."IdEntidad" = p_IdEntidad 
	WHERE TPO."IdTipoEntidad" = p_IdTipoEntidad AND TPO."EsPreferencia" = p_EsPreferencia AND TPO."IdEstado" = 'ACT'
	AND TPO."IdEmpresa" = p_IdEmpresa;
	
	select count(1) into p_out_AtributosContador from :p_out_Atributos;

	

END;