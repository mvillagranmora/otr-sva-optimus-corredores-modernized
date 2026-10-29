SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ConsultarLogNormativo";

--call"BSCL.Normativa.BD.Servicios.NOR::ConsultarLogNormativo"('2016-10-26',0,10)


CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ConsultarLogNormativo"
( 
    -- Parametros de Entrada

	--IN 	p_Fecha             	NVARCHAR(10) default '',
	IN 	p_IdProceso         	BIGINT default 0,
	IN 	p_IdSolicitudCircular   BIGINT default 0

    -- Salida
   -- OUT p_out_resultado						  
   
  
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
  declare v_LogCondicion BIGINT DEFAULT 0;
  
--------------------
--LOGICA
--------------------

select count(1)  into v_LogCondicion  from "BSCL.Normativa.BD::NOR.LogNormativa"
where "IdProceso" = p_IdProceso and "IdSolicitudCircular" = p_IdSolicitudCircular;

IF(v_LogCondicion>0)
THEN


SELECT 
--"IdLogNormativa",
--"IdTipoLogNormativa",
"IdUsuario",
"FechaLog",
"IdProceso",
"IdSolicitudCircular",
"IdOperacion",
"Registro",
"Observacion"
FROM "BSCL.Normativa.BD::NOR.LogNormativa" AS NOR 
--where  "FechaLog" = p_Fecha and
where "IdProceso" = case when  (p_IdProceso <> 0) then p_IdProceso else "IdProceso" end
and "IdSolicitudCircular" = case when (p_IdSolicitudCircular <> 0) then p_IdSolicitudCircular else "IdSolicitudCircular" end
ORDER BY "IdLogNormativa" DESC;

ELSE
select 
--'-' as "IdLogNormativa",
--'-' as "IdTipoLogNormativa",
'-' as "IdUsuario",
'-'as "FechaLog",
'-' as "IdProceso",
'-' as "IdSolicitudCircular" ,
'-' as "IdOperacion" ,
'-' as "Registro" ,
'Sin Datos Registrados' as "Observacion"
from dummy where 1=2;



END IF;



	
END;

