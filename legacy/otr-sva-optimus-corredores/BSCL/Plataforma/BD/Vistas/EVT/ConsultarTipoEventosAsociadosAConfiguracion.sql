SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion"  
AS
SELECT 
tipo."CodigoTipoEvento",
CASE "DireccionEvento" 
	WHEN 'SYSEMP' THEN 'Del Sistema al Empleado'
	WHEN 'EMPCLI' THEN 'Del Empleado al Cliente'
	WHEN 'EMPEMP' THEN 'Del Empleado al Empleado'
	WHEN 'CLIEMP' THEN 'Del cliente al Empleado'
	WHEN 'EMPPOR' THEN 'Del Empleado al Portafolio de Cliente'	
END AS "DireccionEvento",
"DireccionEvento" as "DirEvt",
tipo."Descripcion" as "DescripcionTipoEvento",
ce."IdConfiguracionEvento",
ce."Descripcion" as "DescripcionConfiguracion",
"IdConfiguracionEventoDetalle",
ce."FechaCreacion",
"IdPlantilla",
MAP(configdet."IdEstado",'ACT','UI_ACT','UI_INA') as "IdEstadoDetalle",
MAP(ce."IdEstado",'ACT','UI_ACT','UI_INA') as "IdEstado"
FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" configdet
INNER JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" tipo
on tipo."CodigoTipoEvento" = configdet."CodigoTipoEvento" 
INNER JOIN  "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce
ON ce."IdConfiguracionEvento" = configdet."IdConfiguracionEvento";



GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
