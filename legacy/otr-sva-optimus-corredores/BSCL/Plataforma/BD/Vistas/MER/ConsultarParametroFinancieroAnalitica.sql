SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinancieroAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinancieroAnalitica" AS
SELECT 
        PF."IdParametroFinanciero",
        TO_VARCHAR(PF."Fecha",'YYYY-MM-DD') AS "Fecha",
        DPF."Periodo",
        DPF."IdMonedaParametro",
        DPF."Nombre",
        DPF."IdMonedaValor",
        PF."AgnoMes", 
        PF."Agno",    
        PF."Valor",   
        PF."FechaActualizacion"
    FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
    INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DPF
    ON PF."IdParametroFinanciero" = DPF."IdParametroFinanciero"
    WHERE (DPF."Periodo" = 'ME' AND PF."AgnoMes" != '') OR 
    (DPF."Periodo" = 'DI' AND PF."Fecha" != '0001-01-01') OR
    (DPF."Periodo" = 'AN' AND PF."Agno" != '');

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinancieroAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
