SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMatrizRiesgo";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMatrizRiesgo"
AS 

SELECT MatrizRiesgo."IdEmpresa"
	 , MatrizRiesgo."IdMatrizRiesgo"
 	 , MatrizRiesgo."IdTipoProducto"
	 , MatrizRiesgo."IdInstrumento"
	 , MatrizRiesgo."IdTipoOperacion"
	 , TipoOperacion."Accion"
 	 , MatrizRiesgo."IdMonedaPrincipal"
 	 , MatrizRiesgo."IdMonedaSecundaria"
 	 , MatrizRiesgo."Plazo"
 	 , MatrizRiesgo."Porcentaje"
  FROM "BSCL.Mesa.BD::FWD.MatrizRiesgo" AS MatrizRiesgo 
  INNER JOIN "BSCL.Mesa.BD::MES.InstrumentoMoneda" AS InstrumentoMoneda
  		  On InstrumentoMoneda."IdInstrumento" = MatrizRiesgo."IdInstrumento"
  		 AND InstrumentoMoneda."IdMonedaPrincipal" = MatrizRiesgo."IdMonedaPrincipal"
  		 AND InstrumentoMoneda."IdMonedaSecundaria" = MatrizRiesgo."IdMonedaSecundaria"
  		 AND InstrumentoMoneda."IdTipoProducto" = MatrizRiesgo."IdTipoProducto"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacion
  			   ON TipoOperacion."IdTipoOperacion" = MatrizRiesgo."IdTipoOperacion";
  		   
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarMatrizRiesgo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 