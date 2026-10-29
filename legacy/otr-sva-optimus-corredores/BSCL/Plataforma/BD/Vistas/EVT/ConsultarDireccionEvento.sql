SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento"  
AS
 
 SELECT tipoevt."CodigoTipoEvento", direvt."DireccionEvento","TipoEntidadOrigen","IdEntidadOrigen","TipoEntidadDestino","IdEntidadDestino"
 FROM 
	 (
	 select 'SYSEMP' as "DireccionEvento", 'SYS' as "TipoEntidadOrigen", 1 as "IdEntidadOrigen",'EMP' as "TipoEntidadDestino", 1 as "IdEntidadDestino" from dummy
	  UNION ALL 
	 select 'EMPCLI' as "DireccionEvento", 'EMP' as "TipoEntidadOrigen", 2 as "IdEntidadOrigen",'CLI' as "TipoEntidadDestino", 2 as "IdEntidadDestino" from dummy
	   UNION ALL 
	 select 'CLIEMP' as "DireccionEvento", 'CLI' as "TipoEntidadOrigen", 3 as "IdEntidadOrigen",'EMP' as "TipoEntidadDestino", 1 as "IdEntidadDestino" from dummy
	 	UNION ALL
	 select 'EMPEMP' as "DireccionEvento", 'EMP' as "TipoEntidadOrigen", 2 as "IdEntidadOrigen",'EMP' as "TipoEntidadDestino", 1 as "IdEntidadDestino" from dummy
	  	UNION ALL
	 select 'EMPPOR' as "DireccionEvento", 'EMP' as "TipoEntidadOrigen", 2 as "IdEntidadOrigen",'POR' as "TipoEntidadDestino", 3 as "IdEntidadDestino" from dummy
	 ) as direvt
 INNER JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" tipoevt
 ON direvt."DireccionEvento" = tipoevt."DireccionEvento";
 
 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


