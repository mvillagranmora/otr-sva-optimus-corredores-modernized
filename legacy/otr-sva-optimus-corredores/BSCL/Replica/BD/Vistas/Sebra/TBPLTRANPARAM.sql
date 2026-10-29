SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTRANPARAM"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTRANPARAM" AS

SELECT
		C."CodigoInscripcionRegulador" AS CODCORR
		, CO."CodigoOperador" AS CODOPE
		, COALESCE(P."CodigoPortafolio", '0') AS SEC_CLI
		, COALESCE(CLI."IdPersona" , 0) AS PER_ID
		, 'N' AS GASTOS
		, 'N' AS COMISION
		, 'N' AS DERECHO
		, 'C' AS TITULOS
		, 'N' AS GEN_MOVTO_FAC_RV
		, 'N' AS GEN_MOVTO_FAC_RF
		, 'N' AS GEN_MOVTO_FAC_IF
		, CAST(CO."ReservaTransaccion" AS VARCHAR(1)) AS RESERVAR
		, 'N' AS ANULA_IND_RUTEO
	FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" CO
	INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" C ON C."IdCorredora" = CO."IdCorredora"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = CO."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = P."IdCliente"
	WHERE "CodigoOperador" NOT LIKE_REGEXPR '[A-Z]'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLTRANPARAM" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
