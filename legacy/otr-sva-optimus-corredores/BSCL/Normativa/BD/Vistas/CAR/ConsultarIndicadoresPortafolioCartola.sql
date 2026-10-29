SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarIndicadoresPortafolioCartola";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarIndicadoresPortafolioCartola" AS
SELECT  "IdPortafolio"
        ,MAX(CASE WHEN "IdUso" = 'PRE' AND "CodigoTipoIndicador" = 'CAR_PERIOD' THEN "CodigoValorIndicador" ELSE null END) AS "Periodicidad"
        ,MAX(CASE WHEN "IdUso" = 'PRE' AND "CodigoTipoIndicador" = 'CAR_COSECARTIMP' THEN "ValorIndicador" ELSE null END) AS "ContratoServicioCartola"
FROM (

    SELECT "IdPortafolio","IdUso","CodigoTipoIndicador","IdIndicador","ValorIndicador", "CodigoValorIndicador"
    FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
GROUP BY "IdPortafolio"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarIndicadoresPortafolioCartola" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
