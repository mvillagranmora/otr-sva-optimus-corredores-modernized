SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoCombo"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoCombo"  
AS
SELECT 
"CodigoTipoEvento",
CASE "DireccionEvento" 
	WHEN 'SYSEMP' THEN 'Del Sistema al Empleado'
	WHEN 'EMPCLI' THEN 'Del Empleado al Cliente'
	WHEN 'EMPPOR' THEN 'Del Empleado al Portafolio'
	WHEN 'EMPEMP' THEN 'Del Empleado al Empleado'
	WHEN 'CLIEMP' THEN 'Del cliente al Empleado'
END AS "DireccionEvento",
"Descripcion"
  
FROM "BSCL.Plataforma.BD::EVT.TipoEvento"
UNION ALL
SELECT 'TagGenerales',
'',
'Tag Predeterminados'
FROM DUMMY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoCombo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 