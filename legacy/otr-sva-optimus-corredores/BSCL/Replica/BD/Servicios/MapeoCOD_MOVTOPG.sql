CREATE OR REPLACE FUNCTION "BSCL.Replica.BD.Servicios::MapeoCOD_MOVTO"(
    IdTipoOperacion  VARCHAR(6),              -- antes NVARCHAR(6)
    IdTipoProducto   VARCHAR(4),              -- antes NVARCHAR(4)
    EsAPlazo         VARCHAR(1) DEFAULT 'N',  -- antes NVARCHAR(1)
    IdFormaCustodia  VARCHAR(3) DEFAULT ''    -- antes NVARCHAR(3)
)
RETURNS VARCHAR(11)
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT CASE
        WHEN IdTipoOperacion IN ('COMACN','COMCFI','COMETN','COMACX','COMCFX','COMETX','COMOSA') THEN 'CIRV'
        WHEN IdTipoOperacion IN ('VENACN','VENCFI','VENETN','VENACX','VENCFX','VENETX','VENOSA') THEN 'VIRV'
        WHEN IdTipoOperacion IN ('ANCACN','ANCCFI','ANCETN','ANCACX','ANCCFX','ANCETX','ANCOSA') THEN 'NRVC'
        WHEN IdTipoOperacion IN ('ANVACN','ANVCFI','ANVETN','ANVACX','ANVCFX','ANVETX','ANVOSA') THEN 'NRVV'
        WHEN IdTipoOperacion = 'COMMON' THEN 'COMO'
        WHEN IdTipoOperacion = 'VENMON' THEN 'VIMO'
        WHEN IdTipoOperacion = 'COMRF'  THEN 'CIRF'
        WHEN IdTipoOperacion = 'ANCRF'  THEN 'NRFC'
        WHEN IdTipoOperacion = 'VENRF'  THEN 'VIRF'
        WHEN IdTipoOperacion = 'ANVRF'  THEN 'NRFV'  -- duplicado en original línea 60, ignorado
        WHEN IdTipoOperacion = 'COMIIF' THEN 'CIIF'
        WHEN IdTipoOperacion = 'ANCIIF' THEN 'NIFC'
        WHEN IdTipoOperacion = 'VENIIF' THEN 'VIIF'
        WHEN IdTipoOperacion = 'ANVIIF' THEN 'NIFV'
        WHEN IdTipoOperacion = 'VENSPT' THEN 'VFRD'
        WHEN IdTipoOperacion = 'COMSPT' THEN 'CFRD'
        WHEN IdTipoOperacion = 'COMSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'N' THEN 'CIRV'
        WHEN IdTipoOperacion = 'COMSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'S' THEN 'CITP'
        WHEN IdTipoOperacion = 'VENSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'N' THEN 'VIRV'
        WHEN IdTipoOperacion = 'VENSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'S' THEN 'VITP'
        WHEN IdTipoOperacion = 'COMOTC' THEN 'FCOT'
        WHEN IdTipoOperacion = 'VENOTC' THEN 'FVOT'
        -- AND tiene mayor precedencia que OR: 'ANVSIM' siempre entra aquí; 'ANSIMV' solo si cumple custodia
        WHEN IdTipoOperacion = 'ANVSIM' OR (IdTipoOperacion = 'ANSIMV' AND (IdFormaCustodia = '' OR IdFormaCustodia = 'CUS')) THEN 'NCTP'
        WHEN IdTipoOperacion = 'ANVSIM' OR (IdTipoOperacion = 'ANSIMV' AND IdFormaCustodia = 'NOM')                           THEN 'MCTP'
        WHEN IdTipoOperacion = 'ANCSIM' OR (IdTipoOperacion = 'ANSIMC' AND (IdFormaCustodia = '' OR IdFormaCustodia = 'CUS')) THEN 'NVTP'
        WHEN IdTipoOperacion = 'ANCSIM' OR (IdTipoOperacion = 'ANSIMC' AND IdFormaCustodia = 'NOM')                           THEN 'MVTP'
        WHEN IdTipoOperacion = 'ANCSPT' THEN 'NCOT'
        WHEN IdTipoOperacion = 'ANVSPT' THEN 'NVOT'
        WHEN IdTipoOperacion = 'ANCOTC' THEN 'NCOT'
        WHEN IdTipoOperacion = 'ANVOTC' THEN 'NVOT'
        WHEN IdTipoOperacion = 'SUSOSA' THEN 'CIRV'
        ELSE 'XXXX'
    END;
$$;

-- Ejemplo de llamada
-- SELECT "BSCL.Replica.BD.Servicios::MapeoCOD_MOVTO"('COMACN', '', 'N', '');
