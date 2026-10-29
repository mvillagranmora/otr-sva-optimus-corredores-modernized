SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConvertirMensajesReglas";

CREATE PROCEDURE
-- 
--  Proposito: Convierte los mensajes de las reglas a mensajes de salida de servicios
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ConvertirMensajesReglas" (
	 IN p_out_ResultadoMatriz	Table("idRegla"         bigint,         --valor de configuracion de la regla para el producto
	 							  "Habilitado"          nvarchar(1),    --valor de configuracion de la regla para el producto
	 							  "CodComportamiento"   nvarchar(40),   --valor de configuracion de la regla para el producto
	 							  "ResultadoEvaluacion" integer,        --Valor que retorna la funcion de regla
	 							  "ReglaActivada" 	 	integer,        --En base al comportamiento indica si se activa
	 							  "MensajeRegla"        nvarchar(520)   --Mensaje de la regla
	 							  ),					 							 
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS
BEGIN
	declare CURSOR c_c1 FOR SELECT *  FROM :p_out_ResultadoMatriz;
	p_out_AdvertenciasDetalle=select '-' as "Codigo",'-' as "RequiereAutorizacion",'-' as "Descripcion" from dummy where (1=2);
	p_out_ErroresDetalle=select '-' as "Codigo",'-' as "Descripcion" from dummy where (1=2);
	FOR reg AS c_c1 DO
		if reg."Habilitado"='S' and reg."ReglaActivada"=1 then
			if reg."CodComportamiento"='ADVE' 
			then
				p_out_AdvertenciasDetalle = select cast(reg."idRegla" as nvarchar) as "Codigo",'N' as "RequiereAutorizacion",reg."MensajeRegla" as "Descripcion" from dummy UNION ALL
				                             select * from :p_out_AdvertenciasDetalle;
			elseif reg."CodComportamiento"='AUTH' or reg."CodComportamiento"='SAUT'
			then
				p_out_AdvertenciasDetalle = select cast(reg."idRegla" as nvarchar) as "Codigo",'S' as "RequiereAutorizacion",reg."MensajeRegla" as "Descripcion" from dummy UNION ALL
				                             select * from :p_out_AdvertenciasDetalle;				
			elseif reg."CodComportamiento"='DETO' or reg."CodComportamiento"='SDET'
			then
				p_out_ErroresDetalle = select cast(reg."idRegla" as nvarchar) as "Codigo",reg."MensajeRegla" as "Descripcion" from dummy UNION ALL
				                       select * from :p_out_ErroresDetalle;								
			end if;
		end if;
	END FOR;		
 
END;
