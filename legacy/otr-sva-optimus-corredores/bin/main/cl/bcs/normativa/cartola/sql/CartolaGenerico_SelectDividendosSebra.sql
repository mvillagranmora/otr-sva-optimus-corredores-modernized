 SELECT  por."IdPortafolio",sum(MONTO) AS "Monto",TRIM(NEMO) AS "Nemotecnico"
FROM "BSCL.Normativa.BD::CAR.TBCTMVTO" a
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" por ON por."DocumentoIdentidad" = TRIM(A.RUT_CLI)
	AND to_varchar(A.SEC_RUT_CLI) = por."CodigoPortafolio"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" B ON por."IdPortafolio" = B."IdPortafolio"
where cod_movto = 'DIPE' and fec_mvto >= :FechaDesde and fec_mvto <= :FechaHasta
GROUP BY por."IdPortafolio",NEMO;