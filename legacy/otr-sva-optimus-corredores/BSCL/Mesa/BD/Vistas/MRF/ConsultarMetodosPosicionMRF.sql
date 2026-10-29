--* ------------------------------------------------------------------------
--* Nombre .....: ConsultarMetodosPosicionMRF
--* Modulo .....: Mesa RF
--* F. Creación : Noviembre 2016
--* Proposito ..: Consultar los metodos de calculos para la Posición MRF
--* ------------------------------------------------------------------------

SET SCHEMA "BSCL"; 
--* ------------------------------------------------------------------------

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarMetodosPosicionMRF";
--* ------------------------------------------------------------------------

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarMetodosPosicionMRF" AS
	SELECT 	POS.*, COALESCE(MON."CantidadDecimales", 0) AS "CantidadDecimales"
	FROM 	"BSCL.Mesa.BD::MES.MetodoPosicion" POS
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MON
				 ON  MON."IdMoneda" = POS."IdTipoReajuste" AND "IdUsoMonedaFormato" = 'MES' AND "IdTipoInstrumento" = ''
	WITH 	READ ONLY;		
	
--* ------------------------------------------------------------------------
GRANT 	SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarMetodosPosicionMRF"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

		
		
		
