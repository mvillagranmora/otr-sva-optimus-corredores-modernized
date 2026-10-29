set schema "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ObtenerSolicitudCircular";

--call "BSCL.Normativa.BD.Servicios::ObtenerSolicitudCircular" (1,?,?,TO_DATE('2010-01-12', 'YYYY-MM-DD'),TO_DATE('2010-01-12', 'YYYY-MM-DD'),?,TO_DATE('2010-01-12', 'YYYY-MM-DD'),TO_DATE('2010-01-12', 'YYYY-MM-DD'),'Proceso',6)


--update "BSCL.Normativa.BD::NOR.SolicitudCircular" set "FechaInicio" = TO_DATE('2010-01-12', 'YYYY-MM-DD') , "FechaFin" =TO_DATE('2010-01-12', 'YYYY-MM-DD'), "FechaVencimientoSolicitud"=TO_DATE('2010-01-12', 'YYYY-MM-DD'), "FechaIngresoSolicitud" = TO_DATE('2010-01-12', 'YYYY-MM-DD')



--select * from "BSCL.Normativa.BD::NOR.SolicitudCircular"

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ObtenerSolicitudCircular"
( 
    -- Parametros de Entrada
    IN p_IdTipoCircular 					  BIGINT  default 0,  
    IN p_IdCliente							  NVARCHAR(40) default '',
    IN p_Periodicidad       				  NVARCHAR(10) default '',
    IN p_FechaInicio  						  DATE default '0001-01-01',
    IN p_FechaFin  						 	  DATE default '0001-01-01',
    IN p_IdUsuario      					  NVARCHAR(10) default '', 
    IN p_FechaIngresoSolicitud			      DATE default '0001-01-01',
    IN p_FechaVencimientoSolicitud			  DATE default '0001-01-01',
    IN p_Estado     						  NVARCHAR(100) default '',
    IN p_SLA     							  BIGINT default 0
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	
		Select 
    	"SC"."IdSolicitudCircular",
		"SC"."IdTipoCircular",            
		"SC"."IdCliente",                 
		"SC"."Periodicidad",              
		"SC"."FechaInicio",               
		"SC"."FechaFin",                  
		"SC"."Usuario",                 
		"SC"."FechaIngresoSolicitud",     
		"SC"."FechaVencimientoSolicitud", 
		"SC"."EstadoSolicitud",           
		"SC"."SLA",                       
		"SC"."Archivo",
		"SC"."Observaciones",   
		"SC"."IdUsuario"	                
		from
		"BSCL.Normativa.BD::NOR.SolicitudCircular" as "SC"
		where 
		("SC"."IdTipoCircular" = CASE WHEN p_IdTipoCircular = 0 THEN "SC"."IdTipoCircular" ELSE p_IdTipoCircular END )AND 
		("SC"."IdCliente" = CASE WHEN p_IdCliente = '' THEN "SC"."IdCliente" ELSE p_IdCliente END )AND 
		("SC"."Periodicidad" =   CASE WHEN p_Periodicidad = '' THEN "SC"."Periodicidad" ELSE p_Periodicidad  END )AND 
		("SC"."FechaIngresoSolicitud" =    CASE WHEN p_FechaIngresoSolicitud = '0001-01-01' THEN "SC"."FechaIngresoSolicitud" ELSE p_FechaIngresoSolicitud  END )AND 
		("SC"."FechaVencimientoSolicitud" =  CASE WHEN p_FechaVencimientoSolicitud = '0001-01-01' THEN "SC"."FechaVencimientoSolicitud" ELSE p_FechaVencimientoSolicitud  END )AND 
		("SC"."SLA" =  CASE WHEN p_SLA = 0 THEN "SC"."SLA" ELSE p_SLA  END) AND 
		("SC"."EstadoSolicitud" =  CASE WHEN p_Estado = '' THEN "SC"."EstadoSolicitud" ELSE p_Estado END) 
		;
				
END;

