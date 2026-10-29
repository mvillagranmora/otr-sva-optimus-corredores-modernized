SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMonedaPuntas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMonedaPuntas"
AS 
	SELECT	*
	FROM	"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"
	WHERE	"IdEstado" = 'ACT'
			AND "IdMoneda" IN 
				 ( 	SELECT	 DISTINCT "IdMonedaPrincipal" 
				 	FROM 	"BSCL.Mesa.BD::MES.InstrumentoMoneda" 
				 	WHERE 	"IdTipoProducto" = 'MEME' AND "IdEstado" = 'ACT');

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarMonedaPuntas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

