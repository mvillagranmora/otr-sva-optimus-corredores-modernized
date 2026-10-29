SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" as

	SELECT 
	   "IdTipoIndicador",
	   "IdTipoEntidad" ,
	   "IdEmpresa",
	   "ValorIndicadorDefecto",
	   "ClaseDato",
	   "NombreTipoIndicador",
	   "CodigoTipoIndicador",
	   "GrupoIndicador",
	   "IdUso", -- IND: Indicador, REQ: Requisito
	   "IdEstado",
	   "RequiereAutorizacion",
	   "CantidadDecimales",
	   "LongitudMinima",
	   "LongitudMaxima",
	   "IdTipoPersona",
	   "RequiereFechaVencimiento"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 

 
