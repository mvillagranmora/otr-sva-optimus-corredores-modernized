SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarArbolClasificacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarArbolClasificacion" AS
    SELECT clasif."IdArbolClasificacionActivo" as "IdCalsificacionActivo", 
    	clasif."Descripcion" as "DescripcionClasificacionActivo" , 
    	subMerc."Descripcion" as "SubMercado", 
    	merca."Descripcion" as "Mercado" 
    FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" clasif  
		INNER JOIN   "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" subMerc ON ( subMerc."IdArbolClasificacionActivo" = clasif."Padre" and subMerc."Padre" != 0)
		INNER JOIN   "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" merca ON (merca."IdArbolClasificacionActivo" = subMerc."Padre" and merca."Padre" = 0 )
	WHERE clasif."Padre" != 0;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarArbolClasificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
